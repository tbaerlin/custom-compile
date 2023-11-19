package de.marketmaker.istar.merger.web.easytrade.moleculestatistics;

/**
 * This class holds the aggregation values when aggregating request durations.
 */
class DurationAggregate {
    private Integer totalDuration;
    private Integer blockDuration;
    private Integer requestCount;
    private Integer blockCount;

    public DurationAggregate() {
        totalDuration = 0;
        blockDuration = 0;
        requestCount = 0;
        blockCount = 0;
    }

    public Integer getTotalDuration() {
        return totalDuration;
    }

    public void addTotalDuration(Integer d) {
        totalDuration = +d;
    }

    public Integer getBlockDuration() {
        return blockDuration;
    }

    public void addBlockDuration(Integer d) {
        blockDuration += d;
    }

    public Integer getRequestCount() {
        return requestCount;
    }

    public void increaseRequestCount() {
        this.requestCount++;
    }

    public void setRequestCount(Integer count) {
        this.requestCount = count;
    }

    public Integer getBlockCount() {
        return blockCount;
    }

    public void increaseBlockCount() {
        this.blockCount++;
    }

    public void setBlockCount(Integer count) {
        this.blockCount = count;
    }
}
