package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;

/**
 * @author umaurer
 */
public class InvestorConfigController extends AbstractPageController {
    final InvestorConfigView view = new InvestorConfigView(this);

    public void onPlaceChange(PlaceChangeEvent event) {
        getContentContainer().setContent(this.view);
    }

    public void saveGroup(InvestorItem item) {
        this.view.reload(item);
    }

}
