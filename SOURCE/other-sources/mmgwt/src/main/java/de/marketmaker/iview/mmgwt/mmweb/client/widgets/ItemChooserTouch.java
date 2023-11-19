/*
 * ItemChooserTouch.java
 *
 * Created on 10/10/14 5:05 PM
 *
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Stefan Willenbrock
 */
@NonNLS
public class ItemChooserTouch extends Composite implements ItemChooserWidget {

    public interface Callback {
        void onOk();
        void onCancel();
    }

    @UiField(provided = true)
    final Label panelAvailable = new Label(I18n.I.availableColumns());

    @UiField(provided = true)
    final Label panelSelected = new Label(I18n.I.selectedColumns());

    @UiField(provided = true)
    final CellList<String> cellListAvailable;

    @UiField(provided = true)
    final CellList<String> cellListSelected;

    @UiField(provided = true)
    Button select;

    @UiField(provided = true)
    Button deselect;

    @UiField(provided = true)
    Button moveUp;

    @UiField(provided = true)
    Button moveDown;

    @UiField(provided = true)
    Button toTop;

    @UiField(provided = true)
    Button toBottom;

    @UiField
    Button resetButton;

    @UiField
    Button cancelButton;

    @UiField
    Button okButton;

    private final List<String> widgetAvailableList;

    private final List<String> widgetSelectedList;

    private final Map<String, ItemChooserItem> itemMap;

    private final MultiSelectionModel<String> selectionModelAvailable;

    private final MultiSelectionModel<String> selectionModelSelected;

    interface MyUiBinder extends UiBinder<HTMLPanel, ItemChooserTouch> {
    }

    private static final MyUiBinder UI_BINDER = GWT.create(MyUiBinder.class);

    public ItemChooserTouch(final List<ItemChooserItem> allItems, List<ItemChooserItem> selectedItems,
                            final List<ItemChooserItem> defaultSelection, final Callback callback) {
        if (selectedItems == null) {
            selectedItems = new ArrayList<>();
            selectedItems.addAll(defaultSelection);
        }

        // available list

        this.selectionModelAvailable = new MultiSelectionModel<>();
        this.cellListAvailable = new CellList<>(new TextCell());

        this.cellListAvailable.setSelectionModel(this.selectionModelAvailable);
        this.cellListAvailable.setVisibleRange(0, allItems.size());

        // selected list

        this.selectionModelSelected = new MultiSelectionModel<>();
        this.cellListSelected = new CellList<>(new TextCell());

        this.cellListSelected.setSelectionModel(this.selectionModelSelected);
        this.cellListSelected.setVisibleRange(0, allItems.size());

        //

        this.widgetAvailableList = new ArrayList<>();
        this.widgetSelectedList = new ArrayList<>();
        initialize(widgetAvailableList, widgetSelectedList, allItems, selectedItems);

        this.itemMap = new HashMap<>();
        for (ItemChooserItem item : allItems) {
            this.itemMap.put(item.getText(), item);
        }

        this.select = Button.icon("mm-list-move-right") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        Set<String> selectedSet = selectionModelAvailable.getSelectedSet();
                        selectionModelAvailable.clear();
                        widgetAvailableList.removeAll(selectedSet);
                        widgetSelectedList.addAll(selectedSet);
                        updateCellLists();
                    }
                })
                .build();

        this.deselect = Button.icon("mm-list-move-left") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        Set<String> selectedSet = selectionModelSelected.getSelectedSet();
                        selectionModelSelected.clear();
                        widgetSelectedList.removeAll(selectedSet);
                        widgetAvailableList.addAll(selectedSet);
                        updateCellLists();
                    }
                })
                .build();

        this.moveUp = Button.icon("mm-list-move-up") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (hasNoMovementPermission(widgetSelectedList, true)) {
                            return;
                        }

                        for (int i = 1; i < widgetSelectedList.size(); i++) {
                            if (selectionModelSelected.isSelected(widgetSelectedList.get(i))) {
                                switchItems(widgetSelectedList, i - 1, i);
                            }
                        }
                        updateCellLists();
                    }
                })
                .build();

        this.moveDown = Button.icon("mm-list-move-down") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (hasNoMovementPermission(widgetSelectedList, false)) {
                            return;
                        }

                        for (int i = widgetSelectedList.size() - 1; i >= 0; i--) {
                            if (selectionModelSelected.isSelected(widgetSelectedList.get(i))) {
                                switchItems(widgetSelectedList, i + 1, i);
                            }
                        }
                        updateCellLists();
                    }
                })
                .build();

        this.toTop = Button.icon("mm-list-move-top") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (hasNoMovementPermission(widgetSelectedList, true)) {
                            return;
                        }

                        int offset = 0;
                        for (int i = 1; i < widgetSelectedList.size(); i++) {
                            if (selectionModelSelected.isSelected(widgetSelectedList.get(i))) {
                                if (offset == 0) {
                                    offset = i;
                                }
                                for (int j = 0; j < offset; j++) {
                                    switchItems(widgetSelectedList, i - j, i - j - 1);
                                }
                            }
                        }
                        updateCellLists();
                    }
                }).build();

        this.toBottom = Button.icon("mm-list-move-bottom") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (hasNoMovementPermission(widgetSelectedList, false)) {
                            return;
                        }

                        final int lastIndex = widgetSelectedList.size() - 1;
                        int offset = 0;
                        for (int i = lastIndex - 1; i > 0; i--) {
                            if (selectionModelSelected.isSelected(widgetSelectedList.get(i))) {
                                if (offset == 0) {
                                    offset = lastIndex - i;
                                }
                                for (int j = 0; j < offset; j++) {
                                    switchItems(widgetSelectedList, i + j, i + j + 1);
                                }
                            }
                        }
                        updateCellLists();
                    }
                }).build();

        initWidget(UI_BINDER.createAndBindUi(this));

        setButtonStyle(this.resetButton, this.okButton, this.cancelButton);

        this.resetButton.withClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                widgetSelectedList.clear();
                widgetAvailableList.clear();
                initialize(widgetAvailableList, widgetSelectedList,
                        allItems, defaultSelection);
            }
        });

        if (callback != null) {
            this.cancelButton.withClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    callback.onCancel();
                }
            });
            this.okButton.withClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    callback.onOk();
                }
            });
        }
        else {
            this.cancelButton.setVisible(false);
            this.okButton.setVisible(false);
        }
    }

    private void setButtonStyle(Button... buttons) {
        for (Button button : buttons) {
            button.addStyleName(Button.FORCED_LEGACY_BORDERS_STYLE);
            button.addStyleName("alignCenter");
            button.getElement().getStyle().setPropertyPx("minWidth", 80);
        }
    }

    private void initialize(List<String> widgetAvailableList, List<String> widgetSelectedList,
                            List<ItemChooserItem> allItemsList, List<ItemChooserItem> newSelectedList) {
        for (ItemChooserItem item : allItemsList) {
            if (!newSelectedList.contains(item)) {
                widgetAvailableList.add(item.getText());
            }
        }

        for (ItemChooserItem item : newSelectedList) {
            widgetSelectedList.add(item.getText());
        }
        updateCellLists();
    }

    private void updateCellLists() {
        cellListAvailable.setVisibleRange(0, widgetAvailableList.size());
        cellListAvailable.setRowData(0, widgetAvailableList);
        cellListSelected.setVisibleRange(0, widgetSelectedList.size());
        cellListSelected.setRowData(0, widgetSelectedList);

    }

    @Override
    public int getSelectedRowsCount() {
        return this.widgetSelectedList.size();
    }

    @Override
    public String getColumnValue(int idx) {
        final String itemText = this.widgetSelectedList.get(idx);
        return itemMap.get(itemText).getId();
    }

    @Override
    public ItemChooserWidget withStyleForSelectedItemsListBox(String styleName) {
        return this;
    }

    @Override
    public void setLeftColHead(String text) {
        this.panelAvailable.setText(text);
    }

    @Override
    public void setRightColHead(String text) {
        this.panelSelected.setText(text);
    }

    private void switchItems(List<String> list, int i, int j) {
        final String temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    private boolean hasNoMovementPermission(List<String> selectedProviderList, boolean top) {
        return selectedProviderList.size() < 1
                || this.selectionModelSelected.isSelected(selectedProviderList.get(top ? 0 : selectedProviderList.size() - 1));
    }

    public Widget getDefaultFocusWidget() {
        this.cellListAvailable.setFocus(true);
        return this.cellListAvailable;
    }
}
