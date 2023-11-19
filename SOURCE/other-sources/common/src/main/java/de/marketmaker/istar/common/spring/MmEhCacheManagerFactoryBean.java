/*
 * MmEhCacheManagerFactoryBean.java
 *
 * Created on 08.11.11 15:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.spring;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.management.ManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringValueResolver;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Replacement for {@link org.springframework.cache.ehcache.EhCacheManagerFactoryBean}. If the
 * context in which this bean is defined contains a
 * {@link org.springframework.beans.factory.config.PropertyPlaceholderConfigurer}, this object's
 * delegate <code>resolver</code> will be set and used to resolve all placeholders in the
 * ehcache configuration file.
 * In addition to creating a CacheManager, the default behavior
 * of this factory bean is also to register the caches with the jmx mbean server.
 *
 * @author oflege
 */
public class MmEhCacheManagerFactoryBean implements InitializingBean, DisposableBean,
    FactoryBean<CacheManager>, EmbeddedValueResolverAware, BeanNameAware {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Resource configLocation;

    private CacheManager cacheManager;

    private boolean registerWithMBeanServer = true;

    private StringValueResolver resolver;

    private String name = getClass().getSimpleName();

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.resolver = resolver;
    }

    public void setRegisterWithMBeanServer(boolean registerWithMBeanServer) {
        this.registerWithMBeanServer = registerWithMBeanServer;
    }

    /**
     * Set the location of the EHCache config file. A typical value is "/WEB-INF/ehcache.xml".
     * <p>Default is "ehcache.xml" in the root of the class path, or if not found,
     * "ehcache-failsafe.xml" in the EHCache jar (default EHCache initialization).
     *
     * @see net.sf.ehcache.CacheManager#create(java.io.InputStream)
     * @see net.sf.ehcache.CacheManager#CacheManager(java.io.InputStream)
     */
    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    public void afterPropertiesSet() throws IOException, CacheException {
        logger.info("Initializing EHCache CacheManager");
        final Configuration config = this.configLocation == null
                ? ConfigurationFactory.parseConfiguration()
                : ConfigurationFactory.parseConfiguration(getConfig());
        final CacheManager cacheManager = CacheManager.getCacheManager(config.getName());
        if (cacheManager != null) { // use existing cache manager
            this.cacheManager = cacheManager;
        }
        else {
            this.cacheManager = CacheManager.create(config); // create new cache manager
            // register caches with MBean server where they are created
            if (this.registerWithMBeanServer) {
                final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
                ManagementService.registerMBeans(this.cacheManager, mBeanServer, false,
                        true, true, true);
                this.logger.info("<afterPropertiesSet> registerMBeans for cache manager "
                        + this.cacheManager.getName());
            }
        }
    }

    private InputStream getConfig() throws IOException {
        if (this.resolver == null) {
            return this.configLocation.getInputStream();
        }
        final String resolvedConfig = this.resolver.resolveStringValue(FileCopyUtils.copyToString(
                new InputStreamReader(this.configLocation.getInputStream(), UTF_8)));
        return new ByteArrayInputStream(resolvedConfig.getBytes(UTF_8));
    }


    public CacheManager getObject() {
        return this.cacheManager;
    }

    public Class<? extends CacheManager> getObjectType() {
        return (this.cacheManager != null ? this.cacheManager.getClass() : CacheManager.class);
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() {
        this.logger.info("<destroy> " + this.name);
        this.cacheManager.shutdown();
    }
}

