/*
 * AbstractFinderMetadata.java
 *
 * Created on 31.07.2009 12:13:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.common.spring.MessageSourceFactory;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.web.easytrade.ProviderSelectionCommand;
import de.marketmaker.istar.ratios.RatioFieldDescription;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
abstract class AbstractFinderMetadata extends EasytradeCommandController {

    public static class Command implements ProviderSelectionCommand {

        private String providerPreference;

        @RestrictedSet("VWD,SMF,SEDEX")
        public String getProviderPreference() {
            return providerPreference;
        }

        public void setProviderPreference(String providerPreference) {
            this.providerPreference = providerPreference;
        }
    }

    private static final MessageSource MESSAGES
            = MessageSourceFactory.create(InstrumentTypeEnum.class);

    private final RatioDataRecord.Field[] fields;

    protected RatiosProvider ratiosProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    private final String template;

    private final InstrumentTypeEnum type;

    protected AbstractFinderMetadata(InstrumentTypeEnum type,
            RatioDataRecord.Field... fields) {
        this(Command.class, type, fields);
    }

    protected AbstractFinderMetadata(Class cmdClass, InstrumentTypeEnum type,
            RatioDataRecord.Field... fields) {
        this(cmdClass, type, type.name().toLowerCase() + "findermetadata", fields);
    }

    protected AbstractFinderMetadata(Class cmdClass, InstrumentTypeEnum type, String template,
            RatioDataRecord.Field... fields) {
        super(cmdClass);
        this.type = type;
        this.template = template;
        this.fields = fields;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final ProviderSelectionCommand cmd = (ProviderSelectionCommand) o;

        final FinderMetadataMethod metadataMethod = new FinderMetadataMethod(this.type,
                this.ratiosProvider, this.instrumentProvider, this.fields,
                cmd.getProviderPreference(), getQuery(o));
        afterMetaDataMethodConstructed(metadataMethod, o);

        final Map<String, Object> model = metadataMethod.invoke();

        onDoHandle(o, model);

        return new ModelAndView(this.template, model);
    }

    protected void afterMetaDataMethodConstructed(FinderMetadataMethod metadataMethod, Object cmd) {
    }

    protected void onDoHandle(Object o, Map<String, Object> model) {
    }

    protected String getQuery(Object o) {
        return null;
    }

    protected List<String> getKeyList(Map<String, Map<String, Integer>> map, String parameter) {
        final Map<String, Integer> keys = map.get(parameter);
        return keys != null ? new ArrayList<>(keys.keySet())
                : Collections.<String>emptyList();
    }

    static String localize(final String msg) {
        try {
            return MESSAGES.getMessage(msg, null,
                    RequestContextHolder.getRequestContext().getLocale());
        } catch (NoSuchMessageException e) {
            return msg;
        }
    }

    static String localize(final String msg, String fallback) {
        try {
            return MESSAGES.getMessage(msg, null, fallback,
                    RequestContextHolder.getRequestContext().getLocale());
        } catch (NoSuchMessageException e) {
            return fallback;
        }
    }

    private Map<String, String> localize(Collection<String> types) {
        Map<String, String> result = new TreeMap<>();
        for (String s : types) {
            result.put(localize(s), s);
        }
        return result;
    }

    /**
     * If model contains a collection of Strings as value for key, that collection will be replaced
     * by a map, in which the values correspond to the original collection and the keys are
     * localized versions of the values (if a localization for a key cannot be found, the key
     * will be equal to the value).
     * @param model
     * @param key
     */
    protected void localize(Map<String, Object> model, final String key) {
        @SuppressWarnings({"unchecked"}) Collection<String> types
                = (Collection<String>) model.get(key);
        if (types != null) {
            model.put(key, localize(types));
        }
    }

    public static ArrayList<String> getItems(
            Map<Integer, Map<Integer, Map<String, Integer>>> metadata,
            RatioFieldDescription.Field field) {
        final Map<String, Integer> items = metadata.get(field.id()).get(getItemKey(field));
        return (items != null) ? new ArrayList<>(items.keySet())
                : new ArrayList<String>();
    }

    private static int getItemKey(RatioFieldDescription.Field field) {
        if (!field.isLocalized()) {
            return -1;
        }
        final Locale requested = RequestContextHolder.getRequestContext().getLocale();
        for (int i = 0; i < field.getLocales().length; i++) {
            if (field.getLocales()[i].equals(requested)) {
                return i;
            }
        }
        return -2;
    }
}
