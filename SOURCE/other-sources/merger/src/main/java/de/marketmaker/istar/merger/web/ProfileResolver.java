/*
 * ProfileResolver.java
 *
 * Created on 29.03.2007 08:53:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.Ordered;

import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ProfileResolver extends Ordered {
    String CREDENTIAL_KEY = "credential";

    String KEY_KEY = "key";

    String AUTHENTICATION_KEY = "authentication";

    String PM_AUTHENTICATION_KEY = "mmauth";

    String AUTHENTICATION_TYPE_KEY = "authenticationType";

    String ALLOWED_AUTHENTICATIONS_KEY = "allowedAuthentications";

    String CLIENT_ID_KEY = "clientId";

    String APPLICATION_ID_KEY = "appId";

    String ROOT_AUTHENTICATION_TYPE = "r(oo)t";

    Profile resolveProfile(HttpServletRequest request);
}
