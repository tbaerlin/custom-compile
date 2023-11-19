/*
 * PmRestCredentialsPostProcessor.java
 *
 * Created on 11.01.2016 11:11
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.util.Base64;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.http.RestTemplateFactory;
import de.marketmaker.itools.pmxml.frontend.PmxmlCryptUtil;
import de.marketmaker.itools.pmxml.frontend.PmxmlHash;

/**
 * Applies PM encrypted HTTP basic auth credentials to a RestTemplateFactory.
 *
 * Remark: It is not possible to change the values during runtime via JMX, because this will
 * take only effect if a new RestTemplateFactory is created.
 * Hence, JMX managed properties are readonly.
 *
 * @see RestTemplateFactory
 * @author mdick
 */
@ManagedResource
public class PmRestCredentialsPostProcessor implements InitializingBean, BeanPostProcessor {
    private final Log logger = LogFactory.getLog(getClass());

    private String credentials;

    private String host = AuthScope.ANY_HOST;

    private int port = AuthScope.ANY_PORT;

    private String realm = AuthScope.ANY_REALM;

    private String scheme = AuthScope.ANY_SCHEME;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.logger.debug("" + this.credentials + " " + getLogInfo());
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        if (bean instanceof RestTemplateFactory) {
            apply((RestTemplateFactory) bean);
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    private void apply(RestTemplateFactory restTemplateFactory) {
        if (StringUtils.hasText(this.credentials)) {
            final Credentials value = get();
            if (value != null) {
                final AuthScope authScope = new AuthScope(this.host, this.port, this.realm, this.scheme);
                restTemplateFactory.addCredentials(authScope, value);
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Applied HTTP credentials for " + getLogInfo());
                }
            }
        }
    }

    private Credentials get() {
        final Base64.Decoder decoder = Base64.getDecoder();
        final String key = "a990b8646$1Y9dtoN7GpxyPy";

        final String pair;
        try {
            pair = PmxmlCryptUtil.decrypt(key, decoder.decode(this.credentials));
        } catch (Exception e) {
            this.logger.error("Applying HTTP credentials failed for " + getLogInfo(), e);
            return null;
        }

        final String[] encodedValues = pair.trim().split("#");
        if (encodedValues.length != 2) {
            this.logger.error("Applying HTTP credentials failed for " + getLogInfo() + ". Wrong format!");
            return null;
        }

        final String username = new String(decoder.decode(encodedValues[0]), PmxmlHash.PM_CHARSET);
        final String password = new String(decoder.decode(encodedValues[1]), PmxmlHash.PM_CHARSET);

        return new UsernamePasswordCredentials(username, password);
    }

    private String getLogInfo() {
        return this.host + ":" + this.port + " realm '" + this.realm + "' scheme '" + this.scheme + "'";
    }

    @ManagedAttribute
    public String getUserPrincipalName() {
        if (StringUtils.hasText(this.credentials)) {
            final Credentials credentials = get();
            if (credentials != null && credentials.getUserPrincipal() != null) {
                return credentials.getUserPrincipal().getName();
            }
        }
        return null;
    }

    @ManagedAttribute
    public String getAuthScopeInfo() {
        return new AuthScope(this.host, this.port, this.realm, this.scheme).toString();
    }

    @ManagedAttribute
    public String getCredentials() {
        return this.credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    @SuppressWarnings("unused")
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
}
