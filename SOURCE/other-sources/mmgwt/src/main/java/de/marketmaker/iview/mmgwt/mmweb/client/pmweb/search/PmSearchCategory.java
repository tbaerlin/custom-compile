package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search;

import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellObjectSearchResponse;

import java.util.List;

/**
 * Created on 22.02.13 09:57
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface PmSearchCategory {
    void issueRequest(PmSearchController controller);
    TableColumnModel getTableColumnModel();
    TableDataModel getTableDataModel(List<ShellMMInfo> infos);
}
