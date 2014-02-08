/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package webservices;

import java.io.Serializable;
import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import viabilitycommonclasses.Activity;

/**
 *WebServices che offre un servizio ai client di tipo due
 * @author Alessio_Gregory_Ricky
 */
@WebService(serviceName = "WebServicesClient2")
public class WebServicesClient2 {

    /**
     * Risorsa connection factory
     */
    @Resource(mappedName = "jms/ViabilityConnectionFactoryQueue")
    private ConnectionFactory connectionFactoryQueue;
    
    /**
     * Risorsa coda sulla quale mandare le richieste dei client che devono essere smistate ed eseguite
     * dal gestore di TM
     */
    @Resource(mappedName = "jms/queueTM2")
    private Queue queue;
    
    /**
     * WebServices per i client di tipo 2
     */
    private static String WBid="WB2";
    /**
     * Web service operation operazione di aggiunta in coda di un attivita' da eseguire 
     */
    @WebMethod(operationName = "addActivity")
    public void addActivity(@WebParam(name = "Activity") Activity Activity) {
        System.out.println("Metodo richiamato");
        sendMessage(Activity);
    }

    /**
     * Serve per mandare messaggi al gestore di TM per eseguire una modifica nei db in regime
     * transazionale
     * @param Message messaggio da inviare
     */
    private void sendMessage(Serializable Message){
        try {
            Connection connection= connectionFactoryQueue.createConnection();
            Session session=connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer=session.createProducer(queue);
            ObjectMessage message=session.createObjectMessage();
            message.setJMSCorrelationID(WBid);
            message.setObject(Message);
            producer.send(message);
            System.out.println(Message);
            producer.close();
            connection.close();
        } catch (JMSException ex) {
            ex.printStackTrace();;
        }
    }

    
}
