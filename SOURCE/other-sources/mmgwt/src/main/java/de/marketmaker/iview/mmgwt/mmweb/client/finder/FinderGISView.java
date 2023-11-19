package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SelectorVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.CHANGE_NET;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.CHANGE_PERCENT;

/**
 * @author Michael Wohlfart
 */
public class FinderGISView<F extends AbstractFinder> extends AbstractFinderView<F> {

    FinderGISView(F controller) {
        super(controller);
    }

    protected void initColumnModels(TableColumnModel[] columnModels) {
        columnModels[0] = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.gisResearchAbbr(), -1f, TableCellRenderers.DZ_RESEARCH_POPUP_ICON_LINK)
                        .withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.isDZResearch())),
                new TableColumn(I18n.I.info(), -1f, TableCellRenderers.DZPIB_DOWNLOAD_ICON_LINK_ELEM).withVisibilityCheck(new SelectorVisibilityCheck(Selector.PRODUCT_WITH_PIB)),
                new TableColumn("WKN", -1f, TableCellRenderers.STRING, "wkn"),  // $NON-NLS$
                new TableColumn(I18n.I.dzBankOriginalProductName(), -1f, TableCellRenderers.OPTIONAL_QUOTELINK_32),
                new TableColumn(I18n.I.maturity3(), -1f, TableCellRenderers.DATE, "expiration"),  // $NON-NLS$
                new TableColumn(I18n.I.time(), -1f, TableCellRenderers.DATE, "referenceDate"),  // $NON-NLS$
                new TableColumn(I18n.I.price(), -1f, TableCellRenderers.PRICE),
                new TableColumn("+/-", -1f, CHANGE_NET).alignRight(), // $NON-NLS$
                new TableColumn("+/-%", -1f, CHANGE_PERCENT).alignRight(), // $NON-NLS$
                new TableColumn(I18n.I.differenceToUnderlyingInPercent(), -1f, CHANGE_PERCENT).alignRight(),
                new TableColumn(I18n.I.differenceToBarrierInPercent(), -1f, CHANGE_PERCENT).alignRight(),
                new TableColumn(I18n.I.yield(), -1, TableCellRenderers.PERCENT, "rendite").withToolTip("DZ BANK Rendite p.a."),  // $NON-NLS$
                new TableColumn(I18n.I.notices(), 200f, TableCellRenderers.STRING),
                new TableColumn(I18n.I.riskClassAbbr(), -1f, TableCellRenderers.STRING, "risikoklasse"),  // $NON-NLS$
                new TableColumn(I18n.I.gisBonibrief(), -1f, TableCellRenderers.PRICE, "bonibrief"),  // $NON-NLS$
                new TableColumn(I18n.I.gisBonifikation(), 60f, TableCellRenderers.STRING, "bonifikationstyp"),  // $NON-NLS$
        });
    }
}
