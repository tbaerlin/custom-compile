/*
 * DependentValuesSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.BlockListType;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.CERFinder;
import de.marketmaker.iview.dmxml.CERFinderElement;
import de.marketmaker.iview.dmxml.FUTFinder;
import de.marketmaker.iview.dmxml.FUTFinderElement;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.dmxml.OPTFinder;
import de.marketmaker.iview.dmxml.OPTFinderElement;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.dmxml.Sort;
import de.marketmaker.iview.dmxml.WNTFinder;
import de.marketmaker.iview.dmxml.WNTFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderController;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderControllerRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormKeys;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderSection;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderCER;
import de.marketmaker.iview.mmgwt.mmweb.client.history.EmptyContext;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PageLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.OptUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Date;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.Selector.DZ_BANK_USER;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.CHANGE_PERCENT;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.COMPACT_DATETIME;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.DATE_RIGHT;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.LARGE_NUMBER;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PRICE;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.QUOTELINK_32;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@NonNLS
public class DependentValuesSnippet extends AbstractSnippet<DependentValuesSnippet, DependentValuesSnippetView>
        implements SymbolSnippet, PageLoader, LinkListener<String>, ValueChangeHandler<String> {

    /**
     * if the guidefs parameter for this snipped is {@value true}, the futures tab is shown in addition to the options tab, if the current user is
     * entitled for {@link Selector#FINDER_EUREX}.
     * The parameter defaults to {@value false}.
     */
    public static final String CONFIG_PARAM_SHOW_FUTURES_TAB = "showFuturesTab";

    private static final String[] VIEW_NAMES = new String[]{
            I18n.I.certificates(), I18n.I.warrants()
    };

    private static final String[] VIEW_NAMES_WITH_OPTIONS = new String[]{
            VIEW_NAMES[0], VIEW_NAMES[1], I18n.I.typeOptions()
    };

    private static final String[] VIEW_NAMES_WITH_OPTIONS_AND_FUTURES = new String[]{
            VIEW_NAMES_WITH_OPTIONS[0], VIEW_NAMES_WITH_OPTIONS[1], VIEW_NAMES_WITH_OPTIONS[2], I18n.I.typeFutures()
    };

    private static final String[] TYPE_NAMES = new String[]{"CER", "WNT", "OPT", "FUT"};

    private String issuerName;
    
    private final boolean withHistoryContext;

    private static final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowWkn());

    private static final VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowIsin());

    private static final DefaultTableColumnModel CER_COLUMN_MODEL = new DefaultTableColumnModel(new TableColumn[]{
            new TableColumn(I18n.I.type(), -1f, "certificateType")
            , new TableColumn("WKN", -1f, "wkn").withVisibilityCheck(showWknCheck)
            , new TableColumn("ISIN", -1f, "isin").withVisibilityCheck(showIsinCheck)
            , new TableColumn(I18n.I.name(), 220, QUOTELINK_32, "name")
            , new TableColumn(I18n.I.paid(), -1f, PRICE)
            , new TableColumn(I18n.I.bid(), -1f, PRICE)
            , new TableColumn(I18n.I.ask(), -1f, PRICE)
            , new TableColumn("+/-%", -1f, CHANGE_PERCENT).alignRight()
            , new TableColumn(I18n.I.volume(), -1f, LARGE_NUMBER, "turnover").alignRight()
            , new TableColumn(I18n.I.date(), -1f, COMPACT_DATETIME).alignRight()
            , new TableColumn(I18n.I.marketName(), -1f, "market1").alignRight()
    });

    private static final DefaultTableColumnModel FUT_COLUMN_MODEL = new DefaultTableColumnModel(new TableColumn[]{
            new TableColumn(I18n.I.name(), 220, QUOTELINK_32, "name")
            , new TableColumn(I18n.I.paid(), -1f, PRICE)
            , new TableColumn(I18n.I.bid(), -1f, PRICE)
            , new TableColumn(I18n.I.ask(), -1f, PRICE)
            , new TableColumn("+/-%", -1f, CHANGE_PERCENT).alignRight()
            , new TableColumn(I18n.I.volume(), -1f, LARGE_NUMBER, "turnover").alignRight()
            , new TableColumn(I18n.I.date(), -1f, COMPACT_DATETIME).alignRight()
            , new TableColumn(I18n.I.marketName(), -1f, "market1").alignRight()
            , new TableColumn(I18n.I.expirationPeriod(), -1f, DATE_RIGHT, "expirationDate")
    });

    private static final DefaultTableColumnModel OPT_COLUMN_MODEL = new DefaultTableColumnModel(new TableColumn[]{
            new TableColumn(I18n.I.name(), 220, QUOTELINK_32, "name")
            , new TableColumn(I18n.I.paid(), -1f, PRICE)
            , new TableColumn(I18n.I.bid(), -1f, PRICE)
            , new TableColumn(I18n.I.ask(), -1f, PRICE)
            , new TableColumn("+/-%", -1f, CHANGE_PERCENT).alignRight()
            , new TableColumn(I18n.I.volume(), -1f, LARGE_NUMBER, "turnover").alignRight()
            , new TableColumn(I18n.I.date(), -1f, COMPACT_DATETIME).alignRight()
            , new TableColumn(I18n.I.marketName(), -1f, "market1").alignRight()
            , new TableColumn(I18n.I.strike(), -1f, PRICE, "strike").alignRight()
            , new TableColumn(I18n.I.type(), -1f, "optionType").alignCenter()
            , new TableColumn(I18n.I.expirationPeriod(), -1f, DATE_RIGHT, "expirationDate")
    });

    private static final DefaultTableColumnModel WNT_COLUMN_MODEL = new DefaultTableColumnModel(new TableColumn[]{
            new TableColumn("WKN", -1f, "wkn").withVisibilityCheck(showWknCheck)
            , new TableColumn("ISIN", -1f, "isin").withVisibilityCheck(showIsinCheck)
            , new TableColumn(I18n.I.name(), 220, QUOTELINK_32, "name")
            , new TableColumn(I18n.I.paid(), -1f, PRICE)
            , new TableColumn(I18n.I.bid(), -1f, PRICE)
            , new TableColumn(I18n.I.ask(), -1f, PRICE)
            , new TableColumn("+/-%", -1f, CHANGE_PERCENT).alignRight()
            , new TableColumn(I18n.I.volume(), -1f, LARGE_NUMBER, "turnover").alignRight()
            , new TableColumn(I18n.I.date(), -1f, COMPACT_DATETIME).alignRight()
            , new TableColumn(I18n.I.marketName(), -1f, "market1").alignRight()
            , new TableColumn(I18n.I.strike(), -1f, PRICE, "strike").alignRight()
            , new TableColumn(I18n.I.expirationPeriod(), -1f, DATE_RIGHT, "expirationDate")
            , new TableColumn(I18n.I.type(), -1f, "warrantType").alignCenter()
    });

    public static class Class extends SnippetClass {
        public Class() {
            super("DependentValues", SessionData.isAsDesign() ? I18n.I.derivatives() : I18n.I.associates());
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new DependentValuesSnippet(context, config);
        }
    }

    public static final int DEFAULT_COUNT = 30;

    private final String[] viewNames;
    private final FinderResultHandler[] finderResultHandler;
    private InstrumentData instrumentData;
    private final MultiViewSupport multiViewSupport;
    private final DmxmlContext.Block<MSCPriceData> block;
    private PagingFeature pagingFeature;

    private DependentValuesSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        initIssuerName();

        this.finderResultHandler = new FinderResultHandler[2];

        if (Selector.FINDER_EUREX.isAllowed() && config.getBoolean(CONFIG_PARAM_SHOW_FUTURES_TAB, false)) {
            this.viewNames = VIEW_NAMES_WITH_OPTIONS_AND_FUTURES;
        }
        else if (Selector.FINDER_EUREX.isAllowed()) {
            this.viewNames = VIEW_NAMES_WITH_OPTIONS;
        }
        else {
            this.viewNames = VIEW_NAMES;
        }

        this.multiViewSupport = new MultiViewSupport(this.viewNames, null);
        this.multiViewSupport.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            public void onValueChange(ValueChangeEvent<Integer> e) {
                // view changed
                requestDependent();
            }
        });

        final DependentValuesSnippetView view = new DependentValuesSnippetView(this);
        setView(view);

        this.block = createBlock("MSC_PriceData");
        setSymbol(null, config.getString("symbol", null), null);

        final FloatingToolbar toolbar = getView().getToolbar();
//        toolbar.addSpacer();
        toolbar.add(new SeparatorToolItem());
//        toolbar.addSpacer();

        final PagingWidgets pagingWidgets = new PagingWidgets(getPagingConfig(toolbar));
        this.pagingFeature = new PagingFeature(this, (DmxmlContext.Block<BlockListType>)null, DEFAULT_COUNT);
        this.pagingFeature.setPagingWidgets(pagingWidgets);

        this.withHistoryContext = config.getBoolean("withHistoryContext", false);
    }

    private PagingWidgets.Config getPagingConfig(FloatingToolbar toolbar) {
        return new PagingWidgets.Config()
                .withToolbar(toolbar)
                .withAddSearchButton(new PagingWidgets.SearchCallback() {
                    public void onSearch() {
                        getFinderResultHandler().gotoFinder();
                    }
                });
    }

    private void initIssuerName() {
        this.issuerName = GuiDefsLoader.getIssuerName();
    }

    String getIssuerName() {
        return this.issuerName;
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        onQueryChanged();
    }

    void onQueryChanged() {
        // will be called during first render when instrumentData is still null, so check
        if (this.instrumentData != null) {
            getFinderResultHandler().onQueryChanged();
        }
    }

    MultiViewSupport getMultiViewSupport() {
        return multiViewSupport;
    }

    DefaultTableColumnModel getColumnModel() {
        return CER_COLUMN_MODEL;
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setEnabled(symbol != null);
        this.block.setParameter("symbol", symbol);
    }

    public void onClick(LinkContext<String> context, Element e) {
        getFinderResultHandler().sortListener.onClick(context, e);
    }

    private FinderResultHandler getFinderResultHandler() {
        final int viewId = this.multiViewSupport.getSelectedView();
        if (this.finderResultHandler[viewId] == null) {
            this.finderResultHandler[viewId] = createFinderResultHandler(viewId);
        }
        return this.finderResultHandler[viewId];
    }

    public void reload() {
        requestDependent();
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (this.block.isResponseOk()) {
            this.instrumentData = this.block.getResult().getInstrumentdata();
            requestDependent();
        }
        else {
            getView().update(DefaultTableDataModel.NULL);
        }
    }

    private FinderResultHandler createFinderResultHandler(int viewId) {
        return new FinderResultHandler(viewId);
    }

    private void requestDependent() {
        final DependentValuesSnippet.FinderResultHandler handler = getFinderResultHandler();
        final int viewId = this.multiViewSupport.getSelectedView();
        handler.issueRequest();
        getConfiguration().put("titleSuffix", this.viewNames[viewId]);
        getView().onViewChange(viewId);
    }

    class FinderResultHandler implements AsyncCallback<ResponseType> {
        final DmxmlContext context = new DmxmlContext();
        final DmxmlContext.Block<BlockListType> block;
        private final String type;
        private final LinkListener<String> sortListener;
        private final LiveFinderCER.LeverageConfig leverage;

        private int view;

        FinderResultHandler(int view) {
            this.view = view;
            this.type = TYPE_NAMES[view];
            final String blockKey = type + "_Finder";
            this.block = this.context.addBlock(blockKey);
            this.block.setParameter("offset", "0");
            this.block.setParameter("count", getConfiguration().getString("count", String.valueOf(DEFAULT_COUNT)));
            this.block.setParameter("sortBy", getConfiguration().getString("sortBy",
                    "OPT_Finder".equals(blockKey) ? "expirationDate" : "issueDate2"));
            this.block.setParameter("ascending", getConfiguration().getString("ascending", "false"));
            this.sortListener = new SortLinkSupport(this.block, new Command() {
                public void execute() {
                    refresh();
                }
            }, true);
            if (DZ_BANK_USER.isAllowed()) {
                this.leverage = LiveFinderCER.LeverageConfig.CER;
            } else {
                this.leverage = LiveFinderCER.LeverageConfig.BOTH;
            }
        }

        void refresh() {
            this.context.issueRequest(this);
        }

        void onQueryChanged() {
            this.block.setParameter("offset", "0");
            issueRequest();
        }

        private void issueRequest() {
            final String iid = instrumentData.getIid();
            final String instrumentId = iid.substring(0, iid.length() - 4);
            final StringBuilder query =
                    new StringBuilder("underlyingIid=='").append(instrumentId).append("'");
            final String subQuery = getView().getQuery(this.view);
            if (subQuery != null) {
                query.append(" && ").append(subQuery);
            }
            if ("CER".equals(type)) {
                switch (this.leverage) {
                    case CER:
                        query.append(" && dzIsLeverageProduct=='false'"); // $NON-NLS-0$
                        break;
                    case LEV:
                        query.append(" && dzIsLeverageProduct=='true'"); // $NON-NLS-0$
                        break;
                    default:
                        // nothing
                }
            }
            this.block.setParameter("query", query.toString());
            pagingFeature.setBlock(this.block);
            this.context.issueRequest(this);
        }

        public void onFailure(Throwable caught) {
            Firebug.error("DependentValueSnippet.FinderResultHandler.onFailure()", caught);
            updateView(null);
        }

        public void onSuccess(ResponseType result) {
            pagingFeature.onResult();
            if (this.block.isResponseOk()) {
                updateView(this.block.getResult());
            }
            else {
                updateView(null);
            }
        }

        void gotoFinder() {
            final FinderController controller = FinderControllerRegistry.get("L" + this.type);
            if (controller == null) {
                return;
            }
            final String iid = instrumentData.getIid();
            final String instrumentId = iid.substring(0, iid.length() - 4);
            final FinderFormConfig config = new FinderFormConfig("multifind", this.type);
            config.put(FinderFormKeys.VIEW, FinderFormKeys.DISPLAY_SETTINGS);
            FinderSection.enableUnderlyingSection(config, this.type);
            config.put(FinderFormKeys.UNDERLYING, "true");
            config.put(FinderFormKeys.UNDERLYING + "-symbol", instrumentId);
            config.put(FinderFormKeys.UNDERLYING + "-name", instrumentData.getName());
            FinderSection.enableBaseSection(config, this.type);
            config.put(FinderFormKeys.ISSUER_NAME + "-item", getIssuerName());
            config.put(FinderFormKeys.SORT, "true");
            config.put(FinderFormKeys.SORT + "-item", "Name");
            final String ascReq = this.block.getParameter("ascending");
            final String ascParam = "true".equals(ascReq) ? "true" : "false";
            config.put(FinderFormKeys.SORT + "-desc", ascParam);
            addExpiration(config);

            getView().addConfigTo(this.view, config);
            Firebug.log(config.toString());
            controller.prepareFind(config);
            PlaceUtil.goTo("M_LF_" + this.type);
        }

        private void addExpiration(FinderFormConfig config) {
            final Date expires = getView().getExpiresDate(this.view);
            if (expires == null) {
                return;
            }
            final String isoToday = Formatter.formatDateAsISODay(new Date());
            final String isoExpires = Formatter.formatDateAsISODay(expires);
            config.put(FinderFormKeys.REMAINING, "true");
            config.put(FinderFormKeys.REMAINING + "-from", isoToday);
            config.put(FinderFormKeys.REMAINING + "-to", isoExpires);
        }
    }

    private void updateView(BlockType result) {
        final TableColumnModel tcm;
        final TableDataModel tdm;
        if (result instanceof CERFinder) {
            final CERFinder finder = (CERFinder) result;
            tdm = updateCER(finder.getElement(), finder.getSort());
            tcm = CER_COLUMN_MODEL;
        }
        else if (result instanceof WNTFinder) {
            final WNTFinder wntFinder = (WNTFinder) result;
            tdm = updateWNT(wntFinder.getElement(), wntFinder.getSort());
            tcm = WNT_COLUMN_MODEL;
        }
        else if (result instanceof OPTFinder) {
            final OPTFinder optFinder = (OPTFinder) result;
            tdm = updateOPT(optFinder.getElement(), optFinder.getSort());
            tcm = OPT_COLUMN_MODEL;
        }
        else if (result instanceof FUTFinder) {
            final FUTFinder futFinder = (FUTFinder) result;
            tdm = updateFUT(futFinder.getElement(), futFinder.getSort());
            tcm = FUT_COLUMN_MODEL;
        }
        else {
            tdm = DefaultTableDataModel.NULL;
            tcm = CER_COLUMN_MODEL; // doesn't matter
        }
        getView().update(tcm, tdm);
    }

    private TableDataModel updateCER(List<CERFinderElement> elements, final Sort sort) {
        return DefaultTableDataModel.create(elements, new AbstractRowMapper<CERFinderElement>() {
            public Object[] mapRow(CERFinderElement e) {
                final InstrumentData instrumentData = e.getInstrumentdata();
                final QuoteData quoteData = e.getQuotedata();
                return new Object[]{
                        Renderer.CERTIFICATE_CATEGORY.render(e.getCertificateType()),
                        instrumentData.getWkn(),
                        instrumentData.getIsin(),
                        createQwi(instrumentData, quoteData),
                        e.getPrice(),
                        e.getBid(),
                        e.getAsk(),
                        e.getChangePercent(),
                        e.getVolume(),
                        e.getDate(),
                        quoteData.getMarketName()
                };
            }
        }).withSort(sort);
    }

    private QuoteWithInstrument createQwi(InstrumentData instrumentData, QuoteData quoteData) {
        final QuoteWithInstrument qwi = new QuoteWithInstrument(instrumentData, quoteData);
        if (this.withHistoryContext) {
            return qwi.withHistoryContext(
                    EmptyContext.create(DependentValuesSnippet.this.instrumentData.getName())
            );
        }
        return qwi;
    }

    private TableDataModel updateWNT(final List<WNTFinderElement> elements, final Sort sort) {
        return DefaultTableDataModel.create(elements, new AbstractRowMapper<WNTFinderElement>() {
            public Object[] mapRow(WNTFinderElement e) {
                final InstrumentData instrumentData = e.getInstrumentdata();
                final QuoteData quoteData = e.getQuotedata();
                return new Object[]{
                        instrumentData.getWkn(),
                        instrumentData.getIsin(),
                        createQwi(instrumentData, quoteData),
                        e.getPrice(),
                        e.getBid(),
                        e.getAsk(),
                        e.getChangePercent(),
                        e.getVolume(),
                        e.getDate(),
                        quoteData.getMarketName(),
                        e.getStrike(),
                        e.getExpirationDate(),
                        Renderer.WARRANT_TYPE.render(e.getWarrantType()),
                };
            }
        }).withSort(sort);
    }

    private TableDataModel updateOPT(final List<OPTFinderElement> elements, final Sort sort) {
        return DefaultTableDataModel.create(elements, new AbstractRowMapper<OPTFinderElement>() {
            public Object[] mapRow(OPTFinderElement e) {
                final InstrumentData instrumentData = e.getInstrumentdata();
                final QuoteData quoteData = e.getQuotedata();
                return new Object[]{
                        createQwi(instrumentData, quoteData),
                        e.getPrice(),
                        e.getBid(),
                        e.getAsk(),
                        e.getChangePercent(),
                        e.getVolume(),
                        e.getDate(),
                        quoteData.getMarketName(),
                        e.getStrike(),
                        e.getOptionType(),
                        e.getExpirationDate()
                };
            }

            @Override
            public String getRowClass(int row, OPTFinderElement e) {
                return OptUtil.getNearMoneyStyle(e);
            }
        }).withSort(sort);
    }

    private TableDataModel updateFUT(final List<FUTFinderElement> elements, final Sort sort) {
        return DefaultTableDataModel.create(elements, new AbstractRowMapper<FUTFinderElement>() {
            public Object[] mapRow(FUTFinderElement e) {
                final InstrumentData instrumentData = e.getInstrumentdata();
                final QuoteData quoteData = e.getQuotedata();
                return new Object[]{
                        new QuoteWithInstrument(instrumentData, quoteData),
                        e.getPrice(),
                        e.getBid(),
                        e.getAsk(),
                        e.getChangePercent(),
                        e.getVolume(),
                        e.getDate(),
                        quoteData.getMarketName(),
                        e.getExpirationDate()
                };
            }
        }).withSort(sort);
    }
}
