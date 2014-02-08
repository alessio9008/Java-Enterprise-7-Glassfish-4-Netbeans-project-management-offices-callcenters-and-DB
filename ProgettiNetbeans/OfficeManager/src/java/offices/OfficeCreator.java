
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package offices;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import frontendrm.FrontEndRM;
import java.io.File;

/**
 * Classes that lookup for JMS Resources and instantiates Office.
 * @author Alessio_Gregory_Ricky
 */
public class OfficeCreator {

    //Connection Factory
    //ConnectionFactory for Queues between Office and TM
    private static ConnectionFactory connectionFactoryqueue;
    static JMSContext contextqueue;
    //ConnectionFactory for Topic between Office and Databases
    private static ConnectionFactory connectionFactorytopic;
    static JMSContext contexttopic;

    //JMS Resources
    //JMSTopic used by Office and Databases for request to databases
    private static Topic topic;
    //JMSQueue used by Office and Databases for response from databases
    private static Queue queueRM;

    //JMSQueue used by Office and TM for 2PC alghoritm
    private static Queue queueoffice;


    //ReplicaManager instance
    private static FrontEndRM rm;
    //Office instances
    private static Office office;

    //District ID
    private static String globalidDistrict;
    //Office ID
    private static String globalidOffice;
    
    //Path for logs file
    private static String filePath;

    /***
     * Empty Constructor
     */
    public OfficeCreator() {
    }

    /**
     * Office Creator main method. If you want to instantiate Office11 you have to pass from arguments: "1 1"
     * @param args the command line arguments
     * args[0]= idDistrict
     * args[1]= idOffice
     */
    public static void main(String[] args) {
        try {
            OfficeCreator district = new OfficeCreator();

            lookupResource(args[0],args[1]); 
            //lookupResource("1","1"); 

            prepareFiles();
            //Create Context Resources
            contextqueue = connectionFactoryqueue.createContext();
            contexttopic = connectionFactorytopic.createContext();

            //Create RM instance
            rm = new FrontEndRM(filePath,globalidOffice, contextqueue, contexttopic, topic, queueRM);

            //Create Office Resources
            String idOffice1 = globalidOffice;
            office = new Office(filePath, idOffice1, rm, contextqueue, queueoffice);
            Thread thoffice1 = new Thread(office);
            thoffice1.start();
            thoffice1.join();

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Method that lookup for JMS Resources.
     * @param districtID District ID
     * @param idOffice Office ID
     */
    private static void lookupResource(String districtID,String idOffice) {
        String district;
        String office;
        //Control for null input message
        if (districtID == null) {
            district = "1";
        }
        if(idOffice==null){
            office="1";
        }
        district = districtID;
        office=idOffice;
        Context jndi;
        try {
            jndi = new InitialContext();
            connectionFactoryqueue = (ConnectionFactory) jndi.lookup("jms/ViabilityConnectionFactoryQueue" + district);
            connectionFactorytopic = (ConnectionFactory) jndi.lookup("jms/ViabilityConnectionFactoryTopic" + district);
            topic = (Topic) jndi.lookup("jms/TopicDistrict" + district);
            queueRM = (Queue) jndi.lookup("jms/DBQueueOffice" + office + district);
            queueoffice = (Queue) jndi.lookup("jms/OfficeA" + office + district);
            globalidDistrict = district;
            globalidOffice= "Office"+office+district;

        } catch (NamingException ex) {
            ex.printStackTrace(System.out);
        }
    }

    /***
     * Method that create directory for logs files.
     */
    public static void prepareFiles() {
        //elimina i file relativi a prove precedenti
        filePath = "./District" + globalidDistrict;
        File file = new File(filePath);
        file.mkdir();

//        if (file.isDirectory()) {
//
//            for (File f : file.listFiles()) {
//                if (f.isFile()) {
//                    f.delete();
//                }
//            }
//        }
    }
}
