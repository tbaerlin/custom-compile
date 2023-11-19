/*
 * PushModule.java
 *
 * Created on 16.02.2010 16:01:06
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.push;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Command;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * @author oflege
 * @see <a href="http://code.google.com/webtoolkit/doc/latest/DevGuideCodeSplitting.html">Code Splitting</a>
 */
public class PushModule {

    public static Command createLoader(final SessionData sessionData) {
        return new Command() {
            public void execute() {
                if (!sessionData.isWithPush()) {
                    finish();
                    return;
                }
                AbstractMainController.INSTANCE.updateProgress(I18n.I.loadPushModule());
                createAsync(new Client() {
                    public void onSuccess(PushModule instance) {
                        finish();
                    }

                    public void onUnavailable() {
                        finish();
                    }
                }, sessionData);
            }

            private void finish() {
                AbstractMainController.INSTANCE.runInitSequence();
            }
        };
    }


    // A callback for using the module instance once it's loaded

    public interface Client {
        void onSuccess(PushModule instance);

        void onUnavailable();
    }

    private static PushModule instance = null;

    /**
     * Access the module's instance.  The callback
     * runs asynchronously, once the necessary
     * code has downloaded.
     */
    public static void createAsync(final Client client, final SessionData sessionData) {
        GWT.runAsync(new RunAsyncCallback() {
            public void onFailure(Throwable err) {
                Firebug.error("load push failed", err); // $NON-NLS-0$
                client.onUnavailable();
            }

            public void onSuccess() {
                if (instance == null) {
                    instance = new PushModule(sessionData);
                }
                client.onSuccess(instance);
            }
        });
    }

    private PushModule(SessionData sessionData) {
        new PushSupport(sessionData);
    }
}
