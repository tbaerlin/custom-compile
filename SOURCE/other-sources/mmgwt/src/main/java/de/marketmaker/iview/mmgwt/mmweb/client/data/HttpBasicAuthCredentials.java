/*
 * HttpBasicAuthCredentials.java
 *
 * Created on 14.06.2016 14:27
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.data;

import java.io.Serializable;

import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * HTTP Basic Auth credentials, e.g., for authenticating GIS Portal via a pre-flight XHR request before opening the GIS Portal window.
 * Note: It is highly insecure to transport the password to the client to trigger a XHR request with it.
 * It would be more secure to use OAuth or some kind of a security token, but we do not have enough time to implement such a solution.
 * Additionally, GIS Portal is not accessible via a secure HTTP connection, so every man in the middle attacker can easily read the transmitted passwords.
 * FFM insists on HTTP Basic Auth for the GIS Portal, because it is the easiest way for them and they do not need to touch their out-dated JOOMLA.
 *
 * @author mdick
 */
@NonNLS
public class HttpBasicAuthCredentials implements Serializable {
    protected static final long serialVersionUID = 1L;

    private String user;

    private String password;

    @SuppressWarnings("unused")
    public HttpBasicAuthCredentials() {
        // necessary for GWT RPC do not remove!
    }

    public HttpBasicAuthCredentials(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "HttpBasicAuthCredentials{" +
                "user='" + this.user + '\'' +
                '}';
    }
}
