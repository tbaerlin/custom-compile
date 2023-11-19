package de.marketmaker.istar.merger.provider.profile;

import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;

/**
 * Creates an "empty" fake profile e.g. to enable pmxml-only operations
 *
 * @author Michael Lösch
 */

public class EmptyProfileProvider implements ProfileProvider {

    @Override
    public ProfileResponse getProfile(final ProfileRequest request) {
        return new ProfileResponse(ProfileFactory.valueOf(false));
    }
}