package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.ui.FlexTable;
import com.extjs.gxt.ui.client.widget.ContentPanel;

import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

/**
 * @author umaurer
 */
public class MultiTableView extends ContentPanel {
    private final ContentContainer container;
    private final TableColumnModel columnModel;
    private String tableStyleName = "mm-snippetTable"; // $NON-NLS-0$
    private FlexTable table = new FlexTable();
    private int rowId = 0;
    private LinkListener<String> sortLinkListener;
    private final FlexTable.FlexCellFormatter formatter;

    public MultiTableView(ContentContainer container, TableColumnModel columnModel) {
        this.container = container;
        this.columnModel = columnModel;
        addStyleName("mm-contentData"); // $NON-NLS-0$
        setBorders(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);
        add(this.table);
        this.table.setCellSpacing(4);
        this.table.setCellPadding(0);
        this.table.setStyleName("mm-gridSnippets"); // $NON-NLS-0$
        this.formatter = this.table.getFlexCellFormatter();
    }

    public void setTableStyleName(String tableStyleName) {
        this.tableStyleName = tableStyleName;
    }

    public void setSortLinkListener(LinkListener<String> sortLinkListener) {
        this.sortLinkListener = sortLinkListener;
    }

    public void start() {
        this.table.clear();
        for (int row = this.table.getRowCount() - 1; row >= 0; row--) {
            this.table.removeRow(row);
        }
        this.rowId = 0;
    }

    public void add(String title, TableDataModel tdm) {
        final SnippetTableWidget stw = SnippetTableWidget.create(this.columnModel, this.tableStyleName);
        if (this.sortLinkListener != null) {
            stw.setSortLinkListener(this.sortLinkListener);
        }
        stw.updateData(tdm);

        final ContentPanel panel = new ContentPanel();
        panel.setHeading(title);
        panel.setStyleName("mm-snippet"); // $NON-NLS-0$
        panel.add(stw);
        panel.setBorders(false);
        this.table.setWidget(this.rowId, 0, panel);
        this.formatter.setStyleName(this.rowId, 0, "mm-gridSnippets"); // $NON-NLS-0$

        this.rowId++;
    }

    public String getHtml() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.table.getRowCount(); i++) {
            final Widget table = this.table.getWidget(i, 0);
            if (table != null) {
                sb.append(table.getElement().getInnerHTML())
                        .append("<br>"); // $NON-NLS-0$
            }
        }
        return sb.toString();
    }

    public void finish() {
        this.doLayout();
        this.container.setContent(this);
        this.show();
    }
}
