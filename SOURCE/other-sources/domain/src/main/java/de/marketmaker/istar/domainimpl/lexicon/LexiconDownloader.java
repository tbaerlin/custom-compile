/*
 * Downloader.java
 *
 * Created on 02.08.2006 21:20:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.lexicon;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.PublicKey;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributeListImpl;

import de.marketmaker.istar.common.xml.XmlWriter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LexiconDownloader {

    private static final int MAX_ROWS_PER_PAGE = 50;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DEFAULT_TYPE = "default";

    private final static String[] INITIALS = new String[]{
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
    };

    private final static DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");

    private final static DateTimeFormatter OUT_DTF = ISODateTimeFormat.basicDateTimeNoMillis();

    private static final String BASE_URL_INITIAL_DEFAULT = "http://interface.vwd.com/vwd/LEX/POOL/EXCHANGE/";

    private static final String BASE_URL_INITIAL_POSTBANK = "http://interface.vwd.com/postbank/interface/CMSinterface.htn?function=getLexicon&dest=xml&sectionCode=DICTIONARY_EXCHANGE&collection=all&literal=";
//    private static final String BASE_URL_TEXT_POSTBANK= "http://interface.vwd.com/postbank/interface/CMSinterface.htn?function=getLexicon&dest=xml&sectionCode=DICTIONARY_EXCHANGE&id=";


    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("target file required as program parameter");
        }
        new LexiconDownloader().start(args[0]);
    }

    private void start(String filename) throws Exception {
        final XmlWriter writer = new XmlWriter();
        writer.setCdataElements(new String[]{"item", "text"});
        writer.setEncoding("UTF-8");
        writer.setGzipped(true);
        writer.setPrettyPrint(true);
        writer.setRootElement("lexicon");
        writer.setFile(new File(filename));
        writer.start();

        // TODO: remove null-elements after update in frontend and backend
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("vwd2", "sommer69".toCharArray());
            }
        });
        writeLexicon(writer, null, BASE_URL_INITIAL_DEFAULT);
        writeLexicon(writer, DEFAULT_TYPE, BASE_URL_INITIAL_DEFAULT);

        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("postbank", "8x6n1kd7".toCharArray());
            }
        });
        writeLexiconNewStyle(writer, "postbank", BASE_URL_INITIAL_POSTBANK);

        writer.stop();
    }

    private void writeLexiconNewStyle(XmlWriter writer, String type,
            String baseUrlInitialStandard) throws Exception {
        final org.w3c.dom.Document outDocument = writer.getDocument();
        writeHeader(writer, type);

        final SAXBuilder saxBuilder = new SAXBuilder();
        
        for (final String c : INITIALS) {
            Document document;
            final URL url = new URL(baseUrlInitialStandard + c);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<writeLexiconNewStyle> loading new style url " + url);
            }
            try{
                Thread.sleep(500);
                document = saxBuilder.build(url);
            }
            catch (Exception e){
                this.logger.debug("Exception while loading new style url " + e);
                Thread.sleep(5000);
                document = saxBuilder.build(url);
                
            }
            //noinspection unchecked
            final List<Element> rows = document.getRootElement().getChildren("element");
            for (final Element row : rows) {
                final String item = row.getChildTextTrim("item");
                final String initial = row.getChildTextTrim("initial").toUpperCase();
                final String id = row.getChildTextTrim("id");
                final String text = row.getChildTextTrim("text");

                writeElement(writer, outDocument, item, initial, id, text, null, new DateTime());
            }
        }

        if (type != null) {
            writer.endElement("elements");
        }
    }

    private void writeElement(XmlWriter writer, org.w3c.dom.Document outDocument, String item,
            String initial, String id, String text, String source,
            DateTime date) throws IOException {
        final org.w3c.dom.Element element = outDocument.createElement("element");
        element.appendChild(createElement(outDocument, "id", id));
        element.appendChild(createElement(outDocument, "source", source));
        element.appendChild(createElement(outDocument, "initial", initial));
        element.appendChild(createElement(outDocument, "item", item));
        element.appendChild(createElement(outDocument, "date", OUT_DTF.print(date)));
        element.appendChild(createElement(outDocument, "text", text));
        writer.writeNode(element);
    }

    private void writeLexicon(XmlWriter writer, String type, String baseUrlInitialStandard)
            throws Exception {
        final org.w3c.dom.Document outDocument = writer.getDocument();
        writeHeader(writer, type);

        final SAXBuilder saxBuilder = new SAXBuilder();
        for (final String initial : INITIALS) {
            int start;
            int retrieved = 0;
            while (true) {
                start = retrieved + 1;
                final URL url = new URL(baseUrlInitialStandard + initial
                        + "/" + start + "-" + (start + MAX_ROWS_PER_PAGE));
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<writeLexicon> loading initial " + initial + " with url " + url);
                }
                final Document document = saxBuilder.build(url);
                //noinspection unchecked
                final List<Element> rows = document.getRootElement().getChildren("article");
                for (final Element row : rows) {
                    final String item = row.getChildTextTrim("headline");
                    final String text = row.getChildTextTrim("text");
                    final String id = row.getAttributeValue("id");
                    writeElement(writer, outDocument, item, initial, id, text, "Börsenlexikon", null);
                    retrieved++;
                }

                final Element meta = document.getRootElement().getChild("meta");
                final int totalRows = Integer.parseInt(meta.getChildTextTrim("totalRowCount"));
                if (retrieved == totalRows) {
                    break;
                }
            }
        }

        if (type != null) {
            writer.endElement("elements");
        }
    }

    private void writeHeader(XmlWriter writer, String type) throws SAXException {
        if (type != null) {
            final AttributeListImpl attributeList = new AttributeListImpl();
            attributeList.addAttribute("type", "string", type);
            writer.startElement("elements", attributeList);
        }
    }

    private org.w3c.dom.Element createElement(org.w3c.dom.Document document, String tagname,
            String item) {
        final org.w3c.dom.Element el = document.createElement(tagname);
        el.appendChild(document.createTextNode(item));
        return el;
    }
}
