package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.PmWebProfile;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataResponse;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import de.marketmaker.itools.pmxml.frontend.PmxmlHash;
import de.marketmaker.iview.mmgwt.mmweb.client.ChangePasswordResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.UserRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.UserResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.data.MessageOfTheDay;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.BooleanFeature;
import de.marketmaker.iview.pmxml.ChangePasswordRequest;
import de.marketmaker.iview.pmxml.FeatureGroup;
import de.marketmaker.iview.pmxml.FeaturesMisc;
import de.marketmaker.iview.pmxml.FeaturesResponse;
import de.marketmaker.iview.pmxml.FeaturesServer;
import de.marketmaker.iview.pmxml.GetEnvironmentResponse;
import de.marketmaker.iview.pmxml.LoginRequest;
import de.marketmaker.iview.pmxml.LoginResponse;
import de.marketmaker.iview.pmxml.NetRegIniFile;
import de.marketmaker.iview.pmxml.PasswordState;
import de.marketmaker.iview.pmxml.VersionConfig;
import de.marketmaker.iview.pmxml.VoidRequest;
import org.springframework.remoting.RemoteLookupFailureException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created on 17.08.12 17:22
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 * @author Markus Dick
 */

public class PmUserServiceImpl extends UserServiceImpl {

    private static final String PM_LICENSE = "3B80E3BB-D73A-4030-97FA-8DFBD4B46DE8-vwdkl-2012022";

    private PmxmlHandler pmxmlHandlerPm;

    public void setPmxmlHandlerPm(PmxmlHandler pmxmlHandlerPm) {
        this.pmxmlHandlerPm = pmxmlHandlerPm;
    }

    @Override
    public Profile getProfileByLogin(String login, ClientConfig config) {
        final Profile profile = super.getProfileByVwdId(login, config.getAppId());
        if (!(profile instanceof VwdProfile)) {
            throw new IllegalStateException("profile must be a instanceof VwdProfile");
        }
        return getPmWebProfile(login, (VwdProfile) profile);
    }

    public Profile getPmWebProfile(String login, VwdProfile profile) {
        final Map<String, Boolean> features;
        try {
            final FeaturesResponse featuresResponse = this.pmxmlHandlerPm.exchangeData(
                    new VoidRequest(), "um_getfeatures", FeaturesResponse.class
            );

            final FeaturesServer featuresServer = featuresResponse.getFeaturesServer();
            Class serverFeaturClass = featuresServer.getClass();
            features = Arrays.asList(serverFeaturClass.getDeclaredMethods()).stream()
                    .filter(m -> m.getName().startsWith("get") && m.getReturnType() == BooleanFeature.class)
                    .collect(Collectors.toMap((Method m) -> "SERVER." + m.getName().substring(3).toUpperCase(),
                            getValueMapper(featuresServer)
                    ));

            final FeaturesMisc featuresMisc = featuresResponse.getFeaturesMisc();
            Class miscFeaturClass = featuresMisc.getClass();
            features.putAll(
                    Arrays.asList(miscFeaturClass.getDeclaredMethods()).stream()
                            .filter(m -> m.getName().startsWith("get"))
                            .collect(Collectors.toMap((Method m) -> "MISC." + m.getName().substring(3).toUpperCase(),
                                    getValueMapper(featuresMisc)
                            ))
            );
        }
        catch (PmxmlException e) {
            throw new IllegalStateException("getting features failed!", e);
        }
        return new PmWebProfile(login, PM_LICENSE, profile, features);
    }

    private Function<Method, Boolean> getValueMapper(FeatureGroup featureGroup) {
        return (Method m) -> {
            try {
                BooleanFeature bf = (BooleanFeature) m.invoke(featureGroup);
                return bf.isValue();
            }
            catch (Exception e) {
                logger.error("could not get feature flag for " + m.getName() + ". returning false");
                e.printStackTrace();
            }
            return false;
        };
    }

    @Override
    protected void countLogin(ClientConfig config, String login, HttpSession session) {
        // TODO do we want to count?
    }

    @Override
    public UserResponse login(UserRequest userRequest) {
        final HttpServletRequest request = ServletRequestHolder.getHttpServletRequest();
        this.logger.debug("(pm)login " + userRequest.getLogin() + " from " + request.getRemoteHost());
        return new PmUserLoginMethod(this, userRequest).invoke();
    }

    LoginResponse login(LoginRequest loginRequest) {
        loginRequest.setLicIsHashed(true);
        loginRequest.setLicense(PmxmlHash.toSHA256(PM_LICENSE));
        try {
            return this.pmxmlHandlerPm.exchangeData(loginRequest, "um_login", LoginResponse.class);
        }
        catch (Exception e) {
            this.logger.error("Login error! ", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean checkPassword(String pw, String hashedPw) {
        // password is checked by pm when the login method is called
        return true;
    }

    @Override
    public ChangePasswordResponse changePassword(String login, String pwOld, String pwNew, String moduleName) {
        final PasswordState passwordState = changePassword(pwOld, pwNew);
        switch (passwordState) {
            case PS_OK:
                return ChangePasswordResponse.OK;
            case PS_USED_IN_HISTORY:
                return ChangePasswordResponse.REPEATED_RECENT_PASSWORD;
            case PS_TOO_SHORT:
                return ChangePasswordResponse.PASSWORD_IS_TOO_SHORT;
            case PS_UNKNOWN_USER_OR_PASSWORD:
                return ChangePasswordResponse.UNKNOWN_USER_OR_PASSWORD;
            default:
                return ChangePasswordResponse.INTERNAL_ERROR;
        }
    }

    private PasswordState changePassword(String oldPwd, String newPwd) {
        final ChangePasswordRequest cpr = new ChangePasswordRequest();
        cpr.setOldPassword(oldPwd == null ? "" : oldPwd);
        cpr.setNewPassword(newPwd == null ? "" : newPwd);

        final de.marketmaker.iview.pmxml.ChangePasswordResponse response;
        try {
            response = this.pmxmlHandlerPm.exchangeData(cpr, "UM_ChangePassword", de.marketmaker.iview.pmxml.ChangePasswordResponse.class);
        }
        catch (PmxmlException e) {
            throw new IllegalStateException(e);
        }
        return response.getState();
    }

    public String requestPasswordReset(String login, String module, String locale) {
        return "nothing done"; // $NON-NLS$
    }

    @Override
    protected String getEmailAddress(UserMasterDataResponse data) {
        // TODO, can we get this from pm?
        return null;
    }

    public MessageOfTheDay getMessageOfTheDay() {
        return null;
    }

    public String getMessageOfTheDayByDate() {
        return null;
    }

    public void setMessageOfTheDay(MessageOfTheDay motd) {
        //not implemented!
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // nothing to do
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        this.logger.info("HTTP- or PM-Session destroyed: " + (se.getSession() != null ? se.getSession().getId() : "null"));
    }

    @Override
    public void logout() {
        final HttpServletRequest gwtRequest = ServletRequestHolder.getHttpServletRequest();
        if (gwtRequest != null && !(gwtRequest instanceof MyHttpServletRequest)) {
            final HttpSession session = gwtRequest.getSession(false);
            if (session != null) {
                pmLogout(session);
            }
        }
        super.logout();
    }

    private void pmLogout(HttpSession httpSession) {
        final String pmAuthToken = (String) httpSession.getAttribute(ProfileResolver.PM_AUTHENTICATION_KEY);
        if (!StringUtil.hasText(pmAuthToken)) {
            this.logger.warn("could not call UM_Logout because of missing pmAuthToken!");
            return;
        }
        try {
            this.logger.info("Calling UM_Logout");
            this.pmxmlHandlerPm.exchangeData(new VoidRequest(), "UM_Logout");
        }
        catch (PmxmlException e) {
            this.logger.error("pm logout call failed!", e);
        }
    }

    @Override
    protected boolean checkSuPassword(String pw) {
        return pw.contains("!") && super.checkPassword(pw.substring(0, pw.indexOf("!") + 1), HASHED_PW);
    }


    @Override
    public Map<String, String> getEnvInfo() {
        final Map<String, String> params = super.getEnvInfo();
        final GetEnvironmentResponse envResponse;
        try {
            envResponse = PmEnv.getEnvResponse(this.pmxmlHandlerPm);
        }
        catch (RemoteLookupFailureException e) {
            final String error = params.get("error");
            params.put("error", e.getMessage() + "\n" + error);
            return params;
        }
        putVersionConfig(params, "pmVersion", envResponse.getMain());
        putVersionConfig(params, "psiVersion", envResponse.getPsi());
        putVersionConfig(params, "asVersion", envResponse.getWebgui());

        final NetRegIniFile netRegIni = envResponse.getNetRegIni();
        if (netRegIni != null) {
            params.put("sharedEnvName", netRegIni.getSharedEnvName());
            params.put("sharedEnvColorSchema", Boolean.toString(netRegIni.isSharedEnvColorSchema()));
        }

        final String mail = envResponse.getCSCDEMail();
        final String fax = envResponse.getCSCDFax();
        final String name = envResponse.getCSCDName();
        final String phone = envResponse.getCSCDPhone();
        if (StringUtil.hasText(mail) || StringUtil.hasText(fax) || StringUtil.hasText(name) || StringUtil.hasText(phone)) {
            params.put("serviceEMail", mail);
            params.put("serviceFax", fax);
            params.put("serviceName", name);
            params.put("servicePhone", phone);
        }
        return params;
    }

    private void putVersionConfig(Map<String, String> params, String key, VersionConfig versionConfig) {
        if (versionConfig == null) {
            this.logger.warn("VersionConfig of " + key + " is not available");
            params.put(key, "n/a");
        }
        else {
            params.put(key, versionConfig.getCompleteVersion());
        }
    }
}