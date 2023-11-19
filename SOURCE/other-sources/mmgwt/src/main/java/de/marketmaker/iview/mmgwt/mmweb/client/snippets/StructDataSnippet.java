/*
* StructDataSnippet.java
*
* Created on 18.07.2008 10:59:25
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.AllocationItemType;
import de.marketmaker.iview.dmxml.FNDAllocations;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.Iterator;
import java.util.List;

/**
 * @author Michael Lösch
 */
public class StructDataSnippet extends AbstractSnippet<StructDataSnippet, StructDataSnippetView> implements SymbolSnippet,
        IsVisible {
    public static class Class extends SnippetClass {
        public Class() {
            super("StructData"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new StructDataSnippet(context, config);
        }
    }

    private DmxmlContext.Block<FNDAllocations> block;
    private boolean isVisible = false;

    public StructDataSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
        this.block = createBlock("FND_Allocations"); // $NON-NLS-0$
        this.block.setParameter("withConsolidatedAllocations", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.setView(new StructDataSnippetView(this));
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        DefaultTableDataModel dtmAllocs = DefaultTableDataModel.NULL;
        if (!block.isResponseOk()) {
            getView().update(dtmAllocs);
            return;
        }
        this.isVisible = false;
        final FNDAllocations allocs = this.block.getResult();
        int i;
        for (i = 0; i < allocs.getAllocation().size(); i++) {
            if (allocs.getAllocation().get(i).getType().equals(this.getConfiguration().getString("allocationtype"))) {                 // $NON-NLS-0$
                this.isVisible = true;
                dtmAllocs = getTableDataModel(allocs.getAllocation().get(i).getItem());
                break;
            }
        }
        getView().update(dtmAllocs);

        final String peerId = this.getConfiguration().getString("peerId", null); // $NON-NLS-0$
        if (peerId != null) {
            final StructPieSnippet peer = (StructPieSnippet) this.contextController.getSnippet(peerId);
            peer.setVisible(this.isVisible);
        }
    }

    private DefaultTableDataModel getTableDataModel(List<AllocationItemType> elements) {
        final DefaultTableDataModel dtm = new DefaultTableDataModel(elements.size(), 2);
        int row = 0;
        Iterator<AllocationItemType> it = elements.iterator();
        AllocationItemType current;
        while (it.hasNext()) {
            current = it.next();
            dtm.setValuesAt(row, new Object[]{
                    current.getTitle(),
                    current.getShare()
            });
            row++;
        }
        return dtm;
    }

    @Override
    public boolean isVisible() {
        return this.isVisible;        
    }

}
