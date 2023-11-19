package de.marketmaker.iview.mmgwt.mmweb.client.customer.wgz;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.CERFinder;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CertificateTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderController;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormKeys;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderCER;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Assumption: always 3x3 matrix
 *
 * @author umaurer
 */
public class WGZCertificateTypesController extends AbstractPageController {

    private final DmxmlContext.Block<CERFinder> block;
    private final WGZCertificateTypesView view;


    public WGZCertificateTypesController(ContentContainer contentContainer) {
        super(contentContainer);
        this.block = this.context.addBlock("CER_Finder"); // $NON-NLS-0$
        this.block.setParameter("count", "0"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("query", "issuername=='WGZ' AND dzIsLeverageProduct=='false'"); // $NON-NLS-0$ $NON-NLS-1$
        this.view = new WGZCertificateTypesView(this);
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        getContentContainer().setContent(this.view.asWidget());
        refresh();
    }

    public void destroy() {
        this.context.removeBlock(this.block);
    }

    @Override
    protected void onResult() {
        if (!this.block.isResponseOk()) {
            this.view.update(null);
            return;
        }
        final LinkedHashMap<CertificateTypeEnum, String> rows = new LinkedHashMap<>();
        final List<FinderTypedMetaList> metadata = this.block.getResult().getMetadata();
        for (FinderTypedMetaList finderTypedMetaList : metadata) {
            if (!"typeKey".equals(finderTypedMetaList.getName())) { // $NON-NLS$
                continue;
            }
            final List<FinderMetaList.Element> types = finderTypedMetaList.getElement();
            for (FinderMetaList.Element type : types) {
                try {
                    final CertificateTypeEnum certificateType = CertificateTypeEnum.valueOf(type.getKey());
                    if (CertificateTypeEnum.isCertificateAllowed(certificateType, false)) {
                        rows.put(certificateType, type.getCount());
                    }
                } catch (IllegalArgumentException e) {
                    Firebug.log("WGZCertificateTypeSnippet <updateView> could not handle typekey " + type.getKey());
                }
            }
            break;
        }
        this.view.update(rows);
    }

    public void openFinder(CertificateTypeEnum type) {
        final FinderController controller = LiveFinderCER.INSTANCE_CER;
        final FinderFormConfig ffc = new FinderFormConfig("temp", controller.getId()); // $NON-NLS$
        ffc.put(LiveFinderCER.BASE_ID, "true"); // $NON-NLS$
        ffc.put(FinderFormKeys.TYPE, "true"); // $NON-NLS$
        ffc.put(FinderFormKeys.TYPE + "-item", type.name()); // $NON-NLS$
        ffc.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS-0$
        ffc.put(FinderFormKeys.ISSUER_NAME + "-item", "WGZ"); // $NON-NLS-0$ $NON-NLS-1$
        controller.prepareFind(ffc);
        PlaceUtil.goTo("M_LF_CER"); // $NON-NLS$
    }
}
