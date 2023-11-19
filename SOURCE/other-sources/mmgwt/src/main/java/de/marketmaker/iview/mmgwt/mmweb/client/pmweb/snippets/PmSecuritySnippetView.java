/*
 * PmSecuritySnippetView.java
 *
 * Created on 07.06.13 16:48
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.Instrument;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.InstrumentWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;

/**
 * @author Markus Dick
 * @author Michael Lösch
 */
public class PmSecuritySnippetView<S extends Snippet<S>> extends SnippetView<S> {
    final InstrumentWidget instrumentWidget;

    public PmSecuritySnippetView(S snippet) {
        super(snippet);
        this.instrumentWidget = new InstrumentWidget();
    }

    public void update(Instrument instrument) {
        this.instrumentWidget.setValue(instrument);
    }

    public void showNoData() {
        this.instrumentWidget.setValue(null);
    }

    @Override
    protected void onContainerAvailable() {
        this.container.setHeaderVisible(false);
        this.container.setContentWidget(this.instrumentWidget);
    }
}
