package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;

import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.TopProductsCell;
import de.marketmaker.iview.dmxml.TopProductsElement;
import de.marketmaker.iview.dmxml.TopProductsRow;
import de.marketmaker.iview.dmxml.TopProductsTable;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.QwiAndPricedata;
import de.marketmaker.iview.mmgwt.mmweb.client.data.BestOfCell;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ContentFlagsEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.MetadataAware;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Shows TopProducts (i.e., DZ/WGZ Certificates that belong to a security).
 * This snippet is complicated by the fact that is is part of a page with other snippets but it
 * needs to run in its own DmxmlContext, as is depends on QuoteMetadata.
 * When a new Portait-Page is first shown, the following requests will be issued in "threads"
 * A and B:<p>
 * A MscQuoteMetadata, then TopProducts, then IMGResults<br>
 * B <em>all Blocks except TopProducts</em>
 * <p>
 * It may happen that the 3 requests in A complete before B. But only after B completes will the
 * snippets view be created and shown. Therefore, we have to evaluate whether the view is already
 * attached to its container and, if not, store the result of TopProducts/IMGResults in a
 * Command that will be executed after the view becomes available.
 *
 * @author Michael Lösch
 */

public class TopProductsSnippet extends AbstractMiniPortraitsSnippet<TopProductsTable>
        implements MetadataAware, IsVisible, PdfParameterSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("TopProducts"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            try {
                return new TopProductsSnippet(context, config);
            } catch (Exception e) {
                // TODO improve handling for missing configuration
                DebugUtil.logToServer("<newSnippet> TopProducts init failed ", e);  // $NON-NLS$
                return null;
            }
        }
    }

    private String[] names = null;

    private final MultiViewSupport multiViewSupport;

    private String currentSymbol;

    private BestOfProvider.BestOfConfiguration bestOfConfiguration;

    private boolean visible;

    private Command pendingViewUpdate;

    public TopProductsSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        final String[] tabIds = config.getArray("tabIds"); // $NON-NLS$
        final String[] tabTitles = new String[tabIds.length];
        for (int i = 0, tabIdsLength = tabIds.length; i < tabIdsLength; i++) {
            tabTitles[i] = BestOfProvider.INSTANCE.getConfiguration(tabIds[i]).getTitle();
        }
        this.multiViewSupport = new MultiViewSupport(tabTitles, null);
        this.multiViewSupport.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            public void onValueChange(ValueChangeEvent<Integer> e) {
                setConfiguration(tabIds[e.getValue()]);
                ackParametersChanged();
            }
        });
        setConfiguration(tabIds[0]);
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setEnabled(false);
        // do not call super.setSymbol: we wait for metadata
    }

    private void setConfiguration(String tabId) {
        this.bestOfConfiguration = BestOfProvider.INSTANCE.getConfiguration(tabId);
        if (this.bestOfConfiguration != null) {
            final List<BestOfCell> cells = this.bestOfConfiguration.getCells();
            this.names = new String[cells.size()];
            for (int i = 0; i < cells.size(); i++) {
                this.names[i] =  cells.get(i).getTitle();
            }
        }
        else {
            this.names = null;
        }
    }

    @Override
    protected void initBlock() {
        this.block = createBlock("MSC_TopProducts"); // $NON-NLS-0$
        this.block.setEnabled(false);
    }

    @Override
    protected MiniPortraitsSnippetView<TopProductsTable> createView() {
        return new MiniPortraitsSnippetView<>(this);
    }

    @Override
    protected void onContainerAvailable() {
        if (this.pendingViewUpdate != null) {
            final Command cmd = this.pendingViewUpdate;
            this.pendingViewUpdate = null;
            Scheduler.get().scheduleDeferred(cmd);
        }
    }

    @Override
    protected int getInstrumentMaxCount() {
        return BestOfProvider.INSTANCE.getMaxCellCount();
    }

    public boolean isMetadataNeeded() {
        return true;
    }

    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        final String iid = metadata.getInstrumentdata().getIid();
        if (iid != null && !StringUtil.equals(this.currentSymbol, iid)) {
            final QuoteData qd = metadata.getQuotedata();
            this.visible = ContentFlagsEnum.CerUnderlyingDzbank.isAvailableFor(qd)
                    || ContentFlagsEnum.CerUnderlyingWgzbank.isAvailableFor(qd);
            this.currentSymbol = iid;
            ackParametersChanged();
        }
    }

    @Override
    protected void onParametersChanged() {
        if (this.currentSymbol == null || this.bestOfConfiguration == null) {
            this.block.setEnabled(false);
            return;
        }

        this.block.removeAllParameters();
        for (Map.Entry<String, String> entry : this.bestOfConfiguration.getParameters().entrySet()) {
            this.block.setParameter(entry.getKey(), entry.getValue());
        }
        this.block.setParameter("symbol", this.currentSymbol); // $NON-NLS$
        this.block.setParameter("issuername", GuiDefsLoader.getIssuerName()); // $NON-NLS$
        this.block.setEnabled(true);
        getView().enhanceTitle(this.bestOfConfiguration.getLongTitle());
    }

    private TopProductsCell getCellByKey(TopProductsRow row, String key) {
        final List<TopProductsCell> cols = row.getColumn();
        for (TopProductsCell col : cols) {
            if (col != null && key.equals(col.getKey())) {
                return col;
            }
        }
        return null;
    }

    protected void updateView(TopProductsTable result) {
        // we run with our own context and rely on MetaDataResults which also arrive out of context,
        // so we need to tell our controller about visibility
        this.contextController.updateVisibility(getView(), this.visible);
        if (result == null) {
            update(null);
            return;
        }
        final Object[][][] additionalLines = getAdditionalLines(result);

        if (getView().getViewMode() == MiniPortraitsSnippetView.ViewMode.CHART) {
            issueChartContext(new ChartBlocksCallback() {
                @Override
                void onSuccess(List<IMGResult> chartResults) {
                    update(chartResults, additionalLines);
                }
            });
        } else {
            update(additionalLines, getQwiAndPricedatas());
        }
    }

    private void update(final List<IMGResult> chartResults, final Object[][][] additionalLines) {
        final String[] head = this.names;
        if (getView().hasContainer()) {
            update(chartResults, names, additionalLines);
        }
        else {
            this.pendingViewUpdate = new Command() {
                @Override
                public void execute() {
                    update(chartResults, head, additionalLines);
                }
            };
        }
    }

    private void update(final Object[][][] additionalLines, final List<QwiAndPricedata> qwiAndPrices) {
        final String[] head = this.names;
        if (getView().hasContainer()) {
            super.update(head, additionalLines, qwiAndPrices);
        }
        else {
            this.pendingViewUpdate = new Command() {
                @Override
                public void execute() {
                    update(head, additionalLines, qwiAndPrices);
                }
            };
        }
    }

    // Object[cell][line][column]
    private Object[][][] getAdditionalLines(TopProductsTable result) {
        if (result.getRow().isEmpty()) {
            return null;
        }
        final TopProductsRow row = result.getRow().get(0);
        final List<BestOfCell> bestOfCells = this.bestOfConfiguration.getCells();
        final Object[][][] cells = new Object[bestOfCells.size()][][];
        for (int i = 0, cellsSize = bestOfCells.size(); i < cellsSize; i++) {
            final BestOfCell bestOfCell = bestOfCells.get(i);
            final String colKey = bestOfCell.getKey();
            final TopProductsCell fgCell = getCellByKey(row, colKey);
            if (fgCell == null) {
                cells[i] = new Object[0][];
                continue;
            }
            final TopProductsElement element = fgCell.getItem().get(0);
            cells[i] = new Object[1][];
            cells[i][0] = new Object[]{this.bestOfConfiguration.getSortFieldTitle(), Renderer.PERCENT.render(element.getValue())};
        }
        return cells;
    }


    @Override
    protected List<QwiAndPricedata> getQwiAndPricedatas() {
        final ArrayList<QwiAndPricedata> result = new ArrayList<QwiAndPricedata>();
        if (!this.block.isResponseOk() /*|| this.block.getResult().getRow().size() != 1*/) {
            return result;
        }

        final List<TopProductsRow> rows = this.block.getResult().getRow();
        if (rows.isEmpty()) {
            return result;
        }

        final TopProductsRow row = rows.get(0);
        if (row == null) {
            return Collections.emptyList();
        }

        final List<BestOfCell> listCells = this.bestOfConfiguration.getCells();
        for (BestOfCell cell : listCells) {
            result.add(getQwiAndPricedata(getCellByKey(row, cell.getKey())));
        }
        return result;
    }

    private QwiAndPricedata getQwiAndPricedata(TopProductsCell cell) {
        if (cell == null || cell.getItem() == null || cell.getItem().isEmpty()) {
            return null;
        }
        final TopProductsElement tpe = cell.getItem().get(0);
        return tpe == null ? null : new QwiAndPricedata(tpe.getTopproduct());
    }

    @Override
    protected IndexedViewSelectionModel getViewSelectionModel() {
        return this.multiViewSupport.getViewSelectionModel();
    }

    public void addPdfSnippetParameters(Map<String, String> mapParameters) {
        mapParameters.put("topProductTypes", this.block.getParameter("productType")); // $NON-NLS$
        mapParameters.put("topIssuername", this.block.getParameter("issuername")); // $NON-NLS$
    }
}