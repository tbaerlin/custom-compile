/*
 * LiveFinderBND.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.BNDFinder;
import de.marketmaker.iview.dmxml.BNDFinderMetadata;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.Ginjector;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LiveFinderBND extends LiveFinder<BNDFinder, BNDFinderMetadata> {

    public static final LiveFinderBND INSTANCE = new LiveFinderBND();

    private static final String BASE_ID = "bndbase"; // $NON-NLS$

    private static final String RATIOS_ID = "bndratios"; // $NON-NLS$

    private static final String EXTENDED_ID = "bndextended"; // $NON-NLS$

    private static final String FIXED_TOP_ID = "bndfixedtop"; // $NON-NLS$

    private static final List<Item> P_ALL
            = Arrays.asList(ONE_WEEK, ONE_MONTH, THREE_MONTH, SIX_MONTHS, ONE_YEAR, THREE_YEARS, FIVE_YEARS, TEN_YEARS);

    private FeatureFlags featureFlags = Ginjector.INSTANCE.getFeatureFlags();

    private LiveFinderBND() {
        super("BND_Finder"); // $NON-NLS-0$
        this.sortFields.add(new Item(I18n.I.name(), "name"));  // $NON-NLS-0$
    }

    protected ViewSpec[] getResultViewSpec() {
        return new ViewSpec[]{
                new ViewSpec(I18n.I.basis()),
                new ViewSpec(I18n.I.ratios()),
                new ViewSpec(I18n.I.performance()),
                new ViewSpec(I18n.I.risk())
        };
    }

    public String getId() {
        return "LBND"; // $NON-NLS-0$
    }

    public String getViewGroup() {
        return "finder-bnd"; // $NON-NLS-0$
    }

    public LiveFinderBND findDzPibs() {
        final FinderFormConfig result = new FinderFormConfig("withDzPibs", getId()); // $NON-NLS-0$
        result.put(BASE_ID, "true"); // $NON-NLS$
        result.put(FinderFormKeys.ONLY_CUSTOM_ISSUER, "true"); // $NON-NLS$
        result.put(FinderFormKeys.PIB_AVAILABLE, "true"); // $NON-NLS$
        prepareFind(result);
        return this;
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
                return createRiskModel();
            default:
                return null;
        }
    }

    protected AbstractFinderView createView() {
        return new FinderBNDView<LiveFinderBND>(this);
    }

    @Override
    protected List<FinderTypedMetaList> getLiveMetadata() {
        return this.block.getResult().getMetadata();
    }

    protected Map<String, FinderMetaList> getMetaLists() {
        final BNDFinderMetadata result = this.metaBlock.getResult();
        final HashMap<String, FinderMetaList> map = new HashMap<String, FinderMetaList>();
        map.put("country", result.getCountry()); // $NON-NLS-0$
        map.put("currency", result.getCurrency()); // $NON-NLS-0$
        map.put("bondType", result.getBondType()); // $NON-NLS-0$
        map.put("couponType", result.getCouponType()); // $NON-NLS-0$
        map.put("ratingFitchLongTerm", result.getRatingFitchLongTerm()); // $NON-NLS-0$
        map.put("issuerCategory", result.getIssuerCategory()); // $NON-NLS-0$
        map.put("sector", result.getSector()); // $NON-NLS-0$
        map.put("issuername", result.getIssuername()); // $NON-NLS-0$
        map.put("ratingMoodys", result.getRatingMoodys()); // $NON-NLS-0$
        map.put("marketgroups", result.getMarkets()); // $NON-NLS$
        return map;
    }

    private void addSectionExtended() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<FinderFormElement>();
                final String[] sectionConf = SectionConfigUtil.getSectionConf(EXTENDED_ID);
                int defaultOrder = 0;

                if (Selector.RATING_FITCH.isAllowed() || Selector.RATING_MOODYS.isAllowed()) {
                    if (Selector.RATING_FITCH.isAllowed()) {
                        elements.add(createLiveFromToBoxOption("ratingFitchLongTerm", // $NON-NLS$
                                I18n.I.fitchRating(), null, new DynamicSearchHandler()).withSortReverse(true).withConf(sectionConf, defaultOrder++));
                    }
                    if (Selector.RATING_MOODYS.isAllowed()) {
                        elements.add(createLiveFromToBoxOption("ratingMoodys", // $NON-NLS$
                                I18n.I.moodysRating(), null, new DynamicSearchHandler()).withSortReverse(true).withConf(sectionConf, defaultOrder++));
                    }
                }
                elements.add(new LiveFromToTextOption("accruedInterest", "brokenPeriodInterest", I18n.I.accruedInterest(), // $NON-NLS$
                        I18n.I.currency(), searchHandler).withConf(sectionConf, defaultOrder++));
                final String interestRateElasticity = "interestRateElasticity"; // $NON-NLS$
                elements.add(new LiveFromToTextOption(interestRateElasticity, interestRateElasticity, I18n.I.interestRateElasticity(),
                        "%", searchHandler).withConf(sectionConf, defaultOrder)); // $NON-NLS$

                elements.add(new LiveBooleanOption("bondRank", I18n.I.subordinatedBonds(), searchHandler) {  // $NON-NLS$
                    @Override
                    protected String doGetQuery() {
                        return getFieldname() + "=='nachrangig'";  // $NON-NLS$
                    }
                }.withConf(sectionConf, defaultOrder));

                elements.addAll(handleClones(sectionConf, elements));
                return orderBySectionConf(elements, sectionConf);
            }
        };
        addSection(EXTENDED_ID, I18n.I.extendedInfo(), false, elementsLoader, this.searchHandler)
                .withConfigurable().loadElements();
    }

    private void addSectionRatios() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<FinderFormElement>();
                final String[] sectionConf = SectionConfigUtil.getSectionConf(RATIOS_ID);
                int defaultOrder = 0;

                elements.add(new LiveFromToTextOption("yieldRelativePerYear", "yieldRelativePerYear", I18n.I.yield(), // $NON-NLS$
                        "%", searchHandler).withConf(sectionConf, defaultOrder++));
                elements.add(new LiveFromToTextOption("basePointValue", "basePointValue", I18n.I.basePointValue(), // $NON-NLS$
                        "", searchHandler).withConf(sectionConf, defaultOrder++));
                elements.add(new LiveFromToTextOption("duration", "duration", I18n.I.duration(), // $NON-NLS$
                        "", searchHandler).withConf(sectionConf, defaultOrder));
                elements.add(new LiveFromToTextOption("convexity", "convexity", I18n.I.convexity(), // $NON-NLS$
                        "", searchHandler).withConf(sectionConf));
                elements.add(new LiveFromToTextOption("modifiedDuration", "modifiedDuration", I18n.I.modifiedDuration(), // $NON-NLS$
                        "", searchHandler).withConf(sectionConf));
                elements.add(createLivePeriodFromToTextOption("volatility", "volatility", I18n.I.volatility(), "%", // $NON-NLS$
                        P_ALL, searchHandler).withConf(sectionConf));

                elements.addAll(handleClones(sectionConf, elements));
                return orderBySectionConf(elements, sectionConf);
            }
        };
        addSection(RATIOS_ID, I18n.I.ratios(), false, elementsLoader, this.searchHandler)
                .withConfigurable().loadElements();
    }

    private void addFixedTopSection() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<FinderFormElement>();

                addCustomIssuerOnlyElement(elements);

                if (Selector.PRODUCT_WITH_PIB.isAllowed() && Selector.DZ_BANK_USER.isAllowed()) {
                    elements.add(new LiveBooleanOption(FinderFormKeys.PIB_AVAILABLE,
                            I18n.I.offerWithPib(), searchHandler));
                }

                if (FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled()) {
                    AbstractOption hideNotActive = new LiveBooleanOption("notActive", I18n.I.hideInactiveInstruments(), true, searchHandler);  // $NON-NLS$;
                    hideNotActive.setValue(true);
                    elements.add(hideNotActive);
                }

                return elements;
            }
        };

        final FinderSection finderSection = addSection(FIXED_TOP_ID,
                "fixedTopSection", true, elementsLoader, searchHandler).loadElements(); // $NON-NLS$
        finderSection.setAlwaysExpanded();
    }

    private void addSectionBase() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<FinderFormElement>();
                final String[] sectionConf = SectionConfigUtil.getSectionConf(BASE_ID);
                int defaultOrder = 0;
                final Map<String, FinderMetaList> metaLists = getMetaLists();

                elements.add(createLiveFromToTextOption("coupon", "coupon", I18n.I.coupon(), "%", searchHandler) // $NON-NLS$
                        .withConf(sectionConf, defaultOrder++));
                final String issuername = FinderFormKeys.ISSUER_NAME;
                elements.add(createSortableLiveSuggestEnum(issuername, I18n.I.issuer(), null, metaLists, issuername,
                        new DynamicSearchHandler()).withStyle("width160").withConf(sectionConf, defaultOrder++)); // $NON-NLS$
                elements.add(new FinderFormElements.LiveStartEndOption(FinderFormKeys.EXPIRATION_DATE, FinderFormKeys.EXPIRATION_DATE,
                        I18n.I.maturity2(), "", LIST_EXPIRATION_DATES, DateTimeUtil.PeriodMode.FUTURE, searchHandler)
                        .withConf(sectionConf, defaultOrder));
                elements.add(new FinderFormElements.LiveStartEndOption(FinderFormKeys.ISSUE_DATE, FinderFormKeys.ISSUE_DATE, I18n.I.issueDate2(), "", LIST_EXPIRATION_DATES,
                        DateTimeUtil.PeriodMode.PAST, searchHandler).withConf(sectionConf));
                final LiveFromToTextOption remaining =
                        new FinderFormElements.LiveFromToTextOption(FinderFormKeys.REMAINING, "expirationDate", I18n.I.remainingTime(), I18n.I.years(), searchHandler) { // $NON-NLS$

                            protected String toQueryValue(String input, boolean query) {
                                return toQueryValueYears(input, query);
                            }
                        };
                elements.add(remaining.withConf(sectionConf));
                addMarkets(elements, sectionConf);
                final String issuerCategory = "issuerCategory"; // $NON-NLS$
                elements.add(createSortableLiveMultiEnum(issuerCategory, I18n.I.issuerCategory(), null, metaLists,
                        issuerCategory, new DynamicSearchHandler()).withStyle("width160").withConf(sectionConf)); // $NON-NLS$
                if (Selector.ANY_VWD_TERMINAL_PROFILE.isAllowed()) {
                    final String sector = "sector"; // $NON-NLS$
                    elements.add(createSortableLiveMultiEnum(sector, I18n.I.sector(), null, metaLists,
                            sector, new DynamicSearchHandler()).withStyle("width160").withConf(sectionConf)); // $NON-NLS$
                    Firebug.log("sector criteria added");
                }
                final String couponType = "couponType"; // $NON-NLS$
                elements.add(createSortableLiveMultiEnum(couponType, I18n.I.couponType(), null, metaLists, couponType,
                        new DynamicSearchHandler()).withStyle("width160").withConf(sectionConf)); // $NON-NLS$

                final FinderFormElements.LiveListBoxOption par = new FinderFormElements.LiveListBoxOption("par", "price", I18n.I.par(), LIST_PAR, // $NON-NLS$
                        I18n.I.abovePar(), searchHandler) {

                    @Override
                    protected String doGetQuery() {
                        for (Item item : LIST_PAR) {
                            if (item.value.equals(this.getValueStr())) {
                                return this.field + item.value + "100"; // $NON-NLS$
                            }
                        }
                        return "";
                    }
                };
                elements.add(par.withoutMetadataUpdate().withConf(sectionConf));

                final String country = "country"; // $NON-NLS$
                elements.add(createSortableLiveMultiEnum(country, I18n.I.country(), null, metaLists, country,
                        new DynamicSearchHandler()).withStyle("width160").withConf(sectionConf)); // $NON-NLS$
                final String bondType = "bondType"; // $NON-NLS$
                elements.add(createSortableLiveMultiEnum(bondType, I18n.I.bondType(), null, metaLists, bondType,
                        new DynamicSearchHandler()).withStyle("width160").withConf(sectionConf)); // $NON-NLS$
                final String currency = "currency"; // $NON-NLS$
                elements.add(createSortableLiveMultiEnum(currency, I18n.I.currency(), "EUR", metaLists, currency, // $NON-NLS$
                        searchHandler).withConf(sectionConf));
                elements.add(createAverageVolume1wGtZero(searchHandler).withConf(sectionConf));

                if (featureFlags.isEnabled0(FeatureFlags.Feature.VWD_RELEASE_2014) || featureFlags.isEnabled0(FeatureFlags.Feature.DZ_RELEASE_2016)) {
                    final String smallestTransferableUnit = "smallestTransferableUnit";  // $NON-NLS$
                    elements.add(createLiveFromToTextOption(smallestTransferableUnit, smallestTransferableUnit, I18n.I.smallestTransferableUnit(), "", searchHandler) // $NON-NLS$
                            .withConf(sectionConf));
                }

                elements.addAll(handleClones(sectionConf, elements));
                return orderBySectionConf(elements, sectionConf);
            }
        };
        final FinderSection baseSection = addSection(BASE_ID, I18n.I.baseInfo(), false, elementsLoader, this.searchHandler)
                .expand().withConfigurable().loadElements();
        if (FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled()) {
            baseSection.setValue(true);
        }
    }

    private TableDataModel createBaseModel() {
        return createModel(getResult().getElement(), BNDFinderElementMapper.BASE_ROW_MAPPER);
    }

    private TableDataModel createPerfModel() {
        return createModel(getResult().getElement(), BNDFinderElementMapper.PERF_ROW_MAPPER);
    }

    private TableDataModel createRatioModel() {
        return createModel(getResult().getElement(), BNDFinderElementMapper.RATIO_ROW_MAPPER);
    }

    private TableDataModel createRiskModel() {
        return createModel(getResult().getElement(), BNDFinderElementMapper.RISK_ROW_MAPPER);
    }

    @Inject
    public void setFeatureFlags(FeatureFlags featureFlags) {
        this.featureFlags = featureFlags;
    }
}
