/*
 * FinderFND.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.marketmaker.iview.dmxml.FUTFinder;
import de.marketmaker.iview.dmxml.FUTFinderElement;
import de.marketmaker.iview.dmxml.FUTFinderMetadata;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LiveFinderFUT extends LiveFinder<FUTFinder, FUTFinderMetadata> {

    static final RowMapper<FUTFinderElement> ROW_MAPPER = new AbstractRowMapper<FUTFinderElement>() {
        public Object[] mapRow(FUTFinderElement e) {
            return new Object[]{
                    createQuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata()),
                    e.getExpirationDate(),
                    e.getPrice(),
                    e.getVolumeTrade(),
                    e.getChangeNet(),
                    e.getChangePercent(),
                    e.getVolume(),
                    e.getBid(),
                    e.getAsk(),
                    e.getDate(),
                    e.getPreviousSettlement()
            };
        }
    };

    public static final LiveFinderFUT INSTANCE = new LiveFinderFUT();

    public static final String BASE_ID = "futbase"; // $NON-NLS$

    public static final String FIXED_TOP_ID = "futfixedtop"; // $NON-NLS$

    public static final String UNDERLYING_ID = "futunderlying"; // $NON-NLS$

    private final LiveFromToTextOption remainingMonthsOption
            = new LiveFromToTextOption(FinderFormKeys.REMAINING, "expirationDate", I18n.I.remainingTime(), I18n.I.months(), searchHandler) {  // $NON-NLS$
        protected String toQueryValue(String input, boolean query) {
            return toQueryValueMonths(input, 0, 120, query);
        }
    };

    private LiveFinderFUT() {
        super("FUT_Finder"); // $NON-NLS-0$
        this.sortFields.add(new Item(I18n.I.name(), "name"));  // $NON-NLS-0$
    }

    protected ViewSpec[] getResultViewSpec() {
        return new ViewSpec[]{new ViewSpec(I18n.I.result())};
    }

    public String getId() {
        return "LFUT"; // $NON-NLS-0$
    }

    public String getViewGroup() {
        return "finder-FUT"; // $NON-NLS-0$
    }

    public void prepareFind(String field1, String value1, String field2, String value2) {
    }

    protected void addSections() {
        addFixedTopSection();
        addSectionUnderlying();
        addSectionBase();
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

    private void addSectionBase() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<FinderFormElement>();
                final String[] sectionConf = SectionConfigUtil.getSectionConf(BASE_ID);
                int defaultOrder = 0;

                elements.add(createLiveStartEndOption(FinderFormKeys.EXPIRATION_DATE, FinderFormKeys.EXPIRATION_DATE, I18n.I.maturity2(), "", // $NON-NLS$
                        LIST_EXPIRATION_DATES, DateTimeUtil.PeriodMode.FUTURE, searchHandler).withConf(sectionConf, defaultOrder++));
                elements.add(remainingMonthsOption.withConf(sectionConf, defaultOrder));

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


    private void addSectionUnderlying() {
        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {
            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<FinderFormElement>();

                elements.add(createUnderlyingOption());
                return elements;
            }
        };
        addSection(UNDERLYING_ID, I18n.I.underlyingInstrument(), true, elementsLoader, searchHandler)
                .loadElements();
    }

    protected TableDataModel createDataModel(int view) {
        return createModel(getResult().getElement(), ROW_MAPPER);
    }

    protected AbstractFinderView createView() {
        return new FinderFUTView<LiveFinderFUT>(this);
    }

    protected Map<String, FinderMetaList> getMetaLists() {
        final FUTFinderMetadata result = this.metaBlock.getResult();
        return Collections.singletonMap("underlyingVwdcode", result.getUnderlyingVwdcode()); // $NON-NLS-0$
    }

    @Override
    protected List<FinderTypedMetaList> getLiveMetadata() {
        return this.block.getResult().getMetadata();
    }
}
