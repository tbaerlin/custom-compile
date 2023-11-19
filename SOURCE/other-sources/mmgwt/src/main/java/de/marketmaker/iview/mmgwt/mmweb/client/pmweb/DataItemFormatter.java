package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import de.marketmaker.itools.gwtutil.client.i18n.LocalizedFormatter;
import de.marketmaker.itools.gwtutil.client.i18n.NumberFormatterIfc;
import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.chart.MiniBar;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsDateTimeEdit;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.HasCode;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMBool;
import de.marketmaker.iview.pmxml.MMDBRef;
import de.marketmaker.iview.pmxml.MMDateTime;
import de.marketmaker.iview.pmxml.MMNumber;
import de.marketmaker.iview.pmxml.MMString;
import de.marketmaker.iview.pmxml.MMTypRef;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ThreeValueBoolean;
import de.marketmaker.iview.pmxml.TiDateKind;
import de.marketmaker.iview.pmxml.TiType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Render a given DataItem to a String.
 * <br/>
 * If the DataItem is a MMNumber, VUnit and NumberProcent are evaluated
 * in order to set the decimal places and percent sign.
 *
 * @author umaurer
 * Created: 05.12.14
 */
public class DataItemFormatter {
    private static final DateTimeFormatter DTF_DATE = new DateTimeFormatter() {
        public String format(MmJsDate date) {
            return JsDateFormatter.formatDdmmyyyy(date);
        }
    };
    private static final DateTimeFormatter DTF_DATETIME = new DateTimeFormatter() {
        public String format(MmJsDate date) {
            return JsDateFormatter.formatDdmmyyyyHhmm(date);
        }
    };
    private static final DateTimeFormatter DTF_DATETIME_SECONDS = new DateTimeFormatter() {
        public String format(MmJsDate date) {
            return JsDateFormatter.formatDdmmyyyyHhmmss(date);
        }
    };
    private static final DateTimeFormatter DTF_TIME = new DateTimeFormatter() {
        public String format(MmJsDate date) {
            return JsDateFormatter.formatHhmm(date);
        }
    };
    private static final DateTimeFormatter DTF_TIME_SECONDS = new DateTimeFormatter() {
        public String format(MmJsDate date) {
            return JsDateFormatter.formatHhmmss(date);
        }
    };
    private static final BooleanFormatter BOOLEAN_FORMATTER = new BooleanFormatter();

    private final NumberFormatter numberFormatter;
    private final DateTimeFormatter dateTimeFormatter;
    private final MMDBRefFormatter mmDBRefFormatter;
    private final MMTypeRefFormatter mmTypeRefFormatter;

    public static String format(ParsedTypeInfo pti, MM item) {
        return format(item, new NumberFormatter(pti), getDateTimeFormatter(pti), getMMDBRefFormatter(pti), getMMTypeRefFormatter(pti));
    }

    public static String format(MM item, NumberFormatter numberFormatter, DateTimeFormatter dateTimeFormatter
            , MMDBRefFormatter mmDBRefFormatter, MMTypeRefFormatter mmTypeRefFormatter) {
        if (item == null || item instanceof DefaultMM) {
            return null;
        }
        else if (item instanceof MMNumber) {
            return numberFormatter.format((MMNumber)item);
        }
        else if (item instanceof MMDateTime) {
            return dateTimeFormatter.format((MMDateTime)item);
        }
        else if (item instanceof MMBool) {
            return BOOLEAN_FORMATTER.format((MMBool) item);
        }
        else if (item instanceof ShellMMInfo) {
            return ((ShellMMInfo)item).getBezeichnung();
        }
        else if (item instanceof MMString) {
            return ((MMString)item).getValue();
        }
        else if (item instanceof MMDBRef) {
            return mmDBRefFormatter == null ? ((MMDBRef)item).getValue() : mmDBRefFormatter.format((MMDBRef)item);
        }
        else if (item instanceof MMTypRef) {
            return mmTypeRefFormatter == null ? ((MMTypRef)item).getValue() : mmTypeRefFormatter.format((MMTypRef)item);
        }
        throw new IllegalArgumentException("DataItemFormatter <format> DataItem type not handled: " + item.getClass().getSimpleName()); // $NON-NLS$
    }

    public DataItemFormatter(ParsedTypeInfo pti, String style) {
        if (CssUtil.hasStyle(style, "miniBar")) { // $NON-NLS$
            this.numberFormatter = new MiniBarFormatter(pti, style);
        }
        else {
            this.numberFormatter = new NumberFormatter(pti);
        }
        this.dateTimeFormatter = getDateTimeFormatter(pti);
        this.mmDBRefFormatter = getMMDBRefFormatter(pti);
        this.mmTypeRefFormatter= getMMTypeRefFormatter(pti);
    }

    public NumberFormatter getNumberFormatter() {
        return this.numberFormatter;
    }

    public DataItemFormatter withTrailingZeros(boolean trailingZeros) {
        this.numberFormatter.setRemoveTrailingZeros(!trailingZeros);
        return this;
    }

    public String format(MM item) {
        return format(item, this.numberFormatter, this.dateTimeFormatter, this.mmDBRefFormatter, this.mmTypeRefFormatter);
    }

    public String format(BigDecimal bd) {
        return format(bd, false);
    }

    public String format(BigDecimal bd, boolean removeTrailingZeros) {
        return this.numberFormatter.format(bd, removeTrailingZeros);
    }

    public BigDecimal value(MM item) {
        return item instanceof MMNumber
                ? this.numberFormatter.value((MMNumber) item)
                : null;
    }

    public BigDecimal value(String s) {
        return this.numberFormatter.value(s);
    }

    public boolean isPercent() {
        return this.numberFormatter.pti.isNumberProcent();
    }

    interface Formatter<DI extends MM> {
        String format(DI item);
    }

    static class BooleanFormatter implements Formatter<MMBool> {
        @Override
        public String format(MMBool item) {
            return (item == null || item.getValue() == ThreeValueBoolean.TV_NULL)
                    ? null
                    : (item.getValue() == ThreeValueBoolean.TV_TRUE ? I18n.I.yes() : I18n.I.no());
        }
    }

    public static class NumberFormatter implements Formatter<MMNumber>, NumberFormatterIfc {
        private final ParsedTypeInfo pti;
        private final int numberScale;
        private boolean removeTrailingZeros = true;

        public NumberFormatter(ParsedTypeInfo pti) {
            this.pti = pti;
            this.numberScale = MmTalkHelper.getBigDecimalScale(pti.getVUnit());
        }

        public void setRemoveTrailingZeros(boolean removeTrailingZeros) {
            this.removeTrailingZeros = removeTrailingZeros;
        }

        @Override
        public String format(MMNumber item) {
            final String s = item.getValue();
            if (s == null) {
                return null;
            }
            return format(new BigDecimal(s), this.removeTrailingZeros);
        }

        public String format(BigDecimal bd, boolean removeTrailingZeros) {
            if (this.numberScale != -1) {
                bd = bd.setScale(this.numberScale, RoundingMode.HALF_UP);
            }
            if (this.pti.isNumberProcent()) {
                bd = bd.movePointRight(2);
            }
            final String formatted = LocalizedFormatter.getInstance().formatDecimal(bd, removeTrailingZeros);
            return this.pti.isNumberProcent() ? (formatted + "%") : formatted;
        }

        public String format(Number n) {
            final BigDecimal bd = n instanceof BigDecimal
                    ? (BigDecimal) n
                    : new BigDecimal(n.doubleValue());
            return format(bd, this.removeTrailingZeros);
        }

        public BigDecimal value(MMNumber item) {
            return value(item.getValue());
        }

        public BigDecimal value(String s) {
            if (s == null) {
                return null;
            }
            final BigDecimal value = new BigDecimal(s);
            return this.pti.isNumberProcent() ? value.movePointRight(2) : value;
        }
    }

    public static class MiniBarFormatter extends NumberFormatter {
        private final double minValue;
        private final double maxValue;
        private final Integer width;

        public MiniBarFormatter(ParsedTypeInfo pti, String style) {
            super(pti);
            this.minValue = parseMinMax("Min", pti.getMin()); // $NON-NLS$
            this.maxValue = parseMinMax("Max", pti.getMax()); // $NON-NLS$
            this.width = CssUtil.getStyleValueInt(style, "width"); // $NON-NLS$
        }

        private double parseMinMax(String type, String m) {
            if (m == null) {
                throw new NullPointerException("ParsedTypeInfo." + type + " of MiniBarFormatter value is null"); // $NON-NLS$
            }
            return Double.parseDouble(m);
        }

        @Override
        public String format(MMNumber item) {
            final String s = item.getValue();
            return s == null
                    ? null
                    : MiniBar.asHtml(Double.parseDouble(s), this.minValue, this.maxValue, this.width);
        }
    }

    public static DateTimeFormatter getDateTimeFormatter(ParsedTypeInfo pti) {
        final TiDateKind dateKind = pti.getDateKind() == null ? TiDateKind.DK_DATE : pti.getDateKind();
        switch (dateKind) {
            case DK_DATE_TIME:
                return pti.isIsTimeSeconds()
                        ? DTF_DATETIME_SECONDS
                        : DTF_DATETIME;
            case DK_TIME:
                return pti.isIsTimeSeconds()
                        ? DTF_TIME_SECONDS
                        : DTF_TIME;
            default:
                return DTF_DATE;
        }
    }

    public abstract static class DateTimeFormatter implements Formatter<MMDateTime> {
        @Override
        public String format(MMDateTime item) {
            final MmJsDate date = JsDateFormatter.parseDdmmyyyy(item.getValue(), true, SpsDateTimeEdit.MSG);
            if (date == null) {
                return null;
            }
            return format(date);
        }

        abstract String format(MmJsDate date);
    }

    public static MMDBRefFormatter getMMDBRefFormatter(ParsedTypeInfo pti) {
        if (pti.getTypeId() == TiType.TI_ENUMERATION) {
            return new MMDBRefFormatter(pti);
        }
        return null;
    }

    public static class MMDBRefFormatter implements Formatter<MMDBRef> {
        private final Map<String, String> map;

        public MMDBRefFormatter(ParsedTypeInfo pti) {
            this.map = toEnumMap(pti);
        }

        @Override
        public String format(MMDBRef item) {
            final String display = this.map.get(item.getValue());
            return display == null ? item.getValue() : display;
        }
    }

    public static MMTypeRefFormatter getMMTypeRefFormatter(ParsedTypeInfo pti) {
        if (pti.getTypeId() == TiType.TI_ENUMERATION) {
            return new MMTypeRefFormatter(pti);
        }
        return null;
    }

    public static class MMTypeRefFormatter implements Formatter<MMTypRef> {
        private final Map<String, String> map;

        public MMTypeRefFormatter(ParsedTypeInfo pti) {
            this.map = toEnumMap(pti);
        }

        @Override
        public String format(MMTypRef item) {
            final String display = this.map.get(item.getValue());
            return display == null ? item.getValue() : display;
        }
    }

    public static Map<String, String> toEnumMap(ParsedTypeInfo pti) {
        final List<MM> mms = pti.getEnumElements();
        final Map<String, String> map = new HashMap<>(mms.size() * 4 / 3);
        for (MM mm : mms) {
            if(mm instanceof HasCode) {
                HasCode mmEnum = (HasCode)mm;
                map.put(mmEnum.getCode(), mmEnum.getValue());
            }
            else {
                Firebug.warn("<DataItemFormatter.toEnumMap> enum element does not implement HasCode: " + MmTalkHelper.toLogString(mm));
            }
        }
        return map;
    }
}