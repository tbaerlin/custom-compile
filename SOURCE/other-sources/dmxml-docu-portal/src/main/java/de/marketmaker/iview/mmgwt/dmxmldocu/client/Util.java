/*
 * JsUtil.java
 *
 * Created on 23.03.12 16:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;
import de.marketmaker.itools.gwtutil.client.util.Firebug;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class Util {

    /**
     * Get a value from the host html page. To define the key "rpcUrl", add the following code to your index.html page:
     * <pre>
     * &lt;script type="text/javascript"&gt;
     *   var ServerSettings = {
     *       rpcUrl: "/docman-xml-1/vwdgroup/docman.rpc"
     *   };
     * &lt;/script&gt;
     * </pre>
     * @param key .
     * @return The value, that is defined for the key, or null, if the key is not specified in the index.html page.
     */
    public static String getServerSetting(String key) {
        try {
            return Dictionary.getDictionary("ServerSettings").get(key); // $NON-NLS$
        } catch (Exception e) {
            return null;
        }
    }

    public static String getModuleName() {
        final String url = GWT.getModuleBaseURL();  // e.g., http://server.com/foo/ : we need to extract foo
        final int p = url.lastIndexOf('/', url.length() - 2);
        return url.substring(p + 1, url.length() - 1);
    }


    public static String getZoneName() {
        return getServerSetting("zoneName"); // $NON-NLS-0$
    }
    
    public static void onError(String userMessage, Throwable throwable) {
        Firebug.error(userMessage, throwable);
        Window.alert(userMessage);
    }
    
    public static void onError(Throwable throwable) {
        onError(throwable.getMessage(), throwable);
    }
    
}
