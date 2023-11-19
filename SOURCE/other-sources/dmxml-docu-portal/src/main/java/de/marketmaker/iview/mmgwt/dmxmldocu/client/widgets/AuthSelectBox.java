/*
 * AuthSelectBox.java
 *
 * Created on 26.07.12 08:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Markus Dick
 */
public class AuthSelectBox extends HorizontalPanel implements HasChangeHandlers {
    public static final String LOCAL_STORE_KEY = "AuthSelectBox"; //$NON-NLS$

    private static final AuthItemFactory AUTH_ITEM_FACTORY = GWT.create(AuthItemFactory.class);

    private SortedMap<String, AuthItem> authItems;

    private ListBox listBox;
    private Label addButton;
    private Label deleteButton;
    private Label editButton;
    private EditAuthItemDialog editPopup;

    public AuthSelectBox() {
        super();
        authItems = new TreeMap<String, AuthItem>();
        initComponents();
        retrieveValues();
        refreshAuthItemsListBox();
    }

    private void initComponents() {
        listBox = new ListBox();
        add(listBox);

        addButton = new Label();
        addButton.setStyleName("plus"); //$NON-NLS$
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                editPopup.setAuthItem(AUTH_ITEM_FACTORY.authItem().as());
                editPopup.showRelativeTo(addButton);
            }
        });
        add(addButton);

        deleteButton = new Label();
        deleteButton.setStyleName("minus"); //$NON-NLS$
        deleteButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                confirmRemoveAuthItem();
            }
        });
        add(deleteButton);

        editButton = new Label();
        editButton.setStyleName("equals"); //$NON-NLS$
        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int currentIndex = listBox.getSelectedIndex();
                if(currentIndex < 0) return;

                final String currentAuthItemName = listBox.getValue(currentIndex);
                final AuthItem currentAuthItem = authItems.get(currentAuthItemName);
                editPopup.setAuthItem(currentAuthItem);
                editPopup.showRelativeTo(editButton);
            }
        });
        add(editButton);

        editPopup = new EditAuthItemDialog(new EditAuthItemDialog.Callback() {
            @Override
            public void onEditOk() {
                updateAuthItem();
            }
        });
        editPopup.setAuthItem(AUTH_ITEM_FACTORY.authItem().as());
        editPopup.setModal(true);
        editPopup.setAutoHideEnabled(true);
    }

    public AuthItem getSelectedAuthItem() {
        int selectedIndex = listBox.getSelectedIndex();
        if(selectedIndex < 0) return null;
        final String itemName = listBox.getValue(selectedIndex);
        return authItems.get(itemName);
    }

    public void setDefaultAuthItem(String authentication, String authenticationType) {
        if(authentication == null || authenticationType == null) return;
        final AuthItem authItem = AUTH_ITEM_FACTORY.authItem().as();
        authItem.setAuthentication(authentication);
        authItem.setAuthenticationType(authenticationType);
        authItem.setItemName(editPopup.toItemName(authentication, authenticationType));
        authItem.setLocale(null);
        addAuthItem(authItem);
    }

    private void syncListBoxWithAuthItems(boolean useGivenIndex, AuthItem authItem, int selectedIndex) {
        listBox.clear();

        int i = 0;
        for(AuthItem item : authItems.values()) {
            listBox.addItem(item.getItemName());

            if(!useGivenIndex && (item == authItem) || useGivenIndex && (selectedIndex == i)) {
                listBox.setItemSelected(i, true);
            }
            i++;
        }
    }

    private void updateAuthItem() {
        final AuthItem authItem = editPopup.getAuthItem();
        authItems.values().remove(authItem);
        addAuthItem(authItem);
    }

    private void addAuthItem(AuthItem authItem) {
        authItems.put(authItem.getItemName(), authItem);
        syncListBoxWithAuthItems(false, authItem, -1);
        storeValues();
        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), this);
    }

    private void removeCurrentAuthItem(String authItemName) {
        int selectedIndex = listBox.getSelectedIndex();
        if(selectedIndex > 0) {
            selectedIndex--;
        }
        else {
            selectedIndex = 0;
        }
        authItems.remove(authItemName);
        syncListBoxWithAuthItems(true, null, selectedIndex);
        storeValues();
        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), this);
    }

    private void refreshAuthItemsListBox() {
        int selectedIndex = listBox.getSelectedIndex();
        if(selectedIndex > authItems.size()) {
            selectedIndex = authItems.size();
        }
        syncListBoxWithAuthItems(true, null, selectedIndex);
    }

    private void confirmRemoveAuthItem() {
        final int selectedIndex = listBox.getSelectedIndex();
        if(selectedIndex == -1) return;

        final String itemToRemove = listBox.getValue(selectedIndex);

        MessageBox.confirmPopup("Do you really want to delete '" + itemToRemove + "'?", //$NON-NLS$
                deleteButton, new MessageBox.Callback() {
            @Override
            public void onSelect(MessageBox.Choice value) {
                switch(value) {
                    case YES:
                        removeCurrentAuthItem(itemToRemove);
                }
            }
        });
    }

    private void storeValues() {
        final Storage storage = Storage.getLocalStorageIfSupported();
        if(storage != null) {
            AutoBean<AuthItemStore> astore = AUTH_ITEM_FACTORY.authItemStore();
            ArrayList<AuthItem> a = new ArrayList<AuthItem>(authItems.values());

            AuthItemStore b = astore.as();
            b.setIAuthItems(a);

            String s = AutoBeanCodex.encode(astore).getPayload();
            storage.setItem(LOCAL_STORE_KEY, s);
        }
    }

    private void retrieveValues() {
        final Storage storage = Storage.getLocalStorageIfSupported();
        if(storage != null) {
            final String jsonValue = storage.getItem(LOCAL_STORE_KEY);
            if(jsonValue != null) {
                AutoBean<AuthItemStore> astore = AutoBeanCodex.decode(AUTH_ITEM_FACTORY, AuthItemStore.class, jsonValue);
                for(AuthItem i : astore.as().getIAuthItems()) {
                    authItems.put(i.getItemName(), i);
                }
            }
        }
    }

    /**
     * Adds a {@link com.google.gwt.event.dom.client.ChangeEvent} handler.
     *
     * @param handler the change handler
     * @return {@link com.google.gwt.event.shared.HandlerRegistration} used to remove this handler
     */
    @Override
    public HandlerRegistration addChangeHandler(ChangeHandler handler) {
        return addDomHandler(handler, ChangeEvent.getType());
    }
}
