/*
 * MetaDataKey.java
 *
 * Created on 11.01.12 15:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author zzhao
 */
public class MetaDataMapKey implements Comparable<MetaDataMapKey>, Serializable {

    private static final long serialVersionUID = 7526471155622716147L;

    private static final Set<RatioFieldDescription.Field> SCALAR_FIELDS =
            new HashSet<>(Arrays.asList(
                    RatioFieldDescription.morningstars,
                    RatioFieldDescription.ratingFeri,
                    RatioFieldDescription.ratingFitchLongTerm,
                    RatioFieldDescription.ratingFitchShortTerm,
                    RatioFieldDescription.ratingMoodysShortTerm,
                    RatioFieldDescription.ratingMoodysLongTerm,
                    RatioFieldDescription.ratingSnPLongTerm,
                    RatioFieldDescription.ratingSnPShortTerm
            ));

    private final RatioDataRecord.Field fromfield;

    private transient final RatioFieldDescription.Field toField;

    private int fieldId; // for serialization

    public MetaDataMapKey(RatioDataRecord.Field fromfield, RatioFieldDescription.Field toField) {
        this.fromfield = fromfield;
        this.toField = toField;
        this.fieldId = toField.id();
    }

    public String getName() {
        return this.fromfield.name();
    }

    public boolean isEnum() {
        return isEnum(this.toField);
    }

    public static boolean isEnum(RatioFieldDescription.Field field) {
        return field.isEnum() && !SCALAR_FIELDS.contains(field);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetaDataMapKey that = (MetaDataMapKey) o;

        if (fromfield != that.fromfield) return false;
        if (!toField.equals(that.toField)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fromfield.hashCode();
        result = 31 * result + toField.hashCode();
        return result;
    }

    private Object readResolve() throws ObjectStreamException {
        return new MetaDataMapKey(this.fromfield, RatioFieldDescription.getFieldById(this.fieldId));
    }

    @Override
    public int compareTo(MetaDataMapKey o) {
        return this.fromfield.name().compareTo(o.fromfield.name());
    }
}
