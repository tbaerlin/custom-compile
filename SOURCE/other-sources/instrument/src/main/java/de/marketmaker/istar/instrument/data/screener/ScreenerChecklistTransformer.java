/*
 * ScreenerChecklistTransformer.java
 *
 * Created on 27.04.2005 16:08:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.data.screener;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import java.io.*;
import java.nio.charset.Charset;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.util.StringUtils;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.PropertiesLoader;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id$
 */
public class ScreenerChecklistTransformer {
    private File file;
    private File imageNamesFile;
    private final List<Element> elements = new ArrayList<>();
    private Element current = null;
    private Properties imageProps;

    public ScreenerChecklistTransformer() {
    }

    private void setImageNamesFile(File imageNamesFile) {
        this.imageNamesFile=imageNamesFile;
    }

    private void setFile(File file) {
        this.file = file;
    }

    private void start() throws IOException {
        this.imageProps = PropertiesLoader.load(this.imageNamesFile);

        final Scanner s = new Scanner(this.file);
        while (s.hasNext()) {
            final String line = s.nextLine();

            process(line);
        }
        if (this.current != null) {
            this.elements.add(this.current);
        }


        final XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        final Charset cs = Charset.forName("ISO-8859-1");
        final PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out, cs));
        out.println("<?xml version=\"1.0\" encoding=\"" + cs.displayName() + "\"?>");
        out.println("<screenerConfig>");
        outputter.output(this.elements, out);
        out.println();
        out.println("</screenerConfig>");
        out.close();
    }

    private void process(final String line) {
        final String[] tokens = line.split(";");

        if (StringUtils.hasText(tokens[0])) {
            if (this.current != null) {
                this.elements.add(this.current);
            }

            this.current = new Element("element");
            this.current.setAttribute("id", tokens[0]);
            this.current.setAttribute("name", tokens[1]);
        }

            final Element rule = new Element("rule");
            this.current.getChildren().add(rule);

            addElement(rule, "condition", null, getCondition(tokens[2]));
            final String imageName = this.imageProps.getProperty(this.current.getAttributeValue("id")+ "#"+tokens[2]);
            if(StringUtils.hasText(imageName)) {
                addElement(rule, "imageName", null, imageName);
            }

            addElement(rule, "short", "fr", tokens[4]);
            addElement(rule, "long", "fr", tokens[5]);
            addElement(rule, "short", "en", tokens[6]);
            addElement(rule, "long", "en", tokens[7]);
            addElement(rule, "short", "de", tokens[8]);
            addElement(rule, "long", "de", tokens[9]);
    }

    private String getCondition(final String token) {
        String condition = token.toLowerCase().replaceAll(",", ".").trim();

        if (condition.indexOf("<") >= 0 || condition.indexOf(">") >= 0) {
            condition = condition.replaceAll("<", "value <");
            condition = condition.replaceAll(">", "value >");
            condition = condition.replaceAll(" and ", " && ");
            condition = condition.replaceAll(" or ", " || ");
        }
        else if ("true".equals(condition) || !StringUtils.hasText(condition)) {
            condition = "true";
        }
        else {
            if (condition.startsWith("=")) {
                condition = condition.substring(1);
            }
            condition = "value == " + condition;
        }

        return "final boolean condition = " + condition + ";";
    }

    private void addElement(final Element addTo, final String name,
            final String language, final String content) {
        final Element el = new Element(name);
        if (language != null) {
            el.setAttribute("language", language);
        }
        el.addContent(transform(content));
        addTo.getChildren().add(el);
    }

    private String transform(final String content) {
        String str = content;
        if(str.startsWith("\"")) {
            str = str.substring(1);
        }
        if(str.endsWith("\"")) {
            str = str.substring(0,str.length()-1);
        }
        str = str.replaceAll("\"\"","\"");
        str = str.replaceAll("&amp~","&");
        str = str.replaceAll("&gt~",">");
        str = str.replaceAll("&lt~","<");
        str = str.replaceAll("<bold>","");
        str = str.replaceAll("</bold>","");

        return str;
    }

    public static void main(String[] args) throws Exception {
        final ScreenerChecklistTransformer t = new ScreenerChecklistTransformer();
        t.setFile(new File(LocalConfigProvider.getProductionBaseDir(), "entwicklung/screener/doc/docs-20060313/Checklist_Partners_modified.csv"));
        t.setImageNamesFile(new File(LocalConfigProvider.getProductionBaseDir(), "entwicklung/istar/instrument/src/conf/screener-image-names.prop"));
        System.setOut(new PrintStream(new FileOutputStream(new File(LocalConfigProvider.getProductionBaseDir(), "entwicklung/screener/doc/data/screener.xconf"))));
        t.start();

    }
}
