/*
 * AsChangePasswordPageController.java
 *
 * Created on 22.01.2015 12:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.ChangePasswordPresenter;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskViewPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author mdick
 */
public class AsChangePasswordPageController extends SimpleResetPageController {
    private final AsChangePasswordContentView view;
    private final ChangePasswordPresenter presenter;

    public AsChangePasswordPageController(ContentContainer contentContainer) {
        super(contentContainer);
        this.view = new AsChangePasswordContentView();
        this.presenter = new ChangePasswordPresenter(this.view,
                AbstractMainController.INSTANCE.getPasswordStrategy(),
                null);
    }

    @Override
    protected void reset() {
        updatePinAndDoLayout();
        this.view.reset();
    }

    private void updatePinAndDoLayout() {
        final TaskViewPanel tvp = this.view.asWidget();
        tvp.updateSouthWidgetPinned();
        tvp.layout();
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        this.presenter.reset();
        updatePinAndDoLayout();
        getContentContainer().setContent(this.view.asWidget());
    }
}
