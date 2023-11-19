/*
 * Version.java
 *
 * Created on 10.06.2008 12:49:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

/**
 * Constants whose properties will be created by the build script at compile time, so
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * ! DO NOT ADD A Version.properties FILE !
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * 
 * @author Oliver Flege
 */
public interface Version extends Constants {
    public static final Version INSTANCE = (Version) GWT.create(Version.class);

    String build();
}
