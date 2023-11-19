package de.marketmaker.itools.gwtutil.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Author: umaurer
 * Created: 18.03.14
 */
public interface PasteHandler extends EventHandler {
    void onPaste(PasteEvent e);
}
