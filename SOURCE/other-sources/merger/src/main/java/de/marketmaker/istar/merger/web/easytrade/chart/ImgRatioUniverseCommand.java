/*
 * FndInvestmentsCommand.java
 *
 * Created on 29.08.2006 14:06:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.web.easytrade.ProviderSelectionCommand;

import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgRatioUniverseCommand extends BaseImgCommand implements ProviderSelectionCommand {
    private String query;
    private String field;
    private int numElements = Integer.MIN_VALUE;
    private int minCount=Integer.MIN_VALUE;
    private InstrumentTypeEnum type;
    private String providerPreference;

    @RestrictedSet("VWD,SMF,SEDEX")
    public String getProviderPreference() {
        return providerPreference;
    }

    public void setProviderPreference(String providerPreference) {
        this.providerPreference = providerPreference;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @NotNull
    public InstrumentTypeEnum getType() {
        return type;
    }

    public void setType(InstrumentTypeEnum type) {
        this.type = type;
    }

    public int getNumElements() {
        return numElements;
    }

    public void setNumElements(int numElements) {
        this.numElements = numElements;
    }

    public int getMinCount() {
        return minCount;
    }

    public void setMinCount(int minCount) {
        this.minCount = minCount;
    }

    @NotNull
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getParameterString() {
        // only values expected to be set by user appear here
        final StringBuilder sb = new StringBuilder(200);
        appendParameters(sb);
        sb.append("&type=").append(getType());
        if (getMinCount() != Integer.MIN_VALUE) {
            sb.append("&minCount=").append(getMinCount());
        }
        else if (getNumElements() != Integer.MIN_VALUE) {
            sb.append("&numElements=").append(getNumElements());
        }
        if (StringUtils.hasText(this.providerPreference)) {
            sb.append("&providerPreference=").append(this.providerPreference);
        }
        if (StringUtils.hasText(this.query)) {
            try {
                sb.append("&query=").append(URLEncoder.encode(this.query, "ISO-8859-1"));
            }
            catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return sb.append("&field=").append(getField()).toString();
    }
}