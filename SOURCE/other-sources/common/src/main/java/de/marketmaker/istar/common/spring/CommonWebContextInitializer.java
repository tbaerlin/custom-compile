/*
 * CommonContextInitializer.java
 *
 * Created on 03.09.15 07:08
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.spring;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import de.marketmaker.istar.common.log.JmxLog;

/**
 * Adds common beans to a web application context, configured in web.xml with
 * <pre>
 * &lt;context-param>
 *  &lt;param-name>contextInitializerClasses&lt;/param-name>
 *  &lt;param-value>de.marketmaker.istar.common.spring.CommonWebContextInitializer&lt;/param-value>
 * &lt;/context-param>
 * </pre>
 * 
 * @author oflege
 */
public class CommonWebContextInitializer
        implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {

    @Override
    public void initialize(ConfigurableWebApplicationContext ac) {
        ac.addBeanFactoryPostProcessor(Main::addCommonBeans);
        LoggerFactory.getLogger(getClass()).info(JmxLog.OK_DEFAULT);
    }
}
