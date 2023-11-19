package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.Alert;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.AlertController;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.LimitsUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.LimitsUpdatedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.MetadataAware;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.ActionRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.AlertUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.List;

/**
 * @author Ulrich Maurer
 *         Date: 03.01.13
 */
public class AlertSnippet extends AbstractSnippet<AlertSnippet, SnippetTableView<AlertSnippet>> implements MetadataAware, LimitsUpdatedHandler {
    public static class Class extends SnippetClass {
        public Class() {
            super("Alert"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new AlertSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("colSpan", 3); // $NON-NLS$
        }
    }

    private InstrumentData currentInstrument;

    protected AlertSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        final TableColumnModel tableColumnModel = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.limitActions(), 0.05f, new ActionRenderer<>(AlertController.ALERT_ACTION_HANDLER)),
                new TableColumn(I18n.I.numberAbbr(), 0.02f, TableCellRenderers.STRING),
                new TableColumn(I18n.I.limitName(), 0.23f, TableCellRenderers.STRING),
                new TableColumn(I18n.I.limitFieldName(), 0.05f, TableCellRenderers.STRING),
                new TableColumn(I18n.I.marketName(), 0.1f, TableCellRenderers.STRING),
                new TableColumn(I18n.I.referenceValue(), 0.05f, TableCellRenderers.PRICE),
                new TableColumn(I18n.I.lowerBorder(), 0.1f, TableCellRenderers.STRING_RIGHT),
                new TableColumn(I18n.I.upperBorder(), 0.1f, TableCellRenderers.STRING_RIGHT),
                new TableColumn(I18n.I.limitCreationTime(), 0.1f, TableCellRenderers.LOCAL_TZ_DATE_TIME),
                new TableColumn(I18n.I.limitTriggered(), 0.2f, TableCellRenderers.ALERT_STATUS)
        });
        final SnippetTableView<AlertSnippet> view;
        if (FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled()) {
            view = new SnippetToggleTableView<>(this, tableColumnModel);
            refreshVisibilityFromAppConfig((SnippetToggleTableView)view);
            EventBusRegistry.get().addHandler(ConfigChangedEvent.getType(), event -> refreshVisibilityFromAppConfig((SnippetToggleTableView)view));
        } else {
            view = SnippetTableView.create(this, tableColumnModel);
        }
        setView(view);
        EventBusRegistry.get().addHandler(LimitsUpdatedEvent.getType(), this);
    }

    private void refreshVisibilityFromAppConfig(SnippetToggleTableView view) {
        boolean userConf = !SessionData.INSTANCE.getUser().getAppConfig()
                .getBooleanProperty(AppConfig.HIDE_LIMITS, false);
        view.setUserConfVisibility(userConf);
        view.setContentVisible(userConf);
    }

    @Override
    public void destroy() {
        //ignore
    }

    @Override
    public void updateView() {
        boolean visible = false;
        if (!SessionData.INSTANCE.isAnonymous() && this.currentInstrument != null) {
            final List<Alert> alerts = AlertController.INSTANCE.getAlertInformation(this.currentInstrument.getIid());
            visible = alerts != null;
            updateTitle(alerts);
            getView().update(createDataModel(alerts));
        }
        this.contextController.updateVisibility(getView(), visible);
    }

    private void updateTitle(List<Alert> alerts) {
        if (alerts != null
                && FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled()) {
            if (((SnippetToggleTableView)getView()).isContentVisible()) {
                getConfiguration().remove("titleSuffix"); // $NON-NLS$
            } else {
                getConfiguration().put("titleSuffix", createTitleSuffix(alerts)); // $NON-NLS$
            }
        }
    }

    private String createTitleSuffix(List<Alert> alerts) {
        int fired = 0;
        int totalCount = 0;
        for (Alert alert : alerts) {
            totalCount++;
            // alerts can be restarted and fired/executed multiple times
            if (alert.getExecution().size() > 0) {
                fired++;
            }
        }
        return " " + fired + "/" +  totalCount + " " + I18n.I.triggered(); // $NON-NLS$
    }

    @Override
    public boolean isMetadataNeeded() {
        return true;
    }

    @Override
    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        this.currentInstrument = metadata.getInstrumentdata();
    }

    @Override
    public void onLimitsUpdated(LimitsUpdatedEvent event) {
        updateView();
    }

    private TableDataModel createDataModel(List<Alert> alerts) {
        if (alerts == null) {
            return DefaultTableDataModel.NULL;
        }
        return DefaultTableDataModel.create(alerts, new AbstractRowMapper<Alert>() {
            private int n = 0;

            public Object[] mapRow(Alert alert) {
                return new Object[]{
                        alert,
                        Integer.toString(++this.n),
                        alert.getName(),
                        AlertUtil.getLimitFieldName(alert),
                        alert.getQuotedata().getMarketName(),
                        alert.getReferenceValue(),
                        getBoundary(alert.getLowerBoundary(), alert.getLowerBoundaryPercent()),
                        getBoundary(alert.getUpperBoundary(), alert.getUpperBoundaryPercent()),
                        alert.getCreated(),
                        alert
                };
            }
        });
    }

    private String getBoundary(String boundary, String boundaryPercent) {
        if (boundary == null) {
            return "--"; // $NON-NLS-0$
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(Renderer.PRICE23.render(boundary));
        if (boundaryPercent != null) {
            sb.append(" (").append(Renderer.PRICE.render(boundaryPercent)).append("%)"); // $NON-NLS-0$ $NON-NLS-1$
        }
        return sb.toString();
    }

}
