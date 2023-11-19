/*
 * AuthorizationInfo.java
 *
 * Created on 23.03.12 18:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import java.io.Serializable;

/**
 * Collects auth
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class AuthorizationInfo implements Serializable {

    public AuthorizationInfo(String authentication, String authenticationType) {
        this.authentication = authentication;
        this.authenticationType = authenticationType;
    }

    public AuthorizationInfo() {
    }
    
    public AuthorizationInfo withCurrentZone() {
        this.zoneName = Util.getZoneName();
        return this;
    }

    private String authentication; 
    private String authenticationType;
    private String zoneName;


    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }
}
