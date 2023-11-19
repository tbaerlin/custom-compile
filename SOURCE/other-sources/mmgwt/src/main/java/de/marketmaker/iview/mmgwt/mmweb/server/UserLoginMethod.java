/*
 * UserLoginMethod.java
 *
 * Created on 10.12.2009 11:42:23
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.ProfileUtil;
import de.marketmaker.istar.domain.profile.UserMasterData;
import de.marketmaker.istar.domainimpl.profile.PmAboProfile;
import de.marketmaker.istar.domainimpl.profile.PmWebProfile;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.UserRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.UserResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppProfile;
import de.marketmaker.iview.mmgwt.mmweb.client.data.HttpBasicAuthCredentials;
import de.marketmaker.iview.mmgwt.mmweb.client.data.PmUser;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.server.statistics.UserStatsDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig.PROP_KEY_MUSTERDEPOT_USERID;
import static de.marketmaker.iview.mmgwt.mmweb.server.UserServiceImpl.*;

/**
 * Encapsulates all steps to be performed for login.
 *
 * @author oflege
 */
class UserLoginMethod extends AbstractUserLoginMethod {
    public static final String SESSION_KEY_SESSION_VALIDATOR = "sessionValidator"; //$NON-NLS$

    private static final String STATISTICS = "statistics";

    private static final char AUTH_PREFIX_SEP = '_';

    protected final Log logger = LogFactory.getLog(getClass());

    private LoginMode mode;

    private UserMasterData masterData;

    private User user;

    private ClientConfig config;

    private String authType = UserServiceImpl.VWD_ID_AUTH_TYPE;

    private static UserRequest resolve(UserRequest userRequest) {
        final HttpServletRequest request = ServletRequestHolder.getHttpServletRequest();
        if (request != null) {
            final HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute(UserServiceImpl.SESSION_KEY_PRE_LOGIN) != null) {
                final String preLogin = (String) session.getAttribute(UserServiceImpl.SESSION_KEY_PRE_LOGIN);
                final String prePassword = (String) session.getAttribute(UserServiceImpl.SESSION_KEY_PRE_PASSWORD);
                session.removeAttribute(UserServiceImpl.SESSION_KEY_PRE_LOGIN);
                session.removeAttribute(UserServiceImpl.SESSION_KEY_PRE_PASSWORD);
                return new UserRequest(userRequest.isPmZone(), preLogin, prePassword,
                        userRequest.getModule(), userRequest.getScreenInfo(), userRequest.getLocale());
            }
        }
        return userRequest;
    }

    UserLoginMethod(UserServiceImpl service, UserRequest userRequest) {
        super(service, resolve(userRequest));
    }

    UserLoginMethod withMode(LoginMode mode) {
        this.mode = mode;
        return this;
    }

    UserResponse invoke() {
        try {
            return login();
        }
        catch (Throwable t) {
            this.logger.error("<login> failed", t);
            return new UserResponse().withState(UserResponse.State.INTERNAL_ERROR);
        }
    }

    private UserResponse login() {
        initConfigAndMode();

        final UserResponse response = getUser();

        if (!responseOK(response)) {
            return response;
        }

        this.user = response.getUser();

        HttpSession session = null;

        final long then = System.currentTimeMillis();

        if (this.config.isWithSession()) {
            session = getSession();
            addSessionParamsToUser(session);
        }

        if (isUserMode() && session != null) {
            boolean sessionCreated = session.getCreationTime() >= then;
            boolean moduleChanged = false;

            final String moduleName = (String) session.getAttribute(UserServiceImpl.SESSION_KEY_MODULE);
            if (moduleName != null && !moduleName.equals(config.getModuleName())) {
                // user changed module, count logout from previous module first
                this.service.countLogout(session);
                moduleChanged = true;
            }

            if (moduleChanged || sessionCreated) {
                this.service.countLogin(this.config, this.user.getLogin(), session);
            }
        }

        return response;
    }

    protected void initConfigAndMode() {
        if (this.mode == null) {
            this.mode = getMode();
        }
        if (this.config == null) {
            this.config = this.service.getConfig(this.userRequest.getModule());
        }
    }

    protected HttpSession getSession() {
        final HttpServletRequest request = ServletRequestHolder.getHttpServletRequest();
        if (request == null || (request instanceof MyHttpServletRequest)) {
            return null;
        }
        HttpSession session = createSession(request);
        if (!isUserMode()) {
            this.logger.info("<login> SU for '" + this.user.getVwdId()
                    + "'@'" + this.userRequest.getModule() + "', session = " + session.getId());
        }
        return session;
    }

    protected void addSessionParamsToUser(HttpSession session) {
        this.user.getAppConfig().addProperty(AppConfig.PROP_KEY_SESSIONID, session.getId());
    }

    protected boolean responseOK(UserResponse response) {
        return response.getState() == UserResponse.State.OK;
    }

    protected String getLogin() {
        final String login = this.userRequest.getLogin();
        if (login != null && login.startsWith("$")) {
            HttpServletRequest request = ServletRequestHolder.getHttpServletRequest();
            return HttpRequestUtil.getValue(request, login.substring(1));
        }
        if (isUserMode()) {
            return this.userRequest.getLoginUpperCase();
        }
        return this.userRequest.getLoginUpperCase().substring(SU_PREFIX.length());
    }

    private HttpSession createSession(HttpServletRequest request) {
        return createSession(request, this.user.getUid());
    }

    protected HttpSession createSession(HttpServletRequest request, String uId) {
        final HttpSession existingSession = checkZone(request, request.getSession(false));

        if (existingSession != null) {
            this.logger.info("<createSession> reusing existing for " + uId);
            prepareSession(request, existingSession);
            return existingSession;
        }

        final HttpSession session = request.getSession();
        prepareSession(request, session);

        if (isUserMode()) {
            this.service.maintainSessions(this.config, uId, session);
            this.logger.info("<createSession> for " + uId + ": " + session.getId());
        }
        return session;
    }

    private HttpSession checkZone(HttpServletRequest request, HttpSession session) {
        final Zone zone = this.service.getZone(request);

        final Map<String, String[]> parameterMap = zone.getParameterMap(Collections.emptyMap(), "xxxx");
        final String[] strings = parameterMap.get("ensureURIConsistency");
        final boolean ensureURIConsistency = strings != null && strings.length == 1 && "true".equals(strings[0]);
        if (ensureURIConsistency) {
            if (!request.getRequestURI().contains(zone.getName())) {
                this.logger.warn("<resolveBySession> ensureURIConsistency detected session to uri mismatch => invalidate");
                session.invalidate();
                return null;
            }
        }
        return session;
    }

    private boolean isUserMode() {
        return this.mode == LoginMode.USER;
    }

    private boolean isSUMode() {
        return this.mode == LoginMode.SU;
    }

    protected void prepareSession(HttpServletRequest request, HttpSession session) {
        session.setAttribute(ProfileResolver.AUTHENTICATION_KEY, getAuth());
        session.setAttribute(ProfileResolver.AUTHENTICATION_TYPE_KEY, getAuthType());
        session.setAttribute(ProfileResolver.APPLICATION_ID_KEY, this.config.getAppId());
        session.setAttribute(ProfileResolver.CLIENT_ID_KEY, this.config.getClientId());

        session.setAttribute(SESSION_KEY_SESSION_VALIDATOR, getUid());
        session.setAttribute(SESSION_KEY_LOGIN, getLogin());

        // if reuse existing session, it might contain a zone attribute and that would be used
        // by getZoneName, so remove it to make sure the zone name is retrieved from the URI
        session.removeAttribute(UserServiceImpl.SESSION_KEY_ZONENAME);
        session.setAttribute(UserServiceImpl.SESSION_KEY_ZONENAME,
                this.service.getZoneName(request));

        if (this.masterData != null) {
            session.setAttribute(UserServiceImpl.SESSION_KEY_CUSTOMER_NAME,
                    this.masterData.getCustomerName());
        }

        final Map<String, Object> contextMap = this.service.getZone(request).getContextMap("context");
        if (!contextMap.containsKey(STATISTICS) || (Boolean) contextMap.get(STATISTICS)) {
            session.setAttribute(UserStatsDao.VISIT_ID,
                    this.service.getVisitId(request, config, this.masterData, this.userRequest));
        }

        session.setAttribute(UserServiceImpl.SESSION_LOCALE, this.userRequest.getLocale());

        if (this.mode != LoginMode.USER) {
            session.setAttribute(UserServiceImpl.SESSION_SU_FLAG, Boolean.TRUE);
        }
        else {
            session.removeAttribute(UserServiceImpl.SESSION_SU_FLAG);
        }
    }

    protected String getAuthType() {
        return this.authType;
    }

    protected String getUid() {
        return this.user.getUid();
    }

    protected String getAuth() {
        if (this.user.getVwdId() == null) {
            return this.user.getUid();
        }
        final int p = this.user.getVwdId().indexOf(AUTH_PREFIX_SEP) + 1;
        return this.user.getVwdId().substring(p);
    }

    private UserResponse getUser() {
        if (this.config == null) {
            return new UserResponse().withState(UserResponse.State.INVALID_PRODUCT);
        }

        if (isSUMode()) {
            final HttpServletRequest request = ServletRequestHolder.getHttpServletRequest();
            final String remoteAddr = request.getRemoteAddr();
            try {
                final InetAddress address = InetAddress.getByName(remoteAddr);
                if (!this.service.getLocalIPCheckPredicate().test(address)) {
                    this.logger.warn("<getUser> rejected SU access for " + remoteAddr);
                    return new UserResponse().withState(UserResponse.State.UNKNOWN_USER);
                }
            } catch (UnknownHostException e) {
                // should not happen anyways
                throw new IllegalArgumentException(e);
            }
        }

        final String login = getLogin();
        final String module = this.userRequest.getModule();

        final Profile profile = getProfile(login);

        if (profile instanceof VwdProfile) {
            return getUser((VwdProfile) profile, module, login);
        }
        else if (profile instanceof PmWebProfile) {
            return getUser((PmWebProfile) profile, module, this.userRequest.getLoginUpperCase());
        }
        else if (profile instanceof PmAboProfile) {
            return getUser((PmAboProfile) profile, login);
        }
        else if (profile != null) {
            this.logger.warn("<getUser> cannot handle profile for login: '"
                    + login + "'@'" + module + "'" + ", profile=" + profile);
        }
        else {
            this.logger.warn("<getUser> no profile for login: '" + login + "'@'" + module + "'");
        }
        return new UserResponse().withState(UserResponse.State.UNKNOWN_USER);
    }

    protected Profile getProfile(String login) {
        return this.service.getProfileByLogin(login, this.config);
    }

    private UserResponse getUser(PmWebProfile profile, String module, String login) {
        final VwdProfile delegate = profile.getDelegate();
        final UserResponse ur;
        final User user;
        if (delegate != null && delegate.getState() == VwdProfile.State.ACTIVE) {
            ur = getUser(delegate, module, login, login);
            user = ur.getUser();
            if (user == null) {
                return ur;
            }
            final String vwdId = delegate.getVwdId();
            user.setVwdId(vwdId);
            user.getAppConfig().addProperty(AppConfig.PROP_KEY_CREDENTIALS,
                    ProfileUtil.encodeCredential(vwdId, "vwd-ent:ByVwdId"));
        }
        else {
            ur = new UserResponse();
            user = createUserForPmWeb(profile, login);
            if (delegate == null && hasAsMarketData(profile)) {
                handleInvalidVwdId(ur, profile);
                this.logger.error("<getUser> no market data available for login " + login + " but AS_MARKETDATA descriptor set!");
            }
            if (delegate != null && delegate.getState() != VwdProfile.State.ACTIVE) {
                handleInvalidVwdId(ur, profile);
                this.logger.error("<getUser> no market data available for vwd-ID " + delegate.getVwdId() + "! VwdProfile.State: " + delegate.getState());
            }
        }
        if (!doesMandantIdMatch(user, profile, getMode())) {
            handleInvalidVwdId(ur, profile);
            this.logger.error("<getUser> mandantId from vwd-ent (byVwdId) doesn't match configured mandantId! no marketdata available!");
        }
        setPmFeatures(user, profile.getFeatures());
        initMySpaceIfNotSet(user);

        this.logger.info("<getUser> pm user '" + user.getUid() + "' logged in");
        return ur.withUser(new PmUser(user));
    }

    private boolean hasAsMarketData(PmWebProfile profile) {
        return profile.getFeatures().containsKey(Selector.AS_MARKETDATA.getDescriptor())
                && profile.getFeatures().get(Selector.AS_MARKETDATA.getDescriptor());
    }

    private void handleInvalidVwdId(UserResponse ur, PmWebProfile profile) {
        if (hasAsMarketData(profile)) {
            profile.getFeatures().remove(Selector.AS_MARKETDATA.getDescriptor()); // no marketdata if there's an issue with permissioning
        }
        ur.withState(UserResponse.State.INVALID_VWDID);
    }

    private void initMySpaceIfNotSet(User user) {
        user.getAppConfig().ensureInitialMySpace(this.userRequest.getLocale());
    }

    private boolean doesMandantIdMatch(User user, PmWebProfile profile, LoginMode mode) {
        // this line: user.getAppProfile().getFunctions().contains(Selector.AS_INHOUSE.getDescriptor())
        // should actually look like this: Selector.AS_INHOUSE.isAllowed().
        // That doesn't work because this Selector class comes from istar
        // and istar doesn't support AS-Selectors...
        if (this.service.ignoreMandantId() || profile.getDelegate() == null || mode == LoginMode.SU
                || user.getAppProfile().getFunctions().contains(Selector.AS_INHOUSE.getDescriptor())) {
            return true;
        }
        final VwdProfile vwdProfile = profile.getDelegate();
        final UserMasterData userMasterData = this.service.getUserMasterData(vwdProfile.getVwdId(), vwdProfile.getAppId());
        // if the app-ID does not match the app-ID of the profile, user master data will be null, e.g. if s.o. tries
        // to use a vwd-ID for docman (app-ID 120) for web zone as-vwd (app-ID 7).
        if (userMasterData == null) {
            this.logger.warn("<doesMandantIdMatch> failed to retrieve user master data for vwd-ID '"
                    + vwdProfile.getVwdId() + "' with app-ID '" + vwdProfile.getAppId() + "'!");
            return false;
        }
        return this.config.getMandatorId().equals(userMasterData.getMandatorId());
    }

    private void setPmFeatures(User user, Map<String, Boolean> features) {
        final AppProfile appProfile = user.getAppProfile();
        features.entrySet().stream().filter(Map.Entry::getValue).forEach(entry -> appProfile.getFunctions().add(entry.getKey()));
    }

    public static void neverChangePwd(User user) {
        user.setPasswordChangeDate(new DateTime().plusDays(1).toDate()); // never
    }

    private UserResponse getUser(PmAboProfile profile, String login) {
        final User user = createUserForPm(profile, login);
        // make sure profile.getName doesn't clash with real vwdId by adding a prefix
        user.setVwdId("pm" + AUTH_PREFIX_SEP + profile.getName());
        this.authType = UserServiceImpl.PM_ID_AUTH_TYPE;
        this.logger.info("<getUser> user '" + user.getVwdId() + "' logged in");
        return new UserResponse().withUser(user);
    }

    private User createUserForPm(Profile profile, String login) {
        final User user = new User();
        user.setAppConfig(new AppConfig());
        neverChangePwd(user);

        user.setClient(this.config.getId());
        user.setAppId(this.config.getAppId());
        user.setLogin(login);

        final AppProfile ap = toAppProfile(profile);
        user.setAppProfile(ap);
        return user;
    }

    private User createUserForPmWeb(Profile profile, String login) {
        final User pmUser = createUserForPm(profile, login);
        final User storedUser = this.service.getUserByUid(login);
        if (storedUser != null && storedUser.getAppConfig() != null) {
            pmUser.setAppConfig(storedUser.getAppConfig());

        }
        handleFeatureFlag(pmUser.getAppConfig());
        setClientProperties(pmUser.getAppConfig());
        return pmUser;
    }

    private UserResponse getUser(VwdProfile profile, String module, String login) {
        return getUser(profile, module, login, profile.getVwdId());
    }

    private UserResponse getUser(VwdProfile profile, String module, String login, String uid) {
        final String vwdId = profile.getVwdId();

        if (!this.service.profileAllowsModule(profile, module)) {
            this.logger.warn("<getUser> invalid product: '" + vwdId + "'@'" + module + "'");
            return new UserResponse().withState(UserResponse.State.INVALID_PRODUCT);
        }

        if (profile.getState() != VwdProfile.State.ACTIVE) {
            this.logger.warn("<getUser> inactive profile: '" + vwdId + "'@'" + module + "'");
            return new UserResponse().withState(UserResponse.State.INACTIVE_USER);
        }

        this.masterData = this.service.getUserMasterData(vwdId, profile.getAppId());
        if (this.masterData == null) {
            this.masterData = this.service.getUserMasterData(login, profile.getAppId(), this.config.getClientId());
            if (this.masterData == null) {
                this.logger.warn("<getUser> no master data for user " + vwdId + "@" + module);
                return new UserResponse().withState(UserResponse.State.INTERNAL_ERROR);
            }
        }

        boolean authenticated = false;

        final Map<String, String> staticAccounts = this.masterData.getStaticAccounts();
        if (!staticAccounts.isEmpty()) {
            if (isUserMode()) {
                final String staticPassword = staticAccounts.get(login);
                if (!checkPassword(staticPassword, this.userRequest.getPassword())) {
                    this.logger.warn("<getUser> user '" + vwdId + "'@'" + module
                            + "', wrong static password");
                    return new UserResponse().withState(UserResponse.State.WRONG_PASSWORD);
                }
            }
            authenticated = true;
        }

        User user = this.service.getUserByUid(uid);

        if (user == null) {
            if (!isUserMode()) {
                this.logger.info("<getUser> unknown user '" + vwdId + "'@'" + module
                        + "' for su");
                return new UserResponse().withState(UserResponse.State.INTERNAL_ERROR);
            }
            if (!authenticated && !isWithInitialPassword()) {
                this.logger.warn("<getUser> new user '" + vwdId + "'@'" + module
                        + "', wrong initial password");
                return new UserResponse().withState(UserResponse.State.WRONG_INITIAL_PASSWORD);
            }

            user = this.service.createDefaultUser(vwdId, config, this.userRequest.getLocale());
        }
        else if (isUserMode() && !authenticated
                && StringUtils.hasText(this.config.getInitialPassword())
                && !this.service.checkPassword(this.userRequest.getPassword(), user.getPassword())) {
            this.logger.warn("<getUser> user '" + vwdId + "'@'" + module + "', wrong password");
            return new UserResponse().withState(UserResponse.State.WRONG_PASSWORD);
        }

        user.setClient(this.config.getId());
        user.setAppId(this.config.getAppId());
        user.setLogin(login);

        setFields(user);
        tryInitGisPortalHttpBasicAuthCredentials(user);

        final AppProfile ap = toAppProfile(profile);
        user.setAppProfile(ap);

        adaptAppConfig(user, profile);

        if (this.mode == LoginMode.SU) {
            user.setFirstName(SU_PREFIX + (user.getFirstName() != null ? user.getFirstName() : login));
        }
        else {
            user.setPasswordChangeRequired(staticAccounts.isEmpty() && isPasswordChangeRequired(user));
        }

        this.logger.info("<getUser> user '" + vwdId + "' logged in");
        return new UserResponse().withUser(user);
    }

    /**
     * Tries to amend an existing user object with HTTP basic auth credentials for GIS Portal.
     * Password and username are defined by fixed zone properties in istar.
     *
     * @param user the user to amend with HTTP basic auth credentials for GIS Portal.
     */
    private void tryInitGisPortalHttpBasicAuthCredentials(User user) {
        try {
            final HttpServletRequest request = ServletRequestHolder.getHttpServletRequest();
            final Zone zone = this.service.getZone(request);
            final Map<String, String[]> parameterMap = zone.getParameterMap(Collections.emptyMap(), "gisPortalHttpBasicAuth");
            final String[] usernames = parameterMap.get("username");
            final String[] passwords = parameterMap.get("password");
            if (usernames != null && passwords != null && usernames.length > 0 && passwords.length > 0) {
                user.setGisPortalHttpBasicAuthCredentials(new HttpBasicAuthCredentials(usernames[0], passwords[0]));
            }
        }
        catch(Exception e) {
            this.logger.warn("<tryInitGisPortalHttpBasicAuthCredentials> failed", e);
        }
    }

    private boolean isWithInitialPassword() {
        return !StringUtils.hasText(config.getInitialPasswordHash())
                && !StringUtils.hasText(this.userRequest.getPassword())
                || this.service.checkPassword(this.userRequest.getPassword(), config.getInitialPasswordHash());
    }

    private boolean checkPassword(String staticPassword, String password) {
        return password != null && password.equals(staticPassword);
    }

    private void adaptAppConfig(User user, VwdProfile vwdProfile) {
        final AppConfig appConfig = user.getAppConfig();
        appConfig.addProperty(PROP_KEY_MUSTERDEPOT_USERID, config.getIdOfMusterdepotUser());
        appConfig.addProperty(AppConfig.PROP_KEY_PUSH, vwdProfile.isWithPush() ? "true" : null);
        handleFeatureFlag(appConfig);
        setClientProperties(appConfig);
    }

    private void handleFeatureFlag(AppConfig appConfig) {
        if (!FeatureFlags.Flag.PROD.isEnabled()) {
            appConfig.addProperty(AppConfig.PROP_KEY_DEV, "true");
        }
    }

    protected void setClientProperties(AppConfig appConfig) {
        final Map<String, String> clientProperties = this.service.getClientProperties();
        if (clientProperties != null && !clientProperties.isEmpty()) {
            appConfig.addClientProps(clientProperties);
        }
    }

    private boolean isPasswordChangeRequired(User result) {
        if (result.getPasswordChangeDate() == null) {
            return true;
        }
        if (config.getChangePasswordAfterDays() == 0) {
            return false;
        }
        final LocalDate then = new LocalDate(result.getPasswordChangeDate());
        return (then.plusDays(config.getChangePasswordAfterDays()).isBefore(new LocalDate()));
    }

    private void setFields(User user) {
        user.setLastName(masterData.getLastName());
        user.setFirstName(masterData.getFirstName());
        user.setCentralBank(masterData.getCentralBank());
        user.setGenoId(masterData.getGenoId());
        user.setGisCustomerId(masterData.getGisCustomerId());
        user.setVdbLogin(masterData.getVdbLogin());
        user.setVdbPassword(masterData.getVdbPassword());
        user.setAppTitle(masterData.getAppTitle());
        user.setCustomerName(masterData.getCustomerName());
        user.setGender(User.Gender.valueOf(masterData.getGender().name()));
        //TODO: gisPortalCredentials
    }

    protected UserServiceImpl.LoginMode getMode() {
        if (this.userRequest.getLoginUpperCase().startsWith(SU_PREFIX)
                && this.service.checkSuPassword(this.userRequest.getPassword())) {
            return LoginMode.SU;
        }
        return LoginMode.USER;
    }
}