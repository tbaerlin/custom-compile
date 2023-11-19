/*
 * StaticReader.java
 *
 * Created on 25.10.2005 20:32:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.ratios.frontend.RatioEnumSet;
import java.util.BitSet;
import java.util.Map;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.BackendUpdateReceiver;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StaticMessageBuilder implements StaticDataCallback {
    private BackendUpdateReceiver frontend;

    private final RatiosEncoder encoder = new StaticRatiosEncoder();

    public void setFrontend(BackendUpdateReceiver frontend) {
        this.frontend = frontend;
    }

    public void process(final InstrumentTypeEnum type, final long instrumentid,
            final Map<Integer, Object> fields) {
        this.encoder.reset(type, instrumentid, Long.MIN_VALUE);

        for (final Map.Entry<Integer, Object> entry : fields.entrySet()) {
            final RatioFieldDescription.Field field
                    = RatioFieldDescription.getFieldById(entry.getKey());
            if (!field.isApplicableFor(type)) {
                continue;
            }

            switch (field.type()) {
                case NUMBER:
                case DECIMAL:
                    this.encoder.add(entry.getKey(), asLong(entry.getValue()));
                    break;
                case ENUMSET:
                    this.encoder.add(entry.getKey(), asBitSet(entry.getValue()));
                    break;
                case DATE:
                case TIME:
                    this.encoder.add(entry.getKey(), asInt(entry.getValue()));
                    break;
                case TIMESTAMP:
                    //                    this.encoder.add(entry.getKey(), (Integer) entry.getValue());
                    break;
                case STRING:
                    addString(entry, field);
                    break;
                case BOOLEAN:
                    this.encoder.add(entry.getKey(), asBoolean(entry.getValue()));
                    break;
            }
        }

        if (this.encoder.hasData()) {
            this.frontend.update(this.encoder.getData());
        }
    }

    private void addString(Map.Entry<Integer, Object> entry, RatioFieldDescription.Field field) {
        if (field.isLocalized()) {
            final String[] values = (entry.getValue() != null)
                    ? (String[]) entry.getValue()
                    : new String[field.getLocales().length];
            for (int i = 0; i < values.length; i++) {
                this.encoder.add(entry.getKey(), i, values[i]);
            }
        }
        else {
            this.encoder.add(entry.getKey(), (String) entry.getValue());
        }
    }

    private Boolean asBoolean(final Object value) {
        return (value == null) ? Boolean.FALSE : (Boolean) value;
    }

    private int asInt(final Object value) {
        return (value == null) ? Integer.MIN_VALUE : (Integer) value;
    }

    private long asLong(final Object value) {
        return (value == null) ? Long.MIN_VALUE : (Long) value;
    }

    private BitSet asBitSet(final Object value) {
        return (value == null) ? RatioEnumSet.unmodifiableBitSet() : (BitSet) value;
    }
}
