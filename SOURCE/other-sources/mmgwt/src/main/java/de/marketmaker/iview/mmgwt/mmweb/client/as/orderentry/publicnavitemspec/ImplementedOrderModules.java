/*
 * ImplementedOrderModules.java
 *
 * Created on 17.06.13 11:33
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Bank;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.BrokerageModuleID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * This class may be used in ordinary code, even if the order module has not been loaded.
 * It does not depend on any other classes of the orderentry package.
 *
 * @author Markus Dick
 */
public final class ImplementedOrderModules {
    private final static HashSet<BrokerageModuleID> BROKERAGE_MODULE_IDS;
    private final static HashSet<BrokerageModuleID> BROKERAGE_MODULE_IDS_WITH_ORDER_BOOK;
    private final static HashMap<String, Filter<Depot>> DEPOT_FILTERS;

    static {
        BROKERAGE_MODULE_IDS = new HashSet<BrokerageModuleID>(Arrays.asList(
                BrokerageModuleID.BM_HA,
                BrokerageModuleID.BM_BHLKGS,
                BrokerageModuleID.BM_FUCHSBRIEFE
        ));
        BROKERAGE_MODULE_IDS_WITH_ORDER_BOOK = new HashSet<BrokerageModuleID>(Arrays.asList(
                BrokerageModuleID.BM_BHLKGS
        ));
        DEPOT_FILTERS = new HashMap<String, Filter<Depot>>();
        DEPOT_FILTERS.put("BHL", new BHLDepotFilter()); //$NON-NLS$
    }

    public static boolean isImplemented(String brokerageModuleID) {
        return StringUtil.hasText(brokerageModuleID) &&
                isImplemented(toBrokerageModuleId(brokerageModuleID));
    }

    public static boolean hasOrderBook(String brokerageModuleID) {
        return StringUtil.hasText(brokerageModuleID) &&
                hasOrderBook(toBrokerageModuleId(brokerageModuleID));
    }

    public static boolean isImplemented(BrokerageModuleID brokerageModuleID) {
        return BROKERAGE_MODULE_IDS.contains(brokerageModuleID);
    }

    public static boolean hasOrderBook(BrokerageModuleID brokerageModuleID) {
        return BROKERAGE_MODULE_IDS_WITH_ORDER_BOOK.contains(brokerageModuleID);
    }

    public static Filter<Depot> getDepotFilter(String filterName) {
        return DEPOT_FILTERS.get(filterName);
    }

    public static BrokerageModuleID toBrokerageModuleId(String mmTalkBmModuleId) {
        if(StringUtil.hasText(mmTalkBmModuleId)) {
            try {
                final int ordinal = Integer.parseInt(mmTalkBmModuleId);

                for(BrokerageModuleID bmId : BrokerageModuleID.values()) {
                    if(ordinal == bmId.ordinal()) {
                        return bmId;
                    }
                }
            }
            catch(Exception e) {
                Firebug.warn("Exception while converting brokerage module id '" + mmTalkBmModuleId + "' to internal enum", e);
            }
        }

        return BrokerageModuleID.BM_NONE;
    }

    public static List<Depot> filterDepots(List<Depot> depots) {
        final String filterName = getDepotChoiceFilterName();
        final Filter<Depot> filter = ImplementedOrderModules.getDepotFilter(filterName);
        if(filterName != null) {
            Firebug.debug("<ImplementedOrderModules.filterDepots> filter=" + (filter != null ? filterName : "none"));
        }

        final List<Depot> filtered = new ArrayList<Depot>();
        for(Depot depot : depots) {
            final Bank bank = depot.getBank();
            if(!depot.isDeactivated()
                    && bank != null
                    && ImplementedOrderModules.isImplemented(bank.getBrokerageModuleId())
                    && (filter == null || filter.isAcceptable(depot))) {
                filtered.add(depot);
            }
        }

        return filtered;
    }

    private static String getDepotChoiceFilterName() {
        if(Customer.INSTANCE.isCustomerAS()) {
            return Customer.INSTANCE.asCustomerAS().getOrderEntryDepotChoiceFilterName();
        }
        return null;
    }
}
