/*
 * LiveFinderNews.java
 *
 * Created on 9/8/14 9:21 AM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.dmxml.NWSSearch;
import de.marketmaker.iview.dmxml.NWSSearchElement;
import de.marketmaker.iview.dmxml.NWSSearchMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractSearchController;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.VisibilityUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DzNewsRelatedOffersSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsEntrySnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsrelatedQuotesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PortraitChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsController;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PrintUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.*;

/**
 * @author Stefan Willenbrock
 */
public class LiveFinderNews extends LiveFinder<NWSSearch, NWSSearchMetadata> implements
        LinkListener<NWSSearchElement> {

    static class MultipleTextOption extends FinderFormElements.LiveTextOption {
        public MultipleTextOption(String field, String label, SearchHandler searchHandler) {
            super(field, label, searchHandler);
        }

        protected static String getQuery(String inputText, String fieldName) {
            final StringBuilder sb = new StringBuilder();

            final List<String> split = StringUtil.split(inputText, ' ');
            //final List<String> split = Arrays.asList(inputText.split("\\s+")); // $NON-NLS-0$
            Iterator<String> it = split.iterator();

            int count = 0;
            while (it.hasNext()) {
                final String s = it.next();
                if (s.length() == 0)
                    continue;
                if (count++ != 0) {
                    sb.append(" OR "); // $NON-NLS-0$
                }
                sb.append(fieldName + "==" + quote(s)); // $NON-NLS-0$
            }

            final String result = sb.toString();
            return result.length() == 0 ? null : result ;
        }


        @Override
        protected String doGetQuery() {
            return getQuery(this.textBox.getText(), getFieldname());
        }
    }

    final RowMapper<NWSSearchElement> baseRowMapper = new AbstractRowMapper<NWSSearchElement>() {
        public Object[] mapRow(NWSSearchElement e) {
            return new Object[]{
                    e.getDate(),
                    new LinkContext<>(LiveFinderNews.this, e),
                    e.getSource()
            };
        }
    };

    private enum Mode {
        DEFAULT,
        WITHOUT_STK_AND_CATEGORY
    }

    public final static String BASE_ID = "base"; // $NON-NLS-0$

    public final static String TOPICS_ID = "topics"; // $NON-NLS-0$

    public final static String PROVIDERCODE_ID = "providercode"; // $NON-NLS-0$

    public final static String SEMANTIC_ID = "semantic"; // $NON-NLS-0$

    private static final List<FinderFormElements.Item> P_ALL = Arrays.asList(ONE_WEEK, ONE_MONTH, THREE_MONTH, SIX_MONTHS, ONE_YEAR);

    public final static LiveFinderNews INSTANCE = new LiveFinderNews();

    private static final String DEF = "nws_finder"; // $NON-NLS-0$

    private final Mode mode;

    private NewsEntrySnippet entry;

    private NewsrelatedQuotesSnippet related;

    private DzNewsRelatedOffersSnippet relatedDzOffers;

    private SymbolSnippet relatedDzOffersStaticData;

    private PortraitChartSnippet chart;

    private FinderSection topicSection;

    private FinderSection providerSection;

    protected LiveFinderNews() {
        super("NWS_Finder"); // $NON-NLS-0$
        this.mode = getMode();
    }

    @Override
    protected List<FinderTypedMetaList> getLiveMetadata() {
        return new ArrayList<>();
    }

    @Override
    protected ViewSpec[] getResultViewSpec() {
        return new ViewSpec[]{new ViewSpec(I18n.I.result())};
    }

    @Override
    protected void addSections() {
        addSectionBase();
        if (FeatureFlags.Feature.VWD_RELEASE_2015.isEnabled())
            addSectionSemantic();
        addSectionTopics();
        if (FeatureFlags.Feature.VWD_RELEASE_2015.isEnabled())
            addSectionProvidercodeHidden();

        this.sortFields.clear();
    }

    private void addSectionBase() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] baseConf = SectionConfigUtil.getSectionConf(BASE_ID);
            int defaultOrder = 0;
            final FinderFormElements.NewsSearchLiveTextOption newsSearchLiveTextOption = new FinderFormElements.NewsSearchLiveTextOption(FinderFormKeys.TEXT, I18n.I.searchString(), searchHandler);
            newsSearchLiveTextOption.withWidth("190px"); // $NON-NLS-0$
            elements.add(newsSearchLiveTextOption.withConf(baseConf, defaultOrder++));
            final FinderFormElements.LiveMultiEnumOption liveMultiEnumOption = createSortableLiveMultiEnum(FinderFormKeys.AGENCY, I18n.I.agency(), "", getMetaLists(), FinderFormKeys.AGENCY, searchHandler); // $NON-NLS$
            elements.add(liveMultiEnumOption.withConf(baseConf, defaultOrder++));
            final FinderFormElements.LiveStartEndOption dateOption = new FinderFormElements.LiveStartEndOption(FinderFormKeys.DATE, FinderFormKeys.DATE, I18n.I.period(), "", P_ALL, DateTimeUtil.PeriodMode.PAST, searchHandler);
            dateOption.setDefault(new MmJsDate().addDays(-7), new MmJsDate());
            elements.add(dateOption.withConf(baseConf, defaultOrder++));

            if (mode != Mode.WITHOUT_STK_AND_CATEGORY) {

                final FinderFormElements.LiveSymbolOption option = new FinderFormElements.LiveSymbolOption(FinderFormKeys.IID, I18n.I.instrument(), AbstractSearchController.DEFAULT_COUNT_TYPES,
                        false, null, false, searchHandler);
                elements.add(option.withConf(baseConf));

                addOnRenderCommand(new Command() {
                    public void execute() {
                        final DropTarget dt = new DropTarget(option.getName()) {
                            @Override
                            protected void onDragDrop(DNDEvent dndEvent) {
                                super.onDragDrop(dndEvent);
                                option.setParameters((QuoteWithInstrument) dndEvent.getData());
                            }
                        };
                        dt.setGroup("ins"); // $NON-NLS-0$
                        dt.setOverStyle("drag-ok"); // $NON-NLS-0$
                    }
                });
            }

            elements.addAll(handleClones(baseConf, elements));
            return orderBySectionConf(elements, baseConf);
        };

        addSection(BASE_ID, I18n.I.baseInfo(), false, elementsLoader, searchHandler)
                .expand().withConfigurable().loadElements();
    }

    private void addSectionSemantic() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();

            final FinderFormElements.BooleanOption semanticOption = new FinderFormElements.BooleanOption("providerSemantic", I18n.I.providerSemantic(), false); // $NON-NLS-0$
            semanticOption.addClickHandler(clickEvent -> {
                if (semanticOption.getValue()) {
                    this.topicSection.reset();
                    this.topicSection.hide();
                    this.providerSection.show();

                }
                else {
                    this.providerSection.reset();
                    this.providerSection.hide();
                    this.topicSection.show();
                }
            });
            elements.add(semanticOption.withConf(null, 0));
            semanticOption.setSilent();

            elements.addAll(handleClones(null, elements));
            return orderBySectionConf(elements, null);
        };

        addSection(SEMANTIC_ID, I18n.I.classification(), true, elementsLoader, searchHandler).expand().loadElements();
    }

    private void addSectionTopics() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] topics = SectionConfigUtil.getSectionConf(TOPICS_ID);
            int defaultOrder = 0;
            final FinderFormElements.LiveCheckBoxGroupsOption checkBoxGroupsOption = new FinderFormElements.LiveCheckBoxGroupsOption(FinderFormKeys.TOPICS, I18n.I.topics(), null, Collections.<String, String>emptyMap(), searchHandler);
            elements.add(checkBoxGroupsOption.withConf(topics, defaultOrder++));

            elements.addAll(handleClones(topics, elements));
            return orderBySectionConf(elements, topics);
        };

        this.topicSection = addSection(TOPICS_ID, I18n.I.topics(), true, elementsLoader, searchHandler)
                .expand().loadElements();
    }

    private void addSectionProvidercodeHidden() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();

            final MultipleTextOption textOption = new MultipleTextOption(FinderFormKeys.PROVIDERCODE, I18n.I.providercodes(), searchHandler);
            textOption.withWidth("190px"); // $NON-NLS-0$
            elements.add(textOption.withConf(null, 0));

            elements.addAll(handleClones(null, elements));
            return orderBySectionConf(elements, null);
        };

        this.providerSection = addSection(PROVIDERCODE_ID, I18n.I.providercodes(), true, elementsLoader, searchHandler)
                .expand().hideDeferred().loadElements();
    }

    @Override
    protected TableDataModel createDataModel(int view) {
        switch (view) {
            default:
                return createBaseModel();
        }
    }

    private TableDataModel createBaseModel() {
        return createModel(getResult().getElement(), baseRowMapper);
    }

    @Override
    protected AbstractFinderView createView() {
        return new LiveFinderNewsView(this);
    }

    @Override
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
    public String getId() {
        return "LNWS"; // $NON-NLS-0$
    }

    @Override
    public String getViewGroup() {
        return "finder-nws"; // $NON-NLS-0$
    }

    @Override
    public void prepareFind(String field1, String value1, String field2, String value2) {

    }

    @Override
    protected FinderFormConfig createFormConfig(HistoryToken historyToken) {
        final FinderFormConfig result = new FinderFormConfig("temp", getId()); // $NON-NLS$
        result.put(BASE_ID, "true"); // $NON-NLS-0$
        final String text = historyToken.getByNameOrIndex("s", 1); // $NON-NLS$
        if (text != null) {
            result.put(FinderFormKeys.TEXT, "true"); // $NON-NLS-0$
            result.put(FinderFormKeys.TEXT + "-text", text); // $NON-NLS-0$
        }
        return result;
    }

    @Override
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

            if (this.relatedDzOffers != null && this.relatedDzOffersStaticData != null) {
                this.entry.addSymbolListSnippet(this.relatedDzOffers);
                this.relatedDzOffers.addSymbolSnippet(this.relatedDzOffersStaticData);

                EventBusRegistry.get().addHandler(VisibilityUpdatedEvent.getType(), this.related);
                EventBusRegistry.get().addHandler(VisibilityUpdatedEvent.getType(), this.relatedDzOffers);
            }
        }
    }

    @Override
    protected void onResult() {
        super.onResult();
        this.entry.ackNewsid(null);
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

    private static Mode getMode() {
        final String mode = SessionData.INSTANCE.getGuiDef("nws_finder_mode").stringValue(); // $NON-NLS-0$
        return (mode != null) ? Mode.valueOf(mode) : Mode.DEFAULT;
    }

    private List<NWSSearchElement> getElementList() {
        if (!this.block.isResponseOk()) {
            return null;
        }
        final List<NWSSearchElement> elementList = this.block.getResult().getElement();
        if (elementList.isEmpty()) {
            return null;
        }
        return elementList;
    }

    String[] getNewsIds() {
        final List<NWSSearchElement> elementList = getElementList();
        if (elementList == null) {
            return null;
        }
        final String[] newsIds = new String[elementList.size()];
        for (int i = 0; i < newsIds.length; i++) {
            newsIds[i] = elementList.get(i).getNewsid();
        }
        return newsIds;
    }

    @Override
    public void onClick(LinkContext<NWSSearchElement> context, Element e) {
        this.entry.ackNewsid((context.data).getNewsid());
    }
}
