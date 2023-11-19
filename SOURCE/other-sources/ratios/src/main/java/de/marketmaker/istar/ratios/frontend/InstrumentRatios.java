package de.marketmaker.istar.ratios.frontend;

import java.io.Serializable;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class InstrumentRatios implements Serializable, Selectable, RatioUpdatable,
        PropertySupported {
    static final long serialVersionUID = 4527583791859053033L;

    private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentRatios.class);

    private static final BitSet ACCESSED_DEPRECATED_FIELDS
            = new BitSet(RatioFieldDescription.getDeprecated().length());

    private final long id;

    private long timestamp;

    public static InstrumentRatios create(InstrumentTypeEnum type, long instrumentid) {
        switch (type) {
            case BND:
                return new InstrumentRatiosBND(instrumentid);
            case CER:
                return new InstrumentRatiosCER(instrumentid);
            case FND:
                return new InstrumentRatiosFND(instrumentid);
            case STK:
                return new InstrumentRatiosSTK(instrumentid);
            case WNT:
                return new InstrumentRatiosWNT(instrumentid);
            case FUT:
                return new InstrumentRatiosFUT(instrumentid);
            case OPT:
                return new InstrumentRatiosOPT(instrumentid);
            case IND:
                return new InstrumentRatiosIND(instrumentid);
            case CUR:
                return new InstrumentRatiosCUR(instrumentid);
            case GNS:
                return new InstrumentRatiosGNS(instrumentid);
            case ZNS:
                return new InstrumentRatiosZNS(instrumentid);
            case MER:
                return new InstrumentRatiosMER(instrumentid);
            default:
                throw new IllegalArgumentException("unsupported type: " + type);
        }
    }

    protected InstrumentRatios(long id) {
        this.id = id;
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

    public abstract QuoteRatios createQuoteRatios(long quoteid);
}
