/*
 * ProductGuard.java
 *
 * Created on 20.02.2009 18:14:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.jcip.annotations.GuardedBy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class ProductGuard implements InitializingBean {
    private static final String VWD_KONZERN_ID = "0";

    private final Log logger = LogFactory.getLog(getClass());

    @GuardedBy("this")
    private Map<String, Set<String>> productsByModule = Collections.emptyMap();

    private Resource productsByModuleProperties;

    public void afterPropertiesSet() throws Exception {
        if (!loadProductsByModule()) {
            throw new Exception();
        }
    }

    @ManagedOperation
    public boolean loadProductsByModule() {
        if (this.productsByModuleProperties == null) {
            return true;
        }
        final Properties p;
        try {
            p = PropertiesLoader.load(this.productsByModuleProperties);
        } catch (IOException e) {
            this.logger.error("<loadProductsByModule> failed to load " + this.productsByModuleProperties, e);
            return false;
        }
        final HashMap<String, Set<String>> m = new HashMap<>();
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            m.put((String) entry.getKey(), StringUtils.commaDelimitedListToSet((String) entry.getValue()));
        }
        setProductsByModule(m);
        return true;
    }

    public void setProductsByModuleProperties(Resource productsByModuleProperties) {
        this.productsByModuleProperties = productsByModuleProperties;
    }

    private synchronized Set<String> getProductsAllowedForModule(String module) {
        return this.productsByModule.get(module);
    }

    public boolean profileAllowsModule(Profile profile, String module) {
        if (!(profile instanceof VwdProfile)) {
            return true;
        }
        final VwdProfile vp = (VwdProfile) profile;
        if (VWD_KONZERN_ID.equals(vp.getKonzernId())) {
            this.logger.info("<profileAllowsModule> " + vp.getVwdId() + " allowed for all, uses " + module);
            return true;
        }
        final Set<String> s = getProductsAllowedForModule(module);
        if (s == null) {
            return true;
        }
        if (vp.getProduktId() == null) {
            this.logger.warn("<profileAllowsModule> no produktId for user " + vp.getVwdId());
            return false;
        }
        if (s.contains(vp.getProduktId())) {
            return true;
        }
        this.logger.warn("<profileAllowsModule> user " + vp.getVwdId() + " not allowed for '" + module + "'"
            + " which requires " + s + ", user has " + vp.getProduktId());
        return false;
    }

    private synchronized void setProductsByModule(Map<String, Set<String>> productsByModule) {
        this.productsByModule = productsByModule;
        this.logger.info("<setProductsByModule> " + this.productsByModule);
    }
}
