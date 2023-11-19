/*
 * Prospect.java
 *
 * Created on 05.06.2014 08:06
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.AbstractMmTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkNodeMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asString;

/**
 * @author Markus Dick
 */
public class Prospect extends AbstractOwner {
    public static class ProspectTalker extends AbstractMmTalker<DatabaseIdQuery, Prospect, Prospect> {

        public ProspectTalker() {
            super("Interessent"); // $NON-NLS$
        }

        @Override
        protected DatabaseIdQuery createQuery() {
            return new DatabaseIdQuery();
        }

        public MmTalkWrapper<Prospect> createWrapper(Formula formula) {
            return AbstractOwner.withLinkedPersons(Prospect.createWrapper(formula));
        }

        @Override
        public Prospect createResultObject(MMTalkResponse response) {
            final List<Prospect> resultObject = this.wrapper.createResultObjectList(response);
            if (!resultObject.isEmpty()) {
                return resultObject.get(0);
            }
            return null;
        }
    }

    public static MmTalkWrapper<Prospect> createWrapper(Formula formula) {
        final MmTalkWrapper<Prospect> cols = MmTalkWrapper.create(formula, Prospect.class);
        AbstractOwner.appendMappers(cols, "Interessent_Steuersatz");  // $NON-NLS$

        cols.appendColumnMapper(new MmTalkColumnMapper<Prospect>("Bezeichnung") { // $NON-NLS$
            @Override
            public void setValue(Prospect o, MM item) {
                o.description = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Prospect>("ProspectStatus") { // $NON-NLS$
            @Override
            public void setValue(Prospect o, MM item) {
                o.status = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Prospect>("LastContactAt") { // $NON-NLS$
            @Override
            public void setValue(Prospect o, MM item) {
                o.lastContactAt = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Prospect>("ProspectValuation") { // $NON-NLS$
            @Override
            public void setValue(Prospect o, MM item) {
                o.valuation = asString(item);
            }
        }).appendNodeMapper(new MmTalkNodeMapper<Prospect, Advisor>(Advisor.createWrapper("Advisor")) { // $NON-NLS$
            @Override
            public void setValue(Prospect o, MmTalkWrapper<Advisor> wrapper, MMTable table) {
                o.advisor = wrapper.createResultObject(table);
            }
        });

        return cols;
    }

    private String description;
    private String status;
    private String lastContactAt;
    private String valuation;
    private Advisor advisor;

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getLastContactAt() {
        return lastContactAt;
    }

    public String getValuation() {
        return valuation;
    }

    public Advisor getAdvisor() {
        return advisor;
    }

    @Override
    public ShellMMType getShellMMType() {
        return ShellMMType.ST_INTERESSENT;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prospect)) return false;
        if (!super.equals(o)) return false;

        final Prospect prospect = (Prospect) o;

        if (description != null ? !description.equals(prospect.description) : prospect.description != null)
            return false;
        if (lastContactAt != null ? !lastContactAt.equals(prospect.lastContactAt) : prospect.lastContactAt != null)
            return false;
        if (status != null ? !status.equals(prospect.status) : prospect.status != null) return false;
        if (valuation != null ? !valuation.equals(prospect.valuation) : prospect.valuation != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (lastContactAt != null ? lastContactAt.hashCode() : 0);
        result = 31 * result + (valuation != null ? valuation.hashCode() : 0);
        return result;
    }
}
