/*
 * NewsEntrySnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.NWSNews;
import de.marketmaker.iview.dmxml.NWSSearchElement;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsEntrySnippet extends AbstractSnippet<NewsEntrySnippet, NewsEntrySnippetView>
        implements LinkListener<NWSSearchElement> {
    public static class Class extends SnippetClass {
        public Class() {
            super("NewsEntry"); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new NewsEntrySnippet(context, config);
        }
    }

    private List<SymbolListSnippet> symbolListSnippets = new ArrayList<>();

    private DmxmlContext.Block<NWSNews> block;

    public DmxmlContext.Block<NWSNews> getNWSNewsBlock() {
        return this.block;
    }

    private NewsEntrySnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        setView(new NewsEntrySnippetView(this));

        this.block = createBlock("NWS_News"); // $NON-NLS$
        this.block.setEnabled(false);
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final String newsid = event.getHistoryToken().get("newsid"); // $NON-NLS$
        if (newsid != null) {
            setNewsId(newsid);
        }
    }

    public void ackNewsid(String newsid) {
        Firebug.log(getClass().getSimpleName() + " <ackNewsid> newsid=" + newsid);
        if (newsid == null) {
            this.block.setEnabled(false);
            getView().update(null);
            setDependentSymbols(null);
            return;
        }
        setNewsId(newsid);
        this.contextController.reload();
    }

    private void setNewsId(String newsid) {
        this.block.setEnabled(true);
        this.block.setParameter("newsid", newsid); // $NON-NLS$
    }

    public void clear() {
        getView().update(null);
        setDependentSymbols(null);
    }

    public void updateView() {
        if (!this.block.isEnabled()) {
            getView().update(null);
            setDependentSymbols(null);
            return;
        }
        if (this.block.isEnabled() && !this.block.blockChanged()) {
            return;
        }
        if (!block.isResponseOk()) {
            clear();
            return;
        }
        getView().update(this.block.getResult());
        setDependentSymbols(this.block.getResult().getInstruments().getInstrumentdata());
    }

    NWSNews getNews() {
        if (this.block.isResponseOk()) {
            return this.block.getResult();
        }
        return null;
    }

    public void addSymbolListSnippet(SymbolListSnippet s) {
        this.symbolListSnippets.add(s);
    }

    private void setDependentSymbols(List<InstrumentData> symbols) {
        for (SymbolListSnippet s : this.symbolListSnippets) {
            s.setSymbols(symbols);
        }
    }

    public void onClick(LinkContext<NWSSearchElement> context, Element e) {
        ackNewsid(context.data == null ? null : context.data.getNewsid());
    }
}
