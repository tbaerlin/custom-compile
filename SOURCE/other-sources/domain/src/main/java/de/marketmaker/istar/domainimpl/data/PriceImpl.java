/*
 * PriceImpl.java
 *
 * Created on 07.07.2006 15:16:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Objects;

import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;

import org.joda.time.DateTime;
import net.jcip.annotations.Immutable;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Default Price implementation.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class PriceImpl implements Price, Serializable {
    static final long serialVersionUID = 7909506777256833187L;

    private final PriceQuality quality;

    private final DateTime date;

    private final String supplement;

    private final BigDecimal value;

    private final Long volume;

    public PriceImpl(BigDecimal value, Long volume, String supplement, DateTime date,
            PriceQuality quality) {
        this.value = value;
        this.volume = volume;
        this.supplement = supplement;
        this.date = date;
        this.quality = quality;
    }

    public DateTime getDate() {
        return this.date;
    }

    public String getSupplement() {
        return this.supplement;
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public Long getVolume() {
        return this.volume;
    }

    /**
     * Always returns true; use {@link NullPriceRecord} to return an undefined price.
     * @return true
     */
    public boolean isDefined() {
        return true;
    }

    public boolean isDelayed() {
        return this.quality == PriceQuality.DELAYED;
    }

    public boolean isEndOfDay() {
        return this.quality == PriceQuality.END_OF_DAY;
    }

    public boolean isRealtime() {
        return this.quality == PriceQuality.REALTIME;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(60).append("PriceImpl[").append(this.value);
        if (supplement != null && supplement.length() > 0) {
            sb.append("(").append(this.supplement).append(")");
        }
        if (volume != null) {
            sb.append(", #").append(this.volume);
        }
        sb.append(", ").append(this.date != null ? ISODateTimeFormat.dateTimeNoMillis().print(this.date) : null);
        sb.append(", ").append(this.quality.name());
        return sb.append(']').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PriceImpl other = (PriceImpl) o;

        if (!Objects.equals(date, other.date)) return false;
        if (quality != other.quality) return false;
        if (!Objects.equals(supplement, other.supplement)) return false;
        if (!Objects.equals(value, other.value)) return false;
        if (!Objects.equals(volume, other.volume)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = quality != null ? quality.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (supplement != null ? supplement.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (volume != null ? volume.hashCode() : 0);
        return result;
    }
}
