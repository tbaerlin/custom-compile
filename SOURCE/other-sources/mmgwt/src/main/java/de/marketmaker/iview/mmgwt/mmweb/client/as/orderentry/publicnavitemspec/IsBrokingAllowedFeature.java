/*
 * IsBrokingAllowedFeature.java
 *
 * Created on 02.07.2015 08:54
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec;

import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.IsOrderingAllowedRequest;
import de.marketmaker.iview.pmxml.IsOrderingAllowedResponse;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
@NonNLS
public class IsBrokingAllowedFeature {
    private final DmxmlContext.Block<IsOrderingAllowedResponse> block;
    private final IsOrderingAllowedRequest request;

    public IsBrokingAllowedFeature(DmxmlContext context) {
        this.request = new IsOrderingAllowedRequest();
        this.block = context.addBlock("OE_IsAllowed");
        this.block.setParameter(this.request);
        this.block.setEnabled(Selector.AS_ORDERING.isAllowed());
    }

    public void setId(String id) {
        this.request.setShellMMId(id);
        this.block.setToBeRequested();
    }

    public String getId() {
        return this.request.getShellMMId();
    }

    public String getDepotId() {
        if(!Selector.AS_ORDERING.isAllowed() || !this.block.isResponseOk()) {
            return null;
        }
        final String depotId = this.block.getResult().getDepotId();
        if(!StringUtil.hasText(depotId) || "0".equals(depotId)) {
            return null;
        }
        return depotId;
    }

    public boolean isResponseOk() {
        return this.block.isResponseOk();
    }

    public boolean isPending() {
        return this.block.isPending();
    }

    public boolean isActivityBrokingAllowed() {
        return Selector.AS_ORDERING.isAllowed() && this.block.isResponseOk() && this.block.getResult().isUmbrOnlineBrokingActivity();
    }

    public boolean isStandaloneBrokingAllowed() {
        return Selector.AS_ORDERING.isAllowed() && this.block.isResponseOk() && this.block.getResult().isUmbrOnlineBroking();
    }

    public String getError() {
        if(isResponseOk()) {
            return null;
        }

        final ErrorType error = this.block.getError();
        return error != null ? error.getDescription() : "";
    }
}
