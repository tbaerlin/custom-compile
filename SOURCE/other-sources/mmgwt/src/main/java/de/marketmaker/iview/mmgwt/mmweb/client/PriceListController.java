/*
 * PriceListController.java
 *
 * Created on 16.07.2008 11:31:49
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.util.Format;
import com.google.gwt.user.client.Command;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PageLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ListDetailsHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.ErrorMessageUtil.getMessage;

/**
 * @author Ulrich Maurer
 */
public class PriceListController extends AbstractPageController implements PageLoader,
        PushRegisterHandler {
    public static final int DEFAULT_COUNT = 50;

    public static final String LIST_ID = "DZ"; // $NON-NLS-0$

    private ListDetailsHelper listDetailsHelper;

    private DmxmlContext.Block<MSCListDetails> blockIndizes;

    private DmxmlContext.Block<MSCListDetails> blockConstituents;

    private PagingFeature pagingFeature;

    private PriceListView view;

    private boolean reduceCurrencyName;

    private DefaultTableDataModel tdmIndizes;

    private DefaultTableDataModel tdmConstituents;

    private final PriceSupport priceSupport = new PriceSupport(this);

    protected PriceListController(ContentContainer contentContainer) {
        super(contentContainer);
        init();
    }

    private void init() {
        this.listDetailsHelper = new ListDetailsHelper(ListDetailsHelper.LinkType.NAME, true, true)
                .withWkn(true)
                .withBidAskVolume(true)
                .withDzBankLink(Permutation.GIS.isActive());

        this.blockIndizes = createDetailsBlock();
        this.blockIndizes.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.blockConstituents = createDetailsBlock();

        this.pagingFeature = new PagingFeature(this, this.blockConstituents, DEFAULT_COUNT);
    }

    private DmxmlContext.Block<MSCListDetails> createDetailsBlock() {
        final DmxmlContext.Block<MSCListDetails> result = this.context.addBlock("MSC_List_Details"); // $NON-NLS-0$
        result.setParameter("sortBy", "name"); // $NON-NLS-0$ $NON-NLS-1$
        result.setParameter("ascending", "true"); // $NON-NLS-0$ $NON-NLS-1$
        return result;
    }

    LinkListener<String> getSortLinkListener() {
        return new SortLinkSupport(this.blockConstituents, new Command() {
            public void execute() {
                reload();
            }
        });
    }


    public ListDetailsHelper getListDetailsHelper() {
        return listDetailsHelper;
    }

    public PagingFeature getPagingFeature() {
        return this.pagingFeature;
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String marketOverviewVariant = SessionData.INSTANCE.getGuiDefValue("market-overview-variant"); // $NON-NLS$
        final String marketOverviewSuffix = marketOverviewVariant == null ? "" : ("|" + marketOverviewVariant);
        final String param1 = historyToken.get(1, null);
        final String param2 = historyToken.get(2, null);
        this.reduceCurrencyName = param1.toLowerCase().startsWith("devisen") || "reduceCurrencyName".equals(param2); // $NON-NLS$
        this.blockIndizes.setParameter("listid", "listoverview|" + LIST_ID + "|elements|" + param1 + marketOverviewSuffix); // $NON-NLS$
        this.blockConstituents.setParameter("listid", "listoverview|" + LIST_ID + "|list|" + param1 + marketOverviewSuffix); // $NON-NLS$
        this.blockConstituents.setParameter("onlyEntitledQuotes", "true"); // $NON-NLS$
        this.pagingFeature.resetPaging();
        final String listName = historyToken.getAllParamCount() >= 3 ? param2 : param1;
        AbstractMainController.INSTANCE.getView().setContentHeader(AbstractMainController.INSTANCE.getContentHeader(historyToken.getControllerId())
                + " \"" + Format.htmlEncode(listName.replace(".", " - ")) + "\""); // $NON-NLS$
        reload();
    }

    public void reload() {
        refresh();
    }

    protected void onResult() {
        this.priceSupport.invalidateRenderItems();

        if (this.view == null) {
            // doing this in init() would cause problems with high load in some browsers (chrome),
            // so we go for lazy init
            this.view = new PriceListView(this);
        }
        this.pagingFeature.onResult();

        final TrendBarData tbd = createTrendBarData();
        this.tdmIndizes = createModel(this.blockIndizes, tbd);
        this.tdmConstituents = createModel(this.blockConstituents, tbd);
        this.view.show(this.tdmIndizes, this.tdmConstituents);
        this.priceSupport.activate();
    }

    private DefaultTableDataModel createModel(final DmxmlContext.Block<MSCListDetails> block,
                                              TrendBarData tbd) {
        if (!block.isResponseOk()) {
            return DefaultTableDataModel.create(getMessage(block.getError()));
        }
        final List<MSCListDetailElement> elements = block.getResult().getElement();
        final DefaultTableDataModel result = this.listDetailsHelper.createTableDataModel(elements.size()).withSort(block.getResult().getSort());
        int row = 0;
        for (final MSCListDetailElement e : elements) {
            if (this.reduceCurrencyName) {
                StringUtil.reduceCurrencyNameLength(e.getInstrumentdata());
            }
            final Price price = Price.create(e);
            this.listDetailsHelper.addRow(result, row, e.getInstrumentdata(), e.getQuotedata(),
                    tbd, price, ItemListContext.createForPortrait(e, elements, I18n.I.overview()));
            row++;
        }
        return result;
    }

    private TrendBarData createTrendBarData() {
        final List<MSCListDetailElement> elements = new ArrayList<MSCListDetailElement>();
        if (this.blockIndizes.isResponseOk()) {
            elements.addAll(this.blockIndizes.getResult().getElement());
        }
        if (this.blockConstituents.isResponseOk()) {
            elements.addAll(this.blockConstituents.getResult().getElement());
        }
        return TrendBarData.create(elements);
    }


    /**
     * Provide special print version of this page.
     *
     * @return the special print version.
     */
    @Override
    public String getPrintHtml() {
        return
                "<div class=\"mm-printHeader\">" + AbstractMainController.INSTANCE.getView().getContentHeader().asString() + "</div>" + // $NON-NLS-0$ $NON-NLS-1$
                        this.view.getPrintHtml() +
                        "<div class=\"mm-printFooter\">" + this.pagingFeature.getPageString() + "</div>"; // $NON-NLS-0$ $NON-NLS-1$
    }

    public String getListId() {
        if (this.blockConstituents.isResponseOk()) {
            return this.blockConstituents.getParameter("listid"); // $NON-NLS-0$
        }
        else {
            return null;
        }
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("listid", getListId()); // $NON-NLS-0$
        map.put("sortBy", this.blockConstituents.getParameter("sortBy")); // $NON-NLS-0$ $NON-NLS-1$
        map.put("ascending", this.blockConstituents.getParameter("ascending")); // $NON-NLS-0$ $NON-NLS-1$
        return new PdfOptionSpec("quotelist.pdf", map, "pdf_options_format"); // $NON-NLS-0$ $NON-NLS-1$
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        final ArrayList<PushRenderItem> list1 = addToEvent(event, this.blockIndizes);
        final ArrayList<PushRenderItem> list2 = addToEvent(event, this.blockConstituents);
        if (list1 == null) {
            return list2;
        }
        if (list2 != null) {
            list1.addAll(list2);
        }
        return list1;
    }

    private ArrayList<PushRenderItem> addToEvent(PushRegisterEvent event, DmxmlContext.Block<MSCListDetails> block) {
        if (block.isResponseOk()) {
            final int numAdded = event.addVwdcodes(block.getResult());
            if (numAdded == block.getResult().getElement().size()) {
                event.addComponentToReload(block, this);
            }
            if (numAdded > 0) {
                return this.view.getRenderItems(block == this.blockIndizes ?
                        this.tdmIndizes : this.tdmConstituents, block == this.blockIndizes);
            }
        }
        return null;
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (!event.isPushedUpdate()) {
            this.view.show(this.tdmIndizes, this.tdmConstituents);
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }
}
