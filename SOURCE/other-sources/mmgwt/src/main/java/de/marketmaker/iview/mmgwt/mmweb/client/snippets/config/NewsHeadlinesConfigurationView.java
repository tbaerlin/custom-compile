/*
 * NewsHeadlinesConfigurationView.java
 *
 * Created on 19.06.2012 14:50:42
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.dmxml.MetadataElements;
import de.marketmaker.iview.dmxml.NWSSearchMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.NewsRealmLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsHeadlinesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NaturalComparator;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.Caption;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ReadonlyField;

import static de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView.SymbolParameterType;

/**
 * @author Ulrich Maurer
 */
public class NewsHeadlinesConfigurationView {
    private static NaturalComparator<String> NATURAL_COMPARATOR = NaturalComparator.createDefault();

    private final SimplePanel view;

    private final SelectButton newsTopic;

    private final NewsHeadlinesSnippet snippet;

    private final ReadonlyField instrumentNameField;

    private String instrumentSymbol = null;

    private String instrumentName = null;

    public static void show(NewsHeadlinesSnippet s) {
        new NewsHeadlinesConfigurationView(s).show();
    }

    private NewsHeadlinesConfigurationView(NewsHeadlinesSnippet s) {
        this.snippet = s;

        this.newsTopic = new SelectButton(SessionData.isAsDesign() ? Button.RendererType.SPAN : Button.getRendererType())
                .withMenu(createNewsTopicMenu())
                .withClickOpensMenu();

        this.instrumentSymbol = s.getConfiguration().getString("symbol"); // $NON-NLS$
        this.instrumentName = s.getConfiguration().getString("symbolTitle"); // $NON-NLS$

        this.view = Styles.tryAddStyles(new SimplePanel(), Styles.get().generalViewStyle());

        final FlexTable table = Styles.tryAddStyles(new FlexTable(), Styles.get().generalFormStyle());
        this.view.setWidget(table);

        table.setWidth("100%"); // $NON-NLS$
        table.setCellSpacing(14);
        table.setWidget(0, 0, new Caption(I18n.I.category()));
        table.setWidget(0, 1, this.newsTopic);
        Styles.tryAddStyles(this.newsTopic, Styles.get().comboBox());

        table.setWidget(1, 0, new Caption(I18n.I.instrument()));
        this.instrumentNameField = new ReadonlyField(this.instrumentName == null ? "" : this.instrumentName);
        if(!SessionData.isAsDesign()) {
            this.instrumentNameField.getElement().getStyle().setPropertyPx("minWidth", 250);  // $NON-NLS$
        }

        final Button buttonSearch = Button.icon("mm-icon-finder") // $NON-NLS$
                .clickHandler(event -> showSelectSymbolForm())
                .build();

        final Button buttonDelete = Button.icon("mm-finder-btn-delete") // $NON-NLS$
                .clickHandler(event -> {
                    this.instrumentSymbol = null;
                    this.instrumentName = null;
                    this.instrumentNameField.setText(null);
                })
                .build();

        final HorizontalPanel horizontalPanel = new HorizontalPanel();
        if(!SessionData.isAsDesign()) {
            horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        }
        horizontalPanel.add(this.instrumentNameField);
        horizontalPanel.add(buttonSearch);
        horizontalPanel.add(buttonDelete);

        table.setWidget(1, 1, horizontalPanel);
    }

    private Menu createNewsTopicMenu() {
        final Menu menu = new Menu();
        menu.add(new MenuItem(I18n.I.newsTopicsAll())); // "key" is null
        new NewsRealmLoader() {
            @Override
            public void handleResult(NWSSearchMetadata result) {
                final List<MenuItem> listMenuItems = new ArrayList<>();
                if (FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled()) {
                    final List<FinderTypedMetaList> metaListList = result.getList();
                    if (metaListList != null) {
                        addMetaList(metaListList, listMenuItems, "topic");  // $NON-NLS-0$
                    }
                }
                else {
                    final List<MetadataElements.Element> topics = result.getTopics().getElement();
                    for (MetadataElements.Element element : topics) {
                        listMenuItems.add(createMenuItem(element.getKey(), element.getName()));
                    }
                }
                sortAndAddToMenu(listMenuItems, menu);
                setSelectedNewsTopic();
            }

            private void addMetaList(final List<FinderTypedMetaList> metaListList,
                    List<MenuItem> listMenuItems, final String metaListName) {
                for (FinderTypedMetaList list : metaListList) {
                    if (metaListName.equals(list.getType())) {
                        for (FinderMetaList.Element element : list.getElement()) {
                            listMenuItems.add(createMenuItem(element.getKey(), element.getName()));
                        }
                    }
                }
            }

            private void sortAndAddToMenu(List<MenuItem> listMenuItems, Menu menu) {
                Collections.sort(listMenuItems, new NewsTopicComparator());
                for (MenuItem menuItem : listMenuItems) {
                    menu.add(menuItem);
                }
            }
        }.execute();

        return menu;
    }

    class NewsTopicComparator implements Comparator<MenuItem> {
        @Override
        public int compare(MenuItem mi1, MenuItem mi2) {
            return NATURAL_COMPARATOR.compareIgnoreCase((String) mi1.getData("name"), (String) mi2.getData("name")); // $NON-NLS$
        }
    }

    private MenuItem createMenuItem(String topic, String name) {
        return new MenuItem(name)
                .withData("topic", topic) // $NON-NLS$
                .withData("name", name); // $NON-NLS$
    }

    private void setSelectedNewsTopic() {
        final String topic = this.snippet.getConfiguration().getString("topic"); // $NON-NLS$
        this.newsTopic.setSelectedData("topic", topic); // $NON-NLS$
    }

    private void showSelectSymbolForm() {
        final HashMap<String, String> symbolFormParams = new HashMap<>();
        final SelectSymbolForm ssf = SelectSymbolForm.create(symbolFormParams, SymbolParameterType.IID);
        ssf.setPixelSize(500, 300);

        final DialogIfc dialog = Dialog.getImpl().createDialog()
                .withTitle(I18n.I.selection())
                .withWidget(ssf)
                .withStyle("mm-noPadding") // $NON-NLS$
                .withCloseButton()
                .withButton(I18n.I.ok(), () -> {
                    ssf.deactivate();
                    this.instrumentSymbol = symbolFormParams.get("symbol"); // $NON-NLS$
                    this.instrumentName = symbolFormParams.get("title"); // $NON-NLS$
                    this.instrumentNameField.setText(this.instrumentName);
                })
                .withButton(I18n.I.cancel());

        dialog.show();
    }

    private void show() {
        final DialogIfc dialog = Dialog.getImpl().createDialog()
                .withTitle(I18n.I.selection())
                .withWidget(this.view)
                .withStyle("mm-noPadding") // $NON-NLS$
                .withCloseButton()
                .withButton(I18n.I.ok(), this::onOk)
                .withButton(I18n.I.cancel());
        dialog.show();
    }

    private void onOk() {
        final HashMap<String, String> params = this.snippet.getCopyOfParameters();
        final MenuItem selectedTopicItem = this.newsTopic.getSelectedItem();
        final String topic = (String) selectedTopicItem.getData("topic"); // $NON-NLS$

        final StringBuilder sb = new StringBuilder();
        if (topic == null) {
            params.remove("topic"); // $NON-NLS$
            params.remove("titleSuffixSelectNewsTopic"); // $NON-NLS$
        }
        else {
            final String topicName = (String) selectedTopicItem.getData("name"); // $NON-NLS$
            params.put("topic", topic); // $NON-NLS$
            params.put("titleSuffixSelectNewsTopic", topicName); // $NON-NLS$
            sb.append(topicName);
        }

        if (this.instrumentSymbol == null) {
            params.remove("symbol"); // $NON-NLS$
            params.remove("symbolTitle"); // $NON-NLS$
            params.remove("titleSuffixSelectSymbol"); // $NON-NLS$
        }
        else {
            params.put("symbol", this.instrumentSymbol); // $NON-NLS$
            params.put("symbolTitle", this.instrumentName); // $NON-NLS$
            params.put("titleSuffixSelectSymbol", this.instrumentName); // $NON-NLS$
            if (sb.length() > 0) {
                sb.append(" - "); // $NON-NLS$
            }
            sb.append(this.instrumentName);
        }

        params.put("titleSuffix", sb.toString()); // $NON-NLS$
        this.snippet.setParameters(params);
    }
}
