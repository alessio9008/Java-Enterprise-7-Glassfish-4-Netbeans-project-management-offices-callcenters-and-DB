
package webservices;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per segnalazione complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="segnalazione">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="officeID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="streetID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="streetStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "segnalazione", propOrder = {
    "officeID",
    "streetID",
    "streetStatus"
})
public class Segnalazione {

    protected String officeID;
    protected long streetID;
    protected String streetStatus;

    /**
     * Recupera il valore della proprietà officeID.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOfficeID() {
        return officeID;
    }

    /**
     * Imposta il valore della proprietà officeID.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOfficeID(String value) {
        this.officeID = value;
    }

    /**
     * Recupera il valore della proprietà streetID.
     * 
     */
    public long getStreetID() {
        return streetID;
    }

    /**
     * Imposta il valore della proprietà streetID.
     * 
     */
    public void setStreetID(long value) {
        this.streetID = value;
    }

    /**
     * Recupera il valore della proprietà streetStatus.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStreetStatus() {
        return streetStatus;
    }

    /**
     * Imposta il valore della proprietà streetStatus.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStreetStatus(String value) {
        this.streetStatus = value;
    }

}
