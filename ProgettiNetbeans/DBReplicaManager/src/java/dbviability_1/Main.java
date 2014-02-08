/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbviability_1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
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
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import lock.LockObject;

/**
 * MAIN class. The main class used to start the RM.
 *
 * @author Alessio_Gregory_Ricky
 */
public class Main {

    //@Resource(mappedName = "jms/ViabilityConnectionFactoryQueue1")
    private static ConnectionFactory connectionFactoryqueue;
    //@Resource(mappedName = "jms/ViabilityConnectionFactoryTopic1")
    private static ConnectionFactory connectionFactorytopic;
    //@Resource(mappedName = "jms/TopicDistrict1")
    private static Topic topic;

    //@Resource(mappedName = "jms/TopicCrash1")
    private static Topic topicCrash;

    //@Resource(lookup = "jms/DBQueueOffice11")
    private static Queue queueOffice1;
    //@Resource(lookup = "jms/DBQueueOffice21")
    private static Queue queueOffice2;

    private static Destination destQueueOffice1 = null;
    private static Destination destQueueOffice2 = null;
    private static JMSProducer producerqueue;
    private static JMSContext context;

    private static String DB_PATH_FILE = "db_stored_file";
    private static String DB_CONFIG_FILE = "db_config_file";

    private static long dbID;
    private static HashMap<Long, String> localDB;

    private static CrashManager crashManager;

    private static String idDistrict;
    
    private static String filePath;
    private static File dbLogFile;
    private static File dbContentsFile;
    
    private static PrintWriter pwLog;
    private static PrintWriter pwCont;
    
    

    /**
     * The main method get the args for the creation of the ID related to the RM.
     * After the Initialization of the Topic used to the Crash Manager, Main code checks for crash thanks to a state file.
     * If a crash is detected, the RM starts the crash procedure. Else, it continue to the Topic and Queues initializations.
     * @param args 
     */
    public static void main(String[] args) {
        idDistrict = args[0];
        lookupResource(idDistrict);
        LockObject lock = new LockObject();
        dbID = (Long.valueOf(idDistrict)) * 10 + (Long.valueOf(args[1]));
        localDB = new HashMap<Long, String>();

        DB_PATH_FILE += String.valueOf(dbID);
        DB_CONFIG_FILE += String.valueOf(dbID);
        
        prepareFile();
        
        //Inizializzo il DB in ogni caso
        printLog("FILL DB");
        fillDB();

        destQueueOffice1 = (Destination) queueOffice1;
        destQueueOffice2 = (Destination) queueOffice2;
        printLog("DB Avviato. ID: " + dbID);

        //Initialization
        printLog("SETTING QUEUE AND TOPIC");
        setConsumerProducer();

        //Check the crash thanks to the file.
        printLog("CHECK CRASH");
        startProcedureCrash(lock);
        if (!checkCrash()) {
            continueInitialization(lock);
        }
        
       showMenu();
       waitForCommand();
    }
    
    /***
     * Method that create directory for logs files.
     */
    public static void prepareDir() {
        //elimina i file relativi a prove precedenti
        filePath = "./District" + idDistrict;
        File file = new File(filePath);
        file.mkdir();
    }
    
    /***
     * Method that create directory for logs files.
     */
    public static void prepareFile(){
        
        prepareDir();
        
        try {
            dbLogFile = new File(filePath+"/RMlogs_"+dbID+".txt");
            dbContentsFile = new File(filePath+"/RMcontents_"+dbID+".txt");
            
            if(dbLogFile.exists()){
                dbLogFile.delete();
            }
            
            if(dbContentsFile.exists()){
                dbContentsFile.delete();
            }
            dbLogFile.createNewFile();
            dbContentsFile.createNewFile();
            
            pwLog = new PrintWriter(dbLogFile);
            
            
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    
    
     /***
      * Method thats write on file and print to standard output a message.
      * @param message message to write and print
      */
    public static void printContent (String message){
            
           
            pwCont.println(message);
            pwCont.flush();
            System.out.println(message);
    }
    
     /***
      * Method thats write on file and print to standard output a message.
      * @param message message to write and print
      */
    public static void printLog(String message){
            
            pwLog.println(message);
            pwLog.flush();
            pwLog.close();
            System.out.println(message);
    }

    /**
     * Method used to retrieve the resource from glassfish.
     * 
     * @param args 
     */
    private static void lookupResource(String args) {
        Context jndi;
        try {
            jndi = new InitialContext();
            connectionFactoryqueue = (ConnectionFactory) jndi.lookup("jms/ViabilityConnectionFactoryQueue" + args);
            connectionFactorytopic = (ConnectionFactory) jndi.lookup("jms/ViabilityConnectionFactoryTopic" + args);
            topic = (Topic) jndi.lookup("jms/TopicDistrict" + args);
            topicCrash = (Topic) jndi.lookup("jms/TopicCrash" + args);
            queueOffice1 = (Queue) jndi.lookup("jms/DBQueueOffice1" + args);
            queueOffice2 = (Queue) jndi.lookup("jms/DBQueueOffice2" + args);

        } catch (NamingException ex) {
            ex.printStackTrace(System.out);
        }
    }
/**
 * This method initialize the asynchronus connection through the creation of a ListenerObject.
 * @param lock 
 */
    private static void startProcedure(LockObject lock) {

        try {
            Connection connection = connectionFactorytopic.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumertopic = session.createConsumer(topic);
            consumertopic.setMessageListener(new ListenerObject(lock, localDB, topic, queueOffice1, queueOffice2, producerqueue, destQueueOffice1, destQueueOffice2, idDistrict));
            connection.start();
        } catch (JMSException ex) {
            ex.printStackTrace();
        }

    }
/**
 * The procedure crash is started after a crash detenction.
 * @param lock 
 */
    private static void startProcedureCrash(LockObject lock) {

        try {
            Connection connection = connectionFactorytopic.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumertopic = session.createConsumer(topicCrash);
            crashManager = new CrashManager(lock, dbID, localDB, topicCrash, connectionFactorytopic.createContext());
            consumertopic.setMessageListener(crashManager);
            connection.start();
        } catch (JMSException ex) {
            ex.printStackTrace();
        }

    }
/**
 * Set the consumer and producer.
 */
    public static void setConsumerProducer() {
        context = connectionFactoryqueue.createContext();
        producerqueue = context.createProducer();

    }
/**
 * Fill the DB with default values.
 */
    public static void fillDB() {
        localDB.put(0l, "buona");
        localDB.put(1l, "buona");
        localDB.put(2l, "buona");
        localDB.put(3l, "buona");
        localDB.put(4l, "buona");
        localDB.put(5l, "buona");
        localDB.put(6l, "buona");
    }
/**
 * The Info daemon provides periodically information about the DB content.
 */
    public static void launchInfoDaemon() {
        Thread thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                Thread.sleep(10000);
                                //printLog("I'm waiting for a DB request");
                                //printLog("DB Contents:");
                                
                                printDB();

                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Print the DB content.
     */
    public static void printDB() {
        FileWriter fw = null;
        try {
            fw = new FileWriter(dbContentsFile,false);
            pwCont = new PrintWriter(fw);
            for (long i = 0; i < localDB.size(); i++) {
                printContent("ID: " + i + " Status: " + localDB.get(i) + "\n");
                
            }   pwCont.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
/**
 * Function used to store DB into a file.
 * @return 
 */
    @Deprecated
    public static boolean storeDB() {
        return storeDB(Main.DB_PATH_FILE);
    }
@Deprecated
    public static boolean storeDB(String path) {
        return storingProcedure(path, localDB);
    }
@Deprecated
    public static void retrieveDB() {
        retrieveDB(Main.DB_PATH_FILE);
    }
@Deprecated
    public static void retrieveDB(String path) {
        Object loaded = retrievingProcedure(path);
        if (loaded != null) {
            localDB = (HashMap<Long, String>) loaded;
        } else {
            printLog("DB not loaded - ERROR");
        }
    }
/**
 * The storing procedure provides a general method to store an object into a file.
 * @param path
 * @param object
 * @return 
 */
    public static boolean storingProcedure(String path, Object object) {
        ObjectOutputStream oos = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(object);
            oos.close();
            return true;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();

        } catch (IOException ex) {
            ex.printStackTrace();

        }
        return false;
    }
/**
 * The retrieving procedure provides a general method to get an object from a file.
 * @param path
 * @param object
 * @return 
 */
    public static Object retrievingProcedure(String path) {
        ObjectInputStream ois = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                ois = new ObjectInputStream(new FileInputStream(file));
                Object objectLoaded = ois.readObject();
                ois.close();
                printLog("Object loaded SUCCESSFUL.");
                return objectLoaded;
            } else {
                printLog("File not found - Can't load");
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
/**
 * Wait for command listen for a command from the System.in.
 */
    public static void waitForCommand() {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader buffer = new BufferedReader(input);

        boolean breaker = false;

        while (!breaker) {
            try {
                String command = buffer.readLine();

                switch (command) {
                    case "help":
                        showMenu();
                        break;
                    case "exit":
                        breaker = true;
                        break;
                    case "about":
                        printLog("ReplicaManager v0.6 - ID: " + dbID + "- Riccardo-Gregory-Alessio");
                        break;
                    default:
                        printLog("command not found. Type 'help' to show menu");
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        quitOperations();
    }
/**
 * A simple menu that shows the avaiable operations.
 */
    private static void showMenu() {
        System.out.println(" - - - - - - - - - - - - - - - - - -");
        System.out.println(" - - ReplicaManager CommandLine - - ");
        System.out.println(" - - - - - - - - - - - - - - - - - -");
        System.out.println(" - - - - - - command list- - - - - -");
        System.out.println(" - - - - - - - - - - - - - - - - - -");
        System.out.println(" - help - show this helper - - - - -");
        System.out.println(" - about - about this process- - - -");
        System.out.println(" - exit - quit this process - - - - ");
        System.out.println(" - - - - - - - - - - - - - - - - - -");
        System.out.println("- - Please, insert a command- - - - ");
        System.out.println(" - - - - - - - - - - - - - - - - - -");
    }
/**
 * The quit operation is a safe mode to exit the DB.
 */
    private static void quitOperations() {
        storingProcedure(DB_CONFIG_FILE, "0");
        printLog("STORED 0 AS QUIT OPERATION");
    }
/**
 * The procedure that grants the corret start of the DB.
 */
    private static void startOperation() {
        storingProcedure(DB_CONFIG_FILE, "1");
        printLog("STORED 1 AS START OPERATION");
    }
/**
 * Tells if a DB is in a consistance status.
 * @return 
 */
    private static String getStatus() {
        Object status = retrievingProcedure(DB_CONFIG_FILE);
        if (status != null) {
            String statusParsed = (String) status;
            return statusParsed;
        }
        return null;
    }
/**
 * Start the Crash procedure. Used only after detenction.
 */
    private static void startCrashProcedure() {
        printLog("Starting crash procedure....");
        crashManager.sendRequestByTopic(dbID);

    }
/**
 * Check if a DB is in crash.
 * @return 
 */
    private static boolean checkCrash() {

        File file = new File(DB_CONFIG_FILE);
        if (!file.exists()) {
            startOperation();
            printLog("[CHECK-CRASH] Nessun crash.");
            return false;
        }

        String status = getStatus();
        if (status.equalsIgnoreCase("1")) {
            printLog("[CHECK-CRASH] Crash RILEVATO.");
            startCrashProcedure();
            return true;
        } else if (status.equalsIgnoreCase("0")) {
            startOperation();
            printLog("[CHECK-CRASH] Nessun crash.");
            return false;
        }
        return false;
    }
/**
 * This method is used after the crash method, to continue the normal initialization.
 * @param lock 
 */
    public static void continueInitialization(LockObject lock) {
        startProcedure(lock);

        //lancia il thread che cicla le info sul contenuto del db
        launchInfoDaemon();

    }

}
