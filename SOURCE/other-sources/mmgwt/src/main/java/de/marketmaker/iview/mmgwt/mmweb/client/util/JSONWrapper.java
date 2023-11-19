/*
 * JSONWrapper.java
 *
 * Created on 13.01.2009 15:11:23
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class JSONWrapper {
    private JSONValue value = null;

    /**
     * Creates a <code>JSONWrapper</code> object from the supplied <code>JSONValue</code> argument.
     *
     * @param value A <code>JSONValue</code> value.
     */
    public JSONWrapper(JSONValue value) {
        this.value = value;
    }
    /**
     * Return the wrapped <code>JSONValue</code> value.
     *
     * @return The wrapped <code>JSONValue</code>.
     */
    public JSONValue getValue() {
        return value;
    }

    /**
     * Constant value that represents an invalid <code>JSONValue</code> value.
     * We reuse this constant to avoid chewing
     * up memory. It will return a <code>null</code> as its wrapped value.
     */
    public final static JSONWrapper INVALID = new JSONWrapper(null);

    @Override
    public String toString() {
        return INVALID == this ? "INVALID" : String.valueOf(this.value); // $NON-NLS-0$
    }

    /**
     * Get the index-th item in the array.
     *
     * @param index non-negative integer
     * @return A <code>JSONWrapper</code> wrapped <code>JSONValue</code>, <code>INVALID</code> if the
     * wrapped value is not an array or if the index is out of bounds.
     * @see #INVALID
     */
    public JSONWrapper get(int index) {
        if (index < 0) {
            return INVALID;
        }
        if (value == null) {
            return INVALID;
        }

        JSONArray arr = value.isArray();
        if (arr == null) {
            return INVALID;
        }
        if (index >= arr.size()) {
            return INVALID;
        }
        JSONValue retval = arr.get(index);
        if (retval == null) {
            return INVALID;
        }

        final JSONValue resolved = resolve(retval);
        return new JSONWrapper(resolved == null ? retval : resolved);
    }

    /**
     * Get the size of the array.
     *
     * @return A non-negative integer representing the size of the array. Zero if the array has no elements
     * or is not a <code>JSONArray</code>.
     */
    public int size() {
        if (value == null) {
            return 0;
        }

        JSONArray arr = value.isArray();
        if (arr == null) {
            return 0;
        }

        return arr.size();
    }

    /*
     * Get the item that corresponds to the <code>key</code> in the map.
     *
     * @param key a string
     * @return A <code>JSONWrapper</code> wrapped <code>JSONValue</code>, <code>INVALID</code> if the
     * wrapped value is not an <code>JSONObject</code> or if the <code>key</code> doesn't have
     * a corresponding value.
     * @see #INVALID
     */
/*
    public JSONWrapper get(String key) {
        if (value == null) {
            return INVALID;
        }

        JSONObject obj = value.isObject();
        if (obj == null) {
            return INVALID;
        }
        JSONValue retval = obj.get(key);
        if (retval == null) {
            return INVALID;
        }

        return new JSONWrapper(retval);
    }
*/
    
    public JSONWrapper get(String path) {
        if (path == null || path.isEmpty() || this == INVALID) {
            return INVALID;
        }
        JSONValue jsonValue = getByPath(splitPath(path));
        return jsonValue == null ? INVALID : new JSONWrapper(jsonValue);
    }

    private String[] splitPath(String path) {
        final ArrayList<String> listElements = new ArrayList<String>();
        int startIndex = 0;
        for (int i = 0, length = path.length(); i < length; i++) {
            final char c = path.charAt(i);
            switch (c) {
                case '.':
                    if (i == 0) {
                        throw new IllegalArgumentException("invalid JSON path (starts with '.'): " + path); // $NON-NLS$
                    }
                    if (startIndex < i) {
                        listElements.add(path.substring(startIndex, i));
                    }
                    startIndex = i + 1;
                    break;
                case '[':
                    if (i == 0) {
                        throw new IllegalArgumentException("invalid JSON path (starts with '['): " + path); // $NON-NLS$
                    }
                    if (startIndex < i) {
                        listElements.add(path.substring(startIndex, i));
                    }
                    startIndex = i;
                    break;
                case ']':
                    if (path.charAt(startIndex) != '[') {
                        throw new IllegalArgumentException("invalid JSON path (']' without '['): " + path); // $NON-NLS$
                    }
                    listElements.add(path.substring(startIndex, i + 1));
                    startIndex = i + 1;
                    break;
                default:
                    break;
            }
        }
        if (startIndex < path.length()) {
            if (path.charAt(startIndex) == '[' && path.charAt(path.length() - 1) != ']') {
                throw new IllegalArgumentException("invalid JSON path ('[' without ']'): " + path); // $NON-NLS$
            }
            listElements.add(path.substring(startIndex));
        }
        return listElements.toArray(new String[listElements.size()]);
    }
    
    private JSONValue resolveChild(JSONObject jsonObjectParent, String key) {
        final JSONValue jsonValue = jsonObjectParent.get(key);
        if (jsonValue == null) {
            return null;
        }
        final JSONValue jsonValueResolved = resolve(jsonValue);
        if (jsonValueResolved == null) {
            return jsonValue;
        }
        jsonObjectParent.put(key, jsonValueResolved);
        return jsonValueResolved;
    }
    
    private JSONValue resolveChild(JSONArray jsonArrayParent, int index) {
        final JSONValue jsonValue = jsonArrayParent.get(index);
        if (jsonValue == null) {
            return null;
        }
        final JSONValue jsonValueResolved = resolve(jsonValue);
        if (jsonValueResolved == null) {
            return jsonValue;
        }
        jsonArrayParent.set(index, jsonValueResolved);
        return jsonValueResolved;
    }
    
    private JSONValue resolve(JSONValue jsonValue) {
        final JSONObject jsonObject = jsonValue.isObject();
        if (jsonObject == null) {
            return null;
        }
        final JSONValue jsonValueUsePath = jsonObject.get("@use"); // $NON-NLS$
        if (jsonValueUsePath == null) {
            return null;
        }
        final String path = jsonValueUsePath.isString().stringValue();
        jsonObject.put("@use", null); // $NON-NLS$
        final JSONValue jsonValueResolved = SessionData.INSTANCE.getGuiDef(path).getValue();
        if (jsonValueResolved == null) {
            throw new IllegalArgumentException("invalid JSON path (@use): " + path); // $NON-NLS$
        }
        if (jsonObject.size() == 0) {
            return jsonValueResolved;
        }
        final JSONObject jsonObjectResolved = jsonValueResolved.isObject();
        if (jsonObjectResolved == null) {
            throw new IllegalArgumentException("invalid JSON path (@use) - not a JSONObject: " + path); // $NON-NLS$
        }
        final JSONValue jsonValueRemove = jsonObject.get("@remove"); // $NON-NLS$
        final Set<String> setRemoveKeys = new HashSet<String>();
        if (jsonValueRemove != null) {
            final String[] removeArray = jsonValueRemove.isString().stringValue().split(",");
            setRemoveKeys.addAll(Arrays.asList(removeArray));
        }
        for (String key : jsonObjectResolved.keySet()) {
            if (!jsonObject.containsKey(key) && !setRemoveKeys.contains(key)) {
                jsonObject.put(key, jsonObjectResolved.get(key));
            }
        }
        return jsonObject;
    }

    public JSONValue getByPath(String[] pathElements) {
        JSONValue v = getValue();
        for (String pathElement : pathElements) {
            if (v == null) {
                return null;
            }
            if (pathElement.matches("\\[.*\\]")) {
                final JSONArray jsonArray = v.isArray();
                if (jsonArray == null) {
                    return null;
                }
                final String sIndex = pathElement.substring(1, pathElement.length() - 1);
                final int index;
                if (sIndex.matches("\\d+")) { // $NON-NLS$
                    index = Integer.parseInt(sIndex);
                }
                else if (sIndex.matches("\\w+:.+")) { // $NON-NLS$
                    index = getArrayIndexByFieldContent(jsonArray, sIndex);
                    if (index == -1) {
                        throw new IllegalArgumentException("invalid JSON path element (index not found): " + pathElement); // $NON-NLS$
                    }
                }
                else {
                    throw new IllegalArgumentException("invalid JSON path element (does not match regex): " + pathElement); // $NON-NLS$
                }
                v = resolveChild(jsonArray, index);
            }            
            else {
                // object field
                final JSONObject jsonObject = v.isObject();
                if (jsonObject == null) {
                    return null;
                }
                v = resolveChild(jsonObject, pathElement);
            }
        }
        return v;
    }

    private int getArrayIndexByFieldContent(JSONArray jsonArray, String keyValuePair) {
        final int colonIndex = keyValuePair.indexOf(':');
        if (colonIndex == -1) {
            return -1;
        }
        final String key = keyValuePair.substring(0, colonIndex);
        final String value = keyValuePair.substring(colonIndex + 1);
        for (int i = 0, size = jsonArray.size(); i < size; i++) {
            final JSONObject jsonObject = jsonArray.get(i).isObject();
            if (jsonObject == null) {
                continue;
            }
            if (jsonObject.containsKey("ifFeatureFlag")) { // $NON-NLS$
                final String ifFeatureFlag = jsonObject.get("ifFeatureFlag").isString().stringValue(); // $NON-NLS$
                if(!FeatureFlags.isEnabled(ifFeatureFlag)) {
                    continue;
                }
            }
            if (jsonObject.containsKey("ifSelector")) { // $NON-NLS$
                final String ifSelector = jsonObject.get("ifSelector").isString().stringValue(); // $NON-NLS$

                if(ifSelector.startsWith("!")) {  // $NON-NLS$
                    if(Selector.valueOf(ifSelector.substring(1)).isAllowed()) {
                        continue;
                    }
                }
                else {
                    if(!Selector.valueOf(ifSelector).isAllowed()) {
                        continue;
                    }
                }
            }
            if (value.equals(jsonObject.get(key).isString().stringValue())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return the <code>Set</code> of strings that make up the key set.
     *
     * @return The set of string keys of the <code>JSONObject</code> map.
     * @see JSONObject
     */
    public Set<String> keySet() {
        if (value == null) {
            return null;
        }

        JSONObject obj = value.isObject();
        if (obj == null) {
            return null;
        }
        return obj.keySet();
    }

    /**
     * Uses the <code>equals</code> method of the underlying <code>JSONValue</code> object.
     *
     * If the wrapped value is <code>null</code>, it is equal to other wrapped <code>null</code>'s.
     *
     * @return <code>true</code> if equal, <code>false</code> if not.
     */
    public boolean equals(Object o) {
        if ((o == null) || (!(o instanceof JSONWrapper))) {
            return false;
        }
        JSONWrapper other = (JSONWrapper)o;
        if (value == null) {
            return other.value == null;
        }
        else {
            return other.value != null && value.equals(other.value);
        }
    }

    /**
     * Uses the <code>hashcode</code> method of the underlying <code>JSONValue</code> object, or of the
     * <code>Object</code> if that value is <code>null</code>.
     *
     * @return an integer hash code
     */
    public int hashCode() {
        if (value == null) {
            return super.hashCode();
        } else {
            return value.hashCode();
        }
    }


    /**
     * Is the wrapped <code>JSONValue</code> a <code>JSONNull</code>?
     *
     * @return <code>true</code> if the underlying value is a <code>JSONNull</code>, <code>false</code>
     * otherwise.
     */
    public boolean isNull() {
        if (value == null) {
            return false;
        }
        return (value.isNull() != null);
    }

    /**
     * Return the string value of the wrapped <code>JSONValue</code>.
     *
     * @return A <code>String</code> object if the wrapped value is a  <code>JSONString</code>,
     * <code>null</code> otherwise.
     */
    public String stringValue() {
        if (value == null) {
            return null;
        }
        JSONString str = value.isString();
        if (str == null) {
            return null;
        }
        return str.stringValue();
    }

    /**
     * Convenience method, that combines get(key) and stringValue().
     * @param key a String
     * @param fallback to return if get(key) is undefined
     * @return The result of get(key).stringValue(), or, if that would return null, fallback
     */
    public String getString(String key, String fallback) {
        String value = getString(key);
        return (value != null) ? value : fallback;
    }

    /**
     * Convenience method, that combines get(key) and stringValue().
     *
     * @param key a String
     * @return The result of get(key).stringValue()
     */
    public String getString(String key) {
        return get(key).stringValue();
    }

    public boolean booleanValue() {
        if (value == null) {
            return false;
        }
        JSONBoolean b = value.isBoolean();
        if (b != null) {
            return b.booleanValue();
        }
        return "true".equals(stringValue()); // $NON-NLS$
    }

    public int intValue(int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        JSONNumber b = value.isNumber();
        if (b != null) {
            return (int) b.doubleValue();
        }
        JSONString s = value.isString();
        if (s != null) {
            return Integer.parseInt(s.stringValue());
        }
        return defaultValue;
    }

    /**
     * Return the double value of the wrapped <code>JSONValue</code>.
     *
     * @return A <code>Double</code> object if the wrapped value is a  <code>JSONNumber</code>,
     * <code>null</code> otherwise.
     */
    public Double numberValue() {
        if (value == null) {
            return null;
        }
        JSONNumber num = value.isNumber();
        if (num == null) {
            return null;
        }
        return num.doubleValue();
    }

    /**
     * Is the wrapped <code>JSONValue</code> a <code>JSONArray</code>?
     *
     * @return <code>true</code> if the underlying value is a <code>JSONArray</code>, <code>false</code>
     * otherwise.
     */
    public boolean isArray() {
        if (value == null) {
            return false;
        }
        return (value.isArray() != null);
    }

    /**
     * Is the wrapped <code>JSONValue</code> a <code>JSONObject</code>?
     *
     * @return <code>true</code> if the underlying value is a <code>JSONObject</code>, <code>false</code>
     * otherwise.
     */
    public boolean isObject() {
        if (value == null) {
            return false;
        }
        return (value.isObject() != null);
    }

    /**
     * Is the wrapped <code>JSONValue</code> a <code>JSONString</code>?
     *
     * @return <code>true</code> if the underlying value is a <code>JSONString</code>, <code>false</code>
     * otherwise.
     */
    public boolean isString() {
        if (value == null) {
            return false;
        }
        return (value.isString() != null);
    }

    /**
     * Is the wrapped <code>JSONValue</code> a <code>JSONNumber</code>?
     *
     * @return <code>true</code> if the underlying value is a <code>JSONNumber</code>, <code>false</code>
     * otherwise.
     */
    public boolean isNumber() {
        if (value == null) {
            return false;
        }
        return (value.isNumber() != null);
    }

    /**
     * Is the wrapped <code>JSONValue</code> a <code>JSONBoolean</code>?
     *
     * @return <code>true</code> if the underlying value is a <code>JSONBoolean</code>, <code>false</code>
     * otherwise.
     */
    public boolean isBoolean() {
        if (value == null) {
            return false;
        }
        return (value.isBoolean() != null);
    }

    /**
     * Is the wrapped <code>JSONValue</code> a valid <code>JSONValue</code> or <code>null</code>?
     *
     * @return <code>true</code> if the underlying value is a <code>JSONValue</code>, <code>false</code>
     * otherwise.
     */
    public boolean isValid() {
        return (value != null);
    }
}
