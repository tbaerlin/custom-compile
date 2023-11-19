/*
 * Main.java
 *
 * Created on 07.10.2004 10:32:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.spring;

import java.io.File;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import de.marketmaker.istar.common.lifecycle.ContextStarter;
import de.marketmaker.istar.common.log.JmxLog;
import de.marketmaker.istar.common.servo.ServoMonitoring;
import de.marketmaker.istar.common.servo.ServoObjectNameMapper;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Main implements BeanFactoryPostProcessor {

    private static final String JMX_MAPPER_CLASS = "com.netflix.servo.DefaultMonitorRegistry.jmxMapperClass";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Thread hook;

    private ConfigurableApplicationContext applicationContext;

    static void addCommonBeans(ConfigurableListableBeanFactory bf) {
        if (!hasBeanOfType(bf, JmxLog.class)) {
            bf.registerSingleton("jmxlog", new JmxLog());
        }
        if (!hasBeanOfType(bf, ContextStarter.class)) {
            bf.registerSingleton("starter", new ContextStarter());
        }
        if (ServoMonitoring.MONITORING_AVAILABLE) {
            bf.registerSingleton("_monitor", ServoMonitoring.getSetup());
        }
    }

    private static boolean hasBeanOfType(ConfigurableListableBeanFactory bf, final Class<?> clazz) {
        return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(bf, clazz).length > 0;
    }

    public Main() {
        this.hook = new Thread(Main.this::stop, "Main-ShutdownHook");
        Runtime.getRuntime().addShutdownHook(this.hook);
        if (System.getProperty(JMX_MAPPER_CLASS) == null) {
            System.setProperty(JMX_MAPPER_CLASS, ServoObjectNameMapper.class.getCanonicalName());
        }
    }

    public void start(String[] s) {
        // if the log file has not been rotated, make sure nagios ignores previous errors
        this.logger.info(JmxLog.OK_DEFAULT);

        this.logger.info("<start> applicationContextLocation = " + Arrays.asList(s));

        final PropertyPlaceholderConfigurer ppc = getPropertyPlaceholderConfigurer();

        final File istarHomeDir = new File(System.getProperty("istar.home"));
        final File istarConfDir = new File(istarHomeDir, "conf");

        this.applicationContext = new FileSystemXmlApplicationContext(s, false) {
            protected void postProcessBeanFactory(ConfigurableListableBeanFactory bf) throws BeansException {
                addBeanFactoryPostProcessor(Main.this);
                if (ppc != null) {
                    addBeanFactoryPostProcessor(ppc);
                }
                addCommonBeans(bf);
            }

            protected Resource getResourceByPath(String path) {
                if (path.indexOf(':') > 1) {
                    // e.g., http://... or classpath:... but NOT c:\foo
                    return getResource(path);
                }
                final File f = new File(path);
                if (f.isAbsolute()) {
                    return new FileSystemResource(path);
                }
                return new FileSystemResource(new File(istarConfDir, path).getAbsolutePath());
            }

            public void close() {
                super.close();

                if (Thread.currentThread() != hook) {
                    Runtime.getRuntime().removeShutdownHook(hook);
                }
            }
        };

        try {
            this.applicationContext.refresh();
            this.applicationContext.start();
            postStart();
        } catch (Throwable t) {
            this.logger.error("<start> failed", t);
            throw new RuntimeException(t);
        }
        this.logger.info("<start> done.");
        if (Boolean.getBoolean("shutdown.immediately")) {
            this.applicationContext.close();
        }
    }

    @Override
    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // empty
    }

    protected void postStart() {
        scheduleMidnightEvent();
    }

    protected void scheduleMidnightEvent() {
        TaskScheduler scheduler;
        try {
            scheduler = this.applicationContext.getBean("scheduler", TaskScheduler.class);
        } catch (Exception e) {
            return;
        }
        scheduler.schedule(() -> {
            this.applicationContext.publishEvent(new MidnightEvent(Main.this));
        }, new CronTrigger("0 0 0 * * *"));
        this.logger.info("<scheduleMidnightEvent> with scheduler");
    }

    protected PropertyPlaceholderConfigurer getPropertyPlaceholderConfigurer() {
        final PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        final String istarPropertiesPath = System.getProperty("istar.properties", null);
        if (istarPropertiesPath == null) {
            return null;
        }

        ppc.setLocation(new FileSystemResource(istarPropertiesPath));
        return ppc;
    }

    synchronized public void stop() {
        if (this.applicationContext != null) {
            this.logger.info("<stop> ...");
            this.applicationContext.close();
            this.logger.info("<stop> done.");
            this.applicationContext = null;
        }
        stopLoggerContext();
    }

    private void stopLoggerContext() {
        try {
            // if an AsyncRedisAppender is present, this call ensures that all log events received
            // so far are actually forwarded to redis before the VM exits
            ((ch.qos.logback.classic.LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory()).stop();
        } catch (Throwable t) {
            // don't care whether ClassNotFound nor s.th. else;
        }
    }

    public synchronized Object getObjectFromAppContext(String id) {
		Object res = null;
		if (this.applicationContext != null) {
			this.logger.info("get Object: Id = " + id);
			res = this.applicationContext.getBean(id);
		}
		return res;
	}

    public static void main(String[] args) {
        final Main main = new Main();
        main.start(args);
    }
}
