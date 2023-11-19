package de.marketmaker.iview.mmgwt.mmweb.client.customer.wgz;

import com.google.gwt.dom.client.Element;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.GISWGZCertificates;
import de.marketmaker.iview.dmxml.WGZCertificateCell;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Assumption: always 3x3 matrix
 * @author umaurer
 */
public class WGZCertificateOverviewSnippet extends
        AbstractSnippet<WGZCertificateOverviewSnippet, SnippetTableView<WGZCertificateOverviewSnippet>> {
    private static final String[] ROW_HEADERS = new String[]{
            I18n.I.capitalGuaranteedProducts(), 
            I18n.I.partAssurance(), 
            I18n.I.participation() 
    };

    public static class Class extends SnippetClass {

        public Class() {
            super("wgz.CertificateOverview"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new WGZCertificateOverviewSnippet(context, config);
        }

    }

    private List<WGZCertificateCell> cells;

    private WGZCertificateListSnippet listSnippet;

    private String selectedListId = null;

    private DmxmlContext.Block<GISWGZCertificates> block;

    public WGZCertificateOverviewSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
        this.block = this.context.addBlock("GIS_WGZCertificates"); // $NON-NLS-0$

        final DefaultTableColumnModel tableColumnModel = new DefaultTableColumnModel(new TableColumn[]
                {
                        new TableColumn("", 0.2f, TableCellRenderers.LABEL), // $NON-NLS-0$
                        new TableColumn(I18n.I.shortTermWithPeriodAbbr(), 0.2f, TableCellRenderers.DEFAULT).withCellClass("mm-bottomSpace12"),  // $NON-NLS-0$
                        new TableColumn(I18n.I.middleTermWithPeriodAbbr(), 0.2f, TableCellRenderers.DEFAULT).withCellClass("mm-bottomSpace12"),  // $NON-NLS-0$
                        new TableColumn(I18n.I.longTermWithPeriodAbbr(), 0.2f, TableCellRenderers.DEFAULT).withCellClass("mm-bottomSpace12"),  // $NON-NLS-0$
                });
        this.setView(new SnippetTableView<>(this, tableColumnModel));
    }

    public void destroy() {
        this.context.removeBlock(this.block);
    }

    public void setSelectedListId(String selectedListId) {
        Firebug.log("WGZCertificateOverviewSnippet selectedListId: " + selectedListId);
        this.selectedListId = selectedListId;
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        setSelectedListId(event.getHistoryToken().get(1, null));
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            return;
        }
        if (this.cells == null)  {
            this.cells = this.block.getResult().getCell();
            this.block.setEnabled(false);
        }
        final List<RowData> rows = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            rows.add(new RowData(ROW_HEADERS[i],
                    createLinks(get(this.cells, i, 1)),
                    createLinks(get(this.cells, i, 2)),
                    createLinks(get(this.cells, i, 3))));
        }
        getView().update(DefaultTableDataModel.createWithRowData(rows));

        if (this.selectedListId == null) {
            final WGZCertificateCell cell = getFirstCell(this.cells);
            if (cell == null) {
                Firebug.log("no cell available in WGZCertificateOverviewSnippet"); // $NON-NLS-0$
            }
            else {
                show(cell);
            }
        }
        else {
            loadList();
        }
    }

    private void show(WGZCertificateCell cell) {
        PlaceUtil.goTo("WZ_C/" + cell.getListid()); // $NON-NLS-0$
    }

    private WGZCertificateCell getFirstCell(final List<WGZCertificateCell> cells) {
        for (int row = 0; row < 3; row++) {
            for (int col = 1; col <= 3; col++) {
                for (final WGZCertificateCell cell : cells) {
                    if (cell.getId().equals(Integer.toString(row * 3 + col))) {
                        return cell;
                    }
                }
            }
        }
        return null;
    }

    private WGZCertificateCell getCell(List<WGZCertificateCell> cells, String listid) {
        for (final WGZCertificateCell cell : cells) {
            if (listid.equals(cell.getListid())) {
                return cell;
            }
        }
        return null;
    }

    private Link[] createLinks(final List<WGZCertificateCell> cells) {
        if (cells.isEmpty()) {
            return null;
        }
        final Link[] links = new Link[cells.size()];
        for (int i = 0; i < links.length; i++) {
            final WGZCertificateCell cell = cells.get(i);
            links[i] = new Link(new LinkListener<Link>() {
                public void onClick(LinkContext<Link> linkContext, Element e) {
                    show(cell);
                }
            }, cell.getName(), null);
        }
        return links;
    }

    private List<WGZCertificateCell> get(List<WGZCertificateCell> cells, int row, int col) {
        final List<WGZCertificateCell> result = new ArrayList<>();
        for (final WGZCertificateCell cell : cells) {
            if (cell.getId().equals(Integer.toString(row * 3 + col))) {
                result.add(cell);
            }
        }
        return result;
    }

    private void loadList() {
        final WGZCertificateListSnippet listSnippet = getListSnippet();
        final WGZCertificateCell cell = getCell(this.cells, this.selectedListId);
        listSnippet.ackList(cell);
    }

    public WGZCertificateListSnippet getListSnippet() {
        if (this.listSnippet != null) {
            return this.listSnippet;
        }

        final String listId = getConfiguration().getString("listId", null); // $NON-NLS-0$
        if (listId == null) {
            throw new IllegalArgumentException("listId not specified"); // $NON-NLS-0$
        }
        this.listSnippet = (WGZCertificateListSnippet) this.contextController.getSnippet(listId);
        if (this.listSnippet == null) {
            throw new IllegalArgumentException("snippet not found: " + listId); // $NON-NLS-0$
        }
        return this.listSnippet;
    }
}
