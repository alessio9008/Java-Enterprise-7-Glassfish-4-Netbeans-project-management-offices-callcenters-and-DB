/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package viabilitycommonclasses;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Alessio_Gregory_Ricky
 */
public class Activity implements Serializable {
    private String id=null;
    private LinkedBlockingQueue<Segnalazione> listsignal=null;

    public Activity() {
        listsignal=new LinkedBlockingQueue<Segnalazione>(GlobalParams.numOffice);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LinkedBlockingQueue<Segnalazione> getListsignal() {
        return listsignal;
    }

    public void setListsignal(LinkedBlockingQueue<Segnalazione> listsignal) {
        this.listsignal = listsignal;
    }

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

    @Override
    public String toString() {
        return "Activity{" + "id=" + id + ", listsignal=" + listsignal + '}';
    } 
}
