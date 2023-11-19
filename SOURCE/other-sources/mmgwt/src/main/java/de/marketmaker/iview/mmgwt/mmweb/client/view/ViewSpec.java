package de.marketmaker.iview.mmgwt.mmweb.client.view;

/**
 * @author umaurer
 */
public class ViewSpec {
    private final String id;
    private final String name;
    private final String iconCls;
    private final String tooltip;

    public ViewSpec(String id, String name, String iconCls, String tooltip) {
        this.id = id;
        this.name = name;
        this.iconCls = iconCls;
        this.tooltip = tooltip;
    }

    public ViewSpec(String name, String iconCls, String tooltip) {
        this(null, name, iconCls, tooltip);
    }

    public ViewSpec(String name, String iconCls) {
        this(name, iconCls, null);
    }

    public ViewSpec(String name) {
        this(name, null, null);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconCls() {
        return iconCls;
    }

    public String getTooltip() {
        return tooltip;
    }

    public String toString() {
        return this.name;
    }
}
