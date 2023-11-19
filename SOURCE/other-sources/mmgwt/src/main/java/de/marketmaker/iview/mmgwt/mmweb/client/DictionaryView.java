/*
* DictionaryView.java
*
* Created on 18.09.2008 13:03:13
*
* Copyright (c) vwd GmbH. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.iview.dmxml.LexiconId;
import de.marketmaker.iview.dmxml.MSCLexiconEntry;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ScrollPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ScrollableTabLayoutPanel;

import java.util.List;

/**
 * @author Michael Lösch
 * @author mdick
 */
public class DictionaryView implements ContentView {
    private final Widget viewWidget;
    private final FloatingToolbar tb;
    private final ScrollableTabLayoutPanel meaningsPanel;
    private final SnippetTableWidget termsPanel;
    private final DictionaryController controller;
    private Button selectedButton;

    public DictionaryView(DictionaryController controller) {
        this.controller = controller;

        final TableColumn[] columns = new TableColumn[1];
        columns[0] = new TableColumn(I18n.I.subjects(), 100); 
        columns[0].setRenderer(new TableCellRenderers.LinkRenderer(28, ""));
        final DefaultTableColumnModel entriesColModel = new DefaultTableColumnModel(columns, false);

        this.tb = new FloatingToolbar(FloatingToolbar.ToolbarHeight.FOR_ICON_SIZE_S);

        this.termsPanel = SnippetTableWidget.create(entriesColModel);
        this.termsPanel.getElement().getStyle().setHeight(100, Style.Unit.PCT);
        final ScrollPanel termsScrollPanel = new ScrollPanel(this.termsPanel);
        termsScrollPanel.setStyleName("mm-dictionaryTerms");

        this.meaningsPanel = new ScrollableTabLayoutPanel(25, Style.Unit.PX);
        this.meaningsPanel.addStyleName("mm-dictionaryMeanings");

        final DockLayoutPanel panel = new DockLayoutPanel(Style.Unit.PX);
        panel.addNorth(this.tb, this.tb.getToolbarHeightPixel());

        panel.addWest(termsScrollPanel, 200);
        panel.add(this.meaningsPanel);

        this.viewWidget = panel;
    }

    public Widget getWidget() {
        return this.viewWidget;
    }

    public void onBeforeHide() {
        // nothing to do
    }

    public void createInitialButtons(List<String> initials) {
        for (int i = 0; i < initials.size(); i++) {
            final String initial = initials.get(i);

            final Button button = Button.text(initial).build();
            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    controller.loadEntriesList(initial);
                    select(button);
                }
            });

            if (i > 0) {
                this.tb.addSeparator();
            } else {
                this.controller.loadEntriesList(initials.get(0));
                select(button);
            }
            this.tb.add(button);
        }
    }

    public void updateView(MSCLexiconEntry entry) {
        final HTML item = new HTML("<p>" + entry.getText() + "</p>");  // $NON-NLS$

        final HTMLPanel tab = createTab(entry, item);

        this.meaningsPanel.add(item, tab);
        this.meaningsPanel.selectTab(item);
    }

    private HTMLPanel createTab(MSCLexiconEntry entry, final HTML item) {
        String imgId = DOM.createUniqueId();
        String labelId = DOM.createUniqueId();
        final HTMLPanel tab = new HTMLPanel("<div class=\"gwt-HTML\"><span id=\"" + labelId +"\"></span><img id=\"" + imgId + "\"></img></div>");  // $NON-NLS$

        final InlineHTML label = new InlineHTML(SafeHtmlUtils.htmlEscape(entry.getTitle()));
        label.setStyleName("");
        IconImageIcon icon = IconImage.getIcon("x-tool-close"); // $NON-NLS$
        icon.addStyleName("mm-dictionaryMeaning-close");

        tab.addAndReplaceElement(label, labelId);
        tab.addAndReplaceElement(icon, imgId);

        icon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                meaningsPanel.remove(item);
            }
        });
        return tab;
    }

    public void updateView(List<LexiconId> entries) {
        DefaultTableDataModel tdm = new DefaultTableDataModel(entries.size(), 1);

        for (int i = 0; i < entries.size(); i++) {
            final LexiconId entry = entries.get(i);
            tdm.setValueAt(i, 0, new Link(new LinkListener<Link>() {
                public void onClick(LinkContext linkContext, Element e) {
                    controller.loadEntry(entry.getId());
                }
            }, entry.getTitle(), ""));  // $NON-NLS$
        }
        this.termsPanel.updateData(tdm);
    }

    private void select(Button button) {
        if (this.selectedButton != button) {
            if (this.selectedButton != null) {
                this.selectedButton.setActive(false);
            }
            this.selectedButton = button;
            this.selectedButton.setActive(true);
        }
    }

    public int getOpenEntriesCount() {
        return this.meaningsPanel.getWidgetCount();
    }
}
