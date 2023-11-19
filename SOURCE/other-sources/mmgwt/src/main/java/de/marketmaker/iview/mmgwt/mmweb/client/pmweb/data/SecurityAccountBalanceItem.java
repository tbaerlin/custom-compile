/*
 * SecurityAccountBalanceItem.java
 *
 * Created on 28.07.2014 13:15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.AbstractMmTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

/**
* @author mdick
*/
@NonNLS
public class SecurityAccountBalanceItem {
    public static class Talker extends AbstractMmTalker<DatabaseIdQuery, List<SecurityAccountBalanceItem>, SecurityAccountBalanceItem> {
        public Talker() {
            super(Formula.create("Depotbewertung[_;_;_;_;_;_;_;_;_;_;_;Nein].Transaktionen.Sort[#less; #[](WP.Name)]"));
        }

        @Override
        protected DatabaseIdQuery createQuery() {
            return new DatabaseIdQuery();
        }

        @Override
        protected MmTalkWrapper<SecurityAccountBalanceItem> createWrapper(Formula formula) {
            final MmTalkWrapper<SecurityAccountBalanceItem> cols = MmTalkWrapper.create(formula, SecurityAccountBalanceItem.class);
            cols.appendColumnMapper(new MmTalkColumnMapper<SecurityAccountBalanceItem>("WP") {
                @Override
                public void setValue(SecurityAccountBalanceItem o, MM item) {
                    if (item instanceof ShellMMInfo) {
                        o.instrument = ((ShellMMInfo) item);
                    }
                }
            }).appendColumnMapper(new MmTalkColumnMapper<SecurityAccountBalanceItem>("Bestand") {
                @Override
                public void setValue(SecurityAccountBalanceItem o, MM item) {
                    o.total = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<SecurityAccountBalanceItem>("TA_AnzGesperrt") {
                @Override
                public void setValue(SecurityAccountBalanceItem o, MM item) {
                    o.locked = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<SecurityAccountBalanceItem>("Bestand - TA_AnzGesperrt") {
                @Override
                public void setValue(SecurityAccountBalanceItem o, MM item) {
                    o.saleable = MmTalkHelper.asString(item);
                }
            });
            return cols;
        }

        @Override
        public List<SecurityAccountBalanceItem> createResultObject(MMTalkResponse response) {
            return this.wrapper.createResultObjectList(response);
        }
    }
    private ShellMMInfo instrument;

    private String total;

    private String locked;

    private String saleable;

    public ShellMMInfo getInstrument() {
        return this.instrument;
    }

    public void setInstrument(ShellMMInfo instrument) {
        this.instrument = instrument;
    }

    public String getTotal() {
        return total;
    }

    public String getLocked() {
        return locked;
    }

    public String getSaleable() {
        return saleable;
    }
}
