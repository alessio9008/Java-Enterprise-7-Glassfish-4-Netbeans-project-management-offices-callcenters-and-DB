/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package viabilitycommonclasses;

import java.io.Serializable;

/**
 *
 * @author Alessio_Gregory_Ricky
 */
public class Segnalazione implements Serializable{
    private String officeID=null;
    
    private long streetID;
    private String streetStatus;

    public long getStreetID() {
        return streetID;
    }

    public void setStreetID(long streetID) {
        this.streetID = streetID;
    }

    public String getStreetStatus() {
        return streetStatus;
    }

    public void setStreetStatus(String streetStatus) {
        this.streetStatus = streetStatus;
    }

    
    public String getOfficeID() {
        return officeID;
    }

    public void setOfficeID(String officeID) {
        this.officeID = officeID;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.officeID != null ? this.officeID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Segnalazione other = (Segnalazione) obj;
        if ((this.officeID == null) ? (other.officeID != null) : !this.officeID.equals(other.officeID)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Segnalazione{" + "officeID=" + officeID + ", streetID=" + streetID + ", streetStatus=" + streetStatus + '}';
    }

    
    
}
