/*
 * ProfiledSnapRecord.java
 *
 * Created on 30.01.2009 09:42:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.NullSnapField;
import de.marketmaker.istar.domain.data.LiteralSnapField;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;

/**
 * A SnapRecord that restricts the fields available in a delegate SnapRecord. Useful if different
 * fields in a record belong to different field groups and a requestor is only allowed to access
 * some of those groups.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ProfiledSnapRecord implements SnapRecord, Serializable {
    protected static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfiledSnapRecord.class);

    private static final VwdFieldDescription.Field[] DATE_FIELDS
            = new VwdFieldDescription.Field[]{
            VwdFieldDescription.MMF_Bezahlt_Datum,
            VwdFieldDescription.ADF_Schluss_Datum,
            VwdFieldDescription.ADF_Handelsdatum,
            VwdFieldDescription.ADF_Datum
    };

    private static final SnapField NULL_TIME =
            LiteralSnapField.createNumber(VwdFieldDescription.ADF_TIMEOFARR.id(), 0);

    private final BitSet allowedFields;

    private final boolean eodFilter;

    private final boolean allowMmfBoersenzeit;

    private final SnapRecord delegate;

    public ProfiledSnapRecord(String vendorkey, SnapRecord delegate, BitSet allowedFields,
            EndOfDayFilter eodFilter, BitSet allowedEodFields) {
        this.delegate = delegate;
        this.eodFilter = (eodFilter != null);

        if (isEoDFilterNeeded(vendorkey, eodFilter)) {
            this.allowedFields = new BitSet(allowedFields.length());
            this.allowedFields.or(allowedFields);
            this.allowedFields.andNot(eodFilter.getNonEodFields());
        }
        else {
            this.allowedFields = allowedFields;
        }

        applyAllowedEodFields(this.allowedFields, allowedEodFields);

        this.allowMmfBoersenzeit = this.allowedFields.get(VwdFieldDescription.ADF_Boersenzeit.id())
                        && this.allowedFields.get(VwdFieldDescription.ADF_Zeit.id());
    }

    /**
     * Leaks real time data, since it is applied without taking into account trading hours.
     * This is the intended behavior for Helaba (MCC).
     */
    private void applyAllowedEodFields(BitSet allowedFields, BitSet allowedEodFields) {
        if (Objects.nonNull(allowedEodFields)) {
            allowedFields.or(allowedEodFields);
        }
    }

    private boolean isEoDFilterNeeded(String vendorkey, EndOfDayFilter eodFilter) {
        if (eodFilter == null) {
            return false;
        }

        DateTime now = new DateTime(DateUtil.DTZ_BERLIN);

        final int maxDate = getMaxDate(DATE_FIELDS);
        final int yyyymmdd = DateUtil.toYyyyMmDd(now);

        if (DateUtil.DTZ_BERLIN != eodFilter.getZone()) {
            final DateTime eodNow = now.toDateTime(eodFilter.getZone());
            if (eodNow.getDayOfYear() < now.getDayOfYear() && maxDate >= DateUtil.toYyyyMmDd(eodNow)) {
                if (maxDate < yyyymmdd) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("<isEodFilterNeeded> true for " + vendorkey + " in local time zone: "
                                + eodFilter.getZone() + " " + eodNow.toLocalDate());
                    }
                }
                return true;
            }
        }

        return maxDate >= yyyymmdd;
    }

    private int getMaxDate(VwdFieldDescription.Field... fields) {
        int result = getDate(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            result = Math.max(result, getDate(fields[i]));
        }
        return result;
    }

    private int getDate(VwdFieldDescription.Field field) {
        return SnapRecordUtils.getInt(this.delegate.getField(field.id()));
    }

    public String toString() {
        return "ProfiledSnapRecord[" + this.allowedFields + ", " + this.delegate + "]";
    }

    public Collection<SnapField> getSnapFields() {
        final Collection<SnapField> snapFields = this.delegate.getSnapFields();
        final ArrayList<SnapField> result = new ArrayList<>(snapFields.size());
        for (SnapField field : snapFields) {
            if (isAllowed(field)) {
                result.add(field);
            }
        }
        return result;
    }

    private boolean isAllowed(SnapField field) {
        return this.allowedFields.get(field.getId())
                || (field.getId() == VwdFieldDescription.MMF_Boersenzeit.id() && this.allowMmfBoersenzeit);
    }

    public SnapField getField(int fieldId) {
        return launderField(this.delegate.getField(fieldId));
    }

    public SnapField getField(String fieldname) {
        return launderField(this.delegate.getField(fieldname));
    }

    @Override
    public int getNominalDelayInSeconds() {
        return this.eodFilter ? 0 : this.delegate.getNominalDelayInSeconds();
    }

    private SnapField launderField(SnapField field) {
        if (field == null || !field.isDefined()) {
            return field;
        }
        if (this.eodFilter && field.getId() == VwdFieldDescription.ADF_TIMEOFARR.id()) {
            return NULL_TIME;
        }
        return isAllowed(field) ? field : NullSnapField.INSTANCE;
    }
}
