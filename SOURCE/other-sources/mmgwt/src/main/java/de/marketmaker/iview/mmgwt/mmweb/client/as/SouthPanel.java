package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.notification.NotificationBox;
import de.marketmaker.iview.mmgwt.mmweb.client.MainView;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.UserServiceAsync;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.AppName;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DeveloperToolsPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.GetEnvironmentResponse;

import java.util.Map;

import javax.inject.Inject;

/**
 * @author Ulrich Maurer
 *         Date: 20.03.13
 */
public class SouthPanel extends DockLayoutPanel {
    private final SessionData sessionData;

    private final HTML message = new HTML();
    private final Image developerTools;
    private final Label labelVersion = new Label();

    private PopupPanel versionBoxPopup;
    private Button buttonScale;

    private GetEnvironmentResponse envResponse = null;
    private Map<String, String> envInfo = null;

    @Inject
    public SouthPanel(SessionData sessionData, @AppName String appName) {
        super(Style.Unit.PX);

        this.sessionData = sessionData;

        setStyleName("as-southPanel"); // $NON-NLS$

        final PopupPanel popup = DeveloperToolsPanel.createPopup();
        this.developerTools = IconImage.get("as-south-developer").createImage(); // $NON-NLS$
        this.developerTools.addClickHandler(event -> popup.showRelativeTo(this.developerTools));
        addEast(this.developerTools, this.developerTools.getWidth());
        setDeveloperToolsVisibility();

        //noinspection Convert2MethodRef
        final NotificationBox messageTray = new NotificationBox(count -> I18n.I.nNotifications(count));
        addEast(messageTray, 200);

        if (CssUtil.getTransformStyle() != null) {
            addEast(this.buttonScale = Button.span().html("100% &rarr; 50%").clickHandler(new ClickHandler() { // $NON-NLS$
                boolean scale = true;

                @Override
                public void onClick(ClickEvent clickEvent) {
                    if (this.scale) {
                        MainView.getInstance().setScale(0.5f);
                        buttonScale.setHTML("50% &rarr; 100%"); // $NON-NLS$
                    }
                    else {
                        MainView.getInstance().resetScale();
                        buttonScale.setHTML("100% &rarr; 50%"); // $NON-NLS$
                    }
                    this.scale = !this.scale;
                }
            }).build(), 80);
        }

        if (Permutation.AS.isActive()) {
            doGetForAdvisorySolution(appName);
        }
        else {
            doGetForMmfAndGis(appName);
        }

        addWest(this.labelVersion, 350d);
        add(this.message);
    }

    private void doGetForMmfAndGis(final String appName) {
        UserServiceAsync.App.getInstance().getEnvInfo(new AsyncCallback<Map<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                DebugUtil.logToServer("could not get env!", throwable);
            }

            @Override
            public void onSuccess(Map<String, String> response) {
                envInfo = response;
                labelVersion.setText(appName);
                labelVersion.addClickHandler(event -> showVersionDetails(labelVersion));
                labelVersion.setStyleName("as-southText");
                labelVersion.addStyleName("mm-link");
            }
        });
    }

    private void doGetForAdvisorySolution(final String appName) {
        PmEnvSupport.requestPmEnv(new AsyncCallback<GetEnvironmentResponse>() {
            @Override
            public void onFailure(Throwable throwable) {
                DebugUtil.logToServer("could not get env!", throwable);
            }

            @Override
            public void onSuccess(GetEnvironmentResponse response) {
                envResponse = response;
                labelVersion.setText(appName + " Version " + (response.getWebgui() != null ? response.getWebgui().getCompleteVersion() : I18n.I.notAvailable("Version"))); // $NON-NLS$
                labelVersion.addClickHandler(event -> showVersionDetails(labelVersion));
                labelVersion.setStyleName("as-southText");
                labelVersion.addStyleName("mm-link");

                if (response.getNetRegIni().isSharedEnvColorSchema()) {
                    addStyleName("sharedEnvColorSchema");  // $NON-NLS$
                }
                final String sharedEnvName = response.getNetRegIni().getSharedEnvName();
                if (StringUtil.hasText(sharedEnvName)) {
                    labelVersion.setText(labelVersion.getText() + " " + sharedEnvName);  // $NON-NLS$
                }
            }
        });
    }

    private void showVersionDetails(final Label version) {
        if (this.versionBoxPopup == null) {
            this.versionBoxPopup = new PopupPanel(false, false);
            this.versionBoxPopup.setStyleName("mm-noteBox-popup");

            final FlowPanel panel = new FlowPanel();

            final Image close = new Image("clear.cache.gif"); // $NON-NLS$
            close.setStyleName("delete-all");
            close.addClickHandler(event -> this.versionBoxPopup.hide());
            panel.add(close);

            final User user = this.sessionData.getUser();
            panel.add(createVersionBoxSection(I18n.I.userDetails(), "vwdId: " + StringUtil.sOrDash(user.getVwdId()) // $NON-NLS$
                    , I18n.I.username() + ": " + StringUtil.sOrDash(user.getLogin())
                    , I18n.I.displayName() + ": " + StringUtil.sOrDash(user.getFirstName()) + " " + StringUtil.sOrDash(user.getLastName())
                    , I18n.I.loggedinSinceLabel() + ": " + this.sessionData.getLoggedInSinceString()));

            if (this.envResponse == null) {
                DebugUtil.logToServer("envResponse == null!");
            }
            else {
                panel.add(createVersionBoxSection(I18n.I.versionDetails(), PmEnvSupport.getVersionDetails(this.envResponse)));
                panel.add(createVersionBoxSection(I18n.I.modules(), PmEnvSupport.getModules(this.envResponse)));

                final String sharedEnvName = this.envResponse.getNetRegIni().getSharedEnvName();
                if (StringUtil.hasText(sharedEnvName)) {
                    panel.add(createVersionBoxSection(I18n.I.environment(), I18n.I.name() + ": " + sharedEnvName));  // $NON-NLS$
                }
            }

            if (this.envInfo != null) {
                panel.add(createVersionBoxSection(I18n.I.versionDetails(), this.envInfo));
            }

            if (this.sessionData.isDev()) {
                panel.add(createVersionBoxSection("Dev Info" // $NON-NLS$
                        , "Websockets aktiv: " + AsyncHandler.Factory.isWebsocketSupported() // $NON-NLS$
                ));
            }

            this.versionBoxPopup.setWidget(panel);
        }

        // the following code is necessary to display the popupPanel in the lower left corner
        this.versionBoxPopup.setVisible(false);
        this.versionBoxPopup.show();
        final Style style = this.versionBoxPopup.getElement().getStyle();
        style.setProperty("top", "auto"); // $NON-NLS$
        style.setProperty("right", "auto"); // $NON-NLS$
        style.setProperty("bottom", version.getOffsetHeight() + "px"); // $NON-NLS$
        style.setProperty("left", "0"); // $NON-NLS$
        this.versionBoxPopup.setVisible(true);
    }

    private IsWidget createVersionBoxSection(String header, Map<String, String> map) {
        final String[] strings = new String[map.size()];
        int i = 0;
        for (Map.Entry entry : map.entrySet()) {
            strings[i++] = entry.getKey() + ": " + entry.getValue();
        }
        return createVersionBoxSection(header, strings);
    }

    private Widget createVersionBoxSection(String header, PmEnvSupport.Entry... lines) {
        final String[] strings = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            strings[i] = lines[i].toString();
        }
        return createVersionBoxSection(header, strings);
    }

    private Widget createVersionBoxSection(String header, String... lines) {
        final FlowPanel panel = new FlowPanel();
        panel.setStyleName("mm-noteBox-message");
        panel.getElement().getStyle().setProperty("width", "auto"); // $NON-NLS$
        final Label lblHeader = new Label(header);
        lblHeader.setStyleName("header");
        panel.add(lblHeader);
        for (String line : lines) {
            panel.add(new Label(line));
        }
        return panel;
    }

    public void setDeveloperToolsVisibility() {
        this.developerTools.setVisible("true".equals(this.sessionData.getUserProperty("developer"))); // $NON-NLS$
    }

    public void setText(SafeHtml safeHtml) {
        this.message.setHTML(safeHtml);
    }

    public void setText(String text) {
        this.message.setText(text);
    }
}