/*
 * RpcExchangeAndRequestQueueDeclarer.java
 *
 * Created on 04.03.2011 10:19:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.connections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Straight forward implementation of {@link AmqpRpcAddress}.
 *
 * @see AmqpRpcAddress
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class AmqpRpcAddressImpl implements AmqpRpcAddress {

    private final Log logger = LogFactory.getLog(getClass());

    protected String exchange = DEFAULT_EXCHANGE;
    protected String requestQueue;

    protected Settings settings = new Settings();

    protected final Set<String> PROPERTIES_IN_PSEUDO_URL =
            new TreeSet<String>(Arrays.asList(
                    "requestQueueMessageTTL",
                    "exchangeAutoDelete", "exchangeDurable",
                    "queueAutoDelete", "queueDurable"
            ));

    public static AmqpRpcAddress createDefault(Class<?> serviceInterface) {
        final String queue = AmqpRpcAddress.DEFAULT_EXCHANGE + "." +
                serviceInterface.getCanonicalName();
        final AmqpRpcAddressImpl address = new AmqpRpcAddressImpl();
        address.setRequestQueue(queue);
        return address;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRequestQueue() {
        return requestQueue;
    }

    public void setRequestQueue(String requestQueue) {
        this.requestQueue = requestQueue;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public String getPseudoUrl() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(getExchange()).append("/");
        sb.append(getRequestQueue()).append("?");
        BeanWrapper settings = new BeanWrapperImpl(this.settings);
        for (PropertyDescriptor propertyDescriptor : settings.getPropertyDescriptors()) {
            final String propertyName = propertyDescriptor.getName();
            if (PROPERTIES_IN_PSEUDO_URL.contains(propertyName)) {
                sb.append(propertyName).append("=");
                sb.append(settings.getPropertyValue(propertyName)).append("&");
            }
        }
        // remove last '&' that was added in last iteration of loop
        if (!PROPERTIES_IN_PSEUDO_URL.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * The character class that we allow in property names and values
     */
    protected static final String ALLOWED_CHARS = "[-a-zA-Z0-9\\._]";

    /**
     * used for parsing a pseudo-URL of the form {@code exchange/queue?key1=value1&key2=value2}
     */
    protected static final Pattern PSEUDO_URL_FIRST_PART = Pattern.compile(
            "(" + ALLOWED_CHARS + "+)/(" + ALLOWED_CHARS + "+)\\??");
    /**
     * used for parsing a pseudo-URL of the form {@code exchange/queue?key1=value1&key2=value2}
     */
    protected static final Pattern PSEUDO_URL_KEY_VALUE = Pattern.compile(
            "[\\?&](" + ALLOWED_CHARS + "+)=(" + ALLOWED_CHARS + "+)");

    public void setPseudoUrl(final String pseudoUrl) {
        BeanWrapper settings = new BeanWrapperImpl(this.settings);
        try {
            // First, just check and store the info found in pseudoUrl
            final Matcher firstPartMatcher = PSEUDO_URL_FIRST_PART.matcher(pseudoUrl);
            firstPartMatcher.find();
            final String exchange = firstPartMatcher.group(1);
            final String queue = firstPartMatcher.group(2);
            final Matcher keyValueMatcher = PSEUDO_URL_KEY_VALUE.matcher(pseudoUrl);
            Map<String, Object> properties = new HashMap<String, Object>(11);
            while (keyValueMatcher.find()) {
                final String key = keyValueMatcher.group(1);
                final String value = keyValueMatcher.group(2);
                if (!PROPERTIES_IN_PSEUDO_URL.contains(key)) {
                    throw new IllegalArgumentException(pseudoUrl + " contains property " + key +
                            ", which not supported by this class.");
                }
                Class<?> type = settings.getPropertyDescriptor(key).getPropertyType();
                if (type == Integer.class || type == int.class) {
                    properties.put(key, Integer.valueOf(value));
                } else if (type == Boolean.class || type == boolean.class) {
                    properties.put(key, Boolean.valueOf(value));
                } else {
                    throw new AssertionError("Class has only properties of types int and boolean");
                }
            }
            // Second -- now that we know it's valid -- apply changes.
            setExchange(exchange);
            setRequestQueue(queue);
            for (Map.Entry<String, Object> p : properties.entrySet()) {
                settings.setPropertyValue(p.getKey(), p.getValue());
            }
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(pseudoUrl + " is not a well-formed AMQP-RPC Pseudo-URL.");
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(pseudoUrl + " is not a well-formed AMQP-RPC Pseudo-URL.");
        }
    }

    @Override
    public String toString() {
        return getPseudoUrl();
    }
}
