package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 21.05.15
 * Copyright (c) vwd GmbH. All Rights Reserved.
 *
 * @author mloesch
 */
public class JsonUtil {

    public static String toJson(DashboardConfig config) {
        final JSONObject dashboard = new JSONObject();
        put(dashboard, "roles", config.getRoles()); // $NON-NLS$
        put(dashboard, "access", config.getAccess().toString()); // $NON-NLS$
        put(dashboard, "id", config.getId()); // $NON-NLS$
        put(dashboard, "name", config.getName()); // $NON-NLS$
        final ArrayList<SnippetConfiguration> snippetConfigs = config.getSnippetConfigs();
        final JSONArray jsonSnippets = new JSONArray();
        for (int i = 0, snippetConfigsSize = snippetConfigs.size(); i < snippetConfigsSize; i++) {
            final SnippetConfiguration snippetConfig = snippetConfigs.get(i);
            jsonSnippets.set(i, toJson(snippetConfig));
        }
        dashboard.put("snippets", jsonSnippets); // $NON-NLS$
        return dashboard.toString();
    }

    private static JSONObject toJson(SnippetConfiguration sc) {
        final HashMap<String, String> params = sc.getCopyOfParameters();
        final JSONObject result = new JSONObject();
        result.put("name", new JSONString(sc.getName())); // $NON-NLS$
        JSONObject jsonParams = new JSONObject();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            final String value = entry.getValue();
            if(value != null) {
                jsonParams.put(entry.getKey(), new JSONString(value));
            }
        }
        result.put("params", jsonParams); // $NON-NLS$
        return result;
    }

    public static DashboardConfig fromJson(String json) {
        if (!StringUtil.hasText(json)) {
            return null;
        }
        final DashboardConfig config = new DashboardConfig();
        final JSONValue parsed;
        try {
            parsed = JSONParser.parseStrict(json);
        }
        catch (JSONException e) {
            Firebug.error("could not parse json: '" + json + "'", e);
            Firebug.info("initializing empty dashboard");
            return new DashboardConfig();
        }
        final JSONObject parsedObject = parsed.isObject();

        //small compatibility layer for 1.30 beta version dashboards.
        final String role = getString(parsedObject, "role"); // $NON-NLS$
        if(role != null) {
            config.setRoles(Collections.singletonList(role));
        }
        else {
            config.setRoles(getStringList(parsedObject, "roles")); // $NON-NLS$
        }

        final String access = getString(parsedObject, "access"); // $NON-NLS$
        if (access != null) {
            config.setAccess(DashboardConfig.Access.valueOf(access));
        }
        config.setId(getString(parsedObject, "id")); // $NON-NLS$
        config.setName(getString(parsedObject, "name")); // $NON-NLS$

        final JSONArray snippets = parsedObject.get("snippets").isArray(); // $NON-NLS$
        for (int i = 0; i < snippets.size(); i++) {
            final JSONObject jsonSnippet = snippets.get(i).isObject();
            final SnippetConfiguration sc = new SnippetConfiguration(jsonSnippet.get("name").isString().stringValue()); // $NON-NLS$
            final JSONObject jsonParams = jsonSnippet.get("params").isObject(); // $NON-NLS$
            sc.setParameters(toMap(jsonParams));
            config.addSnippet(sc);
        }
        return config;
    }

    public static String getString(JSONObject jsonObject, String key) {
        final JSONValue jsonValue = jsonObject.get(key);
        if (jsonValue == null) {
            return null;
        }
        final JSONString jsonString = jsonValue.isString();
        return jsonString == null ? null : jsonString.stringValue();
    }

    public static List<String> getStringList(JSONObject jsonObject, String key) {
        final JSONValue jsonValue = jsonObject.get(key);
        if (jsonValue == null) {
            return null;
        }

        final JSONArray jsonArray = jsonValue.isArray();
        if(jsonArray == null) {
            return null;
        }
        if(jsonArray.size() == 1) {
            final JSONValue arrayJsonValue = jsonArray.get(0);
            if(arrayJsonValue == null || arrayJsonValue.isString() == null) {
                return null;
            }
            return Collections.singletonList(arrayJsonValue.isString().stringValue());
        }
        final ArrayList<String> stringList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            final JSONValue arrayJsonValue = jsonArray.get(0);
            if (arrayJsonValue == null || arrayJsonValue.isString() == null) {
                continue;
            }
            stringList.add(arrayJsonValue.isString().stringValue());
        }
        return stringList;
    }

    private static void put(JSONObject jsonObject, String key, String value) {
        jsonObject.put(key, value == null ? null : new JSONString(value));
    }

    private static void put(JSONObject jsonObject, String key, List<String> value) {
        if(value == null || value.isEmpty()) {
            jsonObject.put(key, null);
            return;
        }

        final JSONArray jsonArray = new JSONArray();
        for (String s : value) {
            jsonArray.set(jsonArray.size(), s == null ? null : new JSONString(s));
        }
        jsonObject.put(key, jsonArray);
    }

    private static HashMap<String, String> toMap(JSONObject json) {
        HashMap<String, String> map = new HashMap<>();
        if (json != null) {
            for (String key : json.keySet()) {
                final JSONValue jsonValue = json.get(key);
                if(jsonValue != null && jsonValue.isString() != null) {
                    map.put(key, jsonValue.isString().stringValue());
                }
            }
        }
        return map;
    }
}
