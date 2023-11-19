/*
 * UserObjectDisplay.java
 *
 * Created on 12.03.13 10:34
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.user.client.ui.IsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Portfolio;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IsPrintable;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Markus Dick
 */
@NonNLS
public interface UserObjectDisplay<U, P extends UserObjectDisplay.UserObjectPresenter<U>> extends IsWidget, IsPrintable {
    String VIEW_STYLE = "as-uod";

    void setPresenter(P presenter);

    void updateView(U userObject);

    void setEditButtonVisible(boolean visible);

    void ensureVisible(String itemToken);

    interface UserObjectPresenter<U> {
        void onEditButtonClicked();
    }

    interface PortfolioUserObjectPresenter extends UserObjectPresenter<Portfolio> {
        void onPortfolioVersionSelected(String portfolioId, String validDate);
        void onCreatePortfolioVersion();
        void onDeletePortfolioVersion();
        void onClonePortfolioVersion();
    }
}
