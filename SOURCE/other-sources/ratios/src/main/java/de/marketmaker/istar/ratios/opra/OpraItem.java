/*
 * MerDataProviderImpl.java
 *
 * Created on 26.09.2008 07:36:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.opra;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.Selectable;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import static de.marketmaker.istar.ratios.frontend.EnumFlyweightFactory.intern;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OpraItem implements Serializable, Selectable {
    private static final long serialVersionUID = 2L;

    private static final String EXERCISE_TYPE = intern(RatioFieldDescription.exerciseType.id(), "american");

    private static final String CALL = intern(RatioFieldDescription.osType.id(), "C");

    private static final String PUT = intern(RatioFieldDescription.osType.id(), "P");

    private static final boolean IS_FLEX = false;

    private static final Map<RatioFieldDescription.Field, Comparator<OpraItem>> COMPARATORS = new HashMap<RatioFieldDescription.Field, Comparator<OpraItem>>() {
        {
            put(RatioFieldDescription.underlyingWkn, Comparator.comparing(OpraItem::getUnderlyingSymbol));
            put(RatioFieldDescription.vwdMarket, Comparator.comparing(OpraItem::getExchange));
            put(RatioFieldDescription.wkn, Comparator.comparing(OpraItem::getOptionSymbol));
            put(RatioFieldDescription.expires, Comparator.comparingInt(o -> o.expirationDate));
            put(RatioFieldDescription.currencyStrike, Comparator.comparing(OpraItem::getVersion));
            put(RatioFieldDescription.strikePrice, Comparator.comparing(OpraItem::getStrike));
            put(RatioFieldDescription.osType, Comparator.comparing(OpraItem::getOptionType));
            put(RatioFieldDescription.vwdCode, Comparator.comparing(OpraItem::getVwdcode));
            put(RatioFieldDescription.isFlex, (o1, o2) -> Boolean.compare(o1.getIsFlex(), o2.getIsFlex()));
            put(RatioFieldDescription.optionCategory, Comparator.comparing(OpraItem::getOptionCategory));
        }
    };

    private final String underlyingSymbol;

    private final String exchange;

    private int expirationDate;

    private final String version;

    private final int strike;

    private final String maturity;

    private long settlement;

    private int settlementDate;

    private long contractSize;

    private String optionCategory; // weekly or default

    public OpraItem(
            String underlyingSymbol,
            String exchange,
            int expirationDate,
            String version,
            int strike,
            String maturity) {
        this.underlyingSymbol = underlyingSymbol;
        this.exchange = exchange;
        this.expirationDate = expirationDate;
        this.version = version;
        this.strike = strike;
        this.maturity = maturity;
    }

    public void setExpirationDate(int expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setSettlement(long settlement, int settlementDate) {
        this.settlement = settlement;
        this.settlementDate = settlementDate;
    }

    private String computeVwdcode() {
        final StringBuilder sb = new StringBuilder(20)
                .append(this.underlyingSymbol)
                .append(".").append(this.exchange)
                .append(".").append(this.strike);
        if (!"0".equals(this.version)) {
            sb.append(".").append(this.version);
        }
        return sb.append(".").append(this.maturity).toString();
    }

    public long getSettlement() {
        return this.settlement;
    }

    public LocalDate getSettlementDate() {
        return this.settlementDate > 0 ? DateUtil.yyyyMmDdToLocalDate(this.settlementDate) : null;
    }

    public String getUnderlyingSymbol() {
        return this.underlyingSymbol;
    }

    public String getExchange() {
        return this.exchange;
    }

    public String getOptionSymbol() {
        return this.underlyingSymbol;
    }

    public DateTime getExpirationDate() {
        if (this.expirationDate == 0) {
            return null;
        }
        return DateUtil.yyyyMmDdToLocalDate(this.expirationDate).toDateTimeAtStartOfDay();
    }

    public DateTime getTradingMonth() {
        // no specific trading month field for opra -> use expiration date
        return getExpirationDate();
    }

    public String getVersion() {
        return this.version;
    }

    public BigDecimal getStrike() {
        return BigDecimal.valueOf(this.strike, 2);
    }

    public String getOptionType() {
        return this.maturity.charAt(1) <= 'L' ? CALL : PUT;
    }

    public String getVwdcode() {
        return computeVwdcode();
    }

    public void setOptionCategory(String optionCategory) {
        this.optionCategory = optionCategory;
    }

    public String getOptionCategory() {
        return this.optionCategory;
    }

    public String getExerciseType() {
        return EXERCISE_TYPE;
    }

    public long getContractSize() {
        return this.contractSize;
    }

    public boolean getIsFlex() {
        return IS_FLEX;
    }

    public void setContractSize(long contractSize) {
        this.contractSize = contractSize;
    }

    @Override
    public String getString(int fieldid) {
        if (RatioFieldDescription.underlyingWkn.id() == fieldid) {
            return getUnderlyingSymbol();
        }
        else if (RatioFieldDescription.vwdMarket.id() == fieldid) {
            return getExchange();
        }
        else if (RatioFieldDescription.wkn.id() == fieldid) {
            return getOptionSymbol();
        }
        else if (RatioFieldDescription.vwdCode.id() == fieldid) {
            return getVwdcode();
        }
        else if (RatioFieldDescription.osType.id() == fieldid) {
            return getOptionType();
        }
        else if (RatioFieldDescription.currencyStrike.id() == fieldid) {
            return getVersion();
        }
        else if (RatioFieldDescription.isFlex.id() == fieldid) {
            return Boolean.toString(getIsFlex());
        }
        else if (RatioFieldDescription.exerciseType.id() == fieldid) {
            return EXERCISE_TYPE;
        }
        else if (RatioFieldDescription.optionCategory.id() == fieldid) {
            return getOptionCategory();
        }
        return null;
    }

    @Override
    public String getString(int fieldid, int localeIndex) {
        return getString(fieldid);
    }

    @Override
    public Long getLong(int fieldid) {
        if (RatioFieldDescription.strikePrice.id() == fieldid) {
            // strike was decoded as part of the feed symbol (e.g. Z_30.OPRA.4500.7F) and is
            // multiplied by 10^2 the normal price encoding to render properly in the view is
            // 10^5 (see: PriceCoder.encode() so we need another 10^3....
            return this.strike * 1000L;
        } else if (RatioFieldDescription.contractSize.id() == fieldid) {
            return this.contractSize;
        }
        return null;
    }

    @Override
    public BitSet getBitSet(int fieldid) {
        // no-op
        return null;
    }

    @Override
    public Integer getInt(int fieldid) {
        if (RatioFieldDescription.expires.id() == fieldid && this.expirationDate != 0) {
            return this.expirationDate;
        }
        return null;
    }

    @Override
    public Boolean getBoolean(int fieldid) {
        if (RatioFieldDescription.isFlex.id() == fieldid) {
            return IS_FLEX;
        }
        return null;
    }

    public static Comparator<OpraItem> getComparator(RatioFieldDescription.Field field) {
        return COMPARATORS.get(field);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final OpraItem opraItem = (OpraItem) o;

        if (this.strike != opraItem.strike) return false;
        if (!this.exchange.equals(opraItem.exchange)) return false;
        if (!this.maturity.equals(opraItem.maturity)) return false;
        if (!this.underlyingSymbol.equals(opraItem.underlyingSymbol)) return false;
        if (!this.version.equals(opraItem.version)) return false;
        if (!this.optionCategory.equals(opraItem.optionCategory)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.underlyingSymbol.hashCode();
        result = 31 * result + this.exchange.hashCode();
        result = 31 * result + this.version.hashCode();
        result = 31 * result + this.strike;
        result = 31 * result + this.maturity.hashCode();
        result = 31 * result + this.optionCategory.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "OpraItem{" + getVwdcode() + "}"
                + " contractSize: " + getContractSize()
                + " optionType: " + getOptionType()
                + " optionCategory: " + getOptionCategory()
                ;
    }


}
