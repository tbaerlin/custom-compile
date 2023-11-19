/*
 * LiveFinderFND.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.gwt.dom.client.Element;

import de.marketmaker.iview.dmxml.FNDFinder;
import de.marketmaker.iview.dmxml.FNDFinderMetadata;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags.Feature;
import de.marketmaker.iview.mmgwt.mmweb.client.Ginjector;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.FinderLinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LiveFinderFND extends LiveFinder<FNDFinder, FNDFinderMetadata> {

    public static final String RATIOS_ID = "fndratios"; // $NON-NLS$

    public static final String EXTENDED_ID = "fndexteded"; // $NON-NLS$

    public static final String BASE_ID = "fndybase"; // $NON-NLS$

    public static final String FIXED_TOP_ID = "fndfixedtop"; // $NON-NLS$

    public static final LiveFinderFND INSTANCE = new LiveFinderFND();

    public static class TypeFinderLinkListener implements FinderLinkListener<String> {
        public static final TypeFinderLinkListener INSTANCE = new TypeFinderLinkListener();

        private TypeFinderLinkListener() {
        }

        public void onClick(LinkContext<String> context, Element e) {
            final String type = context.data;
            final FinderController controller = LiveFinderFND.INSTANCE;
            final FinderFormConfig ffc = new FinderFormConfig("temp", controller.getId()); // $NON-NLS-0$
            ffc.put(BASE_ID, "true"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.FUND_TYPE, "true"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.FUND_TYPE + "-item", type); // $NON-NLS-0$
            Customer.INSTANCE.prepareFndBestToolFinderLinkConfig(ffc);
            controller.prepareFind(ffc);
            PlaceUtil.goTo("M_LF_FND"); // $NON-NLS-0$
        }

        public LinkContext<String> createContext(String type) {
            return new LinkContext<>(this, type);
        }
    }

    private final boolean feriRating;

    private final boolean morningstarRating;

    private final boolean vwdBenlFundData;

    private FeatureFlags featureFlags = Ginjector.INSTANCE.getFeatureFlags();

    private SessionData sessionData = Ginjector.INSTANCE.getSessionData();

    private LiveFinderFND() {
        super("FND_Finder"); // $NON-NLS-0$
        this.sortFields.add(new Item(I18n.I.name(), "name"));  // $NON-NLS-0$

        this.feriRating = Selector.RATING_FERI.isAllowed();
        this.morningstarRating = Selector.RATING_MORNINGSTAR.isAllowed() || Selector.RATING_MORNINGSTAR_UNION_FND.isAllowed();
        this.vwdBenlFundData = Selector.FUNDDATA_VWD_BENL.isAllowed();
    }

    protected ViewSpec[] getResultViewSpec() {
        return new ViewSpec[]{
                new ViewSpec(I18n.I.basis()),
                new ViewSpec(I18n.I.ratios()),
                new ViewSpec(I18n.I.performance()),
                new ViewSpec(I18n.I.staticData())
        };
    }

    public String getId() {
        return "LFND"; // $NON-NLS-0$
    }

    public String getViewGroup() {
        return "finder-fnd"; // $NON-NLS-0$
    }

    public void prepareFind(String field1, String value1, String field2, String value2) {
    }

    protected void addSections() {
        addFixedTopSection();
        addSectionBase();
        addSectionExtended();
        addSectionRatios();
    }

    protected TableDataModel createDataModel(int view) {
        switch (view) {
            case 0:
                return createBaseModel();
            case 1:
                return createRatioModel();
            case 2:
                return createPerfModel();
            case 3:
                return createStaticModel();
            default:
                return null;
        }
    }

    protected AbstractFinderView createView() {
        return new FinderFNDView<>(this);
    }

    @Override
    protected List<FinderTypedMetaList> getLiveMetadata() {
        return this.block.getResult().getMetadata();
    }

    protected Map<String, FinderMetaList> getMetaLists() {
        return LiveFinderFND.getMetaLists(this.metaBlock.getResult());
    }

    static Map<String, FinderMetaList> getMetaLists(FNDFinderMetadata result) {
        final HashMap<String, FinderMetaList> map = new HashMap<>();
        map.put("country", result.getCountry()); // $NON-NLS-0$
        map.put("currency", result.getCurrency()); // $NON-NLS-0$
        map.put("distributionStrategy", result.getDistributionStrategy()); // $NON-NLS-0$
        map.put("fundtype", result.getFundtype()); // $NON-NLS-0$
        map.put(FinderFormKeys.SUBTYPE, result.getFundsubtype());
        map.put("investmentFocus", result.getInvestmentFocus()); // $NON-NLS-0$
        map.put("issuername", result.getIssuername()); // $NON-NLS-0$
        map.put("ratingFeri", result.getRatingFeri()); // $NON-NLS-0$
        map.put("ratingMorningstar", result.getRatingMorningstar()); // $NON-NLS-0$
        map.put("sector", result.getSector()); // $NON-NLS-0$
        map.put("marketAdmission", result.getMarketAdmission()); // $NON-NLS-0$
        map.put("diamondRating", result.getDiamondRating()); // $NON-NLS-0$
        map.put("srriValue", result.getSrriValue()); // $NON-NLS-0$
        return map;
    }

    private void addSectionExtended() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(EXTENDED_ID);
            int defaultOrder = 0;

            if (this.feriRating) {
                elements.add(createLiveFromToBoxOption("ratingFeri", I18n.I.ratingFeri(), null, this.searchHandler) // $NON-NLS$
                        .withSortReverse(true).unoptimized().withConf(sectionConf, defaultOrder++));
            }
            if (this.morningstarRating) {
                elements.add(createLiveFromToBoxOption("ratingMorningstar", I18n.I.ratingMorningstar(), null, this.searchHandler) // $NON-NLS$
                        .unoptimized().withConf(sectionConf, defaultOrder++));
            }

            elements.add(createLiveFromToTextOption("issueSurcharge", "issueSurcharge", I18n.I.issueSurcharge(), "%", this.searchHandler) // $NON-NLS$
                    .withConf(sectionConf, defaultOrder++));
            elements.add(createLiveFromToTextOption("managementFee", "managementFee", I18n.I.managementFee(), "%", this.searchHandler) // $NON-NLS$
                    .withConf(sectionConf, defaultOrder));
            elements.add(createLiveFromToTextOption("accountFee", "accountFee", I18n.I.depotAccountFee(), "%", this.searchHandler) // $NON-NLS$
                    .withConf(sectionConf));
            elements.add(createLiveFromToTextOption("ongoingCharge", "ongoingCharge", I18n.I.ongoingCharges(), "%", this.searchHandler) // $NON-NLS$
                    .withConf(sectionConf));
            elements.add(createLiveFromToTextOption("ter", "ter", I18n.I.totalExpenseRatio(), "%", this.searchHandler) // $NON-NLS$
                    .withConf(sectionConf));
            elements.add(createLiveFromToTextOption("maximumLoss3y", "maximumLoss3y", I18n.I.maximumLossInThreeYears(), "%", this.searchHandler) // $NON-NLS$
                    .withConf(sectionConf));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        addSection(EXTENDED_ID, I18n.I.extendedInfo(), false, elementsLoader, this.searchHandler)
                .withConfigurable().loadElements();
    }

    private void addSectionRatios() {
        final List<Item> p_all
                = Arrays.asList(ONE_WEEK, ONE_MONTH, THREE_MONTH, SIX_MONTHS, ONE_YEAR, THREE_YEARS, FIVE_YEARS, TEN_YEARS);
        final List<Item> bvi = Arrays.asList(ONE_DAY, ONE_WEEK, ONE_MONTH, SIX_MONTHS, ONE_YEAR, THREE_YEARS, FIVE_YEARS, TEN_YEARS);

        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(RATIOS_ID);
            int defaultOrder = 0;

            elements.add(createLivePeriodFromToTextOption("volatility", "volatility", I18n.I.volatility(), "%", p_all, this.searchHandler) // $NON-NLS$
                    .withConf(sectionConf, defaultOrder++));
            elements.add(createLivePeriodFromToTextOption("bviperformance", "bviperformance", I18n.I.performance(), "%", bvi, this.searchHandler) // $NON-NLS$
                    .withConf(sectionConf, defaultOrder++));

            if (this.vwdBenlFundData) {
                elements.add(createLiveFromToBoxOption("srriValue", I18n.I.srri(), null, this.searchHandler) // $NON-NLS$
                        .withConf(sectionConf, defaultOrder++));
                elements.add(createLiveFromToBoxOption("diamondRating", I18n.I.diamondRating(), null, this.searchHandler) // $NON-NLS$
                        .withConf(sectionConf, defaultOrder++));
            }

            elements.add(createLivePeriodFromToTextOption("sharpeRatioWithPeriod", "sharpeRatio", I18n.I.sharpeRatio(), p_all, this.searchHandler) // $NON-NLS$
                    .withConf(sectionConf));
            elements.add(createLivePeriodFromToTextOption("alpha", "alpha", I18n.I.jensensAlpha(), p_all, this.searchHandler) // $NON-NLS$
                    .withConf(sectionConf));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        addSection(RATIOS_ID, I18n.I.ratios(), false, elementsLoader, this.searchHandler)
                .withConfigurable().loadElements();
    }

    class LiveUnionOption extends LiveBooleanOption implements DynamicValueElement {
        LiveUnionOption(String field, String label, SearchHandler searchHandler) {
            super(field, label, searchHandler);
        }
    }

    private void addFixedTopSection() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();

            if (this.featureFlags.isEnabled0(Feature.DZ_RELEASE_2015)) {
                AbstractOption hideNotActive = new LiveBooleanOption("notActive", I18n.I.hideInactiveInstruments(), true, this.searchHandler);  // $NON-NLS$;
                hideNotActive.setValue(true);
                elements.add(hideNotActive);
            }

            return elements;
        };

        final FinderSection finderSection = addSection(FIXED_TOP_ID,
                "fixedTopSection", true, elementsLoader, this.searchHandler).loadElements(); // $NON-NLS$
        finderSection.setAlwaysExpanded();
    }

    private void addSectionBase() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(BASE_ID);
            int defaultOrder = 0;
            final Map<String, FinderMetaList> metalists = getMetaLists();

            if (Customer.INSTANCE.isDzWgz()) {
                final DynamicSearchHandler unionHandler = new DynamicSearchHandler();
                final AbstractOption vrIssuer = new LiveUnionOption("vrIssuer", I18n.I.unionFunds(), unionHandler) // $NON-NLS$
                        .withConf(sectionConf, defaultOrder++);
                vrIssuer.setValue(true);
                elements.add(unionHandler.withElement(vrIssuer));
            }

            final String issuername = FinderFormKeys.ISSUER_NAME;
            elements.add(createSortableLiveMultiEnum(issuername,
                    (I18n.I.fundCompanyNew()), null, metalists,
                    issuername, new DynamicSearchHandler()).withConf(sectionConf, defaultOrder++));
            final String fundtype = "fundtype"; // $NON-NLS$
            elements.add(createSortableLiveMultiEnum(fundtype, I18n.I.fundType(), null, metalists,
                    fundtype, new DynamicSearchHandler()).withConf(sectionConf, defaultOrder));
            if(this.sessionData.isGuiDefValueTrue("liveFinderFndWithSubtype")) { // $NON-NLS$
                elements.add(createSortableLiveMultiEnum(FinderFormKeys.SUBTYPE, I18n.I.subcategory(), null, metalists,
                        FinderFormKeys.SUBTYPE, new DynamicSearchHandler()).withConf(sectionConf, defaultOrder));
            }
            final String investmentFocus = "investmentFocus"; // $NON-NLS$
            elements.add(createSortableLiveMultiEnum(investmentFocus, I18n.I.investmentFocus(), null, metalists,
                    investmentFocus, new DynamicSearchHandler()).withConf(sectionConf));
            elements.add(new LiveBooleanOption("etf", I18n.I.onlyETFFund(), this.searchHandler).withConf(sectionConf));  // $NON-NLS-0$
            final String country = "country"; // $NON-NLS$
            elements.add(createSortableLiveMultiEnum(country, I18n.I.fundCountry(), null, metalists,
                    country, new DynamicSearchHandler()).withConf(sectionConf));
            final String marketAdmission = "marketAdmission"; // $NON-NLS$
            elements.add(createSortableLiveMultiEnum(marketAdmission, I18n.I.marketAdmission(), I18n.I.germanyIso3166Alpha2(),
                    metalists, marketAdmission, new DynamicSearchHandler()).withConf(sectionConf));
            final String currency = "currency"; // $NON-NLS$
            elements.add(createSortableLiveMultiEnum(currency, I18n.I.fundCurrency(), "EUR", metalists, // $NON-NLS$
                    currency, new DynamicSearchHandler()).withConf(sectionConf));
            final DynamicSearchHandler strategyHandler = new DynamicSearchHandler();
            final AbstractOption strategy = new LiveRadioOption("distributionStrategy", I18n.I.distributionStrategy(), // $NON-NLS$
                    null, strategyHandler).withConf(sectionConf);
            elements.add(strategyHandler.withElement(strategy));

            final LiveFromToTextOption fundVol = createLiveFromToInMio("fundVolume", "fundVolume", // $NON-NLS$
                    I18n.I.fundVolume(), this.searchHandler);
            elements.add(fundVol.withConf(sectionConf));
            elements.add(new LiveFromToTextOption("issueDate", "issueDate", I18n.I.issueDateFnd(), I18n.I.years(), this.searchHandler) { // $NON-NLS$
                @Override
                protected String toQueryValue(String input, boolean query) {
                    return toQueryValueYears(input, query);
                }

                @Override
                protected boolean isNegative() {
                    return true;
                }

            }.withConf(sectionConf));
            if (this.featureFlags.isEnabled0(Feature.DZ_RELEASE_2015) && Customer.INSTANCE.isDzWgz()) {
                final String wmClass = "wmInvestmentAssetPoolClass"; // $NON-NLS-0$
                final String ogaw = "OGAW", aif = "AIF", all = ""; // $NON-NLS$
                final List<Item> parList = Arrays.asList(new Item(I18n.I.all(), all), new Item(ogaw, ogaw), new Item(aif, aif));
                final LiveListBoxOption par =
                        new LiveListBoxOption(FinderFormKeys.OGAWAIF, wmClass, "OGAW/AIF", parList, all, this.searchHandler); // $NON-NLS-0$
                elements.add(par.withoutMetadataUpdate().withConf(sectionConf));
            }

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };


        final FinderSection section = addSection(BASE_ID, I18n.I.baseInfo(), false, elementsLoader, this.searchHandler)
                .expand().withConfigurable().loadElements();
        if (Customer.INSTANCE.isDzWgz()) {
            section.setValue(true);
        }
        this.sortFields.add(new Item(I18n.I.issueDateFnd(), "issueDate"));  // $NON-NLS-0$
    }

    private TableDataModel createBaseModel() {
        return createModel(getResult().getElement(), FNDFinderElementMapper.BASE_ROW_MAPPER);
    }

    private TableDataModel createPerfModel() {
        return createModel(getResult().getElement(), FNDFinderElementMapper.PERF_ROW_MAPPER);
    }

    private TableDataModel createRatioModel() {
        return createModel(getResult().getElement(), FNDFinderElementMapper.RATIO_ROW_MAPPER);
    }

    private TableDataModel createStaticModel() {
        return createModel(getResult().getElement(), FNDFinderElementMapper.STATIC_ROW_MAPPER);
    }

    @Inject
    public void setFeatureFlags(FeatureFlags featureFlags) {
        this.featureFlags = featureFlags;
    }

    @Inject
    public void setSessionData(SessionData sessionData) {
        this.sessionData = sessionData;
    }
}