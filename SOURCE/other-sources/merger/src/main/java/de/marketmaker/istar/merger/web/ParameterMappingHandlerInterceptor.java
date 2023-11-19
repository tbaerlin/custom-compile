/*
 * ParameterMappingHandlerInterceptor.java
 *
 * Created on 29.01.2008 14:03:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * A HandlerInterceptor that maps an input parameter of an HttpServletRequest
 * to another parameter that it sets on the given request. The actual mappings are obtained
 * from a {@link de.marketmaker.istar.merger.web.ResourceParameterMapping} object.
 * <p/>
 * Note: Relies on the fact that it can actually add a parameter the the request's parameter
 * map. This will not be the case for genuine Tomcat request objects, so in that case the
 * original request will have to be wrapped with a custom request object (e.g., a
 * {@link de.marketmaker.istar.merger.web.RequestWrapper}).
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ParameterMappingHandlerInterceptor extends HandlerInterceptorAdapter implements
        InitializingBean {
    public enum DefaultValueStrategy {
        NULL, COPY
    }

    public enum DefaultWriteStrategy {
        OVERWRITE, JOIN
    }

    protected String sourceParameterName = null;

    protected String targetParameterName = null;

    protected String defaultParameterMapping = null;

    private String mappingName;

    private ResourceParameterMapping mappingSource;

    private DefaultValueStrategy defaultValueStrategy = DefaultValueStrategy.NULL;

    private DefaultWriteStrategy defaultWriteStrategy = DefaultWriteStrategy.OVERWRITE;

    private boolean ignoreEmptyParameter = false;

    public String toString() {
        return ClassUtils.getShortName(getClass()) + "["
                + this.sourceParameterName + " => "
                + this.targetParameterName + " using "
                + this.mappingName + " default "
                + this.defaultValueStrategy + " defaultMapping"
                + this.defaultParameterMapping + "]";
    }

    @Required
    public void setMappingSource(ResourceParameterMapping mappingSource) {
        this.mappingSource = mappingSource;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    @Required
    public void setSourceParameterName(String sourceParameterName) {
        this.sourceParameterName = sourceParameterName;
    }

    public void setTargetParameterName(String targetParameterName) {
        this.targetParameterName = targetParameterName;
    }

    public String getTargetParameterName() {
        return targetParameterName;
    }

    public void setDefaultValueStrategy(String defaultValueStrategy) {
        this.defaultValueStrategy = DefaultValueStrategy.valueOf(defaultValueStrategy);
    }

    public void setDefaultWriteStrategy(DefaultWriteStrategy defaultWriteStrategy) {
        this.defaultWriteStrategy = defaultWriteStrategy;
    }

    public void setIgnoreEmptyParameter(boolean ignoreEmptyParameter) {
        this.ignoreEmptyParameter = ignoreEmptyParameter;
    }

    public String getDefaultParameterMapping() {
        return defaultParameterMapping;
    }

    public void setDefaultParameterMapping(String defaultParameterMapping) {
        this.defaultParameterMapping = defaultParameterMapping;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.mappingName == null) {
            this.mappingName = this.sourceParameterName;
        }
        if (this.targetParameterName == null) {
            this.targetParameterName = this.sourceParameterName + "_mapped";
        }
    }

    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object o) throws Exception {
        String[] values = getSourceValues(request);
        if (values == null) {
            return true;
        }

        if (this.ignoreEmptyParameter) {
            int n = 0;
            for (final String value : values) {
                if (StringUtils.hasText(value)) {
                    values[n++] = value;
                }
            }

            if (n == 0) {
                return true;
            }

            values = Arrays.copyOf(values, n);
        }

        final String[] result = translate(values);
        if (result != null) {
            if (this.defaultWriteStrategy == DefaultWriteStrategy.JOIN) {
                final String[] existing = request.getParameterValues(this.targetParameterName);
                request.getParameterMap().put(this.targetParameterName, join(existing, result));
            }
            else {
                request.getParameterMap().put(this.targetParameterName, result);
            }
        }
        return true;
    }

    protected String[] getSourceValues(HttpServletRequest request) {
        return request.getParameterValues(this.sourceParameterName);
    }

    public String[] join(String[] s1, String[] s2) {
        if (s1 == null) {
            return s2;
        }
        final String[] result = new String[s1.length + s2.length];
        System.arraycopy(s1, 0, result, 0, s1.length);
        System.arraycopy(s2, 0, result, s1.length, s2.length);
        return result;
    }

    protected String[] translate(String[] values) {
        final String[] result = this.mappingSource.translate(this.mappingName, values);

        if (result != null && result.length != 0) {
            return result;
        }
        if (this.defaultParameterMapping != null) {
            return new String[]{this.defaultParameterMapping};
        }
        if (this.defaultValueStrategy == DefaultValueStrategy.COPY) {
            return values;
        }
        return result;
    }

    protected static String[] toArray(List<String> strings) {
        return strings.toArray(new String[strings.size()]);
    }
}
