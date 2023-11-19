/*
 * ErrorMessageUtil.java
 *
 * Created on 28.04.14 10:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author oflege
 */
@NonNLS
public class ErrorMessageUtil {

    private static final RegExp MISSING_SELECTORS = RegExp.compile("^missing selectors: \\[([0-9]+).*\\]$");

    private static final int IDX_CONST_FTSE_UK = 4150;

    private static final int IDX_CONST_FTSE_IT = 4151;

    private static final int IDX_CONST_EURONXT_EU = 4152;

    public static String getMessage(ErrorType error) {
        if (error == null) {
            return null;
        }
        if ("permission.denied".equals(error.getCode())) {
            return getPermissionDenied(error.getDescription());
        }
        return null;
    }

    private static String getPermissionDenied(String desc) {
        MatchResult mr = MISSING_SELECTORS.exec(desc);
        if (mr != null) {
            int selector = Integer.parseInt(mr.getGroup(1));
            if (selector == IDX_CONST_FTSE_UK) {
                return I18n.I.permissionDenied4150();
            }
            if (selector == IDX_CONST_FTSE_IT) {
                return I18n.I.permissionDenied4151();
            }
            if (selector == IDX_CONST_EURONXT_EU) {
                return I18n.I.permissionDenied4152();
            }
        }
        return null;
    }
}
