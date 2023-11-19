/*
 * PageProvider.java
 *
 * Created on 22.02.2008 16:50:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.feed.api.PageFeedConnector;
import de.marketmaker.istar.feed.api.PageRequest;
import de.marketmaker.istar.feed.api.PageResponse;
import de.marketmaker.istar.feed.api.TypedVendorkeysRequest;
import de.marketmaker.istar.feed.api.TypedVendorkeysResponse;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.EntitlementQuoteProvider;
import de.marketmaker.istar.merger.provider.IntradayData;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProviderImpl;
import de.marketmaker.istar.merger.provider.NoParameterException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VwdPageProvider implements PageProvider {

    public static final String NOT_ALLOWED_DE = "Seite nicht freigeschaltet";

    public static final String NOT_ALLOWED_EN = "No permission to access this page";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    static final int MAX_FRACTION_DIGITS = 6;

    private static final DateTimeFormatter[] DATE_TIME_FORMATTER = new DateTimeFormatter[]{
            DateTimeFormat.forPattern("yyyy"),
            DateTimeFormat.forPattern("dd.MM."),
            DateTimeFormat.forPattern("dd.MM.yy"),
            DateTimeFormat.forPattern("dd.MM.yyyy")
    };

    private final Map<Locale, DecimalFormat[]> decimalFormats
            = Collections.synchronizedMap(new HashMap<>());

    private IsoCurrencyConversionProvider currencyConversionProvider;

    private EntitlementQuoteProvider entitlementQuoteProvider;

    private IntradayProvider intradayProvider;

    private PageFeedConnector pageFeedConnector;

    private final PdlPageFactory pdlPageFactory = new PdlPageFactory();

    public void setCurrencyConversionProvider(
            IsoCurrencyConversionProvider currencyConversionProvider) {
        this.currencyConversionProvider = currencyConversionProvider;
    }

    public void setPageFeedConnector(PageFeedConnector pageFeedConnector) {
        this.pageFeedConnector = pageFeedConnector;
    }

    public void setEntitlementQuoteProvider(EntitlementQuoteProvider entitlementQuoteProvider) {
        this.entitlementQuoteProvider = entitlementQuoteProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    private DecimalFormat[] getDecimalFormat(Locale locale) {
        final DecimalFormat[] cached = this.decimalFormats.get(locale);
        if (cached != null) {
            return cached;
        }
        final DecimalFormat[] result = new DecimalFormat[MAX_FRACTION_DIGITS + 1];
        for (int i = 0; i < result.length; i++) {
            result[i] = (DecimalFormat) NumberFormat.getInstance(locale);
            result[i].setMaximumFractionDigits(i);
            result[i].setMinimumFractionDigits(i);
            result[i].setGroupingUsed(false);
        }
        this.decimalFormats.put(locale, result);
        return result;
    }

    public MergerPageResponse getPage(MergerPageRequest mergerRequest) throws Exception {
        final String pageid = mergerRequest.getPageId();
        if (!StringUtils.hasText(pageid)) {
            throw new NoParameterException("undefined pageId");
        }

        final int id;
        try {
            id = Integer.parseInt(pageid);
        } catch (NumberFormatException e) {
            throw new NoSuchPageException("invalid pageId", pageid);
        }

        if (id <= 0) {
            throw new NoSuchPageException("invalid pageId", pageid);
        }

        final PageRequest request = new PageRequest(id);
        if (mergerRequest.isPreferGermanText()) {
            request.setPreferGermanText(true);
        }
        final PageResponse response = getPage(request);
        if (response == null) {
            throw new NoSuchPageException("invalid pageId", pageid);
        }

        if (!response.isValid()) {
            throw new PageException("page load failed", pageid);
        }

        final MergerPageResponse result = new MergerPageResponse(mergerRequest.getPageId());
        final boolean allowed = isAllowed(response.getSelectors());
        if (allowed) {
            result.setText(response.getText());
            result.setLastUpdate(new DateTime(response.getTimestamp()));
        }
        else {
            result.setText(mergerRequest.isPreferGermanText() ? NOT_ALLOWED_DE : NOT_ALLOWED_EN);
        }

        result.setNextPageId(response.getNextPageId());
        result.setPreviousPageId(response.getPreviousPageId());

        if (!response.isDynamic() || !allowed) {
            return result;
        }

        result.setPdl(true);
        final PdlPage page = this.pdlPageFactory.createPage(pageid, response.getText());
        if (page == null) {
            throw new PageException("page rendering failed", pageid);
        }

        final List<String> vendorkeys = getVendorkeys(pageid, response);

        final Map<String, IntradayData> records = getRecords(pageid, vendorkeys);

        if (!records.isEmpty()) {
            final PageContentEvaluator evaluator = new PageContentEvaluator();
            evaluator.setCurrencyFactors(getCurrencyFactors(mergerRequest, records));
            evaluator.setDateTimeFormatter(DATE_TIME_FORMATTER); // todo: change depending on locale?
            evaluator.setDecimalFormat(getDecimalFormat(mergerRequest.getLocale()));
            evaluator.setPage(page);
            evaluator.setRecords(records);

            evaluator.evaluateDynamicContent();
            result.setLastUpdate(evaluator.getMaxUpdateTimestamp());
        }

        Properties properties = getProperties(mergerRequest.getRenderingProperties());
        final PdlRenderer renderer
                = mergerRequest.getRendererType().createRenderer(properties);

        final String formattedPage = renderer.render(page);
        result.setFormattedText(formattedPage);

        return result;
    }

    protected PageResponse getPage(PageRequest request) {
        if (this.pageFeedConnector == null) {
            return this.intradayProvider.getPage(request);
        }
        return this.pageFeedConnector.getPage(request);
    }

    protected List<String> getVendorkeys(String pageid, PageResponse response) {
        if (this.pageFeedConnector == null) {
            return response.getVendorkeys();
        }
        return getTypedKeys(response, pageid);
    }

    protected List<String> getTypedKeys(PageResponse response, String pageid) {
        List<String> vendorkeys = response.getVendorkeys();
        if (vendorkeys == null || vendorkeys.isEmpty()) {
            return Collections.emptyList();
        }
        TypedVendorkeysResponse typeResponse
                = this.intradayProvider.getTypesForVwdcodes(new TypedVendorkeysRequest(vendorkeys));
        if (!typeResponse.isValid()) {
            throw new PageException("vendorkey resolution failed", pageid);
        }
        return typeResponse.getTypedKeys();
    }

    Properties getProperties(final String[] renderingProperties) {
        final Properties p = StringUtils.splitArrayElementsIntoProperties(renderingProperties, "=");
        return p != null ? p : new Properties();
    }

    private boolean isAllowed(Set<String> selectors) {
        if (selectors == null) {
            return true;
        }
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        for (final String selector : selectors) {
            if (isAllowed(profile, selector)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowed(Profile profile, String selector) {
        return profile.isAllowed(Profile.Aspect.PAGE, selector)
                || profile.isAllowed(Profile.Aspect.NEWS, selector)
                || profile.isAllowed(Profile.Aspect.PRICE, selector)
                || profile.isAllowed(Profile.Aspect.FUNCTION, selector); // index const. groups
    }

    private Map<String, IntradayData> getRecords(String pageid, final List<String> vendorkeys) {
        if (vendorkeys.isEmpty()) {
            return Collections.emptyMap();
        }

        final List<Quote> quotes = this.entitlementQuoteProvider.getQuotes(vendorkeys);
        final List<IntradayData> datas = this.intradayProvider.getIntradayData(quotes, null);

        final Map<String, IntradayData> result =
                new HashMap<>(vendorkeys.size());

        for (int i = 0; i < vendorkeys.size(); i++) {
            final String key = vendorkeys.get(i);
            final IntradayData data = datas.get(i);
            if (data == null) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getRecords> no data for " + key + " on page " + pageid);
                }
                continue;
            }
            result.put(key.substring(key.indexOf('.') + 1), data);
        }

        return result;
    }

    private Map<String, BigDecimal> getCurrencyFactors(MergerPageRequest mergerRequest,
            Map<String, IntradayData> datas) {
        if (!StringUtils.hasText(mergerRequest.getCurrency())) {
            return null;
        }
        
        final Map<String, BigDecimal> result = new HashMap<>();

        for (IntradayData data : datas.values()) {
            final SnapField field = data.getSnap().getField(VwdFieldDescription.ADF_Waehrung.id());
            if (!field.isDefined()) {
                continue;
            }
            final String sourceCurrency = field.getValue().toString();
            if (result.containsKey(sourceCurrency)) {
                continue;
            }

            if (mergerRequest.getCurrency().equals(sourceCurrency)) {
                result.put(sourceCurrency, PageContentEvaluator.NO_CURRENCY_CONVERSION);
                continue;
            }

            try {
                final IsoCurrencyConversionProviderImpl.ConversionResult cdata
                        = this.currencyConversionProvider.getConversion(sourceCurrency, mergerRequest.getCurrency());
                result.put(sourceCurrency, cdata.getFactor());
            } catch (IllegalStateException e) {
                result.put(sourceCurrency, null);
            }
        }

        return result;
    }
}
