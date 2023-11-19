/*
 * PrivacyModeAllowedObjectId.java
 *
 * Created on 13.05.2015 07:43
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.AbstractMmTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asString;

/**
 * @author mdick
 */
@NonNLS
public class PrivacyModeAllowedObjectId {
    public static class Talker extends AbstractMmTalker<DatabaseIdQuery, List<PrivacyModeAllowedObjectId>, PrivacyModeAllowedObjectId> {
        public static Talker createForInvestor() {
            return new Talker("Inhaber.append[Inhaber.portfolio].append[Inhaber.depot].append[Inhaber.Konto].append[Inhaber.PersonLinks.Person]");
        }

        private Talker(String formula) {
            super(formula);
        }

        @Override
        protected DatabaseIdQuery createQuery() {
            return new DatabaseIdQuery();
        }

        @Override
        public List<PrivacyModeAllowedObjectId> createResultObject(MMTalkResponse response) {
            return this.wrapper.createResultObjectList(response);
        }

        @Override
        public MmTalkWrapper<PrivacyModeAllowedObjectId> createWrapper(Formula formula) {
            return PrivacyModeAllowedObjectId.createWrapper(formula);
        }
    }

    private static MmTalkWrapper<PrivacyModeAllowedObjectId> createWrapper(Formula formula) {
        final MmTalkWrapper<PrivacyModeAllowedObjectId> cols = MmTalkWrapper.create(formula, PrivacyModeAllowedObjectId.class);

        cols.appendColumnMapper(new MmTalkColumnMapper<PrivacyModeAllowedObjectId>("ID") {
            @Override
            public void setValue(PrivacyModeAllowedObjectId o, MM item) {
                o.id = asString(item);
            }
        });

        return cols;
    }

    private String id;

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrivacyModeAllowedObjectId)) return false;

        final PrivacyModeAllowedObjectId that = (PrivacyModeAllowedObjectId) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PrivacyModeAllowedObjectId{" +
                "id='" + id + '\'' +
                '}';
    }
}
