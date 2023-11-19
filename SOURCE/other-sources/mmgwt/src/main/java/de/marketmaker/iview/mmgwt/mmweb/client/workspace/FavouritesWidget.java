/*
 * FavouritesWidget.java
 *
 * Created on 17.11.2015 16:27
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DragDropSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.WidgetDropHandler;

/**
 * @author mdick
 */
public class FavouritesWidget extends Composite implements ConfigChangedHandler {
    public static final char UNIT_SEPARATOR = '\u001F';

    private final Panel layout = new FlowPanel();

    private HandlerRegistration configChangedHandlerRegistration;

    private Command doAfterGoto;

    private Command doAfterRemove;

    private List<String> configuredStores;

    private SafeHtml safeHtmlIfAllStoresEmpty;

    public FavouritesWidget() {
        initWidget(this.layout);
        this.layout.setStyleName("mm-favourites-content");
        update();
    }

    public FavouritesWidget withDoAfterGoto(Command command) {
        this.doAfterGoto = command;
        return this;
    }

    public FavouritesWidget withDoAfterRemove(Command command) {
        this.doAfterRemove = command;
        return this;
    }

    public FavouritesWidget withSafeHtmlIfAllStoresEmpty(SafeHtml safeHtml) {
        this.safeHtmlIfAllStoresEmpty = safeHtml;
        return this;
    }

    public FavouritesWidget withConfigChangedHandler() {
        if (this.configChangedHandlerRegistration == null) {
            this.configChangedHandlerRegistration = EventBusRegistry.get().addHandler(ConfigChangedEvent.getType(), this);
        }
        return this;
    }

    public void setConfiguredStores(List<String> configuredStores, boolean update) {
        if (!CompareUtil.equals(this.configuredStores, configuredStores)) {
            this.configuredStores = configuredStores;
            if (update) {
                update();
            }
        }
    }

    public void update() {
        this.layout.clear();

        if (FavouriteItemsStores.isAllStoresEmpty()) {
            if (this.safeHtmlIfAllStoresEmpty != null) {
                this.layout.add(new HTML(this.safeHtmlIfAllStoresEmpty));
            }
            return;
        }

        for (FavouriteItemsStore<?> store : getFavouriteItemsStores()) {
            if (store == null) {
                continue;
            }

            final List<? extends FavouriteItem> items = store.getItems();
            if (items != null && !items.isEmpty()) {
                final Label storeLabel = new Label(store.getLabel());
                storeLabel.addStyleName("mm-favourites-storeLabel");
                this.layout.add(storeLabel);

                final TreeMap<String, FlowPanel> typeLabelToTypeNode = new TreeMap<>((o1, o2) ->
                        CompareUtil.compare((o1 != null ? o1.toLowerCase() : null),
                                (o2 != null ? o2.toLowerCase() : null)));

                for (FavouriteItem item : items) {
                    if (!typeLabelToTypeNode.containsKey(item.getTypeLabel())) {
                        final FlowPanel typePanel = new FlowPanel();
                        typePanel.setStyleName("mm-favourites-typePanel");
                        final Label typeLabel = new Label(item.getTypeLabel());
                        typeLabel.setStyleName("mm-favourites-typeLabel");
                        typePanel.add(typeLabel);
                        typeLabelToTypeNode.put(item.getTypeLabel(), typePanel);
                    }
                    final FlowPanel typeRoot = typeLabelToTypeNode.get(item.getTypeLabel());
                    typeRoot.add(createNode(item));
                }

                for (String typeLabel : typeLabelToTypeNode.keySet()) {
                    final FlowPanel flowPanel = typeLabelToTypeNode.get(typeLabel);
                    if (flowPanel != null) {
                        this.layout.add(flowPanel);
                    }
                }
            }
        }
    }

    public FavouriteItemsStore[] getFavouriteItemsStores() {
        if (this.configuredStores == null) {
            return FavouriteItemsStores.get();
        }

        final FavouriteItemsStore[] selectedStores = new FavouriteItemsStore[this.configuredStores.size()];
        int i = 0;
        for (FavouriteItemsStore store : FavouriteItemsStores.get()) {
            for (String configuredStore : this.configuredStores) {
                if (StringUtil.equals(store.getName(), configuredStore)) {
                    selectedStores[i++] = store;
                }
            }
        }
        return selectedStores;
    }

    public Widget createNode(FavouriteItem item) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setStyleName("mm-favourites-item");

        final SafeHtml safeHtml = SafeHtmlUtils.fromTrustedString(IconImage.get(item.getIconName()).getHTML() + "<span>" + SafeHtmlUtils.htmlEscape(item.getLabel()) + "</span>");  // $NON-NLS$

        final HTML w = new HTML(safeHtml);
        w.setStyleName("mm-link");
        w.addClickHandler(event -> {
            PlaceUtil.goTo(item.getHistoryToken());
            if (this.doAfterGoto != null) {
                this.doAfterGoto.execute();
            }
        });
        panel.add(w);

        final Button renameButton = Button.icon("x-tool-btn-edit").clickHandler(event ->  // $NON-NLS$
                Dialog.prompt(I18n.I.renameElement(), I18n.I.newName(), item.getLabel(),
                        new Dialog.PromptCallback() {
                            @Override
                            protected boolean isValid(String value) {
                                return StringUtil.hasText(value);
                            }

                            @Override
                            public void execute(String value) {
                                item.getSource().renameItem(item, value);
                                update();
                            }
                        })).build();
        panel.add(renameButton);
        /* Note: visibility allocates space for the widget even if it is not visible.
         * setVisible uses display:none and does not allocate space if invisible.
         * Here, we want that space is allocated to avoid that the popup resizes and flickers
         * if we hover the favourite item which alters the visibility of the button.
         */
        renameButton.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
        // If rename is not allowed, do never display the rename button and do not allocate space
        // for it.
        renameButton.setVisible(item.canRename());

        final Button removeButton = Button.icon("mm-minus").clickHandler(event ->  // $NON-NLS$
                Dialog.confirm(I18n.I.tooltipRemoveElement(), () -> removeItem(item))).build();
        panel.add(removeButton);
        removeButton.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
        removeButton.setVisible(item.canRemove());

        panel.addDomHandler(event -> {
            renameButton.getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
            removeButton.getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
        }, MouseOverEvent.getType());

        panel.addDomHandler(event -> {
            renameButton.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
            removeButton.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
        }, MouseOutEvent.getType());

        if (item.canMove()) {
            DragDropSupport.makeDraggable(panel, panel, createTransferData(item), item.getLabel());
            DragDropSupport.makeDroppable(panel, new WidgetDropHandler() {
                @Override
                public boolean isDropAllowed(String transferData) {
                    return transferData != null && transferData.startsWith(item.getSource().getName() + UNIT_SEPARATOR + item.getType()) && !transferData.equals(createTransferData(item));
                }

                @Override
                public boolean onDndEnter(String transferData) {
                    final boolean dropAllowed = isDropAllowed(transferData);
                    if (dropAllowed) {
                        panel.addStyleName("mm-drop-accepted");
                    }
                    else {
                        panel.addStyleName("mm-drop-rejected");
                    }
                    return dropAllowed;
                }

                @Override
                public void onDndLeave() {
                    panel.removeStyleName("mm-drop-accepted");
                    panel.removeStyleName("mm-drop-rejected");
                }

                @Override
                public void onDrop(String transferData) {
                    Firebug.info("dropped: " + transferData);

                    if (StringUtil.hasText(transferData)) {
                        final ArrayList<String> split = StringUtil.split(transferData, UNIT_SEPARATOR);
                        final String identifier = split.get(2);

                        final FavouriteItemsStore store = item.getSource();
                        final FavouriteItem from = store.getItem(identifier);
                        store.moveItem(from, item);
                        update();
                    }
                }
            });
        }

        return panel;
    }

    public void removeItem(FavouriteItem item) {
        item.getSource().removeItem(item);
        update();
        if (this.doAfterRemove != null) {
            this.doAfterRemove.execute();
        }
    }

    public String createTransferData(FavouriteItem item) {
        return item.getSource().getName() + UNIT_SEPARATOR + item.getType() + UNIT_SEPARATOR + item.getIdentifier();
    }

    @Override
    public void onConfigChange(ConfigChangedEvent event) {
        update();
    }

    public void release() {
        this.configChangedHandlerRegistration.removeHandler();
        this.configChangedHandlerRegistration = null;
    }
}
