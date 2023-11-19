package de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ulrich Maurer
 *         Date: 08.05.12
 */
public class Element extends XsdNode {
    private ArrayList<Attribute> attributes;
    private ArrayList<Element> children;
    private Text text = null;

    public Element() { // needed for GWT serialization
    }

    public Element(String label, String tooltip, XsdType xsdType) {
        super(label, tooltip, xsdType);
    }

    public void addAttribute(Attribute attribute) {
        if (this.attributes == null) {
            this.attributes = new ArrayList<Attribute>(3);
        }
        this.attributes.add(attribute);
        attribute.parent = this;
    }

    public boolean hasAttributes() {
        return this.attributes != null;
    }

    public List<Attribute> getAttributes() {
        return this.attributes == null ? Collections.<Attribute>emptyList() : Collections.unmodifiableList(this.attributes);
    }

    public void addChild(Element child) {
        if (this.children == null) {
            this.children = new ArrayList<Element>(3);
        }
        this.children.add(child);
        child.parent = this;
    }

    public boolean hasChildren() {
        return this.children != null;
    }

    public List<Element> getChildren() {
        return this.children == null ? Collections.<Element>emptyList() : Collections.unmodifiableList(this.children);
    }

    public void setText(Text text) {
        this.text = text;
    }

    public boolean hasText() {
        return this.text != null;
    }

    public Text getText() {
        return text;
    }

    @Override
    public String getType() {
        return "Element"; // $NON-NLS$
    }

    @Override
    public String toString() {
        return "Element{label='" + getLabel() + '\'' + ", xsdType=" + this.xsdType + '}'; // $NON-NLS$
    }

    public void appendSubtree(StringBuilder sb, String indent) {
        sb.append(indent + "<" + this.getLabel() + ">\n"); // $NON-NLS$
        sb.append(indent + "|-Help = '" + this.getTooltip() + "'\n"); // $NON-NLS$
        sb.append(indent + "|-XsdType = " + this.getXsdType() + "\n"); // $NON-NLS$
        if (this.attributes != null) {
            for (Attribute child : this.attributes) {
                child.appendSubtree(sb, indent + "   "); // $NON-NLS$
            }
        }
        if (this.children != null) {
            for (Element child : this.children) {
                child.appendSubtree(sb, indent + "   "); // $NON-NLS$
            }
        }
        if (this.text != null) {
            this.text.appendSubtree(sb, indent + "   "); // $NON-NLS$
        }
        sb.append(indent + "</" + this.getLabel() + ">\n"); // $NON-NLS$
    }
}
