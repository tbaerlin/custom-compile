/*
 * Renderers.java
 *
 * Created on 12.07.2006 09:36:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.view.stringtemplate;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.common.spring.MessageSourceFactory;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.XmlUtil;
import de.marketmaker.istar.domain.ItemWithNames;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.HasPriceQuality;
import de.marketmaker.istar.domain.data.Localized;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.StockAnalysis;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.instrument.DerivativeTypeEnum;
import de.marketmaker.istar.domain.instrument.Future;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategies;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategy;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.MarketNameStrategies;
import de.marketmaker.istar.domain.instrument.MarketNameStrategy;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;
import de.marketmaker.istar.domain.instrument.Option;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategies;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.domain.instrument.TickerStrategies;
import de.marketmaker.istar.domain.instrument.TickerStrategy;
import de.marketmaker.istar.domain.util.NameUtil;
import de.marketmaker.istar.domainimpl.instrument.ContentFlagsDp2;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.rating.RatingSource;
import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.iview.dmxml.BlockType;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.temporal.TemporalAccessor;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;
import org.stringtemplate.v4.AttributeRenderer;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

/**
 * Various {@link AttributeRenderer} implementations. In st4, renderers cannot be assigned
 * to templates but just to template groups. Therefore, it is no longer possible to create
 * many small templates that just render <tt>$it$</tt> and assign different renderers to each
 * of them. In most cases, that is not much of a problem. However, if you register renderers for
 * classes that are part of an inheritance hierarchy, you want to make sure the these renderers
 * delegate to each other for formats they don't know.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class Renderer {

    private final static MessageSource MESSAGES
            = MessageSourceFactory.create(Renderer.class);

    private static final String GROUP_SUFFIX = "-group";

    private static String getMessage(String code, String defaultValue, Locale locale) {
        return MESSAGES.getMessage(code, null, defaultValue, locale);
    }

    public static class LocalizedNameRenderer implements AttributeRenderer {
        public static final LocalizedNameRenderer INSTANCE = new LocalizedNameRenderer();

        private LocalizedNameRenderer() {
        }

        public String toString(Object o, String format, Locale l) {
            if ("localName".equals(format)) {
                final Language language = Language.valueOf(l);
                final String localName = ((ItemWithNames) o).getNameOrDefault(language);
                return XmlUtil.encode(localName);
            }
            return String.valueOf(o);
        }
    }

    public static class LocalizedStringRenderer implements AttributeRenderer {

        public String toString(Object o, String format, Locale l) {
            if (format == null) {
                return toString(o, Language.valueOf(l));
            }
            if ("xml".equals(format) || "cdata".equals(format)) {
                return renderString(toString(o, Language.valueOf(l)), format, l);
            }
            if ("xmlWithDefault".equals(format)) {
                final LocalizedString ls = (LocalizedString) o;
                return renderString(ls.getValueOrDefault(Language.valueOf(l)), "xml", l);
            }
            if ("xmlWithLocale".equals(format)) {
                final List<Locale> locales = RequestContextHolder.getRequestContext().getLocales();
                for (final Locale locale : locales) {
                    final String s = toString(o, Language.valueOf(locale));
                    if (StringUtils.hasText(s)) {
                        return renderString(s, format, l);
                    }
                }
                // fall through
            }
            try {
                return toString(o, Language.valueOf(format));
            } catch (IllegalArgumentException e) {
                return toString(o, Language.valueOf(l));
            }
        }

        private String renderString(String value, String format, Locale l) {
            return StringRenderer.INSTANCE.toString(value, format, l);
        }

        public String toString(Object o, Language language) {
            return ((LocalizedString) o).getLocalized(language);
        }

        public String toString(Object o) {
            // implement default behaviour
            return ((LocalizedString) o).getLocalized(Language.de);
        }
    }


    public static class BigDecimalRenderer implements AttributeRenderer {
        private static final Map<String, DecimalFormat> FORMATS =
                new HashMap<>();

        static {
            FORMATS.put("price", createDefault());
            FORMATS.put("percent", createDefault());
        }

        static final BigDecimalRenderer INSTANCE = new BigDecimalRenderer();

        static DecimalFormat createDefault() {
            return create(Locale.US, "0.########");
        }

        private static DecimalFormat create(Locale locale, String pattern) {
            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(locale);
            df.applyLocalizedPattern(pattern);
            return df;
        }

        @Override
        public String toString(Object o, String format, Locale locale) {
            if ("frequency".equals(format)) {
                return FrequencyRenderer.INSTANCE.toString(o, format, locale);
            }
            final DecimalFormat df = FORMATS.get(format);
            if (df == null) {
                return ((BigDecimal) o).toPlainString();
            }
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (df) {
                final double number = "percent".equals(format)
                        ? ((BigDecimal) o).movePointLeft(2).doubleValue()
                        : ((BigDecimal) o).doubleValue();
                return df.format(number);
            }
        }
    }

    public static class ReadableInstantRenderer implements AttributeRenderer {
        final DateTimeFormatter dateTime = ISODateTimeFormat.dateTimeNoMillis();

        final DateTimeFormatter date = ISODateTimeFormat.date();

        final DateTimeFormatter time = ISODateTimeFormat.timeNoMillis();

        final DateTimeFormatter yearMonth = DateTimeFormat.forPattern("yyyy-MM");

        @Override
        public String toString(Object o, String format, Locale locale) {
            return getFormatter(format).print((ReadableInstant) o);
        }

        public DateTimeFormatter getFormatter(String format) {
            if ("date".equals(format)) {
                return this.date;
            }
            if ("time".equals(format)) {
                return this.time;
            }
            if ("yearMonth".equals(format)) {
                return this.yearMonth;
            }
            return dateTime;
        }
    }

    public static class TemporalAccessorRenderer implements AttributeRenderer {

        @Override
        public String toString(Object o, String format, Locale locale) {
            return getFormatter(format, locale).format((TemporalAccessor) o);
        }

        public java.time.format.DateTimeFormatter getFormatter(String format, Locale locale) {
            if ("date".equals(format)) {
                return java.time.format.DateTimeFormatter.ISO_DATE.withLocale(locale);
            }
            if ("time".equals(format)) {
                return java.time.format.DateTimeFormatter.ISO_TIME.withLocale(locale);
            }

            return java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME.withLocale(locale);
        }
    }

    public static class StringRenderer implements AttributeRenderer {
        public static final StringRenderer INSTANCE = new StringRenderer();

        private final static boolean WITH_PLUS = Boolean.getBoolean("metaDataKeyWithPlus");

        @Override
        public String toString(Object o, String s, Locale locale) {
            if (s == null) {
                // true for all template fragements, so handle common case first here
                return String.valueOf(o);
            }
            if ("xml".equals(s)) {
                return XmlUtil.encode((String) o);
            }
            if ("cdata".equals(s)) {
                return XmlUtil.encodeCData((String) o);
            }
            if ("pmParameterKey".equals(s)) {
                return toString((String) o, true);
            }
            if ("pmParameterValue".equals(s)) {
                return toString((String) o, false);
            }
            if ("metaKey".equals(s)) {
                if (WITH_PLUS) {
                    return XmlUtil.encode("+" + o);
                }
                else {
                    return XmlUtil.encode((String) o);
                }
            }
            return String.valueOf(o);
        }

        public String toString(String str, boolean forKey) {
            final int index = str.indexOf("=");
            if (index < 0) {
                return str;
            }
            return XmlUtil.encode(forKey ? str.substring(0, index) : str.substring(index + 1));
        }
    }

    public static class IntRenderer implements AttributeRenderer {
        @Override
        public String toString(Object o, String s, Locale locale) {
            Integer value = (Integer) o;
            if ("intdate".equals(s)) {
                if (value == 0) {
                    return "0000-00-00";
                }
                return ISODateTimeFormat.date()
                        .print(DateUtil.yyyymmddToDateTime(value));
            }
            if ("inttime".equals(s)) {
                return ISODateTimeFormat.timeNoMillis()
                        .print(DateUtil.toDateTime(DateUtil.dateToYyyyMmDd(), value));
            }
            if ("riskclass".equals(s)) {
                return getMessage("risk." + o, "n/a", locale);
            }
            if ("frequency".equals(s)) {
                return FrequencyRenderer.INSTANCE.toString(o, s, locale);
            }
            return String.valueOf(o);
        }
    }

    public static class RecommendationRenderer implements AttributeRenderer {
        @Override
        public String toString(Object o, String format, Locale locale) {
            final StockAnalysis.Recommendation r = (StockAnalysis.Recommendation) o;
            if ("recommendationEnum".equals(format)) {
                return r.name();
            }
            return getMessage(r.name(), r.name(), locale);
        }
    }

    public static class DistributionStrategyRenderer implements AttributeRenderer {
        @Override
        public String toString(Object o, String s, Locale locale) {
            final MasterDataFund.DistributionStrategy ds = (MasterDataFund.DistributionStrategy) o;
            return getMessage(ds.name(), "n/a", locale);
        }
    }

    public static class FrequencyRenderer implements AttributeRenderer {
        public static final FrequencyRenderer INSTANCE = new FrequencyRenderer();

        public String toString(Object o) {
            return toString(o, "frequency", RequestContextHolder.getRequestContext().getLocale());
        }

        @Override
        public String toString(Object o, String s, Locale locale) {
            if ("frequency".equals(s)) {
                return getMessage("freq." + o, "n/a", locale);
            }
            return String.valueOf(o);
        }
    }

    /**
     * Renders content flags just as they are available in the mdps feed: encoded as base64
     */
    public static class ContentFlagsRenderer implements AttributeRenderer {
        @Override
        public String toString(Object o, String s, Locale locale) {
            final ContentFlagsDp2 cf = (ContentFlagsDp2) o;
            return Base64.encodeBase64String(asBytes(cf.asBitSet()));
        }

        private byte[] asBytes(BitSet bs) {
            final byte[] result = new byte[(bs.length() - 1) / 8 + 1];
            for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
                result[i / 8] |= (1 << (i & 0x7));
            }
            return result;
        }
    }

    public static class PriceTypeRenderer implements AttributeRenderer {
        @Override
        public String toString(Object o, String s, Locale locale) {
            final Price price = (Price) o;
            if (price == null || price.getValue() == null) {
                return "n/a";
            }
            return getMessage(getMessageKey(price.getValue()), "n/a", locale);
        }

        private String getMessageKey(BigDecimal price) {
            final int cmp = price.compareTo(Constants.ONE_HUNDRED);
            return (cmp == 0) ? "price.eq" : (cmp > 0 ? "price.gt" : "price.lt");
        }
    }

    public static class DerivativeTypeRenderer implements AttributeRenderer {
        @Override
        public String toString(Object o, String s, Locale locale) {
            final DerivativeTypeEnum type = (DerivativeTypeEnum) o;
            if (type == DerivativeTypeEnum.CALL) {
                return "Call";
            }
            else if (type == DerivativeTypeEnum.PUT) {
                return "Put";
            }
            else {
                return "n/a";
            }
        }
    }

    public static class BooleanRenderer implements AttributeRenderer {
        static final BooleanRenderer INSTANCE = new BooleanRenderer();

        @Override
        public String toString(Object o, String s, Locale locale) {
            final Boolean value = (Boolean) o;
            if ("warrantAmerican".equals(s)) {
                // null as default is ok as both wnt.us and wnt.eu are defined
                return getMessage(value ? "wnt.us" : "wnt.eu", null, locale);
            }
            return value.toString();
        }
    }

    public static class InstrumentTypeRenderer implements AttributeRenderer {
        @Override
        public String toString(Object o, String s, Locale locale) {
            if ("localName".equals(s)) {
                return LocalizedNameRenderer.INSTANCE.toString(o, s, locale);
            }
            final InstrumentTypeEnum type = (InstrumentTypeEnum) o;
            return type.name();
        }
    }

    public static class QuoteRenderer implements AttributeRenderer {
        private QuoteNameStrategy getNameStrategy() {
            RequestContext ctx = RequestContextHolder.getRequestContext();
            return (ctx != null) ? ctx.getQuoteNameStrategy() : QuoteNameStrategies.DEFAULT;
        }

        private MarketNameStrategy getMarketNameStrategy() {
            RequestContext ctx = RequestContextHolder.getRequestContext();
            return (ctx != null) ? ctx.getMarketNameStrategy() : MarketNameStrategies.DEFAULT;
        }

        @Override
        public String toString(Object o, String format, Locale locale) {
            final Quote q = (Quote) o;
            if ("displayName".equals(format)) {
                return XmlUtil.encode(getNameStrategy().getName(q));
            }
            if ("name".equals(format)) {
                return XmlUtil.encode(NameUtil.getDisplayName1(q));
            }
            if ("localName".equals(format)) {
                return LocalizedNameRenderer.INSTANCE.toString(o, format, locale);
            }
            if ("adfLongName".equals(format)) {
                return XmlUtil.encode(q.getSymbolWmWpNameLang());
            }
            if ("pricedataType".equals(format)) {
                return toPricedataType(q);
            }
            if ("marketDisplayName".equals(format)) {
                return XmlUtil.encode(getMarketNameStrategy().getName(q));
            }
            if ("market".equals(format)) {
                final String vwd = q.getSymbolVwdfeedMarket();
                if (StringUtils.hasText(vwd)) {
                    return vwd;
                }
                return "id:" + (q.isNullQuote() ? null : q.getMarket().getId());
            }
            if ("quotedPer".equals(format)) {
                return toQuotedPer(q);
            }
            if ("ticker".equals(format)) {
                return XmlUtil.encode(getTickerStrategy().getTicker(q));
            }
            return String.valueOf(q);
        }

        public String toPricedataType(Quote quote) {
            final Instrument instrument = quote.getInstrument();

            if (InstrumentUtil.isVwdFund(quote)) {
                return "fund-otc";
            }
            else if (instrument instanceof Option || instrument instanceof Future) {
                return "contract-exchange";
            }

            if (InstrumentUtil.isLMEMarket(quote.getSymbolVwdfeedMarket())
                    && (RequestContextHolder.getRequestContext().isEnabled(FeatureFlags.Flag.LME_PRICEDATA_PROD)
                    || RequestContextHolder.getRequestContext().isEnabled(FeatureFlags.Flag.LME_PRICEDATA_DEV))) {
                return "lme";
            }

            return "standard";
        }

        public String toQuotedPer(Quote quote) {
            final MinimumQuotationSize mqs = quote.getMinimumQuotationSize();

            if (mqs == null) {
                return "UNKNOWN";
            }

            switch (mqs.getUnit()) {
                case PERCENT:
                    return "PERCENT";
                case PERMILLE:
                    return "PERMILLE";
                case POINT:
                    return "POINT";
                case UNIT:
                    return "UNIT";
                case NOTHING:
                case OTHER:
                    return "UNKNOWN";
            }

            //TODO: introduce permissioning for WM data

            return "UNIT";
        }

    }

    public static class InstrumentRenderer implements AttributeRenderer {
        @Override
        public String toString(Object o, String format, Locale locale) {
            final Instrument instrument = (Instrument) o;
            if ("isEtf".equals(format)) {
                for (Quote quote : instrument.getQuotes()) {
                    if ("XETF".equals(quote.getSymbolVwdfeedMarket())) {
                        return "true";
                    }
                }
                return "false";
            }
            if ("name".equals(format)) {
                for (final Quote quote : instrument.getQuotes()) {
                    if (quote.getMarket().getId() == instrument.getHomeExchange().getId()) {
                        return XmlUtil.encode(NameUtil.getDisplayName1(quote));
                    }
                }

                final String name = instrument.getName();
                if (StringUtils.hasText(name)) {
                    return XmlUtil.encode(name);
                }

                return "iid:" + instrument.getId();
            }
            if ("localName".equals(format)) {
                return LocalizedNameRenderer.INSTANCE.toString(o, format, locale);
            }
            if ("displayName".equals(format)) {
                return XmlUtil.encode(getNameStrategy().getName(instrument));
            }
            if ("ticker".equals(format)) {
                return XmlUtil.encode(getTickerStrategy().getTicker(instrument));
            }
            return String.valueOf(o);
        }

    }

    private static TickerStrategy getTickerStrategy() {
        RequestContext ctx = RequestContextHolder.getRequestContext();
        return (ctx != null) ? ctx.getTickerStrategy() : TickerStrategies.DEFAULT;
    }

    private static InstrumentNameStrategy getNameStrategy() {
        RequestContext ctx = RequestContextHolder.getRequestContext();
        return (ctx != null) ? ctx.getInstrumentNameStrategy() : InstrumentNameStrategies.DEFAULT;
    }

    public static class PeriodRenderer implements AttributeRenderer {
        @Override
        public String toString(Object o, String s, Locale locale) {
            final Period p;
            if (o instanceof Interval) {
                p = ((Interval) o).toPeriod(PeriodType.yearMonthDayTime());
            }
            else if (o instanceof Period) {
                p = (Period) o;
            }
            else {
                throw new IllegalArgumentException(String.valueOf(o));
            }
            return ISOPeriodFormat.standard().print(p);
        }
    }

    public static class PriceQualityRenderer implements AttributeRenderer {
        @Override
        public String toString(Object o, String s, Locale locale) {
            final HasPriceQuality pr = (HasPriceQuality) o;
            if (pr.isPushAllowed()) {
                return pr.getPriceQuality().name() + "+";
            }
            return pr.getPriceQuality().name();
        }
    }

    public static class LanguageForLocalizedObjectsRenderer implements AttributeRenderer {
        @Override
        public String toString(Object o, String s, Locale locale) {
            final Localized localized = (Localized) o;
            if (localized.getLanguage() != null) {
                return "language=\"" + localized.getLanguage() + "\"";
            }
            return "";
        }
    }

    public static class RatingSourceRenderer implements AttributeRenderer {

        @Override
        public String toString(Object o, String s, Locale locale) {
            if (!(o instanceof RatingSource)) {
                throw new IllegalArgumentException("can only render RatingSource, given: " + o.getClass());
            }
            final RatingSource ratingSource = (RatingSource) o;
            //noinspection IfCanBeSwitch
            if ("xml".equals(s)) {
                return XmlUtil.encode(ratingSource.getFullName());
            }
            else if ("cdata".equals(s)) {
                return XmlUtil.encodeCData(ratingSource.getFullName());
            }
            else {
                return ratingSource.getFullName();
            }
        }
    }

    public static class SnapFieldRenderer implements AttributeRenderer {

        private final DecimalFormat df = Renderer.BigDecimalRenderer.createDefault();

        @Override
        public String toString(Object o, String format, Locale locale) {
            if (!SnapField.class.isAssignableFrom(o.getClass())) {
                throw new IllegalArgumentException("can only render SnapField, given: " + o.getClass());
            }

            final String priceTag = "timeseries".equals(format) ? "decimal" : "price";

            final SnapField snapField = (SnapField) o;
            //noinspection IfCanBeSwitch
            final StringBuilder sb = new StringBuilder();
            switch (snapField.getType()) {
                case PRICE:
                    sb.append("<").append(priceTag).append(" id=\"").append(snapField.getId()).append("\">")
                            .append(format(snapField.getPrice())).append("</").append(priceTag).append(">");
                    break;
                case NUMBER:
                    int value = ((Number) snapField.getValue()).intValue();
                    sb.append("<number id=\"").append(snapField.getId()).append("\">")
                            .append(value).append("</number>");
                    break;
                case STRING:
                    sb.append("<string id=\"").append(snapField.getId()).append("\">")
                            .append(XmlUtil.encode((String) snapField.getValue())).append("</string>");
                    break;
            }
            return sb.toString();
        }

        private String format(BigDecimal bd) {
            synchronized (this.df) {
                return this.df.format(bd);
            }
        }
    }

    public static class BlockTypeRenderer implements AttributeRenderer {
        @Override
        public String toString(Object o, String format, Locale locale) {
            final BlockType blockType = (BlockType) o;
            final JaxbHandler jaxbHandler = new JaxbHandler(blockType.getClass().getPackage().getName());
            return jaxbHandler.marshal(BlockType.class, blockType, "block");
        }
    }

    static void registerDefaultRenderers(STGroup g) {
        // order is important as the first matching renderer will be used
        // renderers for subclasses must be registered before renderers of any
        // superclass/interface (e.g, BigDecimal before Number); however, the renderers may
        // then delegate to renderers of less specific classes if they don't know how to
        // handle a specific format.

        final String name = g.getName();
        final String zone = name.endsWith(GROUP_SUFFIX)
                ? name.substring(0, name.length() - GROUP_SUFFIX.length())
                : name;

        g.registerRenderer(BigDecimal.class, Renderer.BigDecimalRenderer.INSTANCE, true);
        g.registerRenderer(LocalizedString.class, new Renderer.LocalizedStringRenderer(), true);
        g.registerRenderer(ReadableInstant.class, new Renderer.ReadableInstantRenderer(), true);
        g.registerRenderer(TemporalAccessor.class, new TemporalAccessorRenderer(), true);
        g.registerRenderer(String.class, new Renderer.StringRenderer(), true);
        g.registerRenderer(Integer.class, new Renderer.IntRenderer(), true);
        g.registerRenderer(Interval.class, new Renderer.PeriodRenderer(), true);
        g.registerRenderer(StockAnalysis.Recommendation.class, new Renderer.RecommendationRenderer(), true);
        g.registerRenderer(MasterDataFund.DistributionStrategy.class,
                new Renderer.DistributionStrategyRenderer(), true);
        g.registerRenderer(Quote.class, new Renderer.QuoteRenderer(), true);
        g.registerRenderer(Instrument.class, new Renderer.InstrumentRenderer(), true);
        g.registerRenderer(ContentFlagsDp2.class, new Renderer.ContentFlagsRenderer(), true);
        g.registerRenderer(HasPriceQuality.class, new Renderer.PriceQualityRenderer(), true);
        g.registerRenderer(Price.class, new Renderer.PriceTypeRenderer(), true);
        g.registerRenderer(DerivativeTypeEnum.class, new Renderer.DerivativeTypeRenderer(), true);
        g.registerRenderer(InstrumentTypeEnum.class, new Renderer.InstrumentTypeRenderer(), true);
        g.registerRenderer(ItemWithNames.class, Renderer.LocalizedNameRenderer.INSTANCE);
        g.registerRenderer(Number.class, new Renderer.FrequencyRenderer(), true);
        g.registerRenderer(Localized.class, new Renderer.LanguageForLocalizedObjectsRenderer(), true);
        g.registerRenderer(Boolean.class, BooleanRenderer.INSTANCE, true);
        g.registerRenderer(TickImpl.class, new TicksRenderer(zone), true);
        g.registerRenderer(RatingSource.class, new RatingSourceRenderer(), true);
        g.registerRenderer(SnapField.class, new SnapFieldRenderer(), true);
        g.registerRenderer(BlockType.class, new BlockTypeRenderer(), true);

        // this ensures that the instrumentdata-template works if called with an instrument as argument
        // as it references properties using quote.instrument.xyz
        g.registerModelAdaptor(Instrument.class, new ObjectModelAdaptor() {
            @Override
            public Object getProperty(Interpreter interp, ST self, Object o, Object property,
                    String propertyName) throws STNoSuchPropertyException {
                if ("instrument".equals(propertyName)) {
                    return o;
                }
                return super.getProperty(interp, self, o, property, propertyName);
            }
        });
    }

}
