package de.marketmaker.iview.mmgwt.mmweb.client.customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Image;

import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.RightLogoSupplier;
import de.marketmaker.iview.mmgwt.mmweb.client.SimpleHtmlController;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.advisorysolution.CustomerAS;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.advisorysolution.CustomerAsWithoutXlsExportForTableLayouts;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.advisorysolution.CustomerBankhausLampe;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.apo.CustomerApoBank;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.dbk.CustomerDbk;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.dbk.CustomerHvb;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz.CustomerDzWgz;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.kwt.CustomerKwt;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.lbbw.CustomerLbbw;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.olb.CustomerOlb;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.vwd.CustomerVwd;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author umaurer
 */
public class Customer {
    public static final Customer INSTANCE = getInstance();


    private static Customer getInstance() {
        if(SessionData.isAsDesign() && SessionData.isWithPmBackend()) {
            // _as does not have any special Guidefs. It has only the default guidefs.
            // Therefore we need a workaround to set customer specific config parameters;
            final String moduleName = GuiDefsLoader.getModuleName();
            if(StringUtil.hasText(moduleName)) {
                switch (moduleName) {
                    case "hauck-aufhaeuser": // $NON-NLS$
                    case "bankhaus-bauer": // $NON-NLS$
                        return new CustomerAsWithoutXlsExportForTableLayouts();
                    case "bankhaus-lampe": // $NON-NLS$
                        return new CustomerBankhausLampe();
                    default:
                         /*"vwd", donner-reuschel", "fuchsbriefe", "sgkb" (AS-1547), "fuerst-fugger" (AS-1555)*/
                        return new CustomerAS();
                }
            }
        }

        final String customer = SessionData.INSTANCE.getGuiDefValue("customer"); // $NON-NLS-0$
        if ("VWD".equals(customer)) { // $NON-NLS-0$
            return new CustomerVwd();
        }
        else if ("OLB".equals(customer)) { // $NON-NLS-0$
            return new CustomerOlb();
        }
        else if ("ApoBank".equals(customer)) { // $NON-NLS-0$
            return new CustomerApoBank();
        }
        else if ("DZ-WGZ".equals(customer)) { // $NON-NLS-0$
            return new CustomerDzWgz();
        }
        else if ("KWT".equals(customer)) { // $NON-NLS-0$
            return new CustomerKwt();
        }
        else if ("DBK".equals(customer)) {// $NON-NLS$
            return new CustomerDbk();
        }
        else if ("HVB".equals(customer)) {// $NON-NLS$
            return new CustomerHvb();
        }
        else if ("LBBW".equals(customer)) {// $NON-NLS$
            return new CustomerLbbw();
        }
        return new Customer();
    }

    public boolean isVwd() {
        return false;
    }

    public boolean isApobank() {
        return false;
    }

    public boolean isDzWgz() {
        return false;
    }

    public boolean isKwt() {
        return false;
    }

    public boolean isOlb() {
        return false;
    }

    public boolean isDbk() {
        return false;
    }

    public boolean isHvb() {
        return false;
    }

    public boolean isDzWgzApoKwt() {
        return isApobank() || isDzWgz() || isKwt();
    }


    public boolean isPreferredFinderIssuer(String issuer) {
        return false;
    }


    public FinderMetaList addCustomerIssuers(FinderMetaList list) {
        // nothing to do here, implementation maybe in subclasses
        return list;
    }

    protected FinderMetaList.Element createFinderMetaListElement(String key) {
        final FinderMetaList.Element elt = new FinderMetaList.Element();
        elt.setKey(key);
        elt.setName(key.startsWith("+") ? key.substring(1) : key);
        return elt;
    }

    public String getFndBestToolQuery() {
        return null;
    }

    public void prepareFndBestToolFinderLinkConfig(FinderFormConfig ffc) {

    }

    public String getCerBestToolQuery() {
        return null;
    }

    public void prepareCerBestToolFinderLinkConfig(FinderFormConfig ffc) {

    }

    public PageController createHelpController(ContentContainer cc) {
        return SimpleHtmlController.createHelp(cc);
    }

    public boolean isJsonMenuElementTrue(String menuItemId) {
        final Boolean value = getMapJsonElements().get(menuItemId);
        return value != null && value;
    }

    public boolean isJsonMenuElementNotFalse(String menuItemId) {
        final Boolean value = getMapJsonElements().get(menuItemId);
        return value == null || value;
    }

    public boolean isJsonMyspaceSnippetsMenuElementTrue(String menuItemId) {
        final Boolean value = getMapJsonMyspaceSnippetsMenuElements().get(menuItemId);
//        DebugUtil.logToFirebugConsole("isJsonMyspaceSnippetsMenuElementTrue(\"" + menuItemId + "\"): " + (value != null && value));
        return value != null && value;
    }

    private Map<String, Boolean> mapJsonMyspaceSnippetsMenuElements = null;
    private Map<String, Boolean> getMapJsonMyspaceSnippetsMenuElements() {
        if (this.mapJsonMyspaceSnippetsMenuElements == null) {
            this.mapJsonMyspaceSnippetsMenuElements = new HashMap<>();
            addMenuElements("default-myspace-snippets-menu", this.mapJsonMyspaceSnippetsMenuElements); // $NON-NLS$
            addMenuElements("myspace-snippets-menu", this.mapJsonMyspaceSnippetsMenuElements); // $NON-NLS$
        }
        return this.mapJsonMyspaceSnippetsMenuElements;
    }

    private Map<String, Boolean> mapJsonMenuElements = null;
    private Map<String, Boolean> getMapJsonElements() {
        if (this.mapJsonMenuElements == null) {
            this.mapJsonMenuElements = new HashMap<>();
            addMenuElements("default-menu-elements", this.mapJsonMenuElements); // $NON-NLS-0$
            addMenuElements("menu-elements", this.mapJsonMenuElements); // $NON-NLS-0$
        }
        return this.mapJsonMenuElements;
    }

    private void addMenuElements(String jsonKey, Map<String, Boolean> map) {
        final JSONValue jvMenuElements = SessionData.INSTANCE.getGuiDef(jsonKey).getValue();
        if (jvMenuElements != null) {
            final JSONObject joMenuElements = jvMenuElements.isObject();
            if (joMenuElements != null) {
                addMenuElements(joMenuElements, map);
            }
            final JSONArray joMenusArray = jvMenuElements.isArray();
            if (joMenusArray != null) {
                final JSONObject menuElements = pickFirstEnabledMenu(joMenusArray);
                addMenuElements(menuElements, map);
            }
        }
    }

    private JSONObject pickFirstEnabledMenu(JSONArray joMenusArray) {
        for (int i = 0; i < joMenusArray.size(); i++) {
            JSONObject current = joMenusArray.get(i).isObject();
            if (current == null) {
                continue;
            }
            if (!current.containsKey("ifFeatureFlag")) { // $NON-NLS$
                return current;
            }
            final String ifFeatureFlag = current.get("ifFeatureFlag").isString().stringValue(); // $NON-NLS$
            if ( (ifFeatureFlag.startsWith("!") && !FeatureFlags.isEnabled(ifFeatureFlag.substring(1)))
                    || (!ifFeatureFlag.startsWith("!") && FeatureFlags.isEnabled(ifFeatureFlag)) ) {
                return current;
            }
        }
        return new JSONObject();
    }

    private void addMenuElements(JSONObject joMenuElements, Map<String, Boolean> map) {
        for (final String id : joMenuElements.keySet()) {
            if ("ifFeatureFlag".equals(id)) { // $NON-NLS$
                continue;
            }
            final String value = joMenuElements.get(id).isString().stringValue();
            map.put(id, Boolean.valueOf(value));
        }
    }

    protected void handleElement(FinderMetaList list, int count, String key) {
        final int idx = indexOf(list.getElement(), key);
        if (idx < 0) {
            list.getElement().add(count, createFinderMetaListElement(key));
        } else if (idx != count) {
            moveElementToPos(list, key, count);
        }
    }

    private void moveElementToPos(FinderMetaList list, String key, int count) {
        final List<FinderMetaList.Element> tmp = new ArrayList<>();
        tmp.addAll(list.getElement());
        list.getElement().clear();

        final FinderMetaList.Element e = findElementByKey(tmp, key);
        for (FinderMetaList.Element element : tmp) {
            if (e != element) {
                if (list.getElement().size() == count) {
                    list.getElement().add(e);
                }
                list.getElement().add(element);
            }
        }
    }

    private int indexOf(List<FinderMetaList.Element> list, String key) {
        final FinderMetaList.Element element = findElementByKey(list, key);
        if (element == null) {
            return -1;
        }
        return list.indexOf(element);
    }

    private FinderMetaList.Element findElementByKey(List<FinderMetaList.Element> list, String key) {
        for (FinderMetaList.Element e : list) {
            if (e.getKey().equals(key)) {
                return e;
            }
        }
        return null;
    }

    public String getCustomPageTypeString() {
        return "vwd"; // $NON-NLS$
    }

    @Deprecated
    public String getLegacyLogoBackgroundImage() {
        return null;
    }

    public boolean isCustomerAS() {
        return false;
    }

    public CustomerAS asCustomerAS() {
        return null;
    }

    public Supplier<Image> getRightLogoSupplier() {
        return new RightLogoSupplier();
    }
}