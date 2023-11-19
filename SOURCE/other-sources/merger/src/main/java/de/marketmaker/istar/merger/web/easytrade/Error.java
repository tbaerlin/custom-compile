/*
 * Error.java
 *
 * Created on 27.07.2006 15:41:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Error {
    public enum Level{ ERROR, FATAL_ERROR }

    private final Level level;

    private final String code;

    private final String description;

    public static Error error(String code, String description) {
        return new Error(Level.ERROR, code, description);
    }

    public static Error fatal(String description) {
        return new Error(Level.FATAL_ERROR, "unspecified.error", description);
    }

    private Error(Level level, String code, String description) {
        this.level = level;
        this.code = code;
        this.description = description;
    }

    public String toString() {
        return "Error[" + this.level + ", code=" + this.code + ", " + this.description + "]";
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Level getLevel() {
        return level;
    }
}
