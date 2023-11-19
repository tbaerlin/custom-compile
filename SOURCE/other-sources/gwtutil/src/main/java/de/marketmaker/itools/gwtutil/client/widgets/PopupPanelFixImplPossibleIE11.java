/*
 * PopupPanelFixImplPossibleIE11.java
 *
 * Created on 27.03.2014 11:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Element;

/**
 * IE11 claims to be fully compatible to HTML 5. Hence, it does no longer identify itself as MSIE.
 * Unfortunately it is not compatible regarding z-index layering of object and iframe tags.
 *
 * In GWT 2.6 IE11 is identified as a gecko1_8, which requires us to evaluate the user agent
 * property of the navigator by our own.
 *
 * @author Markus Dick
 */
public class PopupPanelFixImplPossibleIE11 extends PopupPanelFixImpl {
    private final PopupPanelFixImpl delegate;

    public static native String getUserAgent() /*-{
        return navigator.userAgent.toLowerCase();
    }-*/;

    public PopupPanelFixImplPossibleIE11() {
        final String userAgent = getUserAgent();

        if(userAgent.contains("trident/7")) { // $NON-NLS$
            this.delegate = new PopupPanelFixImplTrident();
        }
        else {
            this.delegate = new PopupPanelFixImpl();
        }
    }

    @Override
    public void addFrameDummy(Element e) {
        this.delegate.addFrameDummy(e);
    }

    @Override
    public void addFrameDummyForSuggestPopup(Element e) {
        this.delegate.addFrameDummyForSuggestPopup(e);
    }
}
