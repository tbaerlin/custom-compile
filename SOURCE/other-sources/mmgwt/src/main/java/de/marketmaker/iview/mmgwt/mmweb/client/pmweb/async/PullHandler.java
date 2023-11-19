package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

/**
 * Author: umaurer
 * Created: 01.10.14
 */
public interface PullHandler {
    void startMonitoring();

    void startPull();

    void startAsFallback();

    void setMillis();

    void stop();

    boolean isActive();
}
