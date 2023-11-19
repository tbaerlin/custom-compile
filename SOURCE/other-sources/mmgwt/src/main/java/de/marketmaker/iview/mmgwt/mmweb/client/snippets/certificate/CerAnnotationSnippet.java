/*
 * StaticDataSTKSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.certificate;

import de.marketmaker.iview.dmxml.CERDetailedStaticData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.*;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CerAnnotationSnippet
        extends AbstractSnippet<CerAnnotationSnippet, SnippetTextView<CerAnnotationSnippet>>
        implements SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("certificate.CerAnnotation"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new CerAnnotationSnippet(context, config);
        }
    }

    private DmxmlContext.Block<CERDetailedStaticData> blockStatic;

    private CerAnnotationSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        setView(new SnippetTextView<CerAnnotationSnippet>(this));

        this.blockStatic = createBlock("CER_DetailedStaticData"); // $NON-NLS-0$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.blockStatic.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void destroy() {
        destroyBlock(this.blockStatic);
        getView().setHtml("&nbsp;"); // $NON-NLS-0$
    }

    public void updateView() {
        if (!this.blockStatic.isResponseOk()) {
            getView().setHtml("&nbsp;"); // $NON-NLS-0$
            return;
        }

        getView().setHtml(this.blockStatic.getResult().getAnnotation());
    }
}
