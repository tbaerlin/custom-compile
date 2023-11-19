/*
 * HttpDmxmlFacade.java
 *
 * Created on 06.12.13 10:55
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.fusion.dmxml;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.marketmaker.istar.common.http.RestTemplateFactory;
import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.istar.merger.web.GsonUtil;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;

/**
 * Implements {@link de.marketmaker.istar.fusion.dmxml.DmxmlFacade} by sending dmxml requests
 * to a http server for evaluation.
 * @author oflege
 */
public class HttpDmxmlFacade implements DmxmlFacade {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final JaxbHandler handler = new JaxbHandler(getClass().getPackage().getName());

    private RestTemplate restTemplate;

    private String retrieveUrl;

    public void setRetrieveUrl(String retrieveUrl) {
        this.retrieveUrl = retrieveUrl;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.restTemplate.getMessageConverters()
                .add(handler.createMessageConverter(ResponseType.class));
    }

    @Override
    public boolean evaluate(DmxmlRequest request) {
        final ResponseType response = doEvaluate(request);
        if (response == null) {
            return false;
        }
        request.setResult(response);
        return true;
    }

    /**
     * Calls {@link #evaluate(DmxmlRequest)}.
     */
    @Override
    public boolean evaluate(HttpServletRequest servletRequest, DmxmlRequest request) {
        return evaluate(request);
    }

    private ResponseType doEvaluate(final DmxmlRequest request) {
        MoleculeRequest mr = request.getMoleculeRequest();
        final String json = GsonUtil.toJson(mr);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        URI uri = UriComponentsBuilder.fromHttpUrl(this.retrieveUrl).build().toUri();

        try {
            return this.restTemplate.postForObject(uri, entity, ResponseType.class);
        } catch (Exception e) {
            this.logger.error("<doEvaluate> failed for " + json, e);
            return null;
        }
    }

    public static void main(String[] args) {
        RestTemplateFactory f = new RestTemplateFactory();
        HttpDmxmlFacade dmxml = new HttpDmxmlFacade();
        dmxml.setRestTemplate(f.getObject());
        dmxml.setRetrieveUrl("http://gis-test.vwd.com/dmxml-1/iview/retrieve.xml");
        DmxmlRequest.Builder b = new DmxmlRequest.Builder().withAuth("mm-xml", "resource");
        DmxmlRequest.Builder.Block<MSCPriceData> block = b.addBlock("1", "MSC_PriceData");
        block.with("symbol", "86586.iid");

        DmxmlRequest r = b.build();
        if (dmxml.evaluate(r)) {
            System.out.println(block.getResult().getInstrumentdata().getName());
        }
    }
}
