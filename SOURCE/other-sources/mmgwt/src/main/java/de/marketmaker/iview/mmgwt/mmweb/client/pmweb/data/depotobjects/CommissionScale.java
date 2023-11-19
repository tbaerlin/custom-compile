/*
 * CommissionScale
 *
 * Created on 19.03.13 09:44
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.MM;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asString;

/**
 * @author Markus Dick
 */
public class CommissionScale {
    public static MmTalkWrapper<CommissionScale> createWrapper(String formula) {
        final MmTalkWrapper<CommissionScale> cols = MmTalkWrapper.create(formula, CommissionScale.class);

        cols.appendColumnMapper(new MmTalkColumnMapper<CommissionScale>("Name") { // $NON-NLS$
            @Override
            public void setValue(CommissionScale cs, MM item) {
                cs.name = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<CommissionScale>("Währung.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(CommissionScale cs, MM item) {
                cs.currency = asString(item);
            }
        });
        return cols;
    }

    private String name;
    private String currency;

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommissionScale)) return false;

        final CommissionScale that = (CommissionScale) o;

        if (currency != null ? !currency.equals(that.currency) : that.currency != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        return result;
    }
}
