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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import viabilitycommonclasses.Activity;
import viabilitycommonclasses.Segnalazione;

/**
 *
 * @author Alessio_Gregory_Ricky
 */
public class TransactionManagerThread implements Runnable {

    /**
     * Coda relativa alla comunicazione con ufficio numero 1
     */
    private Queue office1;
    /**
     * Coda relativa alla comunicazione con ufficio numero 2
     */
    private Queue office2;
    /**
     * Coda relativa alla comunicazione con ufficio numero 3
     */
    private Queue office3;
    /**
     * Coda relativa alla comunicazione con ufficio numero 4
     */
    private Queue office4;
    /**
     * Coda relativa alla comunicazione con ufficio numero 5
     */
    private Queue office5;
    
    /**
     * Contesto relativo alla comunicazione con l'ufficio 1
     */
    private JMSContext context1;
    /**
     * Contesto relativo alla comunicazione con l'ufficio 2
     */
    private JMSContext context2;
    /**
     * Contesto relativo alla comunicazione con l'ufficio 3
     */
    private JMSContext context3;
    /**
     * Contesto relativo alla comunicazione con l'ufficio 4
     */
    private JMSContext context4;
    /**
     * Contesto relativo alla comunicazione con l'ufficio 5
     */
    private JMSContext context5;
    /**
     * Producer per comunicare con l'ufficio 1
     */
    private JMSProducer producer1;
    /**
     * Producer per comunicare con l'ufficio 2
     */
    private JMSProducer producer2;
    /**
     * Producer per comunicare con l'ufficio 3
     */
    private JMSProducer producer3;
    /**
     * Producer per comunicare con l'ufficio 4
     */
    private JMSProducer producer4;
    /**
     * Producer per comunicare con l'ufficio 5
     */
    private JMSProducer producer5;
    /**
     * Attivita' da processare
     */
    private Activity activity;
    /**
     * Numero del Thread nel pool
     */
    private long numero = 0;
    /**
     * Writer per scrivere un log anche sul file
     */
    private PrintWriter out;
    /**
     * Indica se parliamo del TM manager 1 oppure TM manager 2
     */
    private String args;
    
    /**
     * Funzione che serve ad inizializzare tutto quello che serve per creare log su file 
     * delle attivita eseguite
     * @param args indica se siamo nel caso TM manager 1 oppure 2
     */
    private void initOut() {
        File directory = new File("TM"+args);
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
     * Crea un nuovo TM che implementa Runnable
     * @param args indica se si parla del TM manager 1 oppure TM manager 2
     * @param activity attivita da processare
     * @param context1 contesto relativo alla comunicazione con l'ufficio 1
     * @param context2 contesto relativo alla comunicazione con l'ufficio 2
     * @param context3 contesto relativo alla comunicazione con l'ufficio 3
     * @param context4 contesto relativo alla comunicazione con l'ufficio 4
     * @param context5 contesto relativo alla comunicazione con l'ufficio 5
     * @param office1 coda per la comunicazione con l'ufficio 1
     * @param office2 coda per la comunicazione con l'ufficio 2
     * @param office3 coda per la comunicazione con l'ufficio 3
     * @param office4 coda per la comunicazione con l'ufficio 4
     * @param office5 coda per la comunicazione con l'ufficio 5
     */
    public TransactionManagerThread(String args,Activity activity, JMSContext context1, JMSContext context2, JMSContext context3, JMSContext context4, JMSContext context5, Queue office1, Queue office2, Queue office3, Queue office4, Queue office5) {
        this.args=args;
        this.context1 = context1;
        this.context2 = context2;
        this.context3 = context3;
        this.context4 = context4;
        this.context5 = context5;
        producer1 = context1.createProducer();
        producer2 = context2.createProducer();
        producer3 = context3.createProducer();
        producer4 = context4.createProducer();
        producer5 = context5.createProducer();
        this.office1 = office1;
        this.office2 = office2;
        this.office3 = office3;
        this.office4 = office4;
        this.office5 = office5;
        this.activity = activity;

    }

    /**
     * Metodo run proveniente dall'interfaccia Runnable, esegue tutte le operazioni richieste e ritorna
     * la risposta in regime transazionale.
     */
    @Override
    public void run() {
        initOut();
        numero = Long.parseLong(((Thread.currentThread().getName()).split("pool-[0-9]*-thread-"))[1]);
        producer1.setJMSCorrelationID("TM" + numero);
        producer2.setJMSCorrelationID("TM" + numero);
        producer3.setJMSCorrelationID("TM" + numero);
        producer4.setJMSCorrelationID("TM" + numero);
        producer5.setJMSCorrelationID("TM" + numero);
        sleep(1500);
        if (activity != null) {
            LinkedBlockingQueue<Segnalazione> list = activity.getListsignal();
            try {
                for (Segnalazione s : list) {
                    sendPrepareAndready(s, 2000);
                    ////sleep(100);
                }
                for (Segnalazione s : list) {
                    Print("Invio conferma: " + s.getOfficeID() + " ");
                    sendOfficeMessage(s, "OK");
                    //sleep(100);
                    sendSignalOffice(s);
                    //sleep(100);
                }
                for (Segnalazione s : list) {
                    receiveLocalCommit(s);
                    //sleep(100);
                }
                for (Segnalazione s : list) {
                    Print("Invio global decision commit: ");
                    sendOfficeMessage(s, "commit");
                    //sleep(100);
                }
                for (Segnalazione s : list) {
                    boolean flag = true;
                    while (flag) {
                        flag = false;
                        try {
                            receiveAck(s, 2000);
                            //sleep(100);
                        } catch (Exception e) {
                            Println("Thread" + numero + ":" + e.getMessage());
                            Print("Reinvio global decision commit: ");
                            sendOfficeMessage(s, "commit");
                            //sleep(100);
                            flag = true;
                        }
                    }
                }
                Main.sendMessage("COMMIT", args+activity.getId());
                return;
            } catch (Exception ex) {
                Println("Thread" + numero + ":" + ex.getMessage());
                for (Segnalazione s : list) {
                    Print("Invio global decision Abort: ");
                    sendOfficeMessage(s, "ABORT");
                    //sleep(100);
                }
                Main.sendMessage("ABORT", args+activity.getId());
                sleep(5000);
//                Main.addActivity(activity);
            }

        }
    }

    /**
     * Manda una richiesta di prepare e attende la risposta di ready dallo specifico ufficio indicato
     * nella segnalazione
     * @param s Segnalazione
     * @throws Exception Sollevata quando la risorsa non e' disponibile e non puo' essere bloccata
     */
    private void sendPrepareAndready(Segnalazione s) throws Exception {
        sendPrepareAndready(s, 0);
    }
    /**
     * Manda una richiesta di prepare e attende la risposta di ready dallo specifico ufficio indicato
     * nella segnalazione
     * @param s Segnalazione
     * @param millisec tempo di attesa della risorsa dopo di che si abbortisce la transazione.
     * @throws Exception Sollevata quando la risorsa non e' disponibile e non puo' essere bloccata
     */
    private void sendPrepareAndready(Segnalazione s, long millisec) throws Exception {
        if (s.getOfficeID().equalsIgnoreCase("A"+args+"1")) {
            sendMessage(producer1, office1, "PREPARE");
            Println("Thread" + numero + ":" + "PREPARE inviata a "+"A"+args+"1");
            //sleep(100);
            String txt = (String) receiveMessage(context1, office1, millisec);
            if (txt == null || !txt.equalsIgnoreCase("READY")) {
                throw new Exception("Risorsa Non disponibile al momento");
            }
            Println("Thread" + numero + ":" + "READY ricevuta da "+"A"+args+"1");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"2")) {
            sendMessage(producer2, office2, "PREPARE");
            Println("Thread" + numero + ":" + "PREPARE inviata a "+"A"+args+"2");
            //sleep(100);
            String txt = (String) receiveMessage(context2, office2, millisec);
            if (txt == null || !txt.equalsIgnoreCase("READY")) {
                throw new Exception("Risorsa Non disponibile al momento");
            }
            Println("Thread" + numero + ":" + "READY ricevuta da "+"A"+args+"2");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"3")) {
            sendMessage(producer3, office3, "PREPARE");
            Println("Thread" + numero + ":" + "PREPARE inviata a "+"A"+args+"3");
            //sleep(100);
            String txt = (String) receiveMessage(context3, office3, millisec);
            if (txt == null || !txt.equalsIgnoreCase("READY")) {
                throw new Exception("Risorsa Non disponibile al momento");
            }
            Println("Thread" + numero + ":" + "READY ricevuta da "+"A"+args+"3");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"4")) {
            sendMessage(producer4, office4, "PREPARE");
            Println("Thread" + numero + ":" + "PREPARE inviata a "+"A"+args+"4");
            //sleep(100);
            String txt = (String) receiveMessage(context4, office4, millisec);
            if (txt == null || !txt.equalsIgnoreCase("READY")) {
                throw new Exception("Risorsa Non disponibile al momento");
            }
            Println("Thread" + numero + ":" + "READY ricevuta da "+ "A"+args+"4");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"5")) {
            sendMessage(producer5, office5, "PREPARE");
            Println("Thread" + numero + ":" + "PREPARE inviata a "+"A"+args+"5");
            //sleep(100);
            String txt = (String) receiveMessage(context5, office5, millisec);
            if (txt == null || !txt.equalsIgnoreCase("READY")) {
                throw new Exception("Risorsa Non disponibile al momento");
            }
            Println("Thread" + numero + ":" + "READY ricevuta da "+"A"+args+"5");
        }
    }

    /**
     * Mandare un messaggio allo specifico ufficio
     * @param producer producer per mandare il messaggio su una specifica coda agganciata allo specifico ufficio
     * @param queue coda nella quale produrre lo specifico messaggio
     * @param obj oggetto da mandare sulla coda
     */
    private void sendMessage(JMSProducer producer, Queue queue, Serializable obj) {
        Destination dest = (Destination) queue;
        producer.send(dest, obj);
    }

    /**
     * Riceve il messaggio da una coda specifica, messaggio mandato da un ufficio specifico
     * @param context contesto per la comunicazione con lo specifico ufficio
     * @param office coda sulla quale ricevere il messaggio
     * @return oggetto ricevuto tramite dalla coda.
     */
    private Serializable receiveMessage(JMSContext context, Queue office) {
        return receiveMessage(context, office, 0);
    }
    /**
     * Riceve il messaggio da una coda specifica, messaggio mandato da un ufficio specifico
     * @param context contesto per la comunicazione con lo specifico ufficio
     * @param office coda sulla quale ricevere il messaggio
     * @param millisec tempo dopo il quale viene restituito null se non arriva alcun messaggio
     * @return oggetto ricevuto tramite dalla coda.
     */
    private Serializable receiveMessage(JMSContext context, Queue office, long millisec) {
        JMSConsumer consumer = context.createConsumer(office, "JMSCorrelationID = 'RM" + numero + "'");

        Message messaggio = consumer.receive(millisec);
        ObjectMessage obj = null;
        if (messaggio != null) {
            if (messaggio instanceof ObjectMessage) {
                try {
                    obj = (ObjectMessage) messaggio;
                    consumer.close();
                    return obj.getObject();
                } catch (JMSException ex) {
                    ex.printStackTrace(out);
                    consumer.close();
                    return null;
                }
            }

        }
        consumer.close();
        return null;
    }

    /**
     * Mandare un messaggio allo specifico ufficio contenuto nella segnalazione
     * @param s Segnalazione
     * @param message Messaggio da inviare
     */
    private void sendOfficeMessage(Segnalazione s, Serializable message) {
        if (s.getOfficeID().equalsIgnoreCase("A"+args+"1")) {
            sendMessage(producer1, office1, message);
            Println("Thread" + numero + ":" + "Messaggio inviato a "+"A"+args+"1");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"2")) {
            sendMessage(producer2, office2, message);
            Println("Thread" + numero + ":" + "Messaggio inviato a "+"A"+args+"2");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"3")) {
            sendMessage(producer3, office3, message);
            Println("Thread" + numero + ":" + "Messaggio inviato a "+"A"+args+"3");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"4")) {
            sendMessage(producer4, office4, message);
            Println("Thread" + numero + ":" + "Messaggio inviato a "+"A"+args+"4");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"5")) {
            sendMessage(producer5, office5, message);
            Println("Thread" + numero + ":" + "Messaggio inviato a "+"A"+args+"5");
        }
    }

    /**
     * Manda la segnalazione relativa alla specifica strada ad un ufficio
     * @param s Segnalazione
     */
    private void sendSignalOffice(Segnalazione s) {
        Print("INVIO SEGNALAZIONE: " + s.getOfficeID() + " ");
        sendOfficeMessage(s, s);
    }

    /**
     * Riceve il commit del singolo ufficio
     * @param s Segnalazione riguardante l'ufficio
     * @throws Exception Se non si riceve il commit entro un timeOut si abbortisce la transazione
     */
    private void receiveLocalCommit(Segnalazione s) throws Exception {
        if (s.getOfficeID().equalsIgnoreCase("A"+args+"1")) {
            String txt = (String) receiveMessage(context1, office1);
            if (!txt.equalsIgnoreCase("ValidationOk")) {
                throw new Exception("Problema con la transazione deve essere abortita "+"A"+args+"1");
            }
            Println("Thread" + numero + ":" + "Local commit ricevuto da "+"A"+args+"1");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"2")) {
            String txt = (String) receiveMessage(context2, office2);
            if (!txt.equalsIgnoreCase("ValidationOk")) {
                throw new Exception("Problema con la transazione deve essere abortita "+"A"+args+"2");
            }
            Println("Thread" + numero + ":" + "Local commit ricevuto da "+"A"+args+"2");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"3")) {
            String txt = (String) receiveMessage(context3, office3);
            if (!txt.equalsIgnoreCase("ValidationOk")) {
                throw new Exception("Problema con la transazione deve essere abortita "+"A"+args+"3");
            }
            Println("Thread" + numero + ":" + "Local commit ricevuto da "+"A"+args+"3");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"4")) {
            String txt = (String) receiveMessage(context4, office4);
            if (!txt.equalsIgnoreCase("ValidationOk")) {
                throw new Exception("Problema con la transazione deve essere abortita "+"A"+args+"4");
            }
            Println("Thread" + numero + ":" + "Local commit ricevuto da "+"A"+args+"4");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"5")) {
            String txt = (String) receiveMessage(context5, office5);
            if (!txt.equalsIgnoreCase("ValidationOk")) {
                throw new Exception("Problema con la transazione deve essere abortita "+"A"+args+"5");
            }
            Println("Thread" + numero + ":" + "Local commit ricevuto da "+"A"+args+"5");
        }
    }

    /**
     * Riceve Ack di converma dallo specifico Ufficio
     * @param s Segnalazione che riguarda l'ufficio
     * @throws Exception Se non si riceve ack
     */
    private void receiveAck(Segnalazione s) throws Exception {
        receiveAck(s, 0);
    }
    /**
     * Riceve Ack di converma dallo specifico Ufficio
     * @param s Segnalazione che riguarda l'ufficio
     * @param millisec se non si riceve ack entro tot millisec si manda di nuovo il commit
     * @@throws Exception Se non si riceve ack entro un timeOut si rimanda il commit
     */
    private void receiveAck(Segnalazione s, long millisec) throws Exception {
        if (s.getOfficeID().equalsIgnoreCase("A"+args+"1")) {
            String txt = (String) receiveMessage(context1, office1, millisec);
            if (txt == null) {
                throw new Exception("ACK not received");
            }
            Println("Thread" + numero + ":" + txt + " da "+"A"+args+"1");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"2")) {
            String txt = (String) receiveMessage(context2, office2, millisec);
            if (txt == null) {
                throw new Exception("ACK not received");
            }
            Println("Thread" + numero + ":" + txt + " da "+"A"+args+"2");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"3")) {
            String txt = (String) receiveMessage(context3, office3, millisec);
            if (txt == null) {
                throw new Exception("ACK not received");
            }
            Println("Thread" + numero + ":" + txt + " da "+"A"+args+"3");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"4")) {
            String txt = (String) receiveMessage(context4, office4, millisec);
            if (txt == null) {
                throw new Exception("ACK not received");
            }
            Println("Thread" + numero + ":" + txt + " da "+"A"+args+"4");
        } else if (s.getOfficeID().equalsIgnoreCase("A"+args+"5")) {
            String txt = (String) receiveMessage(context5, office5, millisec);
            if (txt == null) {
                throw new Exception("ACK not received");
            }
            Println("Thread" + numero + ":" + txt + " da "+"A"+args+"5");
        }
    }

    /**
     * Attesa di un certo numero di millisecondi
     * @param millisec numero millisecondi
     */
    private void sleep(long millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException ex) {
            ex.printStackTrace(out);
        }
    }

    /**
     * Stampa a video e su file se definito con ritorno accapo.
     * @param txt oggetto da stampare
     */
    public void Println(Object txt) {
        if (out != null) {
            out.println(txt);
        }
        System.out.println(txt);
    }

    /**
     * Stampa a video e su file se definito senza ritorno accapo.
     * @param txt oggetto da stampare
     */
    public void Print(Object txt) {
        if (out != null) {
            out.print(txt);
        }
        System.out.print(txt);
    }

}
