package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.GISReports;
import de.marketmaker.iview.dmxml.ReportType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;


import java.util.List;

public class DzPibMarginDialog implements AsyncCallback<ResponseType> {

    public static final DzPibMarginDialog INSTANCE = new DzPibMarginDialog();

    public static final String DYN_PIB_BASE_URL = "/dmxml-1/web/"; // $NON-NLS$

    private static final RegExp VALID_MARGIN = RegExp.compile("^\\d*[\\.|,]{0,1}\\d{0,3}$"); // $NON-NLS$

    private final Button confirmButton;
    private final Radio defaultMargin;
    private final TextBox textbox;
    private final Command command;
    private final Window window;
    private final DmxmlContext context = new DmxmlContext();
    private final DmxmlContext.Block<GISReports> blockGisReports;

    private JavaScriptObject pibWindow;

    private DzPibMarginDialog() {
        this.window = GuiUtil.createModalWindow(I18n.I.pibMarginInput(), 350, 140, false);
        this.window.setDraggable(true);
        this.blockGisReports = context.addBlock("GIS_Reports"); //$NON-NLS$

        final FocusPanel focusPanel = new FocusPanel();
        final ContentPanel contentPanel = new ContentPanel();
        final FlexTable flexTable = new FlexTable();
        focusPanel.addKeyDownHandler(new DialogKeyDownHandler());
        focusPanel.add(contentPanel);
        contentPanel.add(flexTable);
        contentPanel.setHeaderVisible(false);

        final RadioGroup marginGroup = new RadioGroup();
        this.defaultMargin = createRadio(I18n.I.standardMargin(), true);
        final Radio customMargin = createRadio(I18n.I.individualMargin(), false);

        marginGroup.add(this.defaultMargin);
        marginGroup.add(customMargin);
        this.textbox = new TextBox();

        this.command = new Command() {
            @Override
            public void execute() {
                String margin = "default"; // $NON-NLS$
                if (customMargin.getValue()) {
                    margin = textbox.getValue().replace(',','.');
                }
                window.setVisible(false);
                setUserConfirmedMargin(margin);
                textbox.setValue(""); // $NON-NLS$
                defaultMargin.setValue(true);
            }
        };

        this.confirmButton = new Button(I18n.I.ok(), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                // Need to open window before additional events are thrown/processed to make links
                // work on iOS devices. See http://jira.vwdkl.net/browse/ISTAR-592 for details
                pibWindow = openWindow("", "_blank", ""); //$NON-NLS$
                command.execute();
            }
        });

        final Button cancelButton = new Button(I18n.I.cancel(), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                window.setVisible(false);
                textbox.setValue("");  // $NON-NLS$
                defaultMargin.setValue(true);
            }
        });

        // validating the margin value and enable/disable the submit button
        this.textbox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                confirmButton.setEnabled(isValidMargin(textbox.getValue()));
                customMargin.setValue(true);
            }
        });
        this.textbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                boolean valid = isValidMargin(textbox.getValue());
                confirmButton.setEnabled(valid);
                if (valid && (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)) {
                    command.execute();
                }
            }
        });
        this.textbox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                customMargin.setValue(true);
            }
        });

        marginGroup.addListener(Events.Change, new Listener<BaseEvent>(){
            public void handleEvent(BaseEvent be) {
                if (customMargin.getValue()) {
                    confirmButton.setEnabled(isValidMargin(textbox.getValue()));
                }
                else {
                    textbox.setValue("");
                    confirmButton.setEnabled(true);
                }
            }
        });
        marginGroup.setValue(this.defaultMargin);

        flexTable.setWidget(0, 0, this.defaultMargin);
        flexTable.setWidget(1, 0, customMargin);
        flexTable.setWidget(1, 1, this.textbox);
        flexTable.setWidget(1, 2, new LabelField("%")); // $NON-NLS$
        this.window.add(focusPanel);

        this.window.addButton(this.confirmButton);
        this.window.addButton(cancelButton);
    }

    final private class DialogKeyDownHandler implements KeyDownHandler {
        @Override
        public void onKeyDown(KeyDownEvent event) {
            if(event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                window.setVisible(false);
                textbox.setValue("");  // $NON-NLS$
                defaultMargin.setValue(true);
            }
            if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER && confirmButton.isEnabled()) {
                pibWindow = openWindow("", "_blank", ""); //$NON-NLS$
                command.execute();
            }
        }
    }

    private boolean isValidMargin(String value) {
        if (!VALID_MARGIN.test(value)) {
            return false;
        }
        try {
            if (Float.parseFloat(value.replace(',', '.')) >= 100) {
                return false;
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    private Radio createRadio(String label, boolean value) {
        final Radio result = new Radio();
        result.setBoxLabel(label);
        result.setValue(value);
        return result;
    }

    public static IsWidget createTriggerWidget(final String symbol) {
        final AbstractImagePrototype aip = IconImage.get("mm-icon-dzbank-pib"); // $NON-NLS$
        final Image image = aip.createImage();
        image.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        image.setTitle(I18n.I.dzBankPibTooltip());
        image.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                DzPibMarginDialog.INSTANCE.showDialog(symbol);
            }
        });
        return image;
    }

    public void showDialog(String symbol) {
        this.blockGisReports.setParameter("filterStrategy", "DZBANK-PIB"); //$NON-NLS$
        this.blockGisReports.setParameter("symbol", symbol); //$NON-NLS$
        this.window.setVisible(true);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
                defaultMargin.focus();
            }
        });
    }

    private void setUserConfirmedMargin(String margin) {
        blockGisReports.setParameter("margin", margin); // $NON-NLS-0$
        blockGisReports.isToBeRequested();
        context.issueRequest(this);
    }

    @Override
    public void onSuccess(ResponseType responseType) {
        if (!blockGisReports.isResponseOk()) {
            Firebug.error("<DzPibMarginDialog> isResponseOk is false, blockGisReports is " + blockGisReports);
            return;
        }

        final GISReports result = blockGisReports.getResult();
        if(result == null) {
            Firebug.error("<DzPibMarginDialog> isResponseOk is false, blockGisReports is " + blockGisReports);
            return;
        }
        List<ReportType> reportTypes = result.getReport();
        if(reportTypes != null && !reportTypes.isEmpty()) {
            String url = reportTypes.get(0).getUrl();
            replaceWindowUrl(pibWindow, DYN_PIB_BASE_URL + url);
        }
        blockGisReports.removeParameter("margin");  // $NON-NLS$
        blockGisReports.removeParameter("symbol");  // $NON-NLS$
    }

    @Override
    public void onFailure(Throwable throwable) {
        Firebug.error("<DzPibMarginDialog> failure requesting, blockGisReports: " + blockGisReports);
    }

    private native JavaScriptObject openWindow(String url, String name, String features) /*-{
        var pibWindow = $wnd.open(url, name, features);
        return pibWindow;
    }-*/;

    private native void replaceWindowUrl(JavaScriptObject pibWindow, String url) /*-{
        pibWindow.location.replace(url);
    }-*/;

}
