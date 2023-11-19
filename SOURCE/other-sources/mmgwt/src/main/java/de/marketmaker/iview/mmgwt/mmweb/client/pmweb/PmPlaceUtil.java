/*
 * PmPlaceUtil.java
 *
 * Created on 04.06.13 12:47
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.NullContext;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractDepotObjectPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;

/**
 * Some pm web specific PlaceUtil like impls., e.g. for ShellMMInfo types.
 *
 * @author Markus Dick
 */
public final class PmPlaceUtil {
    private PmPlaceUtil() {
        /* do nothing */
    }

    /**
     * Handles gotos to mmf[web] market data portraits transparently,
     * if the MMSecurityId of shellMMInfo starts with an '&amp;'
     */
    public static void goTo(ShellMMInfo shellMMInfo) {
        goTo(shellMMInfo, NullContext.getInstance());
    }

    public static void goTo(ShellMMInfo shellMMInfo, HistoryContext context) {
        if(ShellMMTypeUtil.isSecurity(shellMMInfo.getTyp())) {
            final String mmSecurityID = shellMMInfo.getMMSecurityID();

            if (StringUtil.hasText(mmSecurityID)) {
                if(mmSecurityID.startsWith("&") || !SessionData.isWithMarketData()) {
                    Firebug.debug("MM Security ID only suitable for pm[xml]:" + mmSecurityID);
                    goToPmWebPortrait(shellMMInfo, context);
                    return;
                }
                Firebug.debug("MM Security ID suitable for dm[xml]: " + mmSecurityID);
                goToMmWebPortrait(shellMMInfo, context);
                return;
            }

            MainController.INSTANCE.showError("Security without an ID. Cannot goto portrait."); //$NON-NLS$
            return;
        }

        goToPmObject(shellMMInfo, context);
    }

    public static void goToActivityOverview(ShellMMInfo shellMMInfo, HistoryContext context) {
        final String controllerId = getPmObjectControllerId(shellMMInfo);
        if(controllerId == null) return;

        final String place = newObjectPlaceString(controllerId, shellMMInfo.getId())
                + StringUtil.TOKEN_DIVIDER + "sc=" + AbstractDepotObjectPortraitController.HISTORY_TOKEN_ACTIVITY_OVERVIEW; // $NON-NLS$k

        PlaceUtil.goTo(place, context);
    }

    private static void goToPmObject(ShellMMInfo shellMMInfo, HistoryContext context) {
        final String controllerId = getPmObjectControllerId(shellMMInfo);
        if(controllerId == null) return;

        PlaceUtil.goTo(newObjectPlaceString(controllerId, shellMMInfo.getId()), context);
    }

    private static void goToMmWebPortrait(final ShellMMInfo shellMMInfo, final HistoryContext context) {
        final String symbol = shellMMInfo.getMMSecurityID() + ".iid"; //$NON-NLS$

        Firebug.debug("<PmPlaceUtil.goToMmWebPortrait> symbol=" + symbol);

        final DmxmlContext dmxmlContext = new DmxmlContext();
        dmxmlContext.setCancellable(false);

        final DmxmlContext.Block<MSCQuoteMetadata> block = dmxmlContext.addBlock("MSC_QuoteMetadata"); //$NON-NLS$
        block.setParameter("symbol", symbol); //$NON-NLS$
        block.setToBeRequested();

        dmxmlContext.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if(!block.isResponseOk()) {
                    Firebug.warn(block.getError().getDescription());
                    //if the symbol is not found in mmf[web] we try to show the pm[web] portrait
                    goToPmWebPortrait(shellMMInfo, context);
                    return;
                }
                PlaceUtil.goToPortraitUndefView(block.getResult().getInstrumentdata(), null, context);
            }
        });
    }

    private static void goToPmWebPortrait(ShellMMInfo shellMMInfo, final HistoryContext context) {
        assert shellMMInfo.getMMSecurityID() != null;
        assert shellMMInfo.getTyp() != null;

        Firebug.debug("<PmPlaceUtil.goToPmWebPortrait> td=" + shellMMInfo.getTyp().name() + " sid=" + shellMMInfo.getMMSecurityID());

        PlaceUtil.goTo(newSecurityObjectPlaceString(shellMMInfo), context);
    }

    private static String newObjectPlaceString(String shellMMControllerId, String objectId) {
        return shellMMControllerId + StringUtil.TOKEN_DIVIDER + "objectid=" + objectId; //$NON-NLS$
    }

    private static String newSecurityObjectPlaceString(ShellMMInfo shellMMInfo) {
        return StringUtil.joinTokens(PmWebModule.HISTORY_TOKEN_CUSTOM_SECURITY,
                shellMMInfo.getMMSecurityID() + PmWebModule.PM_SECURITY_ID_SUFFIX,
                /*PmWebModule.HISTORY_TOKEN_CUSTOM_SECURITY_OVERVIEW*/ PlaceUtil.UNDEFINDED_PORTRAIT_VIEW,
                StringUtil.joinToken(PmWebModule.TOKEN_NAME_CUSTOM_SECURITY_TYPE, shellMMInfo.getTyp().name()));
    }

    private static String getPmObjectControllerId(ShellMMInfo shellMMInfo) {
        try {
            return ShellMMTypeUtil.getControllerId(shellMMInfo.getTyp());
        }
        catch(Exception e) {
            return null;
        }
    }

    public static boolean canGoTo(ShellMMInfo shellMMInfo) {
        if(shellMMInfo == null) {
            return false;
        }

        final ShellMMType type = shellMMInfo.getTyp();
        if(ShellMMTypeUtil.isDepotObject(type)) {
            //Viewing prospects is only allowed, if activities are allowed, see AS-861
            return !(ShellMMType.ST_INTERESSENT == type && !Selector.AS_ACTIVITIES.isAllowed())
                    && StringUtil.hasText(shellMMInfo.getId());
        }
        return ShellMMTypeUtil.isSecurity(type);
    }
}
