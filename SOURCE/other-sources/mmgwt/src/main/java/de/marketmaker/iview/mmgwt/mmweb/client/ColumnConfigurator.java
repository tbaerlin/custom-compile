/*
 * ColumnConfigurator.java
 *
 * Created on 08.09.2009 15:03:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.PortfolioController;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserItem;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserTouch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows to configure the columns in a TableColumnModel and works as follows:<ul>
 * <li>the TableColumnModel needs to have a unique id, this id is used to store the columns
 * in the user's app config.
 * <li>the configuration is stored as a string "i,j,..,x" where i, j, ... x are the ids of the
 * columns in the table column model
 * <li>the TableColumnModel uses that information when
 * {@link de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel#getColumnOrder()}
 * is invoked.
 * <li>The ids of the columns in the TableColumnModel must NOT be changed.
 * </ul>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Michael Lösch
 * @author Ulrich Maurer
 */
public class ColumnConfigurator {

    private final DialogIfc dialog;

    private final String currentConfig;

    private final ItemChooserTouch icw;

    private final Map<String, String> itemTitleExceptions = new HashMap<>();

    final TableColumnModel model;

    final Command command;

    /**
     * Shows the column configuration dialog and invokes command iff the column configuration
     * was changed by the user.
     *
     * @param model   to be configured
     * @param command to be executed on configuration change
     */
    public static void show(TableColumnModel model, Command command) {
        // TODO 2.0: since a window exists forever, reuse a single instance
        try {
            new ColumnConfigurator(model, command).show();
        } catch (Exception e) {
            Firebug.error("cannot show ColumnConfigurator", e); // $NON-NLS-0$
        }
    }

    private ColumnConfigurator(final TableColumnModel model, final Command command) {
        this.model = model;
        this.command = command;

        this.itemTitleExceptions.put(PortfolioController.AVG_PRICE, I18n.I.averagePriceAbbr());

        this.currentConfig = getCustomColumnConfig(model);

        final List<ItemChooserItem> itemsList = createItemsList(model);

        /*
        this.icw = new ItemChooserWidgetImpl(itemsList, createSelectedItemsList(model, false), createSelectedItemsList(model, true),
                new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        onOk(model, command);
                        hide();
                    }
                }, new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        hide();
                    }
                });
                */
        this.icw = new ItemChooserTouch(itemsList, createSelectedItemsList(model, false),
                createSelectedItemsList(model, true),
                new ItemChooserTouch.Callback() {

                    @Override
                    public void onOk() {
                        ColumnConfigurator.this.onOk();
                        hide();
                    }

                    @Override
                    public void onCancel() {
                        hide();
                    }
                });

        this.dialog = Dialog.getImpl().createDialog()
                .withTitle(I18n.I.columnConfiguration())
                .withWidget(this.icw.asWidget())
/*
                .withDefaultButton(I18n.I.ok(), new Command() {
                    @Override
                    public void execute() {
                        onOk();
                    }
                })
                .withButton(I18n.I.cancel())
*/
                .withCloseButton()
                .withFocusWidget(this.icw.getDefaultFocusWidget())
        ;
    }

    private void show() {
        this.dialog.show();
    }

    private void hide() {
        this.dialog.closePopup();
    }

    private List<ItemChooserItem> createSelectedItemsList(TableColumnModel model, boolean useDefault) {
        final List<ItemChooserItem> result = new ArrayList<>();
        final int[] order = getOrder(model, useDefault);
        for (int col : order) {
            final TableColumn tableColumn = model.getTableColumn(col);
            addItemChooserItem(result, tableColumn);
        }
        return result;
    }

    private void addItemChooserItem(List<ItemChooserItem> result, TableColumn tableColumn) {
        if (tableColumn.isFixed() || !tableColumn.isVisible()) {
            return;
        }
        result.add(new ItemChooserItem(
                tableColumn.getTitleToolTip() != null ? getException(tableColumn.getTitleToolTip()) :
                        getException(tableColumn.getTitle()), tableColumn.getId(), tableColumn.isFixed())
        );
    }

    private List<ItemChooserItem> createItemsList(TableColumnModel model) {
        final List<ItemChooserItem> result = new ArrayList<>();
        for (int i = 0; i < model.getColumnCount(); i++) {
            final TableColumn tableColumn = model.getTableColumn(i);
            addItemChooserItem(result, tableColumn);
        }
        return result;
    }

    private void onOk() {
        final String conf = createConfig(model);
        final int[] order = model.getColumnOrder(conf.split(",")); // $NON-NLS-0$
        final int[] defaultOrder = model.getDefaultColumnOrder();

        if (Arrays.equals(order, defaultOrder)) {
            setCustomColumnConfig(model, null);
            if (this.currentConfig != null) {
                onChangedConfig(command);
            }
            return;
        }

        setCustomColumnConfig(model, conf);
        if (!conf.equals(this.currentConfig)) {
            onChangedConfig(command);
        }
    }

    private String createConfig(TableColumnModel model) {
        final StringBuffer sb = new StringBuffer();
        appendFixedColumns(model, sb);
        for (int i = 0; i < this.icw.getSelectedRowsCount(); i++) {
            append(sb, this.icw.getColumnValue(i));
        }
        return sb.toString();
    }

    private void appendFixedColumns(TableColumnModel model, StringBuffer sb) {
        for (int col = 0; col < model.getColumnCount(); col++) {
            final TableColumn tc = model.getTableColumn(col);
            if (!tc.isFixed()) {
                return;
            }
            append(sb, tc.getId());
        }
    }

    private void append(StringBuffer sb, String col) {
        if (sb.length() > 0) {
            sb.append(","); // $NON-NLS-0$
        }
        sb.append(col);
    }

    private void onChangedConfig(Command command) {
        Scheduler.get().scheduleDeferred(command);
    }

    private void setCustomColumnConfig(TableColumnModel model, String value) {
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(model.getId(), value);
    }

    private String getCustomColumnConfig(TableColumnModel model) {
        return SessionData.INSTANCE.getUserProperty(model.getId());
    }

    private String getException(String value) {
        if (this.itemTitleExceptions.containsKey(value)) {
            return this.itemTitleExceptions.get(value);
        }
        else {
            return value;
        }
    }

    private int[] getOrder(TableColumnModel model, boolean useDefault) {
        if (this.currentConfig == null || useDefault) {
            return model.getDefaultColumnOrder();
        }
        return model.getColumnOrder();
    }
}