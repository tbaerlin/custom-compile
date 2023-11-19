/*
 * HasColumnHeaderHandlers.java
 *
 * Created on 18.03.2015 09:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author mdick
 */
public interface HasColumnHeaderHandlers extends HasHandlers {
    HandlerRegistration addColumnHeaderHandler(ColumnHeaderHandler handler);
}
