/*
 * MmPropertyPlaceholderConfigurer.java
 *
 * Created on 08.11.11 15:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.spring;

import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Makes some methods of its superclass public; as in the Bash shell, default values can be
 * specified using the ":-" operator (e.g., ${undefined:-default} will be resolved as "default").
 * Resources that cannot be found will be ignored by default.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MmPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer
    implements InitializingBean {

    private static final Pattern KEY_WITH_DEFAULT = Pattern.compile("(.*?):-(.*)");

    private Properties mergedProperties;

    public MmPropertyPlaceholderConfigurer() {
        setIgnoreResourceNotFound(true);
    }

    public void afterPropertiesSet() throws Exception {
        this.mergedProperties = mergeProperties();
    }

    public String parseStringValue(String s) throws BeanDefinitionStoreException {
        return super.parseStringValue(s, this.mergedProperties, new HashSet());
    }

    @Override
    protected String resolvePlaceholder(String placeholder, Properties props,
            int systemPropertiesMode) {
        final Matcher matcher = KEY_WITH_DEFAULT.matcher(placeholder);
        if (!matcher.matches()) {
            return super.resolvePlaceholder(placeholder, props, systemPropertiesMode);
        }
        final String resolved
                = super.resolvePlaceholder(matcher.group(1), props, systemPropertiesMode);
        return (resolved != null) ? resolved : matcher.group(2);
    }
}
