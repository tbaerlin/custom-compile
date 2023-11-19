/*
 * OrderFromSecurityView.java
 *
 * Created on 19.02.13 09:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderFromSecurityPresenter.*;

/**
 * @author Markus Dick
 */
public class OrderFromSecurityView extends AbstractOrderView<DisplayAbstract.PresenterAbstract> implements OrderFromSecurityDisplay {
    @Override
    public void setInvestorNoEnabled(boolean enabled) {
        this.investorNumberField.setEnabled(enabled);
    }

    @Override
    public void setDepotNoEnabled(boolean enabled) {
        this.depotNumberField.setEnabled(enabled);
    }

    @Override
    public void setAccountNoEnabled(boolean enabled) {
        this.accountNumberField.setEnabled(enabled);
    }

    @Override
    public void setAccountBalanceEnabled(boolean enabled) {
        this.accountBalanceField.setEnabled(enabled);
    }

    @Override
    public void setPriceFieldEnabled(boolean enabled) {
        this.priceField.setEnabled(enabled);
    }

    @Override
    public void setPriceDateFieldEnabled(boolean enabled) {
        this.priceDateField.setEnabled(enabled);
    }

    @Override
    public void setSelectSymbolFromSearchEnabled(boolean enabled) {
        this.selectInstrumentButton.setEnabled(enabled);
    }

    @Override
    public void setPriceDateLabelVisible(boolean visible) {
        this.priceDateLabel.setVisible(visible);
    }

    @Override
    public void setPriceDateFieldVisible(boolean visible) {
        this.priceDateField.setVisible(visible);
    }

    @Override
    public void setPriceLabelVisible(boolean visible) {
        this.priceLabel.setVisible(visible);
    }

    @Override
    public void setPriceFieldVisible(boolean visible) {
        this.priceField.setVisible(visible);
    }

    @Override
    public void setAccountChoiceEnabled(boolean enabled) {
        this.accountChoice.setEnabled(enabled);
    }
}
