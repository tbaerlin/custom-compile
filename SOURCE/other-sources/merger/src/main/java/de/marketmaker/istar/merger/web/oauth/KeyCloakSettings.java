package de.marketmaker.istar.merger.web.oauth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author zzhao
 */
@Getter
@Setter
@ToString
public class KeyCloakSettings {

  private String baseUrl = "https://idm.cloud-test.eu.infrontservices.com";

  private String realm = "vwd";

  private String scope = "openid";
}
