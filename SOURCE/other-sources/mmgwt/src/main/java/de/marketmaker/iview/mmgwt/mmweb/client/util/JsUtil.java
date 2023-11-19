package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.i18n.client.Dictionary;

/**
 * @author umaurer
 * Container for JavaScript utilities.
 */
public class JsUtil {
    public static String escapeUrl(String url) {
        if (url.startsWith("http:")) { // $NON-NLS-0$
            return "http:" + escape(url.substring(5)); // $NON-NLS-0$
        }
        if (url.startsWith("https:")) { // $NON-NLS-0$
            return "https:" + escape(url.substring(6)); // $NON-NLS-0$
        }
        return escape(url);
    }

    public static native String escape(final String text) /*-{
        return escape(text);
    }-*/;

    public static native String getScreenInfo() /*-{
        var depth = screen.pixelDepth ? screen.pixelDepth : screen.colorDepth;
        return screen.width + "x" + screen.height + "x" + depth; // $NON-NLS-0$ $NON-NLS-1$
    }-*/;


    /**
     * Get a value from the host html page. To define the key "rpcUrl", add the following code to your index.html page:
     * <pre>
     * &lt;script type="text/javascript"&gt;
     *   var ServerSettings = {
     *       rpcUrl: "/docman-xml-1/vwdgroup/docman.rpc"
     *   };
     * &lt;/script&gt;
     * </pre>
     *
     * @param key .
     * @return The value, that is defined for the key, or null, if the key is not specified in the index.html page.
     */
    public static String getServerSetting(String key) {
        try {
            return Dictionary.getDictionary("serverSettings").get(key); // $NON-NLS$
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getMetaValue(String name) {
        NodeList<Element> tags = Document.get().getElementsByTagName("meta"); // $NON-NLS$
        for (int i = 0; i < tags.getLength(); i++) {
            MetaElement metaTag = ((MetaElement) tags.getItem(i));
            if (metaTag.getName().equals(name)) {
                return metaTag.getContent();
            }
        }
        return null;
    }
}
