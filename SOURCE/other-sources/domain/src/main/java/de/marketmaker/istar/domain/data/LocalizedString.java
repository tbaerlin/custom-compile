/*
 * LocalizedString.java
 *
 * Created on 17.12.2010 13:07:55
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.marketmaker.istar.domain.Language;

/**
 * @author oflege
 */
public class LocalizedString implements Serializable {
    protected static final long serialVersionUID = 1L;

    public static final LocalizedString NULL_LOCALIZED_STRING = new LocalizedString(new Language[]{} , new String[]{});

    public static final Language DEFAULT_LANGUAGE = Language.de;

    private static final Language[] DEFAULT_LANGUAGE_ARRAY = new Language[]{DEFAULT_LANGUAGE};

    /**
     * the language that corresponds to the string in the respective positon
     */
    private final Language[] languages;

    private final String[] strings;

    private LocalizedString(Language[] languages, String[] strings) {
        this.languages = languages;
        this.strings = strings;
    }

    public static LocalizedString createDefault(String s) {
        if (s == null) {
            return null;
        }
        return new LocalizedString(DEFAULT_LANGUAGE_ARRAY, new String[]{s});
    }

    public static LocalizedString createDefault(String s, Language l, String[][] table) {
        final Builder builder = new Builder();
        builder.add(s, DEFAULT_LANGUAGE);
        for (final String[] strings : table) {
            if (strings[0].equals(s)) {
                builder.add(strings[1], l);
                break;
            }
        }
        return builder.build();
    }

    public LocalizedString add(Language language, String s) {
        if (isSupported(language)) {
            return this;
        }
        Language[] tmpLangs = Arrays.copyOf(this.languages, this.languages.length + 1);
        tmpLangs[tmpLangs.length - 1] = language;
        String[] tmpStrings = Arrays.copyOf(this.strings, this.strings.length + 1);
        tmpStrings[tmpStrings.length - 1] = s;
        return new LocalizedString(tmpLangs, tmpStrings);
    }

    public Language[] getLanguages() {
        return languages;
    }

    public String getLocalized(Language l) {
        for (int i = 0; i < this.languages.length; i++) {
            if (this.languages[i] == l) {
                return this.strings[i];
            }
        }
        return null;
    }

    public boolean isSupported(Language l) {
        for (final Language language : languages) {
            if (language == l) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalizedString that = (LocalizedString) o;
        return Arrays.equals(this.languages, that.languages)
                && Arrays.equals(this.strings, that.strings);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(languages);
        result = 31 * result + Arrays.hashCode(strings);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < this.languages.length; i++) {
            if (sb.length() > 1) {
                sb.append(",");
            }
            sb.append(this.languages[i]).append("=").append(this.strings[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    public String getDefault() {
        return getLocalized(DEFAULT_LANGUAGE);
    }

    public String getDe() {
        return getLocalized(Language.de);
    }

    public String getEn() {
        return getLocalized(Language.en);
    }

    public String getFr() {
        return getLocalized(Language.fr);
    }

    public String getIt() {
        return getLocalized(Language.it);
    }

    public String getNl() {
        return getLocalized(Language.nl);
    }

    /**
     * Similar to {@link de.marketmaker.istar.domainimpl.ItemWithNamesDp2#getNameOrDefault(de.marketmaker.istar.domain.Language)},
     * tries other languages if no value is defined for the requested language
     * @param lang requested language
     * @return value in lang, or, if none is defined, the value in EN or, if that is also undefined,
     * the value in the default languge (DE).
     */
    public String getValueOrDefault(Language lang) {
        String result = getLocalized(lang);
        if (result == null) {
            result = getEn();
        }
        return (result != null) ? result : getDefault();
    }

    public static class Builder {
        private static ConcurrentMap<String, Language[]> languagesCache
                = new ConcurrentHashMap<>();

        private final List<Language> languages;

        private final List<String> strings;

        public Builder() {
            this(null);
        }

        public Builder(LocalizedString ls) {
            if (ls != null) {
                this.languages = new ArrayList<>(Arrays.asList(ls.languages));
                this.strings = new ArrayList<>(Arrays.asList(ls.strings));
            }
            else {
                this.languages = new ArrayList<>();
                this.strings = new ArrayList<>();
            }
        }

        public void reset() {
            this.languages.clear();
            this.strings.clear();
        }

        public LocalizedString build() {
            if (this.languages.isEmpty()) {
                return null;
            }
            return new LocalizedString(buildLanguages(),
                    this.strings.toArray(new String[this.strings.size()]));
        }

        private Language[] buildLanguages() {
            final Language[] tmp = this.languages.toArray(new Language[this.languages.size()]);
            final String key = Arrays.toString(tmp);
            final Language[] cached = languagesCache.putIfAbsent(key, tmp);
            return (cached != null) ? cached : tmp;
        }

        public void add(String string, Language... languages) {
            if (languages == null || languages.length == 0) {
                throw new IllegalArgumentException("no languages for string " + string);
            }
            for (final Language language : languages) {
                this.languages.add(language);
                this.strings.add(string);
            }
        }
    }
}
