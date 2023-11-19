package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TextBoxBase;

/**
 * @author umaurer
 */
public class BrowserSpecificIE extends BrowserSpecific {
    public void focus(final Component component) {
        final Timer timer = new Timer(){
            public void run() {
                component.focus();
            }
        };
        timer.schedule(600);
    }

    @Override
    public void setCardContentHideMode(Component component) {
        component.setHideMode(Style.HideMode.OFFSETS);
    }

    /**
     * Calling this method is necessary when setting a read-only text box to editable after it got the focus.
     * As intended by <b>Microsoft</b>, a text box is going to take two clicks before you can start typing,
     * because on the first click it is still read-only, so the element would not focus. You need to either
     * call select after changing the read-only property, or your users will have to double click. This is
     * because IE does not focus an element that is read-only as all other browser do.
     *
     * @link http://stackoverflow.com/questions/3764440/ie-readonly-textarea-problem
     */
    @Override
    public void setReadOnly(TextBoxBase textBoxBase, boolean readonly) {
        super.setReadOnly(textBoxBase, readonly);
        if(!readonly) {
            InputElement.as(textBoxBase.getElement()).select();
        }
    }

    /**
     * Microsoft Internet Explorer has a maximum uniform resource locator (URL) length of 2,083 characters.
     * Internet Explorer also has a maximum path length of 2,048 characters. This limit applies to both
     * POST request and GET request URLs.
     *
     * If you are using the GET method, you are limited to a maximum of 2,048 characters, minus the number
     * of characters in the actual path.
     *
     * However, the POST method is not limited by the size of the URL for submitting name/value pairs.
     * These pairs are transferred in the header and not in the URL.
     *
     * Applies to IE4 - IE9 ; IE10 unknown.
     *
     * @link http://support.microsoft.com/kb/208427/EN-US
     */
    @Override
    public boolean isUriTooLong(String absoluteUrlWithProtocol) {
        return absoluteUrlWithProtocol != null && absoluteUrlWithProtocol.length() > 2048;
    }
}
