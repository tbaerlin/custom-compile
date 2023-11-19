package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentryactivity;

import de.marketmaker.iview.mmgwt.mmweb.client.as.SimpleResetPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskViewPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

public class SetPasswordPageController extends SimpleResetPageController {

    private final SetPasswordView view;
    private final SetPasswordPresenter presenter;

    public SetPasswordPageController(ContentContainer contentContainer) {
        super(contentContainer);
        this.view = new SetPasswordView();
        this.presenter = new SetPasswordPresenter(this.view);
    }

    @Override
    protected void reset() {
        updatePinAndDoLayout();
        this.presenter.reset();
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
