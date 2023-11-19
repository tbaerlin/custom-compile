/*
 * PriceListSnippetView.java
 *
 * Created on 3/13/15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.dmxml.MSCPartition;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Willenbrock
 */
@NonNLS
public class PriceListSnippetView extends SnippetView<PriceListSnippet> {

    private final static String OFFSET_KEY = "offset";

    private final SnippetTableView<PriceListSnippet> table;

    private final FloatingToolbar toolbar = new FloatingToolbar();

    private final boolean withPartitions;

    protected PriceListSnippetView(PriceListSnippet snippet, TableColumnModel columnModel,
                                   String tableClass) {
        super(snippet);
        setTitle(snippet.getConfiguration().getString("title"));
        this.table = new SnippetTableView<>(snippet, columnModel, tableClass == null ? "mm-snippetTable" : tableClass);
        this.withPartitions = this.snippet.getConfiguration().getBoolean("withPartitions", false);
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();

        if (this.withPartitions) {
            this.container.setTopWidget(this.toolbar);
        }

        this.table.setContainer(this.container);

    }

    public void update(TableDataModel tdm, List<MSCPartition> partitions) {
        this.table.update(tdm);
        if (partitions != null) {
            this.toolbar.removeAll();
            final int lastOffset = this.snippet.getConfiguration().getInt(OFFSET_KEY, 0);
            for (int i = 0; i < partitions.size(); i++) {
                final String name = getPartitionName(partitions, i);
                final int offset = i;
                if (i > 0) {
                    this.toolbar.addSeparator();
                }
                this.toolbar.add(Button.text(name)
                        .active(i == lastOffset)
                        .clickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                snippet.ackNewOffset(offset);
                                Firebug.log(getClass().getSimpleName() + " <update> text = " + name + ", offset = " + offset);
                            }
                        })
                        .build());
            }
        }
    }

    private String getPartitionName(final List<MSCPartition> partitions, int i) {
        if (i < partitions.size()) {
            MSCPartition mscPartition = partitions.get(i);
            return mscPartition.getName();
        }
        else {
            return "";
        }
    }

    public ArrayList<PushRenderItem> getRenderItems(DefaultTableDataModel model) {
        return this.table.getRenderItems(model);
    }

}
