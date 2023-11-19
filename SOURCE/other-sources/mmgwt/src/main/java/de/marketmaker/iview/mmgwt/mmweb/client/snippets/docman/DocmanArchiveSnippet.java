package de.marketmaker.iview.mmgwt.mmweb.client.snippets.docman;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import de.marketmaker.itools.gwtutil.client.util.date.JsDate;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.Separator;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DateBox;
import de.marketmaker.iview.dmxml.*;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PageLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.*;
import de.marketmaker.iview.mmgwt.mmweb.client.table.*;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.DEFAULT;


public class DocmanArchiveSnippet
        extends AbstractSnippet<DocmanArchiveSnippet, SnippetTableView<DocmanArchiveSnippet>>
        implements PageLoader, SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("DocmanArchive"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new DocmanArchiveSnippet(context, config);
        }
    }

    public static final int DEFAULT_COUNT = 10;

    private final DmxmlContext.Block<MSCPriceDatas> priceDatas;

    private final DmxmlContext.Block<DOCArchiveSearch> docArchiveSearch;

    private final DmxmlContext.Block<DOCURL> docUrl;


    private final PagingFeature pagingFeature;

    private final DmxmlContext docmanCtx;

    private static final Map<String, String> MAP_NAMES;
    static {
        MAP_NAMES = new HashMap<String, String>(14);
        MAP_NAMES.put("Monthly", "Monatsbericht");  // $NON-NLS$
        MAP_NAMES.put("SemiAnnual", I18n.I.fndReportSemiannualReport());  // $NON-NLS$
        MAP_NAMES.put("Annual", I18n.I.fndReportAnnualReport());   // $NON-NLS$
        MAP_NAMES.put("Accounts", "Rechenschaftsbericht");  // $NON-NLS$
        MAP_NAMES.put("Prospectus", I18n.I.fndReportProspectus());  // $NON-NLS$
        MAP_NAMES.put("ProspectusSimplified", I18n.I.fndReportSimplifiedProspectus());   // $NON-NLS$
        MAP_NAMES.put("ProspectusUnfinished", "Verkaufsprospekt unfertig");  // $NON-NLS$
        MAP_NAMES.put("FactSheet", "Monatsreport");  // $NON-NLS$
        MAP_NAMES.put("TermSheet", "term.sheet");  // $NON-NLS$
        MAP_NAMES.put("SpectSheet", "spect.sheet");  // $NON-NLS$
        MAP_NAMES.put("Addendum", "addendum");  // $NON-NLS$
        MAP_NAMES.put("KIID", "Key Investor Information Document");  // $NON-NLS$
        MAP_NAMES.put("PIB", "Produktinformationsblatt");  // $NON-NLS$
        MAP_NAMES.put("Unknown", "Unbekannt"); // $NON-NLS$
    }


    private DocmanArchiveSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        config.put("title", I18n.I.docmanArchive());  // $NON-NLS$

        priceDatas = context.addBlock("MSC_PriceDatas");  // $NON-NLS$
        priceDatas.setEnabled(true);

        docmanCtx = new DmxmlContext();
        docArchiveSearch = docmanCtx.addBlock("DOC_ArchiveSearch"); // $NON-NLS-0$
        docArchiveSearch.setParameter("offset", 0);  // $NON-NLS$
        docArchiveSearch.setParameter("absoluteUrl", "true");  // $NON-NLS$
        docArchiveSearch.setParameter("count", DEFAULT_COUNT);  // $NON-NLS$
        docArchiveSearch.setParameter("start", JsDateFormatter.formatIsoDay(new JsDate()));  // $NON-NLS$
        docArchiveSearch.setParameter("userInfo", "p/olb/PIB-DEMO"); //  // $NON-NLS$ FIXME
        docArchiveSearch.setEnabled(false);

        docUrl = docmanCtx.addBlock("DOC_URL"); // $NON-NLS-0$
        docUrl.setEnabled(false);

        pagingFeature = new PagingFeature(this, new PagerAdaptor(), DEFAULT_COUNT);
        setupView();
    }

    private void setupView() {
        setView(new SnippetTableView<DocmanArchiveSnippet>(this, createColumnModel()));

        final PagingWidgets pagingWidgets = new PagingWidgets(new PagingWidgets.Config());
        this.pagingFeature.setPagingWidgets(pagingWidgets);

        FloatingToolbar toolbar = pagingWidgets.getToolbar();
        final DateBox dateBox = new DateBox();
        dateBox.addValueChangeHandler(new ValueChangeHandler<MmJsDate>() {
            @Override
            public void onValueChange(ValueChangeEvent<MmJsDate> event) {
                docArchiveSearch.setParameter("start", JsDateFormatter.formatIsoDay(event.getValue()));  // $NON-NLS$
                reload();
            }
        });
        toolbar.addFill();
        toolbar.addSeparator();
        toolbar.add(dateBox);
        getView().setTopComponent(toolbar);
    }

    @Override
    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        priceDatas.setParameter("symbol", symbol); // $NON-NLS-0$
        priceDatas.setEnabled(symbol != null);
        context.issueRequest(new ResponseTypeCallback(){
            @Override
            protected void onResult() {
                if (priceDatas.isEnabled() && priceDatas.isResponseOk()) {
                    String isin = priceDatas.getResult().getInstrumentdata().getIsin();
                    getConfiguration().put("isin", isin);  // $NON-NLS$
                    docArchiveSearch.setEnabled(true);
                    onParametersChanged();
                }
            }
        });
    }

    @Override
    protected void onParametersChanged() {
        String isin = getConfiguration().getString("isin");  // $NON-NLS-0$
        if (StringUtil.hasText(isin)) {
            docArchiveSearch.setParameter("symbol", isin);  // $NON-NLS$
            reload();
        }
    }

    @Override // PageLoader interface, called from the paging widget
    public void reload() {
        docmanCtx.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                pagingFeature.onResult();
                updateView();
            }
        });
    }

    @Override
    public void updateView() {
        getView().update(createModelFromBlock());
    }

    @Override
    public void destroy() {
        docmanCtx.removeBlock(docArchiveSearch);
        context.removeBlock(priceDatas);
    }

    public void archiveChanged() {
        docArchiveSearch.setToBeRequested();
        reload();
    }

    private DefaultTableDataModel createModelFromBlock() {
        if (!this.docArchiveSearch.isResponseOk()) {
            return DefaultTableDataModel.NULL;
        }

        final DOCArchiveSearch result = docArchiveSearch.getResult();
        final List<DocArchiveItem> items = result.getItem();
        if (items.size() == 0) {
            return DefaultTableDataModel.NULL;
        }

        final List<RowData> rows = new ArrayList<RowData>(items.size());
        for (DocArchiveItem item : items) {
            rows.add(new RowData(item.getDate(), item, item.getId()));
        }

        return DefaultTableDataModel.createWithRowData(rows);
    }

    private DefaultTableColumnModel createColumnModel() {
        final List<TableColumn> columns = new ArrayList<TableColumn>();
        columns.add(new TableColumn(I18n.I.timeDate(), 130, TableCellRenderers.DATE_AND_TIME).alignLeft());
        columns.add(new TableColumn("Type", -1f, new DocLinkRenderer()));  // $NON-NLS$
        columns.add(new TableColumn("ID", 70, DEFAULT));  // $NON-NLS$
        return new DefaultTableColumnModel(columns.toArray(new TableColumn[columns.size()]));
    }

    private class DocLinkRenderer extends TableCellRendererAdapter {
        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            DocArchiveItem item = (DocArchiveItem) data;
            sb.append("<a href=\"").append(item.getRequest()).append("\""); // $NON-NLS$
            sb.append(" target=\"_blank\">");  // $NON-NLS$
            sb.append(MAP_NAMES.get(item.getType()));
            sb.append("</a>");  // $NON-NLS-0$
        }
    }

    public class PagerAdaptor implements PagingFeature.Pager {

        @Override
        public void setOffset(int offset) {
            docArchiveSearch.setParameter("offset", offset);  // $NON-NLS$
        }

        @Override
        public void setCount(int count) {
            docArchiveSearch.setParameter("count", count);  // $NON-NLS$
        }

        @Override
        public int getTotal() {
            if (isResponseOk() && docArchiveSearch.isEnabled()) {
                return Integer.valueOf(docArchiveSearch.getResult().getTotal());
            }
            return 0;
        }

        @Override
        public int getOffset() {
            if (isResponseOk() && docArchiveSearch.isEnabled()) {
                return Integer.valueOf(docArchiveSearch.getResult().getOffset());
            }
            return 0;
        }

        @Override
        public int getCount() {
            if (isResponseOk() && docArchiveSearch.isEnabled()) {
                return Integer.valueOf(docArchiveSearch.getResult().getCount());
            }
            return 0;
        }

        @Override
        public boolean isResponseOk() {
            return docArchiveSearch.isResponseOk();
        }

    }
}
