package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.history.EmptyContext;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.IdAndName;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.pmxml.ShellMMInfo;

import java.util.List;

/**
 * Created on 22.02.13 12:12
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class DepotObjectCategory implements PmSearchCategory {

    private final TableCellRenderers.IdAndNameLinkRenderer renderer;

    public DepotObjectCategory(String objectControllerId) {
        this.renderer = new TableCellRenderers.IdAndNameLinkRenderer(objectControllerId + "/objectid=", 80, "n/a"); // $NON-NLS$
    }

    @Override
    public void issueRequest(PmSearchController controller) {
        controller.refresh();
    }

    @Override
    public TableDataModel getTableDataModel(final List<ShellMMInfo> infos) {
        if (infos == null || infos.isEmpty()) {
            return DefaultTableDataModel.NULL;
        }

        return DefaultTableDataModel.create(
                infos,
                new AbstractRowMapper<ShellMMInfo>() {
                    public Object[] mapRow(ShellMMInfo i) {
                        final IdAndName idAndName = new IdAndName(i).withHistoryContext(
                                EmptyContext.create(I18n.I.searchResults())
                        );
                        return new Object[]{
                                idAndName,
                                PmSearchController.getZoneDesc(i.getPrimaryZoneId()) != null
                                        ? PmSearchController.getZoneDesc(i.getPrimaryZoneId()).getName()
                                        : "n/a", // $NON-NLS$
                                i.getNumber()
                        };
                    }
                });
    }

    @Override
    public TableColumnModel getTableColumnModel() {
        final TableColumn[] columns = new TableColumn[]{
                new TableColumn(I18n.I.label(), 0.4f, this.renderer),
                new TableColumn(I18n.I.zone(), 0.25f, TableCellRenderers.HTML),
                new TableColumn(I18n.I.number(), 0.35f, TableCellRenderers.STRING)
        };
        return new DefaultTableColumnModel(columns);
    }
}