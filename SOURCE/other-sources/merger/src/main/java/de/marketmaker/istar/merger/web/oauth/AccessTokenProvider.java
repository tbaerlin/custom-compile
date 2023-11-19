package de.marketmaker.istar.merger.web.oauth;

import java.util.Optional;

/**
 * Given a client id returns optional access token. If the optional access token is not empty, it is
 * ensured to be valid.
 *
 * Note: only support Core Data API for now.
 *
 * @author zzhao
 */
public interface AccessTokenProvider {

  /**
   * Returns optional access token. Only supports Core Data API for now.
   *
   * @param clientId a client id, ignored for now because it only supports Core Data API
   * @return optional access token
   */
  Optional<String> getToken(String clientId);
}
