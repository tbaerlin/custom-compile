/*
 * EntitlementQuoteProviderImpl.java
 *
 * Created on 25.02.2008 10:01:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.ContentFlags;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.EntitlementProvider;
import de.marketmaker.istar.domainimpl.instrument.ContentFlagsDp2;
import de.marketmaker.istar.domainimpl.instrument.EntitlementQuote;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EntitlementQuoteProviderImpl implements EntitlementQuoteProvider {

    private static final InstrumentTypeEnum[] TYPES_BY_VENDORKEY_TYPE = new InstrumentTypeEnum[19];

    static {
        TYPES_BY_VENDORKEY_TYPE[1] = InstrumentTypeEnum.STK;
        TYPES_BY_VENDORKEY_TYPE[2] = InstrumentTypeEnum.OPT;
        TYPES_BY_VENDORKEY_TYPE[3] = InstrumentTypeEnum.FUT;
        TYPES_BY_VENDORKEY_TYPE[5] = InstrumentTypeEnum.GNS;
        TYPES_BY_VENDORKEY_TYPE[6] = InstrumentTypeEnum.IND;
        TYPES_BY_VENDORKEY_TYPE[8] = InstrumentTypeEnum.CER;
        TYPES_BY_VENDORKEY_TYPE[9] = InstrumentTypeEnum.FND;
        TYPES_BY_VENDORKEY_TYPE[10] = InstrumentTypeEnum.CUR;
        TYPES_BY_VENDORKEY_TYPE[17] = InstrumentTypeEnum.CER;
        TYPES_BY_VENDORKEY_TYPE[18] = InstrumentTypeEnum.FND;
    }

    private static InstrumentTypeEnum toInstrumentType(int i) {
        return (i >= 0 && i < TYPES_BY_VENDORKEY_TYPE.length) ? TYPES_BY_VENDORKEY_TYPE[i] : null;
    }

    public static class MyEntitlementQuote extends EntitlementQuote {
        private final String vendorkey;

        public MyEntitlementQuote(Entitlement entitlement, String vendorkey) {
            super(null, null, entitlement.getEntitlements(KeysystemEnum.VWDFEED));
            this.vendorkey = vendorkey;
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o) && this.vendorkey.equals(((MyEntitlementQuote) o).vendorkey);
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + this.vendorkey.hashCode();
        }

        public String getSymbolVwdfeed() {
            return vendorkey;
        }

        @Override
        public String getSymbolVwdcode() {
            return this.vendorkey.substring(this.vendorkey.indexOf('.') + 1);
        }

        public Instrument getInstrument() {
            return new InstrumentDp2() {
                @Override
                public InstrumentTypeEnum getInstrumentType() {
                    return toInstrumentType(Integer.parseInt(vendorkey.substring(0, vendorkey.indexOf('.'))));
                }
            };
        }

        @Override
        public ContentFlags getContentFlags() {
            return ContentFlagsDp2.NO_FLAGS_SET;
        }

        public String getSymbolVwdfeedMarket() {
            if (this.vendorkey == null) {
                return null;
            }
            final int start = this.vendorkey.indexOf('.', 3) + 1;
            if (start == 0) {
                return null;
            }

            int end = this.vendorkey.indexOf('.', start + 1);
            if (end == -1) {
                end = this.vendorkey.length();
            }
            return this.vendorkey.substring(start, end);
        }
    }

    private EntitlementProvider entitlementProvider;

    public void setEntitlementProvider(EntitlementProvider entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public List<Quote> getQuotes(List<String> vendorkeys) {
        return vendorkeys.stream()
                .map(this::getQuote)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Quote getQuote(String vendorkey) {
        final Entitlement entitlement = this.entitlementProvider.getEntitlement(vendorkey);
        return new MyEntitlementQuote(entitlement, vendorkey);
    }
}
