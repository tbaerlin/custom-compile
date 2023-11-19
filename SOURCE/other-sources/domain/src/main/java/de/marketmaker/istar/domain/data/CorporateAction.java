package de.marketmaker.istar.domain.data;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface CorporateAction extends Comparable<CorporateAction> {
    public enum Type {
        DIVIDEND,
        FACTOR
    }

    DateTime getDate();
    BigDecimal getFactor();
    Type getType();
    String getCurrency();
}
