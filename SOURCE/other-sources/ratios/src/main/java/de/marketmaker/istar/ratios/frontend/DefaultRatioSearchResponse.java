/*
 * RatioSearchResponse.java
 *
 * Created on 27.10.2005 17:21:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.ratios.Partition;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DefaultRatioSearchResponse extends AbstractIstarResponse implements
        RatioSearchResponse {
    static final long serialVersionUID = -34234213125L;

    private List<RatioDataResult> elements;

    private int numTotal;

    private int offset;

    private int length;

    private Map<Object, Integer> resultGroupCount;

    private Map<Integer, Map<String, Integer>> metadata;

    private List<Partition> partition;

    public DefaultRatioSearchResponse() {
    }

    protected void appendToString(StringBuilder sb) {
        sb.append(", numTotal=").append(this.numTotal)
                .append(", offset=").append(this.offset)
                .append(", length=").append(this.length);
    }

    public void setElements(List<RatioDataResult> elements) {
        this.elements = elements;
    }

    public List<RatioDataResult> getElements() {
        return elements;
    }

    public int getNumTotal() {
        return numTotal;
    }

    public void setNumTotal(int numTotal) {
        this.numTotal = numTotal;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Set<Long> getInstrumentIds() {
        return elements.stream()
                .map(RatioDataResult::getInstrumentid)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public void setResultGroupCount(Map<Object, Integer> counts) {
        this.resultGroupCount = counts;
    }

    public Map<Object, Integer> getResultGroupCount() {
        return resultGroupCount;
    }

    public void setMetadata(Map<Integer, Map<String, Integer>> metadata) {
        this.metadata = metadata;
    }

    public Map<Integer, Map<String, Integer>> getMetadata() {
        return metadata;
    }

    public List<Partition> getPartition() {
        return partition;
    }

    public void setPartition(List<Partition> partition) {
        this.partition = partition;
    }
}