/*
 * WebSimAnalysesBuilder.java
 *
 * Created on 19.04.12 11:52
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.marketmaker.istar.domain.util.IsinUtil;

/**
 * @author oflege
 */
class WebSimAnalysesBuilder {

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy.MM.dd.HH.mm.ss");

    private final XPath xPath = XPathFactory.newInstance().newXPath();

    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    private String getString(Node node, String xPathExpression) throws Exception {
        final String s = (String) xPath.evaluate(xPathExpression, node, XPathConstants.STRING);
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    private Node getNode(Node node, String xPathExpression) throws Exception {
        return (Node) xPath.evaluate(xPathExpression, node, XPathConstants.NODE);
    }

    private NodeList getNodeList(Node node, String xPathExpression) throws Exception {
        return (NodeList) xPath.evaluate(xPathExpression, node, XPathConstants.NODESET);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    Protos.Analysis.Builder createBuilderFrom(File file) throws Exception {
        Document d = factory.newDocumentBuilder().parse(file);

        final Protos.Analysis.Builder builder = Protos.Analysis.newBuilder()
            .setId(AnalysisIdFactory.getDigest(FileCopyUtils.copyToByteArray(file)))
            .setProvider(Protos.Analysis.Provider.WEBSIM)
            .setDate(System.currentTimeMillis())
            .addCategory(getString(d, "WSBBS_RISPOSTA/FUNZIONI/FUNZIONE/@CLASS"));

        Node documento = getNode(d, "WSBBS_RISPOSTA/FUNZIONI/FUNZIONE/DATI_OUTPUT/DOCUMENTO");
        builder.setAgencyDate(DTF.parseDateTime(getString(documento, "@DATAP")).getMillis());
        builder.setAgencyId(getString(documento, "@IDDOC"));

        Node principale = getNode(documento, "PRINCIPALE");
        builder.setHeadline(getString(principale, "TITOLO"));

        final String text1 = getString(principale, "CORPI/CORPO[@ID='1']");
        builder.addText(text1);
        final String text2 = getString(principale, "CORPI/CORPO[@ID='2']");
        if (text2 != null) {
            builder.addText(text2);
        }

        String raccfond = getString(principale, "RACCFOND");
        if (raccfond != null) {
            builder.setWebSimRaccfond(raccfond);
        }
        String racctecn = getString(principale, "RACCTECN");
        if (racctecn != null) {
            builder.setWebSimRacctecn(racctecn);
        }

        final Node strategie = getNode(principale, "STRATEGIE");
        if (strategie != null) {
            builder.setWebSimStrategy(buildStrategy(strategie, false));
            builder.setWebSimDistStrategy(buildStrategy(strategie, true));
        }

        final NodeList graficos = getNodeList(principale, "GRAFICO_AT");
        for (int i = 0; i < graficos.getLength(); i++) {
            String imageRef = getString(graficos.item(i), "@IDIMG");
            if (imageRef != null) {
                builder.addImageRef(imageRef);
            }
        }

        final Node accessori = getNode(documento, "ACCESSORI");

        final NodeList isins = getNodeList(accessori, "ISINCODES/ISINCODE");
        for (int i = 0; i < isins.getLength(); i++) {
            String isin = isins.item(i).getTextContent().trim();
            if (IsinUtil.isIsin(isin)) {
                builder.addSymbol(isin);
            }
        }

        final NodeList authors = getNodeList(accessori, "AUTHORS/AUTHOR");
        for (int i = 0; i < authors.getLength(); i++) {
            Node author = authors.item(i);
            String id = toAnalystName(author.getTextContent().trim(), getString(author, "@EMAIL"));
            if (id != null) {
                builder.addAnalystName(id);
            }
        }

        return builder;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Protos.WebSimStrategy buildStrategy(Node strategie, boolean dist) throws Exception {
        final Protos.WebSimStrategy.Builder builder = Protos.WebSimStrategy.newBuilder();
        final String p = dist ? "DIST_" : "";
        builder.setStrengthEntry(getString(strategie, p + "FORZA_ENTRY"));
        builder.setStrengthTarget1(getString(strategie, p + "FORZA_TARGET_1"));
        builder.setStrengthTarget2(getString(strategie, p + "FORZA_TARGET_2"));
        builder.setStrengthStop(getString(strategie, p + "FORZA_STOP"));
        builder.setWeaknessEntry(getString(strategie, p + "DEBOLEZZA_ENTRY"));
        builder.setWeaknessTarget1(getString(strategie, p + "DEBOLEZZA_TARGET_1"));
        builder.setWeaknessTarget2(getString(strategie, p + "DEBOLEZZA_TARGET_2"));
        builder.setWeaknessStop(getString(strategie, p + "DEBOLEZZA_STOP"));
        return builder.build();
    }

    private String toAnalystName(String name, String email) {
        if (StringUtils.hasText(name)) {
            return (StringUtils.hasText(email))
                    ? name + " <" + email + ">"
                    : name;
        }
        return StringUtils.hasText(email) ? email : null;
    }

}
