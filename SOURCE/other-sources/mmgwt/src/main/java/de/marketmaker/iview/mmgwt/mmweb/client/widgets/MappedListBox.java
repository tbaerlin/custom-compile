/*
 * MappedListBox.java
 *
 * Created on 14.08.13 13:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.user.client.ui.ListBox;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

import java.util.List;

/**
 * @author Markus Dick
 */
public class MappedListBox<T> extends ListBox {
    private boolean withEmptyChoice = false;
    private List<T> items;
    private ItemMapper<T> itemMapper;

    public interface ItemMapper<T> {
        String getLabel(T item);
        String getValue(T item);
    }

    public MappedListBox() {
        super(false);
    }

    public void setItemMapper(ItemMapper<T> itemMapper) {
        this.itemMapper = itemMapper;
    }

    public void setWithEmptyChoice(boolean withEmptyChoice) {
        if(this.items != null) {
            throw new IllegalStateException("setWithEmptyChoice must be called before values are assigned!"); //$NON-NLS$
        }
        this.withEmptyChoice = withEmptyChoice;
        handleEmptyItem();
    }

    public void setItems(List<T> items) {
        clear();

        if(items == null) return;
        if(this.itemMapper == null) throw new IllegalStateException("setItems must be called after an itemMapper has been assigned!"); //$NON-NLS$

        this.items = items;
        for(T item : items) {
            final String label = this.itemMapper.getLabel(item);
            final String value = this.itemMapper.getValue(item);

            addItem(label, value);
        }
    }

    @Override
    public void clear() {
        this.items = null;
        super.clear();
        handleEmptyItem();
    }

    private void handleEmptyItem() {
        if(this.withEmptyChoice) {
            this.addItem(I18n.I.none(), "");
        }
    }

    public T getSelectedItem() {
        if(this.items == null || this.items.isEmpty()) return null;
        int selectedIndex = getSelectedIndex();

        if(selectedIndex < 0) return null;

        if(this.withEmptyChoice) {
            selectedIndex--;
            if(selectedIndex < 0) return null;
        }

        return this.items.get(selectedIndex);
    }

    public void setSelectedItem(T itemToSelect) {
        if(itemToSelect == null || this.items == null || this.items.isEmpty()) {
            if(this.withEmptyChoice) {
                this.setSelectedIndex(0);
                return;
            }
            this.setSelectedIndex(-1);
            return;
        }

        for(int i = 0; i < this.items.size(); i++) {
            if(itemToSelect == this.items.get(i)) {
                if(this.withEmptyChoice) {
                    this.setSelectedIndex(i+1);
                }
                else {
                    this.setSelectedIndex(i);
                }
                break;
            }
        }
    }
}
