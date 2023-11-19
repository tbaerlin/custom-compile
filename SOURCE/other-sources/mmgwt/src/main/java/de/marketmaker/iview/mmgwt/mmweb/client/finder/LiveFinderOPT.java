/*
 * FinderFND.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.dmxml.OPTFinder;
import de.marketmaker.iview.dmxml.OPTFinderMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LiveFinderOPT extends LiveFinder<OPTFinder, OPTFinderMetadata> {
    public static final LiveFinderOPT INSTANCE = new LiveFinderOPT();

    public static final String BASE_ID = "optbase"; // $NON-NLS$

    public static final String RATIOS_ID = "optratios"; // $NON-NLS$

    public static final String UNDERLYING_ID = "optunderlying"; // $NON-NLS$

    public static final String FIXED_TOP_ID = "optfixedtop"; // $NON-NLS$

    private final LiveFromToTextOption remainingMonthsOption
            = new LiveFromToTextOption(FinderFormKeys.REMAINING, "expirationDate", I18n.I.remainingTime(), I18n.I.months(), searchHandler) {  // $NON-NLS$
        protected String toQueryValue(String input, boolean query) {
            return toQueryValueMonths(input, 0, 120, query);
        }
    };

    private LiveFinderOPT() {
        super("OPT_Finder"); // $NON-NLS-0$
        this.sortFields.add(new Item(I18n.I.name(), "name"));  // $NON-NLS-0$
    }

    protected ViewSpec[] getResultViewSpec() {
        return new ViewSpec[]{new ViewSpec(I18n.I.result()), new ViewSpec(I18n.I.greeks())};
    }

    public String getId() {
        return "LOPT"; // $NON-NLS-0$
    }

    public String getViewGroup() {
        return "finder-OPT"; // $NON-NLS-0$
    }

    public void prepareFind(String field1, String value1, String field2, String value2) {
    }

    private void addFixedTopSection() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<FinderFormElement>();

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

    private void addUnderlyingSection() {
        final FinderSection underlying = addSection(UNDERLYING_ID, I18n.I.underlying(), true);
        final SymbolOption underlyingOption = createUnderlyingOption();
        underlying.add(underlyingOption);
        underlyingOption.setAlwaysEnabled();
    }

    private void addBaseSection() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<FinderFormElement>();
                final String[] sectionConf = SectionConfigUtil.getSectionConf(BASE_ID);
                int defaultOrder = 0;

                elements.add(createOptionTypeElement(getOptionTypeItems()).withConf(sectionConf, defaultOrder++));

                elements.add(createLiveFromToTextOption("strike", I18n.I.strike(), searchHandler) // $NON-NLS$
                        .withConf(sectionConf, defaultOrder++));

                elements.add(new LiveStartEndOption(FinderFormKeys.EXPIRATION_DATE, FinderFormKeys.EXPIRATION_DATE, I18n.I.maturity2(),
                        "", LIST_EXPIRATION_DATES, DateTimeUtil.PeriodMode.FUTURE, searchHandler)
                        .withConf(sectionConf, defaultOrder));
                sortFields.add(new Item(I18n.I.maturity2(), "expirationDate"));  // $NON-NLS-0$

                elements.add(remainingMonthsOption.withConf(sectionConf));
                elements.add(createTotalVolumeGtZero(searchHandler).withConf(sectionConf));

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

    private void addRatiosSection() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<FinderFormElement>();
                final String[] sectionConf = SectionConfigUtil.getSectionConf(RATIOS_ID);
                int defaultOrder = 0;

                elements.add(createLiveFromToTextOption("delta", I18n.I.delta(), searchHandler).withConf(sectionConf, defaultOrder++));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("gamma", I18n.I.gamma(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("omega", I18n.I.omega(), searchHandler).withConf(sectionConf, defaultOrder));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("theta", I18n.I.theta(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("rho", I18n.I.rho(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$
                elements.add(createLiveFromToTextOption("vega", I18n.I.vega(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$


                elements.addAll(handleClones(sectionConf, elements));
                return orderBySectionConf(elements, sectionConf);
            }
        };
        addSection(RATIOS_ID, I18n.I.ratios(), false, elementsLoader, searchHandler)
                .withConfigurable().loadElements();
    }

    protected void addSections() {
        addFixedTopSection();
        addUnderlyingSection();
        addBaseSection();
        addRatiosSection();
    }

    /*
    public LiveListBoxOption createOptionTypeElement(final ArrayList<Item> items) {
        return new LiveListBoxOption("optionType", "optionType", I18n.I.optionType(), items, "", searchHandler); // $NON-NLS$
    }
    */

    public static RadioOption createOptionTypeElement(final ArrayList<Item> items) {
        return new RadioOption("optionType", I18n.I.optionType(), items);  // $NON-NLS-0$
    }

    public static ArrayList<Item> getOptionTypeItems() {
        final ArrayList<Item> result = new ArrayList<Item>();
        result.add(new Item("C", "C")); // $NON-NLS-0$ $NON-NLS-1$
        result.add(new Item("P", "P")); // $NON-NLS-0$ $NON-NLS-1$
        return result;
    }

    protected TableDataModel createDataModel(int view) {
        switch (view) {
            case 0:
                return createBaseModel();
            case 1:
                return createGreekModel();
            default:
                return null;
        }
    }

    private TableDataModel createGreekModel() {
        return createModel(getResult().getElement(), OPTFinderElementMapper.GREEK_ROW_MAPPER);
    }

    private TableDataModel createBaseModel() {
        return createModel(getResult().getElement(), OPTFinderElementMapper.BASE_ROW_MAPPER);
    }

    protected AbstractFinderView createView() {
        return new FinderOPTView<LiveFinderOPT>(this);
    }

    protected Map<String, FinderMetaList> getMetaLists() {
        final OPTFinderMetadata result = this.metaBlock.getResult();
        final HashMap<String, FinderMetaList> map = new HashMap<String, FinderMetaList>();
        map.put("optionType", null); // $NON-NLS$
        map.put("underlyingVwdcode", result.getUnderlyingVwdcode()); // $NON-NLS$
        return map;
    }

    @Override
    protected List<FinderTypedMetaList> getLiveMetadata() {
        return this.block.getResult().getMetadata();
    }
}
