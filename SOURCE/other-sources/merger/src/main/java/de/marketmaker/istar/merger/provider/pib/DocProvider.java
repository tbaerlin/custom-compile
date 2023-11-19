/*
 * PibProvider.java
 *
 * Created on 28.03.11 13:59
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pib;

import java.util.Map;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * For accessing PIB (ProduktInformationsBlatt) data.
 *
 * @author oflege
 */
@AmqpAddress(queue = "istar.pib")
public interface DocProvider {
    /**
     * @param request specifies the pdf to be generated
     * @return true iff calling {@link #createDoc} with the same isin/clientId
     * in the request can be expected to return a pdf document.
     */
    boolean isPibAvailable(DocRequest request) throws DocProviderException;

    /**
     * @param request specifies the instrument to look fro
     * @return response containing basic information for an available instrument
     */
    PibAvailabilityResponse checkInstrumentAvailability(
            DocRequest request) throws DocProviderException;

    /**
     * @param request specifies the pdf to be generated
     * @return a response that contains the pdf among other information, or possibly an error code
     */
    DocResponse createDoc(DocRequest request) throws DocProviderException;

    /**
     * @param request specifies the pdf id of the pdf document to be retrieved
     * @return a response that contains the pdf
     */
    DocResponse getDoc(DocRequest request) throws DocProviderException;

    /**
     * @param isin reference to document class
     * @return map of accesses by ip adress, map of accesses is a map of document IDs (string) by dates (long, millis)
     */
    Map<String, Map<Long, String>> getAccessStat(String isin) throws DocProviderException;

    DocQueryResponse getGenerationDetails(DocQueryRequest req) throws DocProviderException;

    // section text related methods
    DocDataResponse adminDocData(DocDataRequest req) throws DocProviderException;

    DocDataResponse getDocData(DocDataRequest req) throws DocProviderException;

    // vendor PIB related methods
    VendorDocResponse createVendorDoc(VendorDocRequest req) throws DocProviderException;

    boolean updateVendorDoc(VendorDocRequest req) throws DocProviderException;

    boolean deleteVendorDoc(VendorDocRequest req) throws DocProviderException;

    VendorDocQueryResp getVendorDocs(VendorDocQueryReq req) throws DocProviderException;

    DocResponse getVendorDoc(VendorDocRequest req) throws DocProviderException;
}
