package de.marketmaker.iview.mmgwt.mmweb.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.MessageOfTheDay;
import de.marketmaker.iview.mmgwt.mmweb.client.data.PmUser;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.pmxml.LoadDataRequest;
import de.marketmaker.iview.pmxml.LoadDataResponse;
import de.marketmaker.iview.pmxml.SaveDataRequest;


/**
 * Created on 30.08.12 15:52
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 */

public class UserDaoPm implements UserDao {

    private static final String NOT_IMPLEMENTED = "pm doesn't support this feature";

    private static final String SAVE_DATA_PREFIX_SESSION = "sn_";

    private static final String SAVE_DATA_PREFIX_APPCONF = "ac_";

    private static final String FUNCTIONKEY_DB_LOAD_DATA = "DB_LoadData";

    private static final String FUNCTIONKEY_DB_SAVA_DATA = "DB_SavaData";

    private final Log logger = LogFactory.getLog(getClass());

    private PmxmlHandler pmxmlHandlerPm;

    private final Map<String, ClientConfig> clientConfig = new HashMap<>();

    public void setPmxmlHandlerPm(PmxmlHandler pmxmlHandlerPm) {
        this.pmxmlHandlerPm = pmxmlHandlerPm;
    }

    public void setClientConfig(List<String> strings) {
        this.clientConfig.clear();
        for (String configStr : strings) {
            try {
                final String[] tokens = configStr.split(Pattern.quote(","));
                final ClientConfig cc = createAsConfig(Integer.parseInt(tokens[0]), tokens[1], tokens[2], tokens[3]);
                this.clientConfig.put(cc.getModuleName(), cc);
            } catch (Exception e) {
                this.logger.error("<setClientConfig> failed for " + configStr);
            }
        }
        this.logger.info("<loadEnvironmentInfos> clientConfig = " + this.clientConfig);
    }

    private ClientConfig createAsConfig(int id, String moduleName, String appId, String clientId) {
        final ClientConfig configAs = new ClientConfig(id);
        configAs.setModuleName(moduleName);
        configAs.setClientId(clientId);
        configAs.setAppId(appId);
        configAs.setSessionMode("EXCLUSIVE");
        configAs.setLoginAuthType("vwd-ent:ByLogin");
        configAs.setChangePasswordAfterDays(0); //never
        configAs.setAppTitle("vwd advisory solution:");
        return configAs;
    }

    @Override
    public User getUser(String id) {
        final LoadDataRequest loadDataRequest = new LoadDataRequest();
        loadDataRequest.setId(SAVE_DATA_PREFIX_APPCONF + id);
        final LoadDataResponse response;
        try {
            response = this.pmxmlHandlerPm.exchangeData(loadDataRequest, FUNCTIONKEY_DB_LOAD_DATA, LoadDataResponse.class);
        } catch (PmxmlException e) {
            throw new IllegalStateException(e);
        }

        final PmUser user = new PmUser();
        if (response.isOk()) {
            user.setAppConfig(decode(response.getContent()));
        }
        else {
            user.setAppConfig(new AppConfig());
        }
        user.setPasswordChangeDate(new DateTime().minusDays(100).toDate());
        return user;
    }

    @Override
    public List<User> getUsers() {
        throw new RuntimeException(NOT_IMPLEMENTED);
    }

    @Override
    public void storeUserConfig(String userId, AppConfig config) {
        try {
            final SaveDataRequest saveDataRequest = new SaveDataRequest();
            saveDataRequest.setContent(encode(config));
            saveDataRequest.setId(SAVE_DATA_PREFIX_APPCONF + userId);
            this.pmxmlHandlerPm.exchangeData(saveDataRequest, FUNCTIONKEY_DB_SAVA_DATA);
        } catch (PmxmlException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException("could not encode appconfig of user " + userId, e);
        }
    }

    public static AppConfig decode(String data) {
        try {
            final byte[] bytes = Base64.decodeBase64(data.getBytes(Charset.forName("UTF-8")));
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            return (AppConfig) new ObjectInputStream(new GZIPInputStream(bais)).readObject();
        } catch (Exception e) {
            throw new IllegalStateException("could not decode appconfig", e);
        }
    }

    public static String encode(AppConfig config) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(baos));
        oos.writeObject(config);
        oos.close();
        final byte[] base64 = Base64.encodeBase64(baos.toByteArray());
        return new String(base64, Charset.forName("UTF-8"));
    }

    @Override
    public String getSessionId(String uid) {
        final LoadDataRequest loadDataRequest = new LoadDataRequest();
        loadDataRequest.setId(SAVE_DATA_PREFIX_SESSION + uid);
        final LoadDataResponse response;
        try {
            response = this.pmxmlHandlerPm.exchangeData(loadDataRequest, FUNCTIONKEY_DB_LOAD_DATA, LoadDataResponse.class);
        } catch (PmxmlException e) {
            throw new IllegalStateException(e);
        }
        if (!response.isOk()) {
            this.logger.debug("<getSessionId> could not load session '" + SAVE_DATA_PREFIX_SESSION + uid + "' from pm");
            return null;
        }
        return response.getContent();
    }

    @Override
    public void setSessionId(String uid, String id) {
        final SaveDataRequest saveDataRequest = new SaveDataRequest();
        saveDataRequest.setContent(id);
        saveDataRequest.setId(SAVE_DATA_PREFIX_SESSION + uid);
        try {
            this.pmxmlHandlerPm.exchangeData(saveDataRequest, FUNCTIONKEY_DB_SAVA_DATA);
        }
        catch (PmxmlException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Map<String, ClientConfig> getClientConfigs() {
        if (this.clientConfig.isEmpty()) {
            this.logger.warn("<getClientConfigs> clientConfig isEmpty!");
        }
        return this.clientConfig;
    }

    @Override
    public NavigableMap<DateTime, AppConfig> getAppConfigHistory(String uid) {
        throw new RuntimeException(NOT_IMPLEMENTED);
    }

    @Override
    public AppConfig getAppConfig(int id) {
        throw new RuntimeException(NOT_IMPLEMENTED);
    }

    @Override
    public MessageOfTheDay getMessageOfTheDay(String zone) {
        throw new RuntimeException(NOT_IMPLEMENTED);
    }

    @Override
    public String getMessageOfTheDayByDate(String zone) {
        throw new RuntimeException(NOT_IMPLEMENTED);
    }

    @Override
    public void setMessageOfTheDay(String zone, MessageOfTheDay motd) {
        throw new RuntimeException(NOT_IMPLEMENTED);
    }

    @Override
    public void insertUser(User u) {
        throw new RuntimeException(NOT_IMPLEMENTED);
    }

    @Override
    public boolean changePassword(String uid, String oldPassword, String newPassword) {
        throw new RuntimeException(NOT_IMPLEMENTED);
    }

    @Override
    public boolean resetPassword(String uid, String password) {
        throw new RuntimeException(NOT_IMPLEMENTED);
    }
}