package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;

/**
 * @author Michael Wohlfart
 */
public class FinderResearchView<F extends AbstractFinder> extends AbstractFinderView<F> {

    FinderResearchView(F controller) {
        super(controller);
    }

    // "Folgende Spalten sind in der Suchergebnisliste anzuzeigen:
    // Titel, Datum, Kategorie, Anlageurteil, Branche, Risikoklasse, Fair Value, Abst. Fair Value, Land"
    @Override
    protected void initColumnModels(TableColumnModel[] columnModels) {
        columnModels[0] = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.title(), -1f, TableCellRenderers.LINK_90, "title"),  // $NON-NLS$
                new TableColumn(I18n.I.date(), -1f, TableCellRenderers.DATE_AND_TIME, "date"), // $NON-NLS$
                new TableColumn(I18n.I.category(), -1f, TableCellRenderers.STRING, "assetClass"), // $NON-NLS$
                new TableColumn(I18n.I.recommendation(), 70f, TableCellRenderers.RESEARCH_RECOMMENDATION, "recommendation"), // $NON-NLS$
                new TableColumn(I18n.I.sector(), -1f, TableCellRenderers.STRING, "sector"), // $NON-NLS$
                new TableColumn(I18n.I.country(), -1f, TableCellRenderers.MULTILINE_ARRAY, "country"), // $NON-NLS$
        });
    }

}
