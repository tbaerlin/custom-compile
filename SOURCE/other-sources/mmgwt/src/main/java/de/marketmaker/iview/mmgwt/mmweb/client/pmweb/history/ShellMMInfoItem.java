package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItem;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 22.02.13 15:36
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class ShellMMInfoItem implements ContextItem {
    private final ShellMMInfo shellMMInfo;

    public ShellMMInfoItem(ShellMMInfo shellMMInfo) {
        this.shellMMInfo = shellMMInfo;
    }

    @Override
    public String getName() {
        return this.shellMMInfo.getBezeichnung();
    }

    public String getId() {
        return this.shellMMInfo.getId();
    }

    public String getIsin() {
        return this.shellMMInfo.getISIN();
    }

    public ShellMMType getShellMMType() {
        return shellMMInfo.getTyp();
    }

    public ShellMMInfo getShellMMInfo() {
        return shellMMInfo;
    }

    public static List<ShellMMInfoItem> asInfoItems(List<ShellMMInfo> infos) {
        final List<ShellMMInfoItem> result = new ArrayList<ShellMMInfoItem>();
        for (ShellMMInfo info : infos) {
            result.add(new ShellMMInfoItem(info));
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShellMMInfoItem)) return false;

        final ShellMMInfoItem that = (ShellMMInfoItem) o;

        if (shellMMInfo != null ? !shellMMInfo.getId().equals(that.shellMMInfo.getId()) : that.shellMMInfo != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return shellMMInfo.getId() != null ? shellMMInfo.getId().hashCode() : 0;
    }
}