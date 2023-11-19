package de.marketmaker.iview.mmgwt.mmweb.client.customer.apo;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.iview.mmgwt.mmweb.client.SimpleTabController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.MultiContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;

/**
 * @author umaurer
 */
public class ApoFundPricelistsController extends SimpleTabController {
    public static final String JSON_KEY_PRICELISTS = "apo-fund-pricelists"; // $NON-NLS-0$
    public static final String JSON_KEY_PRICELIST_PREFIX = "list_apo_funds_"; // $NON-NLS-0$

    public ApoFundPricelistsController(ContentContainer contentContainer) {
        super(contentContainer, JSON_KEY_PRICELISTS + "-controller"); // $NON-NLS-0$
    }

    protected PageController createPageController(final MultiContentView view) {
        return new ApoFundPricelistController(view);
    }

    protected ViewSpec[] initViewSpecs() {
        final JSONValue jsonValue = SessionData.INSTANCE.getGuiDef(JSON_KEY_PRICELISTS).getValue();
        if (jsonValue == null) {
            return new ViewSpec[]{};
        }
        final JSONArray pricelists = jsonValue.isArray();
        final List<ViewSpec> list = new ArrayList<ViewSpec>();

        for (int i = 0; i < pricelists.size(); i++) {
            final JSONObject pricelist = pricelists.get(i).isObject();
            final String id = pricelist.get("id").isString().stringValue(); // $NON-NLS-0$
            final String name = pricelist.get("title").isString().stringValue(); // $NON-NLS-0$
            list.add(new ViewSpec(id, name, null, null));
        }

        return list.toArray(new ViewSpec[list.size()]);
    }
}
