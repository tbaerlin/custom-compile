/*
 * ParameterMappingHandlerInterceptor.java
 *
 * Created on 29.01.2008 14:03:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.iview.dmxml.FNDFinderMetadata;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.istar.merger.util.JaxbContextCache;

/**
 * A KagidHandlerInterceptor that maps the input parameter kagid as a numeric id to the
 * respective issuername key.
 * <p/>
 * Note: Relies on the fact that it can actually add a parameter the the requests parameter
 * map. This will not be the case for genuine Tomcat request objects, so in that case the
 * original request will have to be wrapped with a custom request object (e.g., a
 * {@link de.marketmaker.istar.merger.web.RequestWrapper}).
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class KagidHandlerInterceptor extends HandlerInterceptorAdapter {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String inParameterName;
    private String outParameterName;
    private Resource resource;
    private AtomicReference<Map<Integer, String>> issuer = new AtomicReference<>();
    private AtomicLong lastRead = new AtomicLong(0L);
    private static final String EMPTY_NAME = "_";

    public String toString() {
        return ClassUtils.getShortName(getClass()) + "["
                + this.resource.getDescription() + "]";
    }

    @Required
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Required
    public void setInParameterName(String inParameterName) {
        this.inParameterName = inParameterName;
    }

    @Required
    public void setOutParameterName(String outParameterName) {
        this.outParameterName = outParameterName;
    }

    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String[] values = httpServletRequest.getParameterValues(this.inParameterName);
        if (values == null || values.length == 0) {
            return true;
        }
        final String s = getIssuername(values[0]);
        if(s==null) {
            return true;
        }

        httpServletRequest.getParameterMap().put(this.outParameterName, new String[]{s});

        return true;
    }

    protected String getIssuername(String s) {
        final int id;
        try {
            id = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }

        return getIssuername(id);
    }

    private String getIssuername(int id) {
        // 1 hour refresh
        if (System.currentTimeMillis() - this.lastRead.get() > 60 * 60 * 1000) {
            try {
                refresh();
            } catch (Exception e) {
                this.logger.error("<getMap> failed", e);
            }
        }

        final Map<Integer, String> map = this.issuer.get();

        if (map == null) {
            return EMPTY_NAME;
        }

        final String s = map.get(id);
        return StringUtils.hasText(s) ? s : EMPTY_NAME;
    }

    private void refresh() throws Exception {
        final TimeTaker tt = new TimeTaker();
        // TODO: iview is wrong, should move to fusion, but how?
        final JAXBContext jc = JaxbContextCache.INSTANCE.getContext("de.marketmaker.iview.dmxml");
        final Unmarshaller unmarshaller = jc.createUnmarshaller();
        final InputStream is = this.resource.getInputStream();
        final JAXBElement<ResponseType> jaxbElement = unmarshaller.unmarshal(new StreamSource(is), ResponseType.class);
        is.close();
        final ResponseType response = jaxbElement.getValue();
        final FNDFinderMetadata metadata = (FNDFinderMetadata) response.getData().getBlockOrError().get(0);
        final FinderMetaList list = metadata.getIssuername();

        final Map<Integer, String> map = new HashMap<>();
        for (final FinderMetaList.Element element : list.getElement()) {
            map.put(Math.abs(element.getKey().hashCode()), element.getKey());
        }

        this.issuer.set(map);
        this.lastRead.set(System.currentTimeMillis());

        this.logger.info("<refresh> succeeded in " + tt + " for " + map.size() + " entries");
    }
}
