package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxmldocu.DmxmlBlockDocumentation;
import de.marketmaker.iview.dmxmldocu.DmxmlBlockParameterDocumentation;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.event.GuiEventHandler;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.event.LogoutEvent;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter.ParameterGuiFactory;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter.ParameterInputWidget;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter.XmlTreeSelectionListener;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.widgets.AuthItem;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.Attribute;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.Element;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.XsdNode;


/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 * @author Markus Dick
 */
public class DmxmlDocuController implements GuiEventHandler, ParameterInputWidget.ChangeListener {
    private HasWidgets container;

    private BlocksDocumentation blocksDocumentation;

    private DmxmlDocuGui gui;

    private String selectedBlockName;

    private final HandlerManager eventBus;

    private final ArrayList<ParameterInputWidget> parameterWidgets = new ArrayList<ParameterInputWidget>();

    public DmxmlDocuController(HandlerManager eventBus) {
        this.eventBus = eventBus;
    }

    public void go(HasWidgets container) {
        this.container = container;
        buildGui();
        fetchDocuRepository();
    }

    private void buildGui() {
        container.clear();

        // Build GUI
        gui = new DmxmlDocuGui(this, eventBus);
        container.add(gui.panel);

        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                Firebug.error("uncaught exception", e);
            }
        });
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String blockName = event.getValue();
                setSelectedBlock(blockName);
                blockSelected(blockName);
            }
        });

        gui.xmlTree.setSelectionListener(new XmlTreeSelectionListener() {
            @Override
            public void onElementClicked(Element element) {
                showSelectedResponseElementDocu(element);
            }

            @Override
            public void onAttributeClicked(Attribute attribute) {
                showSelectedResponseElementDocu(attribute);
            }
        });
        gui.requestParameterPanel.setChangeListener(this);

        gui.authSelectBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onParameterValueChanged();
            }
        });
        gui.selectedBlockSuggestBox.setFocus(true);
    }

    public void fetchDocuRepository() {
        DmxmlDocuServiceAsyncProxy.INSTANCE.getRepository(new AsyncCallback<BlocksDocumentation>() {
            @Override
            public void onFailure(Throwable caught) {
                Util.onError("Could not load documentation.", caught); // $NON-NLS$
                // TODO handle, e.g. logout and let user refresh page?
            }

            @Override
            public void onSuccess(BlocksDocumentation result) {
                blocksDocumentation = result;

                for (String block : blocksDocumentation.getBlockNames()) {
                    gui.blockOverviewPanel.add(block);
                    gui.selectedBlockSuggestOracle.add(block);
                }
                gui.authSelectBox.setDefaultAuthItem(blocksDocumentation.getDefaultAuthentication(),
                        blocksDocumentation.getDefaultAuthenticationType());
                final String blockName = History.getToken();
                if (!blockName.isEmpty()) {
                    setSelectedBlock(blockName);
                    blockSelected(blockName);
                }
            }
        });
    }

    private void setSelectedBlock(String blockName) {
        gui.selectedBlockSuggestBox.setValue(blockName);
    }

    @Override
    public void blockSelected(String blockName) {
        this.selectedBlockName = blockName;
        if (blockName == null || "".equals(blockName)) {
            gui.firstSentenceBlockHTML.setHTML("");
            gui.blockNameHTML.setHTML("");
            gui.blockDescriptionHTML.setHTML("");
            gui.parametersDescriptionHTML.setHTML("");
            gui.selectOverview();
            return;
        }
        gui.selectRequestAndResponse();

        // Update GUI with docu
        try {
            final DmxmlBlockDocumentation docu = blocksDocumentation.getDocuFor(this.selectedBlockName);
            gui.parametersDescriptionHTML.setHTML(getDocuForBlock(docu));
            // Add parameter GUI
            this.parameterWidgets.clear();
            gui.requestParameterPanel.clear();
            if (docu != null) {
                for (DmxmlBlockParameterDocumentation paramDocu : docu.getParameters().getParameter()) {
                    final ParameterInputWidget inputWidget = ParameterGuiFactory.newInputWidgetFor(paramDocu);
                    inputWidget.addChangeListener(this);
                    this.parameterWidgets.add(inputWidget);
                    gui.requestParameterPanel.add(inputWidget);
                }
            }
        }
        catch(Exception e) {
            Firebug.error("blockSelected: " + blockName, e);
        }
        finally {
            // Add default request
            History.newItem(blockName);
            onParameterValueChanged();
        }
    }

    private SafeHtml getDocuForBlock(DmxmlBlockDocumentation docu) {
        gui.firstSentenceBlockHTML.setHTML(docu.getFirstSentence());
        gui.blockNameHTML.setHTML("<h2>" + docu.getBlockName() + "</h2>"); // $NON-NLS$
        gui.blockDescriptionHTML.setHTML(docu.getDescription());
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<table class=\"description\">");  // $NON-NLS$
        sb.appendHtmlConstant("<tr><th>Name</th><th>Type and Options</th><th>Description</th></tr>");  // $NON-NLS$
        for (DmxmlBlockParameterDocumentation paramDocu : docu.getParameters().getParameter()) {
            sb.appendHtmlConstant("<tr><td>").appendEscaped(paramDocu.getName()).appendHtmlConstant("</td>"); // $NON-NLS$
            sb.appendHtmlConstant("<td>").appendEscaped(paramDocu.getGuiType().toString()); // $NON-NLS$
            sb.appendHtmlConstant("<br/><span class=\"option\">"); // $NON-NLS$
            sb.appendEscaped(paramDocu.isRequired() ? "[required]" : "[optional]"); // $NON-NLS$
            if (paramDocu.isMultiValued()) {
                sb.appendHtmlConstant("<br/>"); // $NON-NLS$
                sb.appendEscaped("[multi-valued]"); // $NON-NLS$
            }
            sb.appendHtmlConstant("</span></td><td>"); // $NON-NLS$
            if (paramDocu.getDescription() != null) {
                sb.appendHtmlConstant(paramDocu.getDescription());
            }
            sb.appendHtmlConstant("</td></tr>"); // $NON-NLS$
        }
        sb.appendHtmlConstant("</table>"); // $NON-NLS$
        return sb.toSafeHtml();
    }

    @Override
    public void sendRequestClicked() {
        // clear request GUI
        gui.requestStatusImage.setUrl(gui.emptyImageUrl);
        gui.responseStatusImage.setUrl(gui.emptyImageUrl);
        gui.requestStatusImage.setTitle(null);
        gui.responseStatusImage.setTitle(null);
        gui.responseElementDocuXml.setHTML("");
        gui.rawResponseTextArea.setText("");

        final String rawRequest = gui.rawRequestTextArea.getText();
        DmxmlDocuServiceAsyncProxy.INSTANCE.sendDmxmlRequest(new WrappedDmxmlRequest(rawRequest), new AsyncCallback<WrappedDmxmlResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof InvalidDmxmlRequestException) {
                    gui.requestStatusImage.setUrl(gui.statusErrorImageUrl);
                    gui.requestStatusImage.setTitle(caught.getMessage());
                    gui.xmlTree.setError(caught.getMessage(), null);
                }
                else if (caught instanceof InvalidDmxmlResponseException) {
                    gui.requestStatusImage.setUrl(gui.statusOkImageUrl);
                    gui.requestStatusImage.setTitle("Request valid w.r.t. XML Schema"); // $NON-NLS$
                    gui.responseStatusImage.setUrl(gui.statusErrorImageUrl);
                    gui.responseStatusImage.setTitle(caught.getMessage());
                    gui.rawResponseTextArea.setText(((InvalidDmxmlResponseException) caught).getResponseXml());
                    gui.xmlTree.setError(null, caught.getMessage());
                }
                else {
                    gui.xmlTree.setError("Sending dm[xml] request failed: " + caught.getMessage(), null); // $NON-NLS$
                    Firebug.error("Sending dm[xml] request failed", caught);
                }
            }

            @Override
            public void onSuccess(WrappedDmxmlResponse result) {
                gui.rawResponseTextArea.setText(result.getDmxmlResponse());
                gui.xmlTree.setRootNode(result.getXmlTreeRoot());
                gui.requestStatusImage.setUrl(gui.statusOkImageUrl);
                gui.requestStatusImage.setTitle("Request valid w.r.t. XML Schema"); // $NON-NLS$
                gui.responseStatusImage.setUrl(gui.statusOkImageUrl);
                gui.responseStatusImage.setTitle("Response valid w.r.t. XML Schema"); // $NON-NLS$
            }
        });
    }

    @Override
    public void logoutClicked() {
        DmxmlDocuServiceAsyncProxy.INSTANCE.logout(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Logout failed. Probably, the session is broken anyway.\nPlease reload page manually"); // $NON-NLS$
            }

            @Override
            public void onSuccess(Void result) {
                eventBus.fireEvent(new LogoutEvent());
            }
        });
    }

    @Override
    public void onParameterValueChanged() {
        String authentication = ""; //$NON-NLS$
        String authenticationType = ""; //$NON-NLS$
        String locale = null;

        final AuthItem authItem = gui.authSelectBox.getSelectedAuthItem();
        if(authItem != null) {
            authentication = authItem.getAuthentication();
            authenticationType = authItem.getAuthenticationType();
            locale = authItem.getLocale();
        }

        final String requestXml = RequestBuilder.getRequestFromGui(
                this.selectedBlockName, authentication, authenticationType, locale, this.parameterWidgets);
        gui.rawRequestTextArea.setText(requestXml);
    }

    private void showSelectedResponseElementDocu(XsdNode xsdNode) {
        gui.responseElementDocuXml.setHTML(getDocuForNode(xsdNode));
    }

    private String getDocuForNode(XsdNode xsdNode) {
        StringBuilder docu = new StringBuilder(100);
        docu.append("<h3>Documentation for '").append(getHeadline(xsdNode)).append("'</h3>");   // $NON-NLS$
        docu.append("<p><b>Schema Type:</b> "); // $NON-NLS$
        docu.append(xsdNode.getXsdType().getLocalName() == null ? "<span style=\"color: gray\">null</span>" : xsdNode.getXsdType().getLocalName()); // $NON-NLS$
        docu.append("</p>"); // $NON-NLS$
        final String tooltip = xsdNode.getTooltip();
        if (tooltip == null || tooltip.isEmpty()) {
            docu.append("<span style=\"color: gray\">no description available</span>"); // $NON-NLS$
        }
        else {
            docu.append(tooltip);
        }
        return docu.toString();
    }

    private String getHeadline(XsdNode xsdNode) {
        if (xsdNode instanceof Attribute) {
            final Attribute attribute = (Attribute) xsdNode;
            return attribute.getParent().getLabel() + "@" + attribute.getKey(); // $NON-NLS$
        }
        else {
            return xsdNode.getLabel();
        }
    }
}
