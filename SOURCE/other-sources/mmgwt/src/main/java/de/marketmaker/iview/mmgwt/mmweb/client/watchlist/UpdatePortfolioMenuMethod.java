/*
 * UpdatePortfolioMenuMethod.java
 *
 * Created on 18.02.2016 14:27
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import de.marketmaker.iview.dmxml.PortfolioElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * Must be processed before the PlaceChangeEvent is handled by the MainController!
 * @author mdick
 */
public class UpdatePortfolioMenuMethod extends AbstractUpdateMenuMethod<UpdatePortfolioMenuMethod, PortfolioElement> {
    public UpdatePortfolioMenuMethod() {
        super("B_PS", "B_P", I18n.I.portfolioSample(), "B_P", "B_P", SessionData.INSTANCE::getPortfolios); // $NON-NLS$
    }

    @Override
    protected String getId(PortfolioElement element) {
        return element.getPortfolioid();
    }

    @Override
    protected String getName(PortfolioElement element) {
        return element.getName();
    }
}
