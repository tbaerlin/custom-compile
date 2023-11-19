/*
 * OrderEntryServiceException.java
 *
 * Created on 05.02.2015 17:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author mdick
 */
public class OrderEntryServiceException extends RuntimeException implements IsSerializable{
    private String code;

    public OrderEntryServiceException() {

    }

    public OrderEntryServiceException(String code, String message) {
        super(message);
        this.code = code;
    }

    public OrderEntryServiceException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
