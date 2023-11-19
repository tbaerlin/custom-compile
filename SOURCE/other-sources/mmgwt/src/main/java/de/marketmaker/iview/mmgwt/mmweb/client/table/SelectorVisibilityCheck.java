package de.marketmaker.iview.mmgwt.mmweb.client.table;

import de.marketmaker.iview.mmgwt.mmweb.client.Selector;

/**
 * @author umaurer
 */
public class SelectorVisibilityCheck implements VisibilityCheck {
    private final boolean visible;

    public SelectorVisibilityCheck(Selector selector) {
        this.visible = selector.isAllowed();
    }

    public boolean isVisible(TableColumn tc) {
        return this.visible;
    }
}
