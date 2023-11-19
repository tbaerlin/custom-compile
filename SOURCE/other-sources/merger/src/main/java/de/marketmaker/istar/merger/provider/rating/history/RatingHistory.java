/*
 * RatingHistory.java
 *
 * Created on 11.09.12 14:06
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating.history;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDate;

import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author zzhao
 */
public class RatingHistory implements Serializable {

    private static final long serialVersionUID = 7614986595835948841L;

    private transient RatioFieldDescription.Field field;

    private final int fieldId;

    private List<RatingItem> ratings = new ArrayList<>();

    public RatingHistory(RatioFieldDescription.Field field) {
        this.field = field;
        this.fieldId = field.id();
    }

    public RatingHistory(RatioFieldDescription.Field field, List<RatingItem> ratings) {
        this(field);
        this.ratings.addAll(ratings);
    }

    public void aggregateUnchangedRatings() {
        Collections.sort(this.ratings, Collections.reverseOrder()); // ratings defaults descending
        final Iterator<RatingItem> it = this.ratings.iterator();
        RatingItem curItem = null;
        while (it.hasNext()) {
            final RatingItem next = it.next();
            if (curItem != null && next.getValue().equals(curItem.getValue())) {
                it.remove();
            }
            curItem = next;
        }
    }

    void addRatingItem(RatingItem ri) {
        this.ratings.add(ri);
    }

    public RatioFieldDescription.Field getField() {
        return field;
    }

    public String getRatingSystemName() {
        final String ratingSystemName = this.field.getRatingSystemName();
        return null != ratingSystemName ? ratingSystemName : this.field.name();
    }

    public List<RatingItem> getRatings() {
        final ArrayList<RatingItem> ret = new ArrayList<>(this.ratings);
        ret.sort(null);
        return ret;
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.field = RatioFieldDescription.getFieldById(this.fieldId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RatingHistory that = (RatingHistory) o;

        if (!field.equals(that.field)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    public static class RatingItem implements Comparable<RatingItem>, Serializable {
        private static final long serialVersionUID = -4242100802313888041L;

        private final LocalDate date;

        private final String value;

        public RatingItem(LocalDate date, String value) {
            this.date = date;
            this.value = value;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getValue() {
            return value;
        }

        @Override
        public int compareTo(RatingItem o) {
            return o.date.compareTo(this.date);
        }
    }
}
