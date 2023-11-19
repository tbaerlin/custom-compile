/*
 * BrokerLoginDisplay.java
 *
 * Created on 17.10.13 10:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * @author Markus Dick
 */
public interface BrokerLoginDisplay {
    void setPresenter(Presenter presenter);

    void setHeading(String title);

    void setUser(String user);
    String getUser();
    String getPassword();

    void setMessage(SafeHtml message);
    void setMessageVisible(boolean visible);

    void show();
    void hide();

    public interface Presenter {
        void setCallback(Callback callback);

        void onPassword();
        void onCancel();
    }

    public interface Callback {
        public void onPassword(String user, String password);
        public void onCancel();
    }
}
