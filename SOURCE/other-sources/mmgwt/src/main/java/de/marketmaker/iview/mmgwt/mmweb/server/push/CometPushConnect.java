/*
 * CometPushConnect.java
 *
 * Created on 13.12.2016 09:34
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import de.marketmaker.itools.gwtcomet.comet.server.CometSession;

/**
 * @author mdick
 */
public interface CometPushConnect extends PushConnect {
    void connect(String sid, CometSession cometSession);
}
