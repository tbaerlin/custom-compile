/*
 * IstarNamingStrategy.java
 *
 * Created on 22.04.2005 15:42:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.jmx;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.jmx.export.naming.ObjectNamingStrategy;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.util.ClassUtils;

import de.marketmaker.istar.common.Constants;

/**
 * Strategy for naming beans that are registered with a jmx server. By default, a bean defined as
 * <pre>&lt;bean name="foo" class="x.y.Bar"...</pre>
 * will be named <pre>{@value #DOMAIN}:type=Bar,name=foo</pre>, or, if the context is part of a
 * webapp: <pre>{@value #DOMAIN}:type=Bar,name=foo,webapp=appid</pre>
 * For customization, use {@link #setNameMappings(java.util.Map)} for which each key is a bean name/id and each
 * value is either an alias name to be used with the default object name or, if the value
 * contains a ':', a complete object name that will be used as-is
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IstarNamingStrategy implements ObjectNamingStrategy {

    public final static String DOMAIN = "de.marketmaker.istar";

    public final static class ObjectNameComparator implements Comparator<ObjectName>, Serializable {
        protected static final long serialVersionUID = 1L;

        private final String keyProperty;

        public ObjectNameComparator(String keyProperty) {
            this.keyProperty = keyProperty;
        }

        public int compare(ObjectName o1, ObjectName o2) {
            return o1.getKeyProperty(this.keyProperty).compareTo(o2.getKeyProperty(this.keyProperty));
        }
    }

    public final static Comparator<ObjectName> COMPARE_BY_NAME = new ObjectNameComparator("name");

    private Map<String, String> nameMappings = Collections.emptyMap();

    public void setNameMappings(Map<String, String> nameMappings) {
        this.nameMappings = nameMappings;
    }

    public ObjectName getObjectName(Object managedResource,
            String key) throws MalformedObjectNameException {
        final String name = getName(key);
        if (name.contains(":")) {
            return ObjectNameManager.getInstance(name);
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append(DOMAIN);
        sb.append(":type=").append(ClassUtils.getShortName(managedResource.getClass()));
        if (!(managedResource instanceof IstarMBeanExporter)) {
            sb.append(",name=").append(name);
        }
        final String webappName = Constants.getWebappName();
        if (webappName != null) {
            sb.append(",webapp=").append(webappName);
        }
        return ObjectNameManager.getInstance(sb.toString());
    }

    private String getName(String name) {
        return this.nameMappings.getOrDefault(name, name);
    }

    public static ObjectName byDomain() throws MalformedObjectNameException {
        return new ObjectName(DOMAIN + ":*");
    }

    public static ObjectName byType(String type) throws MalformedObjectNameException {
        return new ObjectName(DOMAIN + ":type=" + type + ",*");
    }

    public static ObjectName byName(String name) throws MalformedObjectNameException {
        return new ObjectName(DOMAIN + ":name=" + name + ",*");
    }

    public static String byTypeStr(String type) {
        return DOMAIN + ":type=" + type + ",*";
    }

    public static String byNameStr(String name) {
        return DOMAIN + ":name=" + name + ",*";
    }

    public static String getName(ObjectName on) {
        final String name = on.getKeyProperty("name");
        return (name != null) ? name : on.getKeyProperty("bean");
    }

    public static String getType(ObjectName on) {
        final String name = on.getKeyProperty("type");
        return (name != null) ? name : on.getKeyProperty("class");
    }
}
