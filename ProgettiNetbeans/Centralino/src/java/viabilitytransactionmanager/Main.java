/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package viabilitytransactionmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import viabilitycommonclasses.Activity;
import viabilitycommonclasses.GlobalParams;

/**
 * Classe principale per la gestione delle transazioni
 * @author Alessio_Gregory_Ricky
 */
public class Main {
    //L'utilizzo di piu' factory potrebbe sembrare superfluo se non fosse per il fatto che un unico factory
    //per tutte le connessioni rallenta parecchio il sistema a differenza di quando abbiamo simulato il sistema con
    //piu' factory che accettano le connessioni.
    
    /**
     * Factory per la connessione con la coda per comunicare con il client
     */
    private static ConnectionFactory connectionFactoryQueue;

    /**
     * Factory per la connessione con la coda per comunicare con ufficio 1
     */
    private static ConnectionFactory connectionFactoryQueue1;

    /**
     * Factory per la connessione con la coda per comunicare con ufficio 2
     */
    private static ConnectionFactory connectionFactoryQueue2;

    /**
     * Factory per la connessione con la coda per comunicare con ufficio 3
     */
    private static ConnectionFactory connectionFactoryQueue3;

    /**
     * Factory per la connessione con la coda per comunicare con ufficio 4
     */
    private static ConnectionFactory connectionFactoryQueue4;

    /**
     * Factory per la connessione con la coda per comunicare con ufficio 5
     */
    private static ConnectionFactory connectionFactoryQueue5;

    /**
     * Coda per la comunicazione con il client
     */
    private static Queue queue;

    /**
     * Coda per la comunicazione con l'ufficio 1
     */
    private static Queue office1;
    /**
     * Coda per la comunicazione con l'ufficio 2
     */
    private static Queue office2;
    /**
     * Coda per la comunicazione con l'ufficio 3
     */
    private static Queue office3;
    /**
     * Coda per la comunicazione con l'ufficio 4
     */
    private static Queue office4;
    /**
     * Coda per la comunicazione con l'ufficio 5
     */
    private static Queue office5;

    /**
     * Coda delle attivita' da eseguire
     */
    private static LinkedBlockingQueue<Activity> listActivity;

    /**
     * Contesti di comunicazione uno per ogni ufficio, visto che abbiamo scelto
     * di creare un connectionfactory per ogni ufficio
     */
    private static JMSContext context1;
    private static JMSContext context2;
    private static JMSContext context3;
    private static JMSContext context4;
    private static JMSContext context5;

    /**
     * Pool di Thread che sono i reali TM che eseguono in regime transazionale
     * le nostre attivita'
     */
    private static ThreadPoolExecutor pool;

    /**
     * Writer per scrivere un log anche sul file
     */
    private static PrintWriter out;

    /**
     * Funzione che serve ad inizializzare tutto quello che serve per creare log su file 
     * delle attivita eseguite
     * @param args indica se siamo nel caso TM manager 1 oppure 2
     */
    private static void initOut(String args) {
        File directory = new File("TM" + args);
        if (!(directory.exists() && directory.isDirectory())) {
            directory.mkdir();
        }
        File fp = new File(directory, Thread.currentThread().getName() + "OutFile.txt");
        if (fp.exists() && fp.isFile()) {
            fp.delete();
        }
        try {
            fp.createNewFile();
            out = new PrintWriter(new FileOutputStream(fp), true);
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }

    /**
     * Aggiunge un attivita alla nostra coda
     * @param activity 
     */
    public static void addActivity(Activity activity) {
        listActivity.add(activity);
    }


    /**
     * Main per la gestione dei TM Thread.
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        listActivity=new LinkedBlockingQueue<Activity>(GlobalParams.numMaxQueueActivity);
        initOut(args[0]);
        lookupResource(args[0]);
        Println("Transtaction Manager instantiate correctly");
        embty();
//        
        pool = new ThreadPoolExecutor(GlobalParams.numMaxQueueActivity, GlobalParams.numMaxQueueActivity, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        // Create TranstactionManagerThread resources
        receiveMessage(args[0]);
        context1 = connectionFactoryQueue1.createContext();
        context2 = connectionFactoryQueue2.createContext();
        context3 = connectionFactoryQueue3.createContext();
        context4 = connectionFactoryQueue4.createContext();
        context5 = connectionFactoryQueue5.createContext();

        while (true) {
            Activity lastactivity = listActivity.take();
            if (lastactivity != null) {
                Println("Start activity procedure:" + lastactivity.toString());
                Println(lastactivity);
                pool.execute(new TransactionManagerThread(args[0], lastactivity, context1, context2, context3, context4, context5, office1, office2, office3, office4, office5));
                Thread.sleep(3000);
            }
        }
    }

    /**
     * Metodo per fare il binding a runtime delle risorse in base ai parametri di ingresso
     * @param args indica se si tratta del TM manager 1 oppure TM manager 2
     */
    private static void lookupResource(String args) {
        Context jndi;
        try {
            jndi = new InitialContext();
            connectionFactoryQueue = (ConnectionFactory) jndi.lookup("jms/ViabilityConnectionFactoryQueue");
            connectionFactoryQueue1 = (ConnectionFactory) jndi.lookup("jms/ViabilityConnectionFactoryQueue1");
            connectionFactoryQueue2 = (ConnectionFactory) jndi.lookup("jms/ViabilityConnectionFactoryQueue2");
            connectionFactoryQueue3 = (ConnectionFactory) jndi.lookup("jms/ViabilityConnectionFactoryQueue3");
            connectionFactoryQueue4 = (ConnectionFactory) jndi.lookup("jms/ViabilityConnectionFactoryQueue4");
            connectionFactoryQueue5 = (ConnectionFactory) jndi.lookup("jms/ViabilityConnectionFactoryQueue5");
            office1 = (Queue) jndi.lookup("jms/OfficeA" + args + "1");
            office2 = (Queue) jndi.lookup("jms/OfficeA" + args + "2");
            office3 = (Queue) jndi.lookup("jms/OfficeA" + args + "3");
            office4 = (Queue) jndi.lookup("jms/OfficeA" + args + "4");
            office5 = (Queue) jndi.lookup("jms/OfficeA" + args + "5");
            queue = (Queue) jndi.lookup("jms/queueTM" + args);
        } catch (NamingException ex) {
            ex.printStackTrace(System.out);
        }
    }

    /**
     * Metodo che serve per agganciare un listener alle richieste che arrivano dal client
     * @param args indica se si tratta del TM manager 1 oppure TM manager 2
     */
    private static void receiveMessage(String args) {
        try {
            Connection connection = connectionFactoryQueue.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(queue, "JMSCorrelationID = 'WB" + args + "'");
            consumer.setMessageListener(new ObjectMessageListener());
            connection.start();
        } catch (JMSException ex) {
            ex.printStackTrace(out);
        }

    }

    /**
     * Stampa a video e su file se definito con ritorno accapo.
     * @param txt oggetto da stampare
     */
    public static void Println(Object txt) {
        if (out != null) {
            out.println(txt);
        }
        System.out.println(txt);
    }

    /**
     * Stampa a video e su file se definito senza ritorno accapo.
     * @param txt oggetto da stampare
     */
    public static void Print(Object txt) {
        if (out != null) {
            out.print(txt);
        }
        System.out.print(txt);
    }

    /**
     * Serve per inviare una risposta al client
     * @param Message Messaggio da inviare al client
     * @param args Specifica se si tratta del client tipo 1 oppure tipo 2 e l'id dell'attivita'.
     */
    public static void sendMessage(Serializable Message, String args) {
        try {
            Connection connection = connectionFactoryQueue.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(queue);
            ObjectMessage message = session.createObjectMessage();
            message.setJMSCorrelationID("Servlet" + args);
            message.setObject(Message);
            producer.send(message);
            System.out.println("Messaggio di risposta Inviato: " + Message);
            producer.close();
            connection.close();
        } catch (JMSException ex) {
            ex.printStackTrace();;
        }
    }

    /**
     * Quando avviene il reset del TM prima di andare a regime si svuota la coda di comunicazione 
     * con il client ecco perche questo metodo ritorna un oggetto presente in coda
     * @return primo oggetto trovato in coda oppure null se assente.
     */
    private static Serializable emptyQueue() {
        JMSContext context = connectionFactoryQueue.createContext();
        JMSConsumer consumer = context.createConsumer(queue);
        Message mex = consumer.receive(1000);
        Serializable obj = null;
        try {
            if ((mex != null) && (mex instanceof ObjectMessage)) {
                obj = ((ObjectMessage) mex).getObject();
            }
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
        finally{
            consumer.close();
        }
        return obj;
    }
    /**
     * Quando avviene il reset del TM prima di andare a regime si svuota la coda di comunicazione 
     * con il client ecco perche questo metodo si occupa di svuotare tutta la coda.
     */
    private static void embty(){
        int i=0;
        Serializable obj=emptyQueue();
        while(obj!=null){
            System.out.println("Estratto : "+i+" "+obj);
            obj=emptyQueue();
            i++;
        }
        System.out.println("Coda vuota");
    }

}
