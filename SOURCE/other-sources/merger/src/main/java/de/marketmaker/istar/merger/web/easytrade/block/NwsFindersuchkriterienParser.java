/*
 * NwsFindersuchkriterienParser.java
 *
 * Created on 15.08.2008 12:28:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;

/**
 * A method object used to parse lists definitions returned by a  NwsFindersuchkriterien atom
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NwsFindersuchkriterienParser {
    private XPath xpath;

    Map<String, NwsFindersuchkriterien.Realm> parse(Resource r) throws Exception {
        this.xpath = XPathFactory.newInstance().newXPath();
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document document = builder.parse(r.getInputStream());

        final Map<String, NwsFindersuchkriterien.Realm> result
                = new HashMap<>();

        final NodeList realms = findNodes(document, "//realm");
        for (int i = 0; i < realms.getLength(); i++) {
            Node realm = realms.item(i);
            final String name = getAttribute(realm, "name");
            result.put(name, new NwsFindersuchkriterien.Realm(parseLists(realm)));
        }
        return result;
    }

    private List<NwsFindersuchkriterien.ProfiledList> parseLists(Node realm) throws Exception {
        final List<NwsFindersuchkriterien.ProfiledList> result = new ArrayList<>();

        final NodeList lists = findNodes(realm, "list");
        for (int i = 0; i < lists.getLength(); i++) {
            result.add(parseList(lists.item(i)));
        }

        return result;
    }

    private NwsFindersuchkriterien.ProfiledList parseList(Node node) throws Exception {
        final NamedNodeMap attrs = node.getAttributes();

        final NwsFindersuchkriterien.ProfiledList result
                = new NwsFindersuchkriterien.ProfiledList(getLocalizedAttribute(attrs, "name"),
                getAttribute(attrs, "type"), getSelectors(node));

        final NodeList elements = findNodes(node, "element");
        for (int i = 0; i < elements.getLength(); i++) {
            result.add(toProviledElement(elements.item(i)));
        }

        return result;
    }

    private NwsFindersuchkriterien.ProfiledElement toProviledElement(final Node node) throws Exception {
        final NamedNodeMap attrs = node.getAttributes();
        return new NwsFindersuchkriterien.ProfiledElement(getAttribute(attrs, "key"),
                getLocalizedAttribute(attrs, "value"), getSelectors(node));
    }

    private Set<String> getSelectors(Node n) throws Exception {
        final Node selectors = findNode(n, "selectors");
        if (selectors == null) {
            return null;
        }
        final String s = selectors.getTextContent();
        final String[] tokens = s.trim().split(",");
        final Set<String> result = new HashSet<>();
        for (String token : tokens) {
            result.add(EntitlementsVwd.normalize(token));
        }
        return result;
    }

    private Node findNode(Node root, String expr) throws Exception {
        return (Node) this.xpath.evaluate(expr, root, XPathConstants.NODE);
    }

    private NodeList findNodes(Node root, String expr) throws Exception {
        return (NodeList) this.xpath.evaluate(expr, root, XPathConstants.NODESET);
    }

    private LocalizedString getLocalizedAttribute(NamedNodeMap attributes, String name) {
        final String defaultValue = getAttribute(attributes, name);
        if (defaultValue == null) {
            return null;
        }

        final LocalizedString.Builder b = new LocalizedString.Builder();
        b.add(defaultValue, LocalizedString.DEFAULT_LANGUAGE);

        for (Language lang : Language.values()) {
            String localizedValue = getAttribute(attributes, name + "_" + lang.getLocale().getLanguage());
            if (StringUtils.hasText(localizedValue)) {
                b.add(localizedValue, lang);
            }
        }
        return b.build();
    }

    private String getAttribute(NamedNodeMap attributes, String name) {
        final Node node = attributes.getNamedItem(name);
        return (node != null) ? node.getNodeValue() : null;
    }

    private String getAttribute(Node e, String name) throws Exception {
        final Node attr = findNode(e, "@" + name);
        return (attr != null) ? attr.getNodeValue() : null;
    }

    public static void main(String[] args) throws Exception {
        new NwsFindersuchkriterienParser().parse(new FileSystemResource(LocalConfigProvider.getIstarSrcDir() + "/news/src/conf/newsLists.xml"));
    }
}
