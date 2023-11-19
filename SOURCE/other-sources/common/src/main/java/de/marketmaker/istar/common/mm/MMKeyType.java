/*
 * MMKeyType.java
 *
 * Created on 17.03.2005 08:02:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mm;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum MMKeyType {
    SECURITY(1),

    SECURITY_WKN(2),

    SECURITY_ISIN(3),

    SECURITY_WKN_EXCHANGE(4),

    FOLDER(5),

    FOLDER_NAME(6),

    DEPOSIT(7),

    DEPOSIT_NAME(8),

    ACCOUNT(9),

    ACCOUNT_NAME(10),

    INVESTOR(11),

    INVESTOR_NAME(12),

    SECURITY_NAME(13),

    INVESTORGROUP(14),

    INVESTORGROUP_NAME(15),

    DEPOSIT_NUMBER(16),

    ACCOUNT_NUMBER(17),

    INVESTOR_NUMBER(18);

    private final int value;

    private MMKeyType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
