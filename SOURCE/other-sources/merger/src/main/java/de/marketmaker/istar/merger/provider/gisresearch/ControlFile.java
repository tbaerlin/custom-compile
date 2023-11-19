/*
 * ControlFileReader.java
 *
 * Created on 18.03.14 15:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.jcip.annotations.Immutable;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.marketmaker.istar.common.util.LocalConfigProvider;

import static de.marketmaker.istar.merger.provider.gisresearch.DocumentType.valueOfWithMapping;

/**
 * @author oflege
 */
@Immutable
class ControlFile implements Comparable<ControlFile> {

    private static class Reader {

        private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

        private static final XPathFactory X_PATH_FACTORY = XPathFactory.newInstance();

        private static final String TIME_ENTITLEMENT =
                "Research/Product/Context/ProductDetails/EntitlementGroup/Entitlement/TimeEntitlement/";

        private final XPath xPath;

        private final Document doc;

        private String resourceId;

        private Reader(File f) throws Exception {
            synchronized (DOCUMENT_BUILDER_FACTORY) {
                this.doc = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().parse(f);
            }
            synchronized (X_PATH_FACTORY) {
                this.xPath = X_PATH_FACTORY.newXPath();
            }
        }

        public String getName() throws Exception {
            return getText("Research/Product/Content/Resource/Name");
        }

        public String getResearchId() throws Exception {
            return getAttribute("Research/@researchID");
        }

        public DateTime getPublicationDate() throws Exception {
            return DateTime.parse(getAttribute("Research/Product/Context/ProductDetails/@publicationDateTime"));
        }

        public DateTime getStart() throws Exception {
            String startDateTime = getAttribute(TIME_ENTITLEMENT + "@startDateTime");
            if (!StringUtils.hasText(startDateTime)) {
                startDateTime = getAttribute("Research/Product/StatusInfo[@statusType='Published']/@statusDateTime");
            }
            if (!StringUtils.hasText(startDateTime)) {
                throw new IllegalStateException("no value for getStart()");
            }
            return DateTime.parse(startDateTime);
        }

        public DateTime getEnd() throws Exception {
            final String endDateTime = getAttribute(TIME_ENTITLEMENT + "@endDateTime");
            return StringUtils.hasText(endDateTime)
                    ? DateTime.parse(endDateTime)
                    : getStart().plusMonths(13).plusDays(1).withTimeAtStartOfDay();
        }

        public List<String> getProductEntitlements() throws Exception {
            NodeList nodes = getNodeList(this.doc, "Research/Product/Context/ProductDetails/EntitlementGroup/Entitlement/ProductEntitlement");
            if (nodes == null || nodes.getLength() == 0) {
                return Collections.emptyList();
            }
            ArrayList<String> result = new ArrayList<>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getTextContent().trim());
            }
            return result;
        }

        public String getPrimaryIsin() throws Exception {
            String withPrimaryIndicator = getAttribute("Research/Product/Context/IssuerDetails/Issuer[@primaryIndicator='Yes']/SecurityDetails/Security[@primaryIndicator='Yes']/SecurityID[@idType='ISIN']/@idValue");
            if (withPrimaryIndicator != null) {
                return withPrimaryIndicator;
            }
            final List<String> isins = getIsins();
            return !isins.isEmpty() ? isins.get(0) : null;
        }

        public List<String> getIsins() throws Exception {
            return getAttributes("Research/Product/Context/IssuerDetails/Issuer/SecurityDetails/Security/SecurityID[@idType='ISIN']", "@idValue");
        }

        public DocumentType getDocumentType() throws Exception {
            this.resourceId = getAttribute("Research/Product/Content/Resource/@resourceID");
            return valueOfWithMapping(this.resourceId);
        }

        public GisResearchIssuer getPrimaryIssuer() throws Exception {
            Node node = getNode(doc, "Research/Product/Context/IssuerDetails/Issuer[@primaryIndicator='Yes']");
            if (node != null) {
                return getIssuer(node);
            }
            final List<GisResearchIssuer> issuers = getIssuers();
            return !issuers.isEmpty() ? issuers.get(0) : null;
        }

        public List<GisResearchIssuer> getIssuers() throws Exception {
            NodeList nodes = getNodeList(doc, "Research/Product/Context/IssuerDetails/Issuer");
            if (nodes == null || nodes.getLength() == 0) {
                return Collections.emptyList();
            }
            List<GisResearchIssuer> result = new ArrayList<>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(getIssuer(nodes.item(i)));
            }
            return result;
        }

        private GisResearchIssuer getIssuer(Node item) throws Exception {
            String number = getAttribute(item, "IssuerID[@publisherDefinedValue='Emittentennummer']/@idValue");
            String name = getText(item, "IssuerName[@nameType='Display']/NameValue");
            return new GisResearchIssuer(number, name);
        }

        public List<String> getCountries() throws Exception {
            return getAttributes("Research/Product/Context/ProductClassifications/Country", "@code");
        }

        public List<String> getIndexes() throws Exception {
            return getAttributes("Research/Product/Context/ProductClassifications/Index", "@name");
        }

        public String getRecommendation() throws Exception {
            return getText("Research/Product/Context/IssuerDetails/Issuer/SecurityDetails/Security/Recommendation");
        }

        public String getRiskGroup() throws Exception {
            return getText("Research/Product/Context/IssuerDetails/Issuer/SecurityDetails/Security/RiskGroup");
        }

        public String getTitle() throws Exception {
            return getText("Research/Product/Content/Title");
        }

        public Boolean getStatusTypeDeleted() throws Exception {
            String s = getAttribute("Research/Product/StatusInfo/@statusType");
            return (s != null) ? "Deleted".equals(s) : null;
        }


        private Node getNode(Node node, String xPathExpression) throws Exception {
            return (Node) xPath.evaluate(xPathExpression, node, XPathConstants.NODE);
        }

        private NodeList getNodeList(Node node, String xPathExpression) throws Exception {
            return (NodeList) xPath.evaluate(xPathExpression, node, XPathConstants.NODESET);
        }

        private String getText(String xpath) throws Exception {
            return getText(this.doc, xpath);
        }

        private String getText(Node n, String xpath) throws Exception {
            Node node = getNode(n, xpath);
            return (node != null) ? node.getTextContent().trim() : null;
        }

        private String getAttribute(String xpath) throws Exception {
            return getAttribute(this.doc, xpath);
        }

        private String getAttribute(Node src, String xpath) throws Exception {
            Node node = getNode(src, xpath);
            return (node != null) ? node.getNodeValue() : null;
        }

        private List<String> getAttributes(String nodesXpath, String attribute) throws Exception {
            NodeList nodes = getNodeList(this.doc, nodesXpath);
            if (nodes == null || nodes.getLength() == 0) {
                return Collections.emptyList();
            }
            ArrayList<String> result = new ArrayList<>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(getAttribute(nodes.item(i), attribute));
            }
            return result;
        }

        public ControlFile build() {
            return null;
        }
    }

    final File file;

    final String name;

    final String researchId;

    final DateTime start;

    final DateTime end;

    final List<String> productEntitlements;

    final List<String> isins;

    final DocumentType documentType;

    final String resourceId;

    final List<GisResearchIssuer> issuers;

    final List<String> countries;

    final List<String> indexes;

    final String recommendation;

    final String riskGroup;

    final String title;

    final DateTime publicationDate;

    final Boolean statusTypeDeleted;

    static ControlFile create(File f) throws Exception {
        return new ControlFile(f, new Reader(f));
    }

    private ControlFile(File f, Reader r) throws Exception {
        this.file = f;
        this.name = r.getName();
        this.researchId = r.getResearchId();
        this.start = r.getStart();
        this.end = r.getEnd();
        this.productEntitlements = r.getProductEntitlements();
        this.isins = withPrimaryFirst(r.getIsins(), r.getPrimaryIsin());
        this.documentType = r.getDocumentType();
        this.resourceId = r.resourceId;
        this.issuers = withPrimaryFirst(r.getIssuers(), r.getPrimaryIssuer());
        this.countries = r.getCountries();
        this.indexes = r.getIndexes();
        this.recommendation = r.getRecommendation();
        this.riskGroup = r.getRiskGroup();
        this.title = r.getTitle();
        this.publicationDate = r.getPublicationDate();
        this.statusTypeDeleted = r.getStatusTypeDeleted();
    }

    private static <T> List<T> withPrimaryFirst(List<T> elements, T primary) {
        if (primary == null || primary.equals(elements.get(0))) {
            return elements;
        }
        if (!elements.remove(primary)) {
            throw new IllegalStateException("primary " + primary + " not in " + elements);
        }
        elements.add(0, primary);
        return elements;
    }

    @Override
    public String toString() {
        return "ControlFile{" +
                "file='" + file.getName() + '\'' +
                ", name='" + name + '\'' +
                ", researchId='" + researchId + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", productEntitlements='" + productEntitlements + '\'' +
                ", isins=" + isins +
                ", documentType=" + documentType +
                ", issuers=" + issuers +
                ", countries=" + countries +
                ", indexes=" + indexes +
                ", recommendation='" + recommendation + '\'' +
                ", riskGroup='" + riskGroup + '\'' +
                ", title='" + title + '\'' +
                ", publicationDate=" + publicationDate +
                ", statusTypeDeleted=" + statusTypeDeleted +
                '}';
    }

    @Override
    public int compareTo(ControlFile o) {
        return this.file.getName().compareTo(o.file.getName());
    }

    File getTxtFile() {
        return fileWithSuffix(".txt");
    }

    String getPrimaryIsin() {
        return !this.isins.isEmpty() ? this.isins.get(0) : null;
    }

    GisResearchIssuer getPrimaryIssuer() {
        return !this.issuers.isEmpty() ? this.issuers.get(0) : null;
    }

    List<String> issuerNumbers() {
        if (this.issuers.isEmpty()) {
            return Collections.emptyList();
        }
        return issuers.stream()
                .map(issuer -> issuer.number)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    File getPdfFile() {
        return fileWithSuffix(".pdf");
    }

    private File fileWithSuffix(final String suffix) {
        return new File(this.file.getParentFile(), this.file.getName().replaceFirst("\\.xml$", suffix));
    }

    public static void main(String[] args) throws Exception {
        File dir = LocalConfigProvider.getProductionDir("var/data/gisresearch/current");
        System.out.println(create(new File(dir, "StCredits_Daily_13012014_b6d767d2-f8ed-3d21-a44b-0e5886680cb9.xml")));
    }
}
