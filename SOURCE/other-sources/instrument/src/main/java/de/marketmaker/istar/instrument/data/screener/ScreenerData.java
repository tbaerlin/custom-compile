/*
 * ScreenerData.java
 *
 * Created on 28.04.2005 08:24:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.data.screener;

import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ScreenerData implements Serializable {
    static final long serialVersionUID = 1L;

    private final long instrumentid;
    private final Locale locale;
    private final SnapRecord snapRecord;
    private final Map<Integer, EvaluatedRule> rules = new HashMap<>();

    public ScreenerData(long instrumentid, Locale locale, SnapRecord snapRecord) {
        this.instrumentid = instrumentid;
        this.locale = locale;
        this.snapRecord = snapRecord;
    }

    void add(SnapField field, EvaluatedRule rule) {
        this.rules.put(field.getId(), rule);
    }

    public Collection<SnapField> getFields() {
        return this.snapRecord.getSnapFields();
    }

    public SnapField getField(String fieldname) {
        return this.snapRecord.getField(fieldname);
    }

    public SnapField getField(int fieldid) {
        return this.snapRecord.getField(fieldid);
    }

    public EvaluatedRule getEvaluatedRule(int fieldid) {
        return this.rules.get(fieldid);
    }

    public long getInstrumentid() {
        return instrumentid;
    }

    public Locale getLocale() {
        return locale;
    }

    public List<ScreenerAlternative> getAltGroup() {
        return ((SnapRecordScreener) this.snapRecord).getAltGroup();
    }

    public List<ScreenerAlternative> getAltCountry() {
        return ((SnapRecordScreener) this.snapRecord).getAltCountry();
    }

    public String toString() {
        return "ScreenerData[instrumentid=" + instrumentid
                + ", locale=" + locale
                + ", rules=" + rules
                + "]";
    }

    public static class EvaluatedRule implements Serializable {
        static final long serialVersionUID = 1L;

        private String encodedImage = null;
        private URL imageUrl = null;
        private String shortText;
        private String longText;

        public EvaluatedRule(String shortText, String longText) {
            this.shortText = shortText;
            this.longText = longText;
        }

        public void setEncodedImage(String encodedImage) {
            this.encodedImage = encodedImage;
        }

        public void setImageUrl(URL imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getEncodedImage() {
            return encodedImage;
        }

        public URL getImageUrl() {
            return imageUrl;
        }

        public String getShortText() {
            return shortText;
        }

        public String getLongText() {
            return longText;
        }

        public String toString() {
            return "EvaluatedRule[short=" + shortText
                    + ", long=" + longText
                    + ", imageUrl=" + imageUrl
                    + ", encodedImage=" + encodedImage
                    + "]";
        }
    }
}
