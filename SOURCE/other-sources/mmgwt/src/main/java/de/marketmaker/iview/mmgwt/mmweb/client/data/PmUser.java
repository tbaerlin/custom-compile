/*
 * User.java
 *
 * Created on 29.04.2008 10:19:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Michael Lösch
 */
public class PmUser extends User implements Serializable {
    protected static final long serialVersionUID = 1L;

    public PmUser(User user) {
        super(user);
    }

    public String getUid() {
        return getLogin();
    }

    public PmUser() { //gwt-rpc needs an empty constructor
    }
}