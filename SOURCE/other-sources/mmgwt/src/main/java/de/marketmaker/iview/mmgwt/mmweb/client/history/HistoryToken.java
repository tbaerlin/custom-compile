package de.marketmaker.iview.mmgwt.mmweb.client.history;

import com.google.gwt.user.client.History;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: umaurer
 * Date: 02.12.13
 * Time: 15:16
 */
public class HistoryToken {

    private static final String HISTORY_ID_KEY = "hid"; // $NON-NLS$

    public static class Builder {
        private final String[] allParams;
        private final List<String> indexedParams;
        private final Map<String, String> namedParams;
        private Integer historyId;

        private Builder(String token) {
            this(StringUtil.splitToken(token), new ArrayList<String>(), new HashMap<String, String>());

            for (String param : this.allParams) {
                final int pos = param.indexOf('=');
                if (pos == 0) {
                    throw new IllegalStateException("illegal history token parameter: " + param); // $NON-NLS$
                }
                else if (pos < 0) {
                    this.indexedParams.add(param);
                }
                else {
                    final String key = param.substring(0, pos);
                    final String value = param.substring(pos + 1);
                    if (HISTORY_ID_KEY.equals(key)) {
                        this.historyId = Integer.valueOf(value);
                    }
                    else {
                        this.namedParams.put(key, value);
                    }
                }
            }
        }

        private Builder(String[] allParams, List<String> indexedParams, Map<String, String> namedParams) {
            this.allParams = allParams;
            this.indexedParams = indexedParams;
            this.namedParams = namedParams;
        }

        public static Builder fromCurrent() {
            return new Builder(History.getToken()).withHistoryId(null);
        }

        public static Builder fromToken(String token) {
            return new Builder(token);
        }

        public static Builder fromHistoryToken(HistoryToken historyToken) {
            return new Builder(historyToken.allParams, historyToken.getIndexedParams(), historyToken.getNamedParams());
        }

        public static Builder create(String controllerId, String... params) {
            final ArrayList<String> indexedParams = new ArrayList<>(params.length + 1);
            indexedParams.add(controllerId);
            Collections.addAll(indexedParams, params);
            return new Builder(null, indexedParams, new HashMap<String, String>());
        }

        public Builder with(String key, String value) {
            this.namedParams.put(key, value);
            return this;
        }

        public String remove(String key) {
            return this.namedParams.remove(key);
        }

        public Builder without(String key) {
            remove(key);
            return this;
        }

        public String get(String key) {
            return this.namedParams.get(key);
        }

        public Builder with(int index, String value) {
            if (this.indexedParams.size() > index) {
                this.indexedParams.set(index, value);
            }
            else if (this.indexedParams.size() == index) {
                this.indexedParams.add(value);
            }
            else {
                throw new IndexOutOfBoundsException("HistoryToken.Builder.withParam(" + index + ", " + value + ") -> illegal index"); // $NON-NLS$
            }
            return this;
        }

        public Builder withHistoryId(Integer historyId) {
            this.historyId = historyId;
            return this;
        }

        public Builder add(String value) {
            this.indexedParams.add(value);
            return this;
        }

        public HistoryToken build() {
            return new HistoryToken(this.allParams, new ArrayList<>(this.indexedParams), new HashMap<>(this.namedParams), this.historyId);
        }

        public static HistoryToken.Builder withPrefix(String[] allParams, List<String> indexedParams, Map<String, String> namedParams, String... prefix) {
            final String[] _allParams;
            if (allParams == null) {
                _allParams = null;
            }
            else {
                _allParams = new String[prefix.length + allParams.length];
                System.arraycopy(prefix, 0, _allParams, 0, prefix.length);
                System.arraycopy(allParams, 0, _allParams, prefix.length, allParams.length);
            }
            final ArrayList<String> _indexedParams = new ArrayList<>(prefix.length + indexedParams.size());
            _indexedParams.addAll(Arrays.asList(prefix));
            _indexedParams.addAll(indexedParams);
            return new HistoryToken.Builder(_allParams, _indexedParams, new HashMap<>(namedParams));
        }

        public PlaceChangeEvent buildEvent() {
            return new PlaceChangeEvent(build());
        }

        public void fire() {
            build().fire();
        }

        public void fire(HistoryContext context) {
            build().fire(context);
        }

        public String toString() {
            return build().toString();
        }

    }

    public static Builder builder(String controllerId, String... params) {
        return Builder.create(controllerId, params);
    }

    public static HistoryToken build(String controllerId, String... params) {
        return Builder.create(controllerId, params).build();
    }

    public static HistoryToken fromToken(String tokens) {
        return Builder.fromToken(tokens).build();
    }

    public static HistoryToken buildKeyValue(String key, String value) {
        return new Builder(null).with(key, value).build();
    }

    public static HistoryToken current() {
        return HistoryToken.Builder.fromCurrent().build();
    }

    private final String[] allParams;
    private final List<String> indexedParams;
    private final Map<String, String> namedParams;
    private final Integer historyId;

    private HistoryToken(String[] allParams, List<String> indexedParams, Map<String, String> namedParams, Integer historyId) {
        this.allParams = allParams;
        this.indexedParams = indexedParams;
        this.namedParams = namedParams;
        this.historyId = historyId;
    }

    public String get(int index) {
        return this.indexedParams.get(index);
    }

    public String get(int index, String defaultValue) {
        return this.indexedParams.size() > index ? this.indexedParams.get(index) : defaultValue;
    }

    public List<String> getIndexedParams() {
        return new ArrayList<>(this.indexedParams);
    }

    public String getFromAll(int index, String defaultValue) {
        if (this.allParams == null) {
            return get(index, defaultValue);
        }
        return this.allParams.length <= index ? defaultValue : this.allParams[index];
    }

    public String get(String key) {
        return this.namedParams.get(key);
    }

    public String get(String key, String defaultValue) {
        final String value = this.namedParams.get(key);
        return value != null ? value : defaultValue;
    }

    public boolean containsKey(String key) {
        return this.namedParams.containsKey(key);
    }

    public HashMap<String, String> getNamedParams() {
        return new HashMap<>(this.namedParams);
    }

    public String getByNameOrIndex(String key, int index) {
        return getByNameOrIndex(key, index, null);
    }

    public String getByNameOrIndex(String key, int index, String defaultValue) {
        final String namedValue = this.namedParams.get(key);
        if (namedValue != null) {
            return namedValue;
        }
        return get(index, defaultValue);
    }

    public String getByNameOrIndexFromAll(String key, int index, String defaultValue) {
        final String namedValue = this.namedParams.get(key);
        if (namedValue != null) {
            return namedValue;
        }
        return getFromAll(index, defaultValue);
    }

    public int getIndexedParamCount() {
        return this.indexedParams.size();
    }

    public int getAllParamCount() {
        return this.indexedParams.size() + this.namedParams.size();
    }

    public String getControllerId() {
        return get(0, null);
    }

    public String toString() {
        return toString(false);
    }

    public String toStringWithHid() {
        return toString(SessionData.isAsDesign());
    }

    private String toString(boolean withHistoryId) {
        final StringBuilder sb = new StringBuilder();
        for (String param : this.indexedParams) {
            StringUtil.appendToken(sb, param);
        }
        for (Map.Entry<String, String> entry : this.namedParams.entrySet()) {
            StringUtil.appendToken(sb, entry.getKey() + "=" + entry.getValue());
        }
        if (withHistoryId && this.historyId != null) {
            StringUtil.appendToken(sb, HISTORY_ID_KEY + "=" + this.historyId);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HistoryToken && equals((HistoryToken) obj)
                || obj instanceof String && equalsIgnoreHid((String) obj);
    }

    public boolean equals(HistoryToken other) {
        return this.indexedParams.equals(other.indexedParams) && this.namedParams.equals(other.namedParams);
    }

    private boolean equalsIgnoreHid(String other) {
        final String[] params = StringUtil.splitToken(other);
        int indexCount = 0;
        int nameCount = 0;
        for (String param : params) {
            final int pos = param.indexOf('=');
            if (pos == 0) {
                return false;
            }
            else if (pos < 0) {
                // indexed parameter
                indexCount++;
                if (!param.equals(get(indexCount, null))) {
                    return false;
                }
            }
            else if (!param.startsWith(HISTORY_ID_KEY + "=")) {
                // named parameter
                nameCount++;
                final String key = param.substring(0, pos);
                final String value = param.substring(pos + 1);
                if (!value.equals(get(key))) {
                    return false;
                }
            }
        }
        return indexCount == this.indexedParams.size() && nameCount == this.namedParams.size();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public void fire() {
        PlaceUtil.fire(new PlaceChangeEvent(this));
    }

    public void fire(HistoryContext context) {
        PlaceUtil.fire(new PlaceChangeEvent(this, context));
    }

    public Builder toBuilder() {
        return Builder.fromHistoryToken(this);
    }

    public Builder with(String key, String value) {
        return toBuilder().with(key, value);
    }

    public HistoryToken.Builder withPrefix(String... prefix) {
        return Builder.withPrefix(this.allParams, this.indexedParams, this.namedParams, prefix);
    }

    public Integer getHistoryId() {
        return this.historyId;
    }
}
