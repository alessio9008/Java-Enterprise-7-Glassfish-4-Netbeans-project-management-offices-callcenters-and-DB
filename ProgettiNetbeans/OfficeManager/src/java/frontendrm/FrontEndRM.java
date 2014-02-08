/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package frontendrm;

import offices.Office;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
import javax.jms.Topic;
import viabilitycommonclasses.DBOperation;
import viabilitycommonclasses.Segnalazione;

/**
 * ADT for ReplicaManager's FrontEnd. This class implements funzionality to manage Database Operation throught the frontend. 
 * @author Alessio_Gregory_Ricky
 */
public class FrontEndRM {

    //Destination for JMSTopic
    private Destination destTopic = null;
    //Destination for JMSQueue
    private Destination destQueue = null;
    private JMSProducer producertopic;
    //JMSContext for JMSQueue uses to receive response from databases
    private JMSContext contextqueue;
    //JMSContext for JMSTopic uses to send request databases
    private JMSContext contexttopic;
    //path for FrontEnd logs
    private String filePath;
    //Office ID
    private String idOffice;
    //file for FrontEnd logs
    private File file;
    //printwiter instance to print FrontEnd logs on file
    private PrintWriter pr;
    
    /**
     * Constructor for FrontEndRM instance.
     * @param filepath path for FrontEnd logs
     * @param idOffice Office ID
     * @param contextqueue JMSContext for JMSQueue uses to receive response from databases
     * @param contexttopic JMSContext for JMSTopic uses to send request databases
     * @param topic Destination for JMSTopic
     * @param queue Destination for JMSQueue
     */
    public FrontEndRM(String filepath,String idOffice,JMSContext contextqueue, JMSContext contexttopic, Topic topic, Queue queue) {
        this.filePath=filepath;
        this.idOffice=idOffice;
        this.contextqueue = contextqueue;
        this.contexttopic = contexttopic;
        this.destTopic = (Destination) topic;
        this.destQueue = (Destination) queue;
        prepareFile();
        emptyQueue();
        producertopic = contexttopic.createProducer();
        producertopic.setJMSCorrelationID(idOffice); //IdOffice is: "Office11" or "Office21" for District1, "Office12" or "Office22" for District2
        printOnFile("Replica Manager instantiate correctly");
    }

    /**
     * This method sends an object on JMSTopic.
     * @param object object to send
     */
    public void sendMessageTopic(Serializable object) {
        try {
            producertopic.send(destTopic, object);

        } catch (JMSRuntimeException e) {
            System.err.println("Exception occurred: " + e.toString());
            System.exit(1);
        }
    }

    /**
     * This method receives an object on JMSQueue
     * @return received object
     */
    public Serializable receiveOnQueue() {
        return receiveOnQueue(0);

    }

    /**
     * This method deletes instances loaded on JMSQueue.
     */
    public void emptyQueue() {
        printOnFile("Enqueue DBqueue procedure");
        for (int i = 0; i < 30; i++) {
            String trashMessage = (String) receiveOnQueue(1000);
            if (trashMessage == null) {
                break;
            }
            printOnFile("Deleted :" + trashMessage);
        }
    }
   
    /**
     * This method sends an object on JMSTopic using Timer.
     * @param millisec time in millisec to attend for a response
     * @return received object
     */
    public Serializable receiveOnQueue(int millisec) {
        JMSConsumer consumerqueue = contextqueue.createConsumer(destQueue);
        Message message = consumerqueue.receive(millisec);

        ObjectMessage msg = null;
        if (message != null) {
            if (message instanceof ObjectMessage) {
                try {
                    msg = (ObjectMessage) message;
                    //close consumer
                    consumerqueue.close();
                    return msg.getObject();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                    consumerqueue.close();
                    return null;
                }
            }
        }
        consumerqueue.close();
        return null;
    }

    /**
     * Method that asks lock objects to all databases. Then it calculates quorum for received responses.
     * @return lockOk if the quorum is positive, else lockNo
     */
    public String prepareAck() {
        DBOperation dbop = new DBOperation();
        //dbop.setSignal(signal);
        dbop.setOperation("lockRequest");
        printOnFile("lockRequest: sending DBOperationObject for Lock Request to DBs");
        sendMessageTopic(dbop);
        String response1 = (String) receiveOnQueue(1500);
        printOnFile("Ack Response1 :" + response1);
        String response2 = (String) receiveOnQueue(1500);
        printOnFile("Ack Response2 :" + response2);
        String response3 = (String) receiveOnQueue(1500);
        printOnFile("Ack Response3 :" + response3);
        if (calculateQuorum(response1, response2, response3,"lockOk",2)) {
            String response = "lockOk";
            printOnFile("Ack response: " + response);
            return response;
        } else {
            String response = "lockNo";
            printOnFile("Ack response: " + response);
            return response;
        }
    }
    
    /**
     * Method that asks to all databases for unlock their lock objects.
     */
    public void unlockDBs() {
        DBOperation dbop = new DBOperation();
        //dbop.setSignal(signal);
        dbop.setOperation("unlockRequest");
        printOnFile("unlockRequest: sending DBOperationObject for unlock Request to DBs");
        sendMessageTopic(dbop);
    }
    
    /**
     * Method that asks to all database for validation of a Signal. Then it calculates quorum for received responses.
     * @param signal signal to validate
     * @return 
     */
    public String validateSignal(Segnalazione signal) {
        DBOperation dbop = new DBOperation();
        dbop.setSignal(signal);
        dbop.setOperation("validation");
        printOnFile("Validation: sending DBOperationObject [" + dbop.toString() + "] to DBs");
        sendMessageTopic(dbop);
        String response1 = (String) receiveOnQueue(8000);
        printOnFile("Validation Response1 :" + response1);
        String response2 = (String) receiveOnQueue(8000);
        printOnFile("Validation Response2 :" + response2);
        String response3 = (String) receiveOnQueue(8000);
        printOnFile("Validation Response3 :" + response3);
        if (calculateQuorum(response1, response2, response3,"ValidationOk",2)) {
            String response = "ValidationOk";
            printOnFile("Validation response: " + response);
            return response;
        } else {
            String response = "ValidationNo";
            printOnFile("Validation response: " + response);
            return response;
        }
    }

    /***
      * Method that asks to all database for commit of a Signal. Then it calculates quorum for received responses.
     * @param signal signal to commit
     * @return 
     */
    public String commitSignal(Segnalazione signal) {
        DBOperation dbop = new DBOperation();
        dbop.setSignal(signal);
        dbop.setOperation("commit");
        printOnFile("Commit: sending DBOperationObject [" + dbop.toString() + "] to DBs");
        sendMessageTopic(dbop);
        String response1 = (String) receiveOnQueue(1500);
        printOnFile("Commit Response1 :" + response1);
        String response2 = (String) receiveOnQueue(1500);
        printOnFile("Commit Response2 :" + response2);
        String response3 = (String) receiveOnQueue(1500);
        printOnFile("Commit Response3 :" + response3);
        if (calculateQuorum(response1, response2, response3,"ACK",2)) {
            String response = "ACK";
            printOnFile("Validation response: " + response);
            return response;
        } else {
            String response = "abort";
            printOnFile("Validation response: " + response);
            return response;
        }
    }
    
    /***
     *  Method thats create file to write logs.
     */
     public void prepareFile(){
        try {
            file = new File(filePath+"/FrontEndRM"+idOffice+".txt");
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
    
    /***
     * Method that calculates quorum over responses in input looking to the input number.
     * @param response1 response message 1
     * @param response2 response message 2
     * @param response3 response message 3
     * @param message response message after calculate quorum
     * @param number value n for the quorum
     * @return 
     */
    public boolean calculateQuorum(String response1,String response2, String response3,String message,int number){
        int count=0;
        if(response1!=null && response1.equalsIgnoreCase(message))count++;
        if(response2!=null && response2.equalsIgnoreCase(message))count++;
        if(response3!=null && response3.equalsIgnoreCase(message))count++;
        if(count>=number)return true;
        else return false;
    }

}
