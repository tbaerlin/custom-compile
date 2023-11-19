package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.ContentPanel;

import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

/**
 * @author umaurer
 */
public class TableView extends ContentPanel {
    private final AbstractPageController controller;
    private final SnippetTableWidget tableWidget;

    public TableView(AbstractPageController controller, TableColumnModel columnModel) {
        this.controller = controller;

        addStyleName("mm-contentData"); // $NON-NLS-0$
        setHeaderVisible(false);
        setBorders(false);
        setScrollMode(Style.Scroll.AUTO);

        this.tableWidget = SnippetTableWidget.create(columnModel, "mm-snippetTable mm-WidthAuto"); // $NON-NLS-0$
        add(this.tableWidget);
    }


    public void setSortLinkListener(LinkListener<String> sortLinkListener) {
        this.tableWidget.setSortLinkListener(sortLinkListener);
    }


    public void show(TableDataModel tdm) {
        this.tableWidget.updateData(tdm);
        this.controller.getContentContainer().setContent(this);
        this.show();
    }


    String getPrintHtml() {
        return this.tableWidget.getElement().getInnerHTML();
    }
}
