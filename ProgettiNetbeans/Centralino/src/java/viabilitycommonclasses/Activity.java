/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package viabilitycommonclasses;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Classe che rappresenta la generica attivita' da da eseguire in regime transazionale
 * @author Alessio_Gregory_Ricky
 */
public class Activity implements Serializable {
    /**
     * id dell'attivita'
     */
    private String id=null;
    /**
     * lista delle segnalazioni, ovvero degli uffici che devono essere contattati
     */
    private LinkedBlockingQueue<Segnalazione> listsignal=null;

    /**
     * Costruisce una segnalazione vuota
     */
    public Activity() {
        listsignal=new LinkedBlockingQueue<Segnalazione>(GlobalParams.numOffice);
    }

    /**
     * Restituisce l'id dell'attivita'
     * @return id dell'attivita'
     */
    public String getId() {
        return id;
    }

    /**
     * Settare id dell'attivita'
     * @param id id dell'attivita'
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Ritorna la lista delle segnalazioni
     * @return Ritorna la lista delle segnalazioni
     */
    public LinkedBlockingQueue<Segnalazione> getListsignal() {
        return listsignal;
    }

    /**
     * Cambia la lista delle segnalazioni riguardanti la specifica attivita'
     * @param listsignal lista delle segnalazioni
     */
    public void setListsignal(LinkedBlockingQueue<Segnalazione> listsignal) {
        this.listsignal = listsignal;
    }

    /**
     * Contronta due attivita se hanno lo stesso id
     * @param obj confronta attivita' corrente con oggetto obj
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
        final Activity other = (Activity) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /**
     * Stringa con id ed elenco segnalazioni
     * @return Stringa con id ed elenco segnalazioni
     */
    @Override
    public String toString() {
        return "Activity{" + "id=" + id + ", listsignal=" + listsignal + '}';
    }

    
}
