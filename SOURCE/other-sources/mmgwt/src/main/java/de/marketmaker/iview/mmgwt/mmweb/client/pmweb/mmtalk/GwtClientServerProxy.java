package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk;

import com.google.gwt.core.shared.GWT;

import java.util.Date;

/**
 * User: umaurer
 * Date: 19.12.13
 * Time: 17:13
 */
public abstract class GwtClientServerProxy {
    public abstract String formatIso8601(String sDate);

    public static GwtClientServerProxy createDateFormatInstance() {
        return GWT.isClient()
                ? new GwtClientServerProxyGwt()
                : null;
    }

    public <C> C getClassInstance(Class<C> clazz) {
        return null; // only available in server mode
    }
}
