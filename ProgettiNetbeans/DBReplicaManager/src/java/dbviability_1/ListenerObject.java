/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbviability_1;

import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Topic;
import lock.LockObject;
import viabilitycommonclasses.DBOperation;
import viabilitycommonclasses.Segnalazione;

/**
 * Class that implements MessageLister for receiving in asynchronous way message from JMSTopic between FrontEnd and Databases instances.
 * Databases Replica Manager are client for this topic and FrontEnd Office is producer for this one.
 * @author Alessio_Gregory_Ricky
 */
class ListenerObject implements MessageListener {

    //Database instance
    private HashMap<Long, String> localDB;
    //JMSTopic instance
    private Topic topic;
    //JMSQueue to send response to FrontEnd Office1 for this district
    private Queue queueOffice1;
    //JMSQueue to send response to FrontEnd Office2 for this district
    private Queue queueOffice2;

    //Destination for QueueOffice1
    private Destination destQueueOffice1;
    //Destination for QueueOffice2
    private Destination destQueueOffice2;

    //JMSProducer to send response to FrontEnd Offices for this district
    private JMSProducer producer;

    //LockObject variable for databases lock management
    private LockObject lock;
    //id of this district
    private String idDistrict;

    /**
     * Empty constructor
     */
    public ListenerObject() {
    }

    /**
     * Constructor for ListenerObject instances.
     * @param lock LockObject variable for databases lock management
     * @param localDB Database instance
     * @param topic JMSTopic instance
     * @param queueOffice1 JMSQueue to send response to FrontEnd Office1 for this district
     * @param queueOffice2 JMSQueue to send response to FrontEnd Office2 for this district
     * @param producer JMSProducer to send response to FrontEnd Offices for this district
     * @param destQueueOffice1 Destination for QueueOffice1
     * @param destQueueOffice2 Destination for QueueOffice2
     * @param idDistrict id of this district
     */
    ListenerObject(LockObject lock,HashMap<Long, String> localDB, Topic topic, Queue queueOffice1,Queue queueOffice2, JMSProducer producer, Destination destQueueOffice1,Destination destQueueOffice2,String idDistrict) {
        this.localDB = localDB;
        this.queueOffice1 = queueOffice1;
        this.queueOffice2 = queueOffice2;
        this.topic = topic;
        this.producer = producer;
        this.destQueueOffice1 = destQueueOffice1;
        this.destQueueOffice2 = destQueueOffice2;
        this.idDistrict=idDistrict;
        this.lock=lock;
    }

    /**
     * OnMessage method for MessageListener interface. This method manage onLockRequest, 
     * ValidationRequest, CommitRequest and unLockRequest from FronEnd Office.
     * @param message 
     */
    @Override
    public void onMessage(Message message) {
        Destination destqueue=null;
        ObjectMessage msg = null;
        if (message instanceof ObjectMessage) {
            try {
                String sender =message.getJMSCorrelationID();
                //Set Producer JMS CorrelationID from sender's one and Destination Queue ( office1 or office 2 )
                Main.printLog("Message Received from CorrelationID:"+sender);
                if(sender.equalsIgnoreCase("Office1"+idDistrict)){
                    destqueue=destQueueOffice1;
                }else if(sender.equalsIgnoreCase("Office2"+idDistrict)){
                    destqueue=destQueueOffice2;
                }else Main.printLog("Error with CorrelationID at DBViability");
                msg = (ObjectMessage) message;
                //get operation to do from DBOperation
                DBOperation operation = (DBOperation) msg.getObject();
                //if it's a validation operation
                if (operation.getOperation().equalsIgnoreCase("validation")) {
                    long idToCheck = operation.getSignal().getStreetID();
                    //Look if signal is possibile seeing if localDB contains this street id to check
                    if (localDB.containsKey(idToCheck)) {
                        try {
                            //Simulate Signal Resolution Time
                            Thread.sleep(6000+(int)(Math.random()*1500));
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        //Send Validation OK on correct office queue 
                        sendOnQueue("ValidationOk",destqueue);
                        
                    } else {
                        //Send Validation NO on correct office queue 
                        sendOnQueue("ValidationNo",destqueue);
                    }
                  //if it's a commit operation  
                } else if (operation.getOperation().equalsIgnoreCase("commit")) {
                    //remove from localDB associated signal street
                    localDB.remove(operation.getSignal().getStreetID());
                    //put to localDB new street params
                    String response = localDB.put(operation.getSignal().getStreetID(), operation.getSignal().getStreetStatus());
                    //Send Ack on correct office queue 
                    sendOnQueue("ACK",destqueue);
                  //if it's a lock Request 
                } else if (operation.getOperation().equalsIgnoreCase("lockRequest")) {
                    //try to get Lock for this database
                    if (lock.tryLock()) {
                        sendOnQueue("lockOk",destqueue);
                    } else {
                        sendOnQueue("lockNo",destqueue);
                    }
                  //if it's an unlock Request 
                } else if (operation.getOperation().equalsIgnoreCase("unlockRequest")) {
                    //unlock this database
                    lock.unLock();
                }
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }

    }

    /**
     * Send object on JMSQueue between ReplicaManagers and Front End Office
     * @param object object to send
     * @param destqueue DestinationQueue for Front End Office 
     */
    public void sendOnQueue(Serializable object,Destination destqueue) {
        try {
            Main.printLog("Sending message");
            producer.send(destqueue, object);
            Main.printLog("Text messages sent");

        } catch (JMSRuntimeException e) {
            Main.printLog("Exception occurred: " + e.toString());
            System.exit(1);
        }
    }

}
