/*
 * AnalysisImpl.java
 *
 * Created on 27.04.12 10:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.frontend;

import java.io.Serializable;
import java.math.BigDecimal;

import com.google.protobuf.InvalidProtocolBufferException;
import org.joda.time.DateTime;

import de.marketmaker.istar.analyses.backend.Protos;
import de.marketmaker.istar.domain.data.StockAnalysis;

/**
 * @author oflege
 */
public class AnalysisImpl implements StockAnalysis, Serializable {
    protected static final long serialVersionUID = 1L;

    private final byte[] protobufData;

    /**
     * the serialized analysis may contain only the sector's key and the actual name may only
     * be available in the provider-specific backend. This field can be set by the backend
     * to specify the actual sector name.
     */
    private String sector;
    
    private transient Protos.Analysis analysis;

    public AnalysisImpl(byte[] protobufData) {
        this.protobufData = protobufData;
        //noinspection ResultOfMethodCallIgnored
        readResolve();
    }

    private Object readResolve() {
        if (this.analysis == null) {
            try {
                this.analysis = Protos.Analysis.parseFrom(this.protobufData);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    @Override
    public String getId() {
        return Long.toString(this.analysis.getId(), Character.MAX_RADIX);
    }

    public String getShortId() {
        return (getId() + "zzzzzz").substring(0, 7);
    }

    @Override
    public Long getInstrumentid() {
        return (this.analysis.getIidCount() > 0) ? this.analysis.getIid(0) : null;
    }

    @Override
    public DateTime getDate() {
        return new DateTime(this.analysis.getAgencyDate());
    }

    @Override
    public String getSource() {
        if (this.analysis.hasSource()) {
            return this.analysis.getSource();
        }
        if (isWebSim()) {
            return "websim.it";
        }
        return null;
    }

    @Override
    public String getHeadline() {
        return this.analysis.getHeadline();
    }

    @Override
    public String getText() {
        return isWebSim() ? getWebSimAnalysisText() : getAnalysisText("\n\n");
    }

    @Override
    public String getSector() {
        if (this.sector != null) {
            return this.sector;
        }
        return this.analysis.getBranchCount() > 0 ? this.analysis.getBranch(0) : null;
    }

    public String getRecommendationStr() {
        if (isWebSim()) {
            StringBuilder sb = new StringBuilder();
            if (this.analysis.hasWebSimRaccfond()) {
                sb.append(this.analysis.getWebSimRaccfond());
            }
            sb.append("/");
            if (this.analysis.hasWebSimRacctecn()) {
                sb.append(this.analysis.getWebSimRacctecn());
            }
            return sb.toString();
        }
        return getRecommendation().name();
    }

    @Override
    public Recommendation getRecommendation() {
        if (this.analysis.hasRating()) {
            return Recommendation.values()[this.analysis.getRating().getNumber()];
        }
        return Recommendation.NONE;
    }

    @Override
    public Recommendation getPreviousRecommendation() {
        if (this.analysis.hasPreviousRating()) {
            return Recommendation.values()[this.analysis.getPreviousRating().getNumber()];
        }
        return Recommendation.NONE;
    }

    @Override
    public BigDecimal getTarget() {
        if (this.analysis.hasTarget()) {
            return new BigDecimal(analysis.getTarget());
        }
        return null;
    }

    @Override
    public BigDecimal getPreviousTarget() {
        if (this.analysis.hasPreviousTarget()) {
            return new BigDecimal(analysis.getPreviousTarget());
        }
        return null;
    }


    @Override
    public String getTargetCurrency() {
        if (this.analysis.hasCurrency()) {
            return analysis.getCurrency();
        }
        return null;
    }

    @Override
    public String getTimeframe() {
        if (this.analysis.hasTimeframe()) {
            return analysis.getTimeframe();
        }
        return null;
    }

    @Override
    public String getCompanyName() {
        return this.analysis.getCompanyName();
    }

    public Protos.Analysis getRawAnalysis() {
        return this.analysis;
    }

    public String getProviderName() {
        return this.analysis.getProvider().name();
    }

    private boolean isWebSim() {
        return this.analysis.getProvider() == Protos.Analysis.Provider.WEBSIM;
    }

    private String getAnalysisText(final String paragraphSeparator) {
        if (this.analysis.getTextCount() == 0) {
            return null;
        }
        if (this.analysis.getTextCount() == 1) {
            return this.analysis.getText(0);
        }
        final StringBuilder sb = new StringBuilder(1000);
        for (int i = 0; i < this.analysis.getTextCount(); i++) {
            if (i > 0) {
                sb.append(paragraphSeparator);
            }
            sb.append(this.analysis.getText(i));
        }
        return sb.toString();
    }

    private String getWebSimAnalysisText() {
        if (this.analysis.getTextCount() == 1 && this.analysis.getImageRefCount() == 0) {
            return this.analysis.getText(0);
        }
        StringBuilder sb = new StringBuilder(this.analysis.getText(0));
        if (this.analysis.getTextCount() > 1) {
            sb.append("<p><p>").append(this.analysis.getText(1));
        }
        for (int i = 0; i < this.analysis.getImageRefCount(); i++) {
            sb.append("<p><p><img src=\"").append(this.analysis.getImageRef(i)).append("\">");
        }
        return sb.toString();
    }
}
