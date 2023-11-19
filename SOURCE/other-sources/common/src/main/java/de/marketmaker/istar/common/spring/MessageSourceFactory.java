/*
 * MessageSourceFactory.java
 *
 * Created on 10.12.2010 16:51:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.spring;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.ClassUtils;

/**
 * @author oflege
 */
public class MessageSourceFactory {
    private static final ConcurrentHashMap<String, ResourceBundleMessageSource> MESSAGE_SOURCES
            = new ConcurrentHashMap<>();

    public static MessageSource create(Class c) {
        return create(ClassUtils.addResourcePathToPackagePath(c, "messages"));
    }

    public static MessageSource create(String basename) {
        final ResourceBundleMessageSource existing = MESSAGE_SOURCES.get(basename);
        if (existing != null) {
            return existing;
        }

        final ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
        
        messages.setBasename(basename);
        messages.setFallbackToSystemLocale(false);
        messages.setDefaultEncoding("UTF-8");
        ResourceBundleMessageSource ms = MESSAGE_SOURCES.putIfAbsent(basename, messages);
        return (ms != null) ? ms : messages;
    }

    private MessageSourceFactory() {
    }
}
