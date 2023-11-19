/*
 * MetalsMenuBuilder.java
 *
 * Created on 18.03.2010 14:02:04
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * @author Michael Lösch
 */
public class MetalsMenuBuilder extends MenuBuilder {

    @Override
    void init() {
        if (SessionData.isAsDesign()) {
            this.model.add(createMenu(DASHBOARD_ID, "noname") // $NON-NLS$
                    .add(createItem(DashboardPageController.HISTORY_TOKEN_DASHBOARDS, "Dashboards").hide()) // $NON-NLS$
            );
        }
        this.model.add(createModuleMenu("MS", I18n.I.markets(), "cg-market")  // $NON-NLS$
                .add(createItem("MS_I", "MS_I", "Screen I", null) // $NON-NLS$
                        .add(createItem("MS_I_USD", "MS_I/cur=USD", "in USD", null)) // $NON-NLS$
                        .add(createItem("MS_I_EUR", "MS_I/cur=EUR", "in EUR", null)) // $NON-NLS$
                        .add(createItem("MS_I_GBP", "MS_I/cur=GBP", "in GBP", null)) // $NON-NLS$
                )
                .add(createItem("MS_II", "MS_II/multilistid=metals", "Screen II", null) // $NON-NLS$
                        .add(createItem("MS_II_USD", "MS_II/multilistid=metals/cur=USD", "in USD", null)) // $NON-NLS$
                        .add(createItem("MS_II_EUR", "MS_II/multilistid=metals/cur=EUR", "in EUR", null)) // $NON-NLS$
                        .add(createItem("MS_II_GBP", "MS_II/multilistid=metals/cur=GBP", "in GBP", null)) // $NON-NLS$
                )
                .add(createItem("MS_CUR", I18n.I.currencies1())  // $NON-NLS$
                        .add(createItem("M_ID_FRX", "P_V/202", "Forex", "mm-icon-vwdpage")) // $NON-NLS$
                        .add(createItem("M_ID_EUR", "P_V/1111", I18n.I.somethingCrossrates("EUR"), "mm-icon-vwdpage")) // $NON-NLS$
                        .add(createItem("M_ID_CHF", "P_V/1113", I18n.I.somethingCrossrates("CHF"), "mm-icon-vwdpage"))  // $NON-NLS$
                        .add(createItem("M_ID_EZB", "P_V/2094", I18n.I.somethingReferencePrices("EZB"), "mm-icon-vwdpage"))  // $NON-NLS$
                        .add(createItem("M_ID_EURFR", "P_V/7028", I18n.I.somethingForwardRates("EUR"), "mm-icon-vwdpage"))  // $NON-NLS$
                        .add(createItem("M_ID_USDFR", "P_V/7031", I18n.I.somethingForwardRates("USD"), "mm-icon-vwdpage")) // $NON-NLS$
                        .add(createItem("M_D_D", I18n.I.currencyCalculator()))  // $NON-NLS$
                )
                .add(createItem("M_IM_LME", "P_V/77100", "Index LME", "mm-icon-vwdpage")) // $NON-NLS$
                .add(Selector.COMEX, createItem("M_IM_CMX", "P_V/4020", "Index COMEX", "mm-icon-vwdpage")) // $NON-NLS$
                .add(createItem("M_IM_MET", "P_V/103", "Index Metalle", "mm-icon-vwdpage")) // $NON-NLS$
                .add(createItem("P_MER", I18n.I.portrait()).hide())  // $NON-NLS$
                .add(createItem("P_CUR", I18n.I.portrait()).hide())  // $NON-NLS$
                .add(createItem("P_V", "vwd").hide()) // $NON-NLS$
                .add(createItem("M_S", "vwd").hide()) // $NON-NLS$

                .add(Selector.EXCEL_WEB_QUERY_EXPORT, createItem("M_EXP", "Export")) // $NON-NLS$
        );

        this.model.add(createModuleMenu("N", I18n.I.news(), "cg-news")  // $NON-NLS$
                .add(OverviewNWSController.createMenuModel(this.model, this.mc, "N_UB") // $NON-NLS$
                        .add(createItem("N_D", I18n.I.detail()).hide()))  // $NON-NLS$
                .add(createItem("N_S", I18n.I.newsSearch()))  // $NON-NLS$
        );

        if(!SessionData.isAsDesign()) {
            this.model.add(createMenu("B", I18n.I.user())  // $NON-NLS$
                    .add(createItem("B_A", I18n.I.workspace()))  // $NON-NLS$
                    .add(createItem("B_PW", I18n.I.changePassword()))  // $NON-NLS$
            );
        }

        if(SessionData.isAsDesign()) {
            this.model.add(createModuleMenu("H", I18n.I.help(), "cg-help") // $NON-NLS$
                    .add(createItem("H_CS", I18n.I.customerService())) // $NON-NLS$
                    .add(createItem("H_TOU", I18n.I.termsOfUseMenuItem())) // $NON-NLS$
                    .add(Selector.PAGES_VWD, createItem("H_D", "P_V/10", I18n.I.disclaimer(), "mm-icon-vwdpage")) // $NON-NLS$
            );
        }
    }
}
