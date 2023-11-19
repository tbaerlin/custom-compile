package de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree;

/**
 * @author Ulrich Maurer
 *         Date: 08.05.12
 */
public class Attribute extends XsdNode {
    private String key;
    private String value;

    public Attribute() { // needed for GWT serialization
    }

    public Attribute(String key, String value, String tooltip, XsdType xsdType) {
        super(key + "=\"" + value + "\"", tooltip, xsdType);
        this.key = key;
        this.value = value;
    }

    @Override
    public String getType() {
        return "Attribute"; // $NON-NLS$
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Attribute{label='" + getLabel() + '\'' + ", xsdType=" + this.xsdType + '}'; // $NON-NLS$
    }

    public void appendSubtree(StringBuilder sb, String indent) {
        sb.append(indent + "@" + this.getLabel() + "\n"); // $NON-NLS$
        sb.append(indent + "|-Help = '" + this.getTooltip() + "'\n"); // $NON-NLS$
        sb.append(indent + "|-XsdType = " + this.getXsdType() + "\n"); // $NON-NLS$
    }
}
