package de.marketmaker.iview.mmgwt.mmweb.client.customer.wgz;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import de.marketmaker.iview.dmxml.CERFinder;
import de.marketmaker.iview.dmxml.CERFinderElement;
import de.marketmaker.iview.dmxml.WGZCertificateCell;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PageLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;

import java.util.ArrayList;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.DEFAULT;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PERCENT;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PRICE;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.QUOTELINK_32;

/**
 * @author umaurer
 */
public class WGZCertificateListSnippet
        extends
        AbstractSnippet<WGZCertificateListSnippet, SnippetTableView<WGZCertificateListSnippet>>
        implements PageLoader {

    public static class Class extends SnippetClass {
        public Class() {
            super("wgz.CertificateList", I18n.I.noCertificateListSelected()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new WGZCertificateListSnippet(context, config);
        }
    }

    public static final int DEFAULT_COUNT = 30;

    private DmxmlContext.Block<CERFinder> block;

    private PagingFeature pagingFeature;

    private List<String> columns;

    protected WGZCertificateListSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.block = this.context.addBlock("CER_Finder"); // $NON-NLS-0$
        this.block.setParameter("sortBy", "name"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("ascending", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("additionalType", "BND"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("providerPreference", SessionData.INSTANCE.getGuiDefValue("providerPreference")); // $NON-NLS$
        this.block.setEnabled(false);

        final DefaultTableColumnModel columnModel = new DefaultTableColumnModel(new TableColumn[0]);
        final SnippetTableView<WGZCertificateListSnippet> view = new SnippetTableView<>(this, columnModel);
        setView(view);

        final PagingWidgets pagingWidgets = new PagingWidgets(new PagingWidgets.Config());
        this.pagingFeature = new PagingFeature(this, this.block, DEFAULT_COUNT);
        this.pagingFeature.setPagingWidgets(pagingWidgets);
        view.setTopComponent(pagingWidgets.getToolbar());
        view.setSortLinkListener(new SortLinkSupport(this.block, new Command() {
            public void execute() {
                reload();
            }
        }, true));
    }
                                                                  
    public void destroy() {
        this.context.removeBlock(this.block);
    }

    public void ackList(WGZCertificateCell cell) {
        if (cell == null) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        getConfiguration().put("title", cell.getName()); // $NON-NLS-0$
        this.block.setParameter("query", "wgzListid=='" + cell.getListid() + "'"); // $NON-NLS$
        if (!this.block.isEnabled()) {
            this.block.setEnabled(true);
        }
        this.columns = cell.getColumn();
        this.contextController.reload();
    }

    public void reload() {
        this.contextController.reload();
    }

    public void updateView() {
        this.pagingFeature.onResult();
        if (!this.block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final DefaultTableColumnModel tcm = getColumnModel();

        final CERFinder finder = this.block.getResult();
        final int count = finder.getElement().size();
        if (count == 0) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        final DefaultTableDataModel tdm = new DefaultTableDataModel(count, tcm.getColumnCount()).withSort(finder.getSort());
        for (int i = 0; i < count; i++) {
            final CERFinderElement element = finder.getElement().get(i);
            tdm.setValuesAt(i, getRow(element));
        }
        getView().update(tcm, tdm);
    }

    private Object[] getRow(CERFinderElement e) {
        final List<Object> columns = new ArrayList<>();

        final QuoteWithInstrument qwi = new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
        addElement("1", columns, qwi); // $NON-NLS-0$
        columns.add(e.getInstrumentdata().getWkn());
        addElement("12", columns, e.getUnderlyingName()); // $NON-NLS-0$
        addElement("15", columns, e.getExpirationDate()); // $NON-NLS-0$
        addElement("11", columns, e.getParticipationLevel()); // $NON-NLS-0$
        addElement("14", columns, e.getCoupon()); // $NON-NLS-0$
        addElement("16", columns, e.isIsknockout() ? I18n.I.no() : I18n.I.yes());  // $NON-NLS-0$
        addElement("17", columns, null);//nominal // $NON-NLS-0$
        addElement("18", columns, null); //bonus // $NON-NLS-0$
        addElement("8", columns, e.getCap()); // $NON-NLS-0$
        addElement("13", columns, e.getStrike());//strike // $NON-NLS-0$
        addElement("5", columns, e.getQuotedata().getMarketName()); // $NON-NLS-0$
        addElement("4", columns, e.getDate()); // $NON-NLS-0$
        addElement("6", columns, e.getChangeNet()); // $NON-NLS-0$
        addElement("7", columns, e.getChangePercent()); // $NON-NLS-0$
        addElement("10", columns, e.getBid()); // $NON-NLS-0$
        addElement("9", columns, e.getAsk()); // $NON-NLS-0$
        addElement("3", columns, e.getPrice()); // $NON-NLS-0$
        addElement("19", columns, new Link(new LinkListener<Link>() { // $NON-NLS-0$
            public void onClick(LinkContext<Link> linkLinkContext, Element e) {
                qwi.goToReports();
            }
        }, "PDF", null).withStyle("mime-application-pdf")); // $NON-NLS-0$ $NON-NLS-1$

        return columns.toArray();
    }

    private void addElement(String column, List<Object> columns, Object o) {
        if (this.columns.contains(column)) {
            columns.add(o);
        }
    }

    private DefaultTableColumnModel getColumnModel() {
        final List<TableColumn> columns = new ArrayList<>();

        add("1", columns, new TableColumn(I18n.I.name(), -1f, QUOTELINK_32, "name"));  // $NON-NLS-0$ $NON-NLS-1$
        columns.add(new TableColumn("WKN", -1f, "wkn").alignCenter()); // $NON-NLS-0$ $NON-NLS-1$
        add("12", columns, new TableColumn(I18n.I.underlyingInstrument(), -1f, DEFAULT, "underlyingName"));  // $NON-NLS-0$ $NON-NLS-1$
        add("15", columns, new TableColumn(I18n.I.maturity2(), -1f, DEFAULT, "expirationDate"));  // $NON-NLS-0$ $NON-NLS-1$
        add("11", columns, new TableColumn(I18n.I.participationLevelAbbr(), -1f, PERCENT, "participationLevel"));  // $NON-NLS-0$ $NON-NLS-1$
        add("14", columns, new TableColumn(I18n.I.coupon(), -1f, PERCENT, "coupon"));  // $NON-NLS-0$ $NON-NLS-1$
        add("16", columns, new TableColumn(I18n.I.barrierIntact(), -1f, DEFAULT));  // $NON-NLS-0$
        add("17", columns, new TableColumn(I18n.I.nominal(), -1f, PRICE, "nominal"));  // $NON-NLS-0$ $NON-NLS-1$
        add("18", columns, new TableColumn(I18n.I.bonus(), -1f, DEFAULT, "certBonus"));  // $NON-NLS-0$ $NON-NLS-1$
        add("8", columns, new TableColumn(I18n.I.cap(), -1f, DEFAULT, "cap"));  // $NON-NLS-0$ $NON-NLS-1$
        add("13", columns, new TableColumn(I18n.I.strike(), -1f, DEFAULT, "strike"));  // $NON-NLS-0$ $NON-NLS-1$
        add("5", columns, new TableColumn(I18n.I.exchange(), -1f, DEFAULT));  // $NON-NLS-0$
        add("4", columns, new TableColumn(I18n.I.timeDate(), -1f, DEFAULT));  // $NON-NLS-0$
        add("6", columns, new TableColumn(I18n.I.changeNetAbbr(), -1f, DEFAULT, "changeNet"));  // $NON-NLS-0$ $NON-NLS-1$
        add("7", columns, new TableColumn(I18n.I.changePercentAbbr(), -1f, DEFAULT, "changePercent"));  // $NON-NLS-0$ $NON-NLS-1$
        add("10", columns, new TableColumn(I18n.I.bid(), -1f, DEFAULT, "bid"));  // $NON-NLS-0$ $NON-NLS-1$
        add("9", columns, new TableColumn(I18n.I.ask(), -1f, DEFAULT, "ask"));  // $NON-NLS-0$ $NON-NLS-1$
        add("3", columns, new TableColumn(I18n.I.price(), -1f, DEFAULT, "priceValue"));  // $NON-NLS-0$ $NON-NLS-1$
        add("19", columns, new TableColumn(I18n.I.download(), -1f, DEFAULT));  // $NON-NLS-0$

        return new DefaultTableColumnModel(columns.toArray(new TableColumn[columns.size()]));
    }

    private void add(String column, List<TableColumn> columns, TableColumn tableColumn) {
        if (this.columns.contains(column)) {
            columns.add(tableColumn);
        }
    }
}