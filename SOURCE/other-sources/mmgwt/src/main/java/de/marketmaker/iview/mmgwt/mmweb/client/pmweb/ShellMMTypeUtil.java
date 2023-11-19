/*
 * ShellMMTypeUtil.java
 *
 * Created on 17.05.13 14:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ShellMMUtil;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum.*;

/**
 * @author Markus Dick
 */
public class ShellMMTypeUtil extends ShellMMUtil {
    public static String getControllerId(ShellMMType mmType) {
        switch (mmType) {
            case ST_INHABER:
                return PmWebModule.HISTORY_TOKEN_INVESTOR_PROFILE;
            case ST_PORTFOLIO:
                return PmWebModule.HISTORY_TOKEN_PORTFOLIO;
            case ST_DEPOT:
                return PmWebModule.HISTORY_TOKEN_DEPOT;
            case ST_KONTO:
                return PmWebModule.HISTORY_TOKEN_ACCOUNT;
            case ST_PERSON:
                return PmWebModule.HISTORY_TOKEN_PERSON;
            case ST_INTERESSENT:
                return PmWebModule.HISTORY_TOKEN_PROSPECT;
            default:
                throw new IllegalArgumentException("No controller defined for mm type: " + mmType); // $NON-NLS$
        }
    }

    public static String getIconKey(ShellMMType shellMMType) {
        return getIconKey(toMMClassIndex(shellMMType));
    }

    public static String getIconKey(MMClassIndex mmClassIndex) {
        if(mmClassIndex == null) {
            Firebug.warn("ShellMMTypeUtil <getIcon> no icon defined for mmClassIndex: null");
            return null;
        }

        switch (mmClassIndex) {
            case CI_T_INHABER:
                return "pm-investor"; // $NON-NLS$
            case CI_T_PORTFOLIO:
                return "pm-investor-portfolio"; // $NON-NLS$
            case CI_T_DEPOT:
                return "pm-investor-depot"; // $NON-NLS$
            case CI_T_KONTO:
                return "pm-investor-account"; // $NON-NLS$
            case CI_T_PERSON:
                return "pm-investor-person"; // $NON-NLS$
            case CI_T_INTERESSENT:
                return "pm-investor-prospect"; // $NON-NLS$
        }

        Firebug.warn("ShellMMTypeUtil <getIcon> no icon defined for mmClassIndex: " + mmClassIndex);
        return null;
    }

    public static MMClassIndex toMMClassIndex(ShellMMType shellMMType) {
        switch (shellMMType) {
            case ST_INHABER:
                return MMClassIndex.CI_T_INHABER;
            case ST_PORTFOLIO:
                return MMClassIndex.CI_T_PORTFOLIO;
            case ST_DEPOT:
                return MMClassIndex.CI_T_DEPOT;
            case ST_KONTO:
                return MMClassIndex.CI_T_KONTO;
            case ST_PERSON:
                return MMClassIndex.CI_T_PERSON;
            case ST_INTERESSENT:
                return MMClassIndex.CI_T_INTERESSENT;
        }
        Firebug.warn("ShellMMTypeUtil <toMMClassIndex> no mapping defined for shellMMType: " + shellMMType);
        return null;
    }

    @SuppressWarnings("unused")
    public static ShellMMType toShellMMType(MMClassIndex mmClassIndex) {
        switch (mmClassIndex) {
            case CI_T_INHABER:
                return ShellMMType.ST_INHABER;
            case CI_T_PORTFOLIO:
                return ShellMMType.ST_PORTFOLIO;
            case CI_T_DEPOT:
                return ShellMMType.ST_DEPOT;
            case CI_T_KONTO:
                return ShellMMType.ST_KONTO;
            case CI_T_PERSON:
                return ShellMMType.ST_PERSON;
            case CI_T_INTERESSENT :
                return ShellMMType.ST_INTERESSENT;
        }
        Firebug.warn("ShellMMTypeUtil <toShellMMType> no mapping defined for mmClassIndex: " + mmClassIndex);
        return null;
    }

    public static String[] toStringArray (Collection<ShellMMType> types) {
        final String[] result = new String[types.size()];
        final Iterator<ShellMMType> it = types.iterator();
        for(int i = 0; i < result.length; i++) {
            result[i] = it.next().name();
        }
        return result;
    }

    public static String toInstrumentTypeString(ShellMMType shellMMType) {
        if(shellMMType == null) {
            return NON.name();
        }
        switch(shellMMType) {
            case ST_AKTIE:
                return STK.name();
            case ST_FOND:
                return FND.name();
            case ST_ANLEIHE:
                return BND.name();
            case ST_CERTIFICATE:
                return CER.name();
            case ST_OS:
                return WNT.name();
            case ST_FUTURE:
                return FUT.name();
            case ST_OPTION:
                return OPT.name();
            case ST_INDEX:
                return IND.name();
            case ST_BEZUGSRECHT:
                return BZG.name();
            case ST_GENUSS:
                return GNS.name();
            case ST_DEVISE:
                return CUR.name();
            default:
                return NON.name();
        }
    }
}
