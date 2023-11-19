/*
 * AsZoneProvider.java
 *
 * Created on 06.11.2015 15:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.springframework.beans.BeansException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.merger.web.DefaultZoneProvider;

/**
 * Provides exactly one Web zone based on a given standardized properties file.
 *
 * <p>It's purpose is to provide a Web zone without adding an entry to <code>zones.prop</code> and
 * without adding a specialized zone properties file, which eases the PM/advisory solution
 * installation process.</p>
 *
 * <p>The zone name is usually provided by PM via an application context variable, i.e.,
 * <code>env.zone</code>, see {@link #zoneName}.</p>
 *
 * <p>The standardized zone properties file name can be hard coded in the application context file,
 * see {@link #zonePropFile}.</p>
 *
 * @author mdick
 */
@ManagedResource
public class AsZoneProvider extends DefaultZoneProvider {
    private String zoneName;

    private String zonePropFile;

    @Override
    protected void initApplicationContext() throws BeansException {
        try {
            setZoneSpec(createZoneSpecResource());
            super.initApplicationContext();
        } catch (Exception e) {
            this.logger.error("<initApplicationContext> failed", e);
        }
    }

    private InputStreamResource createZoneSpecResource() {
        try {
            final String zoneSpec = "" + this.zoneName + "=" + this.zonePropFile;
            return new InputStreamResource(new ByteArrayInputStream(zoneSpec.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Creating zone spec failed. UTF-8 is not supported", e);
        }
    }

    @ManagedOperation
    @Override
    public void loadZones() {
        //InputStreamResource can only be read once, so create a new one!
        setZoneSpec(createZoneSpecResource());
        super.loadZones();
    }

    @ManagedAttribute
    public String getZoneName() {
        return zoneName;
    }

    @ManagedAttribute
    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    @ManagedAttribute
    public String getZonePropFile() {
        return zonePropFile;
    }

    @ManagedAttribute
    public void setZonePropFile(String zonePropFile) {
        this.zonePropFile = zonePropFile;
    }

    @Override
    public String toString() {
        return "AsZoneProvider{" +
                "zoneName='" + zoneName + '\'' +
                ", zonePropFile='" + zonePropFile + '\'' +
                "} " + super.toString();
    }
}
