/*
 * DmxmlExample.java
 *
 * Created on 20.09.2012 16:47
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package com.vwd.dmxml.examples.common;

import java.io.PrintStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Common base class of the example programs.
 * Implements some common methods and fields as well as the main method with its argument processing.
 * @author Markus Dick
 */
public abstract class DmxmlExample {
    protected static PrintStream out = System.out;

    protected static final Map<String, String> instrumentTypes = new HashMap<String, String>();

    protected static int port = 80;

    protected static String httpLogin; //provided by vwd customer service

    protected static String httpPassword; //provided by vwd customer service

    protected static String host; //provided by vwd customer service

    protected static String zone; //provided by vwd customer service

    protected static String auth; //provided by vwd customer service

    protected static String authType; //provided by vwd customer service

    protected static Locale locale;

    protected static Class<? extends DmxmlExample> exampleImplementation;

    static {
        instrumentTypes.put("BND", "Bond");
        instrumentTypes.put("CER", "Certificate");
        instrumentTypes.put("CUR", "Currency");
        instrumentTypes.put("FND", "Fund");
        instrumentTypes.put("FUT", "Future");
        instrumentTypes.put("GNS", "Bonus Share");
        instrumentTypes.put("IND", "Index");
        instrumentTypes.put("MER", "Commodity");
        instrumentTypes.put("MK", "Economy Cycle Date");
        instrumentTypes.put("NON", "Unknown");
        instrumentTypes.put("OPT", "Option");
        instrumentTypes.put("STK", "Stock");
        instrumentTypes.put("UND", "Underlying");
        instrumentTypes.put("WNT", "Warrant");
        instrumentTypes.put("ZNS", "Interest Rate");
        instrumentTypes.put("WEA", "Weather");
    }

    protected void printValue(String label, String text) {
        out.printf("%10s: %s\n", label, text);
    }

    public static void main(String args[]) throws IllegalAccessException, InstantiationException {
        //process program arguments
        if (args.length < 6) {
            out.println("usage: http-login  http-password  zone  dmxml-auth  dmxml-authType  hostname  [port]");
            System.exit(1);
        }
        if (args.length == 7) {
            port = Integer.parseInt(args[6]);
        }
        host = args[5];
        authType = args[4];
        auth = args[3];
        zone = args[2];
        httpPassword = args[1];
        httpLogin = args[0];

        locale = Locale.getDefault();

        // VERY SIMPLE authentication handling - use just for this sample
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(httpLogin, httpPassword.toCharArray());
            }
        });

        DmxmlExample example = exampleImplementation.newInstance();
        System.exit(example.execute());
    }

    public abstract int execute();
}
