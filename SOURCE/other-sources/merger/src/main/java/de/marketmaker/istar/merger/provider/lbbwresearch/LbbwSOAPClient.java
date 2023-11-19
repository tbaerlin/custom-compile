package de.marketmaker.istar.merger.provider.lbbwresearch;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.soap.SOAPException;

/**
 * Interface to SOAP Client for accessing LBBW Research data
 * @author mcoenen
 */
public interface LbbwSOAPClient {
    /**
     * Fetch all available documents for the provided date
     * @param publicationDateFrom Date to start looking for new documents
     * @param publicationDateTo Date to end looking for new documents (set to from date if documents for one day are required)
     * @return Mapping of document's objectId to the corresponding BasicDocument instance
     * @throws SOAPException If anything goes wrong fetching the document
     */
    Set<BasicDocument> callGetBasicDocuments(
            LocalDate publicationDateFrom,
            LocalDate publicationDateTo) throws SOAPException;

    /**
     * Fetch company information from the SOAP service
     * @param companyObjectId ObjectId of the company to fetch details for
     * @param enabledDetails List of extra details to fetch
     * @return CompanyInfo object if present in the response
     */
    Optional<CompanyInfo> callGetCompanyInfo(String companyObjectId,
            List<String> enabledDetails);

    /**
     * Fetch the document itself from the SOAP service
     * @param documentObjectId ObjectId of the document to fetch
     * @return Document as byte[] if present in the response
     */
    Optional<byte[]> callGetDocumentContents(String documentObjectId);
}
