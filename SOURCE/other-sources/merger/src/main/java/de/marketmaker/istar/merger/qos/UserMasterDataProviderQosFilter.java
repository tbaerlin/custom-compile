package de.marketmaker.istar.merger.qos;


import de.marketmaker.istar.domainimpl.profile.UserMasterDataProvider;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataResponse;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataRequest;

public class UserMasterDataProviderQosFilter extends
        CachingQosFilter<UserMasterDataProvider, UserMasterDataResponse> implements UserMasterDataProvider {

    private UserMasterDataRequest request;

    public void setVwdId(String id) {
        this.request = UserMasterDataRequest.forVwdId(id);
    }

    protected boolean tryService() throws Exception {
        return this.delegate.getUserMasterData(this.request).isValid();
    }

    public UserMasterDataResponse getUserMasterData(UserMasterDataRequest request) {
        if (isEnabled()) {
            final UserMasterDataResponse result = this.delegate.getUserMasterData(request);
            if (result.isValid()) {
                store(getKey(request), result);
            }
            return result;
        }
        else {
            final UserMasterDataResponse result = retrieve(getKey(request));
            if (result != null) {
                return result;
            }
            return UserMasterDataResponse.createInvalid();
        }
    }

    private String getKey(UserMasterDataRequest request) {
        return request.getId() + ":" + request.getType();
    }
}