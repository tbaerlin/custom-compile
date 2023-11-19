/*
 * UnionDp2.java
 *
 * Created on 29.07.13 09:01
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domainimpl.DomainContext;
import de.marketmaker.istar.domainimpl.instrument.BondDp2;
import de.marketmaker.istar.domainimpl.instrument.FutureDp2;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import de.marketmaker.istar.domainimpl.instrument.OptionDp2;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.domainimpl.instrument.StockDp2;

/**
 * @author zzhao
 */
class UnionDp2 extends InstrumentDp2 {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnionDp2.class);

    enum DpAdfInterfaceField {
        GeneralMeetingDate("agm"),
        OptionUnderlying("opt_und"),
        OptionMaturity("opt_maturity"),
        OptionStrike("opt_strike"),
        OptionDerivativeType("opt_ostype"),
        StrikeCurrency("strike_currencyid"),
        FutureBondUnderlying("fbnd_und"),
        FutureUnderlying("fut_und"),
        FutureMaturity("fut_maturity"),
        FutureContractCurrency("future_contractcurrency"),
        FutureContractValue("future_contractvalue"),
        FutureTickCurrency("future_tickcurrency"),
        FutureTickSize("future_ticksize"),
        FutureTickValue("future_tickvalue"),
        BondMaturity("bnd_maturity"),
        BondMmName("mmnamebond"),;

        private final String columnName; // todo more fields like InstrumentTypeEnum, Type in SQL and Instrument??

        private DpAdfInterfaceField(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }
    }

    private static final EnumSet<DpAdfInterfaceField> FIELDS = EnumSet.allOf(DpAdfInterfaceField.class);

    static UnionDp2 create(long id, ResultSet rs) throws SQLException {
        EnumMap<DpAdfInterfaceField, Object> map = new EnumMap<>(DpAdfInterfaceField.class);
        for (DpAdfInterfaceField field : FIELDS) {
            final Object obj = rs.getObject(field.getColumnName());
            if (!rs.wasNull()) {
                map.put(field, obj);
            }
        }
        return new UnionDp2(id, map);
    }

    private final Map<DpAdfInterfaceField, Object> data;

    private UnionDp2(long id, Map<DpAdfInterfaceField, Object> map) {
        super(id);
        this.data = map;
    }

    @Override
    public InstrumentTypeEnum getInstrumentType() {
        return null;
    }

    public InstrumentDp2 toRealInstrument(QuotesForInstrument quotes, DomainContext dc) {
        for (QuoteDp2 quote : quotes.quotes) {
            final InstrumentDp2 ins = toRealInstrument(quote.getSymbolVwdfeed(), dc);
            if (ins != null) {
                return ins;
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<toRealInstrument> ignoring " + this + ", quotes=" + quotes.quotes);
        }
        return null;
    }

    private InstrumentDp2 toRealInstrument(String vwdfeed, DomainContext dc) {
        if (!StringUtils.hasText(vwdfeed)) {
            return null;
        }

        final InstrumentDp2 ins = createInstrumentDp2(vwdfeed, dc);
        if (ins == null) {
            return null;
        }

        ins.setName(getName());
        ins.setAliases(getAliases());
        ins.setHomeExchange(getHomeExchange());
        ins.setSector(getSector());
        ins.setCountry(getCountry());
        ins.setDetailedInstrumentType(getDetailedInstrumentType());

        for (KeysystemEnum keySystem : KeysystemEnum.values()) {
            if (StringUtils.hasText(getSymbol(keySystem))) {
                ins.setSymbol(keySystem, getSymbol(keySystem));
            }
        }
        return ins;
    }

    private InstrumentDp2 createInstrumentDp2(String vwdfeed, DomainContext dc) {
        final int vwdType = Integer.parseInt(vwdfeed.substring(0, vwdfeed.indexOf(".")));
        switch (vwdType) {
            case 1:
                return toStock();
            case 2:
                return toOption(dc);
            case 3:
                return toFuture(dc);
            case 5:
                return toBond();
            default:
                return null;
        }
    }

    private InstrumentDp2 toBond() {
        final BondDp2 result = new BondDp2(getId());

        if (this.data.containsKey(DpAdfInterfaceField.BondMaturity)) {
            result.setExpirationDate(getYyyyMmDd(DpAdfInterfaceField.BondMaturity));
        }

        if (this.data.containsKey(DpAdfInterfaceField.BondMmName)) {
            result.setName(getString(DpAdfInterfaceField.BondMmName));
        }
        return result;
    }

    private InstrumentDp2 toFuture(DomainContext dc) {
        final FutureDp2 result = new FutureDp2(getId());
        if (this.data.containsKey(DpAdfInterfaceField.FutureBondUnderlying)) {
            result.setUnderlyingId(getLong(DpAdfInterfaceField.FutureBondUnderlying));
            result.setUnderlyingProductId(getLong(DpAdfInterfaceField.FutureBondUnderlying));
            if (this.data.containsKey(DpAdfInterfaceField.FutureUnderlying)) {
                result.setUnderlyingProductId(getLong(DpAdfInterfaceField.FutureUnderlying));
            }
        }
        else if (this.data.containsKey(DpAdfInterfaceField.FutureUnderlying)) {
            result.setUnderlyingId(getLong(DpAdfInterfaceField.FutureUnderlying));
            result.setUnderlyingProductId(getLong(DpAdfInterfaceField.FutureUnderlying));
        }

        if (this.data.containsKey(DpAdfInterfaceField.FutureMaturity)) {
            result.setExpirationDate(getYyyyMmDd(DpAdfInterfaceField.FutureMaturity));
        }
        if (this.data.containsKey(DpAdfInterfaceField.FutureContractCurrency)) {
            result.setContractCurrency(dc.getCurrency(
                    getLong(DpAdfInterfaceField.FutureContractCurrency)));
        }
        if (this.data.containsKey(DpAdfInterfaceField.FutureContractValue)) {
            result.setContractValue(InstrumentReader.toBigDecimal(
                    getDouble(DpAdfInterfaceField.FutureContractValue)));
        }
        if (this.data.containsKey(DpAdfInterfaceField.FutureTickCurrency)) {
            result.setTickCurrency(dc.getCurrency(
                    getLong(DpAdfInterfaceField.FutureTickCurrency)));
        }
        if (this.data.containsKey(DpAdfInterfaceField.FutureTickSize)) {
            result.setTickSize(InstrumentReader.toBigDecimal(
                    getDouble(DpAdfInterfaceField.FutureTickSize)));
        }
        if (this.data.containsKey(DpAdfInterfaceField.FutureTickValue)) {
            result.setTickValue(InstrumentReader.toBigDecimal(
                    getDouble(DpAdfInterfaceField.FutureTickValue)));
        }
        return result;
    }

    private InstrumentDp2 toOption(DomainContext dc) {
        final OptionDp2 result = new OptionDp2(getId());
        if (this.data.containsKey(DpAdfInterfaceField.OptionUnderlying)) {
            result.setUnderlyingId(getLong(DpAdfInterfaceField.OptionUnderlying));
        }
        if (this.data.containsKey(DpAdfInterfaceField.OptionMaturity)) {
            result.setExpirationDate(getYyyyMmDd(DpAdfInterfaceField.OptionMaturity));
        }
        if (this.data.containsKey(DpAdfInterfaceField.OptionStrike)) {
            result.setStrike(InstrumentReader.toBigDecimal(
                    getDouble(DpAdfInterfaceField.OptionStrike)));
        }
        if (this.data.containsKey(DpAdfInterfaceField.StrikeCurrency)) {
            result.setStrikeCurrency(dc.getCurrency(
                    getLong(DpAdfInterfaceField.StrikeCurrency)));
        }
        if (this.data.containsKey(DpAdfInterfaceField.OptionDerivativeType)) {
            result.setType(InstrumentReader.getDerivativeType(
                    getString(DpAdfInterfaceField.OptionDerivativeType)));
        }

        return result;
    }

    private InstrumentDp2 toStock() {
        final StockDp2 result = new StockDp2(getId());
        if (this.data.containsKey(DpAdfInterfaceField.GeneralMeetingDate)) {
            result.setGeneralMeetingDate(getYyyyMmDd(DpAdfInterfaceField.GeneralMeetingDate));
        }
        return result;
    }

    private double getDouble(DpAdfInterfaceField field) {
        return (Double) this.data.get(field);
    }

    private String getString(DpAdfInterfaceField field) {
        return (String) this.data.get(field);
    }

    private long getLong(DpAdfInterfaceField field) {
        return (Long) this.data.get(field);
    }

    private int getYyyyMmDd(DpAdfInterfaceField field) {
        return DateUtil.dateToYyyyMmDd((Date) this.data.get(field));
    }
}
