/*
 * GrainsMainController.java
 *
 * Created on 18.03.2010 14:02:04
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * @author Michael Lösch
 */
public class GrainsMenuBuilder extends MenuBuilder {

    @Override
    void init() {
        final MenuModel model = getModel();

        model.add(createModuleMenu("GS", I18n.I.markets(), "cg-market")  // $NON-NLS$
                .add(createItem("GS_G", I18n.I.grains()))  // $NON-NLS$
                .add(createItem("GS_F", I18n.I.animalFeedStuff()))  // $NON-NLS$
                .add(createItem("GS_CUR", I18n.I.currencies1()))  // $NON-NLS$
                .add(createItem("M_UB_US", "P_V/2995", I18n.I.overviewPages(), "mm-icon-vwdpage")  // $NON-NLS$
                        .add(createItem("M_US_MATIF", "P_V/4025", I18n.I.indexSomething("MATIF"), "mm-icon-vwdpage"))  // $NON-NLS$
                        .add(createItem("M_US_CBOT", "P_V/2810", I18n.I.indexSomething("CBOT"), "mm-icon-vwdpage"))  // $NON-NLS$
                        .add(createItem("M_US_LIF", "P_V/2800", I18n.I.indexSomething("LIFFE"), "mm-icon-vwdpage"))  // $NON-NLS$
                        .add(createItem("M_US_KCBT", "P_V/4502", I18n.I.indexSomething("KCBT"), "mm-icon-vwdpage"))  // $NON-NLS$
                        .add(createItem("M_US_MGE", "P_V/4500", I18n.I.indexSomething("MGE"), "mm-icon-vwdpage"))  // $NON-NLS$
                        .add(createItem("M_US_WCE", "P_V/4501", I18n.I.indexSomething("WCE"), "mm-icon-vwdpage"))  // $NON-NLS$
                        .add(createItem("M_US_OIL", "P_V/1917", I18n.I.indexSomething("European Veg Oils"), "mm-icon-vwdpage"))  // $NON-NLS$
                )
                .add(createItem("M_UB_D", "P_V/2095", I18n.I.currencies1(), "mm-icon-vwdpage")  // $NON-NLS$
                        .add(createItem("M_D_FRX", "P_V/202", "Forex", "mm-icon-vwdpage")) // $NON-NLS$
                        .add(createItem("M_D_EUR", "P_V/1111", I18n.I.somethingCrossrates("EUR"), "mm-icon-vwdpage"))  // $NON-NLS$
                        .add(createItem("M_D_CHF", "P_V/1113", I18n.I.somethingCrossrates("CHF"), "mm-icon-vwdpage"))  // $NON-NLS$
                        .add(createItem("M_D_EZB", "P_V/2094", I18n.I.somethingReferencePrices("EZB"), "mm-icon-vwdpage"))  // $NON-NLS$
                        .add(createItem("M_D_EURFR", "P_V/7028", I18n.I.somethingForwardRates("EUR"), "mm-icon-vwdpage"))  // $NON-NLS$
                        .add(createItem("M_D_USDFR", "P_V/7031", I18n.I.somethingForwardRates("USD"), "mm-icon-vwdpage"))  // $NON-NLS$
                )
                .add(createItem(!SessionData.isAsDesign(), "B_PW", I18n.I.changePassword()))  // $NON-NLS$
                .add(createItem("P_MER", I18n.I.portrait()).hide())  // $NON-NLS$
                .add(createItem("P_CUR", I18n.I.portrait()).hide())  // $NON-NLS$
                .add(createItem("P_FUT", I18n.I.portrait()).hide())  // $NON-NLS$
                .add(createItem("P_UND", I18n.I.portrait()).hide())  // $NON-NLS$
                .add(createItem("P_V", "vwd").hide()) // $NON-NLS$
        );

        model.add(createModuleMenu("N", I18n.I.news(), "cg-news")  // $NON-NLS$
                .add(OverviewNWSController.createMenuModel(this.model, this.mc, "N_UB") // $NON-NLS$
                .add(createItem("N_D", I18n.I.detail()).hide()))  // $NON-NLS$
                .add(createItem("M_S", I18n.I.searchResults())) // $NON-NLS-0$
        );

        if(SessionData.isAsDesign() && AbstractMainController.INSTANCE.getPasswordStrategy().isShowViewAvailable()) {
            this.model.add(createModuleMenu("B", I18n.I.settings(), "cg-settings")  // $NON-NLS$
                    .add(createItem("B_PW", I18n.I.changePassword()))  // $NON-NLS$
            );
        }

        if(SessionData.isAsDesign()) {
            this.model.add(createModuleMenu("H", I18n.I.help(), "cg-help")  // $NON-NLS$
                    .add(createItem("H_CS", I18n.I.customerService())) // $NON-NLS$
                    .add(createItem("H_TOU", I18n.I.termsOfUseMenuItem())) // $NON-NLS$
                    .add(Selector.PAGES_VWD, createItem("H_D", "P_V/10", I18n.I.disclaimer(), "mm-icon-vwdpage")) // $NON-NLS$
            );
        }
    }
}
