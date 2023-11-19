/*
 * Resource2VwdProfileConverter.java
 *
 * Created on 15.07.2008 12:08:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.BitSet;

import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.common.util.EntitlementsVwd;

/**
 * Batch converts all Resource-Profiles to xml files with VwdProfile definitions
 *
 * Needs to read the document provided as
 * http://vwd-ent.market-maker.de:1968/vwdPermissions.asmx/SelectorDefinitions
 * in INPUT_DIR
 *
 * 
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Resource2VwdProfileConverter {
    private static class Sel {
        int id;
        String service;
        String subtype;

        private Sel(int id, String service, String subtype) {
            this.id = id;
            this.service = service;
            this.subtype = subtype;
        }
    }

    // where to read SelectorDefinitions.xml
    private static final String INPUT_DIR = "d:/";

    // where to write converted profiles
    private static final String OUTPUT_DIR = "d:/temp/profiles/";

    public static void main(String[] args) throws Exception {
        final Resource2VwdProfileConverter foo = new Resource2VwdProfileConverter();
        for(String key: ResourcePermissionProvider.getInstanceKeys()) {
            foo.convert(key);
        }

//        final VwdProfileFactory f = new VwdProfileFactory();
//        final VwdProfile vwdProfile = f.read(new FileInputStream("d:/temp/profiles/iview.xml"));
//        System.out.println(vwdProfile);
    }

    private Sel[] sels;

    public Resource2VwdProfileConverter() throws Exception {
        final SAXBuilder builder = new SAXBuilder();
        final Document document = builder.build(new FileInputStream(INPUT_DIR + "SelectorDefinitions.xml"));

        this.sels = new Sel[4000];

        final List<Element> children = document.getRootElement().getChildren("Sel");
        for (Element child : children) {
            final Sel sel = new Sel(Integer.parseInt(child.getAttributeValue("id")), child.getAttributeValue("service"),
                    child.getAttributeValue("subtype"));
            sels[sel.id] = sel;
        }
    }

    public void convert(String key) throws Exception {
        final Profile profile = ProfileFactory.createInstance(ResourcePermissionProvider.getInstance(key));
        Document d = new Document();
        final Element root = new Element("vwdTerminals");
        d.setRootElement(root);
        final Element terminal = addTo(root, "Terminal", null, "id", "1");
        final Element header = addTo(terminal, "Header", null);
        addTo(header, "Version", "0.7");
        addTo(header, "vwdId", key);
        addTo(header, "TerminalName", key);
        addTo(header, "created", "2008-06-09T20:03:48.293");
        addTo(header, "updated", "2008-06-09T20:03:48.293");
        addTo(header, "exported", "2008-06-09T20:03:48.293");
        addTo(header, "MaxLogins", "1");
        addTo(header, "UserType", "Standard", "id", "1");
        addTo(header, "Status", "active", "id", "1");

        final Element vwdPermissions = addTo(terminal, "vwdPermissions", null);

        addPrices(profile, vwdPermissions);
        addNews(profile, vwdPermissions);

        final XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        outputter.output(d, new FileOutputStream(OUTPUT_DIR + key + ".xml"));
    }

    private void addNews(Profile profile, Element vwdPermissions) {
        final Element news = addTo(vwdPermissions, "vwdPermission", null, "type", "Dataservice", "service", "VWDNews",
                "subtype", "Nachrichten");
        final Element selectors = addTo(news, "Selectors", null);
        addSelectors(selectors, profile.toEntitlements(Profile.Aspect.NEWS, null), "1", "Nachrichten");
    }

    private void addPrices(Profile profile, Element vwdPermissions) {
        final Element prices = addTo(vwdPermissions, "vwdPermission", null, "type", "Dataservice", "service", "VWDPrice",
                "subtype", "Kurs");
        final Element selectors = addTo(prices, "Selectors", null);
        addSelectors(selectors, profile.toEntitlements(Profile.Aspect.PRICE, PriceQuality.REALTIME), "1", "Kurs");
        addSelectors(selectors, profile.toEntitlements(Profile.Aspect.PRICE, PriceQuality.DELAYED), "2", "Kurs");
    }

    private void addSelectors(Element parent, BitSet bs, String qlt, final String subtype) {
        for (int i = bs.nextSetBit(0); i != -1; i = bs.nextSetBit(i + 1)) {
            if (sels[i] != null && sels[i].subtype.equals(subtype)) {
                final Element sel = addTo(parent, "Sel", null, "id", i + "", "name", EntitlementsVwd.toEntitlement(i));
                addTo(sel, "mq", null, "mode", "2", "qlt", qlt);
            }
        }
    }

    private Element addTo(Element parent, String name, String content, String... attrpairs) {
        Element e = new Element(name);
        if (content != null) {
            e.setText(content);
        }
        for (int i = 0; i < attrpairs.length; i += 2) {
            e.setAttribute(attrpairs[i], attrpairs[i+1]);
        }
        parent.addContent(e);
        return e;
    }

}
