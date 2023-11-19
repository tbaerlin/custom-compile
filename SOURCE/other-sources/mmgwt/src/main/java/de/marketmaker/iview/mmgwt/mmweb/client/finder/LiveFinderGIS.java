package de.marketmaker.iview.mmgwt.mmweb.client.finder;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.dmxml.GISFinder;
import de.marketmaker.iview.dmxml.GISFinderElement;
import de.marketmaker.iview.dmxml.GISFinderMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.history.EmptyContext;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.LIST_EXPIRATION_DATES;

/**
 * @author Michael Wohlfart
 */
public class LiveFinderGIS extends LiveFinder<GISFinder, GISFinderMetadata> {
    public static final LiveFinderGIS INSTANCE = new LiveFinderGIS();

    private static final String BASE_ID = "gisbase"; // $NON-NLS$

    private static final RowMapper<GISFinderElement> ROW_MAPPER = new AbstractRowMapper<GISFinderElement>() {
        public Object[] mapRow(GISFinderElement elem) {
            final Price price = Price.create(elem);
            final QuoteWithInstrument qwi = getQuoteWithInstrument(elem);
            return new Object[]{
                    qwi,
                    elem,
                    elem.getWkn(),
                    qwi,
                    elem.getExpiration(),
                    elem.getReferenceDate(),
                    price.getLastPrice().getPrice(),
                    price.getChangeNet(),
                    price.getChangePercent(),
                    elem.getDiffToUnderlyingPercent(),
                    elem.getDiffToBarrierPercent(),
                    elem.getRendite(),
                    createHinweisString(elem),
                    elem.getRisikoklasse(),
                    elem.getBonibrief(),
                    elem.getBonifikationstyp()
            };
        }
    };

    private LiveFinderGIS() {
        super("GIS_Finder"); // $NON-NLS$
        this.defaultSortField = "indexPosition"; // $NON-NLS$
        this.sortFields.add(new FinderFormElements.Item(I18n.I.index(), "indexPosition"));  // $NON-NLS$
        this.sortFields.add(new FinderFormElements.Item("WKN", "wkn"));  // $NON-NLS$
    }

    @Override
    protected ViewSpec[] getResultViewSpec() {
        return new ViewSpec[]{new ViewSpec(I18n.I.result())};
    }

    @Override
    public String getId() {
        return "LGIS";  // $NON-NLS$
    }

    @Override
    public String getViewGroup() {
        return "finder-gis";  // $NON-NLS$
    }

    @Override
    public void prepareFind(String field1, String value1, String field2, String value2) {

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
    protected List<FinderTypedMetaList> getLiveMetadata() {
        return this.block.getResult().getMetadata();
    }

    @Override
    protected AbstractFinderView createView() {
        return new FinderGISView<LiveFinderGIS>(this);
    }

    @Override
    protected TableDataModel createDataModel(int view) {
        return createModel(addHeader(getResult().getElement()), ROW_MAPPER);
    }

    private static QuoteWithInstrument getQuoteWithInstrument(GISFinderElement elem) {
        return QuoteWithInstrument
                .createQuoteWithInstrument(elem.getInstrumentdata(), elem.getQuotedata(), elem.getBezeichnung())
                .withHistoryContext(EmptyContext.create(I18n.I.searchResults()));
    }


    private List<GISFinderElement> addHeader(List<GISFinderElement> list) {
        if (!this.block.isResponseOk()
                || this.block.getResult().getElement() == null
                || this.block.getResult().getElement().isEmpty()) {
            return list;
        }
        List<GISFinderElement> result = new ArrayList<GISFinderElement>();
        final List<GISFinderElement> indices = this.block.getResult().getElement();
        for (GISFinderElement elem : indices) {
            result.add(elem);
        }
        return result;
    }

    @Override
    protected Map<String, FinderMetaList> getMetaLists() {
        final GISFinderMetadata result = this.metaBlock.getResult();
        final HashMap<String, FinderMetaList> map = new HashMap<String, FinderMetaList>();
        map.put("type", result.getType()); // $NON-NLS$
        map.put("offertenkategorie", result.getOffertenkategorie()); // $NON-NLS$
        map.put("risikoklasse", result.getRisikoklasse()); // $NON-NLS$
        map.put("bonibrief", result.getBonibrief()); // $NON-NLS$
        map.put("bonifikationstyp", result.getBonifikationstyp()); // $NON-NLS$
        map.put("bonifikation", result.getBonifikation()); // $NON-NLS$
        map.put("underlying", result.getUnderlying()); // $NON-NLS$
        return map;
    }

    public static String createHinweisString(GISFinderElement elem) {
        String value = "";
        final String hinweise = elem.getHinweise();
        final String sonderheit = elem.getSonderheit();

        if (StringUtil.hasText(hinweise)) {
            value += SafeHtmlUtils.htmlEscape(hinweise);
        }
        if (StringUtil.hasText(hinweise) && StringUtil.hasText(sonderheit)) {
            value += "<br/>";  // $NON-NLS$
        }
        if (StringUtil.hasText(sonderheit)) {
            value += SafeHtmlUtils.htmlEscape(sonderheit);
        }

        return value;
    }

    private void addSectionBase() {

        final DataLoader<List<FinderFormElement>> elementsLoader = new DataLoader<List<FinderFormElement>>() {

            public List<FinderFormElement> loadData() {
                final List<FinderFormElement> elements = new ArrayList<FinderFormElement>();
                final String[] sectionConf = SectionConfigUtil.getSectionConf(BASE_ID);
                int defaultOrder = 0;
                final Map<String, FinderMetaList> metaLists = getMetaLists();

                final DynamicSearchHandler topProduktHandler = new DynamicSearchHandler();

                final FinderFormElements.AbstractOption topProdukt =
                        new FinderFormElements.LiveBooleanOption("topProdukt", I18n.I.gisProduktschaufenster(), topProduktHandler) // $NON-NLS$
                                .withConf(sectionConf, defaultOrder++);
                elements.add(topProduktHandler.withElement(topProdukt));

                final DynamicSearchHandler kapitalmarktFavoritHandler = new DynamicSearchHandler();
                final FinderFormElements.AbstractOption kapitalmarktFavorit =
                        new FinderFormElements.LiveBooleanOption("kapitalmarktFavorit", I18n.I.capitalMarketFavorites(), kapitalmarktFavoritHandler) // $NON-NLS$
                                .withConf(sectionConf, defaultOrder++);
                kapitalmarktFavorit.setValue(true);
                elements.add(kapitalmarktFavoritHandler.withElement(kapitalmarktFavorit));

                final String offertenkategorie = "offertenkategorie"; // $NON-NLS$
                final FinderFormElements.LiveMultiEnumOption offertenkategorieSelectBox =
                        createSortableLiveMultiEnum(offertenkategorie, I18n.I.offerKind(), null, metaLists, offertenkategorie, new DynamicSearchHandler())
                                .withoutMatchPrefix();
                elements.add(offertenkategorieSelectBox.withConf(sectionConf, defaultOrder++));

                elements.add(
                        createLiveFromToTextOption("risikoklasse", "risikoklasse", I18n.I.riskClass(), "", searchHandler) // $NON-NLS$
                                .withConf(sectionConf, defaultOrder++));

                elements.add(
                        createLiveFromToTextOption("rendite", "rendite", I18n.I.yield(), "%", searchHandler) // $NON-NLS$
                                .withConf(sectionConf, defaultOrder++));

                final String type = "type"; // $NON-NLS$
                final FinderFormElements.LiveMultiEnumOption typeCheckBox =
                        createSortableLiveMultiEnum(type, I18n.I.offerCategory(), null, metaLists, type, new DynamicSearchHandler())
                                .withoutMatchPrefix();
                elements.add(typeCheckBox.withConf(sectionConf, defaultOrder++));

                final FinderFormElements.LiveMultiEnumOption underlyingSelectBox =
                        createSortableLiveMultiEnum("underlyingIid", I18n.I.underlyingName(), null, metaLists, "underlying", new DynamicSearchHandler())  // $NON-NLS$
                                .withoutMatchPrefix();
                elements.add(underlyingSelectBox.withConf(sectionConf, defaultOrder++));

                elements.add(createLiveStartEndOption("expiration", "expiration", I18n.I.maturity2(), "", // $NON-NLS$
                        LIST_EXPIRATION_DATES, DateTimeUtil.PeriodMode.FUTURE, searchHandler).withConf(sectionConf, defaultOrder++));

                elements.add(
                        createLiveFromToTextOption("coupon", "coupon", I18n.I.coupon(), "%", searchHandler) // $NON-NLS$
                                .withConf(sectionConf, defaultOrder++));

                elements.addAll(handleClones(sectionConf, elements));
                return orderBySectionConf(elements, sectionConf);
            }

        };

        final FinderSection section = addSection(BASE_ID, I18n.I.baseInfo(), false, elementsLoader, searchHandler).expand().withConfigurable().loadElements();
        section.setValue(true);
    }

}
