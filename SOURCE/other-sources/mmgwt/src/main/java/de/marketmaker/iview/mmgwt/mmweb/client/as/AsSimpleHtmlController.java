/*
 * AsSimpleHtmlController.java
 *
 * Created on 08.09.2015 09:44:33
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.UserServiceAsync;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NeedsScrollLayout;
import de.marketmaker.iview.pmxml.GetEnvironmentResponse;

import java.util.Map;

/**
 * @author Ulrich Maurer
 * @author mdick
 */
public class AsSimpleHtmlController implements PageController {
    private final ContentContainer contentContainer;

    private HTML html;

    public AsSimpleHtmlController(ContentContainer contentContainer, String html) {
        this.contentContainer = contentContainer;
        this.html = new ScrolledHtml(html);
        this.html.setStyleName("mm-simpleHtmlView"); // $NON-NLS-0$
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        this.contentContainer.setContent(getHtml());
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

    static class ScrolledHtml extends HTML implements NeedsScrollLayout {
        ScrolledHtml(String html) {
            super(html);
        }

        @Override
        public void setHTML(String html) {
            super.setHTML(html);
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

    public static AsSimpleHtmlController createAdvisorySolutionWithPmLoginCustomerServiceInfo(ContentContainer cc) {
        final SessionData.VwdCustomerServiceContact c = SessionData.INSTANCE.getPmCustomerServiceContact();

        return new AsSimpleHtmlController(cc, "<div class=\"external-tool-header\">" // $NON-NLS-0$
                + I18n.I.customerService()
                + "</div>" // $NON-NLS-0$
                + "<div class=\"external-tool-text\">" // $NON-NLS-0$
                + I18n.I.customerServiceAdvisorySolutionWithPmLogin(c.getName(), c.getEmail(), c.getPhone(), c.getFax())
                + "</div>" // $NON-NLS-0$
        );
    }

    public static AsSimpleHtmlController createAdvisorySolutionVersionDetails(final ContentContainer cc) {
        final AsSimpleHtmlController simpleHtmlController = new AsSimpleHtmlController(cc, createAdvisorySolutionVersionDetailsHtml(null, null));

        UserServiceAsync.App.getInstance().getEnvInfo(new AsyncCallback<Map<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                logError(throwable);
            }

            @Override
            public void onSuccess(final Map<String, String> result) {
                PmEnvSupport.requestPmEnv(new AsyncCallback<GetEnvironmentResponse>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        logError(throwable);
                    }

                    @Override
                    public void onSuccess(GetEnvironmentResponse response) {
                        simpleHtmlController.setHtml(createAdvisorySolutionVersionDetailsHtml(result, response));
                    }
                });
            }
        });

        return simpleHtmlController;
    }

    private static void logError(Throwable throwable) {
        DebugUtil.logToServer("could not get env!", throwable);
    }

    private static String createAdvisorySolutionVersionDetailsHtml(Map<String, String> envMap, GetEnvironmentResponse env) {
        final User user = SessionData.INSTANCE.getUser();

        final String sharedEnvName = envMap != null ? envMap.get("sharedEnvName") : null;  // $NON-NLS$
        final boolean hasSharedEnvName = StringUtil.hasText(sharedEnvName);

        final StringBuilder html = new StringBuilder()
                .append("<div class=\"external-tool-header\">").append(I18n.I.userDetails()).append("</div>")  // $NON-NLS$
                .append("<table align=\"center\"><colgroup><col width=\"160\"><col width=\"200\"></colgroup>")  // $NON-NLS$
                .append("<tr><td>vwdId:</td><td>").append(StringUtil.sOrDash(user.getVwdId())).append("</td></tr>")  // $NON-NLS$
                .append("<tr><td>").append(I18n.I.username()).append(":</td><td>").append(StringUtil.sOrDash(user.getLogin())).append("</td></tr>")  // $NON-NLS$
                .append("<tr><td>").append(I18n.I.displayName()).append(":</td><td>").append(StringUtil.sOrDash(user.getFirstName(), user.getLastName())).append("</td></tr>")  // $NON-NLS$
                .append("<tr><td>").append(I18n.I.loggedinSinceLabel()).append(":</td><td>").append(SessionData.INSTANCE.getLoggedInSinceString()).append("</td></tr>")  // $NON-NLS$
                .append("</table>");  // $NON-NLS$

        if(env != null) {
            html.append("<div class=\"external-tool-header\">").append(I18n.I.versionDetails()).append("</div>")  // $NON-NLS$
                    .append("<table align=\"center\"><colgroup><col width=\"160\"><col width=\"200\"></colgroup>");  // $NON-NLS$
            for (PmEnvSupport.Entry entry : PmEnvSupport.getVersionDetails(env)) {
                html.append("<tr><td>").append(entry.getLabel()).append(":</td><td>").append(entry.getValue()).append("</td></tr>");  // $NON-NLS$
            }
            html.append("</table>");  // $NON-NLS$

            html.append("<div class=\"external-tool-header\">").append(I18n.I.modules()).append("</div>")  // $NON-NLS$
                    .append("<table align=\"center\"><colgroup><col width=\"160\"><col width=\"200\"></colgroup>");  // $NON-NLS$
            for (String module : PmEnvSupport.getModules(env)) {
                html.append("<tr><td colspan=\"2\">").append(module).append("</td></tr>");  // $NON-NLS$
            }
            html.append("</table>");  // $NON-NLS$
        }

        if(hasSharedEnvName) {
            html.append("<div class=\"external-tool-header\">").append(I18n.I.environment()).append("</div>")  // $NON-NLS$
                    .append("<table align=\"center\"><colgroup><col width=\"160\"><col width=\"200\"></colgroup>")  // $NON-NLS$
                    .append("<tr><td>").append(I18n.I.name()).append(":</td><td>").append(SafeHtmlUtils.htmlEscape(sharedEnvName)).append("</td></tr>") // $NON-NLS$
                    .append("</table>");  // $NON-NLS$
        }

        return html.toString();
    }
}
