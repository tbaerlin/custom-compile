/*
 * GenericListSnippet.java
 *
 * Created on 4/20/15
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.datepicker.client.CalendarUtil;

import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.Partition;
import de.marketmaker.iview.mmgwt.mmweb.client.Ginjector;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.GuiDefsChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements;
import de.marketmaker.iview.mmgwt.mmweb.client.logging.Logger;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.PagingPanel;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * @author Stefan Willenbrock
 */
@NonNLS
public abstract class GenericListSnippet<T extends GenericListSnippet<T>> extends
        AbstractSnippet<T, GenericListSnippetView<T>>
        implements PagingPanel.Handler {

    protected final class Selection {

        private String symbol;

        public void findSelectedSymbol() {
            if (this.symbol != null) {
                boolean symbolExists = hasSymbol(symbol);
                if (!symbolExists) {
                    this.symbol = null;
                }
            }
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
            for (SymbolSnippet symbolSnippet : symbolSnippets) {
                symbolSnippet.setSymbol(null, symbol, null);
            }
            if (symbol != null) {
                ackParametersChanged();
            }
        }

        public boolean isSelected(String symbol) {
            if (this.symbol == null) {
                setSymbol(symbol);
                return true;
            }
            return this.symbol.equals(symbol);
        }
    }

    protected final Selection selection;

    protected Logger logger = Ginjector.INSTANCE.getLogger();

    protected SessionData sessionData = Ginjector.INSTANCE.getSessionData();

    protected DmxmlContext.Block block;

    private int offset, count;

    private boolean withType, withIssuer, withInvestFocus, withAlphabet, withMaturity;

    private String issuerField, investFocusField, typeField, issuerLabel, investFocusLabel, typeLabel;

    private String searchPrefix;

    private int maturityPeriodNumber, maturityPeriodLength;

    protected Map<String, String> constraints = new HashMap<>();

    private boolean initialized = false;

    private List<SymbolSnippet> symbolSnippets = new ArrayList<>();

    private String titleSuffix;

    private String queryWithoutContraints;

    public GenericListSnippet(DmxmlContext context, SnippetConfiguration configuration,
            String block, String sortBy) {
        super(context, configuration);

        this.selection = new Selection();

        this.offset = configuration.getInt("offset", 0);
        this.count = configuration.getInt("count", 10);

        this.withType = configuration.getBoolean("withType", false);
        this.withIssuer = configuration.getBoolean("withIssuer", false);
        this.withInvestFocus = configuration.getBoolean("withInvestFocus", false);
        this.withAlphabet = configuration.getBoolean("withAlphabet", false);
        this.withMaturity = configuration.getBoolean("withMaturity", false);

        this.searchPrefix = configuration.getString("searchPrefix");
        this.maturityPeriodNumber = configuration.getInt("maturityPeriodNumber", 10);
        this.maturityPeriodLength = configuration.getInt("maturityPeriodLength", 1);

        readConstraints(configuration);

        this.block = createBlock(block);
        this.block.setParameter("offset", this.offset);
        this.block.setParameter("count", this.count);
        this.block.setParameter("sortBy", configuration.getString("sortBy", sortBy));
        this.block.setParameter("ascending", configuration.getBoolean("ascending", false));
        this.block.setParameter("alphabeticOverview", this.withAlphabet);

        EventBusRegistry.get().addHandler(GuiDefsChangedEvent.getType(), event -> {
            readConstraints(getConfiguration());
            setQueryParameters(this.titleSuffix, this.queryWithoutContraints);
        });
    }

    private void readConstraints(SnippetConfiguration config) {
        final String s = config.getString("constraint");
        if (s != null) {
            // TODO: Handle multiple constraints separated by && or ||
            final String constraint = this.sessionData.getGuiDef(s).stringValue();
            if (constraint == null) {
                this.logger.warn("<" + getClass().getSimpleName() + ".readConstraints> constraint is null");
                return;
            }
            this.constraints.put(constraint.substring(0, constraint.indexOf("==")), constraint);
            this.logger.debug("<" + getClass().getSimpleName() + ".readConstraints> " + this.constraints);
        }
    }

    @Override
    public void ackNewOffset(int offset) {
        getConfiguration().put("offset", Integer.toString(offset));
        ackParametersChanged();
    }

    @Override
    public void destroy() {
        destroyBlock(this.block);
    }

    @Override
    protected void onParametersChanged() {
        this.block.setParameter("offset", getConfiguration().getString("offset", "0"));
    }

    @Override
    public void updateView() {
        if (!this.block.isResponseOk() || this.block.getResult() == null || !isResponseOk()) {
            setNull();
            return;
        }

        this.selection.findSelectedSymbol();
        updateTableData();

        if (!this.initialized && isMetaDataEnabled()) {
            if (this.withIssuer) {
                addMetaData(getIssuerMetaData(), getView().getIssuerBox(), 0);
            }
            if (this.withType) {
                addMetaData(getTypeMetaData(), getView().getTypeBox(), 0);
            }
            if (this.withInvestFocus) {
                addMetaData(getInvestFocusMetaData(), getView().getInvestFocusBox(), 0);
            }
            if (this.withAlphabet) {
                addPartition(getPartition(), getView().getAlphabetBox());
            }
            if (this.withMaturity) {
                addMaturityData(getView().getMaturityBox());
            }

            this.initialized = true;
            setQuery();
            enableMetaData(false);
        }
    }

    @Override
    public void onControllerInitialized() {
        final String[] detailIds = getConfiguration().getArray("detailIds");
        if (detailIds != null) {
            for (String detailId : detailIds) {
                this.symbolSnippets.add((SymbolSnippet) this.contextController.getSnippet(detailId));
            }
        }
        updateView();
    }

    @Override
    protected void setView(GenericListSnippetView<T> view) {
        super.setView(view);
        view.setSortLinkListener(new SortLinkSupport(this.block, this::ackParametersChanged, true, false));
    }

    private void addMetaData(FinderMetaList metaList, SelectButton sb, int defaultIndex) {
        final Menu menu = sb.getMenu();
        addMenuItem(I18n.I.all(), "", menu);
        for (int i = 0; i < metaList.getElement().size(); i++) {
            final String nameValue = metaList.getElement().get(i).getName();
            addMenuItem(nameValue, nameValue, menu);
        }
        sb.setSelectedItem(menu.getItems().get(defaultIndex));
    }

    private void addMenuItem(String name, String value, Menu menu) {
        final MenuItem item = new MenuItem(name, null);
        item.setData("value", value);
        menu.add(item);
    }

    private void addPartition(Partition partition, SelectButton sb) {
        final List<Partition.Page> pages = partition == null ? Collections.emptyList() : partition.getPage();
        final Menu menu = sb.getMenu();
        for (int i = 0; i < pages.size(); i++) {
            final String value = String.valueOf(i * this.count + this.offset);
            final String label = pages.get(i).getStart() + " - " + pages.get(i).getEnd();
            addMenuItem(label, value, menu);
        }
        sb.setSelectedItem(menu.getItems().get(0));
    }

    private void addMaturityData(SelectButton sb) {
        final int maturityLength = this.maturityPeriodLength;
        final Menu menu = sb.getMenu();
        addMenuItem(I18n.I.all(), "", menu);
        addMenuItem(I18n.I.maturityShorterYears(maturityLength), String.valueOf(0), menu);
        for (int i = 0; i < this.maturityPeriodNumber - 1; i++) {
            final String label = I18n.I.maturityBetweenYears((i + 1) * maturityLength, (i + 2) * maturityLength);
            final String value = String.valueOf(i + 1);
            addMenuItem(label, value, menu);
        }
        addMenuItem(I18n.I.maturityLongerYears(maturityLength * (this.maturityPeriodNumber)), String.valueOf(this.maturityPeriodNumber), menu);
        sb.setSelectedItem(menu.getItems().get(0));
    }

    protected void setQuery() {
        if (this.initialized && hasPagingPanel()) {
            final StringBuffer sb = new StringBuffer();
            if (this.searchPrefix != null) {
                sb.append(searchPrefix);
            }
            if (this.withType) {
                addCondition(sb, this.typeField, getView().getTypeValue());
            }
            if (this.withIssuer) {
                addCondition(sb, this.issuerField, getView().getIssuerValue());
            }
            if (this.withInvestFocus) {
                addCondition(sb, this.investFocusField, getView().getInvestFocusValue());
            }
            if (this.withMaturity) {
                addMaturity(sb, getView().getMaturityValue());
            }
            this.logger.debug("<" + getClass().getSimpleName() + ".setQuery> Query: " + sb.toString());

            setQuery(null, sb.toString());
        }
    }

    protected boolean hasPagingPanel() {
        return withType || withIssuer || withInvestFocus || withAlphabet || withMaturity;
    }

    public void setQuery(String name, String query) {
        setQueryParameters(name, query);
        ackParametersChanged();
    }

    protected void setQueryParameters(String name, String query) {
        this.titleSuffix = name;
        this.queryWithoutContraints = query;
        getConfiguration().put("titleSuffix", name);
        StringBuilder sb = new StringBuilder(query);
        for (Map.Entry<String, String> entry : constraints.entrySet()) {
            if (!query.contains(entry.getKey())) {
                if (sb.length() > 0) {
                    sb.append(" && ");
                }
                sb.append(entry.getValue());
            }
        }
        final String value = sb.toString();
        this.block.setParameter("query", value);
        this.logger.debug("<" + getClass().getSimpleName() + ".setQueryParameters> " + value);
    }

    private void addCondition(StringBuffer sb, String field, String value) {
        if (StringUtil.hasText(field) && StringUtil.hasText(value)) {
            if (sb.length() > 0) {
                sb.append(" && ");
            }
            sb.append(field).append("==").append(FinderFormElements.quote(value));
        }
    }

    private void addMaturity(StringBuffer sb, String offset) {
        if (offset.isEmpty()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(" && ");
        }
        final int n = Integer.valueOf(offset);
        final boolean isNotLast = n <= this.maturityPeriodNumber;
        if (n > 0) {
            final Date from = new Date();
            CalendarUtil.addMonthsToDate(from, n * this.maturityPeriodLength * 12);
            sb.append("expirationDate>'")
                    .append(Formatter.FORMAT_ISO_DAY.format(from))
                    .append('\'');
            if (isNotLast) {
                sb.append(" && ");
            }
        }

        if (isNotLast) {
            final Date to = new Date();
            CalendarUtil.addMonthsToDate(to, (n + 1) * this.maturityPeriodLength * 12);
            sb.append("expirationDate<='")
                    .append(Formatter.FORMAT_ISO_DAY.format(to))
                    .append('\'');
        }
    }

    protected void setNull() {
        getView().update(DefaultTableDataModel.NULL, 0, 0, 0);
        this.selection.setSymbol(null);
    }

    protected void setIssuerField(String issuerField) {
        this.issuerField = issuerField;
    }

    protected String getIssuerLabel() {
        return issuerLabel;
    }

    protected void setIssuerLabel(String issuerLabel) {
        this.issuerLabel = issuerLabel;
    }

    protected void setTypeField(String typeField) {
        this.typeField = typeField;
    }

    protected String getTypeLabel() {
        return typeLabel;
    }

    protected void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    public void setInvestFocusField(String investFocusField) {
        this.investFocusField = investFocusField;
    }

    public String getInvestFocusLabel() {
        return investFocusLabel;
    }

    public void setInvestFocusLabel(String investFocusLabel) {
        this.investFocusLabel = investFocusLabel;
    }

    protected abstract boolean isResponseOk();

    protected FinderMetaList getIssuerMetaData() {
        return null;
    }

    protected FinderMetaList getTypeMetaData() {
        return null;
    }

    protected FinderMetaList getInvestFocusMetaData() {
        return null;
    }

    protected Partition getPartition() {
        return null;
    }

    protected abstract boolean isMetaDataEnabled();

    protected abstract void enableMetaData(boolean enabled);

    protected abstract void updateTableData();

    protected abstract boolean hasSymbol(String qid);

    @Inject
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Inject
    public void setSessionData(SessionData sessionData) {
        this.sessionData = sessionData;
    }
}
