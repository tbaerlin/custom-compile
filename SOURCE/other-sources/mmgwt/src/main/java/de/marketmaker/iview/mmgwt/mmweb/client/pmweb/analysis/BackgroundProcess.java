/*
 * BackgroundProcess.java
 *
 * Created on 25.06.2015 08:06
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

/**
 * @author mdick
 */
public interface BackgroundProcess {
    void addHiddenNotification();
    void removeNotification();
    boolean isNotificationRemoved();
    boolean isCanceled();
    void cancel();
}
