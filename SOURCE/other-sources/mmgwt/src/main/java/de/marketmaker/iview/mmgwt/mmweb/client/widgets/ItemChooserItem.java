/*
 * ItemChooserItem.java
 *
 * Created on 10/10/14 4:50 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

/**
 * @author Michael Lösch
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ItemChooserItem {
    private int key;
    private final String text;
    private final String id;
    private final boolean fixed;
    private final boolean selectable;

    public ItemChooserItem(String text, String id, boolean fixed) {
        this(text, id, fixed, true);
    }

    public ItemChooserItem(String text, String id, boolean fixed, boolean selectable) {
        this.selectable = selectable;
        this.text = text;
        this.id = id;
        this.fixed = fixed;
    }

    public String getText() {
        return text;
    }

    public String getId() {
        return id;
    }

    public boolean isFixed() {
        return fixed;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemChooserItem that = (ItemChooserItem) o;
        return fixed == that.fixed && !(id != null ? !id.equals(that.id) : that.id != null) &&
                !(text != null ? !text.equals(that.text) : that.text != null);
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (fixed ? 1 : 0);
        return result;
    }
}
