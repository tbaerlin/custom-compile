/*
 * DesktopSnippetView.java
 *
 * Created on 14.08.2008 15:22:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.economic;

import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.MerCountry;
import de.marketmaker.iview.dmxml.MerItem;
import de.marketmaker.iview.dmxml.MerType;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ExtendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CurrencyRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ulrich Maurer
 */
public class TypeListSnippetView extends SnippetView<TypeListSnippet> {
    private final ListBox listBoxTypes;

    private final SimplePanel panelCountries = new SimplePanel();

    private CheckBoxWithCountry[] checkBoxes;

    private final ClickHandler clickHandler = new ClickHandler() {
        public void onClick(ClickEvent event) {
            snippet.adaptChart();
        }
    };

    public TypeListSnippetView(final TypeListSnippet snippet) {
        super(snippet);
        setTitle(getConfiguration().getString("title")); // $NON-NLS-0$

        this.listBoxTypes = new ListBox();
        this.listBoxTypes.setVisibleItemCount(1);
        this.listBoxTypes.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                snippet.selectType(listBoxTypes.getValue(listBoxTypes.getSelectedIndex()));
            }
        });

        this.panelCountries.addStyleName("mm-eco-countryList"); // $NON-NLS-0$
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();

        final SimplePanel panelToolbar = new SimplePanel();
        panelToolbar.setStyleName("mm-snippetToolbar"); // $NON-NLS-0$
        panelToolbar.setWidget(this.listBoxTypes);

        this.container.setTopWidget(new WidgetComponent(panelToolbar));
        this.container.setContentWidget(this.panelCountries);
    }

    public void setMessage(String text, boolean asHtml) {
        final HTML html = new HTML(text);
        this.panelCountries.setWidget(html);

        html.setStyleName("mm-snippetMessage"); // $NON-NLS-0$
        if (asHtml) {
            html.setHTML(text);
        }
        else {
            html.setText(text);
        }

    }

    public void updateTypes(List<FinderMetaList.Element> list) {
        final String currentKey = listBoxTypes.getItemCount() > 0
                ? this.listBoxTypes.getValue(listBoxTypes.getSelectedIndex())
                : null;
        int selectedIndex = 0;
        this.listBoxTypes.clear();
        for (int i = 0; i < list.size(); i++) {
            FinderMetaList.Element e = list.get(i);
            this.listBoxTypes.addItem(e.getName(), e.getKey());
            if (e.getKey().equals(currentKey)) {
                selectedIndex = i;
            }
        }
        this.listBoxTypes.setSelectedIndex(selectedIndex);
    }

    String getSelectedType() {
        return this.listBoxTypes.getValue(this.listBoxTypes.getSelectedIndex());
    }

    public void updateCountries(List<MerCountry> list) {
        final Set<String> selectedItems = computeSelectedItems(list);
        this.checkBoxes = null;

        final int count = getCheckBoxCount(list);

        final CheckBoxWithCountry[] cb = new CheckBoxWithCountry[count];
        final FlexTable grid = initGrid();
        final FlexTable.FlexCellFormatter formatter = grid.getFlexCellFormatter();
        final ExtendBarData[] extendBarData = new ExtendBarData[count];
        float maxValue = 0f;

        int row = 0;
        int itemCount = 0;
        for (final MerCountry e : list) {
            final MerType type = e.getType().get(0);
            final String country = e.getName();

            grid.setText(row, 0, country);
            formatter.setColSpan(row, 0, 2);
            row++;

            for (final MerItem merItem : type.getItem()) {
                final String name = TypeListSnippet.getName(merItem, country)
                        + " (" + Formatter.LF.formatDateShort(merItem.getDate()) + ")";

                cb[itemCount] = new CheckBoxWithCountry(name, true, country);
                grid.setWidget(row, 0, cb[itemCount]);
                final QuoteData quotedata = merItem.getQuotedata();
                final String price = merItem.getPrice();

                if (quotedata == null || price == null) {
                    cb[itemCount].setEnabled(false);
                }
                else {
                    final String currency = CurrencyRenderer.DEFAULT.render(merItem.getQuotedata().getCurrencyIso());
                    final String qid = quotedata.getQid();
                    cb[itemCount].setName(qid);
                    cb[itemCount].setValue(selectedItems.contains(qid));
                    grid.setHTML(row, 1, Renderer.LARGE_NUMBER.render(price) + " " + currency);
                    formatter.setStyleName(row, 1, "mm-price"); // $NON-NLS$
                    final float fPrice = Float.parseFloat(price);
                    if ("%".equals(currency)) {
                        if (fPrice > maxValue) {
                            maxValue = fPrice;
                        }
                        extendBarData[itemCount] = new ExtendBarData(fPrice);
                    }
                }
                cb[itemCount].addClickHandler(this.clickHandler);
                row++;
                itemCount++;
            }
        }
        for (int i = 0; i < count; i++) {
            if (extendBarData[i] != null) {
                extendBarData[i].setMaxValue(maxValue);
            }
        }
        row = 0;
        itemCount = 0;
        for (final MerCountry e : list) {
            row++;
            //noinspection UnusedDeclaration
            for (final MerItem merItem : e.getType().get(0).getItem()) {
                grid.setHTML(row, 2, Renderer.EXTEND_BAR_LEFT.render(extendBarData[itemCount]));
                formatter.setStyleName(row, 2, "mm-extendBar"); // $NON-NLS-0$
                row++;
                itemCount++;
            }
        }
        this.checkBoxes = cb;
        this.panelCountries.setWidget(grid);
    }

    private Set<String> computeSelectedItems(List<MerCountry> list) {
        final Set<String> result = new HashSet<>(list.size());
        if (this.checkBoxes != null) {
            for (CheckBox box : this.checkBoxes) {
                if (box.getValue()) {
                    result.add(box.getName());
                }
            }
        }
        return result;
    }

    private FlexTable initGrid() {
        final FlexTable result = new FlexTable();
        result.setStyleName("mm-snippetTable"); // $NON-NLS-0$
        result.setCellPadding(0);
        result.setCellSpacing(0);
        final HTMLTable.ColumnFormatter columnFormatter = result.getColumnFormatter();
        columnFormatter.setWidth(0, "66%"); // $NON-NLS-0$
        columnFormatter.setWidth(1, "17%"); // $NON-NLS-0$
        columnFormatter.setWidth(2, "17%"); // $NON-NLS-0$
        return result;
    }

    private int getCheckBoxCount(List<MerCountry> list) {
        int result = 0;
        for (MerCountry merCountry : list) {
            result += merCountry.getType().get(0).getItem().size();
        }
        return result;
    }


    List<String> getSelectedNames() {
        final List<String> result = new ArrayList<>();
        for (final CheckBoxWithCountry box : this.checkBoxes) {
            if (box.isEnabled() && box.getValue()) {
                result.add(box.getCountry() + "/" + box.getText()); // $NON-NLS-0$
            }
        }
        return result;
    }

    List<String> getSelectedSymbols() {
        final List<String> result = new ArrayList<>();
        for (final CheckBoxWithCountry box : this.checkBoxes) {
            if (box.isEnabled() && box.getValue()) {
                result.add(box.getName());
            }
        }
        return result;
    }

    public class CheckBoxWithCountry extends CheckBox {
        private final String country;

        public CheckBoxWithCountry(String s, boolean b, String country) {
            super(s, b);
            this.country = country;
        }

        public String getCountry() {
            return country;
        }
    }
}
