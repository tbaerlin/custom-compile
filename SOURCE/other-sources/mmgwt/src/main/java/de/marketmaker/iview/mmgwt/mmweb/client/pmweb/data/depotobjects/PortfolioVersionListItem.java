/*
 * PortfolioVersionListItem.java
 *
 * Created on 27.03.13 09:57
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.MM;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.*;

/**
 * @author Markus Dick
 */
public class PortfolioVersionListItem {
    private String versionValidFromDate;
    private String versionName;

    public static MmTalkWrapper<PortfolioVersionListItem> createWrapper(String formula) {
        final MmTalkWrapper<PortfolioVersionListItem> cols = MmTalkWrapper.create(formula, PortfolioVersionListItem.class);

        cols.appendColumnMapper(new MmTalkColumnMapper<PortfolioVersionListItem>("EffectiveFrom") { // $NON-NLS$
            @Override
            public void setValue(PortfolioVersionListItem pv, MM item) {
                pv.versionValidFromDate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<PortfolioVersionListItem>("VersionsName") { // $NON-NLS$
            @Override
            public void setValue(PortfolioVersionListItem pv, MM item) {
                pv.versionName = asString(item);
            }
        });

        return cols;
    }

    public String getVersionValidFromDate() {
        return versionValidFromDate;
    }

    public String getVersionName() {
        return versionName;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortfolioVersionListItem)) return false;

        final PortfolioVersionListItem that = (PortfolioVersionListItem) o;

        if (versionName != null ? !versionName.equals(that.versionName) : that.versionName != null) return false;
        if (versionValidFromDate != null ? !versionValidFromDate.equals(that.versionValidFromDate) : that.versionValidFromDate != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = versionValidFromDate != null ? versionValidFromDate.hashCode() : 0;
        result = 31 * result + (versionName != null ? versionName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PortfolioVersionListItem{" + // $NON-NLS$
                "versionValidFromDate='" + versionValidFromDate + '\'' +  // $NON-NLS$
                ", versionName='" + versionName + '\'' +  // $NON-NLS$
                '}';
    }
}
