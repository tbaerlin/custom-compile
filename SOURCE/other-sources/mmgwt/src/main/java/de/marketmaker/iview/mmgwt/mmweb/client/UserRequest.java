package de.marketmaker.iview.mmgwt.mmweb.client;

import java.io.Serializable;

/**
 * @author umaurer
 */
public class UserRequest implements Serializable {
    protected static final long serialVersionUID = 1L;

    private boolean pmZone;
    private String login;
    private String password;
    private String module;
    private String screenInfo;
    private String locale;

    private UserRequest() {
    }

    public UserRequest(boolean pmZone, String login, String password, String module, String screenInfo, String locale) {
        this.pmZone = pmZone;
        this.login = login;
        this.password = password;
        this.module = module;
        this.screenInfo = screenInfo;
        this.locale = locale;
    }

    public boolean isPmZone() {
        return pmZone;
    }

    public String getLogin() {
        return login;
    }

    public String getLoginUpperCase() {
        return this.login == null ? null : login.toUpperCase();
    }


    public String getPassword() {
        return password;
    }

    public String getModule() {
        return module;
    }

    public String getScreenInfo() {
        return screenInfo;
    }

    public String getLocale() {
        return locale;
    }
}
