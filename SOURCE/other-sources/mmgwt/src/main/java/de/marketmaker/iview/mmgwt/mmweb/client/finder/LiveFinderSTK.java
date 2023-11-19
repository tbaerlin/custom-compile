/*
 * LiveFinderSTK.java
 *
 * Created on 10.09.2011 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.ListBox;

import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.dmxml.INDFinder;
import de.marketmaker.iview.dmxml.INDFinderElement;
import de.marketmaker.iview.dmxml.STKFinder;
import de.marketmaker.iview.dmxml.STKFinderElement;
import de.marketmaker.iview.dmxml.STKFinderMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.Ginjector;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.logging.Logger;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.*;

/**
 * @author Michael Lösch
 */
public class LiveFinderSTK extends LiveFinder<STKFinder, STKFinderMetadata> {

    public static final LiveFinderSTK INSTANCE = new LiveFinderSTK();

    public static final String YEAR1;

    public static final String YEAR2;

    static {
        final MmJsDate d = new MmJsDate();
        YEAR1 = Integer.toString(d.getFullYear());
        YEAR2 = Integer.toString(d.getFullYear() + 1);
    }

    private static final List<Item> P_ALL
            = Arrays.asList(ONE_WEEK, ONE_MONTH, THREE_MONTH, SIX_MONTHS, ONE_YEAR, THREE_YEARS, FIVE_YEARS, TEN_YEARS);

    private static final String BASE_ID = "stkbase"; // $NON-NLS$

    private static final String EXTENDED_ID = "stkextended"; // $NON-NLS$

    private static final String RATIOS_ID = "stkratios"; // $NON-NLS$

    private static final String FIXED_TOP_ID = "stkfixedtop"; // $NON-NLS$

    private static final String INDEX_PREFIX = "index=="; // $NON-NLS$

    private static final List<Item> YEARS = Arrays.asList(new Item(YEAR1, "1y"), new Item(YEAR2, "2y")); // $NON-NLS$

    private final Logger logger = Ginjector.INSTANCE.getLogger();

    private LiveMultiEnumOption indexOption;

    private final DmxmlContext.Block<INDFinder> indexBlock;

    private LiveFinderSTK() {
        super("STK_Finder"); // $NON-NLS$
        this.indexBlock = this.context.addBlock("IND_Finder"); // $NON-NLS$
        this.sortFields.add(new Item(I18n.I.name(), "name"));  // $NON-NLS$
    }

    protected ViewSpec[] getResultViewSpec() {
        return new ViewSpec[]{
                new ViewSpec(I18n.I.basis()),
                new ViewSpec(I18n.I.performance()),
                new ViewSpec(I18n.I.risk()),
                new ViewSpec(I18n.I.basicData()),
                new ViewSpec(I18n.I.benchmarkCorrelation())
        };
    }

    public String getId() {
        return "LSTK"; // $NON-NLS$
    }

    public String getViewGroup() {
        return "finder-stk"; // $NON-NLS$
    }

    public void prepareFind(String field1, String value1, String field2, String value2) {
    }

    protected FinderFormConfig createFormConfig(HistoryToken historyToken) {
        final String param1 = historyToken.getFromAll(1, null);
        if (param1 == null || !param1.startsWith(INDEX_PREFIX)) {
            return null;
        }
        FinderFormConfig ffc = new FinderFormConfig(param1, getId());
        ffc.put(BASE_ID, "true"); // $NON-NLS$
        ffc.put("index", "true"); // $NON-NLS$
        ffc.put("index-item", param1.substring(INDEX_PREFIX.length())); // $NON-NLS$
        ffc.put(FinderFormKeys.SORT, "true"); // $NON-NLS$
        ffc.put(FinderFormKeys.SORT + "-item", "name"); // $NON-NLS$
        ffc.put("exchange", "true"); // $NON-NLS$
        ffc.put("exchange-Deutschland", "true"); // $NON-NLS$
        return ffc;
    }

    protected void addSections() {
        addFixedTopSection();
        addSectionBase();
        if (Selector.DZBANK_WEB_INVESTOR.isAllowed()) {
            addSectionExtended();
        }
        addSectionRatios();
    }


    protected TableDataModel createDataModel(int view) {
        switch (view) {
            case 0:
                return createBaseModel();
            case 1:
                return createPerfModel();
            case 2:
                return createRiskModel();
            case 3:
                return createFundamentalModel();
            case 4:
                return createBenchmarkModel();
            default:
                return null;
        }
    }

    protected AbstractFinderView createView() {
        return new FinderSTKView<>(this);
    }

    @Override
    protected List<FinderTypedMetaList> getLiveMetadata() {
        return this.block.getResult().getMetadata();
    }

    public Map<String, FinderMetaList> getMetaLists() {
        return getMetaLists(this.metaBlock.getResult());
    }

    private static Map<String, FinderMetaList> getMetaLists(STKFinderMetadata result) {
        final Map<String, FinderMetaList> map = new HashMap<>();
        map.put("country", result.getCountry()); // $NON-NLS$
        map.put("index", result.getIndex()); // $NON-NLS$
        map.put("sector", result.getSector()); // $NON-NLS$
        map.put("marketgroups", result.getMarkets()); // $NON-NLS$
        map.put(FinderFormKeys.GICS_SECTOR_KEY, result.getGicsSectorKey());
        map.put(FinderFormKeys.GICS_INDUSTRY_GROUP_KEY, result.getGicsIndustryGroupKey());
        map.put(FinderFormKeys.GICS_INDUSTRY_KEY, result.getGicsIndustryKey());
        map.put(FinderFormKeys.GICS_SUB_INDUSTRY_KEY, result.getGicsSubIndustryKey());
        return map;
    }

    private void addFixedTopSection() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();

            if (FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled()) {
                AbstractOption hideNotActive = new LiveBooleanOption("notActive", I18n.I.hideInactiveInstruments(), true, searchHandler);  // $NON-NLS$;
                hideNotActive.setValue(true);
                elements.add(hideNotActive);
            }

            return elements;
        };

        final FinderSection finderSection = addSection(FIXED_TOP_ID,
                "fixedTopSection", true, elementsLoader, searchHandler).loadElements(); // $NON-NLS$
        finderSection.setAlwaysExpanded();
    }

    private void addSectionBase() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(BASE_ID);
            int defaultOrder = 0;
            final Map<String, FinderMetaList> metalists = getMetaLists();

            final String country = "country"; // $NON-NLS$
            final LiveMultiEnumOption countryCheckBox = createSortableLiveMultiEnum(country, I18n.I.country(), null,
                    metalists, country, new DynamicSearchHandler());
            elements.add(countryCheckBox.withConf(sectionConf, defaultOrder++));

            final String idx = "index"; // $NON-NLS$
            indexOption = createSortableLiveMultiEnum(idx, I18n.I.index(), null, metalists,
                    idx, new DynamicSearchHandler()).withDivider('@');
            elements.add(indexOption.withoutMetadataUpdate().withStyle(DEFAULT_WIDTH)
                    .withConf(sectionConf, defaultOrder++));

            addMarkets(elements, sectionConf, defaultOrder);

            if (Customer.INSTANCE.isVwd()) {
                elements.add(createGicsFinderForm(FinderFormKeys.GICS_SECTOR_KEY, I18n.I.gicsSector(), metalists, sectionConf));
                elements.add(createGicsFinderForm(FinderFormKeys.GICS_INDUSTRY_GROUP_KEY, I18n.I.gicsIndustryGroup(), metalists, sectionConf));
                elements.add(createGicsFinderForm(FinderFormKeys.GICS_INDUSTRY_KEY, I18n.I.gicsIndustry(), metalists, sectionConf));
                elements.add(createGicsFinderForm(FinderFormKeys.GICS_SUB_INDUSTRY_KEY, I18n.I.gicsSubIndustry(), metalists, sectionConf));
            }
            else {
                elements.add(createSectorFinderForm("sector", I18n.I.sector(), metalists, sectionConf)); // $NON-NLS-0$
            }

            final String marketCapitalization = "marketCapitalization"; // $NON-NLS$
            final Item marketCapitalItem = new Item(I18n.I.marketCapitalization(), marketCapitalization);
            final LiveFromToTextOption marketCapital = createLiveFromToInMio(marketCapitalization,
                    marketCapitalization, I18n.I.marketCapitalization(), searchHandler);
            sortFields.add(marketCapitalItem);
            elements.add(marketCapital.withConf(sectionConf));

            if (FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled()) {
                final String marketCapitalizationEUR = "marketCapitalizationEUR"; // $NON-NLS$
                final Item marketCapitalItemEUR = new Item(I18n.I.marketCapitalizationEUR(), marketCapitalizationEUR);
                final LiveFromToTextOption marketCapitalEUR = createLiveFromToInMio(marketCapitalizationEUR,
                        marketCapitalizationEUR, I18n.I.marketCapitalizationEUR(), searchHandler);
                sortFields.add(marketCapitalItemEUR);
                elements.add(marketCapitalEUR.withConf(sectionConf));

                final String marketCapitalizationUSD = "marketCapitalizationUSD"; // $NON-NLS$
                final Item marketCapitalItemUSD = new Item(I18n.I.marketCapitalizationUSD(), marketCapitalizationUSD);
                final LiveFromToTextOption marketCapitalUSD = createLiveFromToInMio(marketCapitalizationUSD,
                        marketCapitalizationUSD, I18n.I.marketCapitalizationUSD(), searchHandler);
                sortFields.add(marketCapitalItemUSD);
                elements.add(marketCapitalUSD.withConf(sectionConf));
            }

            elements.add(createLivePeriodFromToTextOption("dividendyield", "dividendYield", I18n.I.dividendYield(), "%", YEARS, // $NON-NLS$
                    searchHandler).withConf(sectionConf));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        final FinderSection baseSection = addSection(BASE_ID, I18n.I.baseInfo(), false, elementsLoader, searchHandler)
                .expand().withConfigurable().loadElements();
        if (FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled()) {
            baseSection.setValue(true);
        }
    }

    private FinderFormElement createGicsFinderForm(String field, String label, Map<String, FinderMetaList> metaListMap, String[] sectionConf) {
        final Map<String, String> gicsKeyNameMap = new HashMap<>();
        for (FinderMetaList.Element e : metaListMap.get(field).getElement()) {
            gicsKeyNameMap.put(e.getKey(), e.getName());
        }

        final LiveListBoxOption result = new LiveListBoxOption(field, field, label, null, null, new DynamicSearchHandler()) {

            @Override
            public void updateMetadata(Map<String, FinderMetaList> map, boolean force) {
                if (getValue() && !force) {
                    return;
                }
                final String selected = selectedValue(this.lb);
                this.lb.clear();

                fillTypeListBox(this.lb, map.get(this.field), gicsKeyNameMap, selected);
                this.cb.setEnabled(this.lb.getItemCount() > 0);
            }
        };
        return result.withStyle(DEFAULT_WIDTH).withConf(sectionConf);
    }

    private static void fillTypeListBox(ListBox lb, FinderMetaList metaListUpdate, Map<String, String> gicsKeyNameMap, String selected) {
        if (metaListUpdate == null || metaListUpdate.getElement() == null || metaListUpdate.getElement().isEmpty()) {
            return;
        }

        final Map<String, String> nameKeyMap = new LinkedHashMap<>();

        for (FinderMetaList.Element element : metaListUpdate.getElement()) {
            if (StringUtil.hasText(element.getName())) {
                final String itemName = StringUtil.hasText(element.getCount())
                        ? (gicsKeyNameMap.get(element.getKey()) + " (" + element.getCount() + ")")
                        : element.getName();
                nameKeyMap.put(itemName, element.getKey());
            }
        }

        LiveFinderCER.addItems(nameKeyMap, selected, lb);
    }

    private FinderFormElement createSectorFinderForm(String field, String label, Map<String, FinderMetaList> metalists, String[] sectionConf) {
        return createSortableLiveMultiEnum(field, label, null, metalists, field,
                new DynamicSearchHandler()).withStyle(DEFAULT_WIDTH).withConf(sectionConf);
    }

    private void addSectionExtended() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(EXTENDED_ID);
            int defaultOrder = 0;

            elements.add(createLivePeriodFromToTextOption("priceEarningRatio", "priceEarningRatio", I18n.I.priceEarningRatioAbbr(), "", // $NON-NLS$
                    YEARS, searchHandler).withConf(sectionConf, defaultOrder++));
            elements.add(createLivePeriodFromToTextOption("priceSalesRatio", "priceSalesRatio", I18n.I.priceSalesRatioAbbr(), // $NON-NLS$
                    YEARS, searchHandler).withConf(sectionConf, defaultOrder++));
            elements.add(createLivePeriodFromToTextOption("priceCashflowRatio", "priceCashflowRatio", I18n.I.priceCashflowRatioAbbr(), // $NON-NLS$
                    YEARS, searchHandler).withConf(sectionConf, defaultOrder));
            elements.add(createLivePeriodFromToTextOption("sales", "sales", I18n.I.volumeTrade(), I18n.I.millionAbbr(), YEARS, searchHandler) // $NON-NLS$
                    .withConf(sectionConf));
            elements.add(createLivePeriodFromToTextOption("profit", "profit", I18n.I.profit(), I18n.I.millionAbbr(), YEARS, searchHandler) // $NON-NLS$
                    .withConf(sectionConf));
            elements.add(createLivePeriodFromToTextOption("ebit", "ebit", I18n.I.ebit(), I18n.I.millionAbbr(), YEARS, searchHandler) // $NON-NLS$
                    .withConf(sectionConf));
            elements.add(createLivePeriodFromToTextOption("ebitda", "ebitda", I18n.I.ebitda(), I18n.I.millionAbbr(), YEARS, searchHandler) // $NON-NLS$
                    .withConf(sectionConf));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        addSection(EXTENDED_ID, I18n.I.extendedInfo(), false, elementsLoader,
                this.searchHandler).withConfigurable().loadElements();
    }

    private void addSectionRatios() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(RATIOS_ID);
            int defaultOrder = 0;

            elements.add(createLivePeriodFromToTextOption("volatility", "volatility", I18n.I.volatility(), "%", // $NON-NLS$
                    P_ALL, searchHandler).withConf(sectionConf, defaultOrder++));
            elements.add(createLivePeriodFromToTextOption("performance", "performance", I18n.I.performance(), "%", P_ALL, // $NON-NLS$
                    searchHandler).withConf(sectionConf, defaultOrder++));
            elements.add(createLivePeriodFromToTextOption("correlation", "correlation", I18n.I.correlation(), P_ALL, // $NON-NLS$
                    searchHandler).withConf(sectionConf, defaultOrder));
            elements.add(createLivePeriodFromToTextOptionInMio("averageVolume", "averageVolume", I18n.I.averageVolume(),  // $NON-NLS$
                    Arrays.asList(ONE_WEEK, THREE_MONTH, ONE_YEAR, THREE_YEARS, FIVE_YEARS), searchHandler)
                    .withConf(sectionConf));

            elements.add(createLivePeriodFromToTextOption("performanceToBenchmark", "performanceToBenchmark", // $NON-NLS$
                    I18n.I.differencePerformanceToBenchmark(), "%", P_ALL, searchHandler)
                    .withConf(sectionConf));

            elements.add(createLiveFromToTextOption("changePercentHighAlltime", "changePercentHighAlltime", // $NON-NLS$
                    I18n.I.changePercentHighAlltime(), "%", searchHandler).withConf(sectionConf));  // $NON-NLS$
            elements.add(createLiveFromToTextOption("changePercentHigh1y", "changePercentHigh1y", // $NON-NLS$
                    I18n.I.changePercentHigh1y(), "%", searchHandler).withConf(sectionConf));  // $NON-NLS$
            elements.add(createLiveFromToTextOption("changePercentLow1y", "changePercentLow1y", // $NON-NLS$
                    I18n.I.changePercentLow1y(), "%", searchHandler).withConf(sectionConf));  // $NON-NLS$
            elements.add(createLiveFromToTextOption("alpha1m", "alpha1m", // $NON-NLS$
                    I18n.I.alphaN(30), "%", searchHandler).withConf(sectionConf));
            elements.add(createLiveFromToTextOption("alpha1y", "alpha1y", I18n.I.alphaN(250), // $NON-NLS$
                    "%", searchHandler).withConf(sectionConf));
            elements.add(createLiveFromToInPercent("beta1m", "beta1m", I18n.I.betaM(30), // $NON-NLS$
                    searchHandler).withConf(sectionConf));
            elements.add(createLiveFromToInPercent("beta1y", "beta1y", I18n.I.betaM(250), // $NON-NLS$
                    searchHandler).withConf(sectionConf));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        addSection(RATIOS_ID, I18n.I.ratios(), false, elementsLoader,
                this.searchHandler).withConfigurable().loadElements();
    }

    private List<STKFinderElement> addHeader(List<STKFinderElement> list) {
        if (!this.isIndexSelected()
                || !this.indexBlock.isResponseOk()
                || this.indexBlock.getResult().getElement() == null
                || this.indexBlock.getResult().getElement().isEmpty()
                || !this.block.isResponseOk()
                || this.block.getResult().getElement() == null
                || this.block.getResult().getElement().isEmpty()) {
            return list;
        }
        List<STKFinderElement> result = new ArrayList<>();


        final List<INDFinderElement> indices = this.indexBlock.getResult().getElement();
        for (INDFinderElement ind : indices) {
            final STKFinderElement stk = createStkFromInd(ind);
            result.add(stk);
        }
        result.add(new STKFinderElement());
        for (STKFinderElement e : list) {
            result.add(e);
        }
        return result;
    }

    private TableDataModel createBaseModel() {
        return createModel(addHeader(getResult().getElement()), STKFinderElementMapper.BASE_ROW_MAPPER);
    }

    @Override
    protected void beforeSearch() {
        super.beforeSearch();
        this.indexBlock.setEnabled(isIndexSelected());

        final String qid = this.indexOption.getValueStr();
        this.indexBlock.setParameter("query", "vwdCode==" + qid); // $NON-NLS$
    }

    public boolean isIndexSelected() {
        return this.indexOption != null && this.indexOption.getValue();
    }

    private STKFinderElement createStkFromInd(INDFinderElement ind) {
        final STKFinderElement stk = new STKFinderElement();
        stk.setInstrumentdata(ind.getInstrumentdata());
        stk.setQuotedata(ind.getQuotedata());
        stk.setPrice(ind.getPrice());
        stk.setChangeNet(ind.getChangeNet());
        stk.setChangePercent(ind.getChangePercent());
        stk.setDate(ind.getDate());

        stk.setPerformance10Y(ind.getPerformance10Y());
        stk.setPerformance5Y(ind.getPerformance5Y());
        stk.setPerformance3Y(ind.getPerformance3Y());
        stk.setPerformance1Y(ind.getPerformance1Y());
        stk.setPerformance6M(ind.getPerformance6M());
        stk.setPerformance3M(ind.getPerformance3M());
        stk.setPerformance1M(ind.getPerformance1M());
        stk.setPerformance1W(ind.getPerformance1W());
        stk.setChangePercentHigh1Y(ind.getChangePercentHigh1Y());
        stk.setChangePercentLow1Y(ind.getChangePercentLow1Y());
        stk.setChangePercentHighAlltime(ind.getChangePercentHighAlltime());
        stk.setAverageVolume1W(ind.getAverageVolume1W());
        stk.setAverageVolume3M(ind.getAverageVolume3M());
        stk.setAverageVolume1Y(ind.getAverageVolume1Y());
        stk.setAverageVolume3Y(ind.getAverageVolume3Y());
        stk.setAverageVolume5Y(ind.getAverageVolume5Y());

        stk.setVolatility1W(ind.getVolatility1W());
        stk.setVolatility1M(ind.getVolatility1M());
        stk.setVolatility3M(ind.getVolatility3M());
        stk.setVolatility6M(ind.getVolatility6M());
        stk.setVolatility1Y(ind.getVolatility1Y());
        stk.setVolatility3Y(ind.getVolatility3Y());
        stk.setVolatility5Y(ind.getVolatility5Y());
        stk.setVolatility10Y(ind.getVolatility10Y());
        stk.setBeta1M(ind.getBeta1M());
        stk.setBeta1Y(ind.getBeta1Y());
        stk.setAlpha1M(ind.getAlpha1M());
        stk.setAlpha1Y(ind.getAlpha1Y());
        return stk;
    }

    private TableDataModel createPerfModel() {
        return createModel(addHeader(getResult().getElement()), STKFinderElementMapper.PERF_ROW_MAPPER);
    }

    private TableDataModel createRiskModel() {
        return createModel(addHeader(getResult().getElement()), STKFinderElementMapper.RISK_ROW_MAPPER);
    }

    private TableDataModel createFundamentalModel() {
        return createModel(addHeader(getResult().getElement()), STKFinderElementMapper.FUNDAMENTAL_ROW_MAPPER);
    }

    private TableDataModel createBenchmarkModel() {
        return createModel(addHeader(getResult().getElement()), STKFinderElementMapper.BENCHMARK_ROW_MAPPER);
    }

    protected FinderFormElements.LivePeriodFromToTextOption createLivePeriodFromToTextOptionInMio(
            String id, String field, String label, List<FinderFormElements.Item> items,
            SearchHandler searchHandler) {
        addToSortFields(field, label, items);
        return new FinderFormElements.LivePeriodFromToTextOption(id, field, label, I18n.I.millionAbbr(), items, searchHandler) {
            @Override
            protected String toQueryValue(String input, boolean query) {
                if ("".equals(input)) {
                    return "";
                }
                try {
                    final Double aDouble = Double.valueOf(input.replace(",", "."));
                    return query ? String.valueOf(aDouble * 1000000) : (input + " " + this.textFieldSuffix);
                } catch (Exception e) {
                    return "";
                }
            }
        };
    }
}