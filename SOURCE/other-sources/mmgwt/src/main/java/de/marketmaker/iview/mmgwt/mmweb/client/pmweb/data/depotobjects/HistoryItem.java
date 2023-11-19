/*
 * HistoryItem.java
 *
 * Created on 26.04.13 11:19
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkNodeMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.*;

/**
* @author Markus Dick
*/
public class HistoryItem {
    static MmTalkWrapper<HistoryItem> createWrapper(String preFormula) {
        final MmTalkWrapper<HistoryItem> historyWrapper = MmTalkWrapper.create(preFormula, HistoryItem.class);

        historyWrapper.appendColumnMapper(new MmTalkColumnMapper<HistoryItem>("value") { //$NON-NLS$
            @Override
            public void setValue(HistoryItem o, MM item) {
                o.date = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<HistoryItem>("value['Wert']") { //$NON-NLS$
            @Override
            public void setValue(HistoryItem o, MM item) {
                o.dataItem = item;
            }
        });

        return historyWrapper;
    }

    String date;
    MM dataItem;

    public String getDate() {
        return date;
    }

    public MM getDataItem() {
        return dataItem;
    }
}
