/*
 * SimpleMainView.java
 *
 * Created on 03.03.2008 08:29:37
 *
 * Copyright (c) Mvwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

/**
 * MainView subclass that removes north and west panel
 * 
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SimpleMainView extends LegacyMainView {
    public SimpleMainView(SimpleMainController mc) {
        super(mc);
    }

    @Override
    protected TopToolbar createTopToolbar() {
        return new LegacyTopToolbar(ToolbarConfiguration.BASIC);
    }


    @Override
    protected void initNorthPanel() {
        // TODO: add logo, maybe print page tool
        // empty
    }

    @Override
    protected void initWestPanel() {
        // empty
    }

    @Override
    protected void initEastPanel() {
        // empty
    }
}
