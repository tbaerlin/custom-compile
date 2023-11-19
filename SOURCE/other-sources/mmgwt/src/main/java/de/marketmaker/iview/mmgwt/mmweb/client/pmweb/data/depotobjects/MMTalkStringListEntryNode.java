/*
 * MMTalkStringListEntryNode.java
 *
 * Created on 25.03.13 17:01
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
public class MMTalkStringListEntryNode {
    private String value;

    public static MmTalkWrapper<MMTalkStringListEntryNode> createWrapper(String formula) {
        final MmTalkWrapper<MMTalkStringListEntryNode> cols = MmTalkWrapper.create(formula, MMTalkStringListEntryNode.class);

        cols.appendColumnMapper(new MmTalkColumnMapper<MMTalkStringListEntryNode>("object") { // $NON-NLS$
            @Override
            public void setValue(MMTalkStringListEntryNode e, MM item) {
                e.value = asString(item);
            }
        });

        return cols;
    }

    public String getValue() {
        return value;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MMTalkStringListEntryNode)) return false;

        MMTalkStringListEntryNode that = (MMTalkStringListEntryNode) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return value;
    }
}
