package de.marketmaker.istar.merger.provider.convensys;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * XmlToolFactory.java
 * Created on Jun 16, 2009 1:13:27 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 */
public class XmlToolFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    public XmlTool createFromString(String xml) throws Exception {
        return this.createFromString(xml, "UTF-8", false);
    }

    public XmlTool createFromString(String xml, String encoding, boolean escapeHtmlInNodeText)
            throws Exception {
        try (InputStream is = new ByteArrayInputStream(xml.getBytes(encoding))) {
            return createFromInputStream(is, escapeHtmlInNodeText);
        }
        catch (Exception e) {
            this.logger.error("<createFromString> failed", e);
            throw e;
        }
    }

    public XmlTool createFromUrl(String url) {
        try (InputStream is = new URL(url).openStream()) {
            return createFromInputStream(is, false);
        }
        catch (Exception e) {
            this.logger.error("<createFromUrl> failed", e);
            return null;
        }
    }

    private XmlTool createFromInputStream(InputStream inputStream, boolean escapeHtmlInNodeText)
            throws Exception {
        final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        final Document document = documentBuilder.parse(inputStream);
        return new XmlTool(document, escapeHtmlInNodeText);
    }

    public static void main(String[] args) {
        new XmlToolFactory().createFromUrl("http://mm.market-maker.de/deutscher-ring/servlet/portrait.xml?wkn=1299981.qid");
    }
}
