/*
 * DataBinderUtils.java
 *
 * Created on 04.11.2008 14:42:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.bind.ServletRequestDataBinder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public final class DataBinderUtils {
    private DataBinderUtils() {
    }

    /** simple helper object to support creating a Mapping in a single statement */
    public static class Mapping {
        private final Map<String, String> map = new HashMap<>();

        public Mapping add(String s) {
            return add(s, s);
        }

        public Mapping add(String s1, String s2) {
            this.map.put(s1, s2);
            return this;
        }
    }

    public static ServletRequestDataBinder createBinder(Object object,
            final Mapping mapping) {
        return createBinder(object, mapping.map);
    }

    /**
     * Creates a customized ServletRequestDataBinder that only uses some of the request's parameters
     * and that can map these parameters to other names (e.g., map uppercase parameters to
     * java bean compatible names starting with a lowercase character).
     * @param object to be bound
     * @param mappings maps parameter names to those that should be used for binding
     * @return new ServletRequestDataBinder
     */
    public static ServletRequestDataBinder createBinder(Object object,
            final Map<String, String> mappings) {
        return new ServletRequestDataBinder(object) {
            public void bind(ServletRequest request) {
                // Translate parameter names into acceptable property names before binding.
                final Map<Object, Object> parameters = new HashMap<>();
                for (Map.Entry<String, String> entry : mappings.entrySet()) {
                    final String value = request.getParameter(entry.getKey());
                    if (value != null) {
                        parameters.put(entry.getValue(), value);
                    }
                }

                doBind(new MutablePropertyValues(parameters));
            }
        };
    }
}
