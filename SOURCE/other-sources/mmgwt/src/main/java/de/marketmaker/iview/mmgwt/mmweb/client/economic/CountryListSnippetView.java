/*
 * DesktopSnippetView.java
 *
 * Created on 14.08.2008 15:22:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.economic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;

import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Ulrich Maurer
 */
public class CountryListSnippetView extends SnippetView<CountryListSnippet> {
    private final SimplePanel panelList = new SimplePanel();

    private List<FinderMetaList.Element> currentList = null;

    private List<String> currentKeyList = null;

    private CheckBox checkboxes[];

    private final Set<String> checked;

    private static final String NAME_PREFIX = "cb-"; // $NON-NLS-0$

    private final CheckBox cbAll;

    public CountryListSnippetView(CountryListSnippet snippet) {
        super(snippet);
        final SnippetConfiguration config = getConfiguration();
        setTitle(config.getString("title")); // $NON-NLS-0$

        this.checked = getSet(config.getString("checked", null)); // $NON-NLS-0$

        this.cbAll = new CheckBox(I18n.I.all()); 
        this.cbAll.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                updateAll((CheckBox) event.getSource());
            }
        });
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();

        final SimplePanel panelToolbar = new SimplePanel();
        panelToolbar.setStyleName("mm-snippetToolbar"); // $NON-NLS-0$
        panelToolbar.setWidget(this.cbAll);

        this.container.setTopWidget(new WidgetComponent(panelToolbar));

        this.panelList.addStyleName("mm-eco-countryList"); // $NON-NLS-0$
        this.container.setContentWidget(this.panelList);
    }

    private Set<String> getSet(String s) {
        return new HashSet<>(StringUtil.split(s, ','));
    }

    public void setMessage(String text, boolean asHtml) {
        final HTML html = new HTML(text);
        this.cbAll.setVisible(false);
        this.panelList.setWidget(html);

        html.setStyleName("mm-snippetMessage"); // $NON-NLS-0$
        if (asHtml) {
            html.setHTML(text);
        }
        else {
            html.setText(text);
        }

    }

    public void update(List<FinderMetaList.Element> list) {
        this.cbAll.setVisible(true);

        final Grid grid = new Grid(list.size(), 1);
        grid.setStyleName("mm-snippetTable"); // $NON-NLS-0$
        grid.setCellPadding(0);
        grid.setCellSpacing(0);

        final ClickHandler updateCheckedListener = new ClickHandler() {
            public void onClick(ClickEvent event) {
                updateChecked((CheckBox) event.getSource());
            }
        };
        this.checkboxes = new CheckBox[list.size()];
        this.currentKeyList = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            FinderMetaList.Element e = list.get(i);
            final String key = e.getKey();
            this.currentKeyList.add(key);
            this.checkboxes[i] = new CheckBox(e.getName());
            this.checkboxes[i].setName(NAME_PREFIX + i);
            this.checkboxes[i].setValue(this.checked.contains(key));
            this.checkboxes[i].addClickHandler(updateCheckedListener);
            grid.setWidget(i, 0, this.checkboxes[i]);
        }
        this.panelList.setWidget(grid);
        this.currentList = list;
    }

    private void updateAll(CheckBox cb) {
        final boolean checked = cb.getValue();
        for (final CheckBox checkBox : this.checkboxes) {
            checkBox.setValue(checked);
        }
        if (checked) {
            this.checked.addAll(this.currentKeyList);
        }
        else {
            this.checked.clear();
        }
        this.snippet.onCountrySelectionChanged();
    }

    private void updateChecked(final CheckBox cb) {
        final int id = Integer.parseInt(cb.getName().substring(NAME_PREFIX.length()));
        final String key = this.currentList.get(id).getKey();
        if (cb.getValue()) {
            this.checked.add(key);
        }
        else {
            this.checked.remove(key);
        }
        this.cbAll.setValue(this.checked.size() == this.currentKeyList.size());
        this.snippet.onCountrySelectionChanged();
    }

    public Set<String> getChecked() {
        return this.checked;
    }
}
