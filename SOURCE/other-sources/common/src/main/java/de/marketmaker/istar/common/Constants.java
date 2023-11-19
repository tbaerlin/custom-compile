/*
 * Constants.java
 *
 * Created on 15.11.13 10:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common;

import java.io.File;
import java.lang.management.ManagementFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.util.StringUtils;

/**
 * @author oflege
 */
public class Constants {

    public static final String PID;

    public static final String DEV_DOMAIN = "dev";

    public static final String DOMAIN_ID = System.getProperty("domainid", DEV_DOMAIN);

    public static final String MACHINE_NAME;

    public static final String APP_NAME;

    static {
        final String[] pidAndHost = ManagementFactory.getRuntimeMXBean().getName().split("@");

        PID = pidAndHost[0];

        String fqHost = pidAndHost[1];
        int p = fqHost.indexOf('.');

        if (p > 0) {
            MACHINE_NAME = System.getProperty("machineid", fqHost.substring(0, p));
        }
        else {
            MACHINE_NAME = System.getProperty("machineid", fqHost);
        }

        APP_NAME = getAppName();
    }

    private static String getAppName() {
        String result = System.getProperty("appid", getWebappName());
        if (StringUtils.hasText(result)) {
            return result;
        }
        final String home = System.getProperty("istar.home");
        if (StringUtils.hasText(home)) {
            File dir = new File(home);
            if (dir.isDirectory()) {
                return dir.getName();
            }
        }
        String cmd = System.getProperty("sun.java.command");
        if (cmd != null) {
            int p = cmd.indexOf(' ');
            return (p < 1) ? cmd : cmd.substring(0, p);
        }
        return "java";
    }

    /**
     * In a webapp context, different appids should be used for different webapps, so that
     * <tt>-Dappid=</tt> cannot be used. Instead, the appid can be defined in the webapp's
     * deployment descriptor.
     * <pre>
     * &lt;Context>
     *   &lt;Environment name="appid" value="dmxml-1" type="java.lang.String" override="false"/>
     * &lt;/Context>
     * </pre>
     * @return appid as defined in webapp's deployment descriptor
     */
    public static String getWebappName() {
        return getEnvironmentValue("appid");
    }

    public static String getFeatureFlags() {
        return getProperty("featureFlags", "prod");
    }

    public static String getProperty(String name, String defaultValue) {
        final String fromEnv = getEnvironmentValue(name);
        if (fromEnv != null) {
            return fromEnv;
        }
        return System.getProperty(name, defaultValue);
    }

    /**
     * @return the (String) value of the jndi context object <tt>java:comp/env/<em>key</em></tt>
     * or null if no binding for key exists
     */
    private static String getEnvironmentValue(String key) {
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");

            return (String) envCtx.lookup(key);
        } catch (NamingException e) {
            return null;
        }
    }
}
