package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.input.CheckBox;
import de.marketmaker.itools.gwtutil.client.widgets.input.Radio;
import de.marketmaker.itools.gwtutil.client.widgets.input.RadioGroup;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.Version;
import de.marketmaker.iview.mmgwt.mmweb.client.as.AsMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PmSessionTimeoutWatchdog;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.PmAsyncManager;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.ObjectMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.BlockAndTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.PortfolioPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.ServiceStatistics;
import de.marketmaker.iview.pmxml.FunctionDesc;
import de.marketmaker.iview.pmxml.FunctionListResponse;
import de.marketmaker.iview.pmxml.VoidRequest;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * User: umaurer
 * Date: 12.06.13
 * Time: 15:58
 */
@NonNLS
@SuppressWarnings("UnusedAssignment")
public class DeveloperToolsPanel extends Composite {
    private CheckBox pmSessionWatchdogStartedCheckBox;
    private CheckBox pmSessionWatchdogDebugCheckBox;
    private Panel privacyModeToolsPanel;

    public static PopupPanel createPopup() {
        final DeveloperToolsPanel toolsPanel = new DeveloperToolsPanel();
        final PopupPanel popup = new PopupPanel(true, true) {
            @Override
            public void setVisible(boolean visible) {
                super.setVisible(visible);
                toolsPanel.tryUpdatePmSessionWatchdogStartedCheckBox();
                toolsPanel.updatePrivacyModeTools();
            }
        };
        popup.setStyleName("mm-devTools-popup");

        popup.setWidget(toolsPanel);
        return popup;
    }

    public DeveloperToolsPanel() {
        final FlowPanel panel = new FlowPanel();

        panel.setStyleName("mm-devTools");

        panel.add(createHeader("Firebug"));
        panel.add(createFirebugTools());

        panel.add(createHeader("LogWindow"));
        panel.add(createLogWindowTools());

        panel.add(createHeader("Edit PM Portfolio"));
        panel.add(createPortfolioEditTools());

        panel.add(createHeader("InstanceLog"));
        panel.add(createInstanceLogTools());

        panel.add(createHeader("History"));
        panel.add(createHistoryTools());

        panel.add(createHeader("Statistics"));
        panel.add(createStatisticsTools());

        panel.add(createHeader("Version"));
        panel.add(createVersionTools());

        panel.add(createHeader("User Properties"));
        panel.add(createUserPropertiesTools());

        panel.add(createHeader("Comet"));
        panel.add(createCometTools());

        panel.add(createHeader("PrivacyMode"));
        panel.add(createPrivacyModeTools());

        if(AbstractMainController.INSTANCE instanceof AsMainController) {
            panel.add(createHeader("PM Session Timeout Watchdog"));
            final AsMainController asMainController = (AsMainController) AbstractMainController.INSTANCE;
            panel.add(createPmSessionWatchdogTools(asMainController.getPmSessionTimeoutWatchdog()));
        }

        initWidget(panel);
    }

    private Widget createPrivacyModeTools() {
        this.privacyModeToolsPanel = new FlowPanel();
        updatePrivacyModeTools();
        return this.privacyModeToolsPanel;
    }

    private void updatePrivacyModeTools() {
        this.privacyModeToolsPanel.clear();

        this.privacyModeToolsPanel.add(new Label("Privacy mode active? " + PrivacyMode.isActive()));
        if(PrivacyMode.isActive()) {
            this.privacyModeToolsPanel.add(Button.text("Log allowed object IDs").clickHandler(clickEvent -> logPrivacyModeAllowedObjects()).build());
        }

        this.privacyModeToolsPanel.add(new Label("Subscriptions: " + PrivacyMode.getSubscriptionsCount()));
    }

    private void logPrivacyModeAllowedObjects() {
        final Set<String> allowedObjectIds = PrivacyMode.getAllowedObjectIds();
        if(allowedObjectIds.isEmpty()) {
            Firebug.info("No Objects allowed in privacy mode");
            return;
        }

        final DmxmlContext context = new DmxmlContext();
        context.setCancellable(false);

        final ArrayList<BlockAndTalker<ObjectMetadata.DbIdTalker, ObjectMetadata, ObjectMetadata>> bats = new ArrayList<>();
        for (String id : allowedObjectIds) {
            final BlockAndTalker<ObjectMetadata.DbIdTalker, ObjectMetadata, ObjectMetadata> bat = new BlockAndTalker<>(context, new ObjectMetadata.DbIdTalker());
            bat.setDatabaseId(id);
            bat.getBlock().setToBeRequested();
            bats.add(bat);
        }

        context.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                final HashMap<String, String> idToName = new HashMap<>();
                for (BlockAndTalker<ObjectMetadata.DbIdTalker, ObjectMetadata, ObjectMetadata> bat : bats) {
                    if(!bat.getBlock().isResponseOk()) {
                        idToName.put(bat.getDatabaseId(), "BLOCK ERROR");
                        continue;
                    }
                    final ObjectMetadata om = bat.createResultObject();
                    final String value = om != null
                            ? PmRenderers.SHELL_MM_TYPE.render(om.getType()) + ", \"" + om.getName() + "\""
                            : "null";
                    idToName.put(bat.getDatabaseId(), value);
                }
                Firebug.logAsGroup("Object IDs allowed in privacy mode", idToName);
            }
        });
    }

    private Widget createPortfolioEditTools() {
        final FlowPanel panel = new FlowPanel();
        addUserPropertyCheckBox(panel, PortfolioPortraitController.EDIT_PORTFOLIO_ALLOWED_AS);
        addUserPropertyCheckBox(panel, "showPortfolioAndVersionIDs");
        return panel;
    }

    private Widget createFirebugTools() {
        final FlowPanel panel = new FlowPanel();
        addUserPropertyCheckBox(panel, "fbRequest");
        addUserPropertyCheckBox(panel, "fbResponse");
        addUserPropertyCheckBox(panel, "fbPending");
        addNewline(panel);
        addUserPropertyCheckBox(panel, "fbSpsTasks");
        addUserPropertyCheckBox(panel, "fbSpsPropsBeforeTryToSave");
        addUserPropertyCheckBox(panel, "fbSpsWidgetOverviewMock");
        addUserPropertyCheckBox(panel, "fbValidationMocker");
        addNewline(panel);
        addUserPropertyCheckBox(panel, "pullHandler");

        addButton(panel, "Internal_FunctionList", event -> logPmFunctions());
        return panel;
    }

    private Widget createPmSessionWatchdogTools(final PmSessionTimeoutWatchdog watchdog) {
        final Panel panel = new FlowPanel();

        this.pmSessionWatchdogStartedCheckBox =
                addCheckBox(panel, "Run watchdog", watchdog.isStarted(), event -> {
                    final Boolean value = event.getValue();
                    if (value == null || !value) {
                        watchdog.stop();
                    } else {
                        if (!watchdog.isStarted()) {
                            watchdog.start();
                        }
                    }
                });
        this.pmSessionWatchdogDebugCheckBox = addCheckBox(panel, "Run debug countdown timer", watchdog.isDebugCountdownStarted(),
                event -> {
                    final Boolean value = event.getValue();
                    if (value == null || !value) {
                        watchdog.stopDebugCountdown();
                    } else {
                        if (!watchdog.isDebugCountdownStarted()) {
                            watchdog.startDebugCountdown();
                        }
                    }
                });
        final Label countDownLabel = new Label(getApproxTimeToNextRequestText(null));
        panel.add(countDownLabel);
        watchdog.addValueChangeHandler(event -> {
            final Long value = event.getValue();
            countDownLabel.setText(getApproxTimeToNextRequestText(value));
        });
        return panel;
    }

    protected String getApproxTimeToNextRequestText(Long value) {
        return "approx. time to next request: " + (value != null ? (value / 1000) + "s" : "--");
    }

    private void tryUpdatePmSessionWatchdogStartedCheckBox() {
        if(this.pmSessionWatchdogStartedCheckBox != null
                && this.pmSessionWatchdogDebugCheckBox != null
                && AbstractMainController.INSTANCE instanceof AsMainController) {
            final PmSessionTimeoutWatchdog watchdog = ((AsMainController) AbstractMainController.INSTANCE).getPmSessionTimeoutWatchdog();
            this.pmSessionWatchdogStartedCheckBox.setChecked(watchdog.isStarted(), false);
            this.pmSessionWatchdogDebugCheckBox.setChecked(watchdog.isDebugCountdownStarted(), false);
        }
    }

    private Widget createHeader(String text) {
        final Label label = new Label(text);
        label.setStyleName("mm-devTools-header");
        return label;
    }

    private Widget createLogWindowTools() {
        final FlowPanel panel = new FlowPanel();
        addButton(panel, "show", event -> LogWindow.show());
        addButton(panel, "user", event -> LogWindow.showUserData());
        addCheckBox(panel, "response: ", event -> LogWindow.setWithResponse(event.getValue()));
        addButton(panel, "logToServer", event -> DebugUtil.logToServer("DeveloperToolsPanel.logToServer()"));
        return panel;
    }

    private Widget createHistoryTools() {
        final FlowPanel panel = new FlowPanel();
        addButton(panel, "show", event -> AbstractMainController.INSTANCE.openHistoryThreadLog());
        return panel;
    }

    private Widget createStatisticsTools() {
        final FlowPanel panel = new FlowPanel();
        addButton(panel, "show", event -> StatisticsWindow.show());
        addButton(panel, "reset", event -> ServiceStatistics.getOrCreate().reset());
        return panel;
    }

    private Widget createInstanceLogTools() {
        final FlowPanel panel = new FlowPanel();
        final RadioGroup<InstanceLog.Type> radioGroup = new RadioGroup<>();
        radioGroup.addValueChangeHandler(event -> InstanceLog.setType(event.getValue()));
        addRadio(panel, "Silent: ", radioGroup, InstanceLog.Type.Silent, true);
        addRadio(panel, "Firebug: ", radioGroup, InstanceLog.Type.Firebug, false);
        addRadio(panel, "LogWindow: ", radioGroup, InstanceLog.Type.LogWindow, false);
        return panel;
    }

    private Widget createVersionTools() {
        final FlowPanel panel = new FlowPanel();
        addButton(panel, "show", event -> Window.alert(Version.INSTANCE.build()));
        return panel;
    }

    private Widget createUserPropertiesTools() {
        final FlowPanel panel = new FlowPanel();
        addUserPropertyCheckBox(panel, "iqdata");
        addUserPropertyCheckBox(panel, "vwdcode");
        addUserPropertyCheckBox(panel, "readonly");

        panel.add(createUserPropertyEditor());

        return panel;
    }

    private IsWidget createUserPropertyEditor() {
        final TextBox propertyNameBox = new TextBox();
        final TextArea propertyValueBox = new TextArea();

        final Button buttonGetProperty = Button.text("Get Property").clickHandler(clickEvent -> {
            final String propertyName = propertyNameBox.getText();
            if (StringUtil.hasText(propertyName)) {
                propertyValueBox.setValue(SessionData.INSTANCE.getUser().getAppConfig().getProperty(propertyName));
            }
        }).build();

        final Button buttonSetProperty = Button.text("Set Property").clickHandler(clickEvent -> {
            final String propertyName = propertyNameBox.getText();
            final String propertyValue = propertyValueBox.getValue();
            if (StringUtil.hasText(propertyName)) {
                if (StringUtil.hasText(propertyValue)) {
                    SessionData.INSTANCE.getUser().getAppConfig().addProperty(propertyName, propertyValue);
                }
                else {
                    SessionData.INSTANCE.getUser().getAppConfig().addProperty(propertyName, null);
                }
            }
        }).build();

        final Grid grid = new Grid(3, 2);
        grid.setText(0, 0, "Property Name");
        grid.setWidget(0, 1, propertyNameBox);
        grid.setText(1, 0, "Property Value");
        grid.setWidget(1, 1, propertyValueBox);
        grid.setWidget(2, 0, buttonGetProperty);
        grid.setWidget(2, 1, buttonSetProperty);
        return grid;
    }

    private void addUserPropertyCheckBox(Panel panel, final String propertyName) {
        final CheckBox checkBox = new CheckBox("true".equals(SessionData.INSTANCE.getUserProperty(propertyName)));
        checkBox.addValueChangeHandler(event -> SessionData.INSTANCE.getUser().getAppConfig().addProperty(propertyName, event.getValue()));
        checkBox.getElement().getStyle().setMarginRight(6, Style.Unit.PX);
        panel.add(new InlineLabel(propertyName + ":"));
        panel.add(checkBox);
    }

    private void addButton(Panel panel, String label, ClickHandler handler) {
        panel.add(Button.span().text(label)
                .clickHandler(handler)
                .build());
    }

    private CheckBox addCheckBox(Panel panel, String labelText, Boolean checked, ValueChangeHandler<Boolean> handler) {
        final CheckBox checkBox = new CheckBox(checked);
        checkBox.addValueChangeHandler(handler);
        panel.add(checkBox.createSpan(SafeHtmlUtils.fromString(labelText)));
        panel.add(checkBox);
        panel.add(new InlineLabel(" "));
        return checkBox;
    }

    private CheckBox addCheckBox(Panel panel, String labelText, ValueChangeHandler<Boolean> handler) {
        return addCheckBox(panel, labelText, false, handler);
    }

    private <V> void addRadio(Panel panel, String labelText, RadioGroup<V> radioGroup, V value, boolean checked) {
        final Radio<V> radio = radioGroup.add(value, checked);
        panel.add(radio.createSpan(SafeHtmlUtils.fromString(labelText)));
        panel.add(radio);
        panel.add(new InlineLabel(" "));

    }

    private void addNewline(FlowPanel panel) {
        panel.add(new Label());
    }

    private Widget createCometTools() {
        final FlowPanel panel = new FlowPanel();
        addButton(panel, "Pull Status", event -> PmAsyncManager.getInstance().pullStatus());
        addButton(panel, "StopPush", event -> PmAsyncManager.getInstance().stopAsync());
        return panel;
    }

    private void logPmFunctions() {
        final DmxmlContext dmxmlContext = new DmxmlContext();
        dmxmlContext.setCancellable(false);

        final DmxmlContext.Block<FunctionListResponse> pmBlock = dmxmlContext.addBlock("PM_FunctionList");
        final DmxmlContext.Block<FunctionListResponse> pmAsyncBlock = dmxmlContext.addBlock("PM_AsyncFunctionList");
        final DmxmlContext.Block<FunctionListResponse> oeBlock = dmxmlContext.addBlock("OE_FunctionList");
        pmBlock.setParameter(new VoidRequest());
        pmAsyncBlock.setParameter(new VoidRequest());
        oeBlock.setParameter(new VoidRequest());

        pmBlock.setToBeRequested();
        pmAsyncBlock.setToBeRequested();
        oeBlock.setToBeRequested();

        dmxmlContext.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.error("Echo requests failed", caught);
            }

            @Override
            public void onSuccess(ResponseType result) {
                if(pmBlock.isResponseOk()) {
                    logFunctionList("PM_FunctionList", pmBlock.getResult().getList());
                }
                if(pmAsyncBlock.isResponseOk()) {
                    logFunctionList("PM_AsyncFunctionList", pmAsyncBlock.getResult().getList());
                }
                if(oeBlock.isResponseOk()) {
                    logFunctionList("OE_FunctionList", oeBlock.getResult().getList());
                }
            }
        });
    }

    private void logFunctionList(String listName, List<FunctionDesc> functionDescs) {
        Firebug.groupStart(listName);
        for (FunctionDesc functionDesc : functionDescs) {
            Firebug.groupCollapsed(functionDesc.getKey(), "Req: " + functionDesc.getRequest(),
                    "Res: " + functionDesc.getResponse());
        }
        Firebug.groupEnd();
    }
}
