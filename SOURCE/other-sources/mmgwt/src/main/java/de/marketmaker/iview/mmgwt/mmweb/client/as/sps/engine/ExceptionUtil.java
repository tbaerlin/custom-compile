/*
 * ExceptionUtil.java
 *
 * Created on 15.05.2014 08:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.event.shared.UmbrellaException;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Markus Dick
 */
@NonNLS
public final class ExceptionUtil {
    private ExceptionUtil() {
        /* avoid instantiation */
    }

    static boolean hasOnlyPropertyIsNullExceptions(Exception e) {
        if(e instanceof UmbrellaException) {
            for (Throwable throwable : ((UmbrellaException) e).getCauses()) {
                if (!(throwable instanceof SpsPropertyIsNullException)) {
                    return false;
                }
            }
            return true;
        }

        return e instanceof SpsPropertyIsNullException;
    }

    static String toMessage(String prefix, Exception e) {
        if(e instanceof UmbrellaException) {
            final StringBuilder sb = new StringBuilder(prefix);

            for (Throwable throwable : ((UmbrellaException) e).getCauses()) {
                sb.append(throwable.getMessage()).append("\n");
            }
            return sb.toString();
        }

        return prefix + " " + e.getMessage();
    }

    public static void logErrorOrPropertyIsNull(String locationMessage, Exception e) {
        if(hasOnlyPropertyIsNullExceptions(e)) {
            final String s = toMessage(locationMessage, e);
            DebugUtil.showDeveloperNotification(s);
            Firebug.debug(s);
        }
        else {
            DebugUtil.showDeveloperNotification(locationMessage, e);
            Firebug.error(locationMessage, e);
        }
    }
}
