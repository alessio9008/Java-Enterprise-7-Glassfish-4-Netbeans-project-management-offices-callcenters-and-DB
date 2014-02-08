/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceRef;
import webservices.Activity;
import webservices.Segnalazione;
import webservices.WebServicesClient1_Service;


/**
 *Servlet che serve per gestire i clienti di tipo 1
 * @author Alessio_Gregory_Ricky
 */
@WebServlet(name = "ServletClient1", urlPatterns = {"/ServletClient1"})
public class ServletClient1 extends HttpServlet {
    /**
     * Riferimento al web services
     */
    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_8080/CentralinoWebServices1/WebServicesClient1.wsdl")
    private WebServicesClient1_Service service_1;


    /**
     * Risorsa connectionfactory
     */
    @Resource(mappedName = "jms/ViabilityConnectionFactoryQueue")
    private ConnectionFactory connectionFactoryQueue;

    /**
     * Risorsa coda che serve per ricevere la risposta relativa alla richiesta del client
     */
    @Resource(mappedName = "jms/queueTM1")
    private Queue queue;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    private static long idActivity = 0;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {

            Activity activity = new Activity();
            activity.setId(String.valueOf(idActivity));
            idActivity++;

            for (int x = 0; x < 5; x++) {

                String idOffice = request.getParameter("selector-id-" + x);
                String idStreet = request.getParameter("selector-street-id-" + x);
                String status = request.getParameter("status-id-" + x);

                System.err.println(" - - - - Sto inserendo : - - - - ");
                if (idOffice != null) {
                    System.err.println("ID OFFICE: " + idOffice);
                } else {
                    System.err.println("idOffice e null");
                }
                if (idStreet != null) {
                    System.err.println("ID STREET: " + idStreet);
                } else {
                    System.err.println("idStreet e null");
                }
                if (status != null) {
                    System.err.println("STATUS: " + status);
                } else {
                    System.err.println("status e null");
                }

                if (idOffice != null && idStreet != null && status != null) {

//                    System.err.println(" - - - - Sto inserendo : - - - - ");
//                    System.err.println("ID OFFICE: " + idOffice);
//                    System.err.println("ID STREET: " + idStreet);
//                    System.err.println("STATUS: " + status);
                    Segnalazione signal = new Segnalazione();
                    signal.setOfficeID(idOffice);
                    signal.setStreetID(Long.valueOf(idStreet));
                    signal.setStreetStatus(status);

                    activity.getListsignal().add(signal);

                } else {
                    System.err.println("Uno dei parametri è null. Nessun inserimento per la posizione  " + x);
                }
            }
            sleep(2000);
            addActivity(activity);
            sleep(2000);
            receiveMessage("1" + activity.getId(), out);
            

        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    

    /**
     * Metodo che serve per ricevere la risposta sulla richiesta inviata
     * @param args risposta relativa alla specifica attivita 
     * @param out parametro per la stampa del risultato
     */
    private void receiveMessage(String args, PrintWriter out) {
        try {
            Connection connection = connectionFactoryQueue.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(queue, "JMSCorrelationID = 'Servlet" + args + "'");
            connection.start();
            Message mex = consumer.receive();
            if ((mex != null) && (mex instanceof ObjectMessage)) {
                String txt = (String) ((ObjectMessage) (mex)).getObject();
                out.println(txt);
                System.err.println(txt);
            }
            consumer.close();
            connection.close();
        } catch (JMSException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Attesa di un certo numero di millisecondi
     * @param millisec numero millisecondi
     */
    private void sleep(long millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Web service operation operazione di aggiunta in coda di un attivita' da eseguire 
     */
    private void addActivity(webservices.Activity activity) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        webservices.WebServicesClient1 port = service_1.getWebServicesClient1Port();
        port.addActivity(activity);
    }
}
