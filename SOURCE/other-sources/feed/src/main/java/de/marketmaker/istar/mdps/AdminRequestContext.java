/*
 * AdmRequestContext.java
 *
 * Created on 18.06.2010 12:26:34
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps;

import java.util.Arrays;

/**
 * @author oflege
 */
class AdminRequestContext {
    private final int userData;
    
    private final String command;
    
    private final String[] args;

    private String result;

    private boolean failed = false;

    private boolean cancelResponse = false;

    AdminRequestContext(int userData, String command, String[] args) {
        this.userData = userData;
        this.command = command;
        this.args = args;
    }

    @Override
    public String toString() {
        return this.command + " " + Arrays.toString(args);
    }

    int getUserData() {
        return userData;
    }

    String getCommand() {
        return command;
    }

    String[] getArgs() {
        return args;
    }

    String getResult() {
        return String.valueOf(this.result);
    }

    boolean isFailed() {
        return failed;
    }

    void setFailed() {
        this.failed = true;
    }

    void setResult(String s) {
        this.result = s;
    }

    void cancelResponse() {
        this.cancelResponse = true;
    }

    boolean isCancelResponse() {
        return this.cancelResponse;
    }
}
