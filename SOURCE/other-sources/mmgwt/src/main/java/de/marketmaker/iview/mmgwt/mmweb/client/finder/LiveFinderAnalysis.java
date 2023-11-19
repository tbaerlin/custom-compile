/*
 * LiveFinderAnalysis.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;

import de.marketmaker.iview.dmxml.BlockListType;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.dmxml.RSCAggregatedFinder;
import de.marketmaker.iview.dmxml.RSCAggregatedFinderElement;
import de.marketmaker.iview.dmxml.RSCAnalysis;
import de.marketmaker.iview.dmxml.RSCFinder;
import de.marketmaker.iview.dmxml.RSCFinderElement;
import de.marketmaker.iview.dmxml.RSCFinderMetadata;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SellHoldBuy;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LiveFinderAnalysis extends LiveFinder<RSCFinder, RSCFinderMetadata>
        implements LinkListener<RSCFinderElement> {

    private static final List<Item> P_ALL = Arrays.asList(ONE_WEEK, ONE_MONTH, THREE_MONTH, SIX_MONTHS, ONE_YEAR);

    public static final LiveFinderAnalysis INSTANCE = new LiveFinderAnalysis();
    private static final String BASE_ID = "anabase"; // $NON-NLS$
    private static final String INSTRUMENTNAME = "instrumentname"; // $NON-NLS$

    private ListBoxOption indexListBoxOption;

    protected final DmxmlContext.Block<RSCAggregatedFinder> aggBlock;

    protected final DmxmlContext.Block<RSCAnalysis> analysisBlock;

    private final List<FinderFormElements.Item> aggSortFields = new ArrayList<>();
    private final List<FinderFormElements.Item> nonAggSortFields = new ArrayList<>();

    private DmxmlContext analysisContext;

    private LiveFinderAnalysis() {
        super("RSC_Finder"); // $NON-NLS-0$

        this.aggBlock = this.context.addBlock("RSC_Finder"); // $NON-NLS-0$
        this.aggBlock.setParameter("resultType", "aggregated"); // $NON-NLS-0$ $NON-NLS-1$
        this.aggBlock.disable();

        this.defaultSortField = "date"; // $NON-NLS-0$
        this.defaultSortDescending = true;

        this.analysisContext = new DmxmlContext();
        this.analysisBlock = analysisContext.addBlock("RSC_Analysis"); // $NON-NLS-0$
    }

    protected ViewSpec[] getResultViewSpec() {
        return new ViewSpec[]{new ViewSpec(I18n.I.result())};
    }

    public String getId() {
        return "LRSC"; // $NON-NLS-0$
    }

    public String getViewGroup() {
        return "finder-rsc"; // $NON-NLS-0$
    }

    protected void prepareBlock() {
        super.prepareBlock();
        this.aggBlock.setParameter("offset", "0"); // $NON-NLS-0$ $NON-NLS-1$
        this.aggBlock.setParameter("count", this.pagingFeature.getPageSize()); // $NON-NLS-0$
    }

    protected DmxmlContext.Block<? extends BlockListType> getBlockForQuery() {
        this.aggBlock.setEnabled(isForIndex());
        return this.aggBlock.isEnabled() ? this.aggBlock : this.block;
    }

    public void prepareFind(String field1, String value1, String field2, String value2) {
    }

    protected void addSections() {
        addSectionMain();
    }

    protected TableDataModel createDataModel(int view) {
        return isForIndex() ? createIndexModel() : createDefaultModel();
    }

    boolean isForIndex() {
        return this.indexListBoxOption.getValue();
    }

    protected AbstractFinderView createView() {
        return new FinderAnalysisView<>(this);
    }

    @Override
    protected String getQuery() {
        handleSorting();
        return super.getQuery();
    }

    @Override
    protected boolean allowEmptyQuery() {
        return true;
    }

    private void handleSorting() {
        final ListBox lb = this.orderByOption.lb;
        final String selected = lb.getItemText(lb.getSelectedIndex());
        lb.clear();
        if (isForIndex()) {
            for (Item aggSortField : aggSortFields) {
                lb.addItem(aggSortField.getItem(), aggSortField.getValue());
            }
        } else {
            for (Item nonAggSortField : nonAggSortFields) {
                lb.addItem(nonAggSortField.getItem(), nonAggSortField.getValue());
            }
        }
        if (StringUtil.hasText(selected)) {
            for (int i = 0; i < lb.getItemCount(); i++) {
                if (selected.equals(lb.getItemText(i))) {
                    lb.setItemSelected(i, true);
                }
            }
        }
    }

    protected Map<String, FinderMetaList> getMetaLists() {
        final RSCFinderMetadata result = this.metaBlock.getResult();
        final HashMap<String, FinderMetaList> map = new HashMap<>();
        map.put("analyst", result.getAnalyst()); // $NON-NLS-0$
        map.put("index", result.getIndex()); // $NON-NLS-0$
        map.put("recommendation", result.getRecommendation()); // $NON-NLS-0$
        map.put("sector", result.getSector()); // $NON-NLS-0$
        return map;
    }

    private void addSectionMain() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<>();
                final String[] sectionConf = SectionConfigUtil.getSectionConf(BASE_ID);
                int defaultOrder = 0;

                final Item item = new Item(I18n.I.name(), INSTRUMENTNAME);
                addToSortField(item, true);
                elements.add(new LiveTextOption("symbol", I18n.I.symbolTypes(), searchHandler).withConf(sectionConf, defaultOrder++));  // $NON-NLS-0$
                elements.add(new LiveStartEndOption("date", "date", I18n.I.period(), "", P_ALL, DateTimeUtil.PeriodMode.PAST, // $NON-NLS$
                        searchHandler).withConf(sectionConf, defaultOrder++));
                addToSortField(new Item(I18n.I.date(), "date"), false);  // $NON-NLS-0$
                final String analyst = "analyst"; // $NON-NLS$
                elements.add(createLiveMultiEnum(analyst, I18n.I.analystsCompany(), null, getMetaLists(),
                        analyst, searchHandler).withStyle("width160").withConf(sectionConf, defaultOrder++));  // $NON-NLS$
                addToSortField(new FinderFormElements.Item(handleLabelExceptions(I18n.I.analystsCompany()), analyst), false);
                indexListBoxOption = createLiveListBoxOption("index", I18n.I.index(), null, "106547.qid", // $NON-NLS$
                        searchHandler);
                elements.add(indexListBoxOption.withStyle("width160").withConf(sectionConf, defaultOrder++)); // $NON-NLS-0$
                final String sector = "sector"; // $NON-NLS$
                elements.add(createLiveMultiEnum(sector, I18n.I.sector(), null, getMetaLists(), sector, searchHandler)
                        .withStyle("width160").withConf(sectionConf, defaultOrder++)); // $NON-NLS$
                final String recommendation = "recommendation"; // $NON-NLS$
                elements.add(createLiveMultiEnum(recommendation, I18n.I.estimation(), null, getMetaLists(),
                        recommendation, searchHandler).withStyle("width160").withConf(sectionConf, defaultOrder)); // $NON-NLS$
                addToSortField(new FinderFormElements.Item(handleLabelExceptions(I18n.I.estimation()), recommendation), false);

                elements.addAll(handleClones(sectionConf, elements));
                return orderBySectionConf(elements, sectionConf);
            }
        };
        addSection(BASE_ID, I18n.I.simple(), false, elementsLoader, searchHandler).withConfigurable().loadElements();
    }

    private void addToSortField(Item item, boolean forIndexOnly) {
        sortFields.add(item);
        if (forIndexOnly) {
            this.aggSortFields.add(item);
        } else {
            this.nonAggSortFields.add(item);
        }
    }

    private TableDataModel createIndexModel() {
        final String index = this.indexListBoxOption.getSelectedItemText();
        final List<SellHoldBuy> list = new ArrayList<>();
        final DefaultTableDataModel model = createModel(this.aggBlock.getResult().getElement(), new AbstractRowMapper<RSCAggregatedFinderElement>() {
            public Object[] mapRow(RSCAggregatedFinderElement e) {
                final SellHoldBuy shb = new SellHoldBuy(e);
                list.add(shb);
                return new Object[]{
                        createQuoteWithInstrument(e),
                        shb,
                        shb,
                        index,
                        e.getSector()
                };
            }
        });
        int numMaxAll = -1;
        for (SellHoldBuy shb : list) {
            if (numMaxAll < shb.getAll()) {
                numMaxAll = shb.getAll();
            }
        }
        for (SellHoldBuy shb : list) {
            shb.setMaxAll(numMaxAll);
        }
        return model;
    }

    private TableDataModel createDefaultModel() {
        return createModel(getResult().getElement(), new AbstractRowMapper<RSCFinderElement>() {
            public Object[] mapRow(RSCFinderElement e) {
                return new Object[]{
                        e.getDate(),
                        new LinkContext<>(LiveFinderAnalysis.this, e),
                        createQuoteWithInstrument(e),
                        e.getRecommendation(),
                        e.getAnalyst()
                };
            }
        });
    }

    private QuoteWithInstrument createQuoteWithInstrument(RSCFinderElement e) {
        return QuoteWithInstrument.createQuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata(), e.getCompanyName());
    }

    private QuoteWithInstrument createQuoteWithInstrument(RSCAggregatedFinderElement e) {
        return QuoteWithInstrument.createQuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata(), null);
    }

    public void onClick(LinkContext<RSCFinderElement> context, Element e) {
        this.analysisBlock.setParameter("analysisid", context.data.getAnalysisid()); // $NON-NLS-0$
        this.analysisContext.issueRequest(new AsyncCallback<ResponseType>() {
            public void onFailure(Throwable throwable) {
                showAnalysis();
            }

            public void onSuccess(ResponseType responseType) {
                showAnalysis();
            }
        });
    }

    private void showAnalysis() {
        final RSCAnalysis analysis = this.analysisBlock.isResponseOk()
                ? this.analysisBlock.getResult() : null;
        ((FinderAnalysisView) this.view).showAnalysis(analysis);
    }

    @Override
    protected List<FinderTypedMetaList> getLiveMetadata() {
        return Collections.emptyList();
    }
}
