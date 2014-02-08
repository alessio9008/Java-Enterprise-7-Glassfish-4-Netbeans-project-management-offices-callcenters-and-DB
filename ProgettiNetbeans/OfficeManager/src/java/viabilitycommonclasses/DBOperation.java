/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package viabilitycommonclasses;

import java.io.Serializable;

/**
 * ADT for DBOperation messages for request to Replica Manager from Front End Office.
 * @author Alessio_Gregory_Ricky
 */
public class DBOperation implements Serializable{
    
    //Signal to validate or commit
    Segnalazione signal;
    //Operation to do ( lockRequest, validation, commit or unlockRequest )
    String operation;

    /**
     * Get signal
     * @return signal
     */
    public Segnalazione getSignal() {
        return signal;
    }

    /**
     * Set signal
     * @param signal 
     */
    public void setSignal(Segnalazione signal) {
        this.signal = signal;
    }

    /**
     * Get Operation to do
     * @return operation
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Set operation to do 
     * @param operation 
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Hashcode function
     * @return hash code value
     */
    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    /**
     * Equals method
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DBOperation other = (DBOperation) obj;
        if (this.signal != other.signal && (this.signal == null || !this.signal.equals(other.signal))) {
            return false;
        }
        if ((this.operation == null) ? (other.operation != null) : !this.operation.equals(other.operation)) {
            return false;
        }
        return true;
    }

    /**
     * ToString method
     * @return string for this object
     */
    @Override
    public String toString() {
        return "DBOperation{" + "signal=" + signal + ", operation=" + operation + '}';
    }
    
    
}
