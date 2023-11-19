/*
 * UserServiceImpl.java
 *
 * Created on 27.02.2008 09:53:57
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.common.http.LocalOrAddressRangePredicate;
import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.UserMasterData;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataProvider;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataRequest;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataResponse;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.merger.alert.AlertProvider;
import de.marketmaker.istar.merger.alert.AlertUser;
import de.marketmaker.istar.merger.alert.RetrieveAlertUserRequest;
import de.marketmaker.istar.merger.alert.RetrieveAlertUserResponse;
import de.marketmaker.istar.merger.provider.profile.CounterService;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneResolver;
import de.marketmaker.iview.mmgwt.mmweb.client.ChangePasswordResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.UserRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.UserResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.Version;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.MessageOfTheDay;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.server.statistics.UserStatsDao;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.util.StringUtils;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Implementation of the UserService interface that can be used in a dmxml-1 webapp.
 * The RemoteService methods may either be invoked by a
 * {@link GwtService} that delegates decoded
 * requests, or it may be invoked by means of jms when this object has been exported by
 * a lingo service wrapper.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class UserServiceImpl extends GwtService implements UserServiceIfc, InitializingBean,
        HttpSessionListener {

    public static final String ZONE_PARAMETER_NEVER_STORE_APP_CONFIG = "neverStoreAppConfig";

    protected static final String HASHED_PW = "$2a$10$fcQcetMOOwHTB2yjaYfyS.9T1hpfoW.bkurlqApMqOTx2mfuaMYtu";

    public static final String SESSION_KEY_ZONENAME = "zone";

    static final String SESSION_KEY_CUSTOMER_NAME = "customerName";

    static final String SESSION_KEY_LOGIN = "login";

    static final String SESSION_KEY_PRE_LOGIN = "pre_login";

    static final String SESSION_KEY_PRE_PASSWORD = "pre_password";

    static final String SESSION_KEY_MODULE = "moduleName";

    public enum LoginMode {
        /**
         * user's profile and app-config, can store app-config, requires user's password
         */
        USER,
        /**
         * with user's profile and app-config, cannot store user's data, requires su password
         * Login is "su <em>userid</em>"
         */
        SU
    }

    private static Map<String, String> doGetEnvInfo() {
        final HashMap<String, String> result = new HashMap<>();
        try {
            Properties p = PropertiesLoader.load(Version.class.getResourceAsStream("Version.properties"));
            result.put("asBuild", p.getProperty("build"));
        }
        catch (Exception ex) {
            result.put("error", ex.getMessage());
        }
        return result;
    }

    static final String VWD_ID_AUTH_TYPE = "vwd-ent:ByVwdId";

    static final String PM_ID_AUTH_TYPE = "pmpub:ByKennung";

    static final String PMWEB_AUTH_TYPE = "pmweb";

    private UserDao dao;

    private UserStatsDao statsDao;

    private ZoneResolver zoneResolver;

    /**
     * clientConfigs by moduleName
     */
    private Map<String, ClientConfig> clientConfigs;

    private final Map<String, String> envInfo = doGetEnvInfo();

    private UserMasterDataProvider userMasterDataProvider;

    private AlertProvider alertProvider;

    private MailSupport mailSupport;

    private ProfileProvider profileProvider;

    private CounterService counterService;

    private ProductGuard productGuard;

    private LocalOrAddressRangePredicate localIPCheckPredicate;

    private boolean ignoreMandantId;

    /**
     * The ObjectName of the tomcat manager mbean that will be used to expire sessions.
     */
    private volatile ObjectName webappManager;

    /**
     * only if the object defined by webappManager is in a state called STARTED, we can assume
     * that a session has been destroyed normally (either due to logout or expiration); if the
     * manager is in state STOPPING, it will destroy all sessions but those sessions will continue
     * to be valid on other cluster nodes, so we must not count it as logout.
     */
    private static final String COUNT_LOGOUT_STATE_NAME = "STARTED";

    static final String SU_PREFIX = "SU ";

    static final String SESSION_SU_FLAG = "su";

    static final String SESSION_LOCALE = "locale";

    /**
     * map of properties that can't be covert by guidefs
     */
    private Map<String, String> clientProperties;

    public void setClientProperties(Map<String, String> clientProperties) {
        this.clientProperties = clientProperties;
    }

    public Map<String, String> getClientProperties() {
        return clientProperties;
    }

    public void setProfileProvider(ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    public void setZoneResolver(ZoneResolver zoneResolver) {
        this.zoneResolver = zoneResolver;
    }

    public void setProductGuard(ProductGuard productGuard) {
        this.productGuard = productGuard;
    }

    public void setLocalIPCheckPredicate(LocalOrAddressRangePredicate localIPCheckPredicate) {
        this.localIPCheckPredicate = localIPCheckPredicate;
    }

    public LocalOrAddressRangePredicate getLocalIPCheckPredicate() {
        return localIPCheckPredicate;
    }

    public void setIgnoreMandantId(boolean ignoreMandantId) {
        this.ignoreMandantId = ignoreMandantId;
    }

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        countLogout(httpSessionEvent.getSession());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.logger.info("<afterPropertiesSet> envInfo = " + this.envInfo);
        reloadClientConfigs();
    }

    @ManagedOperation
    public void reloadClientConfigs() {
        final Map<String, ClientConfig> tmp = this.dao.getClientConfigs();
        synchronized (this) {
            this.clientConfigs = tmp;
        }
        this.logger.info("<reloadClientConfigs> " + tmp);
    }

    @Required
    public void setUserMasterDataProvider(UserMasterDataProvider userMasterDataProvider) {
        this.userMasterDataProvider = userMasterDataProvider;
    }

    public void setWebappManagerObjectName(
            String webappManagerObjectName) throws MalformedObjectNameException {
        if (StringUtils.hasText(webappManagerObjectName)) {
            this.webappManager = new ObjectName(webappManagerObjectName);
        }
    }

    public void setAlertProvider(AlertProvider alertProvider) {
        this.alertProvider = alertProvider;
    }

    public void setCounterService(CounterService counterService) {
        this.counterService = counterService;
    }

    public void setMailSupport(MailSupport mailSupport) {
        this.mailSupport = mailSupport;
    }

    @Override
    public UserResponse login(UserRequest userRequest) {
        return new UserLoginMethod(this, userRequest).invoke();
    }

    public boolean ignoreMandantId() {
        return this.ignoreMandantId;
    }

    void maintainSessions(ClientConfig config, String uId, HttpSession session) {
        if (!config.isWithExclusiveSession()) {
            return;
        }
        final String oldSessionId = this.dao.getSessionId(uId);
        this.dao.setSessionId(uId, session.getId());

        if (oldSessionId != null && !oldSessionId.equals(session.getId())) {
            expireFormerSession(oldSessionId);
        }
    }

    Zone getZone(HttpServletRequest request) {
        if (this.zoneResolver != null) {
            return this.zoneResolver.resolveZone(request);
        }
        return null;
    }

    String getZoneName(HttpServletRequest request) {
        final Zone zone = getZone(request);
        return (zone != null) ? zone.getName() : "iview";
    }

    public void expireFormerSession(String sessionId) {
        if (this.webappManager == null) {
            return;
        }
        try {
            final MBeanServer server = JmxUtils.locateMBeanServer();
            server.invoke(this.webappManager, "expireSession",
                    new String[]{sessionId}, new String[]{"java.lang.String"});
            this.logger.info("<expireFormerSession> invoked expireSession for " + sessionId);
        }
        catch (Exception e) {
            this.logger.error("<expireFormerSession> failed", e);
        }
    }


    @Override
    public UserResponse getUser(UserRequest userRequest) {
        return new UserLoginMethod(this, userRequest).withMode(LoginMode.USER).invoke();
    }

    @Override
    public synchronized ClientConfig getConfig(String moduleName) {
        final ClientConfig result = this.clientConfigs.get(moduleName);
        if (result == null) {
            this.logger.warn("<getConfig> unknown module: '" + moduleName + "'");
        }
        return result;
    }

    protected boolean profileAllowsModule(Profile profile, String moduleName) {
        return this.productGuard == null
                || this.productGuard.profileAllowsModule(profile, moduleName);
    }

    @Override
    public User getUserByUid(String uid) {
        return this.dao.getUser(uid);
    }

    long getVisitId(HttpServletRequest request, ClientConfig client, UserMasterData data,
                    UserRequest userRequest) {
        if (this.statsDao == null) {
            return 0L;
        }
        return this.statsDao.insertVisit(request, client, data, userRequest);
    }

    @Override
    public Profile getProfileByVwdId(final String vwdId, String appId) {
        return getProfile(ProfileRequest.byVwdId(vwdId, appId));
    }

    @Override
    public Profile getProfileByLogin(final String login, ClientConfig config) {
        final ProfileRequest request = new ProfileRequest(config.getLoginAuthType(), login);
        request.setClientId(config.getClientId());
        request.setApplicationId(config.getAppId());
        request.setUseCache(false);
        return getProfile(request);
    }

    Profile getProfile(ProfileRequest request) {
        final ProfileResponse response = this.profileProvider.getProfile(request);
        return response.getProfile();
    }

    protected void countLogin(ClientConfig config, String login, HttpSession session) {
        if (config.isCountLogin() && FeatureFlags.isEnabled(FeatureFlags.Flag.PROD)) {
            this.counterService.countLogin(config.getAppId(), config.getClientId(), login);
            session.setAttribute(SESSION_KEY_MODULE, config.getModuleName());
        }
    }

    protected void countLogout(HttpSession session) {
        if (!FeatureFlags.isEnabled(FeatureFlags.Flag.PROD)
                || !COUNT_LOGOUT_STATE_NAME.equals(getManagerState())) {
            return;
        }

        final String moduleName = (String) session.getAttribute(SESSION_KEY_MODULE);
        if (moduleName == null) {
            return;
        }
        final ClientConfig config = getConfig(moduleName);
        final String login = (String) session.getAttribute(SESSION_KEY_LOGIN);
        if (config != null && config.isCountLogin() && login != null) {
            final String uid = getVwdId(session);
            // an expired session will expire on all nodes in the cluster, we use the database
            // to ensure that the logout will be counted only once.
            if (session.getId().equals(this.dao.getSessionId(uid))) {
                this.counterService.countLogout(config.getAppId(), config.getClientId(), login);
                this.dao.setSessionId(uid, null);
            }
        }
    }

    private Object getManagerState() {
        if (this.webappManager == null) {
            return null;
        }
        try {
            return JmxUtils.locateMBeanServer().getAttribute(this.webappManager, "stateName");
        }
        catch (Exception e) {
            this.logger.error("<getManagerState> failed", e);
            return null;
        }
    }

    @Override
    public UserMasterData getUserMasterData(String vwdId) {
        return getUserMasterData(vwdId, null);
    }

    @Override
    public UserMasterData getUserMasterData(String vwdId, String appId) {
        final UserMasterDataRequest request = UserMasterDataRequest.forVwdId(vwdId);
        request.setAppId(appId);
        final UserMasterDataResponse response = this.userMasterDataProvider.getUserMasterData(request);
        if (response.isValid()) {
            return response.getMasterData();
        }
        return null;
    }

    public UserMasterData getUserMasterData(String login, String appId, String clientId) {
        final UserMasterDataRequest request = UserMasterDataRequest.forLogin(login, clientId);
        request.setAppId(appId);
        final UserMasterDataResponse response = this.userMasterDataProvider.getUserMasterData(request);
        if (response.isValid()) {
            return response.getMasterData();
        }
        return null;
    }

    public void setDao(UserDao dao) {
        this.dao = dao;
    }

    public void setStatsDao(UserStatsDao statsDao) {
        this.statsDao = statsDao;
    }

    private HttpSession getSession() {
        final HttpServletRequest gwtRequest = ServletRequestHolder.getHttpServletRequest();
        if (gwtRequest == null || (gwtRequest instanceof MyHttpServletRequest)) {
            return null;
        }
        return gwtRequest.getSession(false);
    }

    @Override
    public void storeUserConfig(String userId, AppConfig config) {
        if (!isSuSession() && isWithStoreAppConfig()) {
            // never persist the "dev" property
            final boolean dev = config.getBooleanProperty(AppConfig.PROP_KEY_DEV, false);
            try {
                config.addProperty(AppConfig.PROP_KEY_DEV, null);
                this.dao.storeUserConfig(userId, config);
                this.logger.info("<storeData> stored " + userId);
            } finally {
                config.addProperty(AppConfig.PROP_KEY_DEV, dev);
            }
        }
    }

    @Override
    public void logout() {
        final HttpSession session = getSession();
        if (session != null) {
            this.logger.info("<logout> user " + getVwdId(session) + ", " + session.getId());
            session.invalidate();
        }
        else {
            this.logger.warn("<logout> w/o valid session?!");
        }
    }

    @SuppressWarnings("unused")
    private String getVwdId() {
        return getVwdId(getSession());
    }

    private String getVwdId(HttpSession session) {
        return (session != null)
                ? (String) session.getAttribute(ProfileResolver.AUTHENTICATION_KEY)
                : null;
    }

    private boolean isSuSession() {
        final HttpServletRequest request = ServletRequestHolder.getHttpServletRequest();
        if (request != null && !(request instanceof MyHttpServletRequest)) {
            final HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute(SESSION_SU_FLAG) != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isWithStoreAppConfig() {
        final HttpServletRequest request = ServletRequestHolder.getHttpServletRequest();
        if (request != null && !(request instanceof MyHttpServletRequest)) {
            final Zone zone = getZone(request);
            final Map<String, String[]> zoneParameterMap = zone.getParameterMap(Collections.emptyMap(), "xxxx");
            final String[] values = zoneParameterMap.get(ZONE_PARAMETER_NEVER_STORE_APP_CONFIG);
            if(values != null && Stream.of(values).anyMatch("true"::equals)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ChangePasswordResponse changePassword(String vwdId, String pwOld, String pwNew, String moduleName) {
        this.logger.info("<changePassword> user: " + vwdId);
        try {
            final User user = getUserByUid(vwdId);
            if (user == null) {
                this.logger.warn("<changePassword> unknown user: " + vwdId);
                // should never happen since uid is from logged in user
                return ChangePasswordResponse.INTERNAL_ERROR;
            }
            if (pwOld == null || !checkPassword(pwOld, user.getPassword())) {
                this.logger.warn("<changePassword> old password wrong for user: " + vwdId);
                return ChangePasswordResponse.WRONG_OLD_PASSWORD;
            }
            final ClientConfig config = this.clientConfigs.get(moduleName);
            if (config != null && checkPassword(pwNew, config.getInitialPasswordHash())) {
                this.logger.warn("<changePassword> new password is initial password for user: " + vwdId);
                return ChangePasswordResponse.INITIAL_PASSWORD;
            }
            for (String pw : user.getPasswords()) {
                if (pwNew != null && checkPassword(pwNew, pw)) {
                    this.logger.info("<changePassword> user " + vwdId + " repeated recent password");
                    return ChangePasswordResponse.REPEATED_RECENT_PASSWORD;
                }
            }
            final boolean success = this.dao.changePassword(vwdId, user.getPassword(), hashPassword(pwNew));
            if (!success) {
                // should not happen since user exists and we checked the old password?!
                this.logger.warn("<changePassword> returned false for " + vwdId + "?!");
            }
            else {
                this.logger.warn("<changePassword> succeeded for user: " + vwdId);
            }
            return success ? ChangePasswordResponse.OK : ChangePasswordResponse.INTERNAL_ERROR;
        }
        catch (Exception e) {
            this.logger.error("<changePassword> failed for user: " + vwdId, e);
            return ChangePasswordResponse.INTERNAL_ERROR;
        }
    }


    User createDefaultUser(String vwdId, ClientConfig config, String locale) {
        User user = null;
        if (config.getIdOfTemplateUser() != null) {
            user = getUserByUid(config.getIdOfTemplateUser());
            if (user == null) {
                this.logger.warn("<createDefaultUser> no template user for " + config);
            }
        }

        if (user == null) { // no template user
            user = new User();
            final AppConfig ac = new AppConfig();
            ac.ensureInitialMySpace(locale);
            user.setAppConfig(ac);
        }

        user.setVwdId(vwdId);
        user.setPasswords(new String[]{config.getInitialPasswordHash()});
        this.dao.insertUser(user);

        return getUserByUid(vwdId);
    }

    @Override
    public boolean resetPassword(String login, String moduleName) {
        final ClientConfig config = getConfig(moduleName);
        if (config == null) {
            this.logger.warn("<resetPassword> unknown module: '" + moduleName + "'");
            return false;
        }
        return resetPassword(login, config);
    }

    private boolean resetPassword(String login, ClientConfig config) {
        final Profile profile = getProfileByLogin(login, config);
        if (profile == null || !(profile instanceof VwdProfile)) {
            this.logger.warn("<resetPassword> not a VwdProfile: " + profile
                    + " for '" + login + "'@'" + config.getModuleName() + "'");
            return false;
        }
        final VwdProfile vwdProfile = (VwdProfile) profile;
        this.logger.info("<resetPassword> " + vwdProfile.getVwdId());
        return this.dao.resetPassword(vwdProfile.getVwdId(), config.getInitialPasswordHash());
    }

    public static String hashPassword(String pw) {
        final String s = getPasswordWithMinimumLength(pw);
        return BCrypt.hashpw(s, BCrypt.gensalt());
    }

    protected boolean checkSuPassword(String pw) {
        return checkPassword(pw, HASHED_PW);
    }

    public boolean checkPassword(String pw, String hashedPw) {
        if (pw.isEmpty() && hashedPw.isEmpty()) {
            return true;
        }

        if (hashedPw.isEmpty()) {
            return false;
        }

        final String s = getPasswordWithMinimumLength(pw);
        return BCrypt.checkpw(s, hashedPw);
    }

    private static String getPasswordWithMinimumLength(String pw) {
        // the substring call is wrong but must not be corrected as it is already in use...
        return pw.length() >= 8 ? pw : (pw + "qwertzui".substring(8 - pw.length()));
    }

    @Override
    public String requestPasswordReset(String login, String module, String locale) {
        final ClientConfig config = getConfig(module);

        final UserMasterDataRequest userMasterDataRequest = UserMasterDataRequest.forLogin(login, config.getClientId());
        userMasterDataRequest.setAppId(config.getAppId());


        final UserMasterDataResponse data = this.userMasterDataProvider.getUserMasterData(userMasterDataRequest);
        if (!data.isValid()) {
            this.logger.warn("<sendPassword> request returned invalid response: " + userMasterDataRequest);
            return null;
        }
        final String email = getEmailAddress(data);
        if (email == null) {
            this.logger.info("<sendPassword> no email address for '" + login + "'@'" + module + "'");
            return null;
        }

        final String link = getPasswordChangeLink(login, module, locale);
        if (link == null) {
            return null;
        }

        final String appTitle = config.getAppTitle();

        final HashMap<String, Object> model = new HashMap<>();
        model.put("appTitle", appTitle);
        model.put("login", login);
        model.put("link", link);
        model.put("firstName", data.getMasterData().getFirstName());
        model.put("lastName", data.getMasterData().getLastName());
        model.put(locale, Boolean.TRUE);

        try {
            final MailSupport.Builder b = this.mailSupport.newBuilder();
            b.addTo(MailSupport.toAddresses(email));
            b.setView(ServletRequestHolder.getHttpServletRequest(), "passwordemail", module, model);
            b.send();
        }
        catch (Exception e) {
            this.logger.error("<requestPasswordReset> failed to send email", e);
            return null;
        }

        return email;
    }

    @Override
    public UserResponse getPublicProfile(UserRequest userRequest) {
        return new PublicProfileUserLoginMethod(this, userRequest).invoke();
    }

    private String getPasswordChangeLink(String login, String module, String locale) {
        try {
            final ResetPassword.Command c = new ResetPassword.Command();
            c.setUserid(login);
            c.setModule(module);
            c.setLocale(locale);
            c.setDate(new Date());
            final String s = c.encrypt();
            final HttpServletRequest request = ServletRequestHolder.getHttpServletRequest();
            final URI link = new URI(request.getRequestURL().toString())
                    .resolve("../reset-password.html?cmd=" + URLEncoder.encode(s, "US-ASCII"));
            return link.toString();
        }
        catch (Exception e) {
            this.logger.error("<getPasswordChangeLink> failed", e);
            return null;
        }
    }

    protected String getEmailAddress(UserMasterDataResponse data) {
        final String vwdId = data.getMasterData().getVwdId();
        final RetrieveAlertUserResponse userResponse
                = this.alertProvider.retrieveAlertUser(new RetrieveAlertUserRequest(vwdId));
        if (!userResponse.isValid()) {
            this.logger.warn("<getEmailAddress> failed to get AlertUser for vwdId " + vwdId);
            return null;
        }
        return getEmailAddress(userResponse.getUser());
    }

    private String getEmailAddress(AlertUser user) {
        if (StringUtils.hasText(user.getEmailAddress1())) {
            return user.getEmailAddress1();
        }
        if (StringUtils.hasText(user.getEmailAddress2())) {
            return user.getEmailAddress2();
        }
        if (StringUtils.hasText(user.getEmailAddress3())) {
            return user.getEmailAddress3();
        }
        return null;
    }

    @Override
    public void setMessageOfTheDay(MessageOfTheDay motd) {
        this.dao.setMessageOfTheDay(getZoneName(), motd);
    }

    @Override
    public MessageOfTheDay getMessageOfTheDay() {
        return this.dao.getMessageOfTheDay(getZoneName());
    }

    @Override
    public String getMessageOfTheDayByDate() {
        return this.dao.getMessageOfTheDayByDate(getZoneName());
    }

    private String getZoneName() {
        return getZoneName(ServletRequestHolder.getHttpServletRequest());
    }

    @Override
    public Map<String, String> getEnvInfo() {
        return this.envInfo;
    }

    public static void main(String[] args) {
        int n = 0;

        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost/gwtuser";
        String username = "merger";
        String password = "merger";
        boolean withHistory = false;

        while (n < args.length && args[n].startsWith("-")) {
            if (args[n].startsWith("-d")) {
                driver = args[n].substring(2);
            }
            else if (args[n].startsWith("-j")) {
                url = args[n].substring(2);
            }
            else if (args[n].startsWith("-u")) {
                username = args[n].substring(2);
            }
            else if (args[n].startsWith("-p")) {
                password = args[n].substring(2);
            }
            else if (args[n].startsWith("-h")) {
                withHistory = true;
            }
            n++;
        }

        System.out.println("url      = " + url);
        System.out.println("user     = " + username);
        System.out.println("password = " + password);
        final SingleConnectionDataSource ds1 = new SingleConnectionDataSource();
        ds1.setDriverClassName(driver);
        ds1.setPassword(password);
        ds1.setUsername(username);
        ds1.setUrl(url);

        UserDaoDb dao = new UserDaoDb();
        dao.setDataSource(ds1);
        dao.afterPropertiesSet();

        for (int k = n; k < args.length; k++) {
            final String id = args[k].toUpperCase();
            final User user = dao.getUser(id);
            if (user == null) {
                System.err.println("No data for " + id);
                continue;
            }
            System.out.println("------------------------------------------------");
            System.out.println(" " + user.getLogin());
            System.out.println("------------------------------------------------");
            System.out.println(user.getAppConfig());
            if (withHistory) {
                final NavigableMap<DateTime, AppConfig> history = dao.getAppConfigHistory(id);
                int v = 0;
                for (Map.Entry<DateTime, AppConfig> e : history.descendingMap().entrySet()) {
                    System.out.println("--(" + --v + ")- " + e.getKey() + " ------------");
                    System.out.println(e.getValue());
                }
            }
        }
    }
}