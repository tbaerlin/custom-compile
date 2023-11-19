/*
 * Advisor.java
 *
 * Created on 22.03.13 15:21
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

/**
 * @author Markus Dick
 */
public class Advisor {
    private String advisorNumber;
    private String areaNumber;
    private String areaName;
    private Address address;

    public String getAdvisorNumber() {
        return advisorNumber;
    }

    public String getAreaNumber() {
        return areaNumber;
    }

    public String getAreaName() {
        return areaName;
    }

    public Address getAddress() {
        return address;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Advisor)) return false;

        Advisor advisor = (Advisor) o;

        if (address != null ? !address.equals(advisor.address) : advisor.address != null) return false;
        if (advisorNumber != null ? !advisorNumber.equals(advisor.advisorNumber) : advisor.advisorNumber != null)
            return false;
        if (areaName != null ? !areaName.equals(advisor.areaName) : advisor.areaName != null) return false;
        if (areaNumber != null ? !areaNumber.equals(advisor.areaNumber) : advisor.areaNumber != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = advisorNumber != null ? advisorNumber.hashCode() : 0;
        result = 31 * result + (areaNumber != null ? areaNumber.hashCode() : 0);
        result = 31 * result + (areaName != null ? areaName.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }

    public static MmTalkWrapper<Advisor> createWrapper(String formula) {
        final MmTalkWrapper<Advisor> cols = MmTalkWrapper.create(formula, Advisor.class);

        cols.appendColumnMapper(new MmTalkColumnMapper<Advisor>("AdvisorNumber") { // $NON-NLS$
            @Override
            public void setValue(Advisor b, MM item) {
                b.advisorNumber = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Advisor>("AreaName") { // $NON-NLS$
            @Override
            public void setValue(Advisor b, MM item) {
                b.areaName = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Advisor>("AreaNumber") { // $NON-NLS$
            @Override
            public void setValue(Advisor b, MM item) {
                b.areaNumber = MmTalkHelper.asString(item);
            }
        }).appendNodeMapper(new MmTalkNodeMapper<Advisor, Address>(Address.createWrapper("Adresse")) { // $NON-NLS$
            @Override
            public void setValue(Advisor b, MmTalkWrapper<Address> wrapper, MMTable table) {
                b.address = wrapper.createResultObject(table);
            }
        });

        return cols;
    }
}
