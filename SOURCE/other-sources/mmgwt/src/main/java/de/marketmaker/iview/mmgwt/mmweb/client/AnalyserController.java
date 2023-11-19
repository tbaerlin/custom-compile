/*
 * VwdPageController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AnalyserSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AnalyserController extends DelegatingPageController {
    private final String type;
    private final int height;
    private AnalyserSnippet analyserSnippet;

    public AnalyserController(ContentContainer contentContainer, String type) {
        this(contentContainer, type, -1);
    }

    public AnalyserController(ContentContainer contentContainer, String type, int height) {
        super(contentContainer);
        this.type = type;
        this.height = height;
    }

    protected void initDelegate() {
        final String height = this.height == -1 ? "" : (";height=" + this.height); // $NON-NLS-0$ $NON-NLS-1$
        this.delegate = SnippetsFactory.createFlexController(getContentContainer(),
                "Analyser(id=ana;withSymbol=false;type=" + this.type + height + ")"); // $NON-NLS-0$ $NON-NLS-1$
        this.analyserSnippet = (AnalyserSnippet) this.delegate.getSnippet("ana"); // $NON-NLS-0$
    }

    @Override
    public String getPrintHtml() {
        final String printHtml = this.analyserSnippet.getPrintHtml();
        return "super".equals(printHtml) ? super.getPrintHtml() : printHtml; // $NON-NLS-0$
    }
}
