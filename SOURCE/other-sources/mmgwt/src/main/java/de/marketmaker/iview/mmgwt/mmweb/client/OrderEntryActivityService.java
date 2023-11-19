package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.rpc.RemoteService;
import de.marketmaker.iview.pmxml.CheckAndSetBackendCredentialsRequest;
import de.marketmaker.iview.pmxml.CheckAndSetBackendCredentialsResponse;

public interface OrderEntryActivityService extends RemoteService {
    CheckAndSetBackendCredentialsResponse login(CheckAndSetBackendCredentialsRequest request) throws OrderEntryServiceException;
}
