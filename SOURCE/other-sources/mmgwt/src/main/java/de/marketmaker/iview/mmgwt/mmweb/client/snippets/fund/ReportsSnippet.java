/*
 * ReportsSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;

import de.marketmaker.iview.dmxml.ReportType;
import de.marketmaker.iview.dmxml.ReportsBlock;
import de.marketmaker.iview.dmxml.WMStaticData;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz.DzPibReportLinkController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.DesktopIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitFNDController;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DesktopSnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ReportsSnippet extends
        AbstractSnippet<ReportsSnippet, DesktopSnippetView<ReportsSnippet>> implements
        SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("Reports", I18n.I.fundBrochures()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ReportsSnippet(context, config);
        }
    }

    private static final Map<String, String> MAP_NAMES;

    static {
        MAP_NAMES = new HashMap<>(4);
        MAP_NAMES.put("ANNUAL_REPORT", I18n.I.fndReportAnnualReport()); // $NON-NLS-0$
        MAP_NAMES.put("PROSPECTUS", I18n.I.fndReportProspectus()); // $NON-NLS-0$
        MAP_NAMES.put("SEMIANNUAL_REPORT", I18n.I.fndReportSemiannualReport()); // $NON-NLS-0$
        MAP_NAMES.put("SIMPLIFIED_PROSPECTUS", I18n.I.fndReportSimplifiedProspectus()); // $NON-NLS-0$
    }

    private final DmxmlContext.Block<ReportsBlock> block;

    private DmxmlContext.Block<WMStaticData> blockWmStaticData;

    private boolean escapeUrl;

    private ReportsSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.setView(new DesktopSnippetView<>(this));

        this.block = createBlock(config.getString("blockName")); // $NON-NLS-0$

        if (Customer.INSTANCE.isOlb() && "FND_Reports".equals(config.getString("blockName"))) {  // $NON-NLS$
            this.blockWmStaticData = createBlock("WM_StaticData");  // $NON-NLS-0$
        }

        setSymbol(InstrumentTypeEnum.FND, config.getString("symbol", null), null); // $NON-NLS-0$

        final String country = config.getString("country", null); // $NON-NLS-0$
        if (StringUtil.hasText(country)) {
            this.block.setParameter("country", country); // $NON-NLS-0$
        }
        final String language = config.getString("language", null); // $NON-NLS-0$
        if (StringUtil.hasText(language)) {
            this.block.setParameter("language", language); // $NON-NLS-0$
        }

        this.escapeUrl = config.getBoolean("escapeUrl", true); // $NON-NLS-0$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name,
            String... compareSymbols) {
        getConfiguration().put("symbol", symbol);  // $NON-NLS$
        this.block.setEnabled(symbol != null);
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
        if (this.blockWmStaticData != null) {
            this.blockWmStaticData.setParameter("symbol", symbol); // $NON-NLS-0$
        }
    }

    public void destroy() {
        destroyBlock(this.block);
        destroyBlock(this.blockWmStaticData);
    }

    public void updateView() {
        if (this.block.isResponseOk()) {
            final ReportsBlock fndReports = this.block.getResult();
            final List<DesktopIcon> list = new ArrayList<>();
            for (ReportType report : fndReports.getReport()) {
                if (canBeIncluded(report)) {
                    list.add(createReportIcon(report));
                }
            }
            getView().update(list);
        }
        else {
            getView().update(new ArrayList<DesktopIcon>());
        }
    }

    private boolean canBeIncluded(ReportType report) {
        if (this.blockWmStaticData != null) {
            final Map<String, String> fndModel = PortraitFNDController.buildFndModel(this.blockWmStaticData);
            boolean isSingelHedgeFond = PortraitFNDController.isSingleHedgeFond(fndModel);

            if (isSingelHedgeFond && ("KIID".equals(report.getType()) || "Prospectus".equals(report.getType()))) {  // $NON-NLS$
                return false;
            }
        }

        return true;
    }

    private String getTitle(String title) {
        final String result = MAP_NAMES.get(title);
        return result == null ? title : result;
    }

    private DesktopIcon createReportIcon(ReportType report) {
        final String url = this.escapeUrl ? JsUtil.escapeUrl(report.getUrl()) : report.getUrl();
        final String[] labels = new String[]{
                getTitle(report.getTitle()),
                report.getDate(),
                report.getFilesizeBytes() == null ? "" : report.getFilesizeBytes() + " " + I18n.I.bytes(), // $NON-NLS$
        };

        if (report.getType().equals("PIB")) { // $NON-NLS$
            final String symbol = getConfiguration().getString("symbol");  // $NON-NLS$
            return new DesktopIcon<>("mm-desktopIcon-pdf", labels, symbol, new LinkListener<String>() {  // $NON-NLS-0$
                @Override
                public void onClick(LinkContext<String> context, Element e) {
                    DzPibReportLinkController.INSTANCE.openDialogOrReport(context.getData());
                }
            });
        }
        else {
            return new DesktopIcon("mm-desktopIcon-pdf", labels, url);// $NON-NLS-0$
        }
    }

}
