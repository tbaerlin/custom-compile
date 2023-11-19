/*
 * FundReportCounter.java
 *
 * Created on 24.03.2009 12:12:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;

import de.marketmaker.istar.merger.provider.profile.CounterService;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundReportCounter implements ResponseExtractor<Boolean> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CounterService accessCounter;

    public void setAccessCounter(CounterService accessCounter) {
        this.accessCounter = accessCounter;
    }

    public boolean countAccess(String customer) {
        //count access may return null so we can't rely on auto un-boxing
        return accessCounter.countAccess(customer, "", "2080", "4", this) == Boolean.TRUE;
    }

    @Override
    public Boolean extractData(ClientHttpResponse response) throws IOException {
        try {
            return handle(response.getBody());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private Boolean handle(InputStream is) throws Exception {
        final SAXBuilder builder = new SAXBuilder();
        final Document document = builder.build(is);
        final Element root = document.getRootElement();

        final Element cntAccount = root.getChild("CntAccount");
        if (cntAccount == null) {
            return onError(document);
        }

        final Element header = cntAccount.getChild("Header");
        if (header != null) {
            final String status = header.getChildTextTrim("Status");
            if ("1".equals(status)) {
                return true;
            }
            if ("-2".equals(status)) {
                return false;
            }
        }

        return onError(document);
    }

    private Boolean onError(Document document) {
        this.logger.warn("<onError> invalid response");
        this.logger.warn(formatErrorDoc(document));
        return false;
    }

    private String formatErrorDoc(Document d) {
        try {
            return doFormatErrorDoc(d);
        } catch (Exception e) {
            return "format failed";
        }
    }

    private String doFormatErrorDoc(Document d) throws IOException {
        final StringWriter sw = new StringWriter(8192);
        final XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            outputter.output(d, sw);
        } finally {
            sw.close();
        }
        return sw.toString();
    }
}
