/*
 * PortraitChartSnippetView.java
 *
 * Created on 15.05.2008 14:07:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.economic;

import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.BasicChartSnippetView;

import java.util.HashMap;

/**
 * @author Oliver Flege
* @author Thomas Kiesgen
*/
class EconomicChartSnippetView extends BasicChartSnippetView<EconomicChartSnippet, EconomicChartSnippetView> {

    private final Listener<MenuEvent> checkItemListener = new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent event) {
            final CheckMenuItem item = (CheckMenuItem) event.getItem();
            if (item.isChecked()) {
                selectedItem = item;
                if (hButton != null) {
                    hButton.setText(item.getText());
                    select();
                }
            }
        }
    };
    
    private String group = XDOM.getUniqueId();

    private Button hButton;

    private HashMap<String, String> periods = new HashMap<String, String>();

    private CheckMenuItem selectedItem;

    private ToolBar toolbar;

    public EconomicChartSnippetView(EconomicChartSnippet snippet) {
        super(snippet);

        this.toolbar = new ToolBar();
        this.toolbar.addStyleName("mm-viewWidget"); // $NON-NLS-0$

        final Menu m = new Menu();
        m.add(createItem(I18n.I.nMonths(1), "P1M"));  // $NON-NLS-0$
        m.add(createItem(I18n.I.nMonths(3), "P3M"));  // $NON-NLS-0$
        m.add(createItem(I18n.I.nMonths(6), "P6M"));  // $NON-NLS-0$
        final CheckMenuItem defaultItem = createItem(I18n.I.nYears(1), "P1Y");  // $NON-NLS-0$
        m.add(defaultItem);
        m.add(createItem(I18n.I.nYears(2), "P2Y"));  // $NON-NLS-0$
        m.add(createItem(I18n.I.nYears(3), "P3Y"));  // $NON-NLS-0$
        m.add(createItem(I18n.I.nYears(5), "P5Y"));  // $NON-NLS-0$
        m.add(createItem(I18n.I.nYears(10), "P10Y"));  // $NON-NLS-0$
        m.add(createItem(I18n.I.total(), null)); 
        if (this.selectedItem == null) {
            this.selectedItem = defaultItem;
            this.selectedItem.setChecked(true);
        }

        this.hButton = new Button(this.selectedItem.getText(), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                select();
            }
        });
        this.hButton.setMenu(m);

        this.toolbar.add(this.hButton);
    }

    protected void onContainerAvailable() {
        this.container.setTopWidget(this.toolbar);
        super.onContainerAvailable();
    }

    private CheckMenuItem createItem(String name, String period) {
        final boolean selected = isConfiguredPeriod(period);
        final CheckMenuItem result = new CheckMenuItem(name);
        result.setChecked(selected);
        result.setGroup(this.group);
        result.addListener(Events.CheckChange, this.checkItemListener);
        this.periods.put(result.getId(), period);
        if (selected) {
            this.selectedItem = result;
        }
        return result;
    }

    private void select() {
        this.snippet.setPeriod(this.periods.get(this.selectedItem.getId()));
    }

    protected void updateFooter(IMGResult ipr) {
        setFooterText(this.snippet.getFooterMessage(), true);
    }
}
