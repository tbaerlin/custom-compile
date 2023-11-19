/*
 * AuthItem.java
 *
 * Created on 26.03.12 08:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.widgets;

/**
 * @author Markus Dick
 */
public interface AuthItem {
    public String getItemName();
    public void setItemName(String itemName);
    public String getAuthentication();
    public void setAuthentication(String authentication);
    public String getAuthenticationType();
    public void setAuthenticationType(String authenticationType);
    public String getLocale();
    public void setLocale(String locale);
}
