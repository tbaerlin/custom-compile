package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.QwiAndPricedata;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on 10.08.11 09:28
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 */


public abstract class AbstractMiniPortraitsSnippet<V extends BlockType> extends
        AbstractSnippet<AbstractMiniPortraitsSnippet<V>,
                MiniPortraitsSnippetView<V>>
        implements SymbolSnippet, PushRegisterHandler {


    abstract class ChartBlocksCallback implements AsyncCallback<ResponseType> {
        abstract void onSuccess(List<IMGResult> chartResults);

        public void onFailure(Throwable throwable) {
            Firebug.log("ChartBlockCallback request failed! " + throwable);
            currentImgResults = null;
        }

        public void onSuccess(ResponseType responseType) {
            final List<IMGResult> imgResults = new ArrayList<IMGResult>();
            for (DmxmlContext.Block<IMGResult> chartBlock : chartBlocks) {
                imgResults.add(chartBlock.isEnabled() && chartBlock.isResponseOk() ? chartBlock.getResult() : null);
            }
            onSuccess(imgResults);
        }
    }

    protected DmxmlContext.Block<V> block;

    private List<DmxmlContext.Block<IMGResult>> chartBlocks;

    private final DmxmlContext chartContext = new DmxmlContext();

    protected final PriceSupport priceSupport = new PriceSupport(this);

    private List<IMGResult> currentImgResults;

    public AbstractMiniPortraitsSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
        setView(createView());
        initBlock();
        this.chartBlocks = new ArrayList<DmxmlContext.Block<IMGResult>>();
        for (int i = 0; i < getInstrumentMaxCount(); i++) {
            final DmxmlContext.Block<IMGResult> chartBlock = this.chartContext.addBlock("IMG_BasicChart"); // $NON-NLS$
            chartBlock.setParameter("period", "P3M"); // $NON-NLS$
            chartBlock.setParameter("width", "140"); // $NON-NLS$
            chartBlock.setParameter("height", "90"); // $NON-NLS$
            this.chartBlocks.add(chartBlock);
        }
    }

    @Override
    public void activate() {
        getView().handleExpandCollapseState();
    }

    @Override
    public void deactivate() {
        this.priceSupport.deactivate();
    }

    public void destroy() {
        deactivate();
        destroyBlock(this.block);
        for (DmxmlContext.Block<IMGResult> chartBlock : chartBlocks) {
            this.chartContext.removeBlock(chartBlock);
        }
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (!this.priceSupport.isLatestPriceGeneration() && event.isPushedUpdate()) {
            getView().updatePushData(currentImgResults);
        }
    }

    public void updateView() {
        updateView(false);
    }

    public void updateView(boolean force) {
        this.priceSupport.invalidateRenderItems();
        if (!force && (!this.block.isResponseOk() || !this.block.blockChanged())) {
            return;
        }
        updateView(this.block.getResult());
        this.priceSupport.updatePriceGeneration();
        this.priceSupport.activate();
    }

    protected void update(List<IMGResult> imgResults) {
        update(imgResults, (imgResults != null) ? new String[imgResults.size()] : null, null);
    }

    protected void update(List<IMGResult> imgResults, String[] head, Object[][][] additionalLines) {
        this.currentImgResults = imgResults;
        getView().update(imgResults, head, additionalLines);
    }

    protected void update(String[] head, Object[][][] additionalLines,
            List<QwiAndPricedata> qwiAndPrices) {
        getView().update(head, additionalLines, qwiAndPrices);
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        getConfiguration().put("symbol", symbol); // $NON-NLS$
        onParametersChanged();
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (!this.chartBlocks.isEmpty()) {
            for (DmxmlContext.Block<IMGResult> chartBlock : chartBlocks) {
                if (chartBlock.isEnabled() && chartBlock.isResponseOk()) {
                    event.addVwdcode(chartBlock.getResult());
                }
            }
        }
        for (QwiAndPricedata data : getQwiAndPricedatas()) {
            if (data != null) {
                event.addVwdcode(data.getHasPricedata());
            }
        }
        return null;
    }

    protected void issueChartContext(ChartBlocksCallback callback) {
        if (prepareChartBlocks()) {
            this.chartContext.issueRequest(callback);
        }
        else {
            callback.onSuccess(Collections.<IMGResult>emptyList());
        }
    }

    private boolean prepareChartBlocks() {
        for (DmxmlContext.Block<IMGResult> chartBlock : this.chartBlocks) {
            chartBlock.setEnabled(false);
        }

        int n = 0;
        for (QwiAndPricedata qwiP : getQwiAndPricedatas()) {
            if (qwiP == null) {
                continue;
            }
            final DmxmlContext.Block<IMGResult> chartBlock = this.chartBlocks.get(n);
            chartBlock.setEnabled(true);
            chartBlock.setParameter("symbol", qwiP.getInstrumentData().getIid()); // $NON-NLS$
            if (++n == chartBlocks.size()) {
                break;
            }
        }
        return n > 0;
    }

    protected IndexedViewSelectionModel getViewSelectionModel() {
        return null;
    }

    protected abstract void initBlock();

    protected abstract MiniPortraitsSnippetView<V> createView();

    protected abstract void updateView(V result);

    protected abstract int getInstrumentMaxCount();

    protected abstract List<QwiAndPricedata> getQwiAndPricedatas();

    protected void onContainerAvailable() {
        // subclasses may override this
    }
}