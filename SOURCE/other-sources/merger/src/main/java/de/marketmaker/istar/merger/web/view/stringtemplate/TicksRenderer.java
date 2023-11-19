/*
 * TicksRenderer.java
 *
 * Created on 08.03.13 14:15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.view.stringtemplate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.stringtemplate.v4.AttributeRenderer;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.common.util.XmlUtil;
import de.marketmaker.istar.domain.data.BidAskTradeTickImpl;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import static de.marketmaker.istar.domain.data.TickImpl.Type.BID_ASK;
import static de.marketmaker.istar.domain.data.TickImpl.Type.BID_ASK_TRADE;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.Type.TIME;

/**
 * Renders TickImpl objects as TicksTimeseries; due to complex logic, rendering is done in this
 * class rather than in a stringtemplate
 * @author oflege
 */
class TicksRenderer implements AttributeRenderer {
    private final DateTimeFormatter dateTime = ISODateTimeFormat.dateTimeNoMillis();

    private final DateTimeFormatter dateTimeMs = ISODateTimeFormat.dateTime();

    private final DecimalFormat df = Renderer.BigDecimalRenderer.createDefault();

    private boolean withPriceTagname;

    public TicksRenderer(String zone) {
        this.withPriceTagname = "vwd-rimpar".equals(zone) && FeatureFlags.isDisabled(FeatureFlags.Flag.VWD_RIMPAR_DECIMAL_TAG);
    }

    @Override
    public String toString(Object o, String format, Locale locale) {
        TickImpl t = (TickImpl) o;

        boolean withMillis = "ms".equals(format);

        StringBuilder sb = new StringBuilder(96);
        if (t.getType() == TickImpl.Type.SYNTHETIC_TRADE) {
            sb.append("<item priceModifier=\"SYNTHETIC_TRADE\">");
        }
        else {
            sb.append("<item>");
        }

        final DateTimeFormatter dtf = withMillis ? dateTimeMs : dateTime;
        sb.append("<date>").append(dtf.print(t.getDateTime())).append("</date>");

        if (t.hasFields()) {
            appendFields(sb, t.getFields(), withMillis);
        }

        if (t.getType() == BID_ASK_TRADE || t.getType() == BID_ASK) {
            append(sb, (BidAskTradeTickImpl) t);
        }
        else {
            append(sb, t);
        }

        sb.append("</item>");
        return sb.toString();
    }

    private void appendFields(StringBuilder sb, List<SnapField> fields, boolean withMillis) {
        for (SnapField sf : fields) {
            switch (sf.getType()) {
                case PRICE:
                    if (this.withPriceTagname) {
                        sb.append(renderDecimal("price", sf));
                    }
                    else {
                        sb.append(renderDecimal("decimal", sf));
                    }
                    break;
                case NUMBER:
                    int value = ((Number) sf.getValue()).intValue();
                    if (VwdFieldDescription.getField(sf.getId()).type() == TIME) {
                        value = decodeTime(value, withMillis);
                    }
                    sb.append("<number id=\"").append(sf.getId()).append("\">")
                            .append(value).append("</number>");
                    break;
                case STRING:
                    sb.append("<string id=\"").append(sf.getId()).append("\">")
                            .append(XmlUtil.encode((String) sf.getValue())).append("</string>");
                    break;
            }
        }
    }

    private String renderDecimal(String tag, SnapField sf) {
        return "<" + tag + " id=\"" + sf.getId() + "\">" + format(sf.getPrice()) + "</" + tag + ">";
    }

    private int decodeTime(int value, boolean withMillis) {
        if (withMillis) {
            return MdpsFeedUtils.decodeTime(value) * 1000 + MdpsFeedUtils.decodeTimeMillis(value);
        }
        return MdpsFeedUtils.decodeTime(value);
    }

    private void append(StringBuilder sb, TickImpl t) {
        switch (t.getType()) {
            case TRADE:
                // intentional fall-through
            case SYNTHETIC_TRADE:
                appendTrade(sb, t);
                break;
            case BID:
                appendTick(sb, "<bid>", t);
                break;
            case ASK:
                appendTick(sb, "<ask>", t);
                break;
        }
    }

    private void append(StringBuilder sb, BidAskTradeTickImpl t) {
        if (t.getPrice() != null) {
            appendTrade(sb, t);
        }
        if (t.getBidPrice() != null) {
            sb.append("<bid>");
            appendPrice(sb, t.getBidPrice());
            if (t.hasBidVolume()) {
                appendVolume(sb, t.getBidVolume());
            }
            sb.append("</bid>");
        }
        if (t.getAskPrice() != null) {
            sb.append("<ask>");
            appendPrice(sb, t.getAskPrice());
            if (t.hasAskVolume()) {
                appendVolume(sb, t.getAskVolume());
            }
            sb.append("</ask>");
        }
    }

    private void appendTrade(StringBuilder sb, TickImpl t) {
        appendTick(sb, "<trade>", t);
    }

    private void appendTick(StringBuilder sb, String tag, TickImpl t) {
        sb.append(tag);
        appendPrice(sb, t.getPrice());
        if (t.hasVolume()) {
            appendVolume(sb, t.getVolume());
        }
        if (t.getSupplement() != null) {
            sb.append("<supplement>").append(XmlUtil.encode(t.getSupplement())).append("</supplement>");
        }
        if (t.getTradeIdentifier() != null) {
            sb.append("<tradeIdentifier>").append(XmlUtil.encode(t.getTradeIdentifier())).append("</tradeIdentifier>");
        }
        sb.append("</").append(tag.substring(1));
    }

    private void appendVolume(StringBuilder sb, Long volume) {
        sb.append("<volume>").append(volume).append("</volume>");
    }

    private void appendPrice(StringBuilder sb, BigDecimal bd) {
        sb.append("<price>").append(format(bd)).append("</price>");
    }

    private String format(BigDecimal bd) {
        synchronized (this.df) {
            return this.df.format(bd);
        }
    }
}
