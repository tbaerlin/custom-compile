/*
 * LegacyGrainsMainView.java
 *
 * Created on 03.03.2008 08:29:37
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

/**
 * @author Michael Lösch
 * @author Ulrich Maurer
 */
public class LegacyGrainsMainView extends LegacyMainView {

    public LegacyGrainsMainView(GrainsMainController mc) {
        super(mc);
    }

    @Override
    protected TopToolbar createTopToolbar() {
        return new LegacyTopToolbar(ToolbarConfiguration.BASIC);
    }

    @Override
    protected void initWestPanel() {
        // empty
    }
}
