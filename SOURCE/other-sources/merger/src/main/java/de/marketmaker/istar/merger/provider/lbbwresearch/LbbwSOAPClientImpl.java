package de.marketmaker.istar.merger.provider.lbbwresearch;

import java.io.StringWriter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of LbbwSOAPClient interface using default <code>javax.xml.soap</code> package
 * @author mcoenen
 * @see javax.xml.soap
 */
public class LbbwSOAPClientImpl implements LbbwSOAPClient {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, List<String>> documentTypesToFetch = DocumentTypes.ALL_DOCUMENT_TYPES;

    private String endpoint = "http://172.19.67.116/services/researchws";

    private String namespaceUri = "http://ws.service.research.lbbw.de/";

    private String namespace = "ws";

    private int resultCount = 100;

    public void setDocumentTypesToFetch(Map<String, List<String>> documentTypesToFetch) {
        this.documentTypesToFetch = documentTypesToFetch;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }

    @Override
    public Set<BasicDocument> callGetBasicDocuments(
            LocalDate publicationDateFrom,
            LocalDate publicationDateTo) throws SOAPException {

        ImmutableSet.Builder<BasicDocument> builder = ImmutableSet.builder();

        // Grouped by documentType category
        for (Map.Entry<String, List<String>> documentTypeEntry : this.documentTypesToFetch.entrySet()) {
            String category = documentTypeEntry.getKey();
            List<String> documentTypes = documentTypeEntry.getValue();

            // No more than 10 documentTypes at a time
            for (int i = 0; i < documentTypes.size(); i += 10) {

                int end = Math.min(i + 10, documentTypes.size());

                int startIndex = 0;
                boolean possiblyMoreToFetch;

                do {
                    SOAPMessage soapRequest = getGetBasicDocumentsRequest(documentTypes.subList(i, end), publicationDateFrom, publicationDateTo, startIndex);
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("<callGetBasicDocuments> Fetching basicDocuments for " + documentTypes.subList(i, end));
                    }
                    SOAPMessage soapResponse = doRequest(soapRequest);
                    List<BasicDocument> basicDocumentsInResponse = parseBasicDocumentsResponse(soapResponse.getSOAPBody(), category);
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("<callGetBasicDocuments> Found " + basicDocumentsInResponse.size() + " basicDocuments");
                    }

                    builder.addAll(basicDocumentsInResponse);
                    possiblyMoreToFetch = basicDocumentsInResponse.size() == this.resultCount;
                    if (possiblyMoreToFetch) {
                        startIndex += this.resultCount;
                    }
                } while (possiblyMoreToFetch);
            }
        }

        return builder.build();
    }

    @Override
    public Optional<CompanyInfo> callGetCompanyInfo(String companyObjectId,
            List<String> enabledDetails) {
        try {
            SOAPMessage soapRequest = getGetCompanyInfoRequest(companyObjectId, enabledDetails);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<callGetCompanyInfo> Fetching company info for company object id " + companyObjectId);
            }
            SOAPMessage soapResponse = doRequest(soapRequest);
            return parseCompanyInfoResponse(soapResponse.getSOAPBody(), companyObjectId);
        } catch (SOAPException e) {
            this.logger.error("<callGetCompanyInfo> Error while fetching company details", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<byte[]> callGetDocumentContents(String documentObjectId) {
        try {
            SOAPMessage soapRequest = getGetDocumentContentsRequest(documentObjectId);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<callGetDocumentContents> Fetching document content for " + documentObjectId);
            }
            SOAPMessage soapResponse = doRequest(soapRequest);
            return this.parseDocumentContentsResponse(soapResponse.getSOAPBody(), documentObjectId);
        } catch (SOAPException e) {
            this.logger.error("<callGetDocumentContents> Error while fetching document content", e);
        }
        return Optional.empty();
    }

    /**
     * Perform the actual request to the server and return the response
     * @param soapRequest Request to perform
     * @return Response from server
     * @throws SOAPException If anything goes wrong using SOAP
     */
    private SOAPMessage doRequest(SOAPMessage soapRequest) throws SOAPException {

        // Create SOAP Connection
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

        try {
            return soapConnection.call(soapRequest, this.endpoint);
        } finally {
            //noinspection ThrowFromFinallyBlock
            soapConnection.close();
        }
    }

    /**
     * Extract a BasicDocument object from the provided Node object, caller needs to check for null
     * @param returnNode Node to extract data from
     * @param category Category this BasicDocument belong to
     * @return BasicDocument object
     *    or null if the document is not valid (e.g. missing objectId or missing title and headline)
     */
    private BasicDocument getBasicDocument(Node returnNode, String category) {
        final BasicDocument basicDocument = new BasicDocument();

        final Optional<String> objectId = this.getStringValue(returnNode, "objectId");
        if (!objectId.isPresent()) {
            this.logger.warn("<getBasicDocument> Missing objectId in SOAP response, available data for document: "
                    + " headline: '" + this.getStringValue(returnNode, "headline").orElse(null) + "'"
                    + " publicationDate: '" + this.getStringValue(returnNode, "publicationDate").orElse(null) + "'"
                    + " document will be ignored");
            return null;
        }

        basicDocument.setObjectId(objectId.get());
        this.getStringValue(returnNode, "headline").ifPresent(basicDocument::setTitle);
        if (StringUtils.isBlank(basicDocument.getTitle().orElse(""))) {
            this.getStringValue(returnNode, "title").ifPresent(basicDocument::setTitle);
        }

        if (StringUtils.isBlank(basicDocument.getTitle().orElse(""))) {
            this.logger.warn("<getBasicDocument> Missing title for document id " + basicDocument.getObjectId()
                    + " with publication date " + basicDocument.getPublicationDate()
                    + " document will be ignored");
            return null;
        }

        this.getStringValue(returnNode, "filename").ifPresent(basicDocument::setFilename);
        this.getStringValue(returnNode, "publicationDate").ifPresent(basicDocument::setPublicationDate);
        this.getStringValue(returnNode, "language", "domainId").ifPresent(basicDocument::setLanguage);
        this.getStringValue(returnNode, "documentType", "domainId").ifPresent(basicDocument::setDocumentType);
        this.getStringValue(returnNode, "displaySector", "longDescriptor").ifPresent(basicDocument::setSector);
        basicDocument.setCategory(category);

        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        this.getStringValues(returnNode, "company", "objectId")
                .stream()
                .filter(StringUtils::isNotBlank)
                .forEach(builder::add);
        basicDocument.setCompanyObjectIds(builder.build());

        return basicDocument;
    }

    /**
     * Extract a CompanyInfo object from the provided Node object
     * @param returnNode Node to extract data from
     * @return CompanyInfo object
     */
    private CompanyInfo getCompanyInfo(Node returnNode) {
        CompanyInfo companyInfo = new CompanyInfo();

        this.getStringValue(returnNode, "company", "objectId").ifPresent(companyInfo::setObjectId);
        this.getStringValue(returnNode, "isinIdentifier", "identifier").ifPresent(companyInfo::setIsin);
        this.getStringValue(returnNode, "company", "region", "isoCode").ifPresent(companyInfo::setCountry);

        // IMPORTANT: this only works as long as there is only one option creating objectTexts:entry blocks
        this.getStringValues(returnNode, "objectTexts", "entry", "value")
                .stream()
                .filter(StringUtils::isNotBlank)
                .findFirst().ifPresent(companyInfo::setCompanyGuidance);

        return companyInfo;
    }

    /**
     * Constructs SOAP Request Message
     * <pre>
     * &lt;soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
     * xmlns:ws="http://ws.service.research.lbbw.de/"&gt;
     * &lt;soapenv:Header/&gt;
     * &lt;soapenv:Body&gt;
     * &lt;ws:getBasicDocuments&gt;
     * &lt;arg0&gt;
     * &lt;documentTypes&gt;[für alle verfügbaren Dokumenttypen]&lt;/documentTypes&gt;
     * &lt;technicalFormat&gt;
     * &lt;domainId&gt;PDF&lt;/domainId&gt;
     * &lt;/technicalFormat&gt;
     * &lt;releaseLevel&gt;
     * &lt;domainId&gt;PDFRELEASED&lt;/domainId&gt;
     * &lt;/releaseLevel&gt;
     * &lt;publicationDateFrom&gt;[tagesaktuelles Datum]&lt;/publicationDateFrom&gt;
     * &lt;publicationDateTo&gt;[tagesaktuelles Datum]&lt;/publicationDateTo&gt;
     * &lt;/arg0&gt;
     * &lt;arg1&gt;0&lt;/arg1&gt;
     * &lt;arg2&gt;100&lt;/arg2&gt;
     * &lt;/ws:getBasicDocuments&gt;
     * &lt;/soapenv:Body&gt;
     * &lt;/soapenv:Envelope&gt;
     * </pre>
     * @param documentTypes Collection of documentTypes to include in this request
     * @param publicationDateFrom Start date for search
     * @param publicationDateTo End date for search (usually identical with start date for documents of one day)
     * @param startIndex First result element
     * @return SOAPMessage constructed out of given values
     * @throws SOAPException If SOAPMessage cannot be constructed
     */
    private SOAPMessage getGetBasicDocumentsRequest(
            Collection<String> documentTypes,
            LocalDate publicationDateFrom,
            LocalDate publicationDateTo,
            int startIndex) throws SOAPException {

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapRequest = messageFactory.createMessage();
        SOAPPart soapPart = soapRequest.getSOAPPart();

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(this.namespace, this.namespaceUri);

        SOAPBody soapBody = envelope.getBody();

        SOAPElement getBasicDocuments = soapBody.addChildElement("getBasicDocuments", "ws");
        SOAPElement arg0 = getBasicDocuments.addChildElement("arg0");

        for (String documentType : documentTypes) {
            SOAPElement documentTypesElement = arg0.addChildElement("documentTypes");
            documentTypesElement.addTextNode(documentType);
        }

        SOAPElement technicalFormat = arg0.addChildElement("technicalFormat");
        SOAPElement technicalFormatDomainId = technicalFormat.addChildElement("domainId");
        technicalFormatDomainId.addTextNode("PDF");

        SOAPElement releaseLevel = arg0.addChildElement("releaseLevel");
        SOAPElement releaseLevelDomainId = releaseLevel.addChildElement("domainId");
        releaseLevelDomainId.addTextNode("PDFRELEASED");

        SOAPElement publicationDateFromElement = arg0.addChildElement("publicationDateFrom");
        publicationDateFromElement.addTextNode(publicationDateFrom.toString());

        SOAPElement publicationDateToElement = arg0.addChildElement("publicationDateTo");
        publicationDateToElement.addTextNode(publicationDateTo.toString());

        SOAPElement arg1 = getBasicDocuments.addChildElement("arg1");
        arg1.addTextNode(String.valueOf(startIndex));
        SOAPElement arg2 = getBasicDocuments.addChildElement("arg2");
        arg2.addTextNode(String.valueOf(this.resultCount));

        soapRequest.saveChanges();

        return soapRequest;
    }

    /**
     * Constructs SOAP Request Message
     *
     * <pre>
     * &lt;soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
     * xmlns:ws="http://ws.service.research.lbbw.de/"&gt;
     *    &lt;soapenv:Header/&gt;
     *    &lt;soapenv:Body&gt;
     *       &lt;ws:getCompanyInfo&gt;
     *          &lt;arg0&gt;objectId&lt;/arg0&gt;
     *          &lt;arg2&gt;
     *             &lt;companyGuidance&gt;true&lt;/companyGuidance&gt;
     *             &lt;companyProfile&gt;true&lt;/companyProfile&gt;
     *             &lt;instrument&gt;true&lt;/instrument&gt;
     *             &lt;investmentCase&gt;true&lt;/investmentCase&gt;
     *             &lt;isin&gt;true&lt;/isin&gt;
     *             &lt;latestComment&gt;true&lt;/latestComment&gt;
     *             &lt;latestUpdate&gt;true&lt;/latestUpdate&gt;
     *             &lt;prosCons&gt;true&lt;/prosCons&gt;
     *             &lt;rating&gt;true&lt;/rating&gt;
     *             &lt;targetPrice&gt;true&lt;/targetPrice&gt;
     *             &lt;currentQuote&gt;true&lt;/currentQuote&gt;
     *          &lt;/arg2&gt;
     *       &lt;/ws:getCompanyInfo&gt;
     *    &lt;/soapenv:Body&gt;
     * &lt;/soapenv:Envelope&gt;
     * </pre>
     * @param objectId ObjectId to use
     * @param enabledDetails Flags in <code>arg2</code> to actually include (every entry of the list is added as true)
     * @return SOAPMessage constructed out of given values
     * @throws SOAPException If SOAPMessage cannot be constructed
     */
    private SOAPMessage getGetCompanyInfoRequest(String objectId,
            List<String> enabledDetails) throws SOAPException {

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapRequest = messageFactory.createMessage();
        SOAPPart soapPart = soapRequest.getSOAPPart();

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(this.namespace, this.namespaceUri);

        SOAPBody soapBody = envelope.getBody();

        SOAPElement getCompanyInfo = soapBody.addChildElement("getCompanyInfo", "ws");

        SOAPElement arg0 = getCompanyInfo.addChildElement("arg0");
        arg0.addTextNode(objectId);

        SOAPElement arg2 = getCompanyInfo.addChildElement("arg2");
        for (String enabledDetail : enabledDetails) {
            arg2.addChildElement(enabledDetail).addTextNode("true");
        }

        soapRequest.saveChanges();

        return soapRequest;
    }

    /**
     * Constructs SOAP Request Message:
     * <pre>
     * &lt;soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
     * xmlns:ws="http://ws.service.research.lbbw.de/"&gt;
     *    &lt;soapenv:Header/&gt;
     *    &lt;soapenv:Body&gt;
     *       &lt;ws:getDocumentContents&gt;
     *          &lt;arg0&gt;objectId&lt;/arg0&gt;
     *       &lt;/ws:getDocumentContents&gt;
     *    &lt;/soapenv:Body&gt;
     * &lt;/soapenv:Envelope&gt;
     * </pre>
     * @param objectId ObjectId to use
     * @return SOAPMessage constructed out of given values
     * @throws SOAPException If SOAPMessage cannot be constructed
     */
    private SOAPMessage getGetDocumentContentsRequest(String objectId) throws SOAPException {

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapRequest = messageFactory.createMessage();
        SOAPPart soapPart = soapRequest.getSOAPPart();

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(this.namespace, this.namespaceUri);

        SOAPBody soapBody = envelope.getBody();

        SOAPElement getBasicDocuments = soapBody.addChildElement("getDocumentContents", "ws");
        SOAPElement arg0 = getBasicDocuments.addChildElement("arg0");
        arg0.addTextNode(objectId);

        soapRequest.saveChanges();

        return soapRequest;
    }


    /**
     * Get the child nodes at an arbitrary depth
     * @param parentNode Node to start in
     * @param firstMatch Whether to return first match or all matches
     * @param childNodeNames List of names the child nodes to traverse (need to be in correct order)
     * @return Node(s) found with the last <code>childNodeName</code>
     */
    private List<Node> getNodes(Node parentNode, boolean firstMatch, List<String> childNodeNames) {

        ImmutableList.Builder<Node> builder = ImmutableList.builder();

        for (int i = 0, size = childNodeNames.size(); i < size; i++) {
            String nodeName = childNodeNames.get(i);
            NodeList childNodes = parentNode.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node child = childNodes.item(j);
                if (child.getNodeName().equalsIgnoreCase(nodeName)) {
                    if (size == 1) {
                        builder.add(child);
                    }
                    else {
                        List<Node> childResult = getNodes(child, firstMatch, childNodeNames.subList(i + 1, size));
                        builder.addAll(childResult);
                    }
                    if (firstMatch) {
                        return builder.build();
                    }
                }
            }
        }

        return builder.build();
    }

    /**
     * Get the first string value of a child node at an arbitrary depth
     * @param parentNode Node to start in
     * @param childNodeNames List of names the child nodes to traverse (need to be in correct order)
     * @return <b>First</b> value found
     */
    private Optional<String> getStringValue(Node parentNode, String... childNodeNames) {
        List<Node> nodes = getNodes(parentNode, true, Arrays.asList(childNodeNames));

        if (!nodes.isEmpty()) {
            return Optional.ofNullable(nodes.get(0).getTextContent());
        }

        return Optional.empty();
    }

    /**
     * Get the all string values of all child nodes matching the provided child node order
     * @param parentNode Node to start in
     * @param childNodeNames List of names the child nodes to traverse (need to be in correct order)
     * @return All values found
     */
    private List<String> getStringValues(Node parentNode, String... childNodeNames) {
        List<Node> nodes = getNodes(parentNode, false, Arrays.asList(childNodeNames));

        ImmutableList.Builder<String> builder = ImmutableList.builder();

        for (Node node : nodes) {
            Optional.ofNullable(node.getTextContent()).ifPresent(builder::add);
        }

        return builder.build();
    }

    /**
     * Check for basic validity and return all available BasicDocument objects
     * @param body SOAP response body
     * @param category Category these BasicDocuments belong to
     * @return List of BasicDocument objects found in response
     */
    private List<BasicDocument> parseBasicDocumentsResponse(SOAPBody body, String category) {

        ImmutableList.Builder<BasicDocument> basicDocuments = ImmutableList.builder();

        NodeList returnNodes = body.getElementsByTagName("return");
        for (int i = 0, size = returnNodes.getLength(); i < size; i++) {
            final BasicDocument basicDocument = getBasicDocument(returnNodes.item(i), category);
            if (basicDocument != null) {
                basicDocuments.add(basicDocument);
            }
        }

        return basicDocuments.build();
    }

    /**
     * Check for basic validity and return CompanyInfo object
     * @param body SOAP response body
     * @param companyObjectId Object id for this company (for logging only)
     * @return CompanyInfo object if available
     */
    private Optional<CompanyInfo> parseCompanyInfoResponse(SOAPBody body, String companyObjectId) {

        NodeList returnElements = body.getElementsByTagName("return");
        int returnElementsCount = returnElements.getLength();
        if (returnElementsCount == 0) {
            this.logger.warn("<parseCompanyInfoResponse> No company found for " + companyObjectId);
            return Optional.empty();
        }

        // This should never happen
        if (returnElementsCount > 1) {
            this.logger.error("<parseCompanyInfoResponse> Multiple companies found for " + companyObjectId + ". Ignoring everything after first.");
        }

        return Optional.of(getCompanyInfo(returnElements.item(0)));
    }

    /**
     * Check for basic validity of response and return decoded PDF
     * @param body SOAP response body
     * @param documentObjectId Object id for this document (for logging only)
     * @return Decoded PDF as byte[] if present
     */
    private Optional<byte[]> parseDocumentContentsResponse(SOAPBody body, String documentObjectId) {

        NodeList returnElements = body.getElementsByTagName("return");

        if (returnElements.getLength() == 0) {
            this.logger.warn("<parseDocumentContentsResponse> No document found for " + documentObjectId);
            return Optional.empty();
        }

        // This should never happen
        if (returnElements.getLength() > 1) {
            this.logger.error("<parseDocumentContentsResponse> Multiple documents found for " + documentObjectId + ". Ignoring everything after first.");
        }

        String base64EncodedDocument = returnElements.item(0).getTextContent();
        try {
            return Optional.of(Base64.getDecoder().decode(base64EncodedDocument));
        } catch (IllegalArgumentException e) {
            this.logger.error("<parseDocumentContentsResponse> Error decoding document content for " + documentObjectId, e);
        }
        return Optional.empty();
    }

    public static void main(String[] args) throws SOAPException {
        LbbwSOAPClient client = new LbbwSOAPClientImpl();
        Set<BasicDocument> docs = client.callGetBasicDocuments(LocalDate.of(2016, 9, 29), LocalDate.of(2016, 9, 29));

        System.out.println(docs);
    }

    private static String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
        }
        return sw.toString();
    }
}
