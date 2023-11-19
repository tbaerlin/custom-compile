/*
 * OrderEntryHistorySupport.java
 *
 * Created on 26.02.13 15:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.history;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Markus Dick
 */
public class OrderEntryHistorySupport {
    private static final int MAX = 10;
    private static final String APP_CONFIG_PROPERTY = "OrderEntry.history"; //$NON-NLS$

    private final LinkedList<Item> items = new LinkedList<Item>();

    private final ArrayList<ProcessStep> processSteps = new ArrayList<ProcessStep>();
    private int nextProcessStep = 0;
    private OrderEntryHistorySupport.Item pendingHistoryItem = null;

    public void processPendingHistoryItem(Item item) {
        this.pendingHistoryItem = item;
        this.nextProcessStep = 0;
        processPendingHistoryItem();
    }

    public void processPendingHistoryItem() {
        Firebug.debug("processPendingHistoryItem nextProcessStep="+ this.nextProcessStep + " processSteps.size=" + this.processSteps.size());

        if(!hasPendingHistoryItem()) {
            Firebug.debug("no pending history item");
            return;
        }

        if(!(this.nextProcessStep < this.processSteps.size())) {
            Firebug.debug("process steps finished.");

            this.pendingHistoryItem = null;
            this.nextProcessStep = 0;
            return;
        }

        Firebug.debug("getting next process step");

        ProcessStep processStep = this.processSteps.get(this.nextProcessStep++);
        OrderEntryHistorySupport.Item item = this.pendingHistoryItem;

        processStep.process(item);
    }

    public void addProcessStep(ProcessStep processStep) {
        this.processSteps.add(processStep);
    }

    public OrderEntryHistorySupport.Item getPendingHistoryItem() {
        return this.pendingHistoryItem;
    }

    public boolean hasPendingHistoryItem() {
        return this.pendingHistoryItem != null;
    }

    public void cancelPendingHistoryItem() {
        this.pendingHistoryItem = null;
        this.nextProcessStep = 0;
    }

    public void load() {
        this.items.clear();

        String json = SessionData.INSTANCE.getUser().getAppConfig().getProperty(APP_CONFIG_PROPERTY);
        try {
            fromJSONValue(JSONParser.parseStrict(json));
        }
        catch(Exception e) {
            Firebug.warn("Reading OrderEntry history failed", e);
        }
    }

    private void removeIfDuplicate(Item item) {
        for(Item otherItem : this.items) {
            if(item.hasSameParametersAs(otherItem)) {
                this.items.remove(otherItem);
                return;
            }
        }
    }

    public void addItem(Item item) {
        if(item == null) return;

        removeIfDuplicate(item);
        this.items.addFirst(item);

        while(this.items.size() > MAX) {
            this.items.removeLast();
        }

        final String json = toJSONValue().toString();
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(APP_CONFIG_PROPERTY, json);
    }

    public List<Item> getItems() {
        return this.items;
    }

    void fromJSONValue(JSONValue jsonValue) {
        final JSONArray jsonArray = jsonValue.isArray();

        if(jsonArray != null){
            for(int i = 0; i < jsonArray.size() && i < MAX; i++) {
                JSONValue value = jsonArray.get(i);
                Item item = new Item();
                item.fromJSONValue(value);
                this.items.addLast(item);
            }
        }
    }

    JSONValue toJSONValue() {
        final JSONArray jsonArray = new JSONArray();

        int i = 0;
        for(Item item : this.items) {
            jsonArray.set(i++, item.toJSONValue());
        }

        return jsonArray;
    }

    public static class Item {
        private static final String ORDER_ENTRY_PREFIX = "oe_"; //$NON-NLS$

        private static final String LABEL = "itemLabel"; //$NON-NLS$
        private static final String DATE = "itemDate"; //$NON-NLS$
        public static final String KEY = "key"; //$NON-NLS$
        public static final String VALUE = "value"; //$NON-NLS$

        private String label;
        private String date;

        private final HashMap<String, String> parameters = new HashMap<String, String>();

        public Item() {
            this.date = Long.toString(new Date().getTime());
//            this.parameters.put(DATE, Long.toString(new Date().getTime()));
        }

        public String getLabel() {
            return this.label;
//            return this.parameters.get(LABEL);
        }

        public void setLabel(String label) {
            this.label = label;
//            this.parameters.put(LABEL, label);
        }

        public Date getDate() {
//            final String dateStr = this.parameters.get(DATE);
            final String dateStr = this.date;
            try {
                return new Date(Long.parseLong(dateStr));
            }
            catch(NumberFormatException e) {
                return null;
            }
        }

        public void setDate(Date date) {
            if(date != null) {
                this.date = Long.toString(date.getTime());
//                this.parameters.put(DATE, Long.toString(date.getTime()));
            }
        }

        public String get(String key) {
            return this.parameters.get(ORDER_ENTRY_PREFIX + key);
        }

        public void put(String key, String value) {
            this.parameters.put(ORDER_ENTRY_PREFIX + key, value);
        }

        public boolean hasSameParametersAs(Item other) {
            return this.parameters.equals(other.parameters);
        }

        JSONValue toJSONValue() {
            final JSONArray jsonArray = new JSONArray();

            int i = 0;

            final JSONObject jsoLabel = new JSONObject();
            jsoLabel.put(KEY, new JSONString(LABEL));
            jsoLabel.put(VALUE, new JSONString(this.label));
            jsonArray.set(i++, jsoLabel);

            final JSONObject jsoDate = new JSONObject();
            jsoDate.put(KEY, new JSONString(DATE));
            jsoDate.put(VALUE, new JSONString(this.date));
            jsonArray.set(i++, jsoDate);

            for(Map.Entry<String, String> parameter : this.parameters.entrySet()) {
                final JSONObject jsonObject = new JSONObject();

                jsonObject.put(KEY, new JSONString(parameter.getKey()));
                jsonObject.put(VALUE, new JSONString(parameter.getValue()));

                jsonArray.set(i++, jsonObject);
            }

            return jsonArray;
        }

        void fromJSONValue(JSONValue jsonValue) {
            JSONArray jsonArray = jsonValue.isArray();
            if(jsonArray != null) {
                for(int i = 0; i < jsonArray.size(); i++) {
                    if(jsonArray.get(i) != null) {
                        final JSONObject jsonObject = jsonArray.get(i).isObject();
                        if(jsonObject != null) {
                            final String key = jsonObject.get(KEY).isString().stringValue();
                            final String value = jsonObject.get(VALUE).isString().stringValue();

                            if(LABEL.equals(key)) {
                                this.label = value;
                            }
                            else if (DATE.equals(key)) {
                                this.date = value;
                            }
                            else {
                                this.parameters.put(key , value);
                            }
                        }
                    }
                }
            }
        }
    }

    public interface ProcessStep {
        void process(Item historyItem);
    }
}
