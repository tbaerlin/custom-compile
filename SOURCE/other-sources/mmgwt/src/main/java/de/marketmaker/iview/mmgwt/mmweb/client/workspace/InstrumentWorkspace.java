/*
 * InstrumentWorkspace.java
 *
 * Created on 05.04.2008 14:55:07
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.extjs.gxt.ui.client.event.DNDEvent;

import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MainControllerListenerAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class InstrumentWorkspace extends AbstractTreeWorkspace<InstrumentWorkspaceItem> {

    public static final InstrumentWorkspace INSTANCE = new InstrumentWorkspace();

    private static final String ID = "ins"; // $NON-NLS-0$

    private InstrumentWorkspace() {
        super(I18n.I.instruments());
    }

    @Override
    public String getStateKey() {
        return StateSupport.INSTRUMENT;
    }

    @Override
    protected void restoreState() {
        final InstrumentWorkspaceConfig c = (InstrumentWorkspaceConfig)
                SessionData.INSTANCE.getUser().getAppConfig().getWorkspaceConfig(ID);
        if (c != null) {
            for (InstrumentWorkspaceConfig.Item item : c.getItems()) {
                addLeaf(new InstrumentWorkspaceItem(item));
            }
        }

        if(!SessionData.isAsDesign()) {
            AbstractMainController.INSTANCE.addListener(new MainControllerListenerAdapter() {
                public void beforeStoreState() {
                    InstrumentWorkspace.this.beforeStoreState();
                }
            });
        }
    }

    private void beforeStoreState() {
        final InstrumentWorkspaceConfig c = new InstrumentWorkspaceConfig();
        for (InstrumentWorkspaceItem workspaceItem : getContent()) {
            c.addItem(workspaceItem.getItem());
        }
        SessionData.INSTANCE.getUser().getAppConfig().addWorkspace(ID, c);
    }

    public void add(QuoteWithInstrument qwi) {
        addLeaf(InstrumentWorkspaceItem.create(qwi));
    }

    protected String getDdGroup() {
        return "ins"; // $NON-NLS-0$
    }

    protected void onStartDrag(DNDEvent e) {
        final InstrumentWorkspaceItem iwi = getSelectedLeaf();
        if (iwi != null) {
            e.setData(iwi.toQuoteWithInstrument());
        }
    }
}
