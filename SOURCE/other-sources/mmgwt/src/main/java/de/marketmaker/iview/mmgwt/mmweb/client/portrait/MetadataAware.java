package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.dmxml.MSCQuoteMetadata;

/**
 * @author umaurer
 */
public interface MetadataAware {
    boolean isMetadataNeeded();
    void onMetadataAvailable(MSCQuoteMetadata metadata);
}
