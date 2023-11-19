/*
 * GisIpoInstrumentsController.java
 *
 * Created on 24.10.2008 14:33:37
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.iview.dmxml.GISIpoInstruments;
import de.marketmaker.iview.dmxml.IpoInstrument;
import de.marketmaker.iview.dmxml.ReportType;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRendererAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.DzPibMarginDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class GisIpoInstrumentsController extends AbstractPageController {
    private static final int COLUMNCOUNT = 8;

    private final DmxmlContext.Block<GISIpoInstruments> block;
    private GisIpoInstrumentsView view;
    private final String type;

    public GisIpoInstrumentsController(ContentContainer contentContainer, String type) {
        super(contentContainer);

        this.type = type;
        this.block = this.context.addBlock("GIS_IpoInstruments"); // $NON-NLS$
        this.block.setParameter("type", type); // $NON-NLS$
        this.block.setParameter("disablePaging", "true"); // $NON-NLS$
        this.block.setParameter("sortBy", "subscriptionStart"); // $NON-NLS$
        this.block.setParameter("ascending", "true"); // $NON-NLS$
        this.view = new GisIpoInstrumentsView(this);
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        refresh();
    }

    LinkListener<String> getSortLinkListener() {
        return new SortLinkSupport(this.block, this::refresh);
    }

    protected void onResult() {
        if (!this.block.isResponseOk()) {
            this.getContentContainer().setContent(new HTML(I18n.I.error()));
            return;
        }

        final List<IpoInstrument> instruments = this.block.getResult().getElement();

        if (wgzDev()) {
            final TableDataModel dtmCurrent = createDtm(currentOnly(instruments));
            final TableDataModel dtmFixed = createDtm(fixedOnly(instruments));
            this.view.show(dtmCurrent, dtmFixed);
        }
        else {
            final TableDataModel dtm = createDtm(instruments);
            this.view.show(dtm);
        }
    }

    private List<IpoInstrument> currentOnly(List<IpoInstrument> instruments) {
        final ArrayList<IpoInstrument> result = new ArrayList<>();
        final Date now = new Date();
        for (IpoInstrument instrument : instruments) {
            if (StringUtil.hasText(instrument.getSubscriptionEnd())) {
                final Date subscriptionEnd = getSubscriptionEnd(instrument);
                if (subscriptionEnd.after(now)) {
                    result.add(instrument);
                }
            }
        }
        return result;
    }

    private List<IpoInstrument> fixedOnly(List<IpoInstrument> instruments) {
        final ArrayList<IpoInstrument> result = new ArrayList<>();
        final Date now = new Date();
        for (IpoInstrument instrument : instruments) {
            if (StringUtil.hasText(instrument.getSubscriptionEnd())) {
                final Date subscriptionEnd = getSubscriptionEnd(instrument);
                if (subscriptionEnd.before(now)) {
                    result.add(instrument);
                }
            }
        }
        return result;
    }

    private Date getSubscriptionEnd(IpoInstrument instrument) {
        return DateTimeFormat.getFormat("dd.MM.yyyy").parse(instrument.getSubscriptionEnd()); // $NON-NLS$
    }

    private TableDataModel createDtm(List<IpoInstrument> instruments) {
        return DefaultTableDataModel.create(instruments, new AbstractRowMapper<IpoInstrument>() {
            public Object[] mapRow(IpoInstrument instrument) {
                final List<ReportType> listReports = instrument.getReport();
                final Link[] links = new Link[listReports.size()];

                for (int i = 0; i < links.length; i++) {
                    final ReportType report = listReports.get(i);
                    // TODO: we should check the type here not the title
                    if ("Produktinfo".equals(report.getTitle())) {   // $NON-NLS$
                        links[i] = new Link(DzPibMarginDialog.DYN_PIB_BASE_URL + report.getUrl(),
                                "mm-report", I18n.I.openReportInNewWindow(), report.getTitle()).withStyle("mime-application-pdf");  // $NON-NLS$
                    }
                    else {
                        links[i] = new Link(JsUtil.escapeUrl(report.getUrl()),
                                "mm-report", I18n.I.openReportInNewWindow(), report.getTitle()).withStyle("mime-application-pdf");  // $NON-NLS$
                    }
                }

                final String pibReportUrl = getPibReportUrl(instrument);

                return new Object[]{
                        pibReportUrl,
                        instrument.getWkn(),
                        instrument.getName(),
                        instrument.getSubscriptionStart(),
                        instrument.getSubscriptionEnd(),
                        instrument.getValutaDate(),
                        instrument.getExpirationDate(),
                        links
                };
            }
        }).withSort(this.block.getResult().getSort());
    }

    private String getPibReportUrl(IpoInstrument instrument) {
        if (Boolean.TRUE.equals(instrument.isHasDzPib())) {
            final ReportType reportType = instrument.getPibReport();
            if (reportType != null) {
                return reportType.getUrl();
            }
        }
        return null;
    }

    class ReportRenderer extends TableCellRendererAdapter {

        public void render(Object data, StringBuffer sb, Context context) {
            final Link[] links = (Link[]) data;
            String divider = "";
            for (Link link : links) {
                sb.append(divider);
                sb.append("<a href=\"").append(link.getHref()).append("\""); // $NON-NLS$
                if (link.getTarget() != null) {
                    sb.append(" target=\"").append(link.getTarget()).append("\""); // $NON-NLS$
                }
                if (link.getTooltip() != null) {
                    sb.append(" qtip=\"").append(link.getTooltip()).append("\""); // $NON-NLS$
                }
                if (link.getStyle() != null) {
                    sb.append(" class=\"").append(link.getStyle()).append("\""); // $NON-NLS$
                }
                sb.append(">").append(link.getText()).append("</a>"); // $NON-NLS$
                divider = "<br/>"; // $NON-NLS$
            }
        }
    }

    TableColumnModel createTableColumnModel() {
        final TableColumn[] columns = new TableColumn[COLUMNCOUNT];
        final VisibilityCheck dzCheck = tc -> Selector.DZ_BANK_USER.isAllowed() && Selector.PRODUCT_WITH_PIB.isAllowed();
        int col = -1;
        columns[++col] = new TableColumn(I18n.I.info(), 0.02f, TableCellRenderers.DZPIB_DOWNLOAD_ICON_LINK_URL).withVisibilityCheck(dzCheck);
        columns[++col] = new TableColumn("WKN", 0.05f, TableCellRenderers.STRING, "wkn").alignCenter(); // $NON-NLS$
        columns[++col] = new TableColumn(I18n.I.name(), 0.2f, TableCellRenderers.STRING, "name");  // $NON-NLS$
        columns[++col] = new TableColumn(I18n.I.subscriptionStart(), 0.1f, TableCellRenderers.DATE, "subscriptionStart").withCellClass("mm-center");  // $NON-NLS$
        columns[++col] = new TableColumn(I18n.I.subscriptionEnd(), 0.1f, TableCellRenderers.DATE, "subscriptionEnd").withCellClass("mm-center");  // $NON-NLS$

        final VisibilityCheck dzTypeVisibilityCheck = SimpleVisibilityCheck.valueOf("dz".equals(this.type)); // $NON-NLS$
        columns[++col] = new TableColumn(I18n.I.valutaDate(), 0.1f, TableCellRenderers.DATE, "valutaDate").withCellClass("mm-center").withVisibilityCheck(dzTypeVisibilityCheck);  // $NON-NLS$
        columns[++col] = new TableColumn(I18n.I.expirationDate(), 0.1f, TableCellRenderers.DATE, "expirationDate").withCellClass("mm-center").withVisibilityCheck(dzTypeVisibilityCheck);  // $NON-NLS$
        columns[++col] = new TableColumn(I18n.I.reports(), 0.2f, new ReportRenderer());

        return new DefaultTableColumnModel(columns);
    }

    @Override
    public void activate() {
        if (wgzDev() && AbstractMainController.INSTANCE.getView() instanceof LegacyMainView) {
            ((LegacyMainView) AbstractMainController.INSTANCE.getView()).setContentHeaderVisible(false);
        }
    }

    @Override
    public void deactivate() {
        if (wgzDev() && AbstractMainController.INSTANCE.getView() instanceof LegacyMainView) {
            ((LegacyMainView) MainController.INSTANCE.getView()).setContentHeaderVisible(true);
        }
    }

    public boolean wgzDev() {
        return Selector.WGZ_BANK_USER.isAllowed() && "wgz".equals(this.type); // $NON-NLS$
    }

}