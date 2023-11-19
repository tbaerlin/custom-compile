/*
 * DisplayHA.java
 *
 * Created on 29.10.13 13:33
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.HasStringValueHost;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidatorGroup;

/**
 * @author Markus Dick
 */
public interface DisplayHA<P extends DisplayHA.PresenterHA> extends DisplayAbstract<P> {
    public static enum ValidUntilChoice { GOOD_FOR_THE_DAY, DATE, ULTIMO }
    public static enum ShowStopChoice { STOP_LOSS, STOP_BUY }

    String getAmount();
    void setAmount(String value);
    HasStringValueHost getAmountFieldValidator();
    boolean isLimitChecked();

    boolean isMouseOverLimitCheck();
    void setLimitChecked(boolean checked);
    void setLimitPseudoEnabled(boolean pseudoEnabled);
    String getLimit();
    void setLimit(String limit);
    HasStringValueHost getLimitFieldValidator();

    boolean isStopBuyMarketChecked();
    void setStopBuyMarketChecked(boolean checked);

    boolean isStopLossMarketChecked();
    void setStopLossMarketChecked(boolean checked);

    void setShowStopChoice(ShowStopChoice showStopChoice);

    ValidUntilChoice getValidUntilChoice();
    void setValidUntilChoice(ValidUntilChoice choice);
    void setValidUntilChoiceDate(MmJsDate mmJsDate);
    MmJsDate getValidUntilChoiceDate();
    String setValidUntilChoiceEnabled(ValidUntilChoice... enable);

    void setExpectedMarketValue(String approximatedPrice);

    ValidatorGroup getValidatorGroup();

    public interface PresenterHA extends DisplayAbstract.PresenterAbstract {
        void onValidUntilRadioGroupValueChanged(ValueChangeEvent<Boolean> booleanValueChangeEvent);
        void onLimitCheckValueChange(ValueChangeEvent<Boolean> event);
        void onLimitBlur(BlurEvent event);
        void onLimitFocus(FocusEvent event);
        void onAmountKeyUp(KeyUpEvent event);
        void onAmountValueChange(ValueChangeEvent<String> event);
        void onValidDateClick(ClickEvent event);
        void onValidationEventJoint(ValidationEvent event);
    }
}
