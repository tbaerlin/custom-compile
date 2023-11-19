/*
 * LoginView.java
 *
 * Created on 29.04.2008 18:03:37
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData.VwdCustomerServiceContact;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.client.logging.Logger;
import de.marketmaker.iview.mmgwt.mmweb.client.util.*;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author mdick
 */
public class LoginView {
    private static final String VERSION_ELEMENT_ID = "mmLoginVersion";  // $NON-NLS$
    private String resetErrorText = null;
    private static LoginView INSTANCE;

    private Logger logger = Ginjector.INSTANCE.getLogger();

    public LoginView() {
        showVersion();
        requestEnvInfo();
        showMessageOfTheDay();
        INSTANCE = this;
        initializeForm();
        initializeFormWidgets();
        resetError();
        if ("true".equals(getValue("mmLoginSubmitted")) // $NON-NLS$
                && (SessionData.isAsDesign() || StringUtil.hasText(getValue("mmLoginPassword")))) { // $NON-NLS$
            MessageOfTheDayEditor.setActive(RootPanel.get("message_of_the_day") != null); // $NON-NLS$
            login();
        }
    }

    private void showMessageOfTheDay() {
        final RootPanel motdRootPanel = RootPanel.get("message_of_the_day"); // $NON-NLS$
        if (motdRootPanel == null) {
            Firebug.log("no message_of_the_day");
            return;
        }

        MessageOfTheDayEditor.setActive(true);

        UserServiceAsync.App.getInstance().getMessageOfTheDayByDate(new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.error("cannot get message of the day", caught);
            }

            @Override
            public void onSuccess(String motd) {
                showMessageOfTheDay(motd);
            }
        });
    }

    private void showVersion() {
        this.logger.info("<LoginView.showVersion>");
        final Element versionElement = DOM.getElementById(VERSION_ELEMENT_ID);
        if (versionElement != null) {
            versionElement.setInnerText(Version.INSTANCE.build());
            return;
        }
        final Label version = new Label(Version.INSTANCE.build());
        version.getElement().setId(VERSION_ELEMENT_ID);
        RootPanel.get().add(version);
    }

    private void showMessageOfTheDay(String motd) {
        if (motd == null) {
            return;
        }

        final RootPanel motdRootPanel = RootPanel.get("message_of_the_day"); // $NON-NLS$
        final HTML html = new HTML(motd);
        //noinspection GWTStyleCheck
        motdRootPanel.add(html);
        motdRootPanel.getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
        AnimationFactory.createAnimation("fadeIn(2000)", motdRootPanel).run(); // $NON-NLS$
    }

    private void initializeFormWidgets() {
        final RootPanel mmLoginSendPasswordContainer = RootPanel.get("mmLoginSendPasswordContainer"); // $NON-NLS$
        if (mmLoginSendPasswordContainer == null) {
            return;
        }
        final InlineLabel labelSendPassword = new InlineLabel(I18n.I.forgotPassword());
        labelSendPassword.setStyleName("mm-login-sendPassword"); // $NON-NLS$
        labelSendPassword.addClickHandler(event -> showSendPasswordDialog());
        //noinspection GwtToHtmlReferences
        mmLoginSendPasswordContainer.add(labelSendPassword);
    }

    private native void initializeForm() /*-{
        $wnd.mmLoginOnSubmit = @de.marketmaker.iview.mmgwt.mmweb.client.LoginView::mmLoginOnSubmit();
        $wnd.mmLoginResetError = @de.marketmaker.iview.mmgwt.mmweb.client.LoginView::mmLoginResetError();
        $wnd.mmShowLoginHelp = @de.marketmaker.iview.mmgwt.mmweb.client.LoginView::mmShowLoginHelp();
        $wnd.mmShowSendPasswordDialog = @de.marketmaker.iview.mmgwt.mmweb.client.LoginView::mmShowSendPasswordDialog();
        @de.marketmaker.iview.mmgwt.mmweb.client.LoginView::enableButton(Ljava/lang/String;)("mmLoginSubmit");
        @de.marketmaker.iview.mmgwt.mmweb.client.LoginView::enableButton(Ljava/lang/String;)("mmLoginHelp");

        // remove "shaking" after animationend event
        var loginFrameElement = $doc.getElementById("mmLoginFrame"); // $NON-NLS$
        if (loginFrameElement != null) {
            var animationEndEventName = @de.marketmaker.itools.gwtutil.client.util.Transitions::ANIMATION_END_EVENT_NAME;
            try {
                loginFrameElement.addEventListener(animationEndEventName, function () {
                    loginFrameElement.className = "loginFrame"; // $NON-NLS$
                }, false);
            }
            catch (err) {
                console.log('cannot register "' + animationEndEventName + '" listener to loginFrameElement -> ignore'); // $NON-NLS$
            }
        }
    }-*/;

    public static void mmLoginOnSubmit() {
        INSTANCE.login();
    }

    public static void mmLoginResetError() {
        INSTANCE.resetError();
    }

    public static void mmShowLoginHelp() {
        INSTANCE.showLoginHelp();
    }

    public static void mmShowSendPasswordDialog() {
        INSTANCE.showSendPasswordDialog();
    }


    public static void enableButton(String buttonId) {
        final Element eltButton = DOM.getElementById(buttonId);
        if (eltButton != null) {
            final String style = eltButton.getClassName();
            eltButton.setClassName(style.replace(" x-item-disabled", "")); // $NON-NLS$
        }
    }

    private void login() {
        final String username = getValue("mmLoginUsername"); // $NON-NLS$
        final String password = getValue("mmLoginPassword"); // $NON-NLS$
        setResetErrorText(I18n.I.loginUser() + ": " + username); // $NON-NLS$
        resetError();

        login(username, password, new LoginCallback() {
            @Override
            public void onError(String message) {
                showErrorAndReset(message);
            }

            @Override
            public void onSuccess(User user, String message) {
                removeLoginForm();
                SessionData.INSTANCE.setUser(user);
                AbstractMainController.INSTANCE.runInitSequence();
                if (StringUtil.hasText(message)) {
                    AbstractMainController.INSTANCE.showError(I18n.I.loginProblem() + " " + message);
                }
            }
        });
    }

    public interface LoginCallback {
        void onError(String message);

        void onSuccess(User user, String message);
    }

    public interface LoginStrategy {
        void handleUserResponse(UserResponse response);
    }

    public static void login(final String username, String password, final LoginCallback loginCallback) {
        final String module = GuiDefsLoader.getModuleName();
        final UserRequest userRequest = LoginCommand.createUserRequest(username, password, module, JsUtil.getScreenInfo(), I18n.I.locale());
        UserServiceAsync.App.getInstance().login(userRequest, new AsyncCallback<UserResponse>() {
            public void onFailure(Throwable throwable) {
                loginCallback.onError(I18n.I.loginFailed());
                DebugUtil.logToServer("Anmeldung nicht erfolgreich", throwable); // $NON-NLS$
            }

            public void onSuccess(final UserResponse response) {
                getStrategy(loginCallback).handleUserResponse(response);
            }
        });
    }

    private static LoginStrategy getStrategy(final LoginCallback loginCallback) {
        if (SessionData.isWithPmBackend()) {
            return createPmLoginStrategy(loginCallback);
        }
        return createMmfLoginStrategy(loginCallback);
    }

    private static LoginStrategy createPmLoginStrategy(final LoginCallback loginCallback) {
        return response -> {
            if (!(response instanceof PmUserResponse)) {
                throw new IllegalStateException("response must be instanceof PmUserResponse");  // $NON-NLS$
            }
            final User user = response.getUser();
            switch (((PmUserResponse) response).getPmState()) {
                case SLR_OK:
                    loginCallback.onSuccess(user, resolveMessage(response));
                    break;
                case SLR_OK_BUT_PASSWORD_EXPIRED:
                    loginCallback.onSuccess(user, null);
                    break;
                default:
                    loginCallback.onError(resolveMessage(response));
            }
        };
    }

    private static LoginStrategy createMmfLoginStrategy(final LoginCallback loginCallback) {
        return response -> {
            switch (response.getState()) {
                case OK:
                    loginCallback.onSuccess(response.getUser(), null);
                    break;
                default:
                    loginCallback.onError(resolveMessage(response));
            }
        };
    }

    private static String resolveMessage(UserResponse response) {
        switch (response.getState()) {
            case OK:
                return null;
            case PASSWORD_EXPIRED:
                return I18n.I.passwordExpired();
            case WRONG_PASSWORD:
                return I18n.I.invalidUserOrPassword();
            case WRONG_INITIAL_PASSWORD:
                I18n.I.invalidInitialPassword();
            case UNKNOWN_USER:
                return I18n.I.invalidUserOrPassword();
            case INACTIVE_USER:
                return I18n.I.userAccountNotActive();
            case INVALID_PRODUCT:
                return I18n.I.productNotAllowed();
            case INVALID_VWDID:
                return I18n.I.vwdIdNotAllowed();
            case INTERNAL_ERROR:
                return I18n.I.internalError();
            case LICENSE_INVALID:
                return I18n.I.invalidLicense();
            default:
                return "unknown state"; // $NON-NLS$
        }
    }

    private native String getValue(String fieldId) /*-{
        return $doc.getElementById(fieldId).value;
    }-*/;

    private void removeLoginForm() {
        removeElementById(VERSION_ELEMENT_ID);
        removeElementById("mmLoginRoot");  // $NON-NLS$
    }

    private void removeElementById(String id) {
        final Element element = DOM.getElementById(id);
        if (element != null) {
            element.removeFromParent();
        }
    }

    private void showErrorAndReset(final String text) {
        setResetErrorText(null);
        DOM.getElementById("mmLoginResult").setInnerText(text); // $NON-NLS$
        final Element mmLoginFrame = DOM.getElementById("mmLoginFrame"); // $NON-NLS$
        if (mmLoginFrame != null) {
            mmLoginFrame.addClassName("shaking"); // $NON-NLS$ shake it babe!
        }
        reset();
    }

    private void setResetErrorText(String resetErrorText) {
        this.resetErrorText = resetErrorText;
    }

    public void resetError() {
        if (this.resetErrorText == null) {
            DOM.getElementById("mmLoginResult").setInnerHTML("&nbsp;"); // $NON-NLS$
        }
        else {
            DOM.getElementById("mmLoginResult").setInnerText(this.resetErrorText); // $NON-NLS$
        }
    }

    private native void reset() /*-{
        $doc.getElementById("mmLoginForm").reset(); // $NON-NLS$
        $doc.getElementById("mmLoginUsername").focus(); // $NON-NLS$
    }-*/;

    private boolean isDzWgz() {
        // cannot use Customer.INSTANCE.isDzWgzApoKwt, since guidefs is not loaded yet
        final String moduleName = GuiDefsLoader.getModuleName();
        return "web".equals(moduleName); // $NON-NLS$
    }

    private boolean isApoKwt() {
        // cannot use Customer.INSTANCE.isDzWgzApoKwt, since guidefs is not loaded yet
        final String moduleName = GuiDefsLoader.getModuleName();
        return "apobank".equals(moduleName) || "kwt".equals(moduleName); // $NON-NLS$
    }

    private boolean isLbbw() {
        final String moduleName = GuiDefsLoader.getModuleName();
        return "lbbw-mmf3".equals(moduleName); // $NON-NLS$
    }

    private boolean isAdvisorySolutionWithPmLogin() {
        return SessionData.isAsDesign() && SessionData.isWithPmBackend();
    }

    public void showLoginHelp() {
        final DialogIfc dialogShortDescription = createDialog()
                .withTitle(I18n.I.passwordRules())
                .withCloseButton();
        dialogShortDescription.withDefaultButton(I18n.I.ok(), dialogShortDescription::closePopup);

        final FlowPanel panel = new FlowPanel();

        if (isDzWgz()) {
            panel.add(new HTML(I18n.I.passwordRulesDescriptionShortDzWgz()));
        }
        else if (isApoKwt()) {
            panel.add(new HTML(I18n.I.passwordRulesDescriptionShortApoKwt()));
        }
        else if (isLbbw()) {
            panel.add(new HTML(I18n.I.passwordRulesDescriptionShortLbbw()));
        }
        else if (isAdvisorySolutionWithPmLogin()) {
            //TODO: If Advisory Solution uses PM Server to authenticate,
            //TODO: we should display the password rules according to PM's
            //TODO: password config.
            final SessionData.VwdCustomerServiceContact c = SessionData.INSTANCE.getPmCustomerServiceContact();
            panel.add(new HTML(I18n.I.passwordRulesDescriptionShortAdvisorySolutionWithPmLogin(c.getName(), c.getEmail(), c.getPhone(), c.getFax())));
        }
        else {
            final SessionData.VwdCustomerServiceContact c = SessionData.INSTANCE.getFirstVwdCustomerServiceContact();
            panel.add(new HTML(I18n.I.passwordRulesDescriptionShort(c.getEmail(), c.getPhone(), c.getFax())));
        }

        if (!isAdvisorySolutionWithPmLogin()) {
            final Label lbLongLink = new Label(I18n.I.detailedExplanation());
            lbLongLink.setStyleName("mm-passwordrules"); // $NON-NLS$
            lbLongLink.addClickHandler(event -> {
                final DialogIfc dialogLongDescription = createDialog()
                        .withTitle(I18n.I.passwordRules())
                        .withCloseButton();
                dialogLongDescription.withDefaultButton(I18n.I.ok(), dialogLongDescription::closePopup);

                final FlowPanel panel1 = new FlowPanel();
                panel1.add(new HTML(I18n.I.passwordRulesDescriptionLong()));
                dialogLongDescription.withWidget(panel1);

                dialogShortDescription.closePopup();
                dialogLongDescription.show();
            });
            panel.add(lbLongLink);
        }

        dialogShortDescription.withWidget(panel);
        dialogShortDescription.show();
    }

    protected DialogIfc createDialog() {
        if(SessionData.isAsDesign()) {
            Document.get().getBody().addClassName("asView"); // $NON-NLS$
        }
        return Dialog.getImpl().createDialog();
    }

    public void showSendPasswordDialog() {
        final DialogIfc dialog = createDialog()
                .withTitle(I18n.I.changePassword())
                .withCloseButton();

        final FlowPanel panel = new FlowPanel();

        final String passwordDialogText;
        if (isDzWgz()) {
            passwordDialogText = I18n.I.sendPasswordDialogTextDzWgz();
        }
        else if (isApoKwt()) {
            passwordDialogText = I18n.I.sendPasswordDialogTextApoKwt();
        }
        else if (isLbbw()) {
            passwordDialogText = I18n.I.sendPasswordDialogTextLbbw();
        }
        else if (isAdvisorySolutionWithPmLogin()) {
            final SessionData.VwdCustomerServiceContact c = SessionData.INSTANCE.getPmCustomerServiceContact();
            passwordDialogText = I18n.I.sendPasswordDialogTextAdvisorySolutionWithPmLogin(c.getName(), c.getEmail(), c.getPhone(), c.getFax());
        }
        else {
            final VwdCustomerServiceContact c = getVwdCustomerServiceContact();

            final StringBuilder sb = new StringBuilder("<table>"); // $NON-NLS$
            CustomerServiceUtil.customerServiceAddressRowsBuilder(sb, c);
            sb.append("</table>"); // $NON-NLS$
            passwordDialogText = I18n.I.sendPasswordDialogText(c.getName(), sb.toString());
        }
        panel.add(new HTML(passwordDialogText));

        if (!isAdvisorySolutionWithPmLogin()) {
            final String username = getValue("mmLoginUsername"); // $NON-NLS$
            final TextBox textBoxUsername = new TextBox();
            textBoxUsername.setValue(username);
            final Grid grid = new Grid(1, 2);
            grid.setText(0, 0, I18n.I.loginLabelUser() + (SessionData.isAsDesign() ? "" : ":")); // $NON-NLS$
            grid.setWidget(0, 1, textBoxUsername);
            panel.add(grid);

            dialog.withButton(I18n.I.sendPasswordDialogButton(), () -> {
                sendPassword(textBoxUsername.getValue());
                dialog.closePopup();
            });
            dialog.withDefaultButton(I18n.I.cancel(), dialog::closePopup);
        }
        else {
            dialog.withDefaultButton(I18n.I.ok(), dialog::closePopup);
        }

        dialog.withWidget(panel);
        dialog.show();
    }


    private VwdCustomerServiceContact getVwdCustomerServiceContact() {
        // get contact information according to the zone and choose the one with key 'default' if no matching item was found

        VwdCustomerServiceContact cDefault = null;
        final String moduleName = GuiDefsLoader.getModuleName();
        final JsArray<VwdCustomerServiceContact> vwdCustomerServiceContacts = SessionData.INSTANCE.getVwdCustomerServiceContacts();
        for (int i = 0; i < vwdCustomerServiceContacts.length(); i++) {
            final VwdCustomerServiceContact aContact = vwdCustomerServiceContacts.get(i);
            if(moduleName.equals(aContact.getKey())) {
                return vwdCustomerServiceContacts.get(i);
            }
            if("default".equals(aContact.getKey())) { // $NON-NLS$
                cDefault = vwdCustomerServiceContacts.get(i);
            }
        }
        return cDefault;
    }

    public void sendPassword(final String username) {
        final String module = GuiDefsLoader.getModuleName();

        UserServiceAsync.App.getInstance().requestPasswordReset(username, module, I18n.I.locale(), new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
                Firebug.log("Error UserService.requestPasswordReset: " + caught.getMessage()); // $NON-NLS$
                DebugUtil.logToServer("Error UserService.requestPasswordReset", caught); // $NON-NLS$
                Dialog.error(I18n.I.sendPasswordResponseHeader(), I18n.I.sendPasswordResponseFailed());
            }

            public void onSuccess(String emailAddress) {
                final DialogIfc dialog = createDialog()
                        .withTitle(I18n.I.sendPasswordResponseHeader())
                        .withCloseButton();

                final FlowPanel panel = new FlowPanel();
                final String message = GetPasswordOkMessageString();
                panel.add(new HTML(message));

                dialog.withWidget(panel);
                dialog.show();
            }
        });
    }

    private String GetPasswordOkMessageString() {
        String message;
        if(isDzWgz()) {
            message = I18n.I.sendPasswordResponseOkDzWgz();
        }
        else if(isApoKwt()) {
            message = I18n.I.sendPasswordResponseOkApoKwt();
        }
        else if(isLbbw()) {
            message = I18n.I.sendPasswordResponseOkLbbw();
        }
        else if (isAdvisorySolutionWithPmLogin()) {
            final SessionData.VwdCustomerServiceContact c = SessionData.INSTANCE.getPmCustomerServiceContact();
            message = I18n.I.sendPasswordResponseOkAdvisorySolutionWithPmLogin(c.getName(), c.getEmail(), c.getPhone(), c.getFax());
        }
        else {
            final SessionData.VwdCustomerServiceContact c = SessionData.INSTANCE.getFirstVwdCustomerServiceContact();
            message = I18n.I.sendPasswordResponseOk(c.getEmail(), c.getPhone(), c.getFax());
        }
        return message;
    }


    public static void requestEnvInfo() {
        Firebug.log("LoginView.requestEnvInfo()");
        UserServiceAsync.App.getInstance().getEnvInfo(new AsyncCallback<Map<String, String>>() {
            public void onFailure(Throwable caught) {
                Firebug.warn("Error in getEnvInfo: " + caught.getMessage()); // $NON-NLS$
                if (PhoneGapUtil.isPhoneGap()) {
                    if (caught.getMessage().contains("serializationPolicyFile not found")) { // $NON-NLS$
                        com.google.gwt.user.client.Window.alert(I18n.I.appVersionMismatch());
                    }
                    else {
                        com.google.gwt.user.client.Window.alert(I18n.I.appServerCommunicationProblem());
                    }
                }
            }

            public void onSuccess(final Map<String, String> response) {
                Firebug.debug("Environment: "); // $NON-NLS$
                for (String s : response.keySet()) {
                    Firebug.debug(s + ": " + response.get(s));
                }
                if (response.containsKey("error")) { // $NON-NLS$
                    com.google.gwt.user.client.Window.alert(response.get("error")); // $NON-NLS$
                }
                final RootPanel versionPanel = RootPanel.get(VERSION_ELEMENT_ID);
                if (SessionData.isWithPmBackend()) {
                    versionPanel.setTitle(
                            "vwd advisory solution: " + response.get("asVersion") // $NON-NLS$
                                    + "\nvwd portfolio manager: " + response.get("pmVersion") // $NON-NLS$
                                    + "\nPortfolio Sync Interface: " + response.get("psiVersion")); // $NON-NLS$
                    SessionData.INSTANCE.setPmCustomerServiceContact(response);

                    final String sharedEnvName = response.get("sharedEnvName"); // $NON-NLS$
                    if("true".equals(response.get("sharedEnvColorSchema"))) {   // $NON-NLS$
                        versionPanel.addStyleName("sharedEnvColorSchema");  // $NON-NLS$
                    }
                    if (StringUtil.hasText(sharedEnvName)) {
                        final Element vpe = versionPanel.getElement();
                        vpe.setInnerText(vpe.getInnerText() + " " + sharedEnvName);  // $NON-NLS$
                        versionPanel.setTitle(versionPanel.getTitle() + "\n" + I18n.I.environment() + ": " + sharedEnvName); // $NON-NLS$
                    }
                }
                else {
                    versionPanel.setTitle("WebApp: " + response.get("asBuild")); // $NON-NLS$
                }
            }
        });
    }

    @Inject
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
