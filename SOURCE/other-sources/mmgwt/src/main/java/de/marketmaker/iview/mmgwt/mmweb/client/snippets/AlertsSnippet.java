/*
 * AlertsSnippet.java
 *
 * Created on 06.11.2015 08:56
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.dmxml.Alert;
import de.marketmaker.iview.mmgwt.mmweb.client.AlertController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.LimitsUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.LimitsUpdatedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.ActionRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.AlertUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author mdick
 */
public class AlertsSnippet extends AbstractSnippet<AlertsSnippet, SnippetTableView<AlertsSnippet>>
        implements LimitsUpdatedHandler {

    public static class Class extends SnippetClass {
        public Class() {
            super("Alerts", I18n.I.limits()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new AlertsSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            super.addDefaultParameters(config);
            config.put("colSpan", TableConfig.DEFAULT.colSpan()); // $NON-NLS$
            config.put(TABLE_CONFIG_KEY, TableConfig.DEFAULT.name());
            config.put(UNACKNOWLEDGED_ONLY_KEY, UNACKNOWLEDGED_ONLY_DEFAULT);
        }
    }

    public static final String TABLE_CONFIG_KEY = "tableConfig";  // $NON-NLS$

    public static final String UNACKNOWLEDGED_ONLY_KEY = "unacknowledgedOnly";  // $NON-NLS$

    public static final boolean UNACKNOWLEDGED_ONLY_DEFAULT = true;

    public enum TableConfig {
        SIMPLE(I18n.I.simple(), 1), EXTENDED(I18n.I.extended(), 3);

        public final static TableConfig DEFAULT = EXTENDED;

        private final String label;

        private final int colSpan;

        TableConfig(String label, int colSpan) {
            this.label = label;
            this.colSpan = colSpan;
        }

        public String label() {
            return this.label;
        }

        public int colSpan() {
            return this.colSpan;
        }
    }

    private final HandlerRegistration limitsUpdatedHandlerRegistration;

    public AlertsSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
        setView(SnippetTableView.create(this, createTableColumnModel(getTableConfig())));
        this.limitsUpdatedHandlerRegistration = EventBusRegistry.get().addHandler(LimitsUpdatedEvent.getType(), this);
    }

    public DefaultTableColumnModel createTableColumnModel(TableConfig tableConfig) {
        final DefaultTableColumnModel model;

        switch (tableConfig) {
            case SIMPLE:
                model = new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn(I18n.I.limitActions(), 0.05f, new ActionRenderer<>(AlertController.ALERT_ACTION_HANDLER)),
                        new TableColumn(I18n.I.limitName(), 0.1f, TableCellRenderers.STRING),
                        new TableColumn(I18n.I.instrument(), 0.18f, TableCellRenderers.QUOTELINK_22),
                        new TableColumn(I18n.I.limitTriggered(), 0.15f, TableCellRenderers.ALERT_STATUS)
                });
                break;

            case EXTENDED:
            default:
                model = new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn(I18n.I.limitActions(), 0.05f, new ActionRenderer<>(AlertController.ALERT_ACTION_HANDLER)),
                        new TableColumn(I18n.I.numberAbbr(), 0.02f, TableCellRenderers.STRING),
                        new TableColumn(I18n.I.limitName(), 0.1f, TableCellRenderers.STRING),
                        new TableColumn(I18n.I.instrument(), 0.18f, TableCellRenderers.QUOTELINK_22),
                        new TableColumn(I18n.I.marketName(), 0.05f, TableCellRenderers.STRING_CENTER),
                        new TableColumn(I18n.I.limitFieldName(), 0.05f, TableCellRenderers.STRING),
                        new TableColumn(I18n.I.absolute(), 0.05f, TableCellRenderers.PRICE23),
                        new TableColumn(I18n.I.percentalAbbr(), 0.05f, TableCellRenderers.PRICE_PERCENT),
                        new TableColumn(I18n.I.absolute(), 0.05f, TableCellRenderers.PRICE23),
                        new TableColumn(I18n.I.percentalAbbr(), 0.05f, TableCellRenderers.PRICE_PERCENT),
                        new TableColumn(I18n.I.referenceValue(), 0.05f, TableCellRenderers.PRICE),
                        new TableColumn(I18n.I.limitCreationTime(), 0.10f, TableCellRenderers.LOCAL_TZ_DATE_TIME),
                        new TableColumn(I18n.I.limitTriggered(), 0.15f, TableCellRenderers.ALERT_STATUS)
                });
                model.groupColumns(6, 8, I18n.I.lowerLimit());
                model.groupColumns(8, 10, I18n.I.upperLimit());
                break;
        }
        return model;
    }

    public TableConfig getTableConfig() {
        return TableConfig.valueOf(getConfiguration().getString(TABLE_CONFIG_KEY, TableConfig.DEFAULT.name()));
    }

    @Override
    public void onLimitsUpdated(LimitsUpdatedEvent event) {
        updateView();
    }

    @Override
    public void updateView() {
        final List<Alert> alerts = isUnacknowledgedOnly() ?
                AlertController.INSTANCE.getUnacknowledgedAlertInformation() :
                AlertController.INSTANCE.getAlertInformation();

        if (alerts == null) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final DefaultTableDataModel model = createTableDataModel(getTableConfig(), alerts);
        getView().update(model);
    }

    public DefaultTableDataModel createTableDataModel(TableConfig tableConfig, List<Alert> alerts) {
        switch (tableConfig) {
            case SIMPLE:
                return DefaultTableDataModel.create(alerts, new AbstractRowMapper<Alert>() {
                    @Override
                    public Object[] mapRow(Alert alert) {
                        return new Object[]{
                                alert,
                                alert.getName(),
                                new QuoteWithInstrument(alert.getInstrumentdata(), alert.getQuotedata()),
                                alert
                        };
                    }
                });

            case EXTENDED:
            default:
                return DefaultTableDataModel.create(alerts, new AbstractRowMapper<Alert>() {
                    private String iid = null;

                    private int n;

                    @Override
                    public Object[] mapRow(Alert alert) {
                        if (!alert.getInstrumentdata().getIid().equals(this.iid)) {
                            this.n = 0;
                        }
                        this.iid = alert.getInstrumentdata().getIid();
                        return new Object[]{
                                alert,
                                Integer.toString(++this.n),
                                alert.getName(),
                                new QuoteWithInstrument(alert.getInstrumentdata(), alert.getQuotedata()),
                                alert.getQuotedata().getMarketName(),
                                AlertUtil.getLimitFieldName(alert),
                                alert.getLowerBoundary(),
                                alert.getLowerBoundaryPercent(),
                                alert.getUpperBoundary(),
                                alert.getUpperBoundaryPercent(),
                                alert.getReferenceValue(),
                                alert.getCreated(),
                                alert
                        };
                    }
                });
        }
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void configure(Widget triggerWidget) {
        final Menu menu = new Menu();
        //TODO: replace with Stream.of/Arrays.stream when it is available Somewhere in Time
        for (TableConfig tc : TableConfig.values()) {
            final MenuItem menuItem = new MenuItem(tc.label()).withClickHandler(clickEvent -> {
                getConfiguration().put(TABLE_CONFIG_KEY, tc.name());
                ackParametersChanged();
            });
            menu.add(menuItem);

            if (tc.equals(getTableConfig())) {
                menuItem.addStyleName("selected");
                menu.setSelectedItem(menuItem);
            }
            else {
                menuItem.removeStyleName("selected");
            }
        }

        menu.addSeparator();

        final MenuItem mi = new MenuItem(I18n.I.showUnacknowledgedLimitsOnly())
                .withClickHandler(e -> {
                    getConfiguration().put(UNACKNOWLEDGED_ONLY_KEY, !isUnacknowledgedOnly());
                    ackParametersChanged();
                });
        if (isUnacknowledgedOnly()) {
            mi.withIcon("pm-box-checked"); // $NON-NLS$
        }
        menu.add(mi);

        menu.show(triggerWidget);
    }

    public boolean isUnacknowledgedOnly() {
        return getConfiguration().getBoolean(UNACKNOWLEDGED_ONLY_KEY, UNACKNOWLEDGED_ONLY_DEFAULT);
    }

    @Override
    protected void onParametersChanged() {
        getView().update(createTableColumnModel(getTableConfig()), DefaultTableDataModel.NULL);
        updateView();
    }

    @Override
    public void destroy() {
        this.limitsUpdatedHandlerRegistration.removeHandler();
    }
}
