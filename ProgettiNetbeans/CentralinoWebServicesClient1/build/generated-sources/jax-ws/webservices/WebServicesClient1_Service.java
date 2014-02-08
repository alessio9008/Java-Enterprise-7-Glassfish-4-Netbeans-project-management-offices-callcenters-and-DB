
package webservices;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.8
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "WebServicesClient1", targetNamespace = "http://webservices/", wsdlLocation = "http://localhost:8080/CentralinoWebServices1/WebServicesClient1?WSDL")
public class WebServicesClient1_Service
    extends Service
{

    private final static URL WEBSERVICESCLIENT1_WSDL_LOCATION;
    private final static WebServiceException WEBSERVICESCLIENT1_EXCEPTION;
    private final static QName WEBSERVICESCLIENT1_QNAME = new QName("http://webservices/", "WebServicesClient1");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://localhost:8080/CentralinoWebServices1/WebServicesClient1?WSDL");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        WEBSERVICESCLIENT1_WSDL_LOCATION = url;
        WEBSERVICESCLIENT1_EXCEPTION = e;
    }

    public WebServicesClient1_Service() {
        super(__getWsdlLocation(), WEBSERVICESCLIENT1_QNAME);
    }

    public WebServicesClient1_Service(WebServiceFeature... features) {
        super(__getWsdlLocation(), WEBSERVICESCLIENT1_QNAME, features);
    }

    public WebServicesClient1_Service(URL wsdlLocation) {
        super(wsdlLocation, WEBSERVICESCLIENT1_QNAME);
    }

    public WebServicesClient1_Service(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, WEBSERVICESCLIENT1_QNAME, features);
    }

    public WebServicesClient1_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public WebServicesClient1_Service(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns WebServicesClient1
     */
    @WebEndpoint(name = "WebServicesClient1Port")
    public WebServicesClient1 getWebServicesClient1Port() {
        return super.getPort(new QName("http://webservices/", "WebServicesClient1Port"), WebServicesClient1.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns WebServicesClient1
     */
    @WebEndpoint(name = "WebServicesClient1Port")
    public WebServicesClient1 getWebServicesClient1Port(WebServiceFeature... features) {
        return super.getPort(new QName("http://webservices/", "WebServicesClient1Port"), WebServicesClient1.class, features);
    }

    private static URL __getWsdlLocation() {
        if (WEBSERVICESCLIENT1_EXCEPTION!= null) {
            throw WEBSERVICESCLIENT1_EXCEPTION;
        }
        return WEBSERVICESCLIENT1_WSDL_LOCATION;
    }

}