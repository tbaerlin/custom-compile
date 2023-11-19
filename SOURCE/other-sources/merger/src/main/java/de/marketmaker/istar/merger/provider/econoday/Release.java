/*
 * Release.java
 *
 * Created on 15.03.12 16:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.io.Serializable;
import java.util.Set;

import org.joda.time.DateTime;

/**
 * @author zzhao
 */
public class Release extends AbstractRelease implements Serializable {

    private final Event event;

    private final int uid;

    private DateTime modifyDate;

    private String releasedFor;

    private DateTime releasedOn;

    private DateTime releasedOnGmt;

    private DateTime previousDate;

    private Set<Attribute> attributes;

    private String articleUrl;


    Release(Event event, int uid) {
        this.event = event;
        this.uid = uid;
    }

    public Event getEvent() {
        return event;
    }

    public int getUid() {
        return uid;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    void setArticleUrl(String articleUrl) {
        this.articleUrl = articleUrl;
    }

    public Set<Attribute> getAttributes() {
        return attributes;
    }

    void setAttributes(Set<Attribute> attributes) {
        this.attributes = attributes;
    }

    public DateTime getPreviousDate() {
        return previousDate;
    }

    void setPreviousDate(DateTime previousDate) {
        this.previousDate = previousDate;
    }

    public String getReleasedFor() {
        return releasedFor;
    }

    void setReleasedFor(String releasedFor) {
        this.releasedFor = releasedFor;
    }

    public DateTime getReleasedOn() {
        return releasedOn;
    }

    void setReleasedOn(DateTime releasedOn) {
        this.releasedOn = releasedOn;
    }

    public DateTime getReleasedOnGmt() {
        return releasedOnGmt;
    }

    void setReleasedOnGmt(DateTime releasedOnGmt) {
        this.releasedOnGmt = releasedOnGmt;
    }

    public DateTime getModifyDate() {
        return modifyDate;
    }

    void setModifyDate(DateTime modifyDate) {
        this.modifyDate = modifyDate;
    }

    @Override
    public String toString() {
        return "Release{" +
                "uid=" + uid +
                '}';
    }
}
