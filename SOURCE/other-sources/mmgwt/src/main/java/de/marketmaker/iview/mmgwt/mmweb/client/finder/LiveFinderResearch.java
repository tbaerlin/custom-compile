package de.marketmaker.iview.mmgwt.mmweb.client.finder;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.dmxml.GISResearchFinder;
import de.marketmaker.iview.dmxml.GISResearchFinderElement;
import de.marketmaker.iview.dmxml.GISResearchFinderMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.LIST_ISSUE_DATES2;

/**
 * @author Michael Wohlfart
 */
public class LiveFinderResearch extends LiveFinder<GISResearchFinder, GISResearchFinderMetadata> {
    public static final LiveFinderResearch INSTANCE = new LiveFinderResearch();

    private static final RowMapper<GISResearchFinderElement> ROW_MAPPER = new AbstractRowMapper<GISResearchFinderElement>() {
        public Object[] mapRow(GISResearchFinderElement elem) {
            final Link link = new Link(JsUtil.escapeUrl(elem.getUrl()), "_blank", null, elem.getTitle()); // $NON-NLS$
            final String[] countries = elem.getCountry().toArray(new String[elem.getCountry().size()]);
            return new Object[]{
                    link,
                    elem.getDate(),
                    elem.getAssetClass(),
                    elem.getRecommendation(),
                    elem.getSector(),
                    countries,
            };
        }
    };

    private final String baseId = "resbase"; // $NON-NLS-0$

    private final String symbolFormElementId = "symbol"; // $NON-NLS-0$

    private LiveFinderResearch() {
        super("GIS_ResearchFinder"); // $NON-NLS$
        this.defaultSortField = "date"; // $NON-NLS$
        this.defaultSortDescending = true;
    }

    @Override
    protected List<FinderTypedMetaList> getLiveMetadata() {
        return this.block.getResult().getMetadata();
    }

    @Override
    protected ViewSpec[] getResultViewSpec() {
        return new ViewSpec[]{new ViewSpec(I18n.I.result())};
    }

    @Override
    protected boolean allowEmptyQuery() {
        return true;
    }

    @Override
    protected void addSections() {
        addSectionBase();
    }

    @Override
    protected TableDataModel createDataModel(int view) {
        return createModel(addHeader(getResult().getElement()), ROW_MAPPER);
    }

    @Override
    protected AbstractFinderView createView() {
        return new FinderResearchView<>(this);
    }

    @Override
    protected Map<String, FinderMetaList> getMetaLists() {
        final GISResearchFinderMetadata result = this.metaBlock.getResult();
        final HashMap<String, FinderMetaList> map = new HashMap<>();
        map.put("type", result.getType()); // $NON-NLS$
        map.put("country", result.getCountry()); // $NON-NLS$
        map.put("documentType", result.getDocumentType()); // $NON-NLS$
        map.put("issuer", result.getIssuer()); // $NON-NLS$
        map.put("recommendation", result.getRecommendation()); // $NON-NLS$
        map.put("sector", result.getSector()); // $NON-NLS$
        return map;
    }

    @Override
    public String getId() {
        return "LGISRES";  // $NON-NLS$
    }

    @Override
    public String getViewGroup() {
        return "finder-research";  // $NON-NLS$
    }

    @Override
    public void prepareFind(String field1, String value1, String field2, String value2) {

    }

    private List<GISResearchFinderElement> addHeader(List<GISResearchFinderElement> list) {
        if (!this.block.isResponseOk()
                || this.block.getResult().getElement() == null
                || this.block.getResult().getElement().isEmpty()) {
            return list;
        }
        final List<GISResearchFinderElement> result = new ArrayList<>();
        final List<GISResearchFinderElement> indices = this.block.getResult().getElement();
        for (GISResearchFinderElement elem : indices) {
            result.add(elem);
        }
        return result;
    }

    private void addSectionBase() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(baseId);
            int defaultOrder = 0;
            final Map<String, FinderMetaList> metaLists = getMetaLists();

            final FinderFormElements.AbstractOption luceneSearch =
                    new FinderFormElements.LiveTextOption("text", I18n.I.term(), searchHandler) // $NON-NLS$
                            .withWidth("190px")  // $NON-NLS$
                            .withConf(sectionConf, defaultOrder++);
            elements.add(luceneSearch);

            final FinderFormElements.SymbolOption symbolOption =
                    new FinderFormElements.LiveSymbolOption(symbolFormElementId, I18n.I.instrument(),
                            new String[]{"STK", "BND"}, // $NON-NLS$
                            false, // showMarketsPage
                            null,  // filterForUnderlyingsForType
                            false,  // filterForUnderlyingsOfLeveragProducts
                            searchHandler);
            symbolOption.withStyle("width160");  // $NON-NLS$
            asInstrumentDropTarget(symbolOption);
            elements.add(symbolOption.withConf(sectionConf, defaultOrder++));

            final String type = "type"; // $NON-NLS$
            final FinderFormElements.LiveMultiEnumOption typeCheckBox =
                    createLiveMultiEnum(type, I18n.I.category(), null, metaLists, type, searchHandler);
            elements.add(typeCheckBox.withConf(sectionConf, defaultOrder++));

            final String documentType = "documentType"; // $NON-NLS$
            final FinderFormElements.LiveMultiEnumOption documentTypeCheckBox =
                    createLiveMultiEnum(documentType, I18n.I.documentType(), null, metaLists, documentType, searchHandler);
            elements.add(documentTypeCheckBox.withConf(sectionConf, defaultOrder++));

            final String sector = "sector"; // $NON-NLS$
            final FinderFormElements.LiveMultiEnumOption sectorCheckBox =
                    createLiveMultiEnum(sector, I18n.I.sector(), null, metaLists, sector, searchHandler);
            elements.add(sectorCheckBox.withConf(sectionConf, defaultOrder++));

            final String issuer = "issuer"; // $NON-NLS$
            final FinderFormElements.LiveMultiEnumOption issuerCombo =
                    createLiveMultiEnum(issuer, I18n.I.issuer(), null, metaLists, issuer, searchHandler);
            elements.add(issuerCombo.withConf(sectionConf, defaultOrder++));

            elements.add(createLiveStartEndOption("date", "date", I18n.I.date(), "", // $NON-NLS$
                    LIST_ISSUE_DATES2, DateTimeUtil.PeriodMode.PAST, searchHandler).withConf(sectionConf, defaultOrder++));

            final String country = "country"; // $NON-NLS$
            final FinderFormElements.LiveMultiEnumOption countryCombo =
                    createLiveMultiEnum(country, I18n.I.country(), null, metaLists, country, searchHandler);
            elements.add(countryCombo.withConf(sectionConf, defaultOrder++));

            final String recommendation = "recommendation"; // $NON-NLS$
            final FinderFormElements.LiveMultiEnumOption recommendationCheckBox =
                    createLiveMultiEnum(recommendation, I18n.I.recommendation2(), null, metaLists, recommendation, searchHandler);
            //noinspection UnusedAssignment
            elements.add(recommendationCheckBox.withConf(sectionConf, defaultOrder++));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };

        final FinderSection section = addSection(baseId, I18n.I.baseInfo(), false, elementsLoader, searchHandler).expand().withConfigurable().loadElements();
        section.setValue(true);
    }

    public void prepareFindInstrument(final QuoteWithInstrument quote) {
        final FinderFormConfig finderFormConfig = new FinderFormConfig("symbol", this.getId()); // $NON-NLS-0$
        finderFormConfig.put(baseId, "true"); // $NON-NLS-0$
        finderFormConfig.put(symbolFormElementId, "true"); // $NON-NLS-0$
        finderFormConfig.put(symbolFormElementId + "-symbol", quote.getQuoteData().getQid()); // $NON-NLS-0$
        finderFormConfig.put(symbolFormElementId + "-name", quote.getInstrumentData().getName()); // $NON-NLS-0$

        if(this.defaultSortDescending) {
            // config: sort-section=true, sort=true, sort-item=date, sort-desc=true
            // see: de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.OrderByOption()
            finderFormConfig.put(SORT_SECTION_ID, "true");  // $NON-NLS$
            finderFormConfig.put(FinderFormKeys.SORT, "true");  // $NON-NLS$
            finderFormConfig.put(FinderFormKeys.SORT + "-item", this.defaultSortField); // $NON-NLS$
            finderFormConfig.put(FinderFormKeys.SORT + "-desc", "true"); // $NON-NLS$
        }

        prepareFind(finderFormConfig);
    }
}
