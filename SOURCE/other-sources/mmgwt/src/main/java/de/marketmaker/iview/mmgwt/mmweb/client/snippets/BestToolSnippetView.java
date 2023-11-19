/*
 * BestToolSnippetView.java
 *
 * Created on 9/3/14 11:33 AM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderCER;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderFND;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.FinderLinkListener;

/**
 * @author Stefan Willenbrock
 */
public class BestToolSnippetView extends SnippetView<BestToolSnippet> {

    private final SnippetTableWidget firstTable, secondTable;

    private final TableCellRenderers.LocalLinkRenderer localLinkRenderer;

    private final SimplePanel panel;

    protected BestToolSnippetView(BestToolSnippet snippet) {
        super(snippet);
        final SnippetConfiguration config = getConfiguration();

        this.localLinkRenderer = new TableCellRenderers.LocalLinkRenderer(this.snippet, "mm-bestTool-link", "mm-bestTool-link selected", "", null); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$

        this.firstTable = createTable(createTypeLinkColumn(config));
        this.secondTable = this.snippet.hasLeverageProducts() ? createTable(createTypeLinkColumnAlternative()) : null;
        this.panel = new SimplePanel();
        this.panel.setWidth("100%"); // $NON-NLS-0$
        initWidgets();
    }

    private SnippetTableWidget createTable(TableColumn firstColumn) {
        final DefaultTableColumnModel firstTableModel = new DefaultTableColumnModel(new TableColumn[]{
                firstColumn, new TableColumn(I18n.I.name(), 0.5f, TableCellRenderers.QUOTELINK_18),
                new TableColumn("+/-%", 0.2f, TableCellRenderers.CHANGE_PERCENT), // $NON-NLS-0$
                new TableColumn("", 10f, this.localLinkRenderer) // $NON-NLS-0$
        });
        return new SnippetTableWidget(firstTableModel);
    }

    private TableColumn createTypeLinkColumn(SnippetConfiguration config) {
        if ("FND".equals(config.getString("type"))) { // $NON-NLS-0$ $NON-NLS-1$
            return createTypeLinkColumn(I18n.I.type(), 0.3f, 16, LiveFinderFND.TypeFinderLinkListener.INSTANCE);
        }
        else if ("CER".equals(config.getString("type"))) { // $NON-NLS-0$ $NON-NLS-1$
            final String firstName = this.snippet.hasLeverageProducts() ? I18n.I.certificates() : I18n.I.type();
            return createTypeLinkColumn(firstName, 0.3f, 16, LiveFinderCER.INSTANCE_CER.getLinkListener());
        }
        return new TableColumn(config.getString("firstColName", I18n.I.type()), 0.3f, TableCellRenderers.DEFAULT);  // $NON-NLS-0$
    }

    private TableColumn createTypeLinkColumnAlternative() {
        return createTypeLinkColumn(I18n.I.leverageProducts(), 0.3f, 16, LiveFinderCER.INSTANCE_LEV.getLinkListener());
    }

    private void initWidgets() {
        if (this.snippet.hasLeverageProducts()) {
            final Grid g = new Grid(3, 1);
            g.setCellPadding(0);
            g.setCellSpacing(0);
            g.setWidth("100%"); // $NON-NLS-0$
            g.setWidget(0, 0, this.firstTable);
            g.setText(1, 0, " "); // $NON-NLS-0$
            g.getCellFormatter().setStyleName(1, 0, "mm-snippet-topflopBlank"); // $NON-NLS-0$
            g.setWidget(2, 0, this.secondTable);
            this.panel.add(g);
        } else {
            this.panel.add(this.firstTable);
        }
    }

    protected void onContainerAvailable() {
        this.container.setContentWidget(this.panel);
    }

    protected TableColumn createTypeLinkColumn(String columnName, float width, final int maxLength,
                                                      final FinderLinkListener<String> linkListener) {
        return new TableColumn(columnName, width)
                .withRenderer(new TableCellRenderer() {
                    public void render(Object data, StringBuffer sb, Context context) {
                        final String type = (String) data;
                        final String longValue = snippet.renderType(type);
                        final String display = getMaxLengthText(longValue, maxLength);
                        @SuppressWarnings({"StringEquality"})
                        final String tooltip = longValue == display ? null : longValue;
                        context.appendLink(linkListener.createContext(type),
                                longValue, tooltip, sb);
                    }

                    public boolean isPushRenderer() {
                        return false;
                    }

                    public String getContentClass() {
                        return null;
                    }
                });
    }

    private static String getMaxLengthText(String tmp, int maxLength) {
        return (maxLength < 0 || tmp.length() <= maxLength + 3) ? tmp : tmp.substring(0, maxLength) + "..."; // $NON-NLS-0$
    }

    protected TableCellRenderers.LocalLinkRenderer getLocalLinkRenderer() {
        return localLinkRenderer;
    }

    public void update(TableDataModel model) {
        this.firstTable.updateData(model);
    }

    public void update(TableDataModel model, TableDataModel modelAlt) {
        update(model);
        this.secondTable.updateData(modelAlt);
    }
}
