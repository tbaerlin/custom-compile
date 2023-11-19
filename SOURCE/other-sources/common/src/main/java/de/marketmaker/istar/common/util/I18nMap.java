package de.marketmaker.istar.common.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This map gives access to a GWT I18n.properties file.
 * It can handle parameters and amount specifications (none, one, two, few, many).
 *
 * @author umaurer
 */
public class I18nMap {
    private final Map<String, String> map = new HashMap<>();
    private final Set<String> invalidKeys = new HashSet<>();

    enum State {
        STD, VAR
    }

    public String put(String key, String value) {
        return this.map.put(key, value);
    }

    public String get(String key) {
        final String value = unescapeJavaMessageFormat(this.map.get(key));
        if (value == null) {
            this.invalidKeys.add(key);
            return "@i18n error: " + key + "@";
        }
        return value;
    }

    private String unescapeJavaMessageFormat(String s) {
        //actually this is a workaround, because if we would use MessageFormat as intended by GWT,
        //we would not need all these self-parsing done in this class and double single quotes
        //would be unescaped automatically @link{http://docs.oracle.com/javase/6/docs/api/java/text/MessageFormat.html}
        if(s == null) return s;
        return s.replaceAll("''", "'");
    }

    public String getWithParams(String key, Object... params) {
        final String s = get(key);
        return resolveParams(key, s, params);
    }

    public String getN(String key, int n) {
        String value = null;
        if (n <= 2) {
            if (n == 0) {
                value = this.map.get(key + "[none]");
            }
            else if (n == 1) {
                value = this.map.get(key + "[one]");
            }
            else { // n == 2
                value = this.map.get(key + "[two]");
            }
        }
        else if (n <= 10) {
            value = this.map.get(key + "[few]");
        }
        else if (n <= 99) {
            value = this.map.get(key + "[many]");
        }

        if (value == null) {
            value = resolveParams(key, get(key), getParamsWithN(String.valueOf(n)));
        }
        return value;
    }

    public String getN(String key, double n, String formatted) {
        String value = null;
        if (n <= 2) {
            if (n == 0) {
                value = this.map.get(key + "[none]");
            }
            else if (n == 1) {
                value = this.map.get(key + "[one]");
            }
            else { // n == 2
                value = this.map.get(key + "[two]");
            }
        }
        else if (n <= 10) {
            value = this.map.get(key + "[few]");
        }
        else if (n <= 99) {
            value = this.map.get(key + "[many]");
        }

        if (value == null) {
            value = resolveParams(key, get(key), getParamsWithN(formatted));
        }

        return value;
    }

    public String getNWithParams(String key, int n, Object... params) {
        final String s = getN(key, n);
        return resolveParams(key, s, getParamsWithN(String.valueOf(n), params));
    }

    private Object[] getParamsWithN(String n, Object... params) {
        final Object[] result = new Object[params.length + 1];
        result[0] = n;
        System.arraycopy(params, 0, result, 1, params.length);
        return result;
    }

    private String resolveParams(String key, String value, Object[] params) {
        final StringBuilder sbResult = new StringBuilder();
        final StringBuilder sbNumber = new StringBuilder();
        State state = State.STD;
        for (char c: value.toCharArray()) {
            if (state == State.STD) {
                if (c == '{') {
                    state = State.VAR;
                }
                else {
                    sbResult.append(c);
                }
            }
            else {
                if (c == '}') {
                    appendParam(sbResult, key, sbNumber, params);
                    state = State.STD;
                }
                else {
                    sbNumber.append(c);
                }
            }
        }

        if (state == State.VAR) {
            appendParam(sbResult, key, sbNumber, params);
        }
        return sbResult.toString();
    }

    private void appendParam(StringBuilder sbResult, String key, StringBuilder sbNumber, Object[] params) {
        final int paramId = Integer.parseInt(sbNumber.toString().replaceAll("(\\d*)\\,.*", "$1"));
        if (paramId >= params.length) {
            final StringBuilder sb = new StringBuilder();
            sb.append("index ").append(paramId).append(" not found in getWithParams(").append(key);
            for (Object param : params) {
                sb.append(", ").append(param);
            }
            sb.append(")");
            throw new ArrayIndexOutOfBoundsException(sb.toString());
        }
        sbResult.append(params[paramId]);
    }

    public Set<String> getInvalidKeys() {
        return new TreeSet<>(this.invalidKeys);
    }
}
