/*
 * CommonSymbolExporter.java
 *
 * Created on 30.01.2015 08:55
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.exporttools;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mwilke
 */
public class CommonSymbolExporter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ProfileProvider profileProvider;

    protected final InstrumentDirDao instrumentDirDao;

    public CommonSymbolExporter(ProfileProvider profileProvider, InstrumentDirDao instrumentDirDao) {
        this.profileProvider = profileProvider;
        this.instrumentDirDao = instrumentDirDao;
    }

    public Profile getProfile(String vwdId) {
        TimeTaker tt = new TimeTaker();
        this.logger.info("<getProfile> getting profile for " + vwdId);

        final ProfileRequest request = new ProfileRequest("vwd-ent:ByVwdId", vwdId);
        ProfileResponse response = this.profileProvider.getProfile(request);
        // since first request sometimes fails due to slow vwd-ent, just retry
        if (!response.isValid()) {
            this.logger.warn("<getProfile> retry since response was invalid after " + tt);
            response = this.profileProvider.getProfile(request);
        }
        this.logger.info("<getProfile> got profile after " + tt);
        return response.getProfile();
    }
}
