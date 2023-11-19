package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.UserMasterData;
import de.marketmaker.iview.mmgwt.mmweb.client.UserRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.UserResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.UserService;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;

/**
 * @author Ulrich Maurer
 *         Date: 11.01.13
 */
@SuppressWarnings({"GWTRemoteServiceAsyncCheck", "NonSerializableServiceParameters"})
public interface UserServiceIfc extends UserService {

    ClientConfig getConfig(String moduleName);

    UserMasterData getUserMasterData(String vwdId);

    UserMasterData getUserMasterData(String vwdId, String appId);

    Profile getProfileByVwdId(String vwdId, String appId);

    boolean resetPassword(String login, String moduleName);

    Profile getProfileByLogin(String login, ClientConfig config);

    User getUserByUid(String uid);

    UserResponse getUser(UserRequest userRequest);

}
