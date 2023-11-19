/*
 * OrderValidationMessageDisplay.java
 *
 * Created on 18.01.13 11:28
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import de.marketmaker.iview.pmxml.ThreeValueBoolean;
import de.marketmaker.iview.pmxml.ValidationMessage;

import java.util.List;

/**
 * @author Markus Dick
 */
interface OrderValidationMessageDisplay {
    void setPresenter(Presenter presenter);

    void show();
    void hide();
    void setMessages(List<ValidationMessage> messages);

    void setOkButtonVisible(boolean visible);

    interface Presenter {
        void onOkButtonClicked();
        void onCancelButtonClicked();

        void onAnswer(int messageIndex, ThreeValueBoolean answer);
    }
}
