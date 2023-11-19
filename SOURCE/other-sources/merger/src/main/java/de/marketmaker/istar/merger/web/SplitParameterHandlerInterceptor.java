/*
 * ParameterMappingHandlerInterceptor.java
 *
 * Created on 29.01.2008 14:03:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * A SplitParameterHandlerInterceptor that maps an input parameter of an HttpServletRequest
 * to a list of other parameters that it sets on the given request. The actual mappings are obtained
 * from a {@link ResourceParameterMapping} object.
 * <p>
 * Note: Relies on the fact that it can actually add a parameter the the requests parameter
 * map. This will not be the case for genuine Tomcat request objects, so in that case the
 * original request will have to be wrapped with a custom request object (e.g., a
 * {@link RequestWrapper}).
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Ulrich Maurer
 */
public class SplitParameterHandlerInterceptor extends HandlerInterceptorAdapter implements
        InitializingBean {
    private String sourceParameterName = null;

    private Pattern splitPattern = null;
    private int splitPatternGroupCount;

    private List<ParameterMappingHandlerInterceptor> targetHandlers = null;

    public String toString() {
        return ClassUtils.getShortName(getClass()) + "["
                + this.sourceParameterName + ", "
                + this.getSplitPattern() + " => "
                + this.targetHandlers + "]";
    }

    @Required
    public void setSplitPattern(String splitPattern) {
        this.splitPattern = Pattern.compile(splitPattern);
    }

    public String getSplitPattern() {
        return splitPattern.pattern();
    }

    @Required
    public void setSourceParameterName(String sourceParameterName) {
        this.sourceParameterName = sourceParameterName;
    }

    @Required
    public void setTargetHandlers(List<ParameterMappingHandlerInterceptor> targetHandlers) {
        this.targetHandlers = targetHandlers;
    }

    public void afterPropertiesSet() throws Exception {
        final Matcher matcher = this.splitPattern.matcher("");
        this.splitPatternGroupCount = matcher.groupCount();
        if (this.targetHandlers == null) {
            throw new IllegalArgumentException("no targetHandlers specified");
        }
        if (this.splitPatternGroupCount != this.targetHandlers.size()) {
            throw new IllegalArgumentException("splitPattern group count (" + this.splitPatternGroupCount + ") does not match targetHandler count (" + this.targetHandlers.size() + ")");
        }
    }

    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        final String[] values = httpServletRequest.getParameterValues(this.sourceParameterName);
        if (values == null) {
            return true;
        }
        // split the values, using the splitPattern
        final String[][] splittedValues = new String[this.splitPatternGroupCount][values.length];
        for (int v = 0; v < values.length; v++) {
            final Matcher matcher = this.splitPattern.matcher(values[v]);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("argument " + this.sourceParameterName + " does not match pattern " + getSplitPattern() + ": " + values[v]);
            }
            for (int g = 0; g < this.splitPatternGroupCount; g++) {
                splittedValues[g][v] = matcher.group(g + 1);
            }
        }
        // evaluate the splitted values
        for (int g = 0; g < this.splitPatternGroupCount; g++) {
            final ParameterMappingHandlerInterceptor interceptor = this.targetHandlers.get(g);
            final String[] result = interceptor.translate(splittedValues[g]);
            if (result != null) {
                httpServletRequest.getParameterMap().put(interceptor.getTargetParameterName(), result);
            }
        }
        return true;
    }
}