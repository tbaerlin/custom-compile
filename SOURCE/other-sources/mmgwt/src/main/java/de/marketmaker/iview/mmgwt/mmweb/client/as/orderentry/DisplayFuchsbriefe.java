/*
 * DisplayFuchsbriefe.java
 *
 * Created on 28.10.13 13:02
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationEvent;

/**
 * @author Markus Dick
 */
public interface DisplayFuchsbriefe<P extends DisplayFuchsbriefe.PresenterFuchsbriefe> extends DisplayAbstract<P> {
    void setQuantity(String value);
    void setQuantityRaw(String value);

    String getQuantity();
    void setExpectedMarketValue(String value);
    boolean isValid();

    public interface PresenterFuchsbriefe extends PresenterAbstract {
        void onQuantityValueChanged(ValueChangeEvent<String> event);
        void onQuantityFieldKeyUp(KeyUpEvent event);
        void onValidation(ValidationEvent event);
    }
}
