/*
 * PmSecurityUtil.java
 *
 * Created on 17.06.13 16:21
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.InstrumentMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.ShellMMInfo;

/**
 * @author Markus Dick
 */
public class PmSecurityUtil {
    public static void resolveBySecurityId(String securityId, final AsyncCallback<ShellMMInfo> callback) {
        final DmxmlContext context = new DmxmlContext();
        context.setCancellable(false);

        final InstrumentMetadata.Talker talker = new InstrumentMetadata.Talker();
        talker.setSecurityId(securityId);

        final DmxmlContext.Block<MMTalkResponse> block = context.addBlock("PM_MMTalk"); //$NON-NLS$
        block.setParameter(talker.createRequest());
        block.setEnabled(true);

        context.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(ResponseType result) {
                final InstrumentMetadata instrumentMetadata = talker.createResultObject(block.getResult());
                if(instrumentMetadata == null) {
                    callback.onFailure(null);
                    return;
                }

                final ShellMMInfo info = new ShellMMInfo();
                info.setId(instrumentMetadata.getId());
                info.setTyp(instrumentMetadata.getType());
                info.setMMSecurityID(instrumentMetadata.getSecurityId());
                info.setISIN(instrumentMetadata.getIsin());
                info.setBezeichnung(instrumentMetadata.getName());

                callback.onSuccess(info);
            }
        });
    }

    public static String stripOfSecurityIdSuffix(String securityIdWithSuffix) {
        if(securityIdWithSuffix == null) {
            return null;
        }

        final int suffixPos = securityIdWithSuffix.indexOf(PmWebModule.PM_SECURITY_ID_SUFFIX);
        if(suffixPos > -1) {
            return securityIdWithSuffix.substring(0, suffixPos);
        }
        return securityIdWithSuffix;
    }

    public static String appendSecurityIdSuffix(String securityIdWithoutSuffix) {
        if(securityIdWithoutSuffix == null) {
            return null;
        }

        final int suffixPos = securityIdWithoutSuffix.indexOf(PmWebModule.PM_SECURITY_ID_SUFFIX);
        if(suffixPos > -1) {
            return securityIdWithoutSuffix;
        }
        return securityIdWithoutSuffix + PmWebModule.PM_SECURITY_ID_SUFFIX;
    }
}
