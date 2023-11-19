package de.marketmaker.iview.mmgwt.mmweb.client.customer.dbk;

import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.SimpleHtmlController;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormKeys;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author umaurer
 */
public class CustomerDbk extends Customer {

    @Override
    public boolean isPreferredFinderIssuer(String issuer) {
        return issuer.startsWith("Deutsche Bank"); // $NON-NLS$
    }


    @Override
    public FinderMetaList addCustomerIssuers(FinderMetaList list) {
        handleElement(list, 0, "Deutsche Bank"); // $NON-NLS$
        return list;
    }

    @Override
    public String getCerBestToolQuery() {
        return "issuername=='Deutsche Bank'"; // $NON-NLS-0$
    }

    @Override
    public void prepareCerBestToolFinderLinkConfig(FinderFormConfig ffc) {
        ffc.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS-0$
        ffc.put(FinderFormKeys.ISSUER_NAME + "-item", "Deutsche Bank");  // $NON-NLS-0$ $NON-NLS-1$
    }

    @Override
    public boolean isDbk() {
        return true;
    }

    @Override
    public boolean isDzWgz() {
        return true;
    }
}
