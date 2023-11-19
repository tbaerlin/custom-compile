/*
 * DepotItemMapper.java
 *
 * Created on 21.11.13 11:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.mappers;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.MappedListBox;

/**
 * @author Markus Dick
 */
public class DepotItemMapper implements MappedListBox.ItemMapper<Depot> {
    @Override
    public String getLabel(Depot item) {
        if(item == null) {
            return "null"; //$NON-NLS$
        }
        return item.getName();
    }

    @Override
    public String getValue(Depot item) {
        if(item == null) {
            return "null"; //$NON-NLS$
        }
        return item.getId();
    }
}
