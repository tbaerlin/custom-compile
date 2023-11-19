package de.marketmaker.iview.mmgwt.mmweb.client.customer.apo;

import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.SimpleHtmlController;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author umaurer
 */
public class CustomerApoBank extends Customer {
    @Override
    public boolean isApobank() {
        return true;
    }

    @Override
    public PageController createHelpController(ContentContainer cc) {
        return SimpleHtmlController.createHelpApobank(cc);
    }
}
