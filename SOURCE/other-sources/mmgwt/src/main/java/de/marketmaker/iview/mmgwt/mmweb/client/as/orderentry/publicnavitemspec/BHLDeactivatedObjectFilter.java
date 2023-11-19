/*
 * BHLDeactivatedObjectFilter.java
 *
 * Created on 31.01.14 14:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec;

import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * This filter does not accept BHL numbers of objects, e.g. depots or accounts where
 * the depot number starts with '[', because BHL uses '[' and ']' wrapped around the
 * depot/account number to mark depots/accounts that have been discontinued by their
 * investors. The concept of deactivated depots is not used.
 *
 * After negotiations, ordering project management decided that checking if the depot
 * number contains a '[' is sufficient.
 *
 * @author Markus Dick
 */
public class BHLDeactivatedObjectFilter implements Filter<String> {
    @Override
    public boolean isAcceptable(String objectNo) {
        return !(StringUtil.hasText(objectNo) && objectNo.indexOf('[') > -1);
    }
}
