/*
 * AnalyserSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.Settings;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Ulrich Maurer
 */
public class AnalyserSnippet extends
        AbstractSnippet<AnalyserSnippet, SnippetTextView<AnalyserSnippet>> implements
        SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("Analyser"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new AnalyserSnippet(context, config);
        }
    }

    public static final String TYPE_FLEX_CHART = "flexChart"; // $NON-NLS-0$

    public static final String TYPE_INVESTMENT_CALCULATOR = "investmentCalculator"; // $NON-NLS-0$

    private final String type;

    private final int height;

    private final DmxmlContext.Block<MSCPriceData> block;

    private String iframeName = null;

    AnalyserSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.setView(new SnippetTextView<>(this));

        this.type = config.getString("type", null); // $NON-NLS-0$
        this.height = config.getInt("height", 520); // $NON-NLS-0$
        if (config.getBoolean("withSymbol", true)) { // $NON-NLS-0$
            this.block = context.addBlock("MSC_PriceData"); // $NON-NLS-0$
        }
        else {
            this.block = null;
        }
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        if (this.block == null) {
            return;
        }
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void setHtml(String html) {
        getView().setHtml(html);
    }

    public void destroy() {
        setHtml("&nbsp;"); // $NON-NLS-0$
    }

    public void updateView() {
        if (this.block != null && !this.block.isResponseOk()) {
            setHtml("&nbsp;"); // $NON-NLS-0$
            return;
        }

        final String analyserUrl;
        final MSCPriceData data = this.block == null ? null : this.block.getResult();
        if (TYPE_FLEX_CHART.equals(this.type)) {
            final String baseUrl = Settings.INSTANCE.analyserUrlFlexChart();
            analyserUrl = baseUrl
                    + (baseUrl.indexOf("?") > 0 ? "&" : "?") // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
                    + getFlexChartParameter(data);
            Firebug.debug("flexChart-URL: " + analyserUrl);

            getView().setStyleName("mm-simpleHtmlView"); // $NON-NLS-0$
            setHtml("<div class=\"external-tool-header\">flexChartAnalyser</div>" + // $NON-NLS-0$
                    "    <div class=\"external-tool-text\">" + // $NON-NLS-0$
                    I18n.I.messageFlexChartAnalyserInNewWindow() +
                    "    </div>"); // $NON-NLS-0$

            Window.open(analyserUrl, "flexChart", ""); // $NON-NLS-0$ $NON-NLS-1$

            return;
        }
        else if (TYPE_INVESTMENT_CALCULATOR.equals(this.type)) {
            if (Permutation.GIS.isActive()) {
                analyserUrl = Settings.INSTANCE.analyserUrlInvestmentCalculatorGis();
            }
            else {
                if("it".equals(I18n.I.locale())) { // $NON-NLS-0$
                    analyserUrl = Settings.INSTANCE.analyserUrlInvestmentCalculatorIt();
                }
                else if("en".equals(I18n.I.locale())) { // $NON-NLS-0$
                    analyserUrl = Settings.INSTANCE.analyserUrlInvestmentCalculatorEn();
                }
                else if("nl".equals(I18n.I.locale())) { // $NON-NLS-0$
                    analyserUrl = Settings.INSTANCE.analyserUrlInvestmentCalculatorNl();
                }
                else if("fr".equals(I18n.I.locale())) { // $NON-NLS-0$
                    analyserUrl = Settings.INSTANCE.analyserUrlInvestmentCalculatorFr();
                }
                else {
                    analyserUrl = Settings.INSTANCE.analyserUrlInvestmentCalculator();
                }
            }
            this.iframeName = getIframeName(analyserUrl, "gevasysInvestmentCalculator");  // $NON-NLS$
        }
        else if ("BND".equals(this.type)) { // $NON-NLS-0$
            analyserUrl = getAnalyserUrl(Permutation.GIS.isActive()
                    ? Settings.INSTANCE.analyserUrlBndGis() : Settings.INSTANCE.analyserUrlBnd(), data);
            this.iframeName = getIframeName(analyserUrl, "gevasysAnalyserBND");  // $NON-NLS$
        }
        else if ("WNT".equals(this.type)) { // $NON-NLS-0$
            analyserUrl = getAnalyserUrl(Permutation.GIS.isActive()
                    ? Settings.INSTANCE.analyserUrlWntGis() : Settings.INSTANCE.analyserUrlWnt(), data);
            this.iframeName = getIframeName(analyserUrl, "gevasysAnalyserWNT");  // $NON-NLS$
        }
        else {
            setHtml(I18n.I.analyserToolsOnlyForBondsAndWarrants());
            return;
        }

        setHtml("<iframe id=\"" + this.iframeName + "\" name=\"" + this.iframeName + "\" src=\"" // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
                + analyserUrl + "\" width=\"100%\" height=\"" + this.height + "\" frameborder=\"0\"></iframe>"); // $NON-NLS-0$ $NON-NLS-1$
    }

    private String getIframeName(String analyserUrl, String iframeName) {
        return analyserUrl.startsWith("http://") ? null : iframeName;    // $NON-NLS$
    }

    private String getAnalyserUrl(String plainUrl, MSCPriceData data) {
        return plainUrl + getAnalyserParameter(data, "");
    }

    private String getFlexChartParameter(MSCPriceData data) {
        final StringBuilder sb = new StringBuilder();

        append(sb, "userId", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS-0$
        if (data != null) {
            append(sb, "&gdatafield_1", data.getQuotedata().getVwdcode()); // $NON-NLS-0$
        }

        append(sb, "&baseUrl", getBaseUrl());  // $NON-NLS$

        if (!Customer.INSTANCE.isDzWgz()) {
            sb.append("&style=neutral"); // $NON-NLS-0$
        }

        return sb.toString();
    }

    private void append(StringBuilder sb, String key, String value) {
        if (value == null) {
            Firebug.debug("value is null for key: " + key); // $NON-NLS-0$
            return;
        }
        sb.append(key).append("=").append(URL.encodeQueryString(value)); // $NON-NLS-0$
    }

    private String getAnalyserParameter(MSCPriceData data, String def) {
        String result = "?locale=" + I18n.I.locale(); // $NON-NLS-0$
        result += (data == null) ? def : "&vwdcode=" + data.getQuotedata().getVwdcode(); // $NON-NLS-0$
        result += "&baseUrl=" + URL.encodeQueryString(getBaseUrl()); // $NON-NLS$
        result += "&imageBaseUrl=" + URL.encodeQueryString(getFlextoolsZoneBaseUrl());  // $NON-NLS$
        return result;
    }

    private String getBaseUrl() {
        if(SessionData.isAsDesign() && SessionData.isWithPmBackend()) {
            final String flextoolsZoneBaseUrl = getFlextoolsZoneBaseUrl();
            final String credentials = SessionData.INSTANCE.getCredentials();

            return flextoolsZoneBaseUrl + "/retrieve.xml?" // $NON-NLS$
                    + getCompanyIdParameter()
                    + "credential=" + URL.encode(credentials);  // $NON-NLS$
        }
        else {
            return GuiDefsLoader.getHostUrl() + "flexchart/retrieve.xml;"  // $NON-NLS$
                    + "jsessionid=" + SessionData.INSTANCE.getJsessionID(); //  // $NON-NLS$
        }
    }

    private String getCompanyIdParameter() {
        final String companyId = SessionData.INSTANCE.getGuiDefValue("flextools-companyid");  // $NON-NLS$
        if(companyId != null) {
            return "companyid=" + companyId + "&";   // $NON-NLS$
        }
        return "";
    }

    private String getFlextoolsZoneBaseUrl() {
        return GuiDefsLoader.getHostUrl() + (SessionData.isWithPmBackend() && SessionData.isAsDesign() ? "as/": "") + "dmxml-1/flextools" ;  // $NON-NLS$
    }

    public String getPrintHtml() {
        if (this.iframeName != null) {
            printGevasysAnalyser(this.iframeName);
            return null;
        }
        return "super"; // $NON-NLS-0$
    }

    private native void printGevasysAnalyser(String iframeName) /*-{
        for (var i = 0; i < parent.frames.length; i++) {
          if (parent.frames[i].name == iframeName) {
             //noinspection JSUnresolvedFunction
            parent.frames[i].startSWFPrint();
          }
        }
    }-*/;
}
