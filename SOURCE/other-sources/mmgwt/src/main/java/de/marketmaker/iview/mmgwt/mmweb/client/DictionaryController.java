/*
* DictionaryController.java
*
* Created on 18.09.2008 13:04:53
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCLexiconEntries;
import de.marketmaker.iview.dmxml.MSCLexiconEntry;
import de.marketmaker.iview.dmxml.MSCLexiconInitials;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author Michael Lösch
 */

public class DictionaryController extends AbstractPageController {

    private DmxmlContext.Block<MSCLexiconInitials> initialsBlock;
    private DmxmlContext.Block<MSCLexiconEntries> entriesBlock = null;
    private DmxmlContext.Block<MSCLexiconEntry> entryBlock = null;

    private final DictionaryView view;

    class InitialsAsyncCallback implements AsyncCallback<ResponseType> {
        public void onFailure(Throwable caught) {
            Firebug.log("InitialsAsyncCallback.onFailure " + caught.getMessage()); // $NON-NLS-0$
        }

        public void onSuccess(ResponseType result) {
            if (initialsBlock != null && initialsBlock.isResponseOk()) {
                view.createInitialButtons(initialsBlock.getResult().getInitial());
                initialsBlock = null;
            }
        }
    }

    class EntriesAsyncCallback implements AsyncCallback<ResponseType> {
        public void onFailure(Throwable caught) {
            Firebug.log("EntriesAsyncCallback.onFailure " + caught.getMessage()); // $NON-NLS-0$
        }

        public void onSuccess(ResponseType result) {
            if (entriesBlock.isResponseOk()) {
                view.updateView(entriesBlock.getResult().getEntry());
                if (view.getOpenEntriesCount() == 0 && entriesBlock.getResult().getEntry().size() > 0) {
                    loadEntry(entriesBlock.getResult().getEntry().get(0).getId());
                }
            }
        }
    }

    class EntryAsyncCallback implements AsyncCallback<ResponseType> {
        public void onFailure(Throwable caught) {
            Firebug.log("EntryAsyncCallback.onFailure " + caught.getMessage()); // $NON-NLS-0$
        }

        public void onSuccess(ResponseType result) {
            if (entryBlock.isResponseOk()) {
                view.updateView(entryBlock.getResult());
            }
        }
    }

    public DictionaryController(ContentContainer cc) {
        super(cc, null);
        this.view = new DictionaryView(this);
        this.initialsBlock = new DmxmlContext().addBlock("MSC_Lexicon_Initials"); // $NON-NLS-0$
        this.entriesBlock = new DmxmlContext().addBlock("MSC_Lexicon_Entries"); // $NON-NLS-0$
        this.entriesBlock.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.entryBlock = new DmxmlContext().addBlock("MSC_Lexicon_Entry"); // $NON-NLS-0$
    }


    public void onPlaceChange(PlaceChangeEvent event) {
        if (this.initialsBlock != null) {
            this.initialsBlock.issueRequest(new InitialsAsyncCallback());
        }
        final HistoryToken historyToken = event.getHistoryToken();
        final String p1 = historyToken.get(1, null);
        final String p2 = historyToken.get(2, null);
        if (p1 != null && p1.length() == 1) {
            loadEntriesList(p1);
        }
        if (p2 != null) {
            loadEntriesList(p1);
            loadEntry(p2);
        }
        this.getContentContainer().setContent(this.view);
    }

    public void loadEntriesList(String initial) {
        this.entriesBlock.setParameter("initial", initial); // $NON-NLS-0$
        this.entriesBlock.issueRequest(new EntriesAsyncCallback());
    }

    public void loadEntry(String id) {
        this.entryBlock.setParameter("id", id); // $NON-NLS-0$
        this.entryBlock.issueRequest(new EntryAsyncCallback());
    }
}
