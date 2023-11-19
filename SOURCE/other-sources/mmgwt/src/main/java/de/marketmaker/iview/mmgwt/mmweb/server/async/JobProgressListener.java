package de.marketmaker.iview.mmgwt.mmweb.server.async;

import de.marketmaker.iview.pmxml.SEMMJobProgress;

/**
 * User: umaurer
 * Date: 18.10.13
 * Time: 11:53
 */
public interface JobProgressListener {
    void onProgress(SEMMJobProgress event);
}
