
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NeedsScrollLayout;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.DzBankTeaserUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.WorkspaceLists;
import de.marketmaker.iview.mmgwt.mmweb.server.teaser.TeaserDaoDb;


import java.util.Date;

/**
 * UI component to configure a teaser image with a link
 */
public class TeaserConfigForm extends AbstractPageController {
    protected static final long serialVersionUID = 1L;

    private static final int LABEL_WIDTH = 380;

    public static final String READ_URL = "/teaser/read"; // $NON-NLS-0$

    public static final String WRITE_ACTION_URL = "/teaser/write"; // $NON-NLS-0$

    public static final String IMG_SRC_URL = "/teaser/view"; // $NON-NLS-0$

    private final CheckBox teaserEnabled;

    private final FileUploadField teaserUpload;

    private final CheckBox linkEnabled;

    private final TextField<String> linkUrl;

    private final CheckBox newWindowForLink;

    private final Hidden linkTarget;

    private final Hidden version;

    private final Image teaserImage;

    private final LayoutContainer view = new FlowLayoutContainer();

    public TeaserConfigForm() {
        final FormPanel formPanel = createFormPanel();

        final FieldSet settingsFieldSet = createFieldSet(I18n.I.settings());
        settingsFieldSet.setWidth(500);
        formPanel.add(settingsFieldSet);
        formPanel.addListener(Events.Submit, event -> {
            refreshPreviewImage();
            // Teaser is updated even if only the preview was updated
            if (!SessionData.isAsDesign()) {
                WorkspaceLists.INSTANCE.updateTeasers();
            }
            else {
                DzBankTeaserUtil.fireTeaserUpdatedEvent();
            }
        });

        this.teaserEnabled = new CheckBox();
        this.teaserEnabled.setFieldLabel(I18n.I.teaserEnabled());
        this.teaserEnabled.setName("teaserEnabled"); // $NON-NLS-0$
        this.teaserEnabled.setValue(true);
        settingsFieldSet.add(this.teaserEnabled);

        settingsFieldSet.add(createImageInfotxt());

        this.teaserUpload = new FileUploadField();
        this.teaserUpload.setFieldLabel(I18n.I.teaserGraphic());
        this.teaserUpload.setName("teaserUpload"); // $NON-NLS-0$
        settingsFieldSet.add(this.teaserUpload);

        this.linkEnabled = new CheckBox();
        this.linkEnabled.setFieldLabel(I18n.I.teaserLinkEnabled());
        this.linkEnabled.setName("linkEnabled"); // $NON-NLS-0$
        this.linkEnabled.setValue(true);
        settingsFieldSet.add(this.linkEnabled);

        settingsFieldSet.add(createLinkInfotxt());

        this.linkUrl = GuiUtil.createTextField(I18n.I.teaserLinkUrl(), 150, LABEL_WIDTH);
        this.linkUrl.setName("linkUrl"); // $NON-NLS-0$
        settingsFieldSet.add(linkUrl);

        // Setting the linkTarget will have no effect, because window.open always opens a new window.
        // The target just specifies the name of the window, which can be used in links to define
        // in which window the link will be applied.
        // See spec: https://developer.mozilla.org/en-US/docs/Web/API/Window/open
        // Preserve hidden field linkTarget for compatibility reasons is AS/ICE design
        this.newWindowForLink = new CheckBox();
        this.newWindowForLink.setFieldLabel(I18n.I.teaserLinkTarget());
        this.linkTarget = new Hidden();
        this.linkTarget.setName("linkTarget"); // $NON-NLS-0$
        if(!SessionData.isAsDesign()) {
            this.newWindowForLink.addListener(Events.Change, be -> {
                if (this.newWindowForLink.getValue()) {
                    this.linkTarget.setValue("_blank"); // $NON-NLS-0$
                }
                else {
                    this.linkTarget.setValue(""); // $NON-NLS-0$
                }
            });
        }
        else {
            this.newWindowForLink.setVisible(false);
            this.newWindowForLink.setValue(true);
            this.linkTarget.setValue("");
        }
        settingsFieldSet.add(this.linkTarget);
        settingsFieldSet.add(this.newWindowForLink);


        this.version = new Hidden();
        this.version.setName("version"); // $NON-NLS-0$
        settingsFieldSet.add(this.version);

        final FieldSet previewFieldSet = createFieldSet(I18n.I.teaserPreview());
        previewFieldSet.setWidth(250);
        formPanel.add(previewFieldSet);
        // lazy set the image url in refreshPreviewImage
        this.teaserImage = new Image();
        previewFieldSet.add(this.teaserImage);

        if (SessionData.isAsDesign()) {
            final HorizontalPanel buttonPanel = new HorizontalPanel();
            final de.marketmaker.itools.gwtutil.client.widgets.Button uploadButton = de.marketmaker.itools.gwtutil.client.widgets.Button.text(I18n.I.teaserSaveNext())
                    .clickHandler(event -> onUpdate(formPanel))
                    .build();

            final de.marketmaker.itools.gwtutil.client.widgets.Button employButton = de.marketmaker.itools.gwtutil.client.widgets.Button.text(I18n.I.teaserSaveCurrent())
                    .clickHandler(event -> onEmploy(formPanel))
                    .build();

            buttonPanel.add(uploadButton);
            buttonPanel.add(employButton);
            formPanel.add(buttonPanel);
        }
        else {
            final Button uploadButton = new Button(I18n.I.teaserSaveNext(), new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent buttonEvent) {
                    onUpdate(formPanel);
                }
            });

            final Button employButton = new Button(I18n.I.teaserSaveCurrent(), new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent buttonEvent) {
                    onEmploy(formPanel);
                }
            });

            formPanel.addButton(uploadButton);
            formPanel.addButton(employButton);
        }

        this.view.add(formPanel);
    }

    public void onUpdate(FormPanel formPanel) {
        doOnSubmit(formPanel, "next");  // $NON-NLS$
    }


    public void onEmploy(FormPanel formPanel) {
        Dialog.confirm(this.teaserEnabled.getValue() ? I18n.I.teaserConfirmEnable() : I18n.I.teaserConfirmDisable(),
                () -> {
                    doOnSubmit(formPanel, "current");  // $NON-NLS$
                });
    }

    public void doOnSubmit(FormPanel formPanel, String next) {
        this.version.setValue(next);
        formPanel.submit();
    }

    private FieldSet createFieldSet(String name) {
        final FormLayout layout = new FormLayout();
        layout.setLabelWidth(120);
        final FieldSet result = new FieldSet();
        result.setHeading(name);
        result.setLayout(layout);
        return result;
    }

    private Label createImageInfotxt() {
        final Label label = new Label();
        label.setWidth(Integer.toString(LABEL_WIDTH) + "px"); // $NON-NLS-0$
        label.setText(SessionData.isAsDesign() ? I18n.I.teaserImageInfotxtICE(MainView.DZ_BANK_TEASER_INNER_HEIGHT) : I18n.I.teaserImageInfotxt());
        return label;
    }

    private Label createLinkInfotxt() {
        final Label label = new Label();
        label.setWidth(Integer.toString(LABEL_WIDTH) + "px"); // $NON-NLS-0$
        label.setText(I18n.I.teaserLinkInfotxt());
        return label;
    }

    private void refreshPreviewImage() {
        if (teaserIsActive()) {
            teaserImage.setVisible(true);
            final String uid = Long.toHexString(new Date().getTime()); // just a unique id to avoid caching
            teaserImage.setUrl("/" + getZoneName() + IMG_SRC_URL + "?uid=" + uid + "&version=next");  // $NON-NLS$
        }
        else {
            teaserImage.setVisible(false);
        }
    }

    private void refreshFormContent() {
        final RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/" + getZoneName() + READ_URL + "?version=" + TeaserDaoDb.NEXT_VERSION); // $NON-NLS$
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable ex) {
                    Firebug.error("<refreshFormContent> error reading form data for teaser", ex); // $NON-NLS-0$
                }

                public void onResponseReceived(Request request, Response response) {
                    if (Response.SC_OK == response.getStatusCode()) {
                        final String jsonString = response.getText();
                        if (jsonString == null || jsonString.trim().isEmpty()
                                || JSONParser.parseStrict(jsonString).isObject() == null) {
                            teaserUpload.setValue("");
                            linkEnabled.setValue(true);
                            teaserEnabled.setValue(true);
                            linkUrl.setValue("");
                        }
                        else {
                            JSONObject jsonObj = JSONParser.parseStrict(jsonString).isObject();
                            teaserUpload.setValue(jsonObj.get("filename").isString().stringValue()); // $NON-NLS-0$
                            linkEnabled.setValue(jsonObj.get("linkEnabled").isBoolean().booleanValue()); // $NON-NLS-0$
                            teaserEnabled.setValue(jsonObj.get("teaserEnabled").isBoolean().booleanValue()); // $NON-NLS-0$
                            linkUrl.setValue(jsonObj.get("linkUrl").isString().stringValue()); // $NON-NLS-0$
                            linkTarget.setValue(jsonObj.get("linkTarget").isString().stringValue().trim()); // $NON-NLS-0$
                            newWindowForLink.setValue(!linkTarget.getValue().trim().isEmpty());
                        }
                        refreshPreviewImage();
                    }
                    else {
                        Firebug.debug("<refreshFormContent> couldn't get JSON (" + response.getStatusText() + ")"); // $NON-NLS$
                    }
                }
            });
        } catch (Exception ex) {
            Firebug.error("<refreshFormContent> exception while reading form data for teaser", ex); // $NON-NLS-0$
        }
    }

    private FormPanel createFormPanel() {
        final FormPanel formPanel = new FormPanel();
        formPanel.setAction("/" + getZoneName() + WRITE_ACTION_URL);
        formPanel.setEncoding(FormPanel.Encoding.MULTIPART);
        formPanel.setMethod(FormPanel.Method.POST);
        formPanel.setHeaderVisible(false);
        formPanel.setButtonAlign(Style.HorizontalAlignment.LEFT);
        return formPanel;
    }

    private boolean teaserIsActive() {
        return teaserEnabled.getValue();
    }

    private String getZoneName() {
        return GuiDefsLoader.getModuleName();
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        refreshFormContent();
        getContentContainer().setContent(this.view);
    }

    private static class FlowLayoutContainer extends LayoutContainer implements NeedsScrollLayout {
        FlowLayoutContainer() {
            super(new FlowLayout());
        }
    }

    public static abstract class TeaserRequest {

        public void fireRequest() {
            final RequestBuilder builder = new RequestBuilder(
                    RequestBuilder.GET, "/" + GuiDefsLoader.getModuleName() + READ_URL + "?version=current");  // $NON-NLS$

            try {
                builder.sendRequest(null, new RequestCallback() {
                    public void onError(Request request, Throwable ex) {
                        Firebug.error("<TeaserRequest> error reading form data for teaser", ex);  // $NON-NLS-0$
                    }

                    public void onResponseReceived(Request request, Response response) {
                        if (Response.SC_OK == response.getStatusCode()) {
                            final String jsonString = response.getText();
                            if (jsonString == null || jsonString.trim().length() == 0) {
                                teaserReady(null);
                            }
                            else {
                                JSONObject jsonObj = JSONParser.parseStrict(jsonString).isObject();
                                if (jsonObj == null) {
                                    teaserReady(null);
                                }
                                else {
                                    teaserReady(jsonObj);
                                }
                            }
                        }
                        else {
                            Firebug.debug("<TeaserRequest> couldn't get JSON (" + response.getStatusText() + ")"); // $NON-NLS$
                        }
                    }
                });
            } catch (Exception ex) {
                Firebug.error("<TeaserRequest> exception while reading form data for teaser", ex); // $NON-NLS$
            }
        }

        public abstract void teaserReady(JSONObject teaser);
    }

}
