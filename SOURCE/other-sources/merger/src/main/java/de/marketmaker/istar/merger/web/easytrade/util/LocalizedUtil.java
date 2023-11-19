/*
 * LocalizedUtil.java
 *
 * Created on 06.01.2011 13:37:29
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.merger.context.RequestContextHolder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LocalizedUtil {
    private static final Logger logger = LoggerFactory.getLogger(LocalizedUtil.class);

    private LocalizedUtil() {
    }

    public static Language getLanguage(List<Language> availableLanguages, List<Language> prioritizedTargetLanguages) {
        final HashSet<Language> available = new HashSet<>(availableLanguages);
        for (final Language targetLanguage : prioritizedTargetLanguages) {
            if (available.contains(targetLanguage)) {
                return targetLanguage;
            }
        }
        return null;
    }

    public static Language getLanguage(HashSet<Language> availableLanguages, List<Locale> prioritizedTargetLanguageLocales) {
        for(final Locale targetLanguageLocale : prioritizedTargetLanguageLocales) {
            final Language targetLanguage = Language.valueOf(targetLanguageLocale.getLanguage());
            if (availableLanguages.contains(targetLanguage)) {
                return targetLanguage;
            }
        }
        return null;
    }

    public static List<Language> getLanguages(List<Locale> locales) {
        final List<Language> result = new ArrayList<>(locales.size());
        for (final Locale locale : locales) {
            result.add(Language.valueOf(locale));
        }
        return result;
    }

    public static Language getAsLanguage(Object... data) {
        final List<Locale> locales = RequestContextHolder.getRequestContext().getLocales();

        for (final Locale locale : locales) {
            final Language language = Language.valueOf(locale);
            if (isSupported(language, data)) {
                return language;
            }
        }
        return Language.valueOf(locales.get(0));
    }

    public static String getLanguage(Object... data) {
        return getAsLanguage(data).name();
    }

    private static boolean isSupported(Language language, Object[] data) {
        for (final Object o : data) {
            if (o instanceof Collection) {
                final Collection items = (Collection) o;
                for (final Object item : items) {
                    if (isSupported(language, item)) {
                        return true;
                    }
                }
            }
            else {
                if (isSupported(language, o)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSupported(Language language, Object o) {
        for (Method method : findMethods(o)) {
            if (isSupported(language, o, method)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSupported(Language language, Object o, Method method) {
        try {
            final LocalizedString ls = (LocalizedString) method.invoke(o);
            if (ls != null && ls.isSupported(language)) {
                return true;
            }
        } catch (Exception e) {
            logger.error("<isSupported> failed", e);
        }
        return false;
    }

    private static List<Method> findMethods(Object o) {
        final ArrayList<Method> result = new ArrayList<>();
        for (final Method method : o.getClass().getMethods()) {
            if (method.getReturnType().isAssignableFrom(LocalizedString.class)) {
                result.add(method);
            }
        }
        return result;
    }
}
