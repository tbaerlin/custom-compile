/*
 * ImgRatioUniverse.java
 *
 * Created on 29.08.2006 14:00:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.common.spring.MessageSourceFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.web.easytrade.EnumEditor;
import de.marketmaker.istar.merger.web.easytrade.block.AbstractFindersuchergebnis;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;
import de.marketmaker.istar.chart.ChartModelAndView;
import de.marketmaker.istar.chart.data.PieData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgRatioUniverse extends AbstractImgChart {
    private final static MessageSource MESSAGES = MessageSourceFactory.create(InstrumentTypeEnum.class);

    private RatiosProvider ratiosProvider;

    private static Set<String> miscNames = new HashSet<>(Arrays.asList("SONSTIG", "Unbekannt"));

    private static final String MISC_NAME = "Sonstige";

    public ImgRatioUniverse() {
        super(ImgRatioUniverseCommand.class);
    }

    protected void initBinder(HttpServletRequest httpServletRequest,
            ServletRequestDataBinder binder) throws Exception {
        super.initBinder(httpServletRequest, binder);
        binder.registerCustomEditor(InstrumentTypeEnum.class, new EnumEditor<>(InstrumentTypeEnum.class));
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    protected ChartModelAndView createChartModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object object,
            BindException bindException) throws Exception {
        final ImgRatioUniverseCommand cmd = (ImgRatioUniverseCommand) object;

        final InstrumentTypeEnum type = cmd.getType();

        final PermissionType pt;
        if (type == InstrumentTypeEnum.FND) {
            pt = PermissionType.FUNDDATA;
        }
        else if (type == InstrumentTypeEnum.CER) {
            pt = null;
        }
        else {
            pt = null;
        }

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = AbstractFindersuchergebnis.getFields(type, cmd.getProviderPreference(), pt);

        final RatioFieldDescription.Field field = AbstractFindersuchergebnis.getField(fields, cmd.getField());

        if (field == null || !field.isEnum()) {
            bindException.reject("ImgRatiosUniverse.failed", "no enum field: " + cmd.getField());
            return null;
        }

        final Map<Object, Integer> counts = getCounts(this.ratiosProvider, cmd.getProviderPreference(), type, field,
                cmd.getQuery(), cmd.getMinCount(), cmd.getNumElements());

        if (counts == null) {
            bindException.reject("ImgRatiosUniverse.failed", "invalid search response");
            return null;
        }

        final PieData pd = getByPercent(counts, toLocale(cmd.getLocale()));
        pd.finish();

        final ChartModelAndView result = new ChartModelAndView(cmd.getLayout(), cmd.getStyle(),
                cmd.getWidth(), cmd.getHeight(), getEncodingConfig());
        result.addObject("pie", pd);
        return result;
    }

    public static Locale toLocale(String localeStr) {
        //The framework already checks the validity of the locale string.
        //hence, no check is necessary here.
        if(localeStr != null) {
            return new Locale(localeStr);
        }
        return Locale.getDefault();
    }

    public static Map<Object, Integer> getCounts(RatiosProvider ratiosProvider,
            String providerPreference, InstrumentTypeEnum type, RatioFieldDescription.Field field,
            String query, int minCount,
            int numElements) {
        final RatioSearchRequest rsr = new RatioSearchRequest(RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales());
        rsr.setType(type);
        rsr.setFieldidForResultCount(field.id());

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = AbstractFindersuchergebnis.getFields(type, providerPreference);
        final Map<String, String> parameters = AbstractFindersuchergebnis.parseQuery(query, fields);
        rsr.addParameters(parameters);

        final RatioSearchResponse sr = ratiosProvider.search(rsr);

        final Map<Object, Integer> counts;
        if (sr.isValid()) {
            counts = ((DefaultRatioSearchResponse) sr).getResultGroupCount();
        }
        else {
            counts = null;
        }

        if (minCount != Integer.MIN_VALUE) {
            return getMapForMinCount(counts, minCount);
        }
        else if (numElements != Integer.MIN_VALUE) {
            return getMapWithNumElements(counts, numElements);
        }

        return counts;
    }

    private PieData getByPercent(Map<Object, Integer> counts, Locale locale) {
        final PieData.ByValue pd = new PieData.ByValue();
        if (!counts.isEmpty()) {
            for (Map.Entry<Object, Integer> entry : counts.entrySet()) {
                final String key = entry.getKey().toString();
                String mapping = getMessage(key, locale);
                pd.add(mapping == null ? key : mapping + " (" + entry.getValue() + ")", entry.getValue());
            }
        }
        return pd;
    }

    private String getMessage(String key, Locale locale) {
        try {
            return MESSAGES.getMessage(key, null, locale);
        }
        catch(NoSuchMessageException e) {
            return null;
        }
    }

    private static Map<Object, Integer> getMapForMinCount(Map<Object, Integer> counts,
            int minCount) {
        final Map<Object, Integer> result = new HashMap<>();

        int otherCount = 0;
        for (Map.Entry<Object, Integer> entry : counts.entrySet()) {
            final String name = entry.getKey().toString();

            if (miscNames.contains(name) || entry.getValue() < minCount) {
                otherCount += entry.getValue();
            }
            else {
                result.put(name, entry.getValue());
            }
        }

        if (otherCount > 0) {
            result.put(MISC_NAME, otherCount);
        }

        return result;
    }

    private static Map<Object, Integer> getMapWithNumElements(final Map<Object, Integer> counts,
            int numElements) {
        final Object[] keys = counts.keySet().toArray();
        Arrays.sort(keys, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                final Integer i1 = counts.get(o1);
                final Integer i2 = counts.get(o2);

                return i2.compareTo(i1);
            }
        });

        final Map<Object, Integer> result = new HashMap<>();

        int otherCount = 0;
        for (int i = 0; i < keys.length; i++) {
            final Integer itemCount = counts.get(keys[i]);

            if (i <= numElements) {
                result.put(keys[i].toString(), itemCount);
            }
            else {
                otherCount += itemCount;
            }
        }
        result.put(MISC_NAME, otherCount);

        return result;
    }
}