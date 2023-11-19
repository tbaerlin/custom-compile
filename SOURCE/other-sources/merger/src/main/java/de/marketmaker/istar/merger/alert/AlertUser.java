/*
 * AlertUser.java
 *
 * Created on 16.12.2008 14:53:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

import java.io.Serializable;
import java.util.Locale;

/**
 * Data Transfer Object for alert users, this class matches more or less
 * the DB content in the AlertServer Database
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AlertUser implements Serializable {
    static final long serialVersionUID = 1L;

    private boolean active;

    private Locale locale = Locale.GERMAN;

    private String emailAddress1;

    private String emailAddress2;

    private String emailAddress3;

    private String sms;

    public String getEmailAddress1() {
        return emailAddress1;
    }

    public String getEmailAddress2() {
        return emailAddress2;
    }

    public String getEmailAddress3() {
        return emailAddress3;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getSms() {
        return sms;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setEmailAddress1(String emailAddress1) {
        this.emailAddress1 = emailAddress1;
    }

    public void setEmailAddress2(String emailAddress2) {
        this.emailAddress2 = emailAddress2;
    }

    public void setEmailAddress3(String emailAddress3) {
        this.emailAddress3 = emailAddress3;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    @Override
    public String toString() {
        return "AlertUser["
                + "active=" + this.active
                + ", locale=" + this.locale
                + ", email1=" + this.emailAddress1
                + ", email2=" + this.emailAddress2
                + ", email3=" + this.emailAddress3
                + ", sms=" + this.sms
                + "]";
    }
}