/*
 * RestTemplateFactory.java
 *
 * Created on 13.11.14 11:30
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.istar.common.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Factory bean for a {@link org.springframework.web.client.RestTemplate} which configures the
 * {@link org.apache.http.client.HttpClient} with a
 * {@link org.apache.http.impl.conn.PoolingHttpClientConnectionManager}.
 * @author oflege
 */
public class RestTemplateFactory implements FactoryBean<RestTemplate>, InitializingBean,
        DisposableBean {

    private static HttpHost asHttpHost(String spec) {
        if (spec.endsWith("/")) {
            return asHttpHost(spec.substring(0, spec.length() - 1));
        }
        String[] tokens = spec.split(":");
        String host;
        int port = 80;
        String scheme = null;
        if (tokens.length == 3 || tokens[0].startsWith("http")) {
            scheme = tokens[0];
            host = tokens[1].startsWith("//") ? tokens[1].substring(2) : tokens[1];
            port = (tokens.length == 3) ? Integer.parseInt(tokens[2]) : ("http".equals(scheme) ? 80 : 443);
        }
        else {
            host = tokens[0];
            if (tokens.length == 2) {
                port = Integer.parseInt(tokens[1]);
            }
        }
        return new HttpHost(host, port, scheme);
    }

    private static final AtomicInteger TIMER_COUNT = new AtomicInteger();

    private Timer timer;

    private final RestTemplate bean;

    private int connectTimeout = 2000;

    private int maxRedirects = 2;

    private int retryCount = 1;

    private int maxTotalConnections = 10;

    private int maxConnectionsPerRoute = 5;

    private int defaultSoTimeout = 5000;

    private int defaultSocketTimeout = 5000;

    private int connectionRequestTimeout = 1000;

    private int evictionPeriod = 5000;

    private HttpHost proxy;

    private CredentialsProvider credentialsProvider;

    private CloseableHttpClient client;

    private ProxyAuthenticationStrategy proxyAuthStrategy;

    private CookieStore cookieStore;

    private Map<String, String> maxPerRoute = new HashMap<>();

    private String userAgent;

    private boolean trustSelfSignedCert;

    private boolean verifySSLHostname = true;

    public RestTemplateFactory() {
        final List<HttpMessageConverter<?>> converters = new ArrayList<>();

        // default converters copied from RestTemplate default constructor with adaptation for the
        // StringHttpMessageConverter
        converters.add(new ByteArrayHttpMessageConverter());

        // UTF-8 string message converter for certain media types (e.g., those used by dm[xml])
        // application/json is used for writing json requests,
        // text/xml;charset=UTF-8 is the media type of the dm[xml] response
        final StringHttpMessageConverter stringHttpMessageConverterUtf8
                = new StringHttpMessageConverter(Consts.UTF_8);
        stringHttpMessageConverterUtf8.setSupportedMediaTypes(
                MediaType.parseMediaTypes("application/json,text/xml;charset=UTF-8"));
        // avoid to write all charsets known to java as Accept-Charset
        stringHttpMessageConverterUtf8.setWriteAcceptCharset(false);
        converters.add(stringHttpMessageConverterUtf8);

        // string message converter with default encoding (DZ Pages, etc)
        final StringHttpMessageConverter stringHttpMessageConverter
                = new StringHttpMessageConverter();
        stringHttpMessageConverter.setWriteAcceptCharset(false);
        converters.add(stringHttpMessageConverter);

        converters.add(new ResourceHttpMessageConverter());
        converters.add(new SourceHttpMessageConverter<>());
        converters.add(new AllEncompassingFormHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter());

        this.bean = new RestTemplate(converters);
    }

    public void setVerifySSLHostname(boolean verifySSLHostname) {
        this.verifySSLHostname = verifySSLHostname;
    }

    public void setTrustSelfSignedCert(boolean trustSelfSignedCert) {
        this.trustSelfSignedCert = trustSelfSignedCert;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @SuppressWarnings("unused")
    public void setEvictionPeriod(int evictionPeriod) {
        this.evictionPeriod = evictionPeriod;
    }

    @SuppressWarnings("unused")
    public void setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    public void setDefaultSoTimeout(int defaultSoTimeout) {
        this.defaultSoTimeout = defaultSoTimeout;
    }

    public void setDefaultSocketTimeout(int defaultSocketTimeout) {
        this.defaultSocketTimeout = defaultSocketTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    @SuppressWarnings("unused")
    public void setCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    @SuppressWarnings("unused")
    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public void setProxy(HttpHost proxy) {
        this.proxy = proxy;
    }

    @SuppressWarnings("unused")
    public void setProxyAuthStrategy(ProxyAuthenticationStrategy proxyAuthStrategy) {
        this.proxyAuthStrategy = proxyAuthStrategy;
    }

    @SuppressWarnings("unused")
    public void setMaxPerRoute(Map<String, String> maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
    }

    @SuppressWarnings("unused")
    public void setCredentials(Map<AuthScope, Credentials> credentials) {
        this.credentialsProvider = new BasicCredentialsProvider();
        for (Map.Entry<AuthScope, Credentials> e : credentials.entrySet()) {
            this.credentialsProvider.setCredentials(e.getKey(), e.getValue());
        }
    }

    @SuppressWarnings("unused")
    public void addCredentials(AuthScope authScope, Credentials credentials) {
        if(credentialsProvider == null) {
            this.credentialsProvider = new BasicCredentialsProvider();
        }
        this.credentialsProvider.setCredentials(authScope, credentials);
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public RestTemplate getObject() {
        if (this.client == null) {
            afterPropertiesSet();
        }
        return this.bean;
    }

    @Override
    public Class<?> getObjectType() {
        return RestTemplate.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        this.client.close();
    }

    @Override
    public void afterPropertiesSet() {
        final PoolingHttpClientConnectionManager connectionManager = createConnectionManager();
        final HttpClientBuilder builder = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultSocketConfig(SocketConfig.custom()
                .setSoTimeout(defaultSoTimeout)
                .build())
            .setRetryHandler(new StandardHttpRequestRetryHandler(retryCount, true))
            .setDefaultRequestConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setConnectTimeout(connectTimeout)
                .setMaxRedirects(maxRedirects)
                .setCircularRedirectsAllowed(false)
                .setSocketTimeout(defaultSocketTimeout)
                .build());

        if (this.userAgent != null) {
            builder.setUserAgent(this.userAgent);
        }
        if (this.proxy != null) {
            builder.setProxy(this.proxy);
        }
        if (this.credentialsProvider != null) {
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }
        if (this.cookieStore != null) {
            builder.setDefaultCookieStore(this.cookieStore);
        }
        if (this.proxyAuthStrategy != null) {
            builder.setProxyAuthenticationStrategy(proxyAuthStrategy);
        }

        this.client = builder.build();

        this.bean.setRequestFactory(new HttpComponentsClientHttpRequestFactory(client) /*{
            @Override
            protected void postProcessHttpRequest(HttpUriRequest request) {
                // TODO possibly schedule request.abort()
            }
        }*/);

        if (this.evictionPeriod > 0) {
            initTimer(connectionManager);
        }
    }

    private PoolingHttpClientConnectionManager createConnectionManager() {
        final PoolingHttpClientConnectionManager cm =
            new PoolingHttpClientConnectionManager(getRegistry());
        cm.setMaxTotal(this.maxTotalConnections);
        cm.setDefaultMaxPerRoute(this.maxConnectionsPerRoute);
        cm.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(defaultSoTimeout).build());

        for (Map.Entry<String, String> e : maxPerRoute.entrySet()) {
            cm.setMaxPerRoute(new HttpRoute(asHttpHost(e.getKey())), Integer.parseInt(e.getValue()));
        }
        return cm;
    }

    private Registry<ConnectionSocketFactory> getRegistry() {
        final SSLConnectionSocketFactory sslConnSoFac;
        if (this.trustSelfSignedCert || this.verifySSLHostname) {
            final HostnameVerifier hostnameVerifier = this.verifySSLHostname
                ? new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault())
                : NoopHostnameVerifier.INSTANCE;

            sslConnSoFac = new SSLConnectionSocketFactory(getSSLContext(), hostnameVerifier);
        } else {
            sslConnSoFac = SSLConnectionSocketFactory.getSocketFactory();
        }
        return RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", sslConnSoFac)
            .build();
    }

    private SSLContext getSSLContext() {
        try {
            return this.trustSelfSignedCert
                ? new SSLContextBuilder()
                .loadTrustMaterial(null, new TrustSelfSignedStrategy()).build()
                : SSLContext.getDefault();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void initTimer(final HttpClientConnectionManager cm) {
        this.timer = new Timer("RestTemplateConnMgmt-" + TIMER_COUNT.incrementAndGet(), true);
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cm.closeExpiredConnections();
                cm.closeIdleConnections(30, TimeUnit.SECONDS);
            }
        }, this.evictionPeriod, this.evictionPeriod);
    }
}
