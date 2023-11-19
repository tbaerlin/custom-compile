/*
 * PriceListSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSelectionViewButtons;

import java.util.ArrayList;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderWNT.CallPutLinkListener;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TopFlopSnippetView extends SnippetView<TopFlopSnippet> {
    private final DefaultTableColumnModel callPutColumnModel;
    private final DefaultTableColumnModel changeColumnModel;
    private final DefaultTableColumnModel volumeColumnModel;

    private SnippetTableWidget twTops;

    private SnippetTableWidget twFlops;

    private Label numUpDown;

    private final SimplePanel panel;

    public TopFlopSnippetView(final TopFlopSnippet snippet) {
        super(snippet);
        setTitle(I18n.I.topsAndFlops());

        this.callPutColumnModel =
                createCallPutColumnModel(snippet.getConfiguration().getString("issuer", null)); // $NON-NLS-0$

        final String prefixToDelete = snippet.getConfiguration().getString("prefixToDelete", null); // $NON-NLS-0$
        this.changeColumnModel = createChangeColumnModel(prefixToDelete);
        this.volumeColumnModel = createVolumeColumeModel(prefixToDelete);

        this.panel = new SimplePanel();
        this.panel.setWidth("100%"); // $NON-NLS-0$
    }

    protected void onContainerAvailable() {
        if (this.snippet.getViewSelectionModel() != null) {
            createViewSelectionToolbar();
        }
        this.container.setContentWidget(this.panel);
    }

    private void createViewSelectionToolbar() {
        final FloatingToolbar toolBar =
                new ViewSelectionViewButtons(this.snippet.getViewSelectionModel()).getToolbar();

        toolBar.addFill();
        toolBar.add(Button.text(I18n.I.list())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        snippet.gotoList();
                    }
                })
                .build());
        this.container.setTopWidget(toolBar);
    }

    protected static TableColumn createNameColumn(final float width, final int maxLength, final String prefixToDelete) {
        final TableCellRenderer renderer;
        if (SessionData.INSTANCE.isAnonymous()) {
            renderer = TableCellRenderers.STRING;
        }
        else {
            renderer = new TableCellRenderers.QuoteLinkRenderer(maxLength, "").withPrefixToDelete(prefixToDelete);
        }
        return new TableColumn("", width).withRenderer(renderer);
    }

    protected static TableColumn createPriceColumn(final float width) {
        return new TableColumn(I18n.I.price(), width).withRenderer(TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH);
    }

    protected static TableColumn createVolumeColumn(final float width) {
        return new TableColumn(I18n.I.volume(), width).withRenderer(TableCellRenderers.VOLUME_LONG_PUSH);
    }

    protected static TableColumn createChangeColumn(final float width) {
        return new TableColumn("+/-%", width).withCellClass("mm-right") // $NON-NLS-0$ $NON-NLS-1$
                .withRenderer(TableCellRenderers.CHANGE_PERCENT_PUSH);
    }

    protected static TableColumn createTimeColumn(final float width) {
        return new TableColumn(I18n.I.time(), width).withCellClass("mm-center")  // $NON-NLS-0$
                .withRenderer(TableCellRenderers.DATE_OR_TIME_COMPACT_PUSH);
    }

    private static DefaultTableColumnModel createCallPutColumnModel(final String issuer) {
        return new DefaultTableColumnModel(new TableColumn[]{
                createNameColumn(0.50f, 18, null),
                createChangeColumn(0.18f),
                createCallPutLinkColumn(true, 0.16f, issuer),
                createCallPutLinkColumn(false, 0.16f, issuer)
        });
    }

    private static DefaultTableColumnModel createVolumeColumeModel(final String prefixToDelete) {
        return new DefaultTableColumnModel(new TableColumn[]{
                createNameColumn(0.50f, 18, prefixToDelete),
                createPriceColumn(0.18f),
                createVolumeColumn(0.18f),
                createTimeColumn(0.14f)
        });
    }

    private static DefaultTableColumnModel createChangeColumnModel(final String prefixToDelete) {
        return new DefaultTableColumnModel(new TableColumn[]{
                createNameColumn(0.50f, 18, prefixToDelete),
                createPriceColumn(0.18f),
                createChangeColumn(0.18f),
                createTimeColumn(0.14f)
        });
    }

    protected static TableColumn createCallPutLinkColumn(final boolean call, final float width,
                                                         final String issuer) {
        final String name = call ? I18n.I.tableHeaderCalls() : I18n.I.tableHeaderPuts();
        return new TableColumn(name, width).withCellClass("mm-center") // $NON-NLS-0$
                .withRenderer(new TableCellRenderer() {
                    public void render(Object data, StringBuffer sb, Context context) {
                        final QuoteWithInstrument qwi = (QuoteWithInstrument) data;
                        context.appendLink(CallPutLinkListener.INSTANCE.createContext(qwi, call, issuer),
                                name, null, sb);
                    }

                    public boolean isPushRenderer() {
                        return false;
                    }

                    public String getContentClass() {
                        return null;
                    }
                });
    }


    void update(TableDataModel dtmTops, TableDataModel dtmFlops, String numUp, String numDown) {
        update(dtmTops, dtmFlops, getLabel(numUp, numDown));
    }

    void update(TableDataModel dtmTops, TableDataModel dtmFlops, String label) {
        if (this.twTops == null) {
            initWidgets();
        }
        DefaultTableColumnModel columnModel = getColumnModel();
        reloadTitle();

        columnModel.getTableColumn(0).setTitle(I18n.I.tableHeaderTops());
        this.twTops.update(columnModel, dtmTops);
        columnModel.getTableColumn(0).setTitle(I18n.I.tableHeaderFlops());
        this.twFlops.update(columnModel, dtmFlops);

        this.numUpDown.setText(label);
    }

    private String getLabel(String numUp, String numDown) {
        return snippet.mode == TopFlopSnippet.Mode.CHANGE
                ? I18n.I.winnerLooser(numUp, numDown)
                : ""; // $NON-NLS-0$
    }

    private DefaultTableColumnModel getColumnModel() {
        switch (snippet.mode) {
            case CHANGE:
                return this.changeColumnModel;
            case VOLUME:
                return this.volumeColumnModel;
            case CALL_PUT:
                return this.callPutColumnModel;
            default:
                assert false;
                return null;
        }
    }

    private void initWidgets() {
        this.twTops = SnippetTableWidget.create(this.changeColumnModel);
        this.twFlops = SnippetTableWidget.create(this.changeColumnModel);
        this.numUpDown = new Label(""); // $NON-NLS-0$
        this.numUpDown.setStyleName("mm-snippet-topflopNumUpDown"); // $NON-NLS-0$

        final Grid g = new Grid(4, 1);
        g.setCellPadding(0);
        g.setCellSpacing(0);
        g.setWidth("100%"); // $NON-NLS-0$
        g.setWidget(0, 0, this.twTops);
        g.setText(1, 0, " "); // $NON-NLS-0$
        g.getCellFormatter().setStyleName(1, 0, "mm-snippet-topflopBlank"); // $NON-NLS-0$
        g.setWidget(2, 0, this.twFlops);
        g.setWidget(3, 0, this.numUpDown);
        this.panel.add(g);
    }

    public ArrayList<PushRenderItem> getRenderItems(DefaultTableDataModel dtmTop,
                                                    DefaultTableDataModel dtmFlop) {
        final ArrayList<PushRenderItem> result = this.twTops.getRenderItems(dtmTop);
        result.addAll(this.twFlops.getRenderItems(dtmFlop));
        return result;
    }
}
