package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBoxBase;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.suggestoracle.CamelCaseAndUnderscoreSuggestOracle;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.event.GuiEventHandler;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.event.ShowAboutEvent;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter.BlockOverviewPanel;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter.RequestParameterPanel;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter.XmlTree;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.widgets.AuthSelectBox;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 * @author Markus Dick
 */
public class DmxmlDocuGui {
    interface DmxmlDocuGuiUiBinder extends
            UiBinder<DockLayoutPanel, DmxmlDocuGui> {
    }

    private static DmxmlDocuGuiUiBinder ourUiBinder = GWT.create(DmxmlDocuGuiUiBinder.class);

    @UiField(provided = true)
    SuggestBox selectedBlockSuggestBox;

    @UiField
    AuthSelectBox authSelectBox;

    @UiField
    HTML firstSentenceBlockHTML;

    @UiField
    TextArea rawRequestTextArea;

    @UiField
    HTML blockDescriptionHTML;

    @UiField
    TextArea rawResponseTextArea;

    @UiField
    DockLayoutPanel panel;

    @UiField
    Button sendRequestButton;

    @UiField
    HTML parametersDescriptionHTML;

    @UiField
    HTML blockNameHTML;

    @UiField
    HTML responseElementDocuXml;

    @UiField
    Button logoutButton;

    @UiField
    Button aboutButton;

    @UiField
    Image requestStatusImage;

    @UiField
    Image responseStatusImage;

    @UiField
    RequestParameterPanel requestParameterPanel;

    @UiField
    XmlTree xmlTree;

    @UiField
    BlockOverviewPanel blockOverviewPanel;

    @UiField
    TabLayoutPanel topTabPanel;

    @UiField
    HTML supplementaryInformation;

    @UiField
    Hyperlink aboutLink;

    protected CamelCaseAndUnderscoreSuggestOracle selectedBlockSuggestOracle = new CamelCaseAndUnderscoreSuggestOracle();

    public final String statusOkImageUrl = "images/ok.svg"; // $NON-NLS$
    public final String statusErrorImageUrl = "images/cancel.svg";  // $NON-NLS$
    public final String emptyImageUrl = "empty.png";  // $NON-NLS$

    private final GuiEventHandler handler;

    private final HandlerManager eventBus;

    public DmxmlDocuGui(GuiEventHandler handler, HandlerManager eventBus) {
        //Currently embedded into HTML host page.
        //TODO: push to CssResource in the near future.
//        DOMUtil.loadStylesheet("DmxmlDocu.css"); // $NON-NLS$
//        DOMUtil.loadStylesheet("mm-theme.css"); // $NON-NLS$

        selectedBlockSuggestBox = new SuggestBox(selectedBlockSuggestOracle);
        selectedBlockSuggestBox.getValueBox().addFocusHandler(new SuggestBoxFocusHandler());

        ourUiBinder.createAndBindUi(this);
        this.handler = handler;

        this.eventBus = eventBus;
    }

/*
    @UiHandler("selectedBlockListBox")
    public void handleChange(ChangeEvent event) {
        final int i = this.selectedBlockListBox.getSelectedIndex();
        final String blockName = this.selectedBlockListBox.getItemText(i);
        handler.blockSelected(blockName);
    }
*/

    @UiHandler({"sendRequestButton", "logoutButton"})
    public void handleClick(ClickEvent event) {
        if (event.getSource() == sendRequestButton) {
            handler.sendRequestClicked();
        }
        else if (event.getSource() == logoutButton) {
            handler.logoutClicked();
        } else {
            Firebug.log("Click event from unknown source: " + event);
        }
    }

    @UiHandler({"aboutButton"})
    public void handleAboutButtonClick(ClickEvent event) {
        eventBus.fireEvent(new ShowAboutEvent());
    }

    @UiHandler({"aboutLink"})
    public void handleAboutLinkClick(ClickEvent event) {
        aboutLink.setTargetHistoryToken(History.getToken());
        eventBus.fireEvent(new ShowAboutEvent());
    }

    @UiHandler("selectedBlockSuggestBox")
    public void handleSuggestBoxSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
        final SuggestBox suggestBox = (SuggestBox)event.getSource();
        final String blockName = event.getSelectedItem().getReplacementString();

        try {
            handler.blockSelected(blockName);
        }
        catch(Exception e) {
            Firebug.error("handleSuggestBoxSelection: " + e.getMessage(), e);
        }
        suggestBox.getValueBox().selectAll();
        suggestBox.setFocus(true);
        History.newItem(blockName);
    }

    @UiHandler("topTabPanel")
    public void handleTabSelection(SelectionEvent<Integer> event) {
        if(Integer.valueOf(3).equals(event.getSelectedItem())) {
            requestSupplementaryInformation();
        }
    }

    private void requestSupplementaryInformation() {
        RequestBuilder rb =
                new RequestBuilder(RequestBuilder.GET,
                        "supplementaryinformation.html");  //$NON-NLS$

        rb.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                switch(response.getStatusCode()) {
                    case Response.SC_OK:
                        supplementaryInformation.setHTML(response.getText());
                        break;
                    default:
                        supplementaryInformation.setHTML(response.getStatusCode() + " "
                                + response.getStatusText());
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                supplementaryInformation.setHTML("No supplementary information."); //$NON-NLS$
            }
        });

        try {
            rb.send();
        } catch (RequestException e) {
            Firebug.error("<handleTabSelection> send request for supplementary information failed!", e);
        }
    }

    /** This workaround is necessary, because it is not possible to apply the annotation UiHandler on the inner TextBox of a SuggestBox.
     */
    public static class SuggestBoxFocusHandler implements FocusHandler {
        @Override
        public void onFocus(FocusEvent event) {
            final TextBoxBase suggestTextBox = (TextBoxBase)event.getSource();
            suggestTextBox.selectAll();
        }
    }

    public void selectRequestAndResponse() {
        if (this.topTabPanel.getSelectedIndex() == 0) {
            this.topTabPanel.selectTab(1);
        }
    }

    public void selectOverview() {
        if (this.topTabPanel.getSelectedIndex() == 1) {
            this.topTabPanel.selectTab(0);
        }
    }
}