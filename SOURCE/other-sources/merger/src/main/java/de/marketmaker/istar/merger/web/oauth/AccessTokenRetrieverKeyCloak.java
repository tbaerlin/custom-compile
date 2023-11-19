package de.marketmaker.istar.merger.web.oauth;

import com.github.scribejava.apis.KeycloakApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zzhao
 */
@Slf4j
public class AccessTokenRetrieverKeyCloak implements AccessTokenRetriever {

  @Override
  public OAuth2AccessToken retrieve(ClientOAuthSettings settings) {
    final KeyCloakSettings keyCloakSettings = settings.getKeyCloakSettings();
    final OAuth20Service service = new ServiceBuilder(settings.getClientId())
        .apiSecret(settings.getClientSecret())
        .defaultScope(keyCloakSettings.getScope())
        .build(KeycloakApi.instance(keyCloakSettings.getBaseUrl(), keyCloakSettings.getRealm()));
    try {
      return service.getAccessTokenClientCredentialsGrant();
    } catch (Throwable t) {
      log.error("<getOAuth> failed for {}", settings, t);
      return null;
    }
  }
}
