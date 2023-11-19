/*
 * BewCommand.java
 *
 * Created on 07.09.2010 15:46:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Size;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;

/**
 * @author oflege
 */
public class BewCommand {
    private String customer;

    private String license;

    private String version = "1.0.0.2";

    @NotNull
    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    @NotNull
    @Size(min = 19, max = 19)
    public String getLicense() {
        return this.license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    boolean isWithValidCredentials(HttpServletRequest request) {
        final Zone z = (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);
        final Map<String, Object> map = z.getContextMap(getCustomer());
        return getLicense().equals(map.get("license"));
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
