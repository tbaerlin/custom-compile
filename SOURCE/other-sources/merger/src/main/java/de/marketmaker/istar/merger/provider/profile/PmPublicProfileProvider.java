/*
 * PmPublicProfileProvider.java
 *
 * Created on 15.07.11 11:11
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.security.GeneralSecurityException;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.domainimpl.profile.PmAboProfile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;

/**
 * MmauthProfileProvider that retrieves the profile from vwd-ent by mapping properties of the
 * kuka profile to obtain the login of a user known by vwd-ent.
 * @author oflege
 */
@ManagedResource
public class PmPublicProfileProvider extends PmAuthentcationProviderImpl implements
        ProfileProvider {

    private ProfileProvider delegate;

    public void setDelegate(ProfileProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public final ProfileResponse getProfile(ProfileRequest request) {
        final Key key;
        if ("pmpub:ByLogin".equals(request.getAuthenticationType())) {
            try {
                key = decrypt(request.getAuthentication());
            } catch (GeneralSecurityException e) {
                this.logger.warn("<getProfile> invalid mmauth key " + request.getAuthentication(), e);
                return ProfileResponse.invalid();
            }
        }
        else {
            key = new Key(new String[]{request.getAuthentication(), null, null});
        }

        final KukaCustomer customer = getCustomer(key.login);

        if (customer == null) { // anonymous wiso boerse profile
            return new ProfileResponse(new PmAboProfile(key.login));
        }

        if (request.getApplicationId() == null) { // mmauth only login
            return new ProfileResponse(new PmAboProfile(customer.getKennung(), customer.getAbos(), ProfileFactory.valueOf(false)));
        }

        final ProfileResponse response = createProfile(request);
        if (!response.isValid()) {
            return response;
        }

        return new ProfileResponse(new PmAboProfile(customer.getKennung(), customer.getAbos(),
                response.getProfile()));
    }


    protected ProfileResponse createProfile(ProfileRequest request) {
        final ProfileRequest adapted
                = new ProfileRequest("vwd-ent:ByLogin", "PM-ONLINE-DERIVATE-TOOL");
        adapted.setApplicationId(request.getApplicationId());
        adapted.setClientId(request.getClientId());
        adapted.setUseCache(false);

        return this.delegate.getProfile(adapted);
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "auth", description = "auth like 8592VFmqz5....")
    })
    public String getPmProfile(String auth) {
        ProfileRequest request = new ProfileRequest("pmpub:ByLogin", auth);
        return String.valueOf(getProfile(request).getProfile());
    }

}

