/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CdsDataOrdsRowset {
    @JsonProperty("ADF_387")
    private String adf387;

    @JsonProperty("ADF_790")
    private String adf790;

    @JsonProperty("ADF_49")
    private String adf49;

    public String getAdf387() {
        return adf387;
    }

    public String getAdf790() {
        return adf790;
    }

    public String getAdf49() {
        return adf49;
    }
}
