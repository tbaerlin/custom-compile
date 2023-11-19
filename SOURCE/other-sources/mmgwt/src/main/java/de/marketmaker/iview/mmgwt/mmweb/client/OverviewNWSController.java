/*
 * OverviewNWSController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModelImpl;
import de.marketmaker.iview.mmgwt.mmweb.client.view.MultiContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author mdick
 */
public class OverviewNWSController extends AbstractPageController {
    private static final String TOPIC_PARAM = "topic"; // $NON-NLS$

    private static final NewsTopic ALL_NEWS_TOPIC = newsTopic("ALL", "ALL", I18n.I.all()); // $NON-NLS$

    private static final List<NewsTopic> DEFAULT_NEWS_TOPICS = Arrays.asList(
            ALL_NEWS_TOPIC,
            newsTopic("MONE", "6W", I18n.I.marketReports()),
            newsTopic("HOTS", "1H", I18n.I.hotStories()),
            newsTopic("FLAS", "1P", I18n.I.adHoc()),
            newsTopic("COMD", "4D", I18n.I.companys()),
            newsTopic("FUND", "6F", I18n.I.funds()),
            newsTopic("DERI", "6O", I18n.I.derivatives()),
            newsTopic("CURR", "6D", I18n.I.currencies1()),
            newsTopic("BOND", "6A", I18n.I.bonds()),
            newsTopic("1D", "1D", I18n.I.fixedDates()),
            newsTopic("POLI", "2A", I18n.I.politics()),
            newsTopic("ECON", "ECON", I18n.I.economicCycle()),
            newsTopic("SPOR", "19S", I18n.I.sports()));

    private String controllerId;

    private final NewsTopicController newsTopicController;

    private final List<NewsTopic> topics;

    // only present in legacy view!
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<IndexedViewSelectionModel> optionalViewSelectionModel;

    public OverviewNWSController(ContentContainer contentContainer) {
        super(contentContainer);

        this.topics = getNewsTopics();

        final ContentContainer view;
        if (SessionData.isAsDesign()) {
            view = contentContainer;
            this.optionalViewSelectionModel = Optional.empty();
        }
        else {
            final MultiContentView multiContentView = new MultiContentView(contentContainer);
            final String[] viewNames = toViewNames(this.topics);

            final ViewSpec[] viewSpec = new ViewSpec[viewNames.length];
            for (int i = 0; i < viewNames.length; i++) {
                viewSpec[i] = new ViewSpec(viewNames[i]);
            }
            final IndexedViewSelectionModelImpl viewSelectionModel =
                    new IndexedViewSelectionModelImpl(this::onViewChanged, viewSpec, 0, "overview-nws"); // $NON-NLS$
            this.optionalViewSelectionModel = Optional.of(viewSelectionModel);
            multiContentView.init(viewSelectionModel);
            view = multiContentView;
        }

        this.newsTopicController = new NewsTopicController(view);
    }

    private static List<NewsTopic> getNewsTopics() {
        final JSONWrapper topics = SessionData.INSTANCE.getGuiDef("ov_nws_topics"); // $NON-NLS$
        if (!topics.isArray()) {
            return DEFAULT_NEWS_TOPICS;
        }

        final int size = topics.size();
        final List<NewsTopic> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final JSONWrapper topic = topics.get(i);
            if (isAllowed(topic)) {
                final String id = topic.get("id").stringValue(); // $NON-NLS$
                final String name = topic.get("name").stringValue(); // $NON-NLS$
                result.add(new NewsTopic(id, name));
            }
        }
        return result;
    }

    private static String[] toViewNames(List<NewsTopic> newsTopics) {
        final String[] viewNames = new String[newsTopics.size()];
        for (int i = 0; i < viewNames.length; i++) {
            viewNames[i] = newsTopics.get(i).getName();
        }
        return viewNames;
    }

    private static boolean isAllowed(JSONWrapper jw) {
        final JSONWrapper selector = jw.get("selector"); // $NON-NLS$
        return !selector.isValid() || Selector.isAllowed(selector.stringValue());
    }

    @Override
    public void activate() {
        this.newsTopicController.activate();
    }

    @Override
    public void deactivate() {
        this.newsTopicController.deactivate();
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        this.optionalViewSelectionModel.ifPresent(indexedViewSelectionModel ->
                indexedViewSelectionModel.selectView(getViewId(historyToken), false));
        this.controllerId = historyToken.getControllerId();
        this.newsTopicController.onPlaceChange(adaptEvent(event));
    }

    private PlaceChangeEvent adaptEvent(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        if (isAllTopic(historyToken)) {
            // just use controllerId, so that newsTopicController removes the
            // topic parameter from the block that requests the headlines
            return new PlaceChangeEvent(historyToken.getControllerId());
        }
        return event;
    }

    private String getTopic(HistoryToken historyToken) {
        return historyToken.get(TOPIC_PARAM);
    }

    private boolean isWithTopic(HistoryToken historyToken) {
        return getTopic(historyToken) != null;
    }

    private boolean isAllTopic(HistoryToken historyToken) {
        return ALL_NEWS_TOPIC.getId().equals(getTopic(historyToken));
    }

    private int getViewId(HistoryToken historyToken) {
        if (isWithTopic(historyToken)) {
            return getTopicIndex(getTopic(historyToken));
        }
        return 0;
    }

    private int getTopicIndex(String topic) {
        for (int i = 0; i < this.topics.size(); i++) {
            if (topic.equals(this.topics.get(i).getId())) {
                return i;
            }
        }
        return 0; // use ALL
    }

    @Override
    public void refresh() {
        this.newsTopicController.refresh();
    }

    public void onViewChanged() {
        this.optionalViewSelectionModel.ifPresent(indexedViewSelectionModel -> {
            final int n = indexedViewSelectionModel.getSelectedView();
            HistoryToken.builder(this.controllerId).with(TOPIC_PARAM, this.topics.get(n).getId()).fire();
        });
    }

    @Override
    public String getPrintHtml() {
        return this.newsTopicController.getPrintHtml();
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        return this.newsTopicController.getPdfOptionSpec();
    }

    public static MenuModel.Item createMenuModel(MenuModel menuModel, AbstractMainController mc,
            String baseId) {
        final List<NewsTopic> newsTopics = getNewsTopics();

        final MenuModel.Item menu = menuModel.createMenu(toItemId(baseId, ALL_NEWS_TOPIC),
                toControllerId(baseId, ALL_NEWS_TOPIC), I18n.I.newsOverview());
        for (NewsTopic newsTopic : newsTopics) {
            if (ALL_NEWS_TOPIC.getId().equals(newsTopic.getId())) {
                continue;
            }
            final MenuModel.Item item = menuModel.createItem(toItemId(baseId, newsTopic),
                    toControllerId(baseId, newsTopic), newsTopic.getName(), null, true);
            menu.add(item);
        }
        menu.add(menuModel.createItem(baseId, baseId, I18n.I.newsOverview(), null,
                mc.hasController(baseId)).hide());

        return menu;
    }

    private static String toItemId(String baseId, NewsTopic newsTopic) {
        return baseId + "_" + newsTopic.getId(); // $NON-NLS$
    }

    private static String toControllerId(String baseId, NewsTopic newsTopic) {
        return baseId + "/" + TOPIC_PARAM + "=" + newsTopic.getId(); // $NON-NLS$
    }

    private static NewsTopic newsTopic(String id, String oldId, String name) {
        return new NewsTopic(id, oldId, name);
    }

    @NonNLS
    @SuppressWarnings("SimplifiableIfStatement")
    private static class NewsTopic {
        private final String id;

        private final String oldId;

        private final String name;

        public NewsTopic(String id, String oldId, String name) {
            this.id = id;
            this.oldId = oldId;
            this.name = name;
        }

        public NewsTopic(String id, String name) {
            this.id = id;
            this.oldId = id;
            this.name = name;
        }

        public String getId() {
            if (!FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled()) {
                return this.oldId;
            }
            return id;
        }

        public String getName() {
            return name;
        }

        /**
         * Uses getId to compare two NewsTopics!
         * Do not overwrite it with automatically generated method!
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NewsTopic)) return false;

            final NewsTopic newsTopic = (NewsTopic) o;

            if (getId() != null ? !getId().equals(newsTopic.getId()) : newsTopic.getId() != null)
                return false;
            return !(this.name != null ? !this.name.equals(newsTopic.name) : newsTopic.name != null);

        }

        /**
         * Uses getId instead of the id and/or oldId field to generate the hashCode.
         * Do not overwrite it with automatically generated method!
         */
        @Override
        public int hashCode() {
            int result = getId() != null ? getId().hashCode() : 0;
            result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
            return result;
        }

        /**
         * Uses getId instead of the id field to generate the string.
         * Do not overwrite it with automatically generated method!
         */
        @Override
        public String toString() {
            return "NewsTopic{" +
                    "id='" + getId() + '\'' +
                    ", name='" + this.name + '\'' +
                    '}';
        }
    }
}
