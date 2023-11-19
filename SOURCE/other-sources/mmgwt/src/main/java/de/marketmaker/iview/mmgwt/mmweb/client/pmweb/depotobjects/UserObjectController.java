/*
 * UserObjectController.java
 *
 * Created on 18.12.12 08:03
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
public class UserObjectController<U, C extends AbstractDepotObjectPortraitController<U>, P extends UserObjectDisplay.UserObjectPresenter<U>>
        extends AbstractPageController
        implements UserObjectDisplay.UserObjectPresenter<U>   {
    private final UserObjectDisplay<U, P> view;
    private final C controller;

    private U currentUserObject = null;

    public UserObjectController(C controller, UserObjectDisplay<U, P> view) {
        this(controller, view, false);
    }

    @SuppressWarnings("unchecked")
    public UserObjectController(C controller, UserObjectDisplay<U, P> view, boolean editAllowed) {
        this.controller = controller;
        this.view = view;
        this.view.setPresenter((P)this);
        this.view.setEditButtonVisible(editAllowed);
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        if(!(this.currentUserObject == this.controller.getUserObject())) {
            Firebug.debug("<UserObjectController.onPlaceChange> Object is not identical with former object, update view necessary");
            this.currentUserObject = this.controller.getUserObject();
            this.view.updateView(this.currentUserObject);
        }
        else {
            Firebug.debug("<UserObjectController.onPlaceChange> Object is identical with former object, update view not necessary");
        }
        getContentContainer().setContent(this.view.asWidget());
        this.view.ensureVisible(historyToken.get(NavItemSpec.SUBCONTROLLER_KEY));
    }

    protected C getController() {
        return this.controller;
    }

    protected UserObjectDisplay<U, P> getView() {
        return this.view;
    }

    @Override
    public String getPrintHtml() {
        return this.view.getPrintHtml();
    }

    @Override
    public void onEditButtonClicked() {
        this.controller.onEdit();
    }
}