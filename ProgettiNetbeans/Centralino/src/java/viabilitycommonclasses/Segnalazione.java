/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package viabilitycommonclasses;

import java.io.Serializable;

/**
 * Classe che rappresenta lo specifico ufficio al quale si vuole inviare una segnalazione
 * indicando che qualcosa e' cambiato in una sua strada
 * @author Alessio_Gregory_Ricky
 */
public class Segnalazione implements Serializable{
    /**
     * Ufficio id
     */
    private String officeID=null;
    
    /**
     * Strada che si vuole segnalare
     */
    private long streetID;
    /**
     * Stato della strada da aggiornare
     */
    private String streetStatus;

    /**
     * Ritorna id della strada
     * @return id della strada
     */
    public long getStreetID() {
        return streetID;
    }

    /**
     * Setta id della strada
     * @param streetID id della strada
     */
    public void setStreetID(long streetID) {
        this.streetID = streetID;
    }

    /**
     * ritorna lo stato della strada
     * @return stato della strada
     */
    public String getStreetStatus() {
        return streetStatus;
    }
    /**
     * Setta lo stato della strada
     * @param streetStatus stato della strada
     */
    public void setStreetStatus(String streetStatus) {
        this.streetStatus = streetStatus;
    }

    /**
     * Ritorna l'id dell'ufficio
     * @return id dell'ufficio
     */
    public String getOfficeID() {
        return officeID;
    }

    /**
     * Setta l'id dell'ufficio
     * @param officeID id dell'ufficio
     */
    public void setOfficeID(String officeID) {
        this.officeID = officeID;
    }


    /**
     * Confronta l'uguaglianza di due segnalazioni in base ad idOffice e idStreet
     * @param obj confronta l'istanza corrente con l'oggetto obj
     * @return vero se sono uguali
     */
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
        if (this.streetID != other.streetID) {
            return false;
        }
        return true;
    }

    /**
     * Stringa che rappresenta la specifica segnalazione
     * @return Stringa che rappresenta la specifica segnalazione
     */
    @Override
    public String toString() {
        return "Segnalazione{" + "officeID=" + officeID + ", streetID=" + streetID + ", streetStatus=" + streetStatus + '}';
    }


    
    
}
