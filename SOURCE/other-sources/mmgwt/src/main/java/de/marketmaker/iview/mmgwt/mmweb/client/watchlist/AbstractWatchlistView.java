/*
 * AbstractWatchlistView.java
 *
 * Created on 24.04.2009 13:44:20
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.google.gwt.user.client.Window;

import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.ChartIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
abstract class AbstractWatchlistView extends ContentPanel {
    public static class ViewMode {

        final private String text;

        final private String iconCls;

        final private String pdfLink;

        protected ViewMode(String text, String iconCls, String pdfLink) {
            this.text = text;
            this.iconCls = iconCls;
            this.pdfLink = pdfLink;
        }

        public String getText() {
            return text;
        }

        public String getIconCls() {
            return iconCls;
        }

        public String getPdfLink() {
            return pdfLink;
        }
    }

    protected enum ExportType {
        CSV, XLS, WEB_QUERY
    }

    public static final String CHART_PERIOD_DEFAULT = "P3M"; // $NON-NLS-0$

    protected SelectButton btnPeriod;

    protected Button btnExport;

    protected Button btnConf = null;

    protected SelectButton viewButton;

    protected CardLayout cardLayout = new CardLayout();

    protected void addViewMenuTo(FloatingToolbar toolbar, ViewMode... modes) {
        toolbar.addSeparator();
        toolbar.addEmpty();
        toolbar.addLabel(I18n.I.view() + ":"); // $NON-NLS$
        final Menu menuViewMode = new Menu();
        for (final ViewMode mode : modes) {
            final MenuItem menuItem = new MenuItem(mode.getText()).withData("mode", mode); // $NON-NLS$
            if (!SessionData.isAsDesign()) {
                menuItem.withIcon(mode.getIconCls());
            }
            menuViewMode.add(menuItem);
        }
        this.viewButton = new SelectButton()
                .withMenu(menuViewMode)
                .withTooltip(I18n.I.switchView())
                .withClickOpensMenu()
                .withSelectionHandler(event -> {
                    final MenuItem selectedItem = event.getSelectedItem();
                    if (selectedItem != null) {
                        onViewChange((ViewMode) selectedItem.getData("mode")); // $NON-NLS$
                    }
                });
        toolbar.add(this.viewButton);

        this.btnPeriod = new SelectButton()
                .withMenu(createPeriodMenu())
                .withTooltip(I18n.I.chartPeriod())
                .withClickOpensMenu()
                .withSelectionHandler(event -> {
                    final MenuItem checkItem = event.getSelectedItem();
                    if (checkItem != null) {
                        setPeriod((String) checkItem.getData("value")); // $NON-NLS$
                    }
                });
        this.btnPeriod.setSelectedData("value", CHART_PERIOD_DEFAULT, false); // $NON-NLS$
        this.btnPeriod.setVisible(false);
        toolbar.add(this.btnPeriod);
    }

    protected void addColumnConfigButton(FloatingToolbar toolbar) {
        this.btnConf = Button.icon("column-config-icon") // $NON-NLS$
                .tooltip(I18n.I.configColumns())
                .clickHandler(event -> configureColumns())
                .build();
        toolbar.add(this.btnConf);
    }

    protected void addExportButton(FloatingToolbar toolbar) {
        if (Selector.CSV_EXPORT.isAllowed()) {
            final Menu menu = new Menu();
            menu.add(I18n.I.exportAsCvsFile()).withData("exportType", ExportType.CSV); // $NON-NLS$
            menu.add(I18n.I.exportAsXlsFile()).withData("exportType", ExportType.XLS); // $NON-NLS$
            if (getActionToken(ExportType.WEB_QUERY) != null && Selector.EXCEL_WEB_QUERY_EXPORT.isAllowed()) {
                menu.add(I18n.I.exportAsWebQueryUrl()).withData("exportType", ExportType.WEB_QUERY); // $NON-NLS$
            }
            menu.addMenuItemClickedHandler(event -> {
                final ExportType exportType = (ExportType) event.getMenuItem().getData("exportType"); // $NON-NLS$
                onExport(getUrl(exportType), getActionToken(exportType));
            });
            this.btnExport = Button.icon(SessionData.isAsDesign() ? "as-export-with-options" : "csv-button") // $NON-NLS$
                    .menu(menu)
                    .tooltip(I18n.I.exportAsTable())
                    .build();
            toolbar.add(this.btnExport);
        }
    }

    private Menu createPeriodMenu() {
        final Menu result = new Menu();
        result.add(new MenuItem(I18n.I.nMonths(1)).withData("value", "P1M"));  // $NON-NLS$
        result.add(new MenuItem(I18n.I.nMonths(3)).withData("value", "P3M"));  // $NON-NLS$
        result.add(new MenuItem(I18n.I.nMonths(6)).withData("value", "P6M"));  // $NON-NLS$
        result.add(new MenuItem(I18n.I.nYears(1)).withData("value", "P1Y"));   // $NON-NLS$
        result.add(new MenuItem(I18n.I.nYears(2)).withData("value", "P2Y"));   // $NON-NLS$
        result.add(new MenuItem(I18n.I.nYears(3)).withData("value", "P3Y"));   // $NON-NLS$
        result.add(new MenuItem(I18n.I.nYears(5)).withData("value", "P5Y"));   // $NON-NLS$
        result.add(new MenuItem(I18n.I.nYears(10)).withData("value", "P10Y")); // $NON-NLS$
        result.add(new MenuItem(I18n.I.total()).withData("value", ChartIcon.PERIOD_KEY_ALL)); // $NON-NLS$
        return result;
    }

    private void onExport(String url, String actionToken) {
        Window.open(url, "_blank", ""); // $NON-NLS$
        ActionPerformedEvent.fire(actionToken);
    }

    @SuppressWarnings("UnusedParameters")
    public void setMode(ViewMode mode) {
/*
        this.viewButton.setText(mode.getText());
        IconImage.setIconStyle(this.viewButton, mode.getIconCls());
*/
    }

    protected abstract void onViewChange(ViewMode mode);

    protected abstract void setPeriod(String period);

    protected abstract String getActionToken(ExportType exportType);
    protected abstract String getUrl(ExportType exportType);

    protected abstract void configureColumns();
}
