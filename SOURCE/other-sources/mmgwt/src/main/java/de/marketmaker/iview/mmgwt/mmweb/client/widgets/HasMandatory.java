/*
 * HasMandatory.java
 *
 * Created on 06.01.2016 07:56
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

/**
 * Indicates whether or not a widget is mandatory, i.e., the user has to input a value.
 * @author mdick
 */
public interface HasMandatory {
    void setMandatory(boolean mandatory);
    boolean isMandatory();
}
