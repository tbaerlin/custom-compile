package de.marketmaker.iview.mmgwt.mmweb.client.customer.kwt;

import java.util.function.Supplier;

import com.google.gwt.user.client.ui.Image;

import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.Ginjector;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.SimpleHtmlController;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.logging.Logger;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author umaurer
 */
public class CustomerKwt extends Customer {
    private final Logger logger = Ginjector.INSTANCE.getLogger();

    @Override
    public boolean isKwt() {
        return true;
    }


    @Override
    public PageController createHelpController(ContentContainer cc) {
        return SimpleHtmlController.createHelpKwt(cc);
    }

    @Override
    public FinderMetaList addCustomerIssuers(FinderMetaList list) {
        final JSONWrapper guiDef = SessionData.INSTANCE.getGuiDef("kwt_zert_issuer_list"); // $NON-NLS$
        for (int i = 0; i < guiDef.size(); i++) {
            JSONWrapper wrapper = guiDef.get(i);
            handleElement(list, i, wrapper.stringValue());
        }
        return list;
    }

    @Override
    public Supplier<Image> getRightLogoSupplier() {
        return new Supplier<Image>() {
            private String logoUrl = null;

            @Override
            public Image get() {
                if(this.logoUrl == null) {
                    this.logoUrl = Ginjector.INSTANCE.getSessionData().getGuiDefValue("kwt-right-logo-provider", "filename"); // $NON-NLS$
                }
                if(this.logoUrl == null) {
                    return IconImage.get("empty-24").createImage(); // $NON-NLS$
                }

                final String zone = GuiDefsLoader.getModuleName();
                final String imageUri = "/" + zone + "/images/zones/" + zone + "/" + this.logoUrl; // $NON-NLS$
                CustomerKwt.this.logger.info("<CustomerKwt.getRightLogoSupplier..get> failed to load logo " + imageUri);
                return new Image(imageUri);
            }
        };
    }
}
