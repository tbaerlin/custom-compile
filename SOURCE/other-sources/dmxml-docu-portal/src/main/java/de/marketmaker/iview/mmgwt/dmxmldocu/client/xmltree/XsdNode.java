package de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree;

/**
 * @author Ulrich Maurer
 *         Date: 09.05.12
 */
public abstract class XsdNode extends Node {
    protected String tooltip;
    protected XsdType xsdType;

    protected XsdNode() { // needed for GWT serialization
    }

    public XsdNode(String label, String tooltip, XsdType xsdType) {
        super(label);
        this.tooltip = tooltip;
        this.xsdType = xsdType;
    }

    public String getTooltip() {
        return tooltip;
    }

    public XsdType getXsdType() {
        return xsdType;
    }
}
