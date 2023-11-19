package de.marketmaker.iview.pmxml;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeData;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataRequest;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataResponse;
import de.marketmaker.iview.pmxml.block.PmExchangeData;
import de.marketmaker.iview.pmxml.block.PmExchangeDataLogger;

/**
 * Created on 04.09.12 13:23
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * Use {@link PmxmlExchangeDataImpl} (and its derivations) when working with subclasses of ExchangeDataRequest/ExchangeDataResponse.
 * If you need to work with xml use {@link de.marketmaker.iview.pmxml.block.PmExchangeData}.
 *
 * @author Michael Lösch
 */

public class PmxmlExchangeDataImpl implements PmxmlExchangeData {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected PmxmlExchangeData pmxml;

    /**
     * english variant, used for sessions with locale en.
     */
    protected PmxmlExchangeData enPmxml;

    protected JaxbHandler jaxb;

    private String authToken;

    public void setJaxb(JaxbHandler jaxb) {
        this.jaxb = jaxb;
    }

    public void setPmxml(PmxmlExchangeData pmxml) {
        this.pmxml = pmxml;
    }

    public void setEnPmxml(PmxmlExchangeData enPmxml) {
        this.enPmxml = enPmxml;
    }

    public String getAuthToken() {
        return this.authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public PmxmlExchangeDataResponse exchangeData(PmxmlExchangeDataRequest pmxmlExchangeDataRequest) throws PmxmlException {
        return PmExchangeData.exchangeData(pmxmlExchangeDataRequest, getPmxml(), this.logger, PmExchangeDataLogger.SILENT);
    }

    /**
     * one way data exchange, no response is expected
     */
    public void exchangeData(ExchangeDataRequest request,
                             String functionKey) throws PmxmlException {
        doExchangeData(request, functionKey);
    }

    /**
     * two way data exchange with response
     */
    public <T> T exchangeData(ExchangeDataRequest request, String functionKey,
                              Class<T> responseType) throws PmxmlException {
        final PmxmlExchangeDataResponse pmxmlResponse = doExchangeData(request, functionKey);
        return this.jaxb.unmarshal(new String(pmxmlResponse.getData(), Charset.forName("UTF-8")), responseType);
    }

    private PmxmlExchangeDataResponse doExchangeData(ExchangeDataRequest request,
                                                     String functionKey) throws PmxmlException {
        final PmxmlExchangeDataRequest pmxmlRequest = prepareRequest(request, functionKey);
        return exchangeData(pmxmlRequest);
    }

    private PmxmlExchangeData getPmxml() {
        return isEnglish() ? this.enPmxml : this.pmxml;
    }

    protected boolean isEnglish() {
        return this.enPmxml != null;
    }

    private PmxmlExchangeDataRequest prepareRequest(ExchangeDataRequest request,
                                                    String functionKey) {
        final String xml = this.jaxb.marshal(ExchangeDataRequest.class, request, "parameter");
        return new PmxmlExchangeDataRequest(getAuthToken(), xml.getBytes(Charset.forName("UTF-8")), functionKey);
    }
}
