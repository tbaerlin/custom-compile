//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.09.21 at 08:30:23 AM CEST 
//


package com.vwd.dmxml.examples.jaxb.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.vwd.dmxml.examples.jaxb.generated package.
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

    private final static QName _Response_QNAME = new QName("", "response");
    private final static QName _Request_QNAME = new QName("", "request");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.vwd.dmxml.examples.jaxb.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link com.vwd.dmxml.examples.jaxb.generated.RequestedBlockType }
     * 
     */
    public RequestedBlockType createRequestedBlockType() {
        return new RequestedBlockType();
    }

    /**
     * Create an instance of {@link com.vwd.dmxml.examples.jaxb.generated.ResponseType }
     * 
     */
    public ResponseType createResponseType() {
        return new ResponseType();
    }

    /**
     * Create an instance of {@link RequestType }
     * 
     */
    public RequestType createRequestType() {
        return new RequestType();
    }

    /**
     * Create an instance of {@link ErrorType }
     * 
     */
    public ErrorType createErrorType() {
        return new ErrorType();
    }

    /**
     * Create an instance of {@link com.vwd.dmxml.examples.jaxb.generated.DataType }
     * 
     */
    public DataType createDataType() {
        return new DataType();
    }

    /**
     * Create an instance of {@link QuoteData }
     * 
     */
    public QuoteData createQuoteData() {
        return new QuoteData();
    }

    /**
     * Create an instance of {@link com.vwd.dmxml.examples.jaxb.generated.RequestedBlocksType }
     * 
     */
    public RequestedBlocksType createRequestedBlocksType() {
        return new RequestedBlocksType();
    }

    /**
     * Create an instance of {@link com.vwd.dmxml.examples.jaxb.generated.IdentifierData }
     * 
     */
    public IdentifierData createIdentifierData() {
        return new IdentifierData();
    }

    /**
     * Create an instance of {@link HeaderType }
     * 
     */
    public HeaderType createHeaderType() {
        return new HeaderType();
    }

    /**
     * Create an instance of {@link com.vwd.dmxml.examples.jaxb.generated.InstrumentData }
     * 
     */
    public InstrumentData createInstrumentData() {
        return new InstrumentData();
    }

    /**
     * Create an instance of {@link com.vwd.dmxml.examples.jaxb.generated.MSCStaticData }
     * 
     */
    public MSCStaticData createMSCStaticData() {
        return new MSCStaticData();
    }

    /**
     * Create an instance of {@link com.vwd.dmxml.examples.jaxb.generated.RequestedBlockType.Parameter }
     * 
     */
    public RequestedBlockType.Parameter createRequestedBlockTypeParameter() {
        return new RequestedBlockType.Parameter();
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link com.vwd.dmxml.examples.jaxb.generated.ResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "response")
    public JAXBElement<ResponseType> createResponse(ResponseType value) {
        return new JAXBElement<ResponseType>(_Response_QNAME, ResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link RequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "request")
    public JAXBElement<RequestType> createRequest(RequestType value) {
        return new JAXBElement<RequestType>(_Request_QNAME, RequestType.class, null, value);
    }

}
