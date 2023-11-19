package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.extjs.gxt.ui.client.widget.ContentPanel;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

/**
 * @author umaurer
 */
public class IssuerSelectionView extends ContentPanel {
    private final IssuerSelectionController controller;
    private SnippetTableWidget table;
    private TableColumnModel withSubtypes;
    private TableColumnModel withoutSubtypes;
    private TableColumnModel wrapWithSubtypes;
    private TableColumnModel wrapWithoutSubtypes;

    public IssuerSelectionView(IssuerSelectionController controller) {
        addStyleName("mm-contentData"); // $NON-NLS-0$
        setHeaderVisible(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);

        this.controller = controller;

        final TableColumn countColumn = new TableColumn(I18n.I.nrOfElements(), 80, TableCellRenderers.STRING_EMPTY).alignCenter(); 
        final TableColumn underlyingColumn = new TableColumn(I18n.I.underlying(), -1f, TableCellRenderers.LINK_32); 
        final TableColumn category = new TableColumn(I18n.I.category(), -1f, TableCellRenderers.LABEL); 

        this.withSubtypes = new DefaultTableColumnModel(new TableColumn[]{
                category,
                underlyingColumn,
                countColumn,
        });
        this.withoutSubtypes = new DefaultTableColumnModel(new TableColumn[]{
                underlyingColumn,
                countColumn,
        });
        this.wrapWithSubtypes = new DefaultTableColumnModel(new TableColumn[]{
                category,
                underlyingColumn,
                countColumn,
                category,
                underlyingColumn,
                countColumn,
        });
        this.wrapWithoutSubtypes = new DefaultTableColumnModel(new TableColumn[]{
                underlyingColumn,
                countColumn,
                underlyingColumn,
                countColumn,
        });
                
        this.table = SnippetTableWidget.create(this.withSubtypes, "mm-snippetTable mm-WidthAuto"); // $NON-NLS-0$
        add(this.table);
    }

    public void show(boolean doWrap, boolean withSubtypes, TableDataModel tdm) {

        if (doWrap) {
            this.table.update(withSubtypes ? this.wrapWithSubtypes : this.wrapWithoutSubtypes, tdm);
        } else {
            this.table.update(withSubtypes ? this.withSubtypes : this.withoutSubtypes, tdm);
        }

        this.controller.getContentContainer().setContent(this);
    }
}
