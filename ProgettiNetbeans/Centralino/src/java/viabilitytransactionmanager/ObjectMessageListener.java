/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package viabilitytransactionmanager;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import viabilitycommonclasses.Activity;

/**
 *
 * @author Alessio_Gregory_Ricky
 */
public class ObjectMessageListener implements MessageListener {

    /**
     * Metodo che viene richiamato all'arrivo di un nuovo messaggio in coda,
     * infatti abbiamo un listener asincrono sulla nostra coda che viene svegliato quando arriva
     * un nuovo messaggio a lui destinato e lo inserisce nella coda delle attivita' da eseguire.
     * @param message 
     */
    @Override
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {            
            try {
                Serializable obj = ((ObjectMessage) message).getObject();
                if (obj instanceof Activity) {
                    try {
                        Main.addActivity((Activity) obj);
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }
    }

}
