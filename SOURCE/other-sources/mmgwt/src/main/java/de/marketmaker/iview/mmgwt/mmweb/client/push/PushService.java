/*
 * PushService.java
 *
 * Created on 10.02.2010 11:35:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.push;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author oflege
 */
public interface PushService extends RemoteService {
    PushSessionResponse createSession(String vwdId, String appId, boolean websocket);

    PushChangeResponse modifySession(PushChangeRequest request);

    void closeSession(String id);

    /**
     * This method does not do anything. It is used to enable GWT Serialization of PushData for WebSocket communication.
     * PushDataCoderGwt can then be used to serialize and deserialize PushData objects.
     */
    PushData getPushData();
}
