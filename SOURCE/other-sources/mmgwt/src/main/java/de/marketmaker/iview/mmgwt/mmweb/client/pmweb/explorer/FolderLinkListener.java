package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.explorer;

import com.google.gwt.dom.client.Element;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * Created on 24.04.13 08:40
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class FolderLinkListener implements LinkListener<FolderItem> {
    @Override
    public void onClick(LinkContext<FolderItem> context, Element e) {
        final FolderItem data = context.getData();
        goTo(data, data.getHistoryContext());
    }

    public static void goTo(FolderItem data, HistoryContext context) {
        switch (data.getType()) {
            case ST_INHABER:
                goToDepotObject(PmWebModule.HISTORY_TOKEN_INVESTOR_PROFILE, data, context);
                break;
            case ST_PORTFOLIO:
                goToDepotObject(PmWebModule.HISTORY_TOKEN_PORTFOLIO, data, context);
                break;
            case ST_INTERESSENT:
                goToDepotObject(PmWebModule.HISTORY_TOKEN_PROSPECT, data, context);
                break;
            default:
                Firebug.log("no goto defined for type " + data.getType().name());
        }
    }

    private static void goToDepotObject(String token, FolderItem item, HistoryContext context) {
        PlaceUtil.goTo(token + "/objectid=" + item.getId(), context); // $NON-NLS$
    }
}