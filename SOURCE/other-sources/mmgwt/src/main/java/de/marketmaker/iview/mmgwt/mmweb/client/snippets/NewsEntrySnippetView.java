/*
 * NewsEntrySnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.dmxml.NWSNews;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.NewsSendForm;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NewsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PhoneGapUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsEntrySnippetView extends SnippetView<NewsEntrySnippet> {
    private static final String NBSP = "&nbsp;"; // $NON-NLS$

    interface Style extends CssResource {
        String hidden();
    }

    interface ViewBinder extends UiBinder<Widget, NewsEntrySnippetView> {
    }

    private static ViewBinder uiBinder = GWT.create(ViewBinder.class);

    protected NWSNews news;

    @UiField
    Style style;

    @UiField
    DivElement headlineLabel;

    @UiField
    SpanElement dateLabel;

    @UiField
    SpanElement timeLabel;

    @UiField
    SpanElement pressAgencyLabel;

    @UiField
    HorizontalPanel buttonPanel;

    @UiField
    DivElement newsTextLabel;

    @UiField
    DivElement divDetails;

    @UiField
    DivElement divButtons;

    private final boolean withPdf;

    private Widget widget;

    public NewsEntrySnippetView(NewsEntrySnippet snippet) {
        super(snippet);
        final SnippetConfiguration config = snippet.getConfiguration();
        this.withPdf = config.getBoolean("withPdf", true) && !"true".equals(SessionData.INSTANCE.getGuiDefValue("disablePdf")); // $NON-NLS$
        this.widget = uiBinder.createAndBindUi(this);
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.widget);
        if (Selector.SEND_NEWS_MAIL.isAllowed() && !SessionData.INSTANCE.isAnonymous()) {
            this.buttonPanel.add(createEmailButton());
        }
        if (this.withPdf) {
            this.buttonPanel.add(createPdfButton());
        }
        this.container.setHeaderVisible(false);
    }

    void update(final NWSNews news) {
        this.news = news;
        if (news == null) {
            this.divDetails.addClassName(style.hidden());
            this.divButtons.addClassName(style.hidden());
            this.headlineLabel.setInnerText(I18n.I.noNewsSelected());
            this.dateLabel.setInnerHTML(NBSP);
            this.timeLabel.setInnerHTML(NBSP);
            this.pressAgencyLabel.setInnerHTML(NBSP);
            this.newsTextLabel.setInnerSafeHtml(SafeHtmlUtils.EMPTY_SAFE_HTML);
        }
        else {
            this.divDetails.removeClassName(style.hidden());
            this.divButtons.removeClassName(style.hidden());
            this.headlineLabel.setInnerSafeHtml(SafeHtmlUtils.fromTrustedString(getHeadline()));
            this.dateLabel.setInnerText(Formatter.LF.formatDate(news.getDate()));
            this.timeLabel.setInnerText(Formatter.formatTimeHhmm(news.getDate()));
            this.pressAgencyLabel.setInnerText(news.getSource());
            this.newsTextLabel.setInnerSafeHtml(SafeHtmlUtils.fromTrustedString(getNewsText()));
        }
    }

    private String getNewsText() {
        if (this.news.getText() == null) {
            return "";
        }
        final String textAsHTML = NewsUtil.getTextAsHTML(this.news.getText());
        if ("text/html".equals(this.news.getMimetype())) { // $NON-NLS$
            return textAsHTML.replaceAll("\n\n", "<br/><br/>"); // $NON-NLS$
        }
        return "<pre class=\"mm-newstext\">" + textAsHTML + "</pre>"; // $NON-NLS$
    }

    private String getHeadline() {
        final String raw = NewsUtil.headlineWithoutAgency(this.news);
        return NewsUtil.getTextAsHTML(raw);
    }

    private Button createEmailButton() {
        return Button.icon(SessionData.isAsDesign() ? "as-tool-send-email" : "mm-newsdetail-email") // $NON-NLS$
                .tooltip(I18n.I.sendNewsByEMail())
                .clickHandler(event -> NewsSendForm.INSTANCE.show(this.news.getNewsid()))
                .build();
    }

    private Button createPdfButton() {
        return Button.icon(SessionData.isAsDesign() ? "as-tool-export-pdf" : "mm-icon-pdf") // $NON-NLS$
                .tooltip(I18n.I.exportNewsToPdfFile())
                .clickHandler(event -> {
                    ActionPerformedEvent.fire("X_NWS_PDF"); // $NON-NLS$

                    String uri = getNewsUri(this.news.getNewsid());

                    if (PhoneGapUtil.isPhoneGap()) {
                        uri = UrlBuilder.ensureServerPrefix(uri, true);
                        PhoneGapUtil.log("NewsEntrySnippetView - PDF Link: " + uri); //$NON-NLS$
                        PhoneGapUtil.openPdfExternal(uri);
                    }
                    else {
                        Window.open(uri, "_blank", ""); // $NON-NLS$
                    }
                })
                .build();
    }

    public String getPrintHtml() {
        if (this.news == null) {
            return "<pre class=\"mm-newsheadline\">" + // $NON-NLS$
                    SafeHtmlUtils.htmlEscapeAllowEntities(I18n.I.noNewsSelected()) +
                    "</pre>"; // $NON-NLS$
        }
        final SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant("<div class=\"mm-newsheadline\">") // $NON-NLS$
                .append(SafeHtmlUtils.fromTrustedString(getHeadline()))
                .appendHtmlConstant("</div>") // $NON-NLS$
                .appendHtmlConstant("<p class=\"mm-newsdetail-date\">") // $NON-NLS$
                .appendEscaped(Formatter.LF.formatDate(this.news.getDate())).appendHtmlConstant(NBSP)
                .appendEscaped(Formatter.formatTimeHhmm(this.news.getDate())).appendHtmlConstant(NBSP)
                .appendEscaped(news.getSource()).appendHtmlConstant(NBSP)
                .appendHtmlConstant("</p><p>") // $NON-NLS$
                .append(SafeHtmlUtils.fromTrustedString(getNewsText()))
                .appendHtmlConstant("</p>"); // $NON-NLS$
        return builder.toSafeHtml().asString();
    }

    private String getNewsUri(String newsid) {
        final Map<String, String> map = new HashMap<>();
        map.put("newsid", newsid); // $NON-NLS$
        final PdfOptionSpec spec = new PdfOptionSpec("newsentry.pdf", map, null); // $NON-NLS$
        return PdfOptionHelper.getPdfUri(spec, null);
    }
}
