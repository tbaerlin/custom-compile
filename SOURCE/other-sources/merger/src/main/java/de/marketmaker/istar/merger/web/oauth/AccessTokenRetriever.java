package de.marketmaker.istar.merger.web.oauth;

import com.github.scribejava.core.model.OAuth2AccessToken;
import javax.annotation.Nullable;

/**
 * @author zzhao
 */
public interface AccessTokenRetriever {

  @Nullable
  OAuth2AccessToken retrieve(ClientOAuthSettings settings);
}
