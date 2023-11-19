package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.web.easytrade.ListCommandWithOptionalPaging;
import de.marketmaker.istar.ratios.frontend.DataRecordStrategy;

public class BestToolCommand extends ListCommandWithOptionalPaging {
    private InstrumentTypeEnum type;

    private InstrumentTypeEnum[] additionalType;

    private String query;

    private String primaryField;

    private String secondaryField;

    private String primaryFieldOperator;

    private String secondaryFieldOperator;

    private int numResults = 1;

    private String sortField;

    private String maxSortFieldValue;

    private boolean ascending;

    private DataRecordStrategy.Type dataRecordStrategy;

    @NotNull
    public InstrumentTypeEnum getType() {
        return type;
    }

    public void setType(InstrumentTypeEnum type) {
        this.type = type;
    }

    /**
     * @return additional instrument types which should also be searched.
     */
    public InstrumentTypeEnum[] getAdditionalType() {
        return additionalType;
    }

    public void setAdditionalType(InstrumentTypeEnum[] additionalType) {
        this.additionalType = additionalType;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @NotNull
    public String getPrimaryField() {
        return primaryField;
    }

    public void setPrimaryField(String primaryField) {
        this.primaryField = primaryField;
    }

    public String getSecondaryField() {
        return secondaryField;
    }

    public void setSecondaryField(String secondaryField) {
        this.secondaryField = secondaryField;
    }

    @NotNull
    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getMaxSortFieldValue() {
        return maxSortFieldValue;
    }

    public void setMaxSortFieldValue(String maxSortFieldValue) {
        this.maxSortFieldValue = maxSortFieldValue;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public String getPrimaryFieldOperator() {
        return primaryFieldOperator;
    }

    public void setPrimaryFieldOperator(String primaryFieldOperator) {
        this.primaryFieldOperator = primaryFieldOperator;
    }

    public String getSecondaryFieldOperator() {
        return secondaryFieldOperator;
    }

    public void setSecondaryFieldOperator(String secondaryFieldOperator) {
        this.secondaryFieldOperator = secondaryFieldOperator;
    }

    @Range(min = 1, max = 10)
    public int getNumResults() {
        return numResults;
    }

    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }

    public DataRecordStrategy.Type getDataRecordStrategy() {
        return dataRecordStrategy;
    }

    public void setDataRecordStrategy(DataRecordStrategy.Type dataRecordStrategy) {
        this.dataRecordStrategy = dataRecordStrategy;
    }
}