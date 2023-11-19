/*
 * XmlWriter.java
 *
 * Created on 14.09.2005 13:27:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.xml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.sun.org.apache.xml.internal.serialize.LineSeparator;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.util.ArraysUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class XmlWriter {
    private File file;
    private String encoding;
    private boolean prettyPrint;
    private boolean gzipped = false;
    private String rootElement;
    private String[] cdataElements;
    private Document document;
    private OutputStream outputStream;
    private IncrementalXmlSerializer xmlSerializer;
    private AttributesImpl attributes;

    public void setFile(File file) {
        this.file = file;
    }

    public void setGzipped(boolean gzipped) {
        this.gzipped = gzipped;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public void setRootElement(String rootElement) {
        this.rootElement = rootElement;
    }

    public void setCdataElements(String[] cdataElements) {
        this.cdataElements  = ArraysUtil.copyOf(cdataElements);
    }

    public void setAttributes(AttributesImpl attributes) {
        this.attributes = attributes;
    }

    public void start() throws Exception {
        final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        this.document = documentBuilder.newDocument();

        this.outputStream = this.gzipped
                ? new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(this.file)))
                : new BufferedOutputStream(new FileOutputStream(this.file));
        final OutputFormat outputFormat = new OutputFormat(document, this.encoding, this.prettyPrint);
        outputFormat.setLineSeparator(LineSeparator.Unix);
        outputFormat.setIndent(2);
        outputFormat.setCDataElements(this.cdataElements);

        this.xmlSerializer = new IncrementalXmlSerializer(this.outputStream, outputFormat);
        this.xmlSerializer.asDOMSerializer();
        this.xmlSerializer.reset();
        this.xmlSerializer.startDocument();
        this.xmlSerializer.startElement("namespaceURI", "localName", this.rootElement, this.attributes == null ? new AttributesImpl() : this.attributes);
    }


    public void stop() throws Exception {
        this.xmlSerializer.endElement("namespaceURI", "localName", this.rootElement);
        this.xmlSerializer.endDocument();
        this.outputStream.close();
    }


    public Document getDocument() {
        return this.document;
    }

    public void writeNode(Node node) throws IOException {
        this.xmlSerializer.serializeNode(node);
    }

    public void startElement(String tagName, AttributeList attrs) throws SAXException {
        xmlSerializer.startElement(tagName, attrs);
    }

    public void endElement(String tagName) throws SAXException {
        xmlSerializer.endElement(tagName);
    }

    static class IncrementalXmlSerializer extends XMLSerializer {
        public IncrementalXmlSerializer(OutputStream output, OutputFormat format) {
            super(output, format);
        }

        public void serializeNode(Node node) throws IOException {
            super.serializeNode(node);
        }

        public void flush() throws IOException {
            _printer.flush();
        }
    }

    public static void main(String[] args) throws Exception {
        final XmlWriter xmlWriter = new XmlWriter();
        xmlWriter.setEncoding("UTF-8");
        xmlWriter.setGzipped(false);
        xmlWriter.setPrettyPrint(true);
        xmlWriter.setRootElement("batchdata");

        xmlWriter.setFile(new File(args[0]));
        xmlWriter.start();
        final Document document = xmlWriter.getDocument();

        final Element header = document.createElement("header");
        xmlWriter.writeNode(header);
        xmlWriter.stop();
    }
}
