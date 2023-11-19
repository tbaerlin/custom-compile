/*
 * LoginData.java
 *
 * Created on 27.03.12 14:44
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import java.io.Serializable;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class LoginData implements Serializable {
    private String zone;
    private String password; // TODO only send hash?

    public LoginData(String password) {
        this.password = password;
    }

    public LoginData(String zone, String password) {
        this.zone = zone;
        this.password = password;
    }

    public LoginData withCurrentZone() {
        this.zone = Util.getZoneName();
        return this;
    }

    public LoginData() {
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginData{zone='" + zone + '\'' + ", password='" + password + '\'' + '}'; // $NON-NLS$
    }
}
