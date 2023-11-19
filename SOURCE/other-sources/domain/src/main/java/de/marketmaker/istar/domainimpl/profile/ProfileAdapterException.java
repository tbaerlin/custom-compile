/*
 * ProfileAdapterException.java
 *
 * Created on 26.02.15 14:11
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.io.IOException;

/**
 * @author oflege
 */
public class ProfileAdapterException extends IOException {
    static final long serialVersionUID = 1L;

    public ProfileAdapterException(String message) {
        super(message);
    }
}
