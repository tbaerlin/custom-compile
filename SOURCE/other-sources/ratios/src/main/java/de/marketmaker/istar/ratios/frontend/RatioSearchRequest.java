/*
 * RatioSearchRequest.java
 *
 * Created on 26.10.2005 12:11:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.Profile;

/**
 * Represents a ratio search request. Relevant fields are instrument type and parameters.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatioSearchRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 213125435437L;

    private final Profile profile;

    private final List<Locale> locales;

    /**
     * If this list is defined, the search can be restricted to the specified instruments.
     */
    private List<Long> instrumentIds = null;

    /**
     * the chosen or derived fnd-provider
     */
    private String selectedProvider;

    private Map<String, String> parameters;

    private InstrumentTypeEnum type;

    private InstrumentTypeEnum[] additionalTypes;

    private String visitorClassname = PagedResultVisitor.class.getName();

    private String dataRecordStrategyClassname = null;

    private int fieldidForResultCount;

    private String filterForResultCount;

    private List<Integer> metadataFieldids;

    private boolean withDetailedSymbol;

    public RatioSearchRequest(Profile profile) {
        this(profile, null);
    }

    public RatioSearchRequest(Profile profile, List<Locale> locales) {
        this.profile = profile;
        this.locales = locales;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", type=").append(this.type);
        if (this.additionalTypes != null) {
            sb.append(", +types=").append(Arrays.toString(this.additionalTypes));
        }
        sb.append(", visitor=").append(this.visitorClassname);
        sb.append(", params=").append(this.parameters);
        sb.append(", fieldIdForResultCount=").append(this.fieldidForResultCount);
        if (this.filterForResultCount != null) {
            sb.append(", filter=").append(this.filterForResultCount);
        }
        if (this.metadataFieldids != null) {
            sb.append(", metaFields=").append(this.metadataFieldids);
        }
        if (this.locales != null) {
            sb.append(", locales=").append(this.locales);
        }
    }

    public Profile getProfile() {
        return profile;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    public boolean isWithDetailedSymbol() {
        return withDetailedSymbol;
    }

    public void setWithDetailedSymbol(boolean withDetailedSymbol) {
        this.withDetailedSymbol = withDetailedSymbol;
    }

    public Map<String, String> getParameters() {
        return this.parameters != null ? this.parameters : Collections.<String, String>emptyMap();
    }

    public void addParameters(Map<String, String> params) {
        for (final Map.Entry<String, String> entry : params.entrySet()) {
            addParameter(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Adds one key and value pair as a search parameter. Supported parameters are:
     * <table border="1">
     * <tr><th>Name</th><th>Description</th><th>Example</th></tr>
     * <tr><td>sort</td><td>Sort field</td><td>sort1:D - specifies the first sort field, descending<br/>
     * sort2 - specifies the second sort field, ascending</td></tr>
     * <tr><td>i</td><td>Paging offset</td><td></td></tr>
     * <tr><td>n</td><td>Paging count</td><td></td></tr>
     * <tr><td>underlyingiid</td><td>Restrict underlying instrument id</td><td></td></tr>
     * <tr><td>vwdMarket</td><td>Restrict VWD market</td><td></td></tr>
     * <tr><td>group</td><td>Specify group by field</td><td></td></tr>
     * <tr><td>groupSortBy</td><td>Specify sort field within group</td><td></td></tr>
     * <tr><td>isin@wkn@name</td><td>?</td><td></td></tr>
     * <tr><td>?</td><td>?</td><td>?</td></tr>
     * </table>
     * @param key
     * @param value
     */
    public void addParameter(String key, String value) {
        if (this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.put(key, value);
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public InstrumentTypeEnum getType() {
        return type;
    }

    /**
     * Specifies the instrument type to be searched.
     * @param type an instrument type enum.
     */
    public void setType(InstrumentTypeEnum type) {
        this.type = type;
    }

    public InstrumentTypeEnum[] getAdditionalTypes() {
        return additionalTypes;
    }

    public void setAdditionalTypes(InstrumentTypeEnum[] additionalTypes) {
        this.additionalTypes = additionalTypes;
    }

    public List<Long> getInstrumentIds() {
        return instrumentIds;
    }

    /**
     * Restricts the search to specific instruments, improves search performance
     * @param instrumentIds a list of instrument ids.
     */
    public void setInstrumentIds(List<Long> instrumentIds) {
        this.instrumentIds = instrumentIds;
    }

    /**
     * An instance of this class will be used to collect search results; the class has to
     * provider a public no-args constructor. Default is {@link de.marketmaker.istar.ratios.frontend.PagedResultVisitor}
     * @param clazz defines object for collecting search results
     */
    public void setVisitorClass(Class<? extends GenericSearchEngineVisitor<?, ?>> clazz) {
        this.visitorClassname = clazz.getName();
    }

    public String getVisitorClassname() {
        return this.visitorClassname;
    }

    /**
     * An instance of this class will be used as a strategy implementation to select
     * DataRecords; the class has to provide a public no-args constructor.
     * If this value is not set, the {@link DefaultDataRecordStrategy} will be used.
     * @param clazz defines object to select data records
     */
    public void setDataRecordStrategyClass(Class<? extends DataRecordStrategy> clazz) {
        this.dataRecordStrategyClassname = clazz.getName();
    }

    public String getDataRecordStrategyClassname() {
        return this.dataRecordStrategyClassname;
    }

    public void setFieldidForResultCount(int fieldid) {
        this.fieldidForResultCount = fieldid;
    }

    public int getFieldidForResultCount() {
        return fieldidForResultCount;
    }

    public String getFilterForResultCount() {
        return filterForResultCount;
    }

    public void setFilterForResultCount(String filterForResultCount) {
        this.filterForResultCount = filterForResultCount;
    }

    public List<Integer> getMetadataFieldids() {
        return metadataFieldids;
    }

    public void setMetadataFieldids(List<Integer> metadataFieldids) {
        this.metadataFieldids = metadataFieldids;
    }

    public void setSelectedProvider(String selectedProvider) {
         this.selectedProvider = selectedProvider;
    }

    public String getSelectedProvider() {
        return selectedProvider;
    }

}
