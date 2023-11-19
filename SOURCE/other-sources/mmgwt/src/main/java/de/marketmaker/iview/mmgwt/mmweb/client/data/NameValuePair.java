/*
 * NameValuePair.java
 *
 * Created on 20.10.2009 08:39:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.data;

import java.util.List;
import java.util.ArrayList;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;

/**
 * @author oflege
 */
public class NameValuePair extends BaseModelData {
    public static final String DISPLAY_FIELD = "name"; // $NON-NLS-0$

    public static final String VALUE_FIELD = "value"; // $NON-NLS-0$

    public static final ListStore<NameValuePair> createStore(String[][] datas) {
        final ListStore<NameValuePair> result = new ListStore<NameValuePair>();
        result.add(createPairs(datas));
        return result;
    }

    public static final List<NameValuePair> createPairs(String[][] datas) {
        final ArrayList<NameValuePair> result = new ArrayList<NameValuePair>();
        for (String[] data : datas) {
            result.add(new NameValuePair(data[1], data[0]));
        }
        return result;
    }

    public NameValuePair(String name, String value) {
        setName(name);
        setValue(value);
    }

    public String getValue() {
      return get(VALUE_FIELD);
    }

    public void setValue(String value) {
      set(VALUE_FIELD, value);
    }

    public String getName() {
      return get(DISPLAY_FIELD);
    }

    public void setName(String name) {
      set(DISPLAY_FIELD, name);
    }

    @Override
    public String toString() {
        return getName() + "=>" + getValue(); // $NON-NLS-0$
    }
}
