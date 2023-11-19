package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.CorporateAction;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
abstract public class AbstractCorporateAction implements Serializable, CorporateAction {
    protected static final long serialVersionUID = 1L;

    private final DateTime date;
    private final BigDecimal factor;

    public AbstractCorporateAction(DateTime date, BigDecimal factor) {
        this.date = date;
        this.factor = factor;
    }

    public DateTime getDate() {
        return date;
    }

    public BigDecimal getFactor() {
        return factor;
    }

    public int compareTo(CorporateAction o) {
        return this.date.compareTo(o.getDate());
    }

    abstract public Type getType();

    abstract public String getCurrency();

    abstract public BigDecimal getYield();

    public static class Dividend extends AbstractCorporateAction implements Serializable {
        protected static final long serialVersionUID = 1L;

        private final String currency;

        private final BigDecimal yield;

        public Dividend(DateTime date, BigDecimal factor, String currency, BigDecimal yield) {
            super(date, factor);
            this.currency = currency;
            this.yield = yield;
        }

        public String getCurrency() {
            return currency;
        }

        public CorporateAction.Type getType() {
            return CorporateAction.Type.DIVIDEND;
        }

        public BigDecimal getYield() {
            return yield;
        }

        public String toString() {
            return "Dividend[date=" + getDate()
                    + ", value=" + getFactor()
                    + ", currency=" + currency
                    + "]";
        }
    }

    public static class Factor extends AbstractCorporateAction implements Serializable {
        protected static final long serialVersionUID = 1L;

        public Factor(DateTime date, BigDecimal factor) {
            super(date, factor);
        }

        public Type getType() {
            return CorporateAction.Type.FACTOR;
        }

        public String getCurrency() {
            return null;
        }

        @Override
        public BigDecimal getYield() {
            return null;
        }

        public String toString() {
            return "Factor[date=" + getDate()
                    + ", factor=" + getFactor()
                    + "]";
        }
    }
}
