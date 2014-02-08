/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package offices;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.TextMessage;
import frontendrm.FrontEndRM;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import viabilitycommonclasses.Segnalazione;

/**
 * Class that implements Office methods. This class implements also runnable interfaces.
 * @author Alessio_Gregory_Ricky
 */
public class Office implements Runnable {

    //JMSQueue between TM and Office
    private static Queue queue;
    //Destination  for JMSQueue between TM and Office
    private Destination dest = null;
    //JMSContext for JMSQueue between TM and Office
    private JMSContext context;
    //global producer for JMSQueue betweeen TM and Office
    private JMSProducer producer;
    //District FrontEnd object instance
    private FrontEndRM frontend;
    //OfficeID
    private String idOffice;
    //Reception JMSCorrelationID for current served Transiction Manager Thread. When this value is setted
    //no other TM Thread can talk with this Office using 2PC alghoritm.
    private String tmserved = "";
    
    private String filePath;
    private File file;
    private PrintWriter pr;

    /**
     * Constructor for Office instances.
     * @param filePath path for Office logs
     * @param idOffice OfficeID
     * @param frontend District FrontEnd object instance
     * @param context JMSContext for JMSQueue between TM and Office
     * @param queue JMSQueue between TM and Office
     */
    public Office(String filePath,String idOffice, FrontEndRM frontend, JMSContext context, Queue queue) {
        this.filePath=filePath;
        this.idOffice=idOffice;
        this.frontend = frontend;
        this.context = context;
        this.queue = queue;
        this.dest = (Destination) queue;
        prepareFile();
        setConsumerProducer();
        emptyQueue();
        printOnFile("" + idOffice + ": instantiate correctly");
    }

    /**
     * This method deletes instances loaded on JMSQueue.
     */
    public void emptyQueue() {
        printOnFile("Dequeue DBqueue procedure " + idOffice);
        boolean finish = true;
        while (finish) {

            String trashMessage = (String) receiveOnQueue(1000);
            if (trashMessage == null) {
                finish = false;
            }
            printOnFile("Deleted :" + trashMessage);

        }
        printOnFile("Dequeue procedure finished");
    }

    /**
     * Method that creates global producer for JMSQueue betweeen TM and Office.
     */
    public void setConsumerProducer() {
        producer = context.createProducer();

    }

    /**
     * Method that receives prepare message from TM on JMSQueue.
     * @return received object
     */
    public Serializable receivePrepareOnQueue() {
        JMSConsumer consumer = context.createConsumer(dest);
        Message message = consumer.receive();

        ObjectMessage msg = null;
        if (message != null) {
            try {
                //Set served Transiction Manager Thread correlation ID.
                tmserved = message.getJMSCorrelationID();

            } catch (JMSException ex) {
                ex.printStackTrace();
            }
            if (message instanceof ObjectMessage) {
                try {
                    msg = (ObjectMessage) message;
                    consumer.close();
                    return msg.getObject();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                    consumer.close();
                    return null;
                }
            }
        }
        consumer.close();
        return null;
    }


    /**
     * Method that receives prepare message from TM on JMSQueue using Timer.
     * @param millisec time in millisec to attend for a response
     * @return received object
     */
    public Serializable receiveOnQueue(int millisec) {
        //Set Reception JMSCorrelationID for current served Transiction Manager Thread
        JMSConsumer consumer = context.createConsumer(dest, "JMSCorrelationID = '" + tmserved + "'");
        Message message = consumer.receive(millisec);

        ObjectMessage msg = null;
        if (message != null) {
            try {
                printOnFile("TM: " + message.getJMSCorrelationID());
                //If reception JMSCorrelationID for current served Transiction Manager Thread is correct
                if (tmserved.equalsIgnoreCase(message.getJMSCorrelationID())) {

                    if (message instanceof ObjectMessage) {
                        try {
                                 // Comment out the following two lines to receive
                            // a large volume of messages
                            msg = (ObjectMessage) message;
                            consumer.close();
                            return msg.getObject();
                        } catch (JMSException ex) {
                            ex.printStackTrace();
                            consumer.close();
                            return null;
                        }
                    }
                } else {
                    //else pop message from JMSQueue and return to attend for another message
                    receiveOnQueue(millisec);
                }
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }
        consumer.close();
        return null;
    }

    /***
     * Method that receives prepare message from TM on JMSQueue.
     * @return received object
     */
    public Serializable receiveOnQueue() {
        return receiveOnQueue(0);

    }

    /***
     * Method that send a message on JMSQueue to TM.
     * @param object 
     */
    public void sendOnQueue(Serializable object) {
        try {
            //Set Production JMSCorrelationID for current served Transiction Manager Thread
            producer.setJMSCorrelationID(getRMfromTM());
            producer.send(dest, object);

        } catch (JMSRuntimeException e) {
            System.err.println("Exception occurred: " + e.toString());
            System.exit(1);
        }
    }

    /***
     * Run method for runnable interface that implements 2PC transaction communication alghoritm
     * between Transiction Manager and Office.
     */
    @Override
    public void run() {

        while (true) {
            //Set empty the current served Transiction Manager Thread
            tmserved = "";
            printOnFile("" + idOffice + ": I'm waiting a request from Web Service");
            //Attend for a Prepare 2PC request from JMSQueue
            String msg = (String) receivePrepareOnQueue();
            printOnFile("Prepare message received:" + msg);
            if (msg.equalsIgnoreCase("prepare")) {
                 //Contacts FrontEnd to query databases Replica Manager for lock object
                 String lockPossible = frontend.prepareAck();
                 //if Quorum for lock object is ok
                if (lockPossible.equalsIgnoreCase("lockOk")) {
                    printOnFile("Lock correct: send ready on queue");
                    //send 2PC Ready Local Decision to TM
                    sendOnQueue("ready");
                    //wait for 2PC Validation Global Decision
                    String globaldecision = (String) receiveOnQueue();
                    printOnFile("Global decision message received:" + globaldecision);
                    //if 2PC ready global decision from TM is ok
                    if (globaldecision.equalsIgnoreCase("ok")) {
                        //wait for Signal to validate from TM
                        printOnFile("Receiving Signal");
                        Segnalazione signal = (Segnalazione) receiveOnQueue();
                        //Contacts FrontEnd to query databases Replica Manager for signal validation ( if signal is possible to do )
                        String validationAvailable = frontend.validateSignal(signal);
                        printOnFile("Validation Message from DB replica manager:" + validationAvailable);
                        //Send 2PC Validation Local Decision to TM
                        sendOnQueue(validationAvailable);
                        //wait for 2PC Commit Global Decision
                        String finalDecision = (String) receiveOnQueue();
                        printOnFile("Commit decision received:" + finalDecision);
                        //If 2PC Commit Glabal Decision is ok
                        if (finalDecision.equalsIgnoreCase("commit")) {
                            //Contacts FrontEnd to query databases Replica Manager for signal commit
                            String ack = frontend.commitSignal(signal);
                            //Send 2PC commit ack to TM
                            sendOnQueue(ack);
                            printOnFile("Sending ACK message :" + ack);
                            //frontend.unlockDBs();
                            //printOnFile("Resource Unlocked");

                            //Wait for any commit request due to lost ack
                            printOnFile("Listening for lost ACK...");
                            String msgresend = (String) receiveOnQueue(2000);
                            //If other commit request is received
                            while (msgresend != null && msgresend.equalsIgnoreCase("commit")) {
                                //Contacts FrontEnd to query databases Replica Manager for signal commit
                                ack = frontend.commitSignal(signal);
                                //Send 2PC commit ack to TM
                                sendOnQueue(ack);
                                printOnFile("Sending ACK message :" + ack);
                                //Wait for any commit request due to lost ack
                                printOnFile("Listening for lost ACK...");
                                msgresend = (String) receiveOnQueue(2000);

                            }
                            //If no other commit request is received
                            if (msgresend == null) {
                                frontend.unlockDBs();
                                printOnFile("Resource Unlocked");
                            }
                          //If 2PC Commit Glabal Decision is not ok   
                        } else {
                            //Abort
                            printOnFile("CommitFailed.Abort");
                            //Contacts FrontEnd to query databases Replica Manager for unlock their lock objects
                            frontend.unlockDBs();
                            printOnFile("Resource Unlocked");
                        }
                      //if 2PC global decision from TM is not ok
                    } else {
                        //Abort
                        printOnFile("GlobalDecision failed.Abort");
                        //Contacts FrontEnd to query databases Replica Manager for unlock their lock objects
                        frontend.unlockDBs();
                        printOnFile("Resource Unlocked");
                    }
                 //if Quorum for lock object is not ok
                } else if(lockPossible.equalsIgnoreCase("lockNo")) {
                    //Abort
                    printOnFile("Unlock failed: send unready on queue");
                    //send unready 2PC message to TM
                    sendOnQueue("unready");
                    //wait validation global decisione Response from TM
                    String globaldecision = (String) receiveOnQueue();
                    if (!globaldecision.equalsIgnoreCase("ok")) {
                        printOnFile("GlobalDecision failed.Abort because of my lock fail");
                        //Contacts FrontEnd to query databases Replica Manager for unlock their lock objects
                        frontend.unlockDBs();
                    } else {
                        printOnFile("Problem. It's possible that some message has been lost");
                    }
                }
            }
        }
    }

    /***
     * Method that implements sleep for thread.
     * @param millisec time in millisec to sleep
     */
    private void sleep(long millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /***
     * Methods that return the Production JMSCorrelationID for current served Transiction Manager Thread
     * from the reception one.
     * @return 
     */
    public String getRMfromTM() {

        return "RM" + tmserved.substring(2);
    }
    
    /***
     * Method that create directory for logs files.
     */
    public void prepareFile(){
        try {
            file = new File(filePath+"/"+idOffice+".txt");
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();
            
            pr = new PrintWriter(file);
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /***
      * Method thats write on file and print to standard output a message.
      * @param message message to write and print
      */
    public void printOnFile(String message){
            pr.println(message);
            pr.flush();
            System.out.println(message);
    }
  

}
