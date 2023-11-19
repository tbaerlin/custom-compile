/*
* DependentValuesSnippetView.java
*
* Created on 16.07.2008 13:01:43
*
* Copyright (c) vwd AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.LIST_EXPIRATION_DATES;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElement;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormKeys;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderOPT;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSelectionViewButtons;

/**
 * View that displays a small form above the results table that can be used to change the query.
 * Elements on that form are objects of classes in  FinderFormElements (or subclasses thereof).
 * This helps to create queries or an appropriate FinderFormConfig, which is used to display the
 * same query to the respective Finder.
 * @author Oliver Flege
 * @author Markus Dick
 */
public class DependentValuesSnippetView extends SnippetTableView<DependentValuesSnippet> {

    private class MyBooleanOption extends FinderFormElements.BooleanOption {
        private final String query;

        private MyBooleanOption(String field, String label, String query) {
            super(field, label);
            this.query = query;
        }

        @Override
        public Widget getInnerWidget() {
            return this.cb;
        }

        protected String doGetQuery() {
            return this.query;
        }

        @Override
        protected void setEnabled(boolean enabled) {
            snippet.onQueryChanged();
        }
    }

    private class MyListBoxOption extends FinderFormElements.ListBoxOption {
        private MyListBoxOption(String field, String label, List<FinderFormElements.Item> items) {
            super(field, label, items, FinderFormElements.IGNORE_VALUE);
        }

        @Override
        protected String doGetQuery() {
            final Date date = getExpiresDate();
            if (date == null) {
                return null;
            }
            final String isoToday = Formatter.formatDateAsISODay(new Date());
            final String isoDate = Formatter.formatDateAsISODay(date);
            return this.field + ">='" + isoToday + "' && " + this.field + "<'" + isoDate + "'"; // $NON-NLS$
        }

        @Override
        protected String getItemKey() {
            return this.id + "-lb"; // $NON-NLS-0$
        }

        protected Date getExpiresDate() {
            final String selectedValue = FinderFormElements.selectedValue(this.lb);
            if (FinderFormElements.IGNORE_VALUE.equals(selectedValue)) {
                return null;
            }
            return DateTimeUtil.nowPlus(selectedValue);
        }
    }

    private class NonOptFinderForm extends Composite {

        private FinderFormElements.BooleanOption issuerRestriction;

        private NonOptFinderForm(String name) {
            final String issuerDisplayName = GuiDefsLoader.getIssuerDisplayName();
            this.issuerRestriction =
                    new MyBooleanOption(FinderFormKeys.ISSUER_NAME, I18n.I.onlyIssuer(issuerDisplayName), FinderFormKeys.ISSUER_NAME + "=='" + name + "'");  // $NON-NLS$
            this.issuerRestriction.setValue(true);

            final Grid g = new Grid(1, 1);
            g.setCellPadding(2);
            g.setWidget(0, 0, this.issuerRestriction.getInnerWidget());
            initWidget(g);
        }

        public void addConfigTo(FinderFormConfig config) {
            if (this.issuerRestriction.getValue()) {
                // different sections in FinderCER and WNTFinderElementMapper
                config.put(this == cerFinderForm ? "common" : "base", "true"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
                this.issuerRestriction.addConfigTo(config);
            }
        }

        public String getQuery() {
            return this.issuerRestriction.getQuery();
        }
    }

    private class OptFinderForm extends Composite {
        private final FinderFormElement[] elements;

        private MyListBoxOption expires;

        private FinderFormElements.BooleanOption tradeToday;

        private FinderFormElements.RadioOption type;

        private OptFinderForm() {
            this.type = createTypeOption();
            this.expires = createExpiresOption();
            this.tradeToday = new MyBooleanOption(FinderFormKeys.TRADED_TODAY, I18n.I.tradedToday(), "totalVolume>'0'");  // $NON-NLS-0$

            final Grid g = new Grid(1, 4);
            g.addStyleName("mm-dependentValues-tools"); // $NON-NLS-0$
            g.setCellSpacing(2);
            g.setWidget(0, 0, this.type.getInnerWidget());
            g.setWidget(0, 1, this.tradeToday.getInnerWidget());
            g.setText(0, 2, I18n.I.maturity2());
            g.setWidget(0, 3, this.expires.getInnerWidget());
            initWidget(g);

            this.elements = new FinderFormElement[]{this.type, this.tradeToday, this.expires};
        }

        public void addConfigTo(FinderFormConfig config) {
            for (FinderFormElement e : this.elements) {
                e.addConfigTo(config);
            }
        }

        public String getQuery() {
            StringBuilder sb = new StringBuilder();
            for (FinderFormElement e : this.elements) {
                final String q = e.getQuery();
                if (q != null) {
                    sb.append(sb.length() > 0 ? " && " : "").append(q); // $NON-NLS-0$ $NON-NLS-1$
                }
            }
            return sb.length() > 0 ? sb.toString() : null;
        }

        protected Date getExpiresDate() {
            return this.expires.getExpiresDate();
        }
    }

    private class FutFinderForm extends Composite {
        private final FinderFormElement[] elements;

        private MyListBoxOption expires;

        private FinderFormElements.BooleanOption tradeToday;

        private FutFinderForm() {
            this.expires = createExpiresOption();
            this.tradeToday = new MyBooleanOption(FinderFormKeys.TRADED_TODAY, I18n.I.tradedToday(), "totalVolume>'0'"); // $NON-NLS-0$

            final Grid g = new Grid(1, 4);
            g.addStyleName("mm-dependentValues-tools"); // $NON-NLS-0$
            g.setCellSpacing(2);
            g.setWidget(0, 1, this.tradeToday.getInnerWidget());
            g.setText(0, 2, I18n.I.maturity2());
            g.setWidget(0, 3, this.expires.getInnerWidget());
            initWidget(g);

            this.elements = new FinderFormElement[]{this.tradeToday, this.expires};
        }

        public void addConfigTo(FinderFormConfig config) {
            for (FinderFormElement e : this.elements) {
                e.addConfigTo(config);
            }
        }

        public String getQuery() {
            StringBuilder sb = new StringBuilder();
            for (FinderFormElement e : this.elements) {
                final String q = e.getQuery();
                if (q != null) {
                    sb.append(sb.length() > 0 ? " && " : "").append(q); // $NON-NLS-0$ $NON-NLS-1$
                }
            }
            return sb.length() > 0 ? sb.toString() : null;
        }

        protected Date getExpiresDate() {
            return this.expires.getExpiresDate();
        }
    }

    private Widget cerFinderForm = new Label(""); // $NON-NLS-0$

    private SimplePanel finderForm = new SimplePanel();

    private OptFinderForm optFinderForm = new OptFinderForm();

    private FutFinderForm futFinderForm = new FutFinderForm();

    private FloatingToolbar toolbar;

    private Widget wntFinderForm = new Label(""); // $NON-NLS-0$

    public DependentValuesSnippetView(final DependentValuesSnippet snippet) {
        super(snippet, snippet.getColumnModel());
        setSortLinkListener(snippet);
        initToolbar();

        final String issuerName = this.snippet.getIssuerName();
        if (issuerName != null) {
            this.cerFinderForm = new NonOptFinderForm(issuerName);
            this.wntFinderForm = new NonOptFinderForm(issuerName);
        }

        this.panel.add(this.finderForm);
    }

    public void addConfigTo(int view, FinderFormConfig config) {
        if (view == 0 && this.cerFinderForm instanceof NonOptFinderForm) {
            ((NonOptFinderForm)this.cerFinderForm).addConfigTo(config);
        }
        if (view == 1 && this.wntFinderForm instanceof NonOptFinderForm) {
            ((NonOptFinderForm)this.wntFinderForm).addConfigTo(config);
        }
        if (view == 2 && this.optFinderForm instanceof OptFinderForm) {
            this.optFinderForm.addConfigTo(config);
        }
        if (view == 3 && this.futFinderForm instanceof FutFinderForm) {
            this.futFinderForm.addConfigTo(config);
        }
    }

    public void onViewChange(int viewId) {
        reloadTitle();
        this.finderForm.setWidget(getFinderFormWidget(viewId));
    }

    @Override
    protected Panel createPanel(String height) {
        return new FlowPanel();
    }

    protected String getQuery(int view) {
        if (view == 0 && this.cerFinderForm instanceof NonOptFinderForm) {
            return ((NonOptFinderForm)this.cerFinderForm).getQuery();
        }
        if (view == 1 && this.wntFinderForm instanceof NonOptFinderForm) {
            return ((NonOptFinderForm)this.wntFinderForm).getQuery();
        }
        if (view == 2) {
            return this.optFinderForm.getQuery();
        }
        if (view == 3) {
            return this.futFinderForm.getQuery();
        }
        return null;
    }

    protected Date getExpiresDate(int view) {
        if (view == 2) {
            return this.optFinderForm.getExpiresDate();
        } else if (view == 3) {
            return this.futFinderForm.getExpiresDate();
        }
        return null;
    }

    private MyListBoxOption createExpiresOption() {
        final ArrayList<FinderFormElements.Item> items = new ArrayList<FinderFormElements.Item>();
        items.add(new FinderFormElements.Item(I18n.I.all(), FinderFormElements.IGNORE_VALUE));
        items.addAll(LIST_EXPIRATION_DATES);
        MyListBoxOption result = new MyListBoxOption(FinderFormKeys.EXPIRATION_DATE, I18n.I.maturity2(), items);
        result.setValue(true);
        result.addValueChangeHandler(this.snippet);
        return result;
    }

    private FinderFormElements.RadioOption createTypeOption() {
        final ArrayList<FinderFormElements.Item> items = LiveFinderOPT.getOptionTypeItems();
        items.add(0, new FinderFormElements.Item(I18n.I.all(), FinderFormElements.IGNORE_VALUE));
        final FinderFormElements.RadioOption result = LiveFinderOPT.createOptionTypeElement(items);
        result.setValue(true);
        result.addValueChangeHandler(this.snippet);
        return result;
    }

    FloatingToolbar getToolbar() {
        return this.toolbar;
    }

    private Widget getFinderFormWidget(int viewId) {
        switch(viewId) {
            case 0: return this.cerFinderForm;
            case 1: return this.wntFinderForm;
            case 2: return this.optFinderForm;
            case 3: return this.futFinderForm;
            default: throw new IllegalArgumentException("<getFinderFormWidget>Invalid view ID: " + viewId + "!"); //$NON-NLS$
        }
    }

    private void initToolbar() {
        this.toolbar = ViewSelectionViewButtons.createToolbar();
        new ViewSelectionViewButtons(snippet.getMultiViewSupport().getViewSelectionModel(), this.toolbar);
        setTopComponent(this.toolbar);
    }
}
