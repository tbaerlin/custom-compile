package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.DefaultFocusKeyHandler;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingPopupPanel;

/**
 * Author: umaurer
 * Created: 14.08.14
 */
public class PopupTableSelectionHelper {
    public static final String ROW_ATTRIBUTE_KEY = "data-key"; // $NON-NLS$
    public static final int PAGE_ITEM_COUNT = 10;
    private static final String TABLE_SELECTED_ROW_ATTRIBUTE = "data-selectedRow"; // $NON-NLS$
    private static final String STYLE_KEY_SELECT = "keySelect"; // $NON-NLS$
    private final Callback callback;

    private FloatingPopupPanel popupPanel;
    private FlexTable table;
    private int popupItemCount;
    private int lastSelectedRow;

    public interface Callback {
        void showList();
        void focusButtonAdd();
        void focusSelectedEntry(int index);
        int getSelectedEntryCount();
        void removeProperty(int index);
        void addSelection(String key);
    }

    public PopupTableSelectionHelper(Callback callback) {
        this.callback = callback;
    }

    public void onPopupOpened(FloatingPopupPanel popupPanel, FlexTable table) {
        this.popupPanel = popupPanel;
        this.popupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                callback.focusButtonAdd();
                PopupTableSelectionHelper.this.popupPanel = null;
                PopupTableSelectionHelper.this.table = null;
            }
        });
        this.table = table;
        this.popupItemCount = table.getRowCount();
    }

    public void setLastSelectedRow(int lastSelectedRow) {
        this.lastSelectedRow = lastSelectedRow;
    }

    public int getLastSelectedRow() {
        return this.lastSelectedRow;
    }

    public void makeButtonAddFocusable(final Widget buttonAdd) {
        final HasEnabled hasEnabled = buttonAdd instanceof HasEnabled ? (HasEnabled)buttonAdd : null;

        WidgetUtil.makeFocusable(buttonAdd, new DefaultFocusKeyHandler() {
            private boolean isDisabled() {
                return hasEnabled != null && !hasEnabled.isEnabled();
            }

            @Override
            public boolean onFocusKeyHome() {
                if (table == null || isDisabled()) {
                    return false;
                }
                setKeySelectionIndex(0);
                return true;
            }

            @Override
            public boolean onFocusKeyPageUp() {
                if (table == null || isDisabled()) {
                    return false;
                }
                moveKeySelection(-PAGE_ITEM_COUNT);
                return true;
            }

            @Override
            public boolean onFocusKeyUp() {
                if (table == null || isDisabled()) {
                    return false;
                }
                moveKeySelection(-1);
                return true;
            }

            @Override
            public boolean onFocusKeyDown() {
                if(isDisabled()) {
                    return false;
                }
                if (table == null) {
                    callback.showList();
                }
                else {
                    moveKeySelection(1);
                }
                return true;
            }

            @Override
            public boolean onFocusKeyPageDown() {
                if (table == null || isDisabled()) {
                    return false;
                }
                moveKeySelection(PAGE_ITEM_COUNT);
                return true;
            }

            @Override
            public boolean onFocusKeyEnd() {
                if (table == null || isDisabled()) {
                    return false;
                }
                setKeySelectionIndex(popupItemCount - 1);
                return true;
            }

            @Override
            public boolean onFocusKeyEscape() {
                if (table == null || isDisabled()) {
                    return false;
                }
                hidePopup();
                return true;
            }

            @Override
            public boolean onFocusKeyClick() {
                if(isDisabled()) {
                    return false;
                }
                if (table == null) {
                    callback.showList();
                }
                else if (addSelection()) {
                    hidePopup();
                }
                return true;
            }

            @Override
            public boolean onFocusAdd() {
                if(isDisabled()) {
                    return false;
                }

                if (popupPanel == null) {
                    callback.showList();
                    return true;
                }
                return false;
            }
        });
    }


    public void makeListEntryFocusable(final Widget widget, final int propIndex) {
        WidgetUtil.makeFocusable(widget, new DefaultFocusKeyHandler() {
            @Override
            public boolean onFocusDelete() {
                final int entryCount = callback.getSelectedEntryCount();
                final int nextIndex = entryCount == 1
                        ? -1
                        : propIndex == entryCount - 1 ? propIndex - 1 : propIndex;
                callback.removeProperty(propIndex);
                if (nextIndex == -1) {
                    callback.focusButtonAdd();
                }
                else {
                    callback.focusSelectedEntry(nextIndex);
                }
                return true;
            }

            @Override
            public boolean onFocusKeyClick() {
                return onFocusDelete();
            }

            @Override
            public boolean onFocusAdd() {
                callback.showList();
                callback.focusButtonAdd();
                return true;
            }

            @Override
            public boolean onFocusKeyDown() {
                return onFocusAdd();
            }
        }, false);
    }

    private boolean addSelection() {
        final int keySelectionIndex = getKeySelectionIndex();
        if (keySelectionIndex == -1) {
            return false;
        }
        this.callback.addSelection(getRowElement(keySelectionIndex).getAttribute(PopupTableSelectionHelper.ROW_ATTRIBUTE_KEY));
        setLastSelectedRow(keySelectionIndex);
        return true;
    }

    public int getKeySelectionIndex() {
        final String sIndex = this.table.getElement().getAttribute(TABLE_SELECTED_ROW_ATTRIBUTE);
        if (sIndex != null && !sIndex.isEmpty()) {
            final int selectedIndex = Integer.parseInt(sIndex);
            if (selectedIndex >= 0 && selectedIndex < this.table.getRowCount()) {
                return selectedIndex;
            }
        }
        return -1;
    }

    public void setKeySelectionIndex(int newIndex) {
        final HTMLTable.RowFormatter rowFormatter = this.table.getRowFormatter();
        final int keySelectionIndex = getKeySelectionIndex();
        if (keySelectionIndex != -1) {
            rowFormatter.getElement(keySelectionIndex).removeClassName(STYLE_KEY_SELECT);
        }
        if (newIndex < 0) {
            newIndex = 0;
        }
        else if (newIndex >= this.popupItemCount) {
            newIndex = this.popupItemCount - 1;
        }
        this.table.getElement().setAttribute(TABLE_SELECTED_ROW_ATTRIBUTE, String.valueOf(newIndex));
        final Element selectedElement = rowFormatter.getElement(newIndex);
        selectedElement.addClassName(STYLE_KEY_SELECT);
        this.popupPanel.scrollToElement(selectedElement);
    }

    public void moveKeySelection(int delta) {
        final int keySelectionIndex = getKeySelectionIndex();
        if (keySelectionIndex == -1) {
            setKeySelectionIndex(delta < 0 ? (this.lastSelectedRow - 1) : this.lastSelectedRow);
        }
        else {
            setKeySelectionIndex(keySelectionIndex + delta);
        }
    }

    public Element getRowElement(int row) {
        return this.table.getRowFormatter().getElement(row);
    }

    public void hidePopup() {
        if (this.popupPanel == null) {
            return;
        }
        this.popupPanel.hide();
    }
}
