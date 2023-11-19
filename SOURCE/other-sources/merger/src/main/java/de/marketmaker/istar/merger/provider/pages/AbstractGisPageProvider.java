package de.marketmaker.istar.merger.provider.pages;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.web.client.RestTemplate;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
abstract public class AbstractGisPageProvider implements GisPageProvider {
    private static final String INVALID_PAGE = "Sie haben keine Berechtigung diese URL aufzurufen!";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected RestTemplate restTemplate;

    protected final String uri;

    public AbstractGisPageProvider(String uri) {
        this.uri = uri;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String getXsession() {
        final DateTime now = new DateTime();
        return Integer.toString((int) Math.abs(Math.cos(Math.toRadians(
                now.getYear() + now.getMonthOfYear() + now.getDayOfMonth()
        )) * 1000000));
    }

    public final String getPage(String pagenumber) throws Exception {
        final String xsession = getXsession();

        String page = getPageInternal(xsession, pagenumber);
        if (page.contains(INVALID_PAGE)) {
            return null;
        }

        return convertPage(page);
    }

    abstract protected String getPageInternal(String xsession, String s) throws Exception;

    abstract protected String convertPage(String page) throws UnsupportedEncodingException;
}
