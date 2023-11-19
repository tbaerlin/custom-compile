package de.marketmaker.iview.mmgwt.mmweb.client.table;

/**
 * @author umaurer
 */
public class SimpleVisibilityCheck implements VisibilityCheck {
    private static final VisibilityCheck VISIBLE = new SimpleVisibilityCheck(true);

    private static final VisibilityCheck INVISIBLE = new SimpleVisibilityCheck(false);

    private final boolean visible;

    private SimpleVisibilityCheck(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible(TableColumn tc) {
        return this.visible;
    }

    public static VisibilityCheck valueOf(boolean visible) {
        return visible ? VISIBLE : INVISIBLE;
    }
}
