package de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz;

import java.util.function.Supplier;

import com.google.gwt.user.client.ui.Image;

import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.SimpleHtmlController;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormKeys;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author umaurer
 */
public class CustomerDzWgz extends Customer {

    public static final String DZ_BANK_ISSUERNAME = "DZ BANK AG";  // $NON-NLS$

    public static final String WGZ_BANK_ISSUERNAME = "WGZ BANK AG";  // $NON-NLS$

    @Override
    public boolean isDzWgz() {
        return true;
    }

    @Override
    public boolean isPreferredFinderIssuer(String issuer) {
        return Selector.DZ_BANK_USER.isAllowed() && issuer.startsWith("DZ") // $NON-NLS$
                || Selector.WGZ_BANK_USER.isAllowed() && issuer.startsWith("WGZ"); // $NON-NLS$
    }


    @Override
    public FinderMetaList addCustomerIssuers(FinderMetaList list) {
        int count = 0;
        handleElement(list, count++, DZ_BANK_ISSUERNAME);
        handleElement(list, count, WGZ_BANK_ISSUERNAME);
        return list;
    }

    @Override
    public String getFndBestToolQuery() {
        return "vrIssuer == 'true'"; // $NON-NLS-0$
    }

    @Override
    public void prepareFndBestToolFinderLinkConfig(FinderFormConfig ffc) {
        ffc.put("vrIssuer", "true"); // $NON-NLS-0$ $NON-NLS-1$
    }

    @Override
    public String getCerBestToolQuery() {
        return "issuername=='DZ BANK@WGZ BANK'"; // $NON-NLS-0$
    }

    @Override
    public void prepareCerBestToolFinderLinkConfig(FinderFormConfig ffc) {
        ffc.put(FinderFormKeys.ISSUER_NAME, "true");  // $NON-NLS$
        ffc.put(FinderFormKeys.ISSUER_NAME + "-item", "DZ BANK");  // $NON-NLS$
    }

    @Override
    public PageController createHelpController(ContentContainer cc) {
        if (Selector.WGZ_BANK_USER.isAllowed()) {
            return SimpleHtmlController.createHelpWgz(cc);
        }
        return super.createHelpController(cc);
    }

    @NonNLS
    @Override
    @Deprecated
    public String getLegacyLogoBackgroundImage() {
        final JSONWrapper upvs = SessionData.INSTANCE.getGuiDef("userProductVariants");
        if (upvs.isArray()) {
            for (int i = 0, size = upvs.size(); i < size; i++) {
                final JSONWrapper upv = upvs.get(i);
                final String userProductVariant = upv.get("userProductVariant").stringValue();
                if (SessionData.INSTANCE.getUser().getAppProfile().isProductAllowed(userProductVariant)) {
                    return upv.get("legacyTopPanelLogo").stringValue();
                }
            }
        }
        return null;
    }

    @Override
    public Supplier<Image> getRightLogoSupplier() {
        return () -> {
            return IconImage.get("empty-24").createImage(); // $NON-NLS$
        };
    }
}
