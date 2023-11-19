package de.marketmaker.istar.merger.web.easytrade.moleculestatistics;

import java.util.Objects;

public class CustomerBlockAggregationKey {

    private final String zone;
    private final String authentication;
    private final String authenticationType;
    private final String block;

    public CustomerBlockAggregationKey(String zone, String authentication, String authenticationType, String block) {
        this.zone = zone != null ? zone : "";
        this.authentication = authentication != null ? authentication : "";
        this.authenticationType = authenticationType != null ? authenticationType : "";
        this.block = block != null ? block : "";
    }

    public String getZone() {
        return zone;
    }

    public String getAuthentication() {
        return authentication;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public String getBlock() {
        return block;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerBlockAggregationKey that = (CustomerBlockAggregationKey) o;
        return Objects.equals(zone, that.zone) &&
                Objects.equals(authentication, that.authentication) &&
                Objects.equals(authenticationType, that.authenticationType) &&
                Objects.equals(block, that.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zone, authentication, authenticationType, block);
    }
}
