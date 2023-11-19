package de.marketmaker.iview.mmgwt.mmweb.client.customer.wgz;

import de.marketmaker.iview.mmgwt.mmweb.client.DelegatingPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author umaurer
 */
public class WGZCertificatesController extends DelegatingPageController {
    private static final String DEF = "wgz_cert"; // $NON-NLS-0$

    public WGZCertificatesController(ContentContainer contentContainer) {
        super(contentContainer);
    }

    protected void initDelegate() {
        this.delegate = SnippetsFactory.createFlexController(getContentContainer(), DEF);
    }
}
