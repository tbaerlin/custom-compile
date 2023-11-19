/*
 * WebsocketPushConnect.java
 *
 * Created on 13.12.2016 09:34
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import javax.websocket.Session;

/**
 * @author mdick
 */
public interface WebsocketPushConnect extends PushConnect {
    void connect(String sid, Session session);
}
