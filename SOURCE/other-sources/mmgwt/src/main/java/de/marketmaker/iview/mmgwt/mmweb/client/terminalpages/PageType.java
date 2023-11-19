/*
 * PageType.java
 *
 * Created on 08.04.2008 17:11:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.terminalpages;

import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum PageType {
    /**
     * @deprecated should no longer be used; since it appears in every serialized PagesWorkspaceConfig,
     *             it cannot be removed.
     */
    @Deprecated
    REUTERS {
        public String toString() {
            return "Reuters"; // $NON-NLS-0$
        }

        public String getControllerName() {
            return "P_R"; // $NON-NLS-0$
        }
    },
    DZBANK {
        public String toString() {
            return "DZ BANK"; // $NON-NLS-0$
        }

        public String getControllerName() {
            return DzPageController.KEY;
        }
    },
    VWD {
        public String toString() {
            return "vwd"; // $NON-NLS-0$
        }

        public String getControllerName() {
            return VwdPageController.KEY;
        }
    },
    CUSTOM {
        public String toString() {
            return Customer.INSTANCE.getCustomPageTypeString();
        }

        public String getControllerName() {
            return VwdPageController.KEY;
        }

    };

    public abstract String getControllerName();
}
