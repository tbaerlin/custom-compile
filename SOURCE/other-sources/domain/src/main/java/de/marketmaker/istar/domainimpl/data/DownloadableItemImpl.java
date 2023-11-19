/*
 * DownloadableItemImpl.java
 *
 * Created on 20.09.2006 14:40:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.data.DownloadableItem;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DownloadableItemImpl implements Serializable, DownloadableItem {
    protected static final long serialVersionUID = 1L;

    private final Integer year;

    private final String description;

    private final String url;

    private final DateTime date;

    private final Integer filesize;

    private final Source source;

    private final String country;

    private final String language;

    private final InstrumentTypeEnum instrumentType;

    private final String marketAdmission;

    private final String permissionType;

    private final Type type;

    public DownloadableItemImpl(Integer year, String description, String url,
            DateTime date, Integer filesize) {
        this(year, Type.Unknown, description, url, date, filesize, null, null, null, null, null, null);
    }

    public DownloadableItemImpl(Integer year, Type type, String description, String url,
            DateTime date, Integer filesize, Source source, String country, String language,
            InstrumentTypeEnum instrumentType, String marketAdmission) {
        this(year, type, description, url, date, filesize, source, country, language,
                instrumentType, marketAdmission, null);
    }

    public DownloadableItemImpl(Integer year, Type type, String description, String url,
            DateTime date, Integer filesize, Source source, String country, String language,
            InstrumentTypeEnum instrumentType, String marketAdmission, String permissionType) {
        this.year = year;
        this.type = type;
        this.description = description;
        this.url = url;
        this.date = date;
        this.filesize = filesize;
        this.source = source;
        this.country = country;
        this.language = language;
        this.instrumentType = instrumentType;
        this.marketAdmission = marketAdmission;
        this.permissionType = permissionType;
    }

    public DateTime getDate() {
        return date;
    }

    public Integer getFilesize() {
        return filesize;
    }

    public Integer getYear() {
        return year;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public Source getSource() {
        return source;
    }

    public String getCountry() {
        return country;
    }

    public String getLanguage() {
        return language;
    }

    public InstrumentTypeEnum getInstrumentType() {
        return this.instrumentType;
    }

    public String getMarketAdmission() {
        return this.marketAdmission;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public String toString() {
        return "DownloadableItemImpl[year=" + year
                + ", type=" + type
                + ", description=" + description
                + ", url=" + url
                + ", date=" + date
                + ", filesize=" + filesize
                + ", source=" + source
                + ", country=" + country
                + ", language=" + language
                + ", instrumentType=" + instrumentType
                + ", marketAdmission=" + marketAdmission
                + ", permissionType=" + permissionType
                + "]";
    }
}
