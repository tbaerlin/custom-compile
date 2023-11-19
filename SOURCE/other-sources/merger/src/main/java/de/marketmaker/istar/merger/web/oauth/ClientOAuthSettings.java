package de.marketmaker.istar.merger.web.oauth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author zzhao
 */
@Getter
@Setter
@ToString(of = {"keyCloakSettings", "clientId"})
public class ClientOAuthSettings {

  private KeyCloakSettings keyCloakSettings;

  private String clientId;

  private String clientSecret;
}
