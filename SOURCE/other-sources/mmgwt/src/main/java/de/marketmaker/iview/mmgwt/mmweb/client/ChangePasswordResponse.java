/*
 * MmwebRequest.java
 *
 * Created on 07.08.2008 10:25:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum ChangePasswordResponse {
    OK,
    WRONG_OLD_PASSWORD,
    REPEATED_RECENT_PASSWORD,
    INTERNAL_ERROR,
    INITIAL_PASSWORD,
    PASSWORD_IS_TOO_SHORT,
    UNKNOWN_USER_OR_PASSWORD
}
