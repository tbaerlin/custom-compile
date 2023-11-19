/*
 * Visit.java
 *
 * Created on 11.01.2010 17:11:14
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.statistics;

/**
 * @author oflege
 */
class Visit {
    private long id;

    private int client;

    private long created;

    private String ip;

    private String userAgent;

    private String userOS;

    private String selector1;

    private String selector2;

    private String selector3;

    private String selector4;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getClient() {
        return client;
    }

    public void setClient(int client) {
        this.client = client;
    }

    public String getSelector1() {
        return selector1;
    }

    public void setSelector1(String selector1) {
        this.selector1 = selector1;
    }

    public String getSelector2() {
        return selector2;
    }

    public void setSelector2(String selector2) {
        this.selector2 = selector2;
    }

    public String getSelector3() {
        return selector3;
    }

    public void setSelector3(String selector3) {
        this.selector3 = selector3;
    }

    public String getSelector4() {
        return selector4;
    }

    public void setSelector4(String selector4) {
        this.selector4 = selector4;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public String getUserOS() {
        return userOS;
    }

    public void setUserOS(String userOS) {
        this.userOS = userOS;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
