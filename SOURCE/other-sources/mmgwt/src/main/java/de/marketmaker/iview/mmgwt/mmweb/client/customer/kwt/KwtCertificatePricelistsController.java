package de.marketmaker.iview.mmgwt.mmweb.client.customer.kwt;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.iview.mmgwt.mmweb.client.SimpleTabController;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.MultiContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

/**
 * @author umaurer
 */
public class KwtCertificatePricelistsController extends SimpleTabController {
    public static final String JSON_KEY_PRICELISTS = "Kwtzertis"; // $NON-NLS-0$

    public KwtCertificatePricelistsController(ContentContainer contentContainer) {
        super(contentContainer, JSON_KEY_PRICELISTS + "-controller"); // $NON-NLS-0$
    }

    protected PageController createPageController(MultiContentView view) {
        return new KwtCertificatePricelistController(view);
    }

    public ViewSpec[] initViewSpecs() {
        final JSONValue jsonValue = SessionData.INSTANCE.getGuiDef(JSON_KEY_PRICELISTS).getValue();
        if (jsonValue == null) {
            return new ViewSpec[]{};
        }
        final JSONObject pricelists = jsonValue.isObject();
        final List<ViewSpec> list = new ArrayList<ViewSpec>();

        for (String id : pricelists.keySet()) {
            final JSONObject jsonObject = pricelists.get(id).isObject();
            final String name = jsonObject.get("name").isString().stringValue(); // $NON-NLS-0$
            list.add(new ViewSpec(id, name, null, null));
        }

        return list.toArray(new ViewSpec[list.size()]);
    }


    @Override
    public String getPrintHtml() {
        return this.pageController.getPrintHtml();
    }
}
