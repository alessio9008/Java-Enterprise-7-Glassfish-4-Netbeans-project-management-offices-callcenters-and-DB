
package webservices;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the webservices package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _AddActivity_QNAME = new QName("http://webservices/", "addActivity");
    private final static QName _AddActivityResponse_QNAME = new QName("http://webservices/", "addActivityResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: webservices
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AddActivityResponse }
     * 
     */
    public AddActivityResponse createAddActivityResponse() {
        return new AddActivityResponse();
    }

    /**
     * Create an instance of {@link AddActivity }
     * 
     */
    public AddActivity createAddActivity() {
        return new AddActivity();
    }

    /**
     * Create an instance of {@link Segnalazione }
     * 
     */
    public Segnalazione createSegnalazione() {
        return new Segnalazione();
    }

    /**
     * Create an instance of {@link Activity }
     * 
     */
    public Activity createActivity() {
        return new Activity();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddActivity }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices/", name = "addActivity")
    public JAXBElement<AddActivity> createAddActivity(AddActivity value) {
        return new JAXBElement<AddActivity>(_AddActivity_QNAME, AddActivity.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddActivityResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices/", name = "addActivityResponse")
    public JAXBElement<AddActivityResponse> createAddActivityResponse(AddActivityResponse value) {
        return new JAXBElement<AddActivityResponse>(_AddActivityResponse_QNAME, AddActivityResponse.class, null, value);
    }

}
