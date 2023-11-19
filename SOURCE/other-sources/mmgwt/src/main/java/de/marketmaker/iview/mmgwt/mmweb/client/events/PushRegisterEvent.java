package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.HasPricedata;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.dmxml.MSCOrderbook;
import de.marketmaker.iview.dmxml.MSCPriceDataExtended;
import de.marketmaker.iview.dmxml.MSCPriceDatas;
import de.marketmaker.iview.dmxml.MSCTopFlop;
import de.marketmaker.iview.dmxml.PFEvaluation;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.dmxml.WLWatchlist;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushChangeRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created on 10.02.2010 13:42:15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 * @author oflege
 */
public class PushRegisterEvent extends GwtEvent<PushRegisterHandler> {

    private static Type<PushRegisterHandler> TYPE;

    private final HashSet<String> vwdcodes = new HashSet<String>();

    private final ArrayList<DmxmlContext.Block> blocksToReload = new ArrayList<DmxmlContext.Block>();

    private final ArrayList<AsyncCallback<ResponseType>> reloadCallbacks
            = new ArrayList<AsyncCallback<ResponseType>>();

    public static Type<PushRegisterHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<PushRegisterHandler>();
        }
        return TYPE;
    }

    public Type<PushRegisterHandler> getAssociatedType() {
        return TYPE;
    }

    public PushRegisterEvent() {
    }

    protected void dispatch(PushRegisterHandler handler) {
        handler.onPushRegister(this);
    }

    public HashSet<String> getVwdcodes() {
        return this.vwdcodes;
    }

    /**
     * Registers keys of the block's elements for push if allowed
     *
     * @param block has elements that may be pushable
     * @return true iff all element keys have been added
     */
    public int addVwdcodes(MSCListDetails block) {
        return addVwdcodes(block.getElement());
    }

    /**
     * Registers keys of the block's elements for push if allowed
     *
     * @param block has elements that may be pushable
     * @return true iff all element keys have been added
     */
    public int addVwdcodes(MSCPriceDataExtended block) {
        return addVwdcodes(block.getElement());
    }

    /**
     * Registers keys of the block's elements for push if allowed
     *
     * @param block has elements that may be pushable
     * @return true iff all element keys have been added
     */
    public int addVwdcodes(MSCPriceDatas block) {
        return addVwdcodes(block.getElement());
    }

    /**
     * Registers keys of the block's elements for push if allowed
     *
     * @param block has elements that may be pushable
     * @return true iff all element keys have been added
     */
    public int addVwdcodes(MSCTopFlop block) {
        return addVwdcodes(block.getElement());
    }

    /**
     * Registers keys of the block's elements for push if allowed
     *
     * @param block has elements that may be pushable
     * @return true iff all element keys have been added
     */
    public int addVwdcodes(WLWatchlist block) {
        return addVwdcodes(block.getPositions().getPosition());
    }

    /**
     * Registers keys of the block's elements for push if allowed
     *
     * @param block has elements that may be pushable
     * @return true iff all element keys have been added
     */
    public int addVwdcodes(PFEvaluation block) {
        return addVwdcodes(block.getElements().getPosition());
    }

    /**
     * Adds vwdcode in data for push if allowed
     *
     * @param data has price that may be pushable
     * @return true iff push is allowed
     */
    public boolean addVwdcode(IMGResult data) {
        if (isPushable(data.getPricedata().getPriceQuality())) {
            this.vwdcodes.add(data.getQuotedata().getVwdcode());
            return true;
        }
        return false;
    }

    public boolean addVwdcode(MSCOrderbook data) {
        if (isPushable(data.getPriceQuality())) {
            this.vwdcodes.add(PushChangeRequest.ORDERBOOK_PREFIX + data.getVwdcode());
            return true;
        }
        return false;
    }

    private int addVwdcodes(List<? extends HasPricedata> elements) {
        int n = 0;
        for (HasPricedata e : elements) {
            if (addVwdcode(e)) {
                n++;
            }
        }
        return n;
    }

    /**
     * Adds vwdcode in data for push if allowed
     * @param data has price that may be pushable
     * @return true iff push is allowed
     */
    public boolean addVwdcode(HasPricedata data) {
        if (isPushable(data)) {
            this.vwdcodes.add(data.getQuotedata().getVwdcode());
            return true;
        }
        return false;
    }

    private boolean isPushable(HasPricedata data) {
        if (data.getPricedata() != null) {
            return isPushable(data.getPricedata().getPriceQuality());
        }
        if (data.getFundpricedata() != null) {
            return isPushable(data.getFundpricedata().getPriceQuality());
        }
        if (data.getContractpricedata() != null) {
            return isPushable(data.getContractpricedata().getPriceQuality());
        }
        if (data.getLmepricedata() != null) {
            return isPushable(data.getLmepricedata().getPriceQuality());
        }
        return false;
    }

    private boolean isPushable(String quality) {
        return quality != null && quality.endsWith("+"); // $NON-NLS-0$
    }

    public void addComponentToReload(DmxmlContext.Block block, final Snippet s) {
        addComponentToReload(block, new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                s.updateView();
            }
        });
    }

    public void addComponentToReload(DmxmlContext.Block block, AsyncCallback<ResponseType> callback) {
        if (block != null) {
            this.blocksToReload.add(block);
        }
        this.reloadCallbacks.add(callback);
    }

    public ArrayList<DmxmlContext.Block> getBlocksToReload() {
        return this.blocksToReload;
    }

    public ArrayList<AsyncCallback<ResponseType>> getReloadCallbacks() {
        return this.reloadCallbacks;
    }
}
