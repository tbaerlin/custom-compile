/*
 * RatiosProvider.java
 *
 * Created on 20.07.2006 16:40:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.Collection;
import java.util.Map;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.MatrixMetadataRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.RatioSearchMetaResponse;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;
import de.marketmaker.istar.ratios.opra.OpraRatioSearchResponse;

/**
 * Provides ratio related query methods, supports OPRA (Options Price Reporting Authority).
 * <p>
 * Under a specific ratio data record field, the value would be differentiated according to different
 * providers. This is done with a map between {@link de.marketmaker.istar.domain.data.RatioDataRecord.Field}
 * and {@link de.marketmaker.istar.ratios.RatioFieldDescription.Field}. The actual value is decided by
 * {@link de.marketmaker.istar.ratios.RatioFieldDescription.Field}.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface RatiosProvider {

    /**
     * Queries for a given instrument type the static meta data X, identified by a ratio field id
     * (selectFieldId). The return value is grouped according to meta data Y, identified by another
     * ratio field id (groupByFieldId).
     *
     * @param type an InstrumentTypeEnum.
     * @param groupByFieldId an id of a field defined in {@link de.marketmaker.istar.ratios.RatioFieldDescription.Field}.
     * @param selectFieldId an id of a field defined in {@link de.marketmaker.istar.ratios.RatioFieldDescription.Field}.
     * @param withDetailedSymbol set to true if detailed rating symbols should be seen in response.
     * @return a map keyed by meta data Y, valued by a map of meta data X and X' id. Empty map if no
     *         meta data X found for the given instrument type.
     */
    Map<String, Map<String, Integer>> getMetaData(InstrumentTypeEnum type, int groupByFieldId,
            int selectFieldId, boolean withDetailedSymbol);

    /**
     * @return Ratio meta response related with OPRA.
     */
    RatioSearchMetaResponse getOpraMetaData();

    /**
     * Convenience method that calls {@link #getRatioDatas(java.util.Collection, java.util.Map)} with a single quote
     */
    RatioDataRecord getRatioData(Quote quote,
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields);

    /**
     * Queries ratio data record for the given quotes. Differentiated values under specific ratio data
     * record fields are decided by the given fields map.
     *
     * @param quotes for which ratio data is requested.
     * @param fields a map keyed by {@link de.marketmaker.istar.domain.data.RatioDataRecord.Field} and
     * valued by {@link de.marketmaker.istar.ratios.RatioFieldDescription.Field}. See also:
     * {@link de.marketmaker.istar.merger.web.easytrade.block.AbstractFindersuchergebnis#getFields(de.marketmaker.istar.domain.instrument.InstrumentTypeEnum, de.marketmaker.istar.domain.profile.PermissionType...)}
     * @return A Map that contains an entry for each given quote, key is the quote's id, value is
     * the RatioDataRecord (or
     *         {@link de.marketmaker.istar.domain.data.RatioDataRecord#NULL}, if no corresponding ratio data
     *         record found, or the search cannot be performed).
     * @see de.marketmaker.istar.domain.data.RatioDataRecord
     * @see de.marketmaker.istar.domain.data.RatioDataRecord.Field
     * @see de.marketmaker.istar.ratios.RatioFieldDescription.Field
     */
    Map<Long, RatioDataRecord> getRatioDatas(Collection<Quote> quotes,
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields);

    /**
     * Queries OPRA items according to the given ratio search request. Please refer to
     * {@link de.marketmaker.istar.ratios.frontend.RatioSearchRequest} for information how to build
     * a valid request.
     *
     * @param request a ratio search request
     * @return an OPRA ratio search response.
     */
    OpraRatioSearchResponse getOpraItems(RatioSearchRequest request);

    MatrixMetadataRatioSearchResponse getOpraMatrix(RatioSearchRequest request);

    /**
     * Queries ratio data according to the given ratio search request. Please refer to
     * {@link de.marketmaker.istar.ratios.frontend.RatioSearchRequest} for information how to build
     * a valid request.
     *
     * @param request a ratio search request.
     * @return a ratio search response.
     */
    RatioSearchResponse search(RatioSearchRequest request);
}
