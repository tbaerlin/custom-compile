/*
 * ProfiledSnapRecordFactory.java
 *
 * Created on 16.09.2009 12:20:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.BitSet;

import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.data.NullSnapRecord;
import de.marketmaker.istar.feed.vwd.EndOfDayFilter;
import de.marketmaker.istar.feed.vwd.ProfiledSnapRecord;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ProfiledSnapRecordFactory {
    /** never modifies a snap record */
    static final ProfiledSnapRecordFactory IDENTITY = new ProfiledSnapRecordFactory();

    private final BitSet allowed;

    private final EndOfDayFilter eod;

    private final BitSet allowedEodFields;

    private ProfiledSnapRecordFactory() {
        this(null, null);
    }

    public ProfiledSnapRecordFactory(BitSet allowed, EndOfDayFilter eod) {
        this(allowed, eod, null);
    }

    public ProfiledSnapRecordFactory(BitSet allowed, EndOfDayFilter eod, BitSet allowedEodFields) {
        this.allowed = allowed;
        this.eod = eod;
        this.allowedEodFields = allowedEodFields;
    }

    /**
     * Applies a profile to the given SnapRecord and returns an adapted version that only allows
     * access to those fields that are allowed by the user's profile.
     * @param sr to be adapted
     * @return adapted record or sr if no adaptation is necessary
     */
    public SnapRecord applyProfileTo(SnapRecord sr) {
        return applyProfileTo(null, sr);
    }

    public SnapRecord applyProfileTo(String vendorkey, SnapRecord sr) {
        if (this.allowed == null || sr == null || sr == NullSnapRecord.INSTANCE) {
            return sr;
        }
        return new ProfiledSnapRecord(vendorkey, sr, this.allowed, this.eod, this.allowedEodFields);
    }
}
