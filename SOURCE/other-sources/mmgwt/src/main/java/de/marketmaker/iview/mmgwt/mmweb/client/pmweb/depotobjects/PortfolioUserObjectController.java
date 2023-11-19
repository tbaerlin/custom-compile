/*
 * PortfolioUserObjectController
 *
 * Created on 26.03.2015 11:51
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Portfolio;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.UserObjectDisplay.*;

/**
 * @author mdick
 */
public class PortfolioUserObjectController extends UserObjectController<Portfolio, PortfolioPortraitController,
        UserObjectDisplay.PortfolioUserObjectPresenter> implements PortfolioUserObjectPresenter {

    public PortfolioUserObjectController(PortfolioPortraitController controller, UserObjectDisplay<Portfolio, UserObjectDisplay.PortfolioUserObjectPresenter> view, boolean editAllowed) {
        super(controller, view, editAllowed);
    }

    @Override
    public void onPortfolioVersionSelected(String portfolioId, String validDate) {
        getController().onPortfolioVersionSelected(portfolioId, validDate);
    }

    @Override
    public void onCreatePortfolioVersion() {
        getController().onCreatePortfolioVersion();
    }

    @Override
    public void onClonePortfolioVersion() {
        getController().onClonePortfolioVersion();
    }

    @Override
    public void onDeletePortfolioVersion() {
        getController().onDeletePortfolioVersion();
    }
}
