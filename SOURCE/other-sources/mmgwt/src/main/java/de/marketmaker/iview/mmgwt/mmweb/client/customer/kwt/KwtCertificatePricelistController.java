package de.marketmaker.iview.mmgwt.mmweb.client.customer.kwt;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.Command;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.CERFinder;
import de.marketmaker.iview.dmxml.CERFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.MultiTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author umaurer
 */
public class KwtCertificatePricelistController extends AbstractPageController {
    private final MultiTableView view;

    private final List<String> listNames = new ArrayList<String>();

    private final List<DmxmlContext.Block<CERFinder>> listBlocks = new ArrayList<DmxmlContext.Block<CERFinder>>();

    private final SortLinkSupport sortLinkSupport;


    public KwtCertificatePricelistController(ContentContainer contentContainer) {
        super(contentContainer);
        final TableColumnModel tcm = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn("ISIN", -1f, TableCellRenderers.DEFAULT, "isin"), // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn("WKN", -1f, TableCellRenderers.DEFAULT, "wkn"), // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn(I18n.I.name(), -1f, TableCellRenderers.QUOTELINK_22, "name"),  // $NON-NLS-0$
                new TableColumn(I18n.I.issuer(), -1f, TableCellRenderers.DEFAULT, "issuername"),  // $NON-NLS-0$
                new TableColumn(I18n.I.underlying(), -1f, TableCellRenderers.DEFAULT, "underlyingName"),  // $NON-NLS-0$
                new TableColumn(I18n.I.category(), -1f, TableCellRenderers.DEFAULT, "certificateSubtype"),  // $NON-NLS-0$
                new TableColumn(I18n.I.yieldRelativePerYearAbbr(), -1f, TableCellRenderers.PERCENT, "yieldRelativePerYear"),  // $NON-NLS-0$
                new TableColumn(I18n.I.expirationPeriod(), -1f, TableCellRenderers.DEFAULT, "expirationDate"),  // $NON-NLS-0$
                new TableColumn(I18n.I.unchangedYieldRelativePerYearAbbr(), -1f, TableCellRenderers.PERCENT, "unchangedYieldRelativePerYear"),  // $NON-NLS-0$
                new TableColumn(I18n.I.cap(), -1f, TableCellRenderers.PRICE23, "cap"),  // $NON-NLS-0$
                new TableColumn(I18n.I.bonusLevel(), -1f, TableCellRenderers.PRICE23), 
                new TableColumn(I18n.I.securitiesLevel(), -1f, TableCellRenderers.PRICE23), 
                new TableColumn(I18n.I.bufferAndDiscount(), -1f, TableCellRenderers.PERCENT), 
        });
        this.view = new MultiTableView(contentContainer, tcm);


        this.sortLinkSupport = new SortLinkSupport(null, new Command() {
            public void execute() {
                adjustSorting();
            }
        }, false, true);
        this.view.setSortLinkListener(this.sortLinkSupport);

    }

    private void adjustSorting() {
        if (this.listBlocks.size() > 1) {
            final DmxmlContext.Block<CERFinder> blockFirst = this.listBlocks.get(0);
            for (int i = 1; i < this.listBlocks.size(); i++) {
                final DmxmlContext.Block<CERFinder> block = this.listBlocks.get(i);
                block.setParameter("sortBy", blockFirst.getParameter("sortBy")); // $NON-NLS-0$ $NON-NLS-1$
                block.setParameter("ascending", blockFirst.getParameter("ascending")); // $NON-NLS-0$ $NON-NLS-1$
            }
        }
        refresh();
    }


    private void removeAllBlocks() {
        this.listNames.clear();
        for (DmxmlContext.Block<CERFinder> block : this.listBlocks) {
            this.context.removeBlock(block);
        }
        this.listBlocks.clear();
    }

    private void addBlock(final String name, final JSONWrapper jsonArray) {
        this.listNames.add(name);
        final DmxmlContext.Block<CERFinder> block = this.context.addBlock("CER_Finder"); // $NON-NLS-0$
        block.setParameter("offset", "0"); // $NON-NLS-0$ $NON-NLS-1$
        block.setParameter("count", "100"); // $NON-NLS-0$ $NON-NLS-1$
        block.setParameter("sortBy", "name"); // $NON-NLS-0$ $NON-NLS-1$
        block.setParameter("ascending", "true"); // $NON-NLS-0$ $NON-NLS-1$
        block.setParameter("query", createFinderQuery("vwdCode", jsonArray)); // $NON-NLS-0$ $NON-NLS-1$
        block.setParameter("providerPreference", SessionData.INSTANCE.getGuiDefValue("providerPreference")); // $NON-NLS$
        this.listBlocks.add(block);
    }

    private void addJSONArray(final String namePrefix, boolean addPrefix,
                              final JSONWrapper wrapper) {
        final JSONWrapper children = wrapper.get("children"); // $NON-NLS-0$
        if (children.size() == 0) {
            return;
        }
        final String nameSuffix = wrapper.get("name").stringValue(); // $NON-NLS-0$
        if (nameSuffix == null) {
            return;
        }
        final String name = (!addPrefix || namePrefix == null)
                ? nameSuffix
                : (namePrefix + " / " + nameSuffix); // $NON-NLS-0$

        final JSONWrapper firstChild = children.get(0);
        if (firstChild.get("children").isArray()) { // $NON-NLS-0$
            for (int i = 0; i < children.size(); i++) {
                addJSONArray(name, namePrefix != null, children.get(i));
            }
        }
        else if (firstChild.get("symbol").isString()) { // $NON-NLS-0$
            addBlock(name, children);
        }
    }


    private String createFinderQuery(final String key, final JSONWrapper jsonArray) {
        final StringBuilder sbQuery = new StringBuilder();
        String divider = ""; // $NON-NLS-0$
        sbQuery.append(key).append("=='"); // $NON-NLS-0$
        for (int i = 0; i < jsonArray.size(); i++) {
            final String symbol = jsonArray.get(i).get("symbol").stringValue(); // $NON-NLS-0$
            if (symbol != null) {
                sbQuery.append(divider).append(symbol);
                divider = "@"; // $NON-NLS-0$
            }
        }
        sbQuery.append("'"); // $NON-NLS-0$
        return sbQuery.toString();
    }


    public void onPlaceChange(PlaceChangeEvent event) {
        final String jsonKey = event.getHistoryToken().get(1, null);
        assert (jsonKey != null);

        final JSONWrapper jsonWrapper = SessionData.INSTANCE.getGuiDef(KwtCertificatePricelistsController.JSON_KEY_PRICELISTS);
        if (jsonWrapper == null) {
            Firebug.log("guidef for Kwtzertis not available"); // $NON-NLS-0$
            return;
        }
        final JSONWrapper tabWrapper = jsonWrapper.get(jsonKey);
        if (tabWrapper == JSONWrapper.INVALID) {
            Firebug.log("configuration of tab not found: " + jsonKey); // $NON-NLS-0$
            return;
        }
        removeAllBlocks();
        // recursive iteration of available lists
        addJSONArray(null, false, tabWrapper);
        refresh();
    }

    @Override
    protected void onResult() {
        this.view.start();

        for (int i = 0; i < this.listBlocks.size(); i++) {
            final String name = this.listNames.get(i);
            this.view.add(name, getTableData(this.listBlocks.get(i)));
        }
        if (!this.listBlocks.isEmpty()) {
            final DmxmlContext.Block<CERFinder> blockFirst = this.listBlocks.get(0);
            this.sortLinkSupport.setBlock(blockFirst);
        }

        this.view.finish();
    }

    private TableDataModel getTableData(DmxmlContext.Block<CERFinder> block) {
        if (!block.isResponseOk()) {
            return DefaultTableDataModel.NULL;
        }
        final CERFinder cerFinder = block.getResult();
        final List<Object[]> list = new ArrayList<Object[]>();
        for (final CERFinderElement e : cerFinder.getElement()) {
            final boolean isBonus = isBonus(e);

            list.add(new Object[]{
                    e.getInstrumentdata().getIsin(),
                    e.getInstrumentdata().getWkn(),
                    new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata()),
                    e.getIssuername(),
                    e.getUnderlyingName(),
                    e.getCertificateSubtype(),
                    e.getYieldRelativePerYear(),
                    e.getExpirationDate(),
                    e.getUnchangedYieldRelativePerYear(),
                    getParticipationLevelOrCap(e, isBonus),
                    e.getBonusLevel(),
                    e.getBarrier(),
                    isBonus ? e.getUnderlyingToCapRelative() : e.getDiscountRelative()
            });
        }
        return DefaultTableDataModel.create(list).withSort(cerFinder.getSort());
    }

    private String getParticipationLevelOrCap(CERFinderElement e, boolean bonus) {
        if (bonus) {
            return isDefinedAndPositive(e.getParticipationLevel()) ? e.getParticipationLevel() : null;
        }
        else {
            return isDefinedAndPositive(e.getCap()) ? e.getCap() : null;
        }
    }

    private boolean isDefinedAndPositive(String s) {
        return s != null && Double.parseDouble(s) > 0;
    }

    private boolean isBonus(CERFinderElement e) {
        return e.getCertificateSubtype() != null
                && e.getCertificateSubtype().toLowerCase().indexOf("bonus") >= 0; // $NON-NLS-0$
    }

    @Override
    public String getPrintHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"mm-small-font\">") // $NON-NLS-0$
                .append(this.view.getHtml())
                .append("</div>"); // $NON-NLS-0$
        return sb.toString();
    }
}

