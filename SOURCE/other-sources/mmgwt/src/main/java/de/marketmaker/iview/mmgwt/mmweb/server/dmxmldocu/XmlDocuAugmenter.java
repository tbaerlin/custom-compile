/*
 * XmlDocuAugmenter.java
 *
 * Created on 19.03.2012 14:18:04
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.dmxmldocu;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.TypeInfoProvider;
import javax.xml.validation.ValidatorHandler;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

import de.marketmaker.istar.common.dmxmldocu.DictHtmlFormatUtil;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.Attribute;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.Element;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.Text;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.XsdType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.w3c.dom.TypeInfo;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class XmlDocuAugmenter implements InitializingBean {
    private final Log logger = LogFactory.getLog(getClass());

    private Resource xsdFile;

    private XsdDocuProvider xsdDocuProvider;

    public Schema schema;

    public void afterPropertiesSet() throws Exception {
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema = schemaFactory.newSchema(xsdFile.getURL());
    }

    public Element parseAndAugment(Source source) throws IOException, SAXException, TransformerException {
        final ValidatorHandler validatorHandler = schema.newValidatorHandler();
        final SAXResult result = new SAXResult(validatorHandler);
        final TypeInfoProvider infoProvider = validatorHandler.getTypeInfoProvider();
        final NodeCreatingContentHandler nodeCreatingContentHandler =
                new NodeCreatingContentHandler(infoProvider);
        validatorHandler.setContentHandler(nodeCreatingContentHandler);
        TransformerFactory.newInstance().newTransformer().transform(source, result);
        return nodeCreatingContentHandler.getRoot();
    }

    public Resource getXsdFile() {
        return xsdFile;
    }

    public void setXsdFile(Resource xsdFile) {
        this.xsdFile = xsdFile;
    }

    public XsdDocuProvider getXsdTypeDocuProvider() {
        return xsdDocuProvider;
    }

    public void setXsdTypeDocuProvider(XsdDocuProvider xsdDocuProvider) {
        this.xsdDocuProvider = xsdDocuProvider;
    }

    class NodeCreatingContentHandler implements ContentHandler {

        private final Deque<String> anonymousTypes = new LinkedList<>();

        private TypeInfoProvider infoProvider;

        private Element root;

        private Element current;
        private StringBuilder currentCharacterData;

        public NodeCreatingContentHandler(TypeInfoProvider infoProvider) {
            this.infoProvider = infoProvider;
        }

        public Element getRoot() {
            return root;
        }

        public void setDocumentLocator(Locator locator) {
        }

        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        public void endPrefixMapping(String prefix) throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts)
                throws SAXException {
            final XsdType xsdType = getXsdType(this.current, localName);
            final String elemDocu = xsdDocuProvider.getElementDocu(localName, xsdType,
                    current == null ? null : current.getXsdType());

            final Element elemNode = new Element(localName, elemDocu, xsdType);
            addAttributes(elemNode, atts, xsdType);

            if (current == null) {
                this.root = elemNode;
            } else {
                this.current.addChild(elemNode);
            }
            this.current = elemNode;
            this.currentCharacterData = null;
        }

        private void addAttributes(Element element, Attributes atts, XsdType elemType) {
            for (int i = 0; i < atts.getLength(); ++i) {
                final String attrName = atts.getQName(i);
                final String attrValue = atts.getValue(i);
                final TypeInfo attributeTypeInfo = infoProvider.getAttributeTypeInfo(i);
                final XsdType attrType = new XsdType(attributeTypeInfo == null ? null : attributeTypeInfo.getTypeName());
                final String attrDocu = xsdDocuProvider.getAttributeDocu(attrName, elemType);
                final Attribute attrNode = new Attribute(attrName, attrValue, attrDocu, attrType);
                element.addAttribute(attrNode);
            }
        }

        private XsdType getXsdType(Element parent, String label) {
            // anonymous types are written as #AnonType_{innerType}{outerType}
            // but we need {outerType}__{innerType}
            final TypeInfo typeInfo = infoProvider.getElementTypeInfo();
            final boolean anonymous = typeInfo.getTypeName().startsWith("#AnonType"); // $NON-NLS$
            if (anonymous) {
                final String outerType = parent == null ? "TOPLEVEL" : parent.getXsdType().getLocalName(); // $NON-NLS$
                return new XsdType(outerType + DictHtmlFormatUtil.ANONYMOUS_TYPE_FATHER_ELEM_SEP + label);
            }
            else {
                return new XsdType(typeInfo.getTypeName());
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(currentCharacterData != null && currentCharacterData.length() > 0) {
                current.setText(new Text(currentCharacterData.toString(), current.getTooltip()));
            }
            current = current.getParent();
        }

        /**
         * The Parser will call this method to report each chunk of character data.
         * <b>SAX parsers may return all contiguous character data in a single chunk,
         * or they may split it into several chunks!</b>
         * {@see http://docs.oracle.com/javase/7/docs/api/org/xml/sax/ContentHandler.html#characters%28char[],%20int,%20int%29}
         * Hence, it is necessary to concatenate these several chunks to one string!
         */
        public void characters(char[] ch, int start, int length) throws SAXException {
            final String text = String.valueOf(ch, start, length).trim();
            if(text.length() > 0) {
                if(currentCharacterData == null) {
                    currentCharacterData = new StringBuilder();
                }
                currentCharacterData.append(text);
            }
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        public void processingInstruction(String target, String data) throws SAXException {
            logger.info("pi: " + data);
        }

        public void skippedEntity(String name) throws SAXException {
        }

    }
}
