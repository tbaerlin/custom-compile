/*
 * KukaReader.java
 *
 * Created on 15.07.11 10:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.xml.AbstractSaxReader;

/**
 * @author oflege
 */
class KukaReader extends AbstractSaxReader {

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    private KukaCustomer.Builder builder;

    private Map<String, KukaCustomer> result = new HashMap<>(3000);

    private Set<String> currentAbo;

    private Map<String, Set<String>> abos = new HashMap<>();

    public InputSource resolveEntity(String publicId,
            String systemId) throws IOException, SAXException {
        if (systemId.endsWith(".dtd")) { // ignore dtd
            return new InputSource(new StringReader(""));
        }
        return null;
    }

    public void startElement(String uri, String localName, String tagName,
            Attributes attributes) throws SAXException {
        if ("kunde".equals(tagName)) {
            this.builder = new KukaCustomer.Builder(
                    attributes.getValue("kennung"),
                    attributes.getValue("kennwort")
            );
            this.currentAbo = new TreeSet<>();
        }
        else if (this.builder != null && "abo".equals(tagName)) {
            final String id = attributes.getValue("id");
            final String ende = attributes.getValue("laufzeitende");
            if (StringUtils.hasText(ende) && !DTF.parseDateTime(ende).isAfterNow()) {
                return;
            }
            this.currentAbo.add(id);
        }
        else if (this.builder != null && "kunde.abo".equals(tagName)) {
            final String id = attributes.getValue("symbol");
            final String ende = attributes.getValue("laufzeitende");
            if (!"0000-00-00".equals(ende) && StringUtils.hasText(ende) && !DTF.parseDateTime(ende).isAfterNow()) {
                return;
            }
            this.currentAbo.add(id);
        }
    }

    public void endElement(String uri, String localName, String tagName) throws SAXException {
        if ("kunde".equals(tagName)) {
            if (this.currentAbo.isEmpty()) {
                reset();
                return;
            }
            this.builder.setAbos(getAbos());
            final KukaCustomer customer = this.builder.build();
            if (customer != null) {
                this.result.put(customer.getKennung(), customer);
            }
            reset();
        }
    }

    protected void reset() {
        this.builder = null;
    }

    private Set<String> getAbos() {
        final String key = this.currentAbo.toString();
        final Set<String> existing = this.abos.get(key);
        if (existing != null) {
            return existing;
        }
        this.abos.put(key, this.currentAbo);
        return this.currentAbo;
    }

    Map<String, KukaCustomer> getResult() {
        this.logger.info("<getResult> #Customers=" + this.result.size() + ", #Abos=" + this.abos.size());
        return result;
    }

    public static void main(String[] args) throws Exception {
        final File baseDir = LocalConfigProvider.getProductionBaseDir();
        final KukaReader kuka = new KukaReader();
        kuka.read(new File(baseDir, "var/data/profile/kuka/kunden.xml"));
        kuka.getResult();
        final KukaReader wiso = new KukaReader();
        wiso.read(new File(baseDir, "var/data/profile/wiso/kunden.xml"));
        System.out.println(wiso.getResult().get("FPZU8-X7ZVV-784Z4-446YJ-BVG9A"));
    }
}
