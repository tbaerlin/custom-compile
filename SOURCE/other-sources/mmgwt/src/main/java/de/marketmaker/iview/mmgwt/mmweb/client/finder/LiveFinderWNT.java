/*
 * FinderFND.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;

import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.dmxml.WNTFinder;
import de.marketmaker.iview.dmxml.WNTFinderMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LiveFinderWNT extends LiveFinder<WNTFinder, WNTFinderMetadata> {

    public static final String WNT_FINDER_ID = "LWNT"; // $NON-NLS-0$

    public static final String UNDERLYING_ID = "wntunderlying"; // $NON-NLS$

    public static final String BASE_ID = "wntbase"; // $NON-NLS$

    public static final String RATIOS_ID = "wntratios"; // $NON-NLS$

    public static final String FIXED_TOP_ID = "wntfixedtop"; // $NON-NLS$

    public static class CallPutLinkListener implements LinkListener<CallPutLinkListener.Context> {
        public static final CallPutLinkListener INSTANCE = new CallPutLinkListener();

        public static class Context {
            final QuoteWithInstrument qwi;

            final boolean call;

            final String issuer;

            private Context(QuoteWithInstrument qwi, boolean call, String issuer) {
                this.call = call;
                this.qwi = qwi;
                this.issuer = issuer;
            }
        }

        private CallPutLinkListener() {
        }

        public void onClick(LinkContext<Context> context, Element e) {
            final Context c = context.data;
            final FinderController controller = LiveFinderWNT.INSTANCE;

            final FinderFormConfig ffc = new FinderFormConfig("temp", LiveFinderWNT.INSTANCE.getId()); // $NON-NLS-0$
            ffc.put(UNDERLYING_ID, "true"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.UNDERLYING, "true"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.UNDERLYING + "-symbol", c.qwi.getIid(false)); // $NON-NLS-0$
            ffc.put(FinderFormKeys.UNDERLYING + "-name", c.qwi.getInstrumentData().getName()); // $NON-NLS-0$
            ffc.put(BASE_ID, "true"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.WARRANT_TYPE, "true"); // $NON-NLS$
            ffc.put(FinderFormKeys.WARRANT_TYPE + "-item", c.call ? "C" : "P"); // $NON-NLS$ $NON-NLS-2$
            if (c.issuer != null) {
                ffc.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS$
                ffc.put(FinderFormKeys.ISSUER_NAME + "-item", c.issuer); // $NON-NLS-0$
            }

            controller.prepareFind(ffc);
            PlaceUtil.goTo("M_LF_WNT"); // $NON-NLS-0$
        }

        public LinkContext<Context> createContext(QuoteWithInstrument qwi, boolean call,
                String issuer) {
            return new LinkContext<>(this, new Context(qwi, call, issuer));
        }
    }

    public static final LiveFinderWNT INSTANCE = new LiveFinderWNT();

    private LiveFinderWNT() {
        super("WNT_Finder"); // $NON-NLS-0$
        this.block.setParameter("providerPreference", SessionData.INSTANCE.getGuiDefValue("providerPreference")); // $NON-NLS$
        this.metaBlock.setParameter("providerPreference", SessionData.INSTANCE.getGuiDefValue("providerPreference")); // $NON-NLS$

        this.sortFields.add(new Item(I18n.I.name(), "name"));  // $NON-NLS-0$
        if (Selector.EDG_RATING.isAllowed()) {
            this.sortFields.add(new Item(I18n.I.riskClass(), "edgTopClass"));  // $NON-NLS-0$
        }
    }

    protected ViewSpec[] getResultViewSpec() {
        return WNTFinderElementMapper.createResultViewSpec();
    }

    public String getId() {
        return WNT_FINDER_ID;
    }

    @Override
    public String getViewGroup() {
        return "finder-wnt"; // $NON-NLS-0$
    }

    public void prepareFind(String field1, String value1, String field2, String value2) {
        if (field1.equals(FinderFormKeys.ISSUER_NAME) && field2.equals("warrantType")) { // $NON-NLS$
            this.pending = new FinderFormConfig("multifind", getId()); // $NON-NLS-0$
            FinderSection.enableBaseSection(this.pending, "WNT"); // $NON-NLS-0$
            this.pending.put(BASE_ID, "true"); // $NON-NLS$
            this.pending.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS$
            this.pending.put(FinderFormKeys.ISSUER_NAME + "-item", value1); // $NON-NLS-0$
            this.pending.put(FinderFormKeys.WARRANT_TYPE, "true"); // $NON-NLS-0$
            this.pending.put(FinderFormKeys.WARRANT_TYPE + "-item", value2); // $NON-NLS-0$
        }
    }

    public LiveFinderWNT findNewProducts() {
        final FinderFormConfig result = new FinderFormConfig("newproducts", getId()); // $NON-NLS-0$
        result.put(BASE_ID, "true"); // $NON-NLS$
        result.put(FinderFormKeys.ONLY_CUSTOM_ISSUER, "true"); // $NON-NLS$
        result.put(FinderFormKeys.NEW_PRODUCTS_ISSUE_DATE, "true"); // $NON-NLS-0$
        result.put(FinderFormKeys.NEW_PRODUCTS_ISSUE_DATE + "-item", "ONE_DAY"); // $NON-NLS-0$ $NON-NLS-1$
        prepareFind(result);
        return this;
    }

    protected void addSections() {
        addFixedTopSection();
        addSectionUnderlying();
        addSectionBase();
        addSectionRatios();
        if (Selector.EDG_RATING.isAllowed()) {
            addSectionEdg();
        }
    }

    protected TableDataModel createDataModel(int view) {
        switch (view) {
            case 0:
                return createPriceModel();
            case 1:
                return createPerfModel();
            case 2:
                return createRatiosModel();
            case 3:
                return createBaseModel();
            case 4:
                return createEdgModel();
            default:
                return null;
        }
    }

    protected AbstractFinderView createView() {
        return new FinderWNTView<>(this);
    }

    protected Map<String, FinderMetaList> getMetaLists() {
        return WNTFinderElementMapper.getMetaLists(this.metaBlock.getResult());
    }

    @Override
    protected List<FinderTypedMetaList> getLiveMetadata() {
        return this.block.getResult().getMetadata();
    }

    private void addFixedTopSection() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<FinderFormElement>();
                addCustomIssuerOnlyElement(elements);

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

    private void addSectionUnderlying() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<>();
                final String[] sectionConf = SectionConfigUtil.getSectionConf(UNDERLYING_ID);

                final FinderFormElements.AbstractOption option = createUnderlyingOption(false, "WNT", null) // $NON-NLS$
                        .withStyle("width160").withConf(sectionConf, 0); // $NON-NLS$
                option.setAlwaysEnabled();
                elements.add(option);

                elements.addAll(handleClones(sectionConf, elements));
                return orderBySectionConf(elements, sectionConf);
            }
        };
        addSection(UNDERLYING_ID, I18n.I.underlyingInstrument(), false, elementsLoader, searchHandler)
                .loadElements();
    }

    private void addSectionBase() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<>();
                final String[] sectionConf = SectionConfigUtil.getSectionConf(BASE_ID);
                int defaultOrder = 0;

                elements.add(createSortableLiveListBoxOption(FinderFormKeys.WARRANT_TYPE, I18n.I.type(), Arrays.asList(new Item(" Call", "C"), // $NON-NLS$
                        new Item(" Put", "P")), "C", searchHandler).withConf(sectionConf, defaultOrder++)); // $NON-NLS$
                elements.add(createLiveFromToTextOption("strike", I18n.I.strike(), searchHandler).withConf(sectionConf, defaultOrder++));  // $NON-NLS-0$
                elements.add(new LiveStartEndOption(FinderFormKeys.EXPIRATION_DATE, "expirationDate", I18n.I.maturity2(), "", // $NON-NLS$
                        LIST_EXPIRATION_DATES, DateTimeUtil.PeriodMode.FUTURE, searchHandler).withConf(sectionConf, defaultOrder));

                final LiveFromToTextOption remainingMonthsOption
                        = new LiveFromToTextOption(FinderFormKeys.REMAINING, "expirationDate", I18n.I.remainingTime(), I18n.I.months(), searchHandler) {  // $NON-NLS$
                    @Override
                    protected String toQueryValue(String input, boolean query) {
                        return toQueryValueMonths(input, 0, 120, query);
                    }
                };
                elements.add(remainingMonthsOption.withConf(sectionConf));
                final String issuername = FinderFormKeys.ISSUER_NAME;
                elements.add(createSortableLiveMultiEnum(issuername, I18n.I.issuer(), null, getMetaLists(),
                        issuername, searchHandler).withConf(sectionConf));
                addMarkets(elements, sectionConf);
                elements.add(createSortableLiveListBoxOption("isAmerican", I18n.I.isAmerican(), Arrays.asList(new Item(I18n.I.isAmericanAbbr(), "true"), // $NON-NLS$
                        new Item(I18n.I.isEuropeanAbbr(), "false")), I18n.I.isAmericanAbbr(), searchHandler) // $NON-NLS$
                        .withoutMetadataUpdate().withConf(sectionConf));
                elements.add(createAverageVolume1wGtZero(searchHandler).withConf(sectionConf));
                elements.add(new LiveStartEndOption(FinderFormKeys.NEW_PRODUCTS_ISSUE_DATE, "issueDate", I18n.I.newProductsTradedSince(), "", LIST_ISSUE_DATES, // $NON-NLS$
                        DateTimeUtil.PeriodMode.PAST, searchHandler).withConf(sectionConf));
                elements.add(new LiveStartEndOption(FinderFormKeys.ISSUE_DATE, "issueDate", I18n.I.issueDate2(), "", LIST_EXPIRATION_DATES, // $NON-NLS$
                        DateTimeUtil.PeriodMode.PAST, searchHandler).withConf(sectionConf));

                elements.addAll(handleClones(sectionConf, elements));
                return orderBySectionConf(elements, sectionConf);
            }
        };
        final FinderSection baseSection = addSection(BASE_ID, I18n.I.baseInfo(), false, elementsLoader, searchHandler)
                .expand().withConfigurable().loadElements();
        if (FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled()) {
            baseSection.setValue(true);
        }
    }

    private void addSectionRatios() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<>();
                final String[] sectionConf = SectionConfigUtil.getSectionConf(RATIOS_ID);
                int defaultOrder = 0;

                elements.add(createLiveFromToTextOption("omega", I18n.I.omega(), searchHandler).withConf(sectionConf, defaultOrder++));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("impliedVolatility", "impliedVolatility", I18n.I.impliedVolatility(), "%", searchHandler) // $NON-NLS$
                        .withConf(sectionConf, defaultOrder++));
                elements.add(createLiveFromToTextOption("delta", I18n.I.delta(), searchHandler).withConf(sectionConf, defaultOrder));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("intrinsicValue", I18n.I.intrinsicValue(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("fairValue", I18n.I.fairValue(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("optionPricePerYear", "optionPricePerYear", I18n.I.optionPricePerYear(), "%", searchHandler).withConf(sectionConf)); // $NON-NLS$
                elements.add(createLiveFromToTextOption("breakeven", I18n.I.breakeven(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("fairValue", I18n.I.fairValue2(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("volatility1m", "volatility1m", I18n.I.volaNMonths(1), "%", // $NON-NLS$
                        searchHandler).withConf(sectionConf));
                elements.add(createLiveFromToTextOption("volatility3m", "volatility3m", I18n.I.volaNMonths(3), "%", // $NON-NLS$
                        searchHandler).withConf(sectionConf));
                elements.add(createLiveFromToTextOption("leverage", I18n.I.leverage(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("spread", I18n.I.spread(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("spreadPercent", "spreadPercent", I18n.I.spreadPercent(), "%", // $NON-NLS$
                        searchHandler).withConf(sectionConf));
                elements.add(createLiveFromToTextOption("theta", I18n.I.thetaWeek(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("gamma", I18n.I.gamma(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("rho", I18n.I.rho(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("vega", I18n.I.vega(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$

                elements.addAll(handleClones(sectionConf, elements));
                return orderBySectionConf(elements, sectionConf);
            }
        };

        addSection(RATIOS_ID, I18n.I.ratios(), false, elementsLoader, searchHandler).
                withConfigurable().loadElements();
    }

    private TableDataModel createPriceModel() {
        return createModel(getResult().getElement(), WNTFinderElementMapper.PRICE_ROW_MAPPER);
    }

    private TableDataModel createPerfModel() {
        return createModel(getResult().getElement(), WNTFinderElementMapper.PERF_ROW_MAPPER);
    }

    private TableDataModel createRatiosModel() {
        return createModel(getResult().getElement(), WNTFinderElementMapper.RATIOS_ROW_MAPPER);
    }

    private TableDataModel createBaseModel() {
        return createModel(getResult().getElement(), WNTFinderElementMapper.BASE_ROW_MAPPER);
    }

    private TableDataModel createEdgModel() {
        return createModel(getResult().getElement(), WNTFinderElementMapper.EDG_ROW_MAPPER);
    }
}
