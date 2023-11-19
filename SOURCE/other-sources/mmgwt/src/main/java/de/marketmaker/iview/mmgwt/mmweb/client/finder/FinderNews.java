/*
 * FinderFND.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.google.gwt.dom.client.Element;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.dmxml.NWSSearch;
import de.marketmaker.iview.dmxml.NWSSearchElement;
import de.marketmaker.iview.dmxml.NWSSearchMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractSearchController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.VisibilityUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DzNewsRelatedOffersSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsEntrySnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsrelatedQuotesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PortraitChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsController;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PrintUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.CheckBoxGroupsOption;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.CheckBoxOption;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.Item;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.ONE_MONTH;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.ONE_WEEK;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.ONE_YEAR;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.SIX_MONTHS;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.StartEndOption;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.SymbolOption;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.THREE_MONTH;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FinderNews extends AbstractFinder<NWSSearch, NWSSearchMetadata> implements
        LinkListener<NWSSearchElement> {

    private enum Mode {
        DEFAULT,
        WITHOUT_STK_AND_CATEGORY
    }

    public static final String BASE_ID = "base"; // $NON-NLS-0$

    protected static final int DEFAULT_PAGE_SIZE = 15;

    private static final String DEF = "nws_finder"; // $NON-NLS-0$

    private static final List<Item> P_ALL
            = Arrays.asList(ONE_WEEK, ONE_MONTH, THREE_MONTH, SIX_MONTHS, ONE_YEAR);

    public static final FinderNews INSTANCE = new FinderNews();

    private final Mode mode;

    private NewsEntrySnippet entry;

    private NewsrelatedQuotesSnippet related;

    private DzNewsRelatedOffersSnippet relatedDzOffers;
    private SymbolSnippet relatedDzOffersStaticData;

    private PortraitChartSnippet chart;

    private static Mode getMode() {
        final String mode = SessionData.INSTANCE.getGuiDef("nws_finder_mode").stringValue(); // $NON-NLS-0$
        return (mode != null) ? Mode.valueOf(mode) : Mode.DEFAULT;
    }

    private FinderNews() {
        super("NWS_Finder", DEFAULT_PAGE_SIZE); // $NON-NLS-0$
        this.mode = getMode();
    }

    protected ViewSpec[] getResultViewSpec() {
        return new ViewSpec[]{new ViewSpec(I18n.I.result())};
    }

    public String getId() {
        return "NWS"; // $NON-NLS-0$
    }

    public String getViewGroup() {
        return "finder-nws"; // $NON-NLS-0$
    }

    public void prepareFind(String field1, String value1, String field2, String value2) {
    }

    public void onClick(LinkContext context, Element e) {
        this.entry.ackNewsid(((NWSSearchElement) context.data).getNewsid());
    }

    protected FinderFormConfig createFormConfig(HistoryToken historyToken) {
        final FinderFormConfig result = new FinderFormConfig("temp", getId()); // $NON-NLS$
        result.put(BASE_ID, "true"); // $NON-NLS$
        final String text = historyToken.getByNameOrIndex("s", 1); // $NON-NLS$
        if (text != null) {
            result.put("text", "true"); // $NON-NLS$
            result.put("text-text", text); // $NON-NLS$
        }
        return result;
    }

    protected void addSections() {
        final FinderSection section = addSection(BASE_ID, "", false); // $NON-NLS-0$
        section.setAlwaysExpanded();
        section.add(new FinderFormElements.NewsSearchTextOption("text", I18n.I.searchString()).withWidth("400px"));  // $NON-NLS-0$ $NON-NLS-1$
//        TODO: replace line above by next line so that search only in headlines is possible 
//        section.add(new TwoFieldTextOption("text", "text", "Suchbegriff", "headline", "nur Headline").withWidth("400px"));

        section.add(new CheckBoxOption("agency", I18n.I.agency(), null).withEqualColumns());  // $NON-NLS-0$
//        section.add(new CheckBoxOption("language", "Sprache", null).withEqualColumns());
        final StartEndOption dateOption = new StartEndOption("date", "date", I18n.I.period(), "", P_ALL);  // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        dateOption.setDefault(new MmJsDate().addDays(-7), new MmJsDate());
        section.add(dateOption);

        if (this.mode == Mode.WITHOUT_STK_AND_CATEGORY) {
            return;
        }

        final SymbolOption option = new SymbolOption(FinderFormKeys.IID, I18n.I.instrument(),
                AbstractSearchController.DEFAULT_COUNT_TYPES, false);
        section.add(option);
        asInstrumentDropTarget(option);
        final FinderSection topics = addSection("topics", I18n.I.topics(), false);  // $NON-NLS-0$
        // using empty map since the translation is coming from the backend
        topics.add(new CheckBoxGroupsOption("topic", I18n.I.topics(), null, Collections.<String, String>emptyMap()));  // $NON-NLS-0$
    }

    protected TableDataModel createDataModel(int view) {
        if (this.block.getResult().getElement().isEmpty()) {
            this.entry.clear();
            return DefaultTableDataModel.NULL;
        }
        final NWSSearchElement element = this.block.getResult().getElement().get(0);
        this.entry.ackNewsid(element.getNewsid());
        return DefaultTableDataModel.create(this.block.getResult().getElement(), new AbstractRowMapper<NWSSearchElement>() {
            public Object[] mapRow(NWSSearchElement e) {
                return new Object[]{
                        e.getDate(),
                        new LinkContext<>(FinderNews.this, e),
                        e.getSource()
                };
            }
        });
    }

    protected void onMetaBlockResult() {
        super.onMetaBlockResult();

        final SnippetsController controller =
                SnippetsFactory.createFlexController((ContentContainer) this.view, DEF);
        this.entry = (NewsEntrySnippet) controller.getSnippet("ne"); // $NON-NLS-0$
        this.related = (NewsrelatedQuotesSnippet) controller.getSnippet("nrq"); // $NON-NLS-0$
        this.chart = (PortraitChartSnippet) controller.getSnippet("chart"); // $NON-NLS-0$
        this.relatedDzOffers = (DzNewsRelatedOffersSnippet) controller.getSnippet("nrdzo"); // $NON-NLS$
        this.relatedDzOffersStaticData = (SymbolSnippet) controller.getSnippet("nrdzos"); // $NON-NLS$

        if (this.entry != null && this.related != null && this.chart != null) {
            this.entry.addSymbolListSnippet(this.related);
            this.entry.addSymbolListSnippet(this.chart);
            this.related.addSymbolSnippet(this.chart);

            if(this.relatedDzOffers != null && this.relatedDzOffersStaticData != null) {
                this.entry.addSymbolListSnippet(this.relatedDzOffers);
                this.relatedDzOffers.addSymbolSnippet(this.relatedDzOffersStaticData);

                EventBusRegistry.get().addHandler(VisibilityUpdatedEvent.getType(), this.related);
                EventBusRegistry.get().addHandler(VisibilityUpdatedEvent.getType(), this.relatedDzOffers);
            }

        }
    }

    protected AbstractFinderView createView() {
        return new FinderNewsView(this);
    }

    protected Map<String, FinderMetaList> getMetaLists() {
        final NWSSearchMetadata result = this.metaBlock.getResult();
        final HashMap<String, FinderMetaList> map = new HashMap<>();
        final List<FinderTypedMetaList> metaListList = result.getList();
        if (metaListList != null) {
            addMetaLists(map, metaListList, "topic"); // $NON-NLS-0$
            addMetaList(map, metaListList, "agency"); // $NON-NLS-0$
            addMetaList(map, metaListList, "language"); // $NON-NLS-0$
        }
        return map;
    }

    @Override
    public String getPrintHtml() {
        return PrintUtil.getNewsPrintHtml(this.entry, this.related, this.chart, this.relatedDzOffers, this.relatedDzOffersStaticData);
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        final String[] newsIds = getNewsIds();
        if (newsIds == null) {
            return null;
        }
        final StringBuilder sbQuery = new StringBuilder();
        String divider = "id IN ("; // $NON-NLS-0$
        for (String newsId : newsIds) {
            sbQuery.append(divider).append('\'').append(newsId).append('\'');
            divider = ","; // $NON-NLS-0$
        }
        sbQuery.append(")"); // $NON-NLS-0$

        final Map<String, String> map = new HashMap<>();
        map.put("count", String.valueOf(newsIds.length)); // $NON-NLS-0$
        map.put("offset", "0"); // $NON-NLS$
        map.put("query", sbQuery.toString()); // $NON-NLS-0$
        return new PdfOptionSpec("newssearch.pdf", map, null); // $NON-NLS-0$
    }

    String[] getNewsIds() {
        if (!this.block.isResponseOk()) {
            return null;
        }
        final List<NWSSearchElement> elementList = this.block.getResult().getElement();
        if (elementList.isEmpty()) {
            return null;
        }
        final String[] newsIds = new String[elementList.size()];
        for (int i = 0; i < newsIds.length; i++) {
            newsIds[i] = elementList.get(i).getNewsid();
        }
        return newsIds;
    }
}
