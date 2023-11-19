/*
 * StaticDataSTKSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock;

import com.google.gwt.user.client.ui.FlowPanel;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.IdentifierData;
import de.marketmaker.iview.dmxml.IdentifierDataWithPrio;
import de.marketmaker.iview.dmxml.MSCPriceDataExtendedElement;
import de.marketmaker.iview.dmxml.STKDatesElement;
import de.marketmaker.iview.dmxml.STKKeyEstimates;
import de.marketmaker.iview.dmxml.STKStaticData;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.FlipLink;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModelBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StaticDataSTKSnippetView extends SnippetView<StaticDataSTKSnippet> {
    private static final String FLIP_ID_DATE = "date"; // $NON-NLS-0$

    private static final String FLIP_ID_INDEX = "ind"; // $NON-NLS-0$

    private TableColumnModel columnModelStatic;

    private SnippetTableWidget twStatic;

    private TableColumnModel columnModelDates;

    private SnippetTableWidget twDates;

    private TableColumnModel columnModelIndizes;

    private SnippetTableWidget twIndizes;

    private static final TableCellRenderers.StringRenderer LABEL_RENDERER
            = new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label"); // $NON-NLS-0$ $NON-NLS-1$

    public StaticDataSTKSnippetView(StaticDataSTKSnippet snippet) {
        super(snippet);
        setTitle(I18n.I.staticData());

        this.columnModelStatic = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.type(), 0.3f, LABEL_RENDERER),
                new TableColumn(I18n.I.value(), 0.7f, TableCellRenderers.DEFAULT_RIGHT)
        });

        this.columnModelDates = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.date(), 0.3f, LABEL_RENDERER),
                new TableColumn(I18n.I.fixedDate(), 0.7f, TableCellRenderers.DEFAULT_RIGHT)
        });

        this.columnModelIndizes = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.index(), 1f, TableCellRenderers.DEFAULT),
        });
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();

        this.twStatic = SnippetTableWidget.create(this.columnModelStatic);
        this.twDates = SnippetTableWidget.create(this.columnModelDates);
        this.twIndizes = SnippetTableWidget.create(this.columnModelIndizes);
        final FlowPanel panel = new FlowPanel();
        panel.add(this.twStatic);
        panel.add(this.twDates);
        panel.add(this.twIndizes);

        this.container.setContentWidget(panel);

        // TODO 2.0: necessary?
        update(null, null, null, null, null);
    }

    public void update(final STKStaticData staticData, STKKeyEstimates keyEstimates,
                       MSCPriceDataExtendedElement priceDataExtended, final List<STKDatesElement> dates,
                       final List<IdentifierDataWithPrio> list) {
        final String instrumentName;
        if (staticData != null) {
            final boolean factsetAllowed = Selector.isDzProfitEstimate();
            final boolean showWkn = SessionData.INSTANCE.isShowWkn();
            final boolean showIsin = SessionData.INSTANCE.isShowIsin();
            final String dividend = staticData.getDividend();
            final boolean hasDividend = StringUtil.hasText(dividend);
            final boolean gicsEnabled = Customer.INSTANCE.isVwd();

            final int numRows = 3
                    + (showWkn ? 1 : 0)
                    + (showIsin ? 1 : 0)
                    + (factsetAllowed ? 2 : 0)
                    + (hasDividend ? 2 : 1)
                    + (gicsEnabled ? 3 : 0);

            final TableDataModelBuilder builder = new TableDataModelBuilder(numRows, 2);
            if (showIsin) {
                builder.addRow("ISIN", staticData.getInstrumentdata().getIsin()); // $NON-NLS-0$
            }
            if (showWkn) {
                builder.addRow("WKN", staticData.getInstrumentdata().getWkn()); // $NON-NLS-0$
            }
            if (gicsEnabled) {
                builder.addRow(I18n.I.gicsSector(), staticData.getGicsSector());
                builder.addRow(I18n.I.gicsIndustryGroup(), staticData.getGicsIndustryGroup());
                builder.addRow(I18n.I.gicsIndustry(), staticData.getGicsIndustry());
                builder.addRow(I18n.I.gicsSubIndustry(), staticData.getGicsSubIndustry());
            } else {
                builder.addRow(I18n.I.sector(), staticData.getSector());
            }

            if (hasDividend) {
                final LinkListener<Link> linkListener = (linkLinkContext, e) -> {
                    PlaceUtil.goTo(PlaceUtil.getPortraitPlace(staticData.getInstrumentdata(),
                            staticData.getQuotedata(), "K"), true); // $NON-NLS-0$
                };

                builder.addRow(I18n.I.dividend(), new Link(linkListener, getDividend(staticData, dividend), null));
                builder.addRow(I18n.I.dividendYield(), getDividendYield(staticData, priceDataExtended));
            }
            else {
                builder.addRow(I18n.I.dividend(), "--");  // $NON-NLS-0$
            }

            if (factsetAllowed) {
                builder.addRow((Object[])getKGVe(keyEstimates));
                builder.addRow((Object[])getKCVe(keyEstimates));
            }
            builder.addRow(I18n.I.marketCapitalization(), getMarketCapitalization(priceDataExtended));

            builder.addRow(I18n.I.benchmark(), getBenchmarkQuote(staticData));

            this.twStatic.updateData(builder.getResult());
            instrumentName = staticData.getInstrumentdata().getName();
        }
        else {
            this.twStatic.updateData(DefaultTableDataModel.NULL);
            instrumentName = "";
        }

        if (dates != null) {
            final int numDates = dates.size();
            final DefaultTableDataModel tdm = new DefaultTableDataModel(numDates + 1, 2);
            int row = -1;
            String flipId = null;
            if (numDates > 1) {
                final FlipLink link = new FlipLink(this.twDates, FLIP_ID_DATE);
                tdm.setValuesAt(++row, new Object[]{"", link}); // $NON-NLS-0$
                link.setLocation(row, 1);
            }
            for (int i = 0; i < numDates; i++) {
                final STKDatesElement data = dates.get(numDates - i - 1);
                if (row == 1) {
                    flipId = FLIP_ID_DATE;
                }
                tdm.setValuesAt(++row, new Object[]{Formatter.LF.formatDate(data.getDate()), data.getTitle()}, flipId);
            }
            this.twDates.updateData(tdm);
        }
        else {
            this.twDates.updateData(DefaultTableDataModel.NULL);
        }

        if (list != null) {
            boolean empty = list.isEmpty();
            final DefaultTableDataModel tdm
                    = new DefaultTableDataModel(empty ? 1 : list.size() + 1, 1);
            if (empty) {
                tdm.setValueAt(0, 0, null);
            }
            else {
                int row = -1;
                String flipId = null;
                if (list.size() > 2) {
                    final FlipLink link = new FlipLink(this.twIndizes, FLIP_ID_INDEX);
                    tdm.setValuesAt(++row, new Object[]{link});
                    link.setLocation(row, 0);
                    this.columnModelIndizes.getTableColumn(0).setRowRenderer(row,
                            TableCellRenderers.DEFAULT_RIGHT);
                }
                else {
                    this.columnModelIndizes.getTableColumn(0).setRowRenderer(row + 1, null);
                }
                for (final IdentifierData data : list) {
                    if (row == 2) {
                        flipId = FLIP_ID_INDEX;
                    }
                    final QuoteWithInstrument qwi = new QuoteWithInstrument(data.getInstrumentdata(), data.getQuotedata())
                            .withHistoryContext(ItemListContext.createForPortrait(data, list, instrumentName));
                    tdm.setValuesAt(++row, new Object[]{qwi}, flipId);
                }
            }
            this.twIndizes.updateData(tdm);
        }
        else {
            this.twIndizes.updateData(DefaultTableDataModel.NULL);
        }
    }

    private String[] getKCVe(STKKeyEstimates keyEstimates) {
        if (keyEstimates == null) {
            return new String[]{I18n.I.priceCashflowRatioAbbr2(), "--"};
        }
        return new String[]{
                I18n.I.priceCashflowRatioAbbr2() + getYearSuffix(keyEstimates.getPriceCashflowRatioFiscalYearEnd()),
                Renderer.PRICE_MAX2.render(keyEstimates.getPriceCashflowRatio())
        };
    }

    private String[] getKGVe(STKKeyEstimates keyEstimates) {
        if (keyEstimates == null) {
            return new String[]{I18n.I.priceEarningsRatioAbbrE(), "--"};
        }
        return new String[]{
                I18n.I.priceEarningsRatioAbbrE() + getYearSuffix(keyEstimates.getPriceEarningRatioFiscalYearEnd()),
                Renderer.PRICE_MAX2.render(keyEstimates.getPriceEarningRatio())
        };
    }

    private String getYearSuffix(String day) {
        if (day == null) {
            return "";
        }
        return "(" + Formatter.LF.formatDateYyyy(day, "") + ")";
    }

    private String getMarketCapitalization(MSCPriceDataExtendedElement e) {
        return e.getPricedataExtended() == null
                ? "--"
                : Renderer.LARGE_NUMBER.render(e.getPricedataExtended().getMarketCapitalization());
    }

    private QuoteWithInstrument getBenchmarkQuote(STKStaticData staticData) {
        final IdentifierData benchmark = staticData.getBenchmark();
        return benchmark == null
                ? null
                : new QuoteWithInstrument(benchmark.getInstrumentdata(), benchmark.getQuotedata());
    }

    private String getDividend(STKStaticData staticData, String dividend) {
        final String currency = staticData.getDividendCurrency();
        final String day = staticData.getDividendExDay();
        return Renderer.PRICE.render(dividend)
                + (StringUtil.hasText(currency) ? (" " + currency) : "")
                + (StringUtil.hasText(day) ? (" (" + Formatter.LF.formatDate(day) + ")") : "");
    }

    private String getDividendYield(STKStaticData staticData,
                                    MSCPriceDataExtendedElement priceDataExtended) {
        final String dividendStr = staticData.getDividendLastYearInQuoteCurrency() != null
                ? staticData.getDividendLastYearInQuoteCurrency()
                : staticData.getDividendInQuoteCurrency();
        if (!StringUtil.hasText(dividendStr) || priceDataExtended.getPricedata() == null) {
            return "--";
        }
        final String priceStr = priceDataExtended.getPricedata().getPrice();
        if (!StringUtil.hasText(priceStr)) {
            return "--";
        }
        try {
            final double dividend = Double.parseDouble(dividendStr);
            final double price = Double.parseDouble(priceStr);
            return Renderer.PERCENT.render(Double.toString(dividend / price));
        } catch (Exception e) {
            Firebug.log("<getDividendYield> failed w/ " + e.getMessage() // $NON-NLS$
                    + " for " + dividendStr + " and " + priceStr); // $NON-NLS$
            return "--";
        }
    }


    public void setVisibleDate(String flipId, boolean visible) {
        this.twDates.setVisible(flipId, visible);
    }

    public void setLinkTextDate(int row, int column, String text, boolean asHtml) {
        this.twDates.setLinkText(row, column, text, asHtml);
    }

    public void setVisibleIndex(String flipId, boolean visible) {
        this.twIndizes.setVisible(flipId, visible);
    }

    public void setLinkTextIndex(int row, int column, String text, boolean asHtml) {
        this.twIndizes.setLinkText(row, column, text, asHtml);
    }
}
