/*
 * HttpClientFactoryBean.java
 *
 * Created on 15.01.2009 11:25:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.http;

import java.util.concurrent.TimeUnit;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Factory for an HttpClient with a PoolingHttpClientConnectionManager.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class HttpClientFactoryBean implements FactoryBean<HttpClient>, InitializingBean,
    DisposableBean {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private final RequestConfig.Builder requestConfig = RequestConfig.custom();

  private final HttpClientBuilder builder = HttpClientBuilder.create();

  private CloseableHttpClient httpClient;

  public void setDefaultMaxConnectionsPerHost(int i) {
    this.builder.setMaxConnPerRoute(i);
  }

  public void setMaxTotalConnections(int i) {
    this.builder.setMaxConnTotal(i);
  }

  public void setSoTimeout(int i) {
    this.requestConfig.setSocketTimeout(i);
  }

  public void setConnectionTimeout(int i) {
    this.requestConfig.setConnectionRequestTimeout(i);
  }

  public void setRequestSentRetryEnabled(boolean requestSentRetryEnabled) {
    this.builder.setRetryHandler(new StandardHttpRequestRetryHandler(1, requestSentRetryEnabled));
  }

  public void setIdleConnectionTimeout(int idleConnectionTimeout) {
    this.builder.evictIdleConnections(idleConnectionTimeout, TimeUnit.MILLISECONDS);
  }

  public void setUserAgent(String userAgent) {
    this.builder.setUserAgent(userAgent);
  }

  /**
   * See comment of HttpComponentsMessageSender(HttpClient).
   *
   * @param interceptor an interceptor, but as method name suggested, should be
   * RemoveSoapHeadersInterceptor. No check is done because we don't want to have dependency in
   * istar-common to spring-ws
   */
  public void setRemoveSoapHeadersInterceptor(HttpRequestInterceptor interceptor) {
    this.builder.addInterceptorFirst(interceptor);
  }

  /**
   * There is no redirect strategy set per default, see also RFC 2616 10.3.3 302 Found.
   *
   * @param redirectStrategy an instance of redirect strategy.
   */
  public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
    this.builder.setRedirectStrategy(redirectStrategy);
  }

  public void afterPropertiesSet() throws Exception {
    this.builder.setDefaultRequestConfig(this.requestConfig.build());
    this.httpClient = this.builder.build();
  }

  public HttpClient getObject() throws Exception {
    return this.httpClient;
  }

  public Class<? extends HttpClient> getObjectType() {
    return CloseableHttpClient.class;
  }

  public boolean isSingleton() {
    return true;
  }

  @Override
  public void destroy() throws Exception {
    this.httpClient.close();
  }
}
