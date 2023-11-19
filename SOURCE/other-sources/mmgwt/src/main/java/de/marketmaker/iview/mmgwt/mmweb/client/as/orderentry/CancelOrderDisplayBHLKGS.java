/*
 * CancelOrderDisplayBHLKGS.java
 *
 * Created on 11.11.13 13:51
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import de.marketmaker.iview.pmxml.OrderDataTypeBHL;
import de.marketmaker.iview.pmxml.TextWithKey;

import java.util.List;

/**
 * @author Markus Dick
 */
public interface CancelOrderDisplayBHLKGS {
    void setPresenter(Presenter presenter);

    void setOrderNumber(String orderNumber);

    void setCancellationReasons(List<TextWithKey> deleteOrderReasons);
    void setCancellationReasonsSelectedItem(TextWithKey selectedItem);
    TextWithKey getCancellationReasonsSelectedItem();

    void setPrintCancellationConfirmation(boolean print);
    boolean isPrintCancellationConfirmation();

    void setCannedTextForOrderConfirmations(List<TextWithKey> cannedTexts);
    void setCannedTextForOrderConfirmationsSelectedItem(TextWithKey selectedItem);
    TextWithKey getCannedTextForOrderConfirmationsSelectedItem();

    void setInternalText1(String internalText1);
    String getInternalText1();

    void setInternalText2(String internalText2);
    String getInternalText2();

    void setLoadingIndicatorVisible(boolean visible);
    void setProceedButtonEnabled(boolean enabled);
    void setCancelButtonEnabled(boolean enabled);

    void show();
    void hide();

    public interface Presenter {
        void show(OrderDataTypeBHL order);
        void onProceedClicked();
        void onCancelClicked();
    }
}
