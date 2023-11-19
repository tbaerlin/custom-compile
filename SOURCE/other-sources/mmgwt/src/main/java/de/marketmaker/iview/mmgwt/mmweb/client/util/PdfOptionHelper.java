package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.view.PdfOptionView;

import java.util.HashMap;
import java.util.Map;

/**
 * @author umaurer
 */
public class PdfOptionHelper {
    private final static String DEFAULT_PDF_OPTIONS = "default_pdf_options"; // $NON-NLS$

    private final PdfOptionView view;

    private final PdfOptionSpec spec;

    private final String appConfigPrefix;

    private final Map<String, String> mapParameters = new HashMap<>();

    private Map<String, String> mapPageParameters = null;

    private final PostSupport postSupport = new PostSupport("pdfrequest"); //$NON-NLS$

    public PdfOptionHelper(PdfOptionView view, PdfOptionSpec spec,
                           Map<String, String> mapPageParameters) {
        this.view = view;
        this.spec = spec;
        this.appConfigPrefix = "pdfOption/" + spec.getGuidefOptionsId() + "/"; // $NON-NLS-0$ $NON-NLS-1$
        this.mapPageParameters = mapPageParameters;
        setOptions();
    }

    public void setMapPageParameters(Map<String, String> mapPageParameters) {
        this.mapPageParameters = mapPageParameters;
        updateLink();
    }

    public boolean isFor(PdfOptionSpec spec) {
        return this.spec.equals(spec);
    }

    private void setOption(final String option) {
        final AppConfig appConfig = SessionData.INSTANCE.getUser().getAppConfig();
        final JSONValue jsonValue = SessionData.INSTANCE.getGuiDef(option).getValue();
        if (jsonValue == null) {
            return;
        }

        final JSONArray jsonArray = jsonValue.isArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            final JSONObject jsonObject = jsonArray.get(i).isObject();

            if (!isEnabledByFeatureFlag(jsonObject)) {
                continue;
            }

            if (!isAllowed(jsonObject)) {
                continue;
            }

            final String id = jsonObject.get("id").isString().stringValue(); // $NON-NLS-0$
            final String title = jsonObject.get("title").isString().stringValue(); // $NON-NLS-0$
            final String defaultValue = jsonObject.get("selected").isString().stringValue(); // $NON-NLS-0$
            final String appConfigValue = appConfig.getProperty(this.appConfigPrefix + id);
            final String value = appConfigValue != null ? appConfigValue : defaultValue;
            final String style = jsonObject.get("style").isString().stringValue(); // $NON-NLS-0$
            final String values[] = new String[]{
                    getValue(jsonObject, "valueChecked", "true"), // $NON-NLS-0$ $NON-NLS-1$
                    getValue(jsonObject, "valueUnchecked", "false") // $NON-NLS-0$ $NON-NLS-1$
            };
            this.view.addOption(id, title, values, values[0].equals(value), style);

            this.mapParameters.put(id, value);
        }
    }

    public void setOptions() {
        this.mapParameters.clear();
        String optionsID = this.spec.getGuidefOptionsId();
        setOption(optionsID);
        if (isDefaultOptionsAvailable()) {
            setOption(DEFAULT_PDF_OPTIONS);
        }

        final String uri = getPdfUri();
        final boolean usePost = BrowserSpecific.INSTANCE.isUriTooLong(GWT.getHostPageBaseURL() + uri);
        this.view.addLink(uri, "mm-pdf-option-link", usePost); // $NON-NLS-0$
    }

    private String getValue(JSONObject jsonObject, String key, String defaultValue) {
        final JSONValue jsonValue = jsonObject.get(key);
        if (jsonValue == null) {
            return defaultValue;
        }
        return jsonValue.isString().stringValue();
    }

    private boolean isAllowed(JSONObject jsonObject) {
        final JSONValue jsonSelector = jsonObject.get("selector"); // $NON-NLS-0$
        return jsonSelector == null || Selector.isAllowed(jsonSelector.isString().stringValue());
    }

    private boolean isEnabledByFeatureFlag(JSONObject jsonObject) {
        final JSONValue jsonSelector = jsonObject.get("ifFeatureFlag");  // $NON-NLS-0$
        if (jsonSelector == null) {
            return true;
        }
        final String ifFeatureFlag = jsonSelector.isString().stringValue();
        if (ifFeatureFlag == null || ifFeatureFlag.trim().length() == 0) {
            return true;
        }
        if (ifFeatureFlag.startsWith("!") && !FeatureFlags.isEnabled(ifFeatureFlag.substring(1))) {
            return true;
        }
        return FeatureFlags.isEnabled(ifFeatureFlag);
    }

    public void saveOptions() {
        if (this.spec.getGuidefOptionsId() == null) {
            return;
        }
        final AppConfig appConfig = SessionData.INSTANCE.getUser().getAppConfig();
        final JSONValue jsonValue = SessionData.INSTANCE.getGuiDef(this.spec.getGuidefOptionsId()).getValue();
        final JSONArray jsonArray = jsonValue.isArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            final JSONObject jsonObject = jsonArray.get(i).isObject();

            if (!isEnabledByFeatureFlag(jsonObject)) {
                continue;
            }

            if (!isAllowed(jsonObject)) {
                continue;
            }

            final String id = jsonObject.get("id").isString().stringValue(); // $NON-NLS-0$
            final String defaultValue = jsonObject.get("selected").isString().stringValue(); // $NON-NLS-0$
            String currentValue = this.mapParameters.get(id);
            if (defaultValue.equals(currentValue)) {
                currentValue = null;
            }
            appConfig.addProperty(this.appConfigPrefix + id, currentValue);
        }
    }

    public void setOption(String id, boolean checked) {
        setOption(id, String.valueOf(checked));
    }

    public void setOption(String id, String value) {
        this.mapParameters.put(id, value);
        updateLink();
    }

    public void setDisabled(String option, boolean disabled) {
        this.spec.setDisabled(option, disabled);
    }

    private void updateLink() {
        final String uri = getPdfUri();
        final boolean usePost = BrowserSpecific.INSTANCE.isUriTooLong(GWT.getHostPageBaseURL() + uri);
        this.view.updateLink(uri, usePost);

        saveOptions();
    }

    public void openPdfByPost() {
        final Map<String, String> joinedParameters = joinParameters();
        final Map<String, String> formParameters = new HashMap<>();

        for (final Map.Entry<String, String> entry : joinedParameters.entrySet()) {
            final String key = entry.getKey();
            formParameters.put(key, this.spec.isDisabled(key) ? "false" : entry.getValue()); // $NON-NLS$
        }

        final UrlBuilder actionUrlBuilder = UrlBuilder.forPdf(spec.getLinkFile()).addStyleSuffix();
        //Add a non-sense parameter to be compatible with rewrite rules
        actionUrlBuilder.add("p", "np"); //$NON-NLS$
        this.postSupport.createFormAndSubmit(actionUrlBuilder.toURL(), formParameters);
    }

    private Map<String, String> joinParameters() {
        final Map<String, String> joinedParameters = new HashMap<>();

        final String pdfLogo = JsUtil.getMetaValue("pdfLogo"); // $NON-NLS$
        if (pdfLogo != null) {
            Firebug.debug("pdfLogo: " + pdfLogo);
            joinedParameters.put("iviewlogoFile", pdfLogo); // $NON-NLS$
        }

        if (this.mapPageParameters != null) {
            joinedParameters.putAll(this.mapPageParameters);
        }
        if (this.spec.getMapDefaultParameters() != null) {
            joinedParameters.putAll(this.spec.getMapDefaultParameters());
        }
        joinedParameters.putAll(this.mapParameters);

        return joinedParameters;
    }

    public String getPdfUri() {
        final UrlBuilder builder = UrlBuilder.forPdf(this.spec.getLinkFile()).addStyleSuffix();
        final Map<String, String> mapParameters = joinParameters();
        for (final Map.Entry<String, String> entry : mapParameters.entrySet()) {
            final String id = entry.getKey();
            builder.add(id, this.spec.isDisabled(id) ? "false" : entry.getValue()); // $NON-NLS-0$
        }
        return builder.toURL();
    }

    public PdfOptionSpec getPdfOptionSpec() {
        return this.spec;
    }

    public static String getPdfUri(PdfOptionSpec spec, Map<String, String> mapPageParameters) {
        if (StringUtility.hasText(spec.getGuidefOptionsId())) {
            throw new RuntimeException("cannot generate pdf URI for PdfOptionSpec with guidefOptionsId != null"); // $NON-NLS-0$
        }
        final UrlBuilder builder = UrlBuilder.forPdf(spec.getLinkFile()).addStyleSuffix();
        final String pdfLogo = JsUtil.getMetaValue("pdfLogo"); // $NON-NLS$
        if (pdfLogo != null) {
            Firebug.debug("pdfLogo: " + pdfLogo);
            builder.add("iviewlogoFile", pdfLogo); // $NON-NLS$
        }
        final Map<String, String> mapParameters = new HashMap<>();
        if (mapPageParameters != null) {
            mapParameters.putAll(mapPageParameters);
        }
        final Map<String, String> mapDefaultParameters = spec.getMapDefaultParameters();
        if (mapDefaultParameters != null) {
            mapParameters.putAll(mapDefaultParameters);
        }
        for (final Map.Entry<String, String> entry : mapParameters.entrySet()) {
            final String id = entry.getKey();
            builder.add(id, entry.getValue());
        }
        return builder.toURL();
    }

    public static boolean isDefaultOptionsAvailable() {
        return SessionData.INSTANCE.getGuiDef(DEFAULT_PDF_OPTIONS).getValue() != null;
    }
}
