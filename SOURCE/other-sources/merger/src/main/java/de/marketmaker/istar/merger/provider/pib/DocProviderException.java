/*
 * DocProviderException.java
 *
 * Created on 07.01.14 10:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pib;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author zzhao
 */
public class DocProviderException extends MergerException {

    private final String errorCode;

    public DocProviderException(String error, String message) {
        super(message);
        this.errorCode = error;
    }

    @Override
    public String getCode() {
        return this.errorCode;
    }
}
