package de.marketmaker.istar.merger.web.xmlmarket;


import de.marketmaker.istar.common.util.XmlUtil;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class XmlNode {

    private final String tagName;
    private String text;
    private final HashMap<String, String> attributes = new HashMap<>();
    protected final ArrayList<XmlNode> children = new ArrayList<>();

    XmlNode(String tagName) {
        this.tagName = tagName;
    }

    XmlNode child(XmlNode xmlNode) {
        if (xmlNode != null) {
            children.add(xmlNode);
        }
        return this;
    }

    XmlNode firstChild(XmlNode xmlNode) {
        if (xmlNode != null) {
            children.add(0, xmlNode);
        }
        return this;
    }

    XmlNode children(List<XmlNode> xmlNodes) {
        children.addAll(xmlNodes);
        return this;
    }

    public XmlNode attribute(String key, String value) {
        attributes.put(key, StringEscapeUtils.escapeXml(value));
        return this;
    }

    public XmlNode text(String text) {
        this.text = StringEscapeUtils.escapeXml(text);
        return this;
    }

    public XmlNode cdata(String text) {
        if (text != null) {
            this.text = XmlUtil.encodeCData(text);
        }
        return this;
    }

    protected void render(StringBuilder sb, String inset) {
        sb.append(inset);
        sb.append("<");
        sb.append(tagName);

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            sb.append(" ");
            sb.append(entry.getKey());
            sb.append("=\"");
            sb.append(entry.getValue());
            sb.append("\"");
        }

        if (text != null) {
            // we have text content or a zero sized string
            sb.append(">");
            sb.append(text);
            sb.append("</");
            sb.append(tagName);
            sb.append(">\n");

        } else if (!children.isEmpty()) {
            // we have child nodes
            sb.append(">\n");
            for (XmlNode xmlOutput : children) {
                xmlOutput.render(sb, inset + " ");
            }
            sb.append(inset);
            sb.append("</");
            sb.append(tagName);
            sb.append(">\n");

        } else {
            // collapsed tag iff text content is null and no children
            sb.append("/>\n");
        }
    }

}
