//
// This file was generated by the Eclipse Implementation of JAXB, v3.0.0 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2025.01.02 at 10:08:22 AM BDT 
//


package com.example.adservice;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="samAccountName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "samAccountName"
})
@XmlRootElement(name = "enableUserRequest")
public class EnableUserRequest {

    @XmlElement(required = true)
    protected String samAccountName;

    /**
     * Gets the value of the samAccountName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSamAccountName() {
        return samAccountName;
    }

    /**
     * Sets the value of the samAccountName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSamAccountName(String value) {
        this.samAccountName = value;
    }

}
