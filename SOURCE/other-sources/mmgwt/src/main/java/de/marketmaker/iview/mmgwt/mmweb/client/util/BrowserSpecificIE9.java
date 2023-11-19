/*
 * BrowserSpecificIE9.java
 *
 * Created on 17.07.2015 10:21
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.user.client.ui.Focusable;

/**
 * @author mdick
 */
public class BrowserSpecificIE9 extends BrowserSpecificIE {
    public void setFocus(Focusable focusable, boolean focus) {
        if(focusable == null) {
            return;
        }
        focusable.setFocus(focus);   //Calling setFocus twice is necessary due to an IE 9 focus Bug
        focusable.setFocus(focus);
    }
}
