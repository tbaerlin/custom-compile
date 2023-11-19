package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import de.marketmaker.iview.dmxml.CERFinderElement;
import de.marketmaker.iview.dmxml.EDGData;
import de.marketmaker.iview.dmxml.EDGRatingData;
import de.marketmaker.iview.dmxml.WNTFinderElement;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.Settings;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * EdgUtil.java
 * Created on Jul 30, 2009 3:43:51 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 */
public class EdgUtil {

    private final static Map<String, String> EDG_RISK_CLASSES = new HashMap<String, String>();

    static {
        EDG_RISK_CLASSES.put("1", I18n.I.saftyDriven());  // $NON-NLS-0$
        EDG_RISK_CLASSES.put("2", I18n.I.partlyRiskAware());  // $NON-NLS-0$
        EDG_RISK_CLASSES.put("3", I18n.I.riskAware());  // $NON-NLS-0$
        EDG_RISK_CLASSES.put("4", I18n.I.increasedRiskAware());  // $NON-NLS-0$
        EDG_RISK_CLASSES.put("5", I18n.I.speculative());  // $NON-NLS-0$
    }

    public static String getEdgRiskClassString(String riskClassNumber) {
        final String s = EDG_RISK_CLASSES.get(riskClassNumber);
        return (s != null) ? s : riskClassNumber;
    }

    public static Link getEdgTopClassLink(EDGData edg) {
        final EDGRatingData edgRating = edg.getRating();
        final String rating = edgRating.getEdgTopScore();
        final String edgTopClass = edgRating.getEdgTopClass();

        final String toolTip = I18n.I.riskClass() + " " + edgTopClass + ": "  // $NON-NLS$
                + getEdgRiskClassString(rating);
        final String text = I18n.I.edgRiskClass(edgTopClass);
        return new Link(Settings.INSTANCE.edgDescriptionUrl(), "_blank", toolTip, text); // $NON-NLS-0$
    }

    public static String getEdgTopClassRating(EDGData edg) {
        return getStarHtml(edg.getRating().getEdgTopScore());
    }

    public static String getEdgTopClassRating(CERFinderElement edg) {
        return getStarHtml(edg.getEdgTopScore());
    }

    public static String getEdgTopClassRating(WNTFinderElement edg) {
        return getStarHtml(edg.getEdgTopScore());
    }

    public static String getEdgClassRating(EDGRatingData edg, int riskClass) {
        return getStarHtml(getRating(edg, riskClass));
    }

    public static DmxmlContext.Block<EDGData> createBlock(SnippetConfiguration config,
            DmxmlContext context) {
        final String edgBlockType = config.getString("edgBlockType"); // $NON-NLS-0$
        if (edgBlockType != null && Selector.EDG_RATING.isAllowed()) {
            return context.addBlock(edgBlockType);
        }
        return null;
    }

    public static DmxmlContext.Block<EDGData> createBlock(InstrumentTypeEnum type, DmxmlContext context) {
        if(type == null || !Selector.EDG_RATING.isAllowed()) {
            return null;
        }

        switch(type) {
            case CER:
                return context.addBlock("CER_EDG_Data");  // $NON-NLS$
            case WNT:
                return context.addBlock("WNT_EDG_Data");  // $NON-NLS$
            default:
                return null;
        }
    }

    public static boolean isTypeWithEdgData(InstrumentTypeEnum type) {
        return (type == InstrumentTypeEnum.CER || type == InstrumentTypeEnum.WNT);
    }

    private static String getRating(EDGRatingData edg, int riskClass) {
        switch (riskClass) {
            case 1:
                return edg.getEdgScore1();
            case 2:
                return edg.getEdgScore2();
            case 3:
                return edg.getEdgScore3();
            case 4:
                return edg.getEdgScore4();
            case 5:
                return edg.getEdgScore5();
            default:
                return null;
        }
    }

    private static String getStarHtml(String rating) {
        if (rating == null) {
            return "n/a"; // $NON-NLS-0$
        }
        return IconImage.get("edg-" + rating).getHTML(); // $NON-NLS$
    }

    public static SafeHtml getGoldenStarHtml(String rating) {
        if (rating == null) {
            return SafeHtmlUtils.fromString("<div>n/a</div>"); // $NON-NLS-0$
        }
        return IconImage.get("edg-gold-" + rating).getSafeHtml(); // $NON-NLS$
    }
}
