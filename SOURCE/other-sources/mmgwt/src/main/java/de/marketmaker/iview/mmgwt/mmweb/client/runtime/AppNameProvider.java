/*
 * AppNameProvider.java
 *
 * Created on 01.07.2016 12:19
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.runtime;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppProfile;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
@Singleton
public class AppNameProvider implements Provider<String> {
    private final SessionData sessionData;
    private final AppProfile appProfile;
    private final Permutation permutation;

    @Inject
    public AppNameProvider(SessionData sessionData, Permutation permutation) {
        this.permutation = permutation;
        this.sessionData = sessionData;
        this.appProfile = sessionData.getUser().getAppProfile();
    }

    @NonNLS
    @Override
    public String get() {
        if(this.permutation == null) {
            return "Infront Market Manager Web";
        }

        switch (permutation) {
            case GIS:
                if(isProductAllowed(Selector.DZBANK_WEB_INVESTOR_PUSH) || isProductAllowed(Selector.DZBANK_WEB_INVESTOR)){
                    return "GIS WebInvestor";
                }
                else if(isProductAllowed(Selector.DZBANK_WEB_INFORMER)) {
                    return "GIS WebInformer";
                }
                else {
                    return "GIS Webanwendung"; //should never occur
                }

            case AS:
                return "Infront Advisory Solution";

            case MMF:
                final String customer = this.sessionData.getGuiDefValue("customer");
                if(StringUtil.hasText(customer)) {
                    switch (customer) {
                        case "GRAINS":
                            return "Infront Market Manager Soft Commodities Web";
                        case "METALS":
                            return "Infront Market Manager Metals Web";
                    }
                }
            default:
                return "Infront Market Manager Financials Web";
        }
    }

    private boolean isProductAllowed(Selector productSelector) {
        return this.appProfile.isProductAllowed(Integer.toString(productSelector.getId())); //done in this manner for decoupling and to ease testing;
    }
}
