/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbviability_1;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import lock.LockObject;
import viabilitycommonclasses.DBOperation;

/**
 * CRASH MAGAER CLASS
 * @author Alessio_Gregory_Ricky
 */
public class CrashManager implements MessageListener {

    private long idDB;

    private HashMap<Long, String> db;

    private LinkedList<HashMap<Long, String>> dbCollector;

    private Topic topic;

    private JMSProducer producertopic;

    private int counterCrash;

    private LockObject lock;

    private int lockCounter;
    private boolean lockEnabled;
    private long idTempLocked;

    /**
     * CrashManager is used to manage the crash event.
     * This class provides all methods used to receive and send CrashPacket.
     * @param lock
     * @param idDB
     * @param db
     * @param topic
     * @param contexttopic 
     */
    public CrashManager(LockObject lock, long idDB, HashMap<Long, String> db, Topic topic, JMSContext contexttopic) {
        this.lockEnabled = false;
        this.lockCounter = 0;
        this.db = db;
        this.topic = topic;
        producertopic = contexttopic.createProducer();
        this.idDB = idDB;
        counterCrash = 0;
        this.lock = lock;

        dbCollector = new LinkedList<HashMap<Long, String>>();
    }

    /**
     * The onMessage override the standard method. 
     * Message contains a CrashPacket.  
     * When a REQUEST packet is received, a DB copy is sent.
     * When a UNLOCKGLOBAL packet is received, the RM must release his token.
     * When a LOCKRESPONSE packet is received, the RM (surely in crash, collect the token)
     * When a UNLOCKREQUEST packet is received the RM release the token.
     * Else it is in the case where the RM collect the DB received.
     * @param message 
     */
    @Override
    public void onMessage(Message message) {
        ObjectMessage msg = null;
        if (message instanceof ObjectMessage) {
            try {
                msg = (ObjectMessage) message;
                CrashPacket packet = (CrashPacket) msg.getObject();

                Main.printLog("[CRASH MANAGER] PACKET RECEIVED from " + packet.getSenderID());
                if (packet.isARequest() && !packet.isMine(idDB)) {
                    Main.printLog("[CRASH MANAGER] PACKET is a request");
                    sendDBByTopic(db, packet.getSenderID());
                    Main.printLog("[CRASH MANAGER] PACKET with DB sent");
                } else if (packet.isUnlockingGlobal()) {
                    Main.printLog("[CRASH MANAGER] GLOBAL UNLOCK RECEIVED");
                    lock.unLock();

                } else if (packet.isLockingResponse() && packet.isForMe(idDB) && !packet.isMine(idDB)) {
                    Main.printLog("[CRASH MANAGER] PACKET LOCK request");
                    if (packet.getLocking()) {
                        lockCounter++;
                        if (lockCounter == 1) {
                            idTempLocked = packet.getSenderID();
                        }
                        if (lockCounter == 2) {
                            Main.printLog("[CRASH MANAGER] ALL LOCK COLLECTED");
                            lockEnabled = true;
                        }
                    } else {
                        Main.printLog("[CRASH MANAGER] LOCK REFUSED");
                        lockCounter = 0;
                        sendUnlockDB(idTempLocked);
                    }

                } else if (packet.isUnlockRequest() && packet.isForMe(idDB)) {
                    Main.printLog("[CRASH MANAGER] UNLOCK RECEIVED... unlocked!");
                    lock.unLock();

                } else if (packet.isLockRequest() && !packet.isMine(idDB) ) {

                    sendResponseLock(lock.tryLock(), packet.getSenderID());

                } else {
                    if (packet.isForMe(idDB)) {
                        collectDB(packet.getDb());
                        Main.printLog("[CRASH MANAGER] collecting a db");
                    }

                }

            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }

    }
/**
 * This method is used to send a DBCopy through the Topic channel.
 * 
 * @param o
 * @param idDestination 
 */
    public void sendDBByTopic(Serializable o, long idDestination) {
        try {
            CrashPacket packet = new CrashPacket();
            packet.setAction(CrashPacket.SENDING);
            packet.setDb((HashMap<Long, String>) o);
            packet.setSenderID(idDB);
            packet.setDestinationID(idDestination);
            producertopic.send((Destination) topic, packet);

        } catch (JMSRuntimeException e) {
            Main.printLog("Exception occurred: " + e.toString());
            System.exit(1);
        }
    }
/**
 * This methos is used to send a request through the Topic, when RM is crashed.
 * @param idDestination 
 */
    public void sendRequestByTopic(long idDestination) {
        try {
            Main.printLog("[CRASH MANAGER] SENDING A REQUEST PACKET. I AM IN CRASH: " + idDB);
            startLockingRequest();
            CrashPacket packet = new CrashPacket();
            packet.setAction(CrashPacket.REQUEST);
            packet.setSenderID(idDB);
            producertopic.send((Destination) topic, packet);

        } catch (JMSRuntimeException e) {
            Main.printLog("Exception occurred: " + e.toString());
            System.exit(1);
        }
    }
/**
 * Method used to collect the DB received with the packet.
 * @param db 
 */
    private void collectDB(HashMap<Long, String> db) {

        //dbCollector[counterCrash] = db;
        dbCollector.add(db);
        counterCrash++;
        Main.printLog("[CRASH MANAGER] db received: " + counterCrash);
        if (counterCrash >= 2) {
            checkConsistance();
            counterCrash = 0;
            dbCollector.clear();
        }

    }
/**
 * Check the database collected during the crash phase.
 */
    private void checkConsistance() {
        Main.printLog("[CRASH MANAGER] checking CONSISTANCE");
        if (dbCollector.getFirst().equals(dbCollector.getLast())) {

            db.clear();
            db.putAll(dbCollector.getFirst());
            Main.printLog("[CRASH MANAGER] db correctly UPDATED");
            Main.continueInitialization(lock);
        } else {
            Main.printLog("[CRASH MANAGER] Consistance failure....");
        }

    }
/**
 * This method provides a way to send the response to the lock request, trying to lock it self.
 * @param decision
 * @param destinationId 
 */
    private void sendResponseLock(boolean decision, long destinationId) {
        try {
            Main.printLog("[CRASH MANAGER] SENDING A LOCK RESPONSE (" + decision + "): " + idDB);
            CrashPacket packet = new CrashPacket();
            packet.setAction(CrashPacket.LOCKING_RESPONSE);
            packet.setSenderID(idDB);
            packet.setDestinationID(destinationId);
            packet.setLocking(decision);
            producertopic.send((Destination) topic, packet);

        } catch (JMSRuntimeException e) {
            Main.printLog("Exception occurred: " + e.toString());
            System.exit(1);
        }
    }
/**
 * The start locking method send a LOCK REQUEST to all in the Topic. Used after a crash detenction.
 * This method wait for the locking resolve.
 */
    private void startLockingRequest() {

        try {
            Main.printLog("[CRASH MANAGER] SENDING A LOCK REQUEST TO ALL. I am:  " + idDB);
            CrashPacket packet = new CrashPacket();
            packet.setAction(CrashPacket.LOCKING);
            packet.setSenderID(idDB);
            producertopic.send((Destination) topic, packet);

        } catch (JMSRuntimeException e) {
            Main.printLog("Exception occurred: " + e.toString());
            System.exit(1);
        }

        boolean lock = false;
        while (!lock) {
            if (lockEnabled) {
                lock = true;
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        lockEnabled = false;

        try {
            Main.printLog("[CRASH MANAGER] SENDING A UNLOCK REQUEST TO ALL. I am:  " + idDB);
            CrashPacket packet = new CrashPacket();
            packet.setAction(CrashPacket.UNLOCKINGALL);
            packet.setSenderID(idDB);
            producertopic.send((Destination) topic, packet);

        } catch (JMSRuntimeException e) {
            Main.printLog("Exception occurred: " + e.toString());
            System.exit(1);
        }
    }

    /**
     * Send an unlock notification to a DB previously locked.
     * @param idTempLocked 
     */
    private void sendUnlockDB(long idTempLocked) {
        try {
            Main.printLog("[CRASH MANAGER] SENDING A UNLOCK RESPONSE: " + idDB + "for db: " + idTempLocked);
            CrashPacket packet = new CrashPacket();
            packet.setAction(CrashPacket.UNLOCKING);
            packet.setSenderID(idDB);
            packet.setDestinationID(idTempLocked);
            producertopic.send((Destination) topic, packet);

        } catch (JMSRuntimeException e) {
            Main.printLog("Exception occurred: " + e.toString());
            System.exit(1);
        }
    }

}
