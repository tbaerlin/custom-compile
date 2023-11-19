/*
 * BHLDepotFilter.java
 *
 * Created on 21.01.14 15:34
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.pmxml.BrokerageModuleID;

/**
 * This filter does not accept BHL depots where the depot number starts with '[', because
 * BHL uses '[' and ']' wrapped around the depot number to mark depots that have been
 * discontinued by their investors. The concept of deactivated depots is not used.
 *
 * After negotiations, ordering project management decided that checking if the depot number
 * contains a '[' is sufficient.
 *
 * @author Markus Dick
 */
public class BHLDepotFilter implements Filter<Depot> {
    private static final BHLDeactivatedObjectFilter OBJECT_NUMBER_FILTER = new BHLDeactivatedObjectFilter();

    @Override
    public boolean isAcceptable(Depot depot) {
        if(depot == null || depot.getBank() == null) {
            return true;
        }

        final BrokerageModuleID moduleID = ImplementedOrderModules.toBrokerageModuleId(depot.getBank().getBrokerageModuleId());
        return !BrokerageModuleID.BM_BHLKGS.equals(moduleID)
                || OBJECT_NUMBER_FILTER.isAcceptable(depot.getDepotNumber());
    }
}
