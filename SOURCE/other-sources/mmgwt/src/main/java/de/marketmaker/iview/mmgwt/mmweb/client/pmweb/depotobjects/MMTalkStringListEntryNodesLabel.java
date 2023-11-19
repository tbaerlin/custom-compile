/*
 * MMTalkStringListEntryNodesLabel.java
 *
 * Created on 03.05.13 09:32
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.user.client.ui.PopupPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.MMTalkStringListEntryNode;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

import java.util.List;

/**
 * @author Markus Dick
 */
public class MMTalkStringListEntryNodesLabel extends AbstractLabelWithPopup<List<MMTalkStringListEntryNode>>{
    final private SnippetTableWidget table;

    public MMTalkStringListEntryNodesLabel(String label, String lableTableHeader, List<MMTalkStringListEntryNode> popupData, TableCellRenderer tableCellRenderer) {
        super(label, popupData);

        this.updateWidget();

        final PopupPanel p = getPopupPanel();
        this.table = new SnippetTableWidget(new DefaultTableColumnModel(
                new TableColumn[] {
                        new TableColumn(lableTableHeader, 1f, tableCellRenderer)
                })
        );
        p.add(this.table);
    }

    @Override
    protected void updatePopupPanel() {
        final List<MMTalkStringListEntryNode> popupData = getPopupData();

        if(popupData == null || popupData.isEmpty()) {
            return;
        }

        final TableDataModel tdm = DefaultTableDataModel.create(popupData,
                new AbstractRowMapper<MMTalkStringListEntryNode>() {
                    @Override
                    public Object[] mapRow(MMTalkStringListEntryNode entryNode) {
                        return new Object[]{
                                htmlEscape(entryNode.getValue())
                        };
                    }
                });

        this.table.updateData(tdm);
    }
}
