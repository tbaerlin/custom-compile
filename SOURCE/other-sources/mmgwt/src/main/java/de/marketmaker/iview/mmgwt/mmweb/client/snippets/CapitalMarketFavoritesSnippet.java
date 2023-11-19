package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.GISFinder;
import de.marketmaker.iview.dmxml.GISFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderGIS;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.table.ColumnTriggeredSubheading;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SelectorVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.CHANGE_NET;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.CHANGE_PERCENT;


public class CapitalMarketFavoritesSnippet extends AbstractSnippet<CapitalMarketFavoritesSnippet, SnippetTableView<CapitalMarketFavoritesSnippet>> {
    private int columnCount;

    public static class Class extends SnippetClass {
        public Class() {
            super("CapitalMarketFavorites", I18n.I.capitalMarketFavorites()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new CapitalMarketFavoritesSnippet(context, config);
        }
    }

    private DmxmlContext.Block<GISFinder> block;

    public CapitalMarketFavoritesSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        setUpBlockAndView();
    }

    private void setUpBlockAndView() {
        this.block = createBlock("GIS_Finder"); // $NON-NLS$
        this.block.setParameter("query", "kapitalmarktFavorit == 'true'"); // $NON-NLS$
        this.block.setParameter("sortBy", "indexPosition");  // $NON-NLS$
        this.block.setParameter("ascending", true);  // $NON-NLS-0$
        this.block.setParameter("count", 100); // $NON-NLS-0$

        final SnippetTableView<CapitalMarketFavoritesSnippet> view = new SnippetTableView<>(this,
                createColumnModel(), "mm-snippetTable mm-snippet-priceList"); // $NON-NLS-0$
        view.withSubheading(new ColumnTriggeredSubheading(0));
        this.setView(view);
        onParametersChanged();
    }

    private DefaultTableColumnModel createColumnModel() {
        final List<TableColumn> columns = getColumnList();
        this.columnCount = columns.size();
        return new DefaultTableColumnModel(columns.toArray(new TableColumn[columns.size()]));
    }

    private List<TableColumn> getColumnList() {
        final List<TableColumn> result = new ArrayList<>();
        result.add(new TableColumn("subheaders", -1f, TableCellRenderers.STRING)  // column for subheaders  $NON-NLS$
                .withVisibilityCheck(SimpleVisibilityCheck.valueOf(false)));
        result.add(new TableColumn(I18n.I.gisResearchAbbr(), -1f, TableCellRenderers.DZ_RESEARCH_POPUP_ICON_LINK)
                .withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.isDZResearch())));
        result.add(new TableColumn(I18n.I.info(), -1f, TableCellRenderers.DZPIB_DOWNLOAD_ICON_LINK_ELEM).withVisibilityCheck(new SelectorVisibilityCheck(Selector.PRODUCT_WITH_PIB)));
        result.add(new TableColumn(I18n.I.name(), -1f, TableCellRenderers.OPTIONAL_QUOTELINK_27));
        result.add(new TableColumn("WKN", -1f, TableCellRenderers.STRING_10));  // $NON-NLS$
        result.add(new TableColumn(I18n.I.gisRendite(), -1, TableCellRenderers.PERCENT, "rendite"));  // $NON-NLS$
        result.add(new TableColumn(I18n.I.gisExpiration(), -1f, TableCellRenderers.DATE, "expiration"));  // $NON-NLS$
        result.add(new TableColumn(I18n.I.time(), -1f, TableCellRenderers.DATE, "referenceDate"));  // $NON-NLS$
        result.add(new TableColumn(I18n.I.price(), -1f, TableCellRenderers.PRICE));
        result.add(new TableColumn("+/-", -1f, CHANGE_NET).alignRight()); // $NON-NLS$
        result.add(new TableColumn("+/-%", -1f, CHANGE_PERCENT).alignRight()); // $NON-NLS$
        result.add(new TableColumn(I18n.I.gisHinweis(), -1f, TableCellRenderers.STRING_10, "hinweise")); // $NON-NLS$
        result.add(new TableColumn(I18n.I.riskClassAbbr(), -1f, TableCellRenderers.STRING, "risikoklasse"));  // $NON-NLS$
        result.add(new TableColumn(I18n.I.gisBonibrief(), -1f, TableCellRenderers.PRICE, "bonibrief"));  // $NON-NLS$
        result.add(new TableColumn(I18n.I.gisBonifikation(), -1f, TableCellRenderers.STRING, "bonifikationstyp"));  // $NON-NLS$
        return result;
    }

    @Override
    public void destroy() {
        destroyBlock(this.block);
    }

    @Override
    public void updateView() {
        if (!this.block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final GISFinder result = this.block.getResult();
        final List<GISFinderElement> rowElements = result.getElement();
        final DefaultTableDataModel tableModel = new DefaultTableDataModel(rowElements.size(), this.columnCount);
        int row = 0;
        for (final GISFinderElement  elem : rowElements) {
            final QuoteWithInstrument qwi = QuoteWithInstrument
                    .createQuoteWithInstrument(elem.getInstrumentdata(), elem.getQuotedata(), elem.getBezeichnung());
            final Price price = Price.create(elem);
            int col = -1;
            tableModel.setValueAt(row, ++col, getSubHeaderString(elem));
            tableModel.setValueAt(row, ++col, qwi);
            tableModel.setValueAt(row, ++col, elem);
            tableModel.setValueAt(row, ++col, qwi);
            tableModel.setValueAt(row, ++col, elem.getWkn());
            tableModel.setValueAt(row, ++col, elem.getRendite());
            tableModel.setValueAt(row, ++col, elem.getExpiration());
            tableModel.setValueAt(row, ++col, elem.getReferenceDate());
            tableModel.setValueAt(row, ++col, price.getLastPrice().getPrice());
            tableModel.setValueAt(row, ++col, price.getChangeNet());
            tableModel.setValueAt(row, ++col, price.getChangePercent());
            tableModel.setValueAt(row, ++col, LiveFinderGIS.createHinweisString(elem));
            tableModel.setValueAt(row, ++col, elem.getRisikoklasse());
            tableModel.setValueAt(row, ++col, elem.getBonibrief());
            tableModel.setValueAt(row, ++col, elem.getBonifikationstyp());
            row++;
        }
        getView().update(tableModel);
    }

    private String getSubHeaderString(GISFinderElement elem) {
        // this must be in sync with the sort order in the backend (DzBankRecordProvider)
        // all elems with topProdukt flag enabled must be adjacent otherwise the subheader will be messed up
        if (elem.isTopProdukt()) {
            return I18n.I.gisProduktschaufenster();
        } else {
            return elem.getOffertenkategorie();
        }
    }

}
