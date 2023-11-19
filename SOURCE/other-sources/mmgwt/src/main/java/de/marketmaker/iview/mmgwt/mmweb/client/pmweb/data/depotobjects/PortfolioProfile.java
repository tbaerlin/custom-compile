/*
 * PortfolioProfile.java
 *
 * Created on 22.03.13 16:33
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkNodeMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;

import java.util.List;

/**
 * @author Markus Dick
 */
public class PortfolioProfile extends PortfolioProfileBase {
    private String name;
    private List<MMTalkStringListEntryNode> keyList;

    public static MmTalkWrapper<PortfolioProfile> createWrapper(String formula) {
        final MmTalkWrapper<PortfolioProfile> cols = MmTalkWrapper.create(formula, PortfolioProfile.class);
        PortfolioProfileBase.appendMappers(cols);

        cols.appendNodeMapper(new MmTalkNodeMapper<PortfolioProfile, MMTalkStringListEntryNode>(MMTalkStringListEntryNode.createWrapper("GetKeyList")) { // $NON-NLS$
            @Override
            public void setValue(PortfolioProfile pp, MmTalkWrapper<MMTalkStringListEntryNode> wrapper, MMTable table) {
                pp.keyList = wrapper.createResultObjectList(table);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<PortfolioProfile>("Name") { // $NON-NLS$
            @Override
            public void setValue(PortfolioProfile pp, MM item) {
                pp.name = MmTalkHelper.asString(item);
            }
        });

        return cols;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortfolioProfile)) return false;
        if (!super.equals(o)) return false;

        final PortfolioProfile that = (PortfolioProfile) o;

        if (keyList != null ? !keyList.equals(that.keyList) : that.keyList != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (keyList != null ? keyList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PortfolioProfile{" +       // $NON-NLS$
                "name='" + name + '\'' +   // $NON-NLS$
                ", keyList=" + keyList +  // $NON-NLS$
                "} " + super.toString();
    }
}
