package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.extjs.gxt.ui.client.widget.Window;

/**
 * @author umaurer
 */
public class BrowserSpecificGecko18 extends BrowserSpecificGecko {
    @Override
    public void fixCursorBug(Window window) {
        window.getElement().getStyle().setProperty("position", "fixed"); // $NON-NLS$
    }
}
