/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbviability_1;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author Alessio_Gregory_Ricky
 */
public class CrashPacket implements Serializable {

    private String action;
    private HashMap<Long, String> db;
    private long senderID;
    private long destinationID;
    private boolean locking;
    public static String REQUEST = "REQUEST_DB";
    public static String SENDING = "SENDING_DB";

    public static String LOCKING = "LOCKING_DB";
    public static String LOCKING_RESPONSE = "LOCKING_RESPONSE_DB";

    public static String UNLOCKING = "UNLOCKING_DB";
    public static String UNLOCKINGALL = "UNLOCKINGALL_DB";

    /**
     * CrashPacket main constructor. ACTION : defines the kind of action DB :
     * contains the real db information SenderID : ID of the RM sender
     * DestinationID : ID of the RM destination locking : the lock response
     */
    public CrashPacket() {
        this.action = null;
        this.db = null;
        this.senderID = 0;
        this.destinationID = 0;
        this.locking = false;
    }

    /**
     * Tells if the packet is a global unlocking request.
     *
     * @return true or false
     */
    public boolean isUnlockingGlobal() {
        if (action.equalsIgnoreCase(CrashPacket.UNLOCKINGALL)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tells if the packet is a locking response.
     *
     * @return true or false
     */
    public boolean isLockingResponse() {
        if (action.equalsIgnoreCase(CrashPacket.LOCKING_RESPONSE)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tells if the packet is a locking request.
     *
     * @return true or false
     */
    public boolean isLockRequest() {
        if (action.equalsIgnoreCase(CrashPacket.LOCKING)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tells if the packet is an unlock request.
     *
     * @return true or false
     */
    public boolean isUnlockRequest() {
        if (action.equalsIgnoreCase(CrashPacket.UNLOCKING)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * set the lock response
     *
     * @param lock
     */
    public void setLocking(boolean lock) {
        this.locking = lock;
    }

    /**
     * get the lock response.
     *
     * @return
     */
    public boolean getLocking() {
        return locking;
    }

    /**
     * return the sender ID
     *
     * @return
     */
    public long getSenderID() {
        return senderID;
    }

    /**
     * set the sender ID
     *
     * @param senderID
     */
    public void setSenderID(long senderID) {
        this.senderID = senderID;
    }

    /**
     * get the destination ID
     *
     * @return
     */
    public long getDestinationID() {
        return destinationID;
    }

    /**
     * set the destination ID
     *
     * @param destinationID
     */
    public void setDestinationID(long destinationID) {
        this.destinationID = destinationID;
    }

    /**
     * get the Action related to the packet.
     *
     * @return
     */
    public String getAction() {
        return action;
    }

    /**
     * Set the action packet
     *
     * @param action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * get the DB
     *
     * @return
     */
    public HashMap<Long, String> getDb() {
        return db;
    }

    /**
     * set the DB
     *
     * @param db
     */
    public void setDb(HashMap<Long, String> db) {
        this.db = db;
    }

    /**
     * tells if the packet contains a request
     *
     * @return
     */
    public boolean isARequest() {
        if (action.equalsIgnoreCase(CrashPacket.REQUEST)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method is used to know if a packet is created by a RM with the id
     *
     * @param id
     * @return
     */
    public boolean isMine(long id) {
        if (id == getSenderID()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tells if the packet is destineted to the RM with the id specified
     *
     * @param id
     * @return
     */
    public boolean isForMe(long id) {
        if (id == getDestinationID()) {
            return true;
        } else {
            return false;
        }
    }

}
