/*
 * ContextStarter.java
 *
 * Created on 24.02.15 10:06
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Starts the {@link org.springframework.context.Lifecycle} beans in the
 * {@link org.springframework.context.ConfigurableApplicationContext}s for which it receives a
 * {@link org.springframework.context.event.ContextRefreshedEvent}. Does not
 * care about stopping the beans as that happens automatically when the respective contexts are closed.
 * @author oflege
 */
public class ContextStarter implements ApplicationListener<ContextRefreshedEvent> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!(event.getSource() instanceof ConfigurableApplicationContext)) {
            return;
        }
        final ConfigurableApplicationContext cac = (ConfigurableApplicationContext) event.getSource();
        this.logger.info("<onApplicationEvent> starting " + cac.getDisplayName());
        cac.start();
    }
}
