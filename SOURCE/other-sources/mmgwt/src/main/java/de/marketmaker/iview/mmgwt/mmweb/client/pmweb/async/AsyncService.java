package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import com.google.gwt.user.client.rpc.RemoteService;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmInvalidSessionException;
import de.marketmaker.iview.pmxml.EvalLayoutRequest;
import de.marketmaker.iview.pmxml.GetStateResponse;

/**
 * User: umaurer
 * Date: 19.12.13
 * Time: 11:51
 */
public interface AsyncService extends RemoteService {
    /**
     * @param vwdId .
     * @return session id
     */
    public String createSession(String vwdId);
    public void closeSession(String sessionId, boolean cancelHandles);

    public String createUuid();
    public AsyncHandleResult evalLayout(String sessionId, EvalLayoutRequest request) throws PmInvalidSessionException;
    public GetStateResponse getStateResponse(String sessionId, String handle, boolean registerForPush) throws PmInvalidSessionException, InvalidJobStateException;
    public void unregisterHandle(String handle);

    /**
     * This method does not do anything. It is used to enable GWT Serialization of AsyncData for WebSocket communication.
     * AsyncDataCoderGwt can then be used to serialize and deserialize AsyncData objects.
     */
    public AsyncData getAsyncData();
}
