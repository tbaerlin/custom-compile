package de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree;

/**
 * @author Ulrich Maurer
 *         Date: 08.05.12
 */
public class Text extends Node {
    private String tooltip;

    public Text() { // needed for GWT serialization
    }

    public Text(String label, String tooltip) {
        super(label);
        this.tooltip = tooltip;
    }

    public String getTooltip() {
        return tooltip;
    }

    @Override
    public String getType() {
        return "Text"; // $NON-NLS$
    }

    @Override
    public String toString() {
        return "Text{label='" + getLabel() + "\'}"; // $NON-NLS$
    }
}
