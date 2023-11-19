/*
 * DZProfileSource.java
 *
 * Created on 30.06.2008 11:35:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.marketmaker.istar.common.http.RestTemplateFactory;
import de.marketmaker.istar.common.util.TimeTaker;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RealtimeDisclaimerProvider {
    public final static Status INVALID_DEFAULT = new Status(false, false, false);

    public static class Status {
        private final boolean readDisclaimer;

        private final boolean verified;

        private final boolean valid;

        private String status;

        public Status(boolean readDisclaimer, boolean verified, boolean valid) {
            this.readDisclaimer = readDisclaimer;
            this.verified = verified;
            this.valid = valid;
        }

        public boolean isReadDisclaimer() {
            return readDisclaimer;
        }

        public boolean isVerified() {
            return verified;
        }

        public boolean isValid() {
            return valid;
        }

        public Status withStatus(String status) {
            this.status = status;
            return this;
        }

        public String getStatus() {
            return status;
        }

        @SuppressWarnings("unused") // used in velocity
        public String getEbrokerageEncoding() {
            if (this.isVerified()) {
                return "2";
            }

            if (isReadDisclaimer()) {
                return "1";
            }

            return "0";
        }

        @Override
        public String toString() {
            return "Status{" +
                    "readDisclaimer=" + readDisclaimer +
                    ", verified=" + verified +
                    ", valid=" + valid +
                    ", status='" + status + '\'' +
                    '}';
        }
    }

    public static class RealtimeDisclaimerFactory {
        public Status read(InputStream is) throws Exception {
            final SAXBuilder builder = new SAXBuilder();
            final Document document = builder.build(is);
            final Element root = document.getRootElement();

            final boolean readDisclaimer = "1".equals(root.getChildTextTrim("hasReadAgbRT"));
            final boolean verified = "1".equals(root.getChildTextTrim("isEmailVerificated"));
            final boolean valid = "1".equals(root.getChildTextTrim("valid"));

            return new Status(readDisclaimer, verified, valid);
        }
    }

    public static final String URI_PRIO_1_MEL_NG = "http://gisweb.vwd.com/vwd-mel-ng/services/checkRealtimeAgb.htn";

    public static final String URI_PRIO_2_MEL = "http://gisweb.vwd.com/mel/services/checkRealtimeAgb.htn";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private RestTemplate restTemplate;

    private final RealtimeDisclaimerFactory realtimeDisclaimerFactory = new RealtimeDisclaimerFactory();

    private final ResponseExtractor<Status> realtimeDisclaimerHandler = response -> {
        try {
            return realtimeDisclaimerFactory.read(response.getBody());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    };

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Status getDisclaimerStatus(Boolean melNG, String rzid, String rzbk, String kdnr) {
        final boolean checkNewDisclaimer = (melNG != Boolean.FALSE);
        final boolean checkOnlyNewDisclaimer = (melNG == Boolean.TRUE);

        final TimeTaker tt = new TimeTaker();
        try {

            final Status prio1 = checkNewDisclaimer
                    ? getStatus(URI_PRIO_1_MEL_NG, rzid, rzbk, kdnr)
                    : new Status(false, false, false);

            final Status result = prio1.isValid() || checkOnlyNewDisclaimer
                    ? prio1.withStatus("MEL-NG")
                    : getStatus(URI_PRIO_2_MEL, rzid, rzbk, kdnr).withStatus("MEL");

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getDisclaimerStatus> for melNG="
                        + melNG + ", rzid='" + rzid + "', rzbk='" + rzbk + "', kdnr='" + kdnr
                        + "' is: " + result + ", took " + tt);
            }
            return result;
        } catch (Exception e) {
            this.logger.error("<getDisclaimerStatus> failed", e);
            return INVALID_DEFAULT;
        }
    }

    private Status getStatus(String uriStr, String rzid, String rzbk, String kdnr) {
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(uriStr)
                .queryParam("rzid", rzid)
                .queryParam("rzbk", rzbk)
                .queryParam("kdnr", kdnr);
        URI uri = b.build().toUri();
        return this.restTemplate.execute(uri, HttpMethod.GET, null, this.realtimeDisclaimerHandler);
    }


    public static void main(String[] args) throws Exception {
        RestTemplateFactory rtf = new RestTemplateFactory();

        final RealtimeDisclaimerProvider p = new RealtimeDisclaimerProvider();
        p.setRestTemplate(rtf.getObject());

        final Status disclaimer = p.getDisclaimerStatus(null, "XC", "3170", "321643800");
        System.out.println("disclaimer: " + disclaimer);

        rtf.destroy();
    }
}
