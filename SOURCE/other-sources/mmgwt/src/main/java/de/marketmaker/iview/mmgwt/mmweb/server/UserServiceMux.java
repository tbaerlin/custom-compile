package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.UserMasterData;
import de.marketmaker.iview.mmgwt.mmweb.client.ChangePasswordResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.UserRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.UserResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.MessageOfTheDay;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

import static de.marketmaker.istar.merger.web.ProfileResolver.PM_AUTHENTICATION_KEY;

/**
 * @author Ulrich Maurer
 *         Date: 11.01.13
 */
public class UserServiceMux implements UserServiceIfc, HttpRequestHandler {
    private UserServiceIfc pmUserService;

    private UserServiceIfc vwdentUserService;

    public void handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        ServletRequestHolder.setHttpServletRequest(request);
        try {
            ((RemoteServiceServlet)getUserService()).doPost(request, response);
        }
        finally {
            ServletRequestHolder.setHttpServletRequest(null);
        }
    }

    public void setPmUserService(UserServiceIfc pmUserService) {
        this.pmUserService = pmUserService;
    }

    public void setVwdentUserService(UserServiceIfc vwdentUserService) {
        this.vwdentUserService = vwdentUserService;
    }

    private boolean isPmZone() {
        final HttpServletRequest request = ServletRequestHolder.getHttpServletRequest();
        return request != null && isPmSession(request);
    }

    private UserServiceIfc getUserService() {
        return getUserService(isPmZone());
    }

    private UserServiceIfc getUserService(boolean pmZone) {
        return (pmZone && this.pmUserService != null) ? this.pmUserService : this.vwdentUserService;
    }

    private boolean isPmSession(HttpServletRequest request) {
        return WebUtils.getSessionAttribute(request, PM_AUTHENTICATION_KEY) != null;
    }

    @Override
    public UserResponse login(UserRequest userRequest) {
        return getUserService(userRequest.isPmZone()).login(userRequest);
    }

    @Override
    public ChangePasswordResponse changePassword(String uid, String pwOld, String pwNew,
            String module) {
        return getUserService().changePassword(uid, pwOld, pwNew, module);
    }

    @Override
    public void storeUserConfig(String userId, AppConfig config) {
        getUserService().storeUserConfig(userId, config);
    }

    @Override
    public void logout() {
        getUserService().logout();
    }

    @Override
    public String requestPasswordReset(String login, String module, String locale) {
        return getUserService().requestPasswordReset(login, module, locale);
    }

    @Override
    public UserResponse getPublicProfile(UserRequest userRequest) {
        return getUserService(userRequest.isPmZone()).getPublicProfile(userRequest);
    }

    @Override
    public MessageOfTheDay getMessageOfTheDay() {
        return getUserService().getMessageOfTheDay();
    }

    @Override
    public String getMessageOfTheDayByDate() {
        return getUserService().getMessageOfTheDayByDate();
    }

    @Override
    public void setMessageOfTheDay(MessageOfTheDay motd) {
        getUserService().setMessageOfTheDay(motd);
    }

    @Override
    public ClientConfig getConfig(String moduleName) {
        return getUserService().getConfig(moduleName);
    }

    @Override
    public UserMasterData getUserMasterData(String vwdId) {
        return getUserService().getUserMasterData(vwdId);
    }

    @Override
    public UserMasterData getUserMasterData(String vwdId, String appId) {
        return getUserService().getUserMasterData(vwdId, appId);
    }

    @Override
    public Profile getProfileByVwdId(String vwdId, String appId) {
        return getUserService().getProfileByVwdId(vwdId, appId);
    }

    @Override
    public Profile getProfileByLogin(String login, ClientConfig config) {
        return getUserService().getProfileByLogin(login, config);
    }

    @Override
    public boolean resetPassword(String login, String moduleName) {
        return getUserService().resetPassword(login, moduleName);
    }

    @Override
    public User getUserByUid(String uid) {
        return getUserService().getUserByUid(uid);
    }

    @Override
    public UserResponse getUser(UserRequest userRequest) {
        return getUserService(userRequest.isPmZone()).getUser(userRequest);
    }

    @Override
    public Map<String, String> getEnvInfo() {
        return getUserService().getEnvInfo();
    }
}
