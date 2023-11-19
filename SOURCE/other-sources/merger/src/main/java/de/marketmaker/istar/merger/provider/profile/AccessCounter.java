/*
 * DZProfileSource.java
 *
 * Created on 30.06.2008 11:35:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import de.marketmaker.istar.common.http.RestTemplateFactory;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.profile.ProfileAdapter;
import de.marketmaker.istar.domainimpl.profile.ProfileAdapterException;
import de.marketmaker.istar.domainimpl.profile.VwdProfileAdapterFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AccessCounter implements CounterService, DisposableBean {
    private static final String COUNT_URI = "CounterIncrement_ByCustomerUserIdent";

    private static final String READ_URI = "ReadAccount_ByCustomerUser";

    private static final String COUNT_LOGIN_URI = "CounterIncrement_ByMandantIdLoginIdent";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String defaultAppId = "50";

    private String baseUri = "http://vwd-ent:1969/vwdCounter.asmx/";

    private RestTemplate restTemplate;

    // TODO: using this es is a HACK so that client threads don't have to wait for the result
    // of methods that are actually one-way methods. Using amqp's one way methods would
    // do that out of the box.
    private final ExecutorService es = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(100), r -> {
        return new Thread(r, ClassUtils.getShortName(AccessCounter.this.getClass()));
    });

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void destroy() throws Exception {
        this.es.shutdownNow();
    }

    public void setDefaultAppId(String defaultAppId) {
        this.defaultAppId = defaultAppId;
    }

    @Override
    public <T> T countAccess(String customer, String user, String selector,
            String quality, ResponseExtractor<T> handler) {
        return countAccess(this.defaultAppId, customer, user, selector, quality, handler);
    }

    @Override
    public <T> T countAccess(String appId, String customer, String user,
            String ident, String quality, ResponseExtractor<T> handler) {

        if (!StringUtils.hasText(customer)) {
            this.logger.warn("<countAccess> no customer '" + customer + " => no counting");
            return null;
        }

        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(this.baseUri + COUNT_URI)
                .queryParam("AppId", appId)
                .queryParam("Customer", customer)
                .queryParam("User", user)
                .queryParam("Ident", ident)
                .queryParam("Qlt", quality)
                .queryParam("RequestId", "1"); // we don't need this, but the service does

        return doCountAccess(b.build(), handler);
    }

    protected <T> T doCountAccess(UriComponents uc, ResponseExtractor<T> handler) {
        final TimeTaker tt = new TimeTaker();
        URI uri = uc.toUri();
        try {
            T result = restTemplate.execute(uri, HttpMethod.GET, null, handler);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<countAccess> " + uc.getQuery() + " took " + tt);
            }
            return result;
        } catch (RestClientException e) {
            if (e.getCause() instanceof ProfileAdapterException) {
                this.logger.warn("<doCountAccess> no ProfileAdapter for " + uri + ": "
                        + e.getCause().getMessage());
            }
            else {
                this.logger.error("<countAccess> failed for " + uri, e);
            }
        }
        return null;
    }

    public <T> T readAccount(String customer, String user, ResponseExtractor<T> handler) {

        final String appId = this.defaultAppId;

        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(this.baseUri + READ_URI)
                .queryParam("AppId", appId)
                .queryParam("Customer", customer)
                .queryParam("User", user)
                .queryParam("RequestId", "1"); // we don't need this, but the service does
        UriComponents uc = b.build();

        final TimeTaker tt = new TimeTaker();
        try {
            final T result = this.restTemplate.execute(uc.toUri(), HttpMethod.GET, null, handler);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<readAccount> " + uc.getQuery() + " took " + tt);
            }
            return result;
        } catch (Exception e) {
            this.logger.error("<readAccount> failed for " + uc.toUri(), e);
            return null;
        }
    }

    @Deprecated
    public void countLogin(String customer, String login) {
        countLogin(this.defaultAppId, customer, login);
    }

    @Deprecated
    public void countLogout(String customer, String login) {
        countLogout(this.defaultAppId, customer, login);
    }

    public void countLogout(final String appId, final String customer, final String login) {
        countLoginOrLogout(appId, customer, login, true);
    }

    public void countLogin(final String appId, final String customer, final String login) {
        countLoginOrLogout(appId, customer, login, false);
    }

    private Future<Boolean> countLoginOrLogout(final String appId, final String customer,
            final String login,
            boolean logout) {

        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(this.baseUri + COUNT_LOGIN_URI)
                .queryParam("AppId", appId)
                .queryParam("MandantId", customer)
                .queryParam("Login", login)
                .queryParam("Ident", logout ? "2293" : "2110")
                .queryParam("Qlt", "4")
                .queryParam("RequestId", "1");

        final URI uri = b.build().toUri();

        final String method = logout ? "countLogout" : "countLogin";

        try {
            return this.es.submit(() -> {
                try {
                    Boolean result = restTemplate.execute(uri, HttpMethod.GET, null, response ->
                            response.getStatusCode() == HttpStatus.OK);
                    if (logger.isDebugEnabled()) {
                        logger.debug(method + " for " + customer + ":" + defaultAppId + ":" + login);
                    }
                    return result;
                } catch (HttpStatusCodeException e) {
                    logger.warn(method + " failed for " + uri + ": " + e.getStatusCode().value()
                            + " / " + e.getStatusText());
                } catch (ResourceAccessException e) {
                    logger.warn(method + " failed for " + uri, e);
                } catch (Throwable e) {
                    logger.error(method + " failed for " + uri, e);
                }
                return Boolean.FALSE;
            });
        } catch (RejectedExecutionException e) {
            this.logger.error(method + " rejected!");
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        RestTemplateFactory rtf = new RestTemplateFactory();
        RestTemplate rt = rtf.getObject();

        final AccessCounter counter = new AccessCounter();
        counter.setRestTemplate(rt);
        counter.setDefaultAppId("7");

        Future<?> f = counter.countLoginOrLogout("7", "10", "flegeoli", true);
        System.out.println(f.get());

        // --------------------------------------------------
        // as in GisEbrokeragePrices
        // --------------------------------------------------
        final VwdProfileAdapterFactory factory = new VwdProfileAdapterFactory();

        final ResponseExtractor<ProfileAdapter> profileHandler
                = response -> {
            try {
                return factory.read(response.getBody());
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        };

        final ProfileAdapter adapter = counter.countAccess("bank2", "thomas2", "390", "4", profileHandler);
        System.out.println(adapter);
        // --------------------------------------------------

        counter.destroy();
        rtf.destroy();
    }
}
