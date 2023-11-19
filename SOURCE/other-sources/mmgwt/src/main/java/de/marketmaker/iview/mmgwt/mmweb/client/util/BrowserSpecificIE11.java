/*
 * BrowserSpecificIE11
 *
 * Created on 27.03.2014 09:48
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.user.client.ui.HTML;

/**
 * IE11 claims to be fully compatible to HTML 5. Hence, it does no longer identify itself as MSIE.
 * Unfortunately it is not compatible regarding z-index layering of object and iframe tags.
 *
 * @author Markus Dick
 */
public class BrowserSpecificIE11 extends BrowserSpecific {
    @Override
    public void fixDivBehindPdfObjectBugPart2(HTML divContainingTheObjectTag) {
        BrowserSpecificIE10.doFixDivBehindPdfObjectBugPart2(divContainingTheObjectTag);
    }
}
