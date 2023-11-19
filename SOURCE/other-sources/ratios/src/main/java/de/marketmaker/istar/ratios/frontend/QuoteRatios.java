package de.marketmaker.istar.ratios.frontend;

import java.io.Serializable;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class QuoteRatios<T extends InstrumentRatios> implements Serializable, Selectable,
        RatioUpdatable, PropertySupported {
    protected static final long serialVersionUID = 9138274604395452487L;

    private static final int VWD_MARKET_FIELD_ID = RatioFieldDescription.vwdMarket.id();

    private static final Logger LOGGER = LoggerFactory.getLogger(QuoteRatios.class);

    private static final BitSet ACCESSED_DEPRECATED_FIELDS
            = new BitSet(RatioFieldDescription.getDeprecated().length());

    private final long id;

    private long timestamp;

    private T instrumentRatios;

    /** will be set by the current search to true iff the search profile's PriceQuality for
     *  this quote is not NONE */
    private transient boolean allowed;

    /** will be set by the current search to true iff allowed is true and either the
     * query selector is instrument only or it matches properties of this object */
    private transient boolean selected;

    private transient RatioEntitlementQuote quote;

    public QuoteRatios(long id, T instrumentRatios) {
        this.id = id;
        this.instrumentRatios = instrumentRatios;
    }

    boolean isAllowed() {
        return allowed;
    }

    void setAllowed(boolean allowed) {
        this.allowed = allowed;
        this.selected = allowed; // selected may be overridden later if quote selector does not match
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public long getId() {
        return this.id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public T getInstrumentRatios() {
        return this.instrumentRatios;
    }

    public RatioEntitlementQuote getEntitlementQuote() {
        return this.quote;
    }

    public void setEntitlement(RatioEntitlementQuote quote) {
        this.quote = quote;
    }

    public String getSymbolVwdfeedMarket() {
        return getString(VWD_MARKET_FIELD_ID);
    }

    protected RatioFieldDescription.Field getFieldByName(String fieldname) {
        final RatioFieldDescription.Field field = RatioFieldDescription.getFieldByName(fieldname);
        if (field != null && field.isDeprecated()) {
            ackDeprecatedFieldAccess(field);
        }
        return field;
    }

    protected void ackDeprecatedFieldAccess(int fid) {
        ackDeprecatedFieldAccess(RatioFieldDescription.getFieldById(fid));
    }

    protected void ackDeprecatedFieldAccess(RatioFieldDescription.Field f) {
        synchronized (ACCESSED_DEPRECATED_FIELDS) {
            if (!ACCESSED_DEPRECATED_FIELDS.get(f.id())) {
                ACCESSED_DEPRECATED_FIELDS.set(f.id());
                LOGGER.warn("Deprecated field " + f + " is still used");
            }
        }
    }
}
