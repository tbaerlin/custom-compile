/*
 * FlashCheck.java
 *
 * Created on 10/27/14 2:41 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.user.client.Window;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Stefan Willenbrock
 */
@NonNLS
public class FlashCheck {

    public final static String MIN_VERSION = "8";

    public static boolean isFlashAvailable() {
        return isFlashReadyPlatform() || hasFlashPlayerVersion(MIN_VERSION);
    }

    private static native boolean hasFlashPlayerVersion(String version) /*-{
        if (!$wnd.swfobject) {
            return true;
        }
        return $wnd.swfobject.hasFlashPlayerVersion(version)
    }-*/;

    private static boolean isFlashReadyPlatform() {
        final String platform = Window.Navigator.getPlatform();
        switch (platform) {
            case "Win32":
                return true;
            default:
                return false;
        }
    }
}
