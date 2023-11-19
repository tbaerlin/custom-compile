package de.marketmaker.iview.mmgwt.mmweb.client.customer.dbk;

import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormKeys;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderCER;

/**
 * @author mloesch
 */
public class CustomerHvb extends Customer {

    @Override
    public boolean isPreferredFinderIssuer(String issuer) {
        return issuer.startsWith("HypoVereinsbank"); // $NON-NLS$
    }


    @Override
    public FinderMetaList addCustomerIssuers(FinderMetaList list) {
        handleElement(list, 0, "HypoVereinsbank"); // $NON-NLS$
        return list;
    }

    @Override
    public String getCerBestToolQuery() {
        return "issuername=='HypoVereinsbank'"; // $NON-NLS-0$
    }

    @Override
    public void prepareCerBestToolFinderLinkConfig(FinderFormConfig ffc) {
        ffc.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS-0$
        ffc.put(FinderFormKeys.ISSUER_NAME + "-item", "HypoVereinsbank"); // $NON-NLS-0$ $NON-NLS-1$
    }

    @Override
    public boolean isHvb() {
        return true;
    }

    @Override
    public boolean isDzWgz() {
        return true;
    }
}
