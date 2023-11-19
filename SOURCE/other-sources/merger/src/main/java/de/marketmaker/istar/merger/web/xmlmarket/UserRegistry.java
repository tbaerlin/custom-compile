package de.marketmaker.istar.merger.web.xmlmarket;


import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;


public class UserRegistry {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String NAME = "uname";

    public static final String TOKEN = "ukey";

    private static final Map<String, ProfileRequest> USER_2_PROFILE = new HashMap<String, ProfileRequest>() {{
        put("aab-nt", createProfileRequest("vwd-ent:ByVwdId", "120575", "34", null));
    }};

    private static ProfileRequest createProfileRequest(final String authenticationType,
                                                       String authentication, String appId, String clientId) {
        final ProfileRequest profileRequest = new ProfileRequest(authenticationType, authentication);
        if (appId != null) {
            profileRequest.setApplicationId(appId);
        }
        if (clientId != null) {
            profileRequest.setClientId(clientId);
        }
        return profileRequest;
    }

    private ProfileProvider profileProvider;

    public void setProfileProvider(ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    Profile resolveProfile(HttpServletRequest httpRequest) throws Exception {
        if (!isAuthenticated(httpRequest)) {
            throw new IllegalStateException("can't authenticate user");
        }

        final String name = httpRequest.getParameter(NAME);

        final Profile profile = resolveProfile(name);
        if (profile != null) {
            return profile;
        }

        throw new IllegalStateException("can't resolve profile");
    }

    private Profile resolveProfile(String name) throws Exception {
        final ProfileRequest profileRequest = USER_2_PROFILE.get(name);
        if (profileRequest == null) {
            return null;
        }
        final ProfileResponse response = this.profileProvider.getProfile(profileRequest);
        if (!response.isValid()) {
            return null;
        }
        return response.getProfile();
    }

    private boolean isAuthenticated(HttpServletRequest httpRequest) {
        final String name = httpRequest.getParameter(NAME);
        final String token = httpRequest.getParameter(TOKEN);

        final String[] parts;
        try {
            final String decrypted = CryptUkey.getInstance().decrypt(token);
            parts = decrypted.split(CryptUkey.UKEY_DELIMITER);
        } catch (Exception ex) {
            logger.warn("<isAuthenticated> decryption failed");
            return false;
        }

        if (parts.length != 3) {
            logger.warn("<isAuthenticated> invalid ukey");
            return false;
        }

        String uname = parts[0];
        if (uname == null) {
            logger.warn("<isAuthenticated> invalid ukey: uname is null");
            return false;
        }

        if (uname.equals(name)) {
            return true;
        }

        logger.warn("<isAuthenticated> authentication token doesn't match with username,"
                + " username in httpRequest: '" + name + "'"
                + " username in token: '" + uname + "'");
        return false;

    }
}
