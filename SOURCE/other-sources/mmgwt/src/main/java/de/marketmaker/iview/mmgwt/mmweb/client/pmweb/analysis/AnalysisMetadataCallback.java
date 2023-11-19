package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

import de.marketmaker.iview.pmxml.LayoutDesc;

/**
 * @author umaurer
 */
public interface AnalysisMetadataCallback {
    void onMetadataAvailable(LayoutDesc layoutDesc, String jsonConfig);
}
