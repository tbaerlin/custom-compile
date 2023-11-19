/*
 * RadioColumnRenderer.java
 *
 * Created on 21.07.2014 09:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.research;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DTSingleRow;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
@NonNLS
public class RadioColumnRenderer implements DTTableRenderer.CellRenderer {
    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public void appendStyle(StringBuilder sb1) {
        //nothing to do here
    }

    @Override
    public void appendSortStyle(StringBuilder sb1) {
        //nothing to do here
    }

    @Override
    public void appendHeader(StringBuilder sb1) {
        //nothing to do here
    }

    @Override
    public void appendRow(DTSingleRow row, boolean aggregated, StringBuilder sb) {
        final ShellMMInfo shellMMInfo = row.getObjectInfo();
        if(shellMMInfo != null && StringUtil.hasText(shellMMInfo.getId())) {
            sb.append("<input type=\"radio\" name=\"shellMMInfoChoice\" value=\"")
                    .append(shellMMInfo.getId())
                    .append("\">");
        }
        else {
            sb.append("<input type=\"radio\" name=\"shellMMInfoChoice\" value=\"\" readonly=\"readonly\">");
        }
    }
}
