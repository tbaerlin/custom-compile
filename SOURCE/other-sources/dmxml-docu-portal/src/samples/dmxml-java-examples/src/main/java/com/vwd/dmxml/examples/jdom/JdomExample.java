/*
 * JdomExample.java
 *
 * Created on 20.09.2012 11:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package com.vwd.dmxml.examples.jdom;

import com.vwd.dmxml.examples.common.DmxmlClient;
import com.vwd.dmxml.examples.common.DmxmlExample;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathBuilder;
import org.jdom2.xpath.XPathFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;

/**
 * This example demonstrates the usage of data manager [xml] with the JDOM library (version 2.0.3) {@link "http://www.jdom.org"}
 * in conjunction with the Jaxen XPath library (version 1.1.4) {@link "http://jaxen.codehaus.org/"}.
 * <p>Additionally, it demonstrates how to use the so called correlation id to identify different
 * blocks (aka Atoms) in the XML output (aka Molecule), if more than one block was requested
 * in one request.</p>
 * <p>The attribute <code>id</code> supplied with a block is reflected in the response as
 * attribute <code>correlationId</code></p>
 * <p>Therefore, in this example two blocks are requested and printed to stdout.</p>
 *
 * @author Markus Dick
 */
public class JdomExample extends DmxmlExample {
    static {
        exampleImplementation = JdomExample.class;
    }

    @Override
    public int execute() {
        String symbol1 = "DE0005204705"; //e.g. ISIN of vwd AG
        String correlationId1 = "1";
        String symbol2 = "DE0007100000"; //e.g. ISIN of Daimler AG
        String correlationId2 = "a-second-id";

        //Use this parameter to experiment with other locales/languages.
        //Currently de and en are fully supported.
        Locale locale = Locale.getDefault();

        out.printf("Requesting MSC_StaticData for %s with correlationId '%s' and %s with correlationId '%s':\n\n",
                symbol1, correlationId1, symbol2, correlationId2);

        //build xml request
        Document request = createDmxmlRequest(auth, authType, locale);
        addMscStaticData(request, symbol1, correlationId1);
        addMscStaticData(request, symbol2, correlationId2);

        out.println(toXmlString(request));

        //send xml request
        DmxmlClient client = new DmxmlClient(zone, host, port);
        DmxmlClient.Response response;
        try {
            response = client.request(toXmlString(request));
        }
        catch(IOException e) {
            e.printStackTrace();
            return 1;
        }

        //process response
        if(response.getHttpResponseCode() == HttpURLConnection.HTTP_OK) {
            SAXBuilder builder = new SAXBuilder();
            Document doc;
            try {
                doc = builder.build(new StringReader(response.getContent()));
            } catch (JDOMException e) {
                e.printStackTrace();
                return 1;
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            }

            Element root = doc.getRootElement();
            XPathFactory xpf = XPathFactory.instance();

            out.println("Response:");
            printMscStaticData(xpf, root, "1");
            out.println();
            printMscStaticData(xpf, root, "a-second-id");
        }
        else {
            out.printf("Error: %s (%s)\n",
                    response.getHttpResponseCode(),
                    response.getHttpResponseMessage());
        }

        return 0;
    }

    private void printMscStaticData(XPathFactory xpf, Element root, String correlationId) {
        Element block = getElement(xpf, root, "data/block[@correlationId='" + correlationId + "']");

        printInstrumentData(getElement(xpf, block, "instrumentdata"));
        printText(getElement(xpf, block, "typename"), "Type");
        printText(getElement(xpf, block, "sector"), "Sector");
        printText(getElement(xpf, block, "country"), "Country");
        printText(getElement(xpf, block, "tickersymbol"), "Ticker");
    }

    private static Element getElement(XPathFactory xpf, Element root, String expression) {
        XPathBuilder<Element> xpb = new XPathBuilder<Element>(expression, new ElementFilter());
        List<Element> elements = xpb.compileWith(xpf).evaluate(root);
        if(!elements.isEmpty()) {
            return elements.get(0);
        }
        return null;
    }

    private void printInstrumentData(Element element) {
        if(element == null) return;

        printChildText(element, "isin", "ISIN");
        printChildText(element, "name", "Name");
        final String typeEnum = element.getChildText("type");
        printValue("Type Enum", typeEnum + " (" + instrumentTypes.get(typeEnum) + ")");
    }

    private void printChildText(Element element, String child, String label) {
        if(element == null) return;

        String text = element.getChildText(child);
        printValue(label, text);
    }

    private void printText(Element element, String label) {
        if(element == null) return;

        String text = element.getText();
        printValue(label, text);
    }

    private Document createDmxmlRequest(String auth, String authType, Locale language) {
        if(auth == null || authType == null || language == null) {
            throw new IllegalArgumentException("parameters must not be null");
        }

        final Document document = new Document();
        final Element request = new Element("request");

        final Element authentication = new Element("authentication");
        final Element authenticationType = new Element("authenticationType");
        final Element locale = new Element("locale");

        authentication.setText(auth);
        authenticationType.setText(authType);
        locale.setText(language.getLanguage());

        document.setRootElement(request);
        request.addContent(authentication);
        request.addContent(authenticationType);
        request.addContent(locale);

        return document;
    }

    private void addMscStaticData(Document document, String symbol, String correlationId) {
        if(document == null || symbol == null) throw new IllegalArgumentException("document and symbol must not be null");

        final Element block = new Element("block");
        block.setAttribute("key", "MSC_StaticData");
        if(correlationId != null && !correlationId.trim().isEmpty()) {
            block.setAttribute("id", correlationId);
        }

        final Element symbolParam = new Element("parameter");
        symbolParam.setAttribute("key", "symbol");
        symbolParam.setAttribute("value", symbol);
        block.addContent(symbolParam);

        document.getRootElement().addContent(block);
    }

    /**
     * <p>This method resides here for your convenience only,
     * so that you can compile the other examples without
     * any dependencies to JDOM.</p>
     *
     * <p>Additionally, it uses the <code>outputString</code> method. For top performance
     * you should prefer the methods where you can pass in your
     * own writer.</p>
     */
    private String toXmlString(Document document) {
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        return xmlOutputter.outputString(document);
    }
}
