/*
 * RootAppProfile.java
 *
 * Created on 21.11.2008 12:50:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.data;

import java.io.Serializable;

/**
 * @author Oliver Flege
* @author Thomas Kiesgen
*/
public class RootAppProfile extends AppProfile implements Serializable {
    protected static final long serialVersionUID = 2L;

    @Override
    public boolean isFunctionAllowed(String s) {
        return true;
    }

    @Override
    public boolean isNewsAllowed(String s) {
        return true;
    }

    @Override
    public boolean isPageAllowed(String s) {
        return true;
    }

    @Override
    public boolean isProductAllowed(String s) {
        return true;
    }

    @Override
    public String toString() {
        return "AppProfile[ROOT]"; // $NON-NLS-0$
    }
}
