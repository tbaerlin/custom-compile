package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.ArchiveData;

/**
 * Created on 14.04.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public interface JobDoneCommand {
    void onResult(ArchiveData archiveData);
}
