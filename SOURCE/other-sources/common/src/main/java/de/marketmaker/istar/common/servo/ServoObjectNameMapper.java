/*
 * ServoObjectNameMapper.java
 *
 * Created on 28.08.15 21:18
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.servo;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.netflix.servo.annotations.DataSourceLevel;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.jmx.ObjectNameMapper;
import com.netflix.servo.monitor.Monitor;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.tag.Tag;
import com.netflix.servo.tag.TagList;
import com.netflix.servo.util.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.Constants;

/**
 * custom jmx bean naming
 */
public class ServoObjectNameMapper implements ObjectNameMapper {
    @Override
    public ObjectName createObjectName(String _domain, Monitor<?> monitor) {
        final String wn = Constants.getWebappName();
        final String domain = "servo.monitors" + ((wn != null) ? ("." + wn) : "");

        final ObjectNameBuilder b = ObjectNameBuilder.forDomain(domain);

        Map<String, String> tags = new TreeMap<>(monitor.getConfig().getTags().asMap());
        b.addProperty("name", getMeasurement(monitor.getConfig(), tags));

        final String type = tags.remove("class");
        if (type != null) {
            b.addProperty("type", type);
        }

        tags.remove(DataSourceLevel.KEY);
        final String dstype = tags.remove(DataSourceType.KEY);

        for (Map.Entry<String, String> e : tags.entrySet()) {
            b.addProperty(e.getKey(), e.getValue());
        }
        b.addProperty("dstype", dstype);
        return b.build();
    }

    static String getMeasurement(MonitorConfig c, Map<String, String> tags) {
        final String id = tags.remove("id");
        if (id == null) {
            return c.getName();
        }
        // influxdb uses "." as separator in db.retention.measurement, so use a different char
        return id + "_" + c.getName();
    }


    /**
     * A helper class that assists in building
     * {@link ObjectName}s given monitor {@link Tag}s
     * or {@link TagList}. The builder also sanitizes
     * all values to avoid invalid input. Any characters that are
     * not alphanumeric, a period, or hypen are considered invalid
     * and are remapped to underscores.
     */
    private static final class ObjectNameBuilder {

        private static final Pattern INVALID_CHARS = Pattern.compile("[^a-zA-Z0-9_\\-\\.]");

        private static final Logger LOG = LoggerFactory.getLogger(ObjectNameBuilder.class);

        /**
         * Sanitizes a value by replacing any character that is not alphanumeric,
         * a period, or hyphen with an underscore.
         * @param value the value to sanitize
         * @return the sanitized value
         */
        public static String sanitizeValue(String value) {
            return INVALID_CHARS.matcher(value).replaceAll("_");
        }

        /**
         * Creates an {@link ObjectNameBuilder} given the JMX domain.
         * @param domain the JMX domain
         * @return The ObjectNameBuilder
         */
        public static ObjectNameBuilder forDomain(String domain) {
            return new ObjectNameBuilder(domain);
        }

        private final StringBuilder sb;

        private ObjectNameBuilder(String domain) {
            sb = new StringBuilder(sanitizeValue(domain));
            sb.append(":");
        }

        /**
         * Adds the key/value as a {@link ObjectName} property.
         * @param key the key to add
         * @param value the value to add
         * @return This builder
         */
        public ObjectNameBuilder addProperty(String key, String value) {
            sb.append(sanitizeValue(key))
                    .append('=')
                    .append(sanitizeValue(value)).append(",");
            return this;
        }

        /**
         * Builds the {@link ObjectName} given the configuration.
         * @return The created ObjectName
         */
        public ObjectName build() {
            final String name = sb.substring(0, sb.length() - 1);
            try {
                return new ObjectName(name);
            } catch (MalformedObjectNameException e) {
                LOG.warn("Invalid ObjectName provided: " + name);
                throw Throwables.propagate(e);
            }
        }

    }
}
