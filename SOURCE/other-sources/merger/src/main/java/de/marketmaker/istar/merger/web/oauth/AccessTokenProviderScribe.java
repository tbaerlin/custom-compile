package de.marketmaker.istar.merger.web.oauth;

import com.github.scribejava.core.model.OAuth2AccessToken;
import de.marketmaker.istar.common.util.concurrent.InMemoryCache;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Given a client id, provides access token to invoke OAuth protected services.
 * <p>
 * The first token is queried upon the first time it is required. If it fails to acquire a token
 * retry will be made in an interval of 5 minutes (default, can be configured). If a token is
 * acquired successfully, it will be returned and a refresh action is scheduled to refresh the
 * access token before it expires (default 5 minutes before the expiration).
 * </p>
 * <p>
 * Currently only support Core Data API.
 * </p>
 *
 * @author zzhao
 */
@Slf4j
public class AccessTokenProviderScribe
    implements AccessTokenProvider, InitializingBean, DisposableBean {

  public static final int DEFAULT_REFRESH_SECONDS_BEFORE_EXPIRATION = 5 * 60;

  private final ClientOAuthSettings oauthSettings;

  private final AccessTokenRetriever accessTokenRetriever;

  private final ScheduledExecutorService ses;

  private final AtomicReference<OAuth2AccessToken> accessTokenRef = new AtomicReference<>();
  private final AtomicBoolean refreshScheduled = new AtomicBoolean(false);
  private long refreshSecondsBeforeExpiration = DEFAULT_REFRESH_SECONDS_BEFORE_EXPIRATION;
  private InMemoryCache<String, OAuth2AccessToken> accessTokenCache;
  private volatile ScheduledFuture<?> scheduledFuture;

  public AccessTokenProviderScribe(ClientOAuthSettings oauthSettings,
      AccessTokenRetriever accessTokenRetriever, ScheduledExecutorService ses) {
    this.oauthSettings = oauthSettings;
    this.accessTokenRetriever = accessTokenRetriever;
    this.ses = ses;
  }

  public AccessTokenProviderScribe(ClientOAuthSettings oauthSettings,
      ScheduledExecutorService ses) {
    this(oauthSettings, new AccessTokenRetrieverKeyCloak(), ses);
  }

  @Override
  public Optional<String> getToken(String clientId) {
    if (clientId == null) {
      throw new IllegalArgumentException("client id must not be null");
    }
    OAuth2AccessToken token = this.accessTokenRef.get();
    if (token == null) {
      token = this.accessTokenCache.get(clientId);
    }
    return Optional.ofNullable(token == null ? null : token.getAccessToken());
  }

  public void setRefreshSecondsBeforeExpiration(int refreshSecondsBeforeExpiration) {
    this.refreshSecondsBeforeExpiration = refreshSecondsBeforeExpiration;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.accessTokenCache = new InMemoryCache<>(clientId -> {
      final ClientOAuthSettings settings = this.oauthSettings;
      if (settings == null) {
        throw new IllegalStateException("no oauth settings for client id: " + clientId);
      }

      final OAuth2AccessToken oAuth = this.accessTokenRetriever.retrieve(settings);
      this.accessTokenRef.set(oAuth);

      if (oAuth != null && oAuth.getExpiresIn() <= this.refreshSecondsBeforeExpiration) {
        throw new IllegalStateException("oauth token expiration smaller than: "
            + this.refreshSecondsBeforeExpiration + " seconds");
      }

      scheduleRefresh(oAuth != null
              ? oAuth.getExpiresIn() - this.refreshSecondsBeforeExpiration
              : this.refreshSecondsBeforeExpiration,
          clientId);
      return oAuth;
    });
  }

  private void scheduleRefresh(long delayInSeconds, String clientId) {
    if (!this.refreshScheduled.compareAndSet(false, true)) {
      log.warn("<scheduleRefresh> another refresh task already scheduled, ignore this one");
      return;
    }

    log.info("<scheduleRefresh> for {} in {} seconds", clientId, delayInSeconds);
    this.scheduledFuture = this.ses.schedule(() -> {
      this.accessTokenCache.remove(clientId);
      this.refreshScheduled.set(false);
      this.accessTokenCache.get(clientId);
    }, delayInSeconds, TimeUnit.SECONDS);
  }

  @Override
  public void destroy() throws Exception {
    if (this.scheduledFuture != null) {
      this.scheduledFuture.cancel(true);
      this.scheduledFuture = null;
    }
  }
}
