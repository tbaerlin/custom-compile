/*
 * WatchlistActions.java
 *
 * Created on 04.09.2009 15:32:52
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.dmxml.WLWatchlist;
import de.marketmaker.iview.dmxml.WLWatchlistResult;
import de.marketmaker.iview.dmxml.WatchlistElement;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * Bundles all the actions that can be performed on/for watchlists.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class WatchlistActions {

    static final WatchlistActions INSTANCE = new WatchlistActions();

    private static final String CREATE_POS_ERROR_CODE = "user.watchlist.positions.limit.exceeded"; // $NON-NLS-0$

    private final DmxmlContext.Block<WLWatchlistResult> wlCreateBlock
            = createBlock("WL_Watchlist_Create"); // $NON-NLS-0$

    private final DmxmlContext.Block<WLWatchlistResult> wlUpdateBlock
            = createBlock("WL_Watchlist_Update"); // $NON-NLS-0$

    private final DmxmlContext.Block<WLWatchlistResult> wlDeleteBlock
            = createBlock("WL_Watchlist_Delete"); // $NON-NLS-0$

    private final DmxmlContext.Block<WLWatchlistResult> wlPosCreateBlock
            = createBlock("WL_Position_Create"); // $NON-NLS-0$

    private final DmxmlContext.Block<WLWatchlistResult> wlPosDeleteBlock
            = createBlock("WL_Position_Delete"); // $NON-NLS-0$

    private final DmxmlContext.Block<WLWatchlistResult> wlPosUpdateBlock
            = createBlock("WL_Position_Update"); // $NON-NLS-0$


    private final ResponseTypeCallback wlCreatedCallback = new ResponseTypeCallback() {
        protected void onResult() {
            watchlistCreated();
        }
    };

    private final ResponseTypeCallback wlUpdatedCallback = new ResponseTypeCallback() {
        protected void onResult() {
            watchlistUpdated();
        }
    };

    private final ResponseTypeCallback wlDeletedCallback = new ResponseTypeCallback() {
        protected void onResult() {
            watchlistDeleted();
        }
    };

    private final ResponseTypeCallback wlPosCreatedCallback = new ResponseTypeCallback() {
        protected void onResult() {
            positionCreated();
        }
    };

    private final ResponseTypeCallback wlPosUpdatedCallback = new ResponseTypeCallback() {
        protected void onResult() {
            positionUpdated();
        }
    };

    private final ResponseTypeCallback wlPosDeletedCallback = new ResponseTypeCallback() {
        protected void onResult() {
            positionDeleted();
        }
    };

    private WatchlistActions() {
    }

    private DmxmlContext.Block<WLWatchlistResult> createBlock(final String key) {
        final DmxmlContext.Block<WLWatchlistResult> result = new DmxmlContext().addBlock(key);
        result.setParameter("userid", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS-0$
        return result;
    }

    public void createWatchlist(String name) {
        this.wlCreateBlock.setParameter("name", name); // $NON-NLS-0$
        this.wlCreateBlock.issueRequest(this.wlCreatedCallback);
        ActionPerformedEvent.fire("X_WL_INS"); // $NON-NLS-0$
    }

    private void watchlistCreated() {
        onResult(this.wlCreateBlock);
    }

    public void deleteWatchlist(String wlid) {
        this.wlDeleteBlock.setParameter("watchlistid", wlid); // $NON-NLS-0$
        this.wlDeleteBlock.issueRequest(this.wlDeletedCallback);
        ActionPerformedEvent.fire("X_WL_DEL"); // $NON-NLS-0$
    }

    private void watchlistDeleted() {
        onResult(this.wlDeleteBlock);
    }

    public void updateWatchlist(String wlid, String name) {
        this.wlUpdateBlock.setParameter("watchlistid", wlid); // $NON-NLS-0$
        this.wlUpdateBlock.setParameter("name", name); // $NON-NLS-0$
        this.wlUpdateBlock.issueRequest(this.wlUpdatedCallback);
        ActionPerformedEvent.fire("X_WL_UPD"); // $NON-NLS-0$
    }

    private void watchlistUpdated() {
        onResult(this.wlUpdateBlock);
    }

    public void createPosition(String wlid, QuoteWithInstrument qwi) {
        this.wlPosCreateBlock.setParameter("watchlistid", wlid); // $NON-NLS-0$
        this.wlPosCreateBlock.setParameter("symbol", qwi.getId()); // $NON-NLS-0$
        this.wlPosCreateBlock.issueRequest(this.wlPosCreatedCallback);
        ActionPerformedEvent.fire("X_WL_P_INS"); // $NON-NLS-0$
    }

    private void positionCreated() {
        if (!this.wlPosCreateBlock.isResponseOk()) {
            final ErrorType error = this.wlPosCreateBlock.getError();
            if (error != null) {
                if (CREATE_POS_ERROR_CODE.equals(error.getCode())) {
                    Dialog.error(I18n.I.cannotAddPositions(),
                            I18n.I.maximumPositionCountReached());
                }
                return;
            }
        }
        onResult(this.wlPosCreateBlock);
    }

    public void deletePosition(String wlid, String positionId) {
        deletePosition(wlid, positionId, this.wlPosDeletedCallback);
    }

    public void deletePosition(String wlid, String positionId,
            AsyncCallback<ResponseType> delegate) {
        this.wlPosDeleteBlock.setParameter("watchlistid", wlid); // $NON-NLS-0$
        this.wlPosDeleteBlock.setParameter("positionid", positionId); // $NON-NLS-0$
        this.wlPosDeleteBlock.issueRequest(delegate);
        ActionPerformedEvent.fire("X_WL_P_DEL"); // $NON-NLS-0$
    }

    private void positionDeleted() {
        onResult(this.wlPosDeleteBlock);
    }

    public void updatePosition(String watchlistId, String positionId, QuoteWithInstrument qwi) {
        this.wlPosUpdateBlock.setParameter("userid", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS-0$
        this.wlPosUpdateBlock.setParameter("watchlistid", watchlistId); // $NON-NLS-0$
        this.wlPosUpdateBlock.setParameter("positionid", positionId); // $NON-NLS-0$
        this.wlPosUpdateBlock.setParameter("symbol", qwi.getQuoteData().getQid()); // $NON-NLS-0$
        this.wlPosUpdateBlock.issueRequest(this.wlPosUpdatedCallback);
        ActionPerformedEvent.fire("X_WL_P_UPD"); // $NON-NLS-0$
    }

    private void positionUpdated() {
        onResult(this.wlPosUpdateBlock);
    }


    private void onResult(DmxmlContext.Block<WLWatchlistResult> block) {
        if (block.isResponseOk()) {
            onSuccess(block);
        }
        else {
            AbstractMainController.INSTANCE.showError(I18n.I.internalError());
        }
    }


    private String getToken(DmxmlContext.Block<WLWatchlistResult> block, WLWatchlist watchlist) {
        if (!SessionData.isAsDesign()) {
            throw new UnsupportedOperationException("only supported in ICE/AS design"); // $NON-NLS$
        }

        if (block == this.wlDeleteBlock) {
            final List<WatchlistElement> element = watchlist.getWatchlists().getElement();
            if (element.size() < 1) {
                return "B_W"; // $NON-NLS$
            }
            return "B_W_" + element.get(0).getWatchlistid(); // $NON-NLS$
        }

        return "B_W_" + block.getResult().getWatchlistid(); // $NON-NLS$
    }

    private void onSuccess(DmxmlContext.Block<WLWatchlistResult> block) {
        if (!SessionData.isAsDesign()) {
            doLegacyStuffOnSuccess(block);
            return;
        }

        new LoadWatchlistsMethod(new AsyncCallback<WLWatchlist>() {
            @Override
            public void onFailure(Throwable throwable) {
                if (throwable != null && StringUtil.hasText(throwable.getMessage())) {
                    AbstractMainController.INSTANCE.showError(I18n.I.internalError() + ": " + throwable.getMessage()); // $NON-NLS$
                }
                else {
                    AbstractMainController.INSTANCE.showError(I18n.I.internalError() + ": loading watchlists failed"); // $NON-NLS$
                }
            }

            @Override
            public void onSuccess(WLWatchlist wlWatchlist) {
                final List<WatchlistElement> element = wlWatchlist.getWatchlists().getElement();
                new UpdateWatchlistMenuMethod().withElements(element).execute();

                doOnSuccess(block, wlWatchlist);
            }
        }).execute();
    }

    private void doOnSuccess(DmxmlContext.Block<WLWatchlistResult> block, WLWatchlist watchlist) {
        if (!SessionData.isAsDesign()) {
            throw new UnsupportedOperationException("only supported in ICE/AS design"); // $NON-NLS$
        }
        WatchlistController.INSTANCE.setToBeRequested();
        final AppConfig appConfig = SessionData.INSTANCE.getUser().getAppConfig();
        if (block == this.wlPosCreateBlock && !appConfig.getBooleanProperty(AppConfig.OPEN_WATCHLIST_ON_ADD, true)) {
            AbstractMainController.INSTANCE.refresh();
        }
        else {
            PlaceUtil.goTo(getToken(block, watchlist));
        }
    }

    private void doLegacyStuffOnSuccess(DmxmlContext.Block<WLWatchlistResult> block) {
        if (SessionData.isAsDesign()) {
            throw new UnsupportedOperationException("not supported in ICE/AS design"); // $NON-NLS$
        }

        WatchlistController.INSTANCE.setToBeRequested();
        final AppConfig appConfig = SessionData.INSTANCE.getUser().getAppConfig();
        if (block == this.wlPosCreateBlock && !appConfig.getBooleanProperty(AppConfig.OPEN_WATCHLIST_ON_ADD, true)) {
            AbstractMainController.INSTANCE.refresh();
        }
        else {
            PlaceUtil.goTo(getLegacyToken(block));
        }
    }

    private String getLegacyToken(DmxmlContext.Block<WLWatchlistResult> block) {
        if (SessionData.isAsDesign()) {
            throw new UnsupportedOperationException("not supported in ICE/AS design"); // $NON-NLS$
        }

        if (block == this.wlDeleteBlock) {
            return "B_W"; // $NON-NLS-0$
        }
        return "B_W/" + block.getResult().getWatchlistid(); // $NON-NLS-0$
    }
}
