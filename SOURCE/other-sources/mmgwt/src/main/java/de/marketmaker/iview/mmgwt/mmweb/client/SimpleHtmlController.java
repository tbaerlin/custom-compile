/*
 * SimpleHtmlController.java
 *
 * Created on 26.05.2008 10:38:01
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.HTML;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecificIE7;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CustomerServiceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DOMUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NeedsScrollLayout;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Ulrich Maurer
 */
public class SimpleHtmlController implements PageController {
    private final ContentContainer contentContainer;

    private HTML html;

    public SimpleHtmlController(ContentContainer contentContainer, String html) {
        this.contentContainer = contentContainer;
        this.html = new ScrolledHtml(html);
        this.html.setStyleName("mm-simpleHtmlView"); // $NON-NLS-0$
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        display(this.contentContainer, getHtml());
    }

    protected HTML getHtml() {
        return this.html;
    }

    protected void setHtml(String html) {
        this.html.setHTML(html);
    }

    public void activate() {
    }

    public void deactivate() {
    }

    public static void display(ContentContainer cc, HTML html) {
        cc.setContent(html);
    }

    public static void display(ContentContainer cc, String html) {
        final HTML widget = new ScrolledHtml(html);
        widget.setStyleName("mm-simpleHtmlView"); // $NON-NLS-0$
        ie7LaunderLinks(widget);
        display(cc, widget);
    }

    static class ScrolledHtml extends HTML implements NeedsScrollLayout {
        ScrolledHtml(String html) {
            super(html);
            ie7LaunderLinks(this);
        }

        @Override
        public void setHTML(String html) {
            super.setHTML(html);
            ie7LaunderLinks(this);
        }
    }

    private static void ie7LaunderLinks(HTML html) {
        //This is necessary, because otherwise clicks internal history anchors
        //force a page reload in IE7
        if(BrowserSpecific.INSTANCE instanceof BrowserSpecificIE7) {
            Firebug.log("<SimpleHtmlController.display> launder links for IE7");
            DOMUtil.launderLinks(html.getElement());
        }
    }

    public void destroy() {
    }

    public void refresh() {
        // empty
    }


    public boolean supportsHistory() {
        return true;
    }

    protected ContentContainer getContentContainer() {
        return this.contentContainer;
    }

    public String getPrintHtml() {
        return getContentContainer().getContent().getElement().getInnerHTML();
    }

    @Override
    public boolean isPrintable() {
        return true;
    }

    public PdfOptionSpec getPdfOptionSpec() {
        return null;
    }

    public void addPdfPageParameters(Map<String, String> mapParameters) {
    }

    public String[] getAdditionalStyleSheetsForPrintHtml() {
        return null;
    }

    public static SimpleHtmlController createValordataBrowser(ContentContainer cc) {
        return new SimpleHtmlController(cc,
                I18n.I.valorDataBrowserHtml("https://www.tkfweb.com/finval?FD_AUTH=S++de865-" + SessionData.INSTANCE.getUser().getVdbLogin() + "+" + SessionData.INSTANCE.getUser().getVdbPassword()) // $NON-NLS$
        );
    }

    public static SimpleHtmlController createTicker(ContentContainer cc) {
        final String login = SessionData.INSTANCE.getUser().getLogin();
        return new SimpleHtmlController(cc,
                "<div class=\"external-tool-header\">Ticker</div>" + // $NON-NLS$
                        "  <div class=\"external-tool-text\">" + // $NON-NLS$
                        I18n.I.htmlOpenInWindow(3, I18n.I.tickerDescription()) +
                        "    <br/><br/><br/>" + // $NON-NLS$
                        "    <a onmousedown=\"return openToolWindow('/gis-ticker.html?html=true&symbol=106547.qid&authenticationType=vwddz-geno&authentication=" + login + "');\">DAX</a><br/>" + // $NON-NLS$
                        "    <a onmousedown=\"return openToolWindow('/gis-ticker.html?html=true&symbol=624999.qid&authenticationType=vwddz-geno&authentication=" + login + "');\">TecDAX</a><br/>" + // $NON-NLS$
                        "    <a onmousedown=\"return openToolWindow('/gis-ticker.html?html=true&symbol=167820.qid&authenticationType=vwddz-geno&authentication=" + login + "');\">MDAX</a><br/>" + // $NON-NLS$
                        "  </div>" // $NON-NLS$
        );
    }

    public static SimpleHtmlController createPlacard(ContentContainer cc, String linkedFilename) {
        final UrlBuilder urlBuilder = UrlBuilder.forPdf(linkedFilename);
        final String name = I18n.I.notice();  // $NON-NLS$
        return new SimpleHtmlController(cc,
                getDefaultHtml(name,
                        "", // $NON-NLS$
                        "mm-desktopIcon-pdf", // $NON-NLS$
                        urlBuilder.toURL(), "aushang", linkedFilename // $NON-NLS$
                )
        );
    }

    public static SimpleHtmlController createTechnicalAnalysis(ContentContainer cc) {
        return new SimpleHtmlController(cc,
                getDefaultHtml("Technische Analysen", // $NON-NLS$
                        "", // $NON-NLS$
                        "mm-desktopIcon-pdf", // $NON-NLS$
                        Settings.INSTANCE.technicalAnalysisDaily(), "tech-analysis", "Techn. Marktanalyse (tägl.)",  // $NON-NLS$
                        Settings.INSTANCE.technicalAnalysisWeekly(), "tech-analysis", "Techn. Marktanalyse (wöchentl.)"  // $NON-NLS$
                ));
    }

    public static SimpleHtmlController createHelp(ContentContainer cc) {
        return new SimpleHtmlController(cc,
                getDefaultHtml("Leitfaden der neuen GIS Webanwendungen", // $NON-NLS$
                        "mit dem jeweiligen Hilfekapitel",   // $NON-NLS$
                        null,
                        Settings.INSTANCE.helpUrl1(), "help", "Kapitel 1 - Erster Einstieg",  // $NON-NLS$
                        Settings.INSTANCE.helpUrl2(), "help", "Kapitel 2 - DZ BANK Inhalte", // $NON-NLS$
                        Settings.INSTANCE.helpUrl3(), "help", "Kapitel 3 - Mein GIS - Ganz individuell",  // $NON-NLS$
                        Settings.INSTANCE.helpUrl4(), "help", "Kapitel 4 - Märkte & Nachrichten", // $NON-NLS$
                        Settings.INSTANCE.helpUrl5(), "help", "Kapitel 5 - Volkswirtschaft",  // $NON-NLS$
                        Settings.INSTANCE.helpUrl6(), "help", "Kapitel 6 - Seiten",  // $NON-NLS$
                        Settings.INSTANCE.helpUrl7(), "help", "Kapitel 7 - Tools", // $NON-NLS$
                        Settings.INSTANCE.helpUrl8(), "help", "Kapitel 8 - Portal", // $NON-NLS$
                        Settings.INSTANCE.helpUrl9(), "help", "Kapitel 9 - Hilfe", // $NON-NLS$
                        Settings.INSTANCE.helpUrlTotal(), "help", "Gesamtdokument" // $NON-NLS$
                ));
    }

    public static SimpleHtmlController createHelpWgz(ContentContainer cc) {
        return new SimpleHtmlController(cc,
                getDefaultHtml("Leitfaden der neuen GIS Webanwendungen", // $NON-NLS$
                        "mit dem jeweiligen Hilfekapitel",  // $NON-NLS$
                        null,
                        Settings.INSTANCE.helpUrlWgz1(), "help", "Kapitel 1 - Erster Einstieg", // $NON-NLS$
                        Settings.INSTANCE.helpUrlWgz2(), "help", "Kapitel 2 - WGZ BANK Inhalte", // $NON-NLS$
                        Settings.INSTANCE.helpUrlWgz3(), "help", "Kapitel 3 - Mein GIS - Ganz individuell", // $NON-NLS$
                        Settings.INSTANCE.helpUrlWgz4(), "help", "Kapitel 4 - Märkte & Nachrichten",  // $NON-NLS$
                        Settings.INSTANCE.helpUrlWgz5(), "help", "Kapitel 5 - Volkswirtschaft",  // $NON-NLS$
                        Settings.INSTANCE.helpUrlWgz6(), "help", "Kapitel 6 - Seiten", // $NON-NLS$
                        Settings.INSTANCE.helpUrlWgz7(), "help", "Kapitel 7 - Tools",  // $NON-NLS$
                        Settings.INSTANCE.helpUrlWgz8(), "help", "Kapitel 8 - Hilfe", // $NON-NLS$
                        Settings.INSTANCE.helpUrlWgzTotal(), "help", "Gesamtdokument"  // $NON-NLS$
                ));
    }

    public static SimpleHtmlController createHelpApobank(ContentContainer cc) {
        return new SimpleHtmlController(cc,
                getDefaultHtml("Leitfaden der neuen Web.Finance APO-Anwendung", // $NON-NLS$
                        "mit dem jeweiligen Hilfekapitel",  // $NON-NLS$
                        null,
                        Settings.INSTANCE.helpApobankUrl1(), "help", "Kapitel 1 - Erster Einstieg", // $NON-NLS$
                        Settings.INSTANCE.helpApobankUrl2(), "help", "Kapitel 2 - Mein Web.Finance", // $NON-NLS$
                        Settings.INSTANCE.helpApobankUrl3(), "help", "Kapitel 3 - Märkte & Nachrichten", // $NON-NLS$
                        Settings.INSTANCE.helpApobankUrl4(), "help", "Kapitel 4 - Volkswirtschaft", // $NON-NLS$
                        Settings.INSTANCE.helpApobankUrl5(), "help", "Kapitel 5 - Tools",  // $NON-NLS$
                        Settings.INSTANCE.helpApobankUrl6(), "help", "Kapitel 6 - Hilfe",  // $NON-NLS$
                        Settings.INSTANCE.helpApobankUrlTotal(), "help", "Gesamtdokument" // $NON-NLS$
                ));
    }

    public static SimpleHtmlController createHelpKwt(ContentContainer cc) {
        return new SimpleHtmlController(cc,
                getDefaultHtml("Leitfaden der neuen Infothek", // $NON-NLS$
                        "mit dem jeweiligen Hilfekapitel", // $NON-NLS$
                        null,
                        Settings.INSTANCE.helpKwtUrl1(), "help", "Kapitel 1 - Erster Einstieg", // $NON-NLS$
                        Settings.INSTANCE.helpKwtUrl2(), "help", "Kapitel 2 - Meine Infothek", // $NON-NLS$
                        Settings.INSTANCE.helpKwtUrl3(), "help", "Kapitel 3 - Märkte & Nachrichten", // $NON-NLS$
                        Settings.INSTANCE.helpKwtUrl4(), "help", "Kapitel 4 - Volkswirtschaft", // $NON-NLS$
                        Settings.INSTANCE.helpKwtUrl5(), "help", "Kapitel 5 - Tools",  // $NON-NLS$
                        Settings.INSTANCE.helpKwtUrl6(), "help", "Kapitel 6 - Hilfe",  // $NON-NLS$
                        Settings.INSTANCE.helpKwtUrlTotal(), "help", "<br/><br/>Gesamtdokument" // $NON-NLS$
                ));
    }

    public static SimpleHtmlController createWebXl(ContentContainer cc) {
        return new SimpleHtmlController(cc, "<div class=\"external-tool-header\">web.XL (DDE)</div>" + // $NON-NLS-0$
                "    <div class=\"external-tool-text\">" + // $NON-NLS-0$
                "      web.XL (DDE) kann über folgenden Link heruntergeladen werden:<br/><br/>" + // $NON-NLS-0$
                "      <a href=\"download/setup.exe\" target=\"download\">setup.exe (ca. 2,4 Mb)</a>" + // $NON-NLS-0$
                "    </div>" + // $NON-NLS-0$
                "    <div class=\"external-tool-text\">" + // $NON-NLS-0$
                "      <a href=\"../misc/vwd_webxl_dokumentation.pdf\" target=\"download\">web.XL Kurzanleitung (ca. 280 KB)</a>" + // $NON-NLS-0$
                "    </div>"); // $NON-NLS-0$
    }

    public static SimpleHtmlController createOlbVideo(ContentContainer cc) {
        return new SimpleHtmlController(cc, "<div class=\"external-tool-header\">Schulungsvideos</div>" + // $NON-NLS-0$
                "    <div class=\"external-tool-text\">" + // $NON-NLS-0$
                "      Die Schulungsvideo können über die folgenden Links heruntergeladen werden:<br/><br/>" + // $NON-NLS-0$
                "    <div class=\"external-tool-text\">" + // $NON-NLS-0$
                "      <a href=\"../misc/navigation-und-favoriten.exe\" target=\"download\">Schulungsvideo Navigation & Favoriten (ca. 8 MB)</a>" + // $NON-NLS-0$
                "    </div>" + // $NON-NLS-0$
                "    <div class=\"external-tool-text\">" + // $NON-NLS-0$
                "      <a href=\"../misc/fonds.exe\" target=\"download\">Schulungsvideo Fonds (ca. 6 MB)</a>" + // $NON-NLS-0$
                "    </div>" + // $NON-NLS-0$
                "    <div class=\"external-tool-text\">" + // $NON-NLS-0$
                "      <a href=\"../misc/listen-olb.exe\" target=\"download\">Schulungsvideo Kursliste und Watchlist/Portfolio (ca. 8 MB)</a>" + // $NON-NLS-0$
                "    </div>" // $NON-NLS-0$
        );
    }

    @NonNLS
    public static SimpleHtmlController createVwdCustomerServiceInfo(ContentContainer cc) {
        final JsArray<SessionData.VwdCustomerServiceContact> contacts = SessionData.INSTANCE.getVwdCustomerServiceContacts();
        final StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"external-tool-header\">")
                .append(I18n.I.customerService())
                .append("</div>")
                .append("<div class=\"mm-center\"><img src=\"images/infront.png\"/></div>")
                .append("<div class=\"external-tool-text\">")
                .append("<table align=\"center\">");
        for (int i = 0; i < contacts.length(); i++) {
            final SessionData.VwdCustomerServiceContact c = contacts.get(i);
            sb.append("<tr><td colspan=\"2\">").append(c.getName()).append("</td></tr>");
            CustomerServiceUtil.customerServiceAddressRowsBuilder(sb, c);
        }
        sb.append("</table>").append("</div>");
        return new SimpleHtmlController(cc, sb.toString());
    }

    public static SimpleHtmlController createZoneCustomerServiceInfo(ContentContainer cc) {
        final String customerServiceInfo = SessionData.INSTANCE.getGuiDefValue("customerServiceInfo"); // $NON-NLS$
        return new SimpleHtmlController(cc, "<div class=\"external-tool-header\">" // $NON-NLS-0$
                + I18n.I.customerService()
                + "</div>" // $NON-NLS-0$
                + "<div class=\"external-tool-text\">" // $NON-NLS-0$
                + (customerServiceInfo == null ? "" : customerServiceInfo)
                + "</div>" // $NON-NLS-0$
        );
    }

    public static void displayAttraxFondsadvisor(ContentContainer cc, String url) {
        final String name = "ATTRAX FONDSADVISOR"; // $NON-NLS$
        display(cc, "<div class=\"external-tool-header\">" + name + "</div>" + // $NON-NLS-0$ $NON-NLS-1$
                "    <div class=\"external-tool-text\">" + // $NON-NLS-0$
                I18n.I.messageExternalToolInNewWindow(name, url)
                + "    </div>"); // $NON-NLS-0$
    }

    public static void displayPlatow(ContentContainer cc, String platowUrl) {
        display(cc, "<div class=\"external-tool-header\">PLATOW SP&euro;ZIAL</div>" + // $NON-NLS-0$
                "    <div class=\"external-tool-text\">" + // $NON-NLS-0$
                "      Ein Klick auf den folgenden Link &ouml;ffnet PLATOW SP&euro;ZIAL in einem separaten Fenster:<br/><br/>" + // $NON-NLS-0$
                "      <a onmousedown=\"return openPrintableWindow('" + platowUrl + "');\">PLATOW SP&euro;ZIAL</a><br/>" + // $NON-NLS-0$ $NON-NLS-1$
                "    </div>"); // $NON-NLS-0$
    }

    public static SimpleHtmlController createReleaseInformationGisPortal(ContentContainer cc) {
        final StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"external-tool-header\">"); // $NON-NLS$
        sb.append("Release-Information"); // $NON-NLS$
        sb.append("</div>"); // $NON-NLS$
        sb.append("<div class=\"external-tool-text\">"); // $NON-NLS$
        sb.append("<br/><br/>");  // $NON-NLS$
        if(SessionData.isAsDesign()) {
            sb.append("Alle Release-Informationen finden Sie im Media-Archiv im GIS Portal!"); //$NON-NLS$
        }
        else {
            sb.append("NEU: Alle Release-Informationen finden Sie im Media-Archiv im GIS Portal!"); //$NON-NLS$
            sb.append("<br/><br/>");  // $NON-NLS$
            sb.append("<a href=\"#G_P/H\">GIS Portal</a>");  // $NON-NLS$
        }
        sb.append("</div>");  // $NON-NLS$

        return new SimpleHtmlController(cc, sb.toString());
    }

    public static SimpleHtmlController createReleaseInformation(ContentContainer cc) {
        List<String> params = new ArrayList<>(6);

        if (SessionData.isAsDesign() && SessionData.INSTANCE.hasGuiDef("release2016InternalUriKey")) {  // $NON-NLS$
            // note: simple guidefs of feature flag check is not possible here, because we need
            // to show the new release depending on the different staged release dates for the
            // different zones. Hence, we are using the check for as-design and show the 2014 release
            // notes if it is not as-design.
            params.add(I18n.I.release2016InternalIUri());
            params.add("release-info"); // $NON-NLS-0$
            params.add(I18n.I.release() + " " + DateRenderer.date("").render("2016-06-29"));  // $NON-NLS-0$
        }
        else if (SessionData.INSTANCE.hasGuiDef("release2014InternalUriKey")) { // $NON-NLS-0$
            params.add(I18n.I.release2014InternalIUri());
            params.add("release-info"); // $NON-NLS-0$
            params.add(I18n.I.release() + " " + DateRenderer.date("").render("2014-12-15"));  // $NON-NLS-0$
        }

        if (SessionData.INSTANCE.hasGuiDef("release4InformationUri")) { // $NON-NLS-0$
            params.add(SessionData.INSTANCE.getGuiDefValue("release4InformationUri")); // $NON-NLS-0$
            params.add("release-info"); // $NON-NLS-0$
            params.add("neues Release 13.12.2010");  // $NON-NLS-0$
        }

        if (SessionData.INSTANCE.hasGuiDef("release2InformationUri")) { // $NON-NLS-0$
            params.add(SessionData.INSTANCE.getGuiDefValue("release2InformationUri")); // $NON-NLS-0$
            params.add("release-info"); // $NON-NLS-0$
            params.add("neues Release 25.01.2010");  // $NON-NLS-0$
        }

        if (SessionData.INSTANCE.hasGuiDef("releaseInformationUri")) { // $NON-NLS-0$
            params.add(SessionData.INSTANCE.getGuiDefValue("releaseInformationUri")); // $NON-NLS-0$
            params.add("release-info"); // $NON-NLS-0$
            params.add("Zwischenrelease August 2009");  // $NON-NLS-0$
        }

        return new SimpleHtmlController(cc,
                getDefaultHtml(I18n.I.releaseInformation(),
                        I18n.I.withTheReleaseInfo(),
                        "mm-desktopIcon-pdf",   // $NON-NLS$
                        params.toArray(new String[params.size()])
                ));
    }

    public static SimpleHtmlController createCampaign(ContentContainer cc) {
        final List<String> params = new ArrayList<>(3);
        params.add("Information_Newex_April_2011.pdf"); // $NON-NLS$
        params.add("pdf-campaign"); // $NON-NLS$
        params.add("Information Newex April 2011"); // $NON-NLS$
        return new SimpleHtmlController(cc,
                getDefaultHtml("Aktuelle Information", // $NON-NLS$
                        "<br><br><br><br>Gebührenänderung der Deutschen Börse AG ab dem 23. Mai 2011.<br><br><br>",  // $NON-NLS$
                        "mm-desktopIcon-pdf",   // $NON-NLS$
                        params.toArray(new String[params.size()])
                ));
    }

    public static String createPublicationPage(String query) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"external-tool-header\">"); // $NON-NLS$
        sb.append("DZ BANK Publikationen - ").append(KapitalmarktFavoritenWatchdog.getDate()); // $NON-NLS$
        sb.append("</div><div class=\"external-tool-text\">"); // $NON-NLS$
        sb.append(I18n.I.htmlOpenInWindow(2, I18n.I.htmlDefaultDescription(2, "Publikationen"))); // $NON-NLS$
        sb.append("</div>");  // $NON-NLS$
        final SimpleTable simpleTable = new SimpleTable();
        if (query != null) {
            simpleTable.addColumn(
                    new SimpleColumn("KapitalmarktFavoriten") // $NON-NLS$
                            .addCellLink("KapitalmarktFavoriten", Settings.INSTANCE.kapitalmarktFavDefaultCustomersUrlCms() + query) // $NON-NLS$
            );
        }
        simpleTable.addColumn(new SimpleColumn("Weitere Publikationen")  // $NON-NLS$
                .addCellLink("Technische Analyse - Daily", Settings.INSTANCE.technicalAnalysisDaily())  // $NON-NLS$
                .addCellLink("Technische Analyse - Weekly", Settings.INSTANCE.technicalAnalysisWeekly())  // $NON-NLS$
        );
        sb.append(simpleTable.render());
        return sb.toString();
    }

    public static String getDefaultHtml(String header, String description, String iconType,
                                        String... link) {
        assert (link.length % 3 == 0);
        final StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"external-tool-header\">"); // $NON-NLS$
        sb.append(header);
        sb.append("</div><div class=\"external-tool-text\">"); // $NON-NLS$
        sb.append(I18n.I.htmlOpenInWindow(link.length / 3, description));
        for (int i = 0, linkLength = link.length; i < linkLength; i += 3) {
            sb.append("<br/><br/>");  // $NON-NLS$
            sb.append("<a href=\"");  // $NON-NLS$
            sb.append(link[i]);
            sb.append("\" target=\"");  // $NON-NLS$
            sb.append(link[i + 1]);
            sb.append("\" class=\"mm-simpleLink\">");   // $NON-NLS$
            if (iconType != null) {
                sb.append("<div class=\"").append(iconType).append("\"></div>"); // $NON-NLS$
            }
            sb.append(link[i + 2]);
            sb.append("</a>"); // $NON-NLS$
        }
        sb.append("</div>");  // $NON-NLS$
        return sb.toString();
    }


    private static class SimpleTable extends ArrayList<SimpleColumn> {

        SimpleTable addColumn(SimpleColumn column) {
            add(column);
            return this;
        }

        String render() {
            int height = 0;
            StringBuilder sb = new StringBuilder();
            sb.append("<table style=\"width:100%;\"><tr>");   // $NON-NLS$
            for (SimpleColumn column : this) {
                height = Math.max(height, column.size());
                sb.append("<td><b>");  // $NON-NLS$
                sb.append(column.header);
                sb.append("</b></td>");  // $NON-NLS$
            }
            sb.append("</tr>");  // $NON-NLS$
            for (int i = 0; i < height; i++) {
                sb.append("<tr>");  // $NON-NLS$
                for (SimpleColumn column : this) {
                    sb.append("<td>&nbsp;");  // $NON-NLS$
                    if (column.size() > i) {
                      sb.append(column.get(i));
                    }
                    sb.append("&nbsp;</td>");  // $NON-NLS$
                }
                sb.append("</tr>"); // $NON-NLS$
            }
            sb.append("</table>"); // $NON-NLS$
            return sb.toString();
        }
    }

    private static class SimpleColumn extends ArrayList<String> {
        final String header;

        SimpleColumn(String header) {
            this.header = header;
        }

        SimpleColumn addCellContent(String content) {
            add(content);
            return this;
        }

        SimpleColumn addCellLink(String text, String href) {
            return addCellContent(""
               + "<a href=\"" + href + "\" target=\"_blank\" class=\"mm-simpleLink\">"  // $NON-NLS$
               +  text
               + "</a>");  // $NON-NLS$
        }
    }
}
