package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.pmxml.HasPrivRequest;
import de.marketmaker.iview.pmxml.HasPrivResponse;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.PrivWrapper;
import de.marketmaker.iview.pmxml.UMRightBody;

import java.util.List;

/**
 * Created on 01.06.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class PrivFeature {

    private final DmxmlContext.Block<HasPrivResponse> privBlock;
    private final HasPrivRequest privReq;

    public PrivFeature(DmxmlContext context, MMClassIndex classIdx, UMRightBody... umrbs) {
        this.privReq = new HasPrivRequest();
        this.privReq.setClassIdx(classIdx);
        for (UMRightBody umrb : umrbs) {
            this.privReq.getContent().add(createPrivWrapper(umrb));
        }
        this.privBlock = context.addBlock("PM_CheckShellAuth"); //$NON-NLS$
        this.privBlock.setParameter(this.privReq);
    }

    private PrivWrapper createPrivWrapper(UMRightBody umrb) {
        final PrivWrapper pw = new PrivWrapper();
        pw.setRight(umrb);
        return pw;
    }

    public void setId(String id) {
        this.privReq.setId(id);
        this.privBlock.setToBeRequested();
    }

    public String getObjectId() {
        return this.privReq.getId();
    }

    public boolean isResponseOk() {
        return this.privBlock.isResponseOk();
    }

    public boolean allowed(UMRightBody umrb) {
        if (!this.privBlock.isResponseOk()) {
            final String mdg = "priv response not ok!"; // $NON-NLS$
            DebugUtil.logToServer(mdg);
            Firebug.error(mdg);
            return false;
        }
        final HasPrivResponse result = this.privBlock.getResult();
        if (result == null || result.getContent() == null || result.getContent().isEmpty()) {
            return false;
        }
        final List<PrivWrapper> content = result.getContent();
        for (PrivWrapper privWrapper : content) {
            if (privWrapper.getRight() == umrb) {
                return privWrapper.isHasPriv();
            }
        }
        return false;
    }
}
