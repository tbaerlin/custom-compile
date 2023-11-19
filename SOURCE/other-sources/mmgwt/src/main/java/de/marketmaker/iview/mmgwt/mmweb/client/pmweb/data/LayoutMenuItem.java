package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data;

import de.marketmaker.iview.pmxml.LayoutDesc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author umaurer
 */
public class LayoutMenuItem {
    private final String name;
    private final LayoutDesc layoutDesc;
    private List<LayoutMenuItem> children = null;

    public LayoutMenuItem(String name) {
        this.name = name;
        this.layoutDesc = null;
    }

    public LayoutMenuItem(String name, LayoutDesc layoutDesc) {
        this.name = name;
        this.layoutDesc = layoutDesc;
    }

    public String getName() {
        return name;
    }

    public LayoutDesc getLayoutDesc() {
        return layoutDesc;
    }

    public List<LayoutMenuItem> getChildren() {
        return children;
    }

    public void addChild(LayoutMenuItem item) {
        ensureChildren().add(item);
    }

    private List<LayoutMenuItem> ensureChildren() {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        return this.children;
    }

    public boolean hasReports() {
        if (this.layoutDesc != null) {
            return true;
        }
        if (this.children == null) {
            return false;
        }
        for (final LayoutMenuItem item : children) {
            if (item.hasReports()) {
                return true;
            }
        }
        return false;
    }
}
