package de.marketmaker.itools.gwtutil.client.widgets;

import java.util.ArrayList;

/**
 * @author Felix Hoffmann
 */
@SuppressWarnings({"UnusedDeclaration"})
public class DropDownModel<T> {
    private ArrayList<Item<T>> items = new ArrayList<Item<T>>();

    private Item<T> selectedItem = null;

    public class Item<I> {

        private String display;

        private I i;

        Item(I i, String display) {
            this.i = i;
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }

        public void setDisplay(String display) {
            this.display = display;
        }

        public I getValue() {
            return i;
        }

        public void setValue(I i) {
            this.i = i;
        }

        @Override
        public String toString() {
            return this.display;
        }
    }

    public int getItemIndex(T value) {
        for (int i = 0; i < this.items.size(); i++) {
            if (value.equals(this.items.get(i).getValue())) {
                return i;
            }
        }
        return -1;
    }

    public ArrayList<Item<T>> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item<T>> items) {
        this.items = items;
    }

    public Item<T> getSelectedItem() {
        return selectedItem;
    }

    public Item<T> getItem(int index) {
        return this.items.get(index);
    }

    public void setSelectedItem(Item<T> selectedItem) {
        this.selectedItem = selectedItem;
    }

    public Item<T> createItem(T t, String display) {
        return new Item<T>(t, display);
    }
}