/*
 * CommandLineOperation.java
 *
 * Created on 15.04.2008 09:46:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CommandLineOperation {
    private String command;

    private String description;

    private String method;

    private int numParams = 0;

    private String objectNameStr;

    private String signature;


    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public String getMethod() {
        return method;
    }

    public int getNumParams() {
        return numParams;
    }

    public String getObjectNameStr() {
        return objectNameStr;
    }

    public String getSignature() {
        return signature;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setObjectNameStr(String objectNameStr) {
        this.objectNameStr = objectNameStr;
    }

    public void setSignature(String signature) {
        this.signature = signature;
        final String[] tokens = signature.split("\\s+");
        this.command = tokens[0];
        this.numParams = tokens.length - 1;
    }
}
