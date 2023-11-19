package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONValue;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * @author umaurer
 * @author Markus Dick
 */
public class BrowserSpecificIE8 extends BrowserSpecificIE {
    private boolean queryGuidefs = true;
    private boolean showHover = false;

    @Override
    public String getBodyStyles() {
        if(this.queryGuidefs) {
            final JSONWrapper jsonWrapper = SessionData.INSTANCE.getGuiDef("BrowserSpecificIE8-showHover"); //$NON-NLS$
            if(JSONWrapper.INVALID != jsonWrapper) {
                if(jsonWrapper != null) {
                    final JSONValue jsonValue = jsonWrapper.getValue();
                    if(jsonValue != null) {
                        final JSONBoolean jsonBoolean = jsonValue.isBoolean();
                        if(jsonBoolean != null) {
                            this.showHover = jsonBoolean.booleanValue();
                        }
                    }
                }
                this.queryGuidefs = false;
            }
        }

        if(this.showHover) {
            return super.getBodyStyles();
        }

        return "bs-ie"; // $NON-NLS$
    }

    @Override
    public boolean isVmlSupported() {
        return true;
    }
}
