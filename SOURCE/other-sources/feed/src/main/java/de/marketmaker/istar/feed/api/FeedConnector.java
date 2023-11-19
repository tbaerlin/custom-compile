/*
 * FeedConnector.java
 *
 * Created on 14.12.2005 09:46:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import dev.infrontfinance.dm.proto.DmChicago.SnapFieldsReq;
import dev.infrontfinance.dm.proto.DmChicago.SnapFieldsResp;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.chicago3.intraday", dedicatedConsumer = true)
public interface FeedConnector extends BaseFeedConnector {
    /**
     * A symbol sort request contains a list of list of symbols. Each list of symbols is sorted based
     * on the instruments identified by those symbols. The order is determined by a sort type given
     * in this request, whereby only the first symbol in the sorted symbol list is returned in the
     * response.
     *
     * <p>
     * Null or empty list of symbols in the request results in null element at the corresponding
     * list position in the response.
     * @param request a symbol sort request
     * @return a symbol sort response
     */
    SymbolSortResponse getSortedSymbols(SymbolSortRequest request);

    /**
     * Requests type information for a set of vwdcodes.
     * Asking a chicago instance is the fastest way to obtain this information as any data
     * is available in memory; the type would also be available from an istar-instrument
     * instance, but that would add a lucene search which would slow things down.
     * <p>
     * Necessary because pages contain only vwdcodes, but we need types to be able to
     * determine the price quality in the frontend to request rt/nt/eod data accordingly.
     *
     * </p>
     */
    TypedVendorkeysResponse getTypesForVwdcodes(TypedVendorkeysRequest request);

    /**
     * @deprecated use {@link PageFeedConnector}
     */
    PageResponse getPage(PageRequest request);

    /**
     * Request a list of vendorkeys for the specification in request object.
     * @param request specification of vendorkeys to return
     * @return list of vendorkeys
     */
    VendorkeyListResponse getVendorkeys(VendorkeyListRequest request);

    /**
     * Returns snap field values encapsulated in a {@link SnapFieldsResp} for the given
     * {@link SnapFieldsReq}.
     *
     * @param req a {@link SnapFieldsReq}
     * @return a {@link SnapFieldsResp}
     */
    SnapFieldsResp getSnapFields(SnapFieldsReq req);
}
