/*
 * XmlTool.java
 *
 * Created on 10.09.2008 15:43:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.convensys;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class XmlTool {
    private static final Logger logger = LoggerFactory.getLogger(XmlTool.class);

    private static final Set<String> ALLOWED_TAGS_IN_TEXT =
            new HashSet<>(Arrays.asList("table", "tr", "th", "td", "div", "span", "br", "p"));

    public static void main(String[] args) throws Exception {
        /*final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        builder.reset();
        final Document d = builder.parse(new File(args[0]));
        final XmlTool xml = new XmlTool(d);
        final String v = xml.getNodeText(args[1]);
        System.out.println("v = " + v);*/

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
//        builder.reset();
        final Document d = builder.parse(new File(args[0]));
        final XmlTool xml = new XmlTool(d);
        xml.setCurrent(xml.getNode("xml"));

        List<List<Node>> rows = xml.getIlSole24OreBreakDownModel("BreakDown1/item");
        for(List<Node> row : rows) {
            for(Node col : row) {
                System.out.print(col.getNodeName());
                System.out.print(", ");
            }
            System.out.println();
        }
    }

    private Node current;

    private final Document document;

    private final XPath xPath = XPathFactory.newInstance().newXPath();

    private boolean escapeHtmlInNodeText;

    /**
     * Create an XmlTool, when the document was already loaded.
     *
     * @param document       .
     * @param escapeHtmlInNodeText .
     */
    public XmlTool(Document document, boolean escapeHtmlInNodeText) {
        this.document = document;
        this.current = document;
        this.escapeHtmlInNodeText = escapeHtmlInNodeText;
    }

    /**
     * Create an XmlTool, when the document was already loaded.
     *
     * @param document
     */
    public XmlTool(Document document) {
        this(document, true);
    }

    /**
     * Provide the value of the attribute with the given attributeName of the first node that
     * fits the given xPathExpression.
     *
     * @param xPathExpression
     * @param attributeName
     * @return The value of the attribute.
     */
    public String getAttributeValue(String xPathExpression, String attributeName) {
        return getAttributeValue(this.current, xPathExpression, attributeName);
    }

    /**
     * Provide the value of the attribute with the given attributeName of the first node that
     * fits the given xPathExpression.
     *
     * @param node            Only nodes below this node are evaluated
     * @param xPathExpression
     * @param attributeName
     * @return The value of the attribute.
     */
    public String getAttributeValue(Node node, String xPathExpression, String attributeName) {
        final Node subNode = getNode(node, xPathExpression);
        return subNode != null ? getAttributeValue(subNode, attributeName) : null;
    }

    /**
     * Provide the value of the attribute with the given attributeName of the given node
     *
     * @param node          with attribute
     * @param attributeName
     * @return The value of the attribute or null if no such attribute exists
     */
    public String getAttributeValue(Node node, String attributeName) {
        final Node attributeNode = node.getAttributes().getNamedItem(attributeName);
        return attributeNode != null ? attributeNode.getNodeValue() : null;
    }

    /**
     * Provide the content of the given node and its sub nodes as String.
     *
     * @param node
     * @return An xml fragment of the given node and all its sub nodes as String.
     */
    public String getContent(Node node) {
        final StringBuilder sb = new StringBuilder();
        append(sb, node);
        return sb.toString();
    }

    /**
     * Provide the document.
     *
     * @return The document.
     */
    public Document getDocument() {
        return this.document;
    }

    /**
     * Evaluate the xPathExpression and provide all nodes that fit the expression
     *
     * @param xPathExpression
     * @return A List of all nodes that fit the given xPathExpression
     *         or null, if no such node exists or the expression cannot be evaluated
     */
    public List<Node> getList(String xPathExpression) throws XPathExpressionException {
        return getList(getNodeList(xPathExpression));
    }

    /**
     * Evaluate the xPathExpression below the given node and provide all nodes that fit the expression.
     *
     * @param node            Only nodes below this node are evaluated
     * @param xPathExpression
     * @return A List of all nodes that fit the given xPathExpression
     *         or null, if no such node exists or the expression cannot be evaluated
     */
    public List<Node> getList(final Node node, final String xPathExpression) throws XPathExpressionException {
        return getList(getNodeList(node, xPathExpression));
    }

    /**
     * Converts the given NodeList to a reverse List of nodes.
     *
     * @param nodeList
     * @return A List object containing all nodes of the given NodeList in reverse order.
     */
    public List<Node> getList(final NodeList nodeList) {
        if (nodeList == null) {
            return null;
        }
        final List<Node> list = new ArrayList<>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            list.add(nodeList.item(i));
        }
        return list;
    }


    /**
     * Converts the given NodeList to a List of nodes in reverse order.
     *
     * @param xPathExpression .
     * @param maxCount .
     * @return A List object containing last maxCount nodes of the given nodeList in reverse order.
     * @throws XPathExpressionException .
     */

    public List<Node> getReverseList(String xPathExpression, int maxCount) throws XPathExpressionException {
        final List<Node> list = getList(xPathExpression);
        return getReverseList(list, maxCount);
    }

    /**
     * Converts the given NodeList to a List of nodes in reverse order.
     *
     * @param list .
     * @param maxCount .
     * @return A List object containing last maxCount nodes of the given nodeList in reverse order.
     * @throws XPathExpressionException .
     */

    public List<Node> getReverseList(List<Node> list, int maxCount) throws XPathExpressionException {
        Collections.reverse(list);
        return list.size() > maxCount ? list.subList(0, maxCount) : list;
    }

    /**
     * Converts the given NodeList to a List of nodes in reverse order.
     *
     * @param xPathExpression .
     * @return A List object containing all nodes of the given nodeList in reverse order.
     * @throws XPathExpressionException .
     */

    public List<Node> getReverseList(String xPathExpression) throws XPathExpressionException {
        final List<Node> list = getList(xPathExpression);
        return getReverseList(list, list.size());
    }

    /**
     * Special solution for Participationlist
     *
     * @return Participationlist sortet by percent and company
     * @throws XPathExpressionException .
     */
    public List<Node> getSortedParticipationList() throws XPathExpressionException {
        final Collator collator = Collator.getInstance(Locale.GERMANY);
        final List<Node> list = getList("participations/participation");
        list.sort(new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                double percent1 = getDoubleAttribute(o1, "percent");
                double percent2 = getDoubleAttribute(o2, "percent");
                final int percentCompare = Double.compare(percent2, percent1);
                if (percentCompare != 0) {
                    return percentCompare;
                } else {
                    return collator.compare(o1.getTextContent().toLowerCase(), o2.getTextContent().toLowerCase());
                }
            }
            private double getDoubleAttribute(Node node, String attribute) {
                final String sValue = getAttributeValue(node, attribute);
                if (sValue == null || sValue.isEmpty()) {
                    return Double.MIN_VALUE;
                }
                try {
                    return Double.parseDouble(sValue);
                }
                catch (NumberFormatException e) {
                    return Double.MIN_VALUE;
                }
            }
        });
        return list;
    }

    /**
     * Shorten an array and turn it into a list.
     * Shortening is done by cutting off all empty elements at the end of the array so the last
     * element of the list is always not empty. The returned list is empty if all elements in the array are empty.
     *
     * @param result array of not null string values
     * @return list of Strings, last element is not null and not empty
     */
    protected List<String> createList(final String[] result) {
        int last = -1;
        for (int i = result.length-1; i >= 0; i--) { // walk from last to first
            // the first not empty string is the last element in the result list
            if (result[i].length() > 0) {
                last = i;
                break;
            }
        }
        return Arrays.asList(Arrays.copyOf(result, last+1));
    }

    /**
     * Read the quarter amounts from a year node.
     *
     * @param yearNode xml node of the year which should contain the quarter data
     * @return a list of string max size of the list is 3
     * @throws XPathExpressionException
     */
    protected List<String> readQuarterAmounts(final Node yearNode) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList quarterNodes = (NodeList) xPath.evaluate("quartal[@nr!=\"\" and @amount!=\"\" and  @amount!=\"-\"]", yearNode, XPathConstants.NODESET);
        int quarterCount = quarterNodes.getLength();
        String[] quarters = new String[] {"","",""};
        for (int i = 0; i < quarterCount; i++) {
            String amount = quarterNodes.item(i).getAttributes().getNamedItem("amount").getTextContent().trim();
            String nr = quarterNodes.item(i).getAttributes().getNamedItem("nr").getTextContent().trim();
            int j = Integer.valueOf(nr) - 1;
            quarters[j] = amount;
        }
        return createList(quarters);
    }


    /**
     * Read a list of month amounts from a year node.
     *
     * @param yearNode
     * @return a 12 element array with the value of the amount property of the month tags within a year tag
     * @throws XPathExpressionException
     */
    protected BigDecimal[] readMonthAmounts(final Node yearNode) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        BigDecimal[] month = new BigDecimal[12];
        NodeList monthNodes = (NodeList) xPath.evaluate("month[@nr!=\"\" and @amount!=\"\" and  @amount!=\"-\"]", yearNode, XPathConstants.NODESET);
        int monthCount = monthNodes.getLength();
        for (int i = 0; i < monthCount; i++) {
            String amount = monthNodes.item(i).getAttributes().getNamedItem("amount").getTextContent().trim();
            String nr = monthNodes.item(i).getAttributes().getNamedItem("nr").getTextContent().trim();
            int j = Integer.valueOf(nr) - 1;
            month[j] = new BigDecimal(amount);
        }
        return month;
    }


    /**
     * Calculate the quarter values from the month values in a year node.
     *
     * @param yearNode the d´node containing month elements
     * @return List of quarter values
     * @throws XPathExpressionException
     */
    protected List<String> turnMonthAmountsIntoQuarters(final Node yearNode) throws XPathExpressionException {
        BigDecimal[] month = readMonthAmounts(yearNode);

        // month are accumulated values, we have to substract to get the total amount per quarter
        String[] quarters = new String[] {"","",""};
        if (month[2] != null) {  // this is March!
            quarters[0] = month[2].toString();
        }
        if (month[5] != null && month[2] != null) {
            quarters[1] = month[5].subtract(month[2]).toString();
        }
        if (month[8] != null && month[5] != null) {
            quarters[2] = month[8].subtract(month[5]).toString();
        }

        return createList(quarters);
    }


    /**
     * Calculate or read the quarter data from a year node, each list element corresponds to the value
     * of a single quarter, elements at the end of the list are cut off if the amount value is empty.
     *
     * @param yearNode the xml node that contains quarter or month data (uses a "year" tag)
     * @return an ordered list of quarterly amount data, each list item contains the value for a
     * single quarter, the last element of the list has a non-null and non-empty value for a quarter.
     * If there are no quarter data found or no quarter data can be derived from the month elements
     * an empty list is returned
     * @throws XPathExpressionException
     */
    public List<String> getQuarterAmountsForYear(final Node yearNode) {

        List<String> result = new ArrayList<>();
        try {
            if ((result = readQuarterAmounts(yearNode)).size() > 0) {
                return result;
            }
            if ((result = turnMonthAmountsIntoQuarters(yearNode)).size() > 0) {
                return result;
            }
        } catch (XPathExpressionException e) {
            logger.warn("failed to filter quarterly data from year node, "
                    + "returning an empty set of amount data, reason: " + e);
        } catch (NumberFormatException e) {
            logger.warn("failed to filter quarterly data from year node, "
                    + "returning an empty set of amount data, reason: " + e);
        }
        return result;
    }


    /**
     * Creates a list of amount values, each list element corresponds to the value of a single month.
     * Elements at the end of the list are cut off if the amount value is empty.
     *
     * @param yearNode the xml node that contains month data
     * @return an ordered list of monthly data, each list item contains the value for a single month
     * the last element of the list has a non-null and non-empty value. If there are no data found
     * an empty list is returned
     * @throws XPathExpressionException
     */
    public List<String> getMonthAmountsForYear(final Node yearNode) {
        BigDecimal[] month = new BigDecimal[0];
        try {
            month = readMonthAmounts(yearNode);
        } catch (XPathExpressionException e) {
            logger.warn("failed to filter monthly data from year node, using empty values, reason: " + e);
        } catch (NumberFormatException e) {
            logger.warn("failed to filter monthly data from year node, using empty values, reason: " + e);
        }
        String[] stringValues = new String[12];
        for (int i = 0; i < stringValues.length; i++) {
            if (i < month.length && month[i] == null) {
                stringValues[i] = ""; // we don't want null values in the array
            } else {
                stringValues[i] = month[i].toString();
            }
        }
        return createList(stringValues);
    }


    /**
     * A very special solution for Il Sole 24 Ore AMF XML BreakDownX elements
     *
     * @return A list with nodes that can be used within velocity's foreach to build a table.
     * @throws XPathExpressionException
     */
    public List<List<Node>> getIlSole24OreBreakDownModel(String baseNodeXPathExpression) throws XPathExpressionException {
        if (baseNodeXPathExpression == null) {
            return null;
        }

        final List<Node> rows = getList(baseNodeXPathExpression);
        final int maxColumns = maxIlSole24OreBreakDownColumns(rows);
        final List<List<Node>> rowModel = new ArrayList<>();

        //delete unnecessary nodes add empty nodes according to
        // the max number of columns if these are missing.
        for(Node row : rows) {
            List<Node>children = getList(row.getChildNodes());
            Node[] cols = new Node[maxColumns];
            Node voiceCol = null;

            //remove unnecessary nodes (empty text nodes) and find columns
            ListIterator<Node> it = children.listIterator();
            while(it.hasNext()) {
                final Node node = it.next();
                if(node != null) {
                    final String nodeName = node.getNodeName();
                    if(nodeName != null) {
                        if(nodeName.equals("voice")) {
                            voiceCol = node;
                        }
                        else if(nodeName.startsWith("col")) {
                            final String colIndexStr = nodeName.replace("col", "");
                            final int colIndex = Integer.parseInt(colIndexStr);
                            cols[colIndex-1] = node;

                        }
                    }
                }
            }

            //create missing column nodes
            boolean containsOne = false;
            for(int i = 0; i < cols.length; i++) {
                if(cols[i] == null) {
                    cols[i] = document.createElement("col" + (i + 1));
                }
                else {
                    containsOne = true;
                }
            }

            //check if this row contains any elements.
            if(voiceCol == null && !containsOne) {
                continue;
            }

            //check if this row contains only the voice element
            //this indicates that a new snippet should start.
            if(voiceCol != null && !containsOne) {
                children.clear();
                children.add(voiceCol);
                rowModel.add(children);
                continue;
            }

            //add missing and removed column nodes
            if(voiceCol == null) {
                voiceCol = document.createElement("voice");
            }
            children.clear();
            children.add(voiceCol);
            children.addAll(Arrays.asList(cols));
            rowModel.add(children);
        }

        return rowModel;
    }

    public int getIlSole24OreBreakDownColCount(String baseNodeXPathExpression) throws XPathExpressionException {
        return maxIlSole24OreBreakDownColumns(getList(baseNodeXPathExpression));
    }

    private int maxIlSole24OreBreakDownColumns(List<Node> rows) {
        int columnCount = 0;
        for(Node row : rows) {
            int currentCols = 0;
            for(Node col : getList(row.getChildNodes())) {
                if(col.getNodeName().startsWith("col")) {
                    final String colIndexStr = col.getNodeName().replace("col", "");
                    final int colIndex = Integer.parseInt(colIndexStr);
                    currentCols = colIndex;
                }
            }
            if(currentCols > columnCount) {
                columnCount = currentCols;
            }
        }
        return columnCount;
    }


    /**
     * Evaluate the xPathExpression and provide the first node that fits the expression
     *
     * @param xPathExpression
     * @return The first node that fits the given xPathExpression
     *         or null, if no such node exists or the expression cannot be evaluated
     */
    public Node getNode(String xPathExpression) {
        return getNode(this.current, xPathExpression);
    }

    /**
     * Evaluate the xPathExpression below the given node and provide the first node that fits the expression.
     *
     * @param node            Only nodes below this node are evaluated
     * @param xPathExpression
     * @return The first node below the given node that fits the given xPathExpression
     *         or null, if no such node exists or the expression cannot be evaluated
     */
    public Node getNode(Node node, String xPathExpression) {
        if (node == null) {
            return null;
        }

        try {
            return (Node) xPath.evaluate(xPathExpression, node, XPathConstants.NODE);
        }
        catch (XPathExpressionException e) {
            logger.warn("failed to evaluate xpath expression: '" + xPathExpression + "' error: " + e);
            return null;
        }
    }

    /**
     * Evaluate the xPathExpression and provide all nodes that fit the expression
     *
     * @param xPathExpression
     * @return A NodeList of all nodes that fit the given xPathExpression
     *         or null, if no such node exists or the expression cannot be evaluated
     */
    public NodeList getNodeList(String xPathExpression) throws XPathExpressionException {
        return getNodeList(this.current, xPathExpression);
    }

    /**
     * Evaluate the xPathExpression below the given node and provide all nodes that fit the expression.
     *
     * @param node            Only nodes below this node are evaluated
     * @param xPathExpression
     * @return A NodeList of all nodes that fit the given xPathExpression
     *         or null, if no such node exists or the expression cannot be evaluated
     */
    public NodeList getNodeList(Node node, String xPathExpression) {
        if (node == null) {
            return null;
        }

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            return (NodeList) xPath.evaluate(xPathExpression, node, XPathConstants.NODESET);
        }
        catch (XPathExpressionException e) {
            logger.error("failed to evaluate xpath expression: " + xPathExpression, e);
            return null;
        }
    }

    /**
     * Provide the value of the first node that fits the given xPathExpression
     *
     * @param xPathExpression
     * @return The value of the first node that fits the given xPathExpression
     *         or null, if no such node exists or the expression cannot be evaluated
     */
    public String getNodeText(String xPathExpression) {
        return getNodeText(this.current, xPathExpression);
    }

    /**
     * Provide the value of the first node below the given node that fits the given xPathExpression
     *
     * @param node            Only nodes below this node are evaluated
     * @param xPathExpression
     * @return The value of the first node below the given node that fits the given xPathExpression
     *         or null, if no such node exists or the expression cannot be evaluated
     */
    public String getNodeText(Node node, String xPathExpression) {
        Node subNode = getNode(node, xPathExpression);
        return getNodeText(subNode);
    }

    public String getNodeText(Node node) {
        return node != null ? node.getTextContent() : null;
    }

    public String getTextContent(String xPathExpression) {
        Node subNode = getNode(this.current, xPathExpression);
        return subNode != null ? getTextContent(subNode) : null;
    }

    /**
     * Provide the content of the given node and its sub nodes as String.
     * Only the tags &lt;table&gt;, &lt;tr&gt;, &lt;td&gt;, &lt;div&gt;, &lt;span&gt;, &lt;p&gt;
     * are evaluated and sub nodes of &lt;a&gt;.
     * The attributes of the xml nodes are omitted a class attribute with the given styleClass is added to the tags.
     * <p/>
     * This method can be used to return the text contents of an HTML table.
     *
     * @param node
     * @return The content of the given node and its sub nodes as String.
     */
    public String getTextContent(Node node) {
        final StringBuilder sb = new StringBuilder();
        appendText(sb, node, false);
        return sb.toString();
    }

    public Node setCurrent(Node current) {
        this.current = current;
        return current;
    }

    /**
     * Iterate in a recursive way over the given node and its sub nodes and copies them
     * as an xml fragment into the given StringBuilder.
     *
     * @param sb
     * @param node
     */
    private void append(StringBuilder sb, Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            sb.append(node.getNodeValue());
        }
        else if (node.getNodeType() == Node.COMMENT_NODE) {
            // ignore
        }
        else {
            sb.append("<").append(node.getNodeName());
            final NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                final Node att = attributes.item(i);
                sb.append(" ").append(att.getNodeName()).append("=\"").append(att.getNodeValue()).append('"');
            }
            if (node.hasChildNodes()) {
                sb.append('>');
                for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                    final Node nodeChild = node.getChildNodes().item(i);
                    append(sb, nodeChild);
                }
                sb.append("</").append(node.getNodeName()).append('>');
            }
            else {
                sb.append("/>");
            }
        }
    }

    /**
     * Iterate in a recursive way over the given node and its sub nodes and copies them
     * as an xml fragment into the given StringBuilder.
     * For more details see getTextContent().
     *
     * @param sb
     * @param node
     */
    private void appendText(StringBuilder sb, Node node, boolean withNodeName) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            if (this.escapeHtmlInNodeText) {
                sb.append(StringEscapeUtils.escapeHtml4(node.getNodeValue()));
            }
            else {
                sb.append(node.getNodeValue());
            }
            return;
        }
        if (node.getNodeType() == Node.COMMENT_NODE) {
            return;
        }
        if (!withNodeName) {
            if (node.hasChildNodes()) {
                appendChildTexts(sb, node);
            }
            return;
        }
        final String nodeName = node.getNodeName().toLowerCase();
        if (ALLOWED_TAGS_IN_TEXT.contains(nodeName)) {
            sb.append("<").append(nodeName);
            if (node.hasChildNodes()) {
                sb.append('>');
                appendChildTexts(sb, node);
                sb.append("</").append(nodeName).append('>');
            }
            else {
                sb.append("/>");
            }
        }
    }

    private void appendChildTexts(StringBuilder sb, Node node) {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            final Node nodeChild = node.getChildNodes().item(i);
            appendText(sb, nodeChild, true);
        }
    }

}
