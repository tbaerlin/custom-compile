/*
 * PublicProfileUserLoginMethod.java
 *
 * Created on 16.11.12 15:38
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.iview.mmgwt.mmweb.client.UserRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.UserResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppProfile;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Markus Dick
 */
public class PublicProfileUserLoginMethod extends AbstractUserLoginMethod {
    private final Log logger = LogFactory.getLog(getClass());

    private static final String DEFAULT_VWD_ID = "-1";

    private static final String APP_ID_PARAMETER = "appId";
    private static final String AUTHENTICATION_TYPE_PARAMETER = "authenticationType";

    private static final AppProfile EMPTY_APP_PROFILE = new AppProfile();
    private static final AppConfig EMPTY_APP_CONFIG = new AppConfig();

    PublicProfileUserLoginMethod(UserServiceImpl service, UserRequest request) {
        super(service, request);
    }

    @Override
    UserResponse invoke() {
        final HttpServletRequest httpServletRequest = ServletRequestHolder.getHttpServletRequest();

        final User user = new User();
        final String login = this.userRequest.getLogin();

        user.setLogin(login);
        user.setAppProfile(EMPTY_APP_PROFILE);
        user.setAppConfig(EMPTY_APP_CONFIG);
        user.setVwdId(DEFAULT_VWD_ID); //Set default vwdId. Possibly reset to a real vwdId (see below).

        try {
            final Zone zone = this.service.getZone(httpServletRequest);
            final Map<String, String[]> zoneParameterMap = zone.getParameterMap(new HashMap<String, String[]>(), "");

            final String appId = getZoneParameter(zoneParameterMap, APP_ID_PARAMETER);
            final String authenticationType = getZoneParameter(zoneParameterMap, AUTHENTICATION_TYPE_PARAMETER);

            user.setAppId(appId);

            final Profile profile = this.service.getProfile(new ProfileRequest(authenticationType, login));

            final AppProfile appProfile = toAppProfile(profile);
            user.setAppProfile(appProfile);

            if(profile instanceof VwdProfile) {
                final VwdProfile vwdProfile = (VwdProfile)profile;
                user.setVwdId(vwdProfile.getVwdId());
            }
        }
        catch(Exception e) {
            this.logger.warn("Initialization of Login: \"" + login + "\" for public zone failed."
                    + " Proceeding with user and profile as is.", e);
        }

        return new UserResponse().withUser(user).withState(UserResponse.State.OK);
    }

    private String getZoneParameter(Map<String, String[]>zoneParameterMap, String parameterName) {
        final String[] value = zoneParameterMap.get(parameterName);

        if(value != null && value.length > 0) {
            return value[0];
        }

        return null;
    }
}
