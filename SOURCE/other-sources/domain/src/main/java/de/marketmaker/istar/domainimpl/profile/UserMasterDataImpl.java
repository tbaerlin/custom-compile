/*
 * UserMasterDataImpl.java
 *
 * Created on 14.07.2008 14:07:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import de.marketmaker.istar.domain.profile.UserMasterData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * UserMasterData impl based on data from vwd entitlement system<p>
 * See vwdUserStamm... requests in <a href="http://vwd-ent.market-maker.de:1968/">vwd-ent</a>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserMasterDataImpl implements UserMasterData, Serializable {
    static final long serialVersionUID = 2L;

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    private static final XPath X_PATH = XPathFactory.newInstance().newXPath();

    private static final Logger logger = LoggerFactory.getLogger(UserMasterDataImpl.class);

    private transient Node userNode;

    private final byte[] xml;

    public UserMasterDataImpl(byte[] xml) {
        this.xml = xml;
    }

    public String toString() {
        return "UserMasterDataImpl["
                + "vwdId=" + getVwdId()
                + ", genoId=" + getGenoId()
                + ", gisCustomerId=" + getGisCustomerId()
                + ", firstName=" + getFirstName()
                + ", lastName=" + getLastName()
                + ", centralBank=" + getCentralBank()
                + ", appTitle=" + getAppTitle()
                + ", mandatorId=" + getMandatorId()
                + "]";
    }

    @Override
    public Map<String, String> getStaticAccounts() {
        ensureDocument();
        final Map<String, String> login2Password = new HashMap<>();
        try {
            final NodeList list;
            synchronized (X_PATH) {
                list = (NodeList) X_PATH.evaluate("Accounts/Account[@type='1']", this.userNode, XPathConstants.NODESET);
            }
            for (int i = 0; i < list.getLength(); i++) {
                final Node node = list.item(i);
                final String login = nodeText("Login", node);
                final String password = nodeText("Passwd", node);
                if (StringUtils.hasText(login) && StringUtils.hasText(password)) {
                    login2Password.put(login.toUpperCase(), password);
                }
            }
        } catch (XPathExpressionException e) {
            logger.warn("<getStaticAccounts> failed", e);
        }
        return login2Password;
    }

    private void ensureDocument() {
        if (this.userNode == null) {
            try {
                final DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
                final Document document = builder.parse(new ByteArrayInputStream(this.xml));
                this.userNode = getUserNode(document);
            } catch (Exception e) {
                logger.error("<ensureDocument> failed", e);
            }
        }
    }

    private Node getUserNode(Document document) throws XPathExpressionException {
        synchronized (X_PATH) {
            return (Node) X_PATH.evaluate("/vwdUsers/vwdUser", document, XPathConstants.NODE);
        }
    }

    /**
     * Returns text of node specified by expr.
     * @param expr expression relative to "/DZUsers/DZUser"
     * @return text of node or null
     */
    @Override
    public String nodeText(String expr) {
        ensureDocument();
        return nodeText(expr, this.userNode);
    }

    private String nodeText(String expr, Node node) {
        ensureDocument();
        if (node == null) {
            return null;
        }

        try {
            synchronized (X_PATH) {
                final Node result = (Node) X_PATH.evaluate(expr, node, XPathConstants.NODE);
                return result != null ? result.getTextContent() : null;
            }
        } catch (XPathExpressionException e) {
            logger.warn("<nodeText> invalid expression: '" + expr + "'");
            return null;
        }
    }


    /**
     * Returns the String value of a node attribute
     * @param expr e.g.  Accounts/Account/@type to get the type value of the Account node
     * @return
     */
    @Override
    public String attributeText(String expr) {
        ensureDocument();
        return attributeText(expr, this.userNode);
    }

    private String attributeText(String expr, Node node) {
        ensureDocument();
        if (node == null) {
            return null;
        }

        try {
            synchronized (X_PATH) {
                return (String) X_PATH.evaluate(expr, node, XPathConstants.STRING);
            }
        } catch (XPathExpressionException e) {
            logger.warn("<attributeText> invalid expression: '" + expr + "'");
            return null;
        }
    }

    @Override
    public String getAppTitle() {
        return nodeText("TerminalTitel");
    }

    @Override
    public String getFirstName() {
        return nodeText("Vorname");
    }

    @Override
    public String getGenoId() {
        return nodeText("Accounts/Account[@type='1']/Login");
    }

    @Override
    public String getLastName() {
        return nodeText("Name");
    }

    @Override
    public String getVwdId() {
        return nodeText("vwdId");
    }

    @Override
    public String getMandatorId() {
        return nodeText("Mandator/@id");
    }

    @Override
    public String getGisCustomerId() {
        return nodeText("Mandator[@id='10']/Masterdata/Data[@type='10']/@value");
    }

    @Override
    public String getCentralBank() {
        return nodeText("Mandator/Masterdata/Data[@name='Zentralbank']/@value");
    }

    @Override
    public String getVdbLogin() {
        return nodeText("Accounts/Account[@type='3']/Login");
    }

    @Override
    public String getVdbPassword() {
        return nodeText("Accounts/Account[@type='3']/Passwd");
    }

    @Override
    public String getCustomerName() {
        return nodeText("Kunde");
    }

    @Override
    public Gender getGender() {
        final String g = nodeText("Gender");
        return "w".equals(g) ? Gender.FEMALE : ("m".equals(g) ? Gender.MALE : Gender.UNKNOWN);
    }

}
