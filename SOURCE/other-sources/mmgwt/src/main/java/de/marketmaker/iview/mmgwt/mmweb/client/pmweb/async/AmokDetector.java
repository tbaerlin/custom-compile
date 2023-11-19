package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import de.marketmaker.itools.gwtutil.client.util.Firebug;

/**
 * Created on 15.05.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 *         This hack prevents Comet from running amok (connect/disconnect with a high frequence).
 *         This situation can be prompt by terminating the current cometresponse object,
 *         even though it doesn't occur in every case.
 *         The amok-like situation (sometimes) comes up when using a firefox (tested with 29.0.1 on Mac OS X 10.9.2).
 *         It was not able to prompt the amok situation on IE8 (WinXP) and IE9 (Win7)!
 *         <p/>
 *         This (or a likely one) Comet-Bug is known but
 *         there's no fix available or even known. See
 *         https://groups.google.com/forum/#!topic/gwt-comet/7qObfjJRxs8
 *         and
 *         https://code.google.com/p/gwt-comet/issues/detail?id=5
 */

public class AmokDetector {
    private final AsyncCometHandler manager;

    protected static int MS_THRESHOLD = 100; //ms
    protected static int COUNT_THRESHOLD = 3;

    private long lastConnectMillis = 0;
    private int connectionFloodCounter = 0;

    public AmokDetector(AsyncCometHandler manager) {
        this.manager = manager;
    }

    public void reportConnected() {
        final long currentMillis = System.currentTimeMillis();
        if (currentMillis - this.lastConnectMillis < MS_THRESHOLD) {
            this.connectionFloodCounter++;
            Firebug.debug("AmokDetector <reportConnected> counter: " + this.connectionFloodCounter);
            evaluate();
        }
        else {
            resetCounter();
        }
        this.lastConnectMillis = System.currentTimeMillis();
    }

    private void evaluate() {
        if (this.connectionFloodCounter >= COUNT_THRESHOLD) {
            Firebug.debug("AmokDetector <evaluate> (connectionFloodCounter >= COUNT_THRESHOLD) stopping client");
            resetCounter();
            this.manager.amokDetected();
        }
    }

    private void resetCounter() {
        Firebug.debug("AmokDetector <reportConnected> resetting counter");
        this.connectionFloodCounter = 0;
    }

}