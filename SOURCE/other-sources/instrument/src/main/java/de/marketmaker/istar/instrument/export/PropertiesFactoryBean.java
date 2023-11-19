/*
 * PropertyAccessor.java
 *
 * Created on 09.04.2010 14:49:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.DefaultPropertiesPersister;

import de.marketmaker.istar.common.util.IoUtils;

/**
 * A spring factory bean provides access to a configured properties file through
 * {@link java.util.Properties}. The backing properties file is loaded on start-up and written back
 * on shut-down. Note that the persistent operation upon shut-down depends on the Spring Bean's
 * destruction(destroy method invocation) order.
 *
 * @author zzhao
 * @since 1.2
 */
public class PropertiesFactoryBean extends DefaultPropertiesPersister implements FactoryBean,
        InitializingBean, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File propertyFile;

    private Properties object;

    public void setPropertyFile(File propertyFile) {
        this.propertyFile = propertyFile;
    }

    public Object getObject() throws Exception {
        return this.object;
    }

    public Class getObjectType() {
        return Properties.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        if (null == this.propertyFile) {
            throw new IllegalStateException("A property file is required");
        }

        this.object = new Properties();
        if (this.propertyFile.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(this.propertyFile);
                load(this.object, fis);
            } finally {
                IoUtils.close(fis);
            }
        }

        this.logger.info("<afterPropertiesSet> " + this.object.size() + " properties loaded");
    }

    public void destroy() throws Exception {
        FileWriter fw = null;
        try {
            fw = new FileWriter(this.propertyFile);
            store(this.object, fw, "Properties");
            this.logger.info("<destroy> " + this.object.size() + " properties persisted");
        } catch (IOException e) {
            this.logger.error("<destroy> cannot persist properties", e);
        } finally {
            IoUtils.close(fw);
        }
    }
}
