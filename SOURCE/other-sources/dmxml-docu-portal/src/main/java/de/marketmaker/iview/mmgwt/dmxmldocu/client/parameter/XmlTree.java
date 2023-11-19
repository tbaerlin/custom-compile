package de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.Attribute;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.Element;

/**
 * @author Ulrich Maurer
 *         Date: 09.05.12
 */
@SuppressWarnings("GWTStyleCheck")
public class XmlTree extends Composite {
    private final FlowPanel panel;
    private XmlTreeSelectionListener selectionListener;

    public XmlTree() {
        this.panel = new FlowPanel();
        this.panel.setStyleName("xmlTree");
        initWidget(this.panel);
    }

    public void setSelectionListener(XmlTreeSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    public void setRootNode(Element rootNode) {
        clear();
        this.panel.add(createElement(rootNode));
    }

    public void setError(String requestMessage, String responseMessage) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<h3>Request</h3><div style=\"white-space: normal\">"); // $NON-NLS$
        if (requestMessage == null) {
            sb.appendHtmlConstant("<img src=\"images/ok.svg\" style=\"float: left; margin: 0 1em 1em 0;\"/>"); // $NON-NLS$
            sb.appendEscaped("ok"); // $NON-NLS$
        }
        else {
            sb.appendHtmlConstant("<img src=\"images/cancel.svg\" style=\"float: left; margin: 0 1em 1em 0;\"/>"); // $NON-NLS$
            sb.appendEscaped(requestMessage);
        }
        sb.appendHtmlConstant("</div>"); // $NON-NLS$
        if (responseMessage != null) {
            sb.appendHtmlConstant("<h3>Response</h3><div style=\"white-space: normal\">"); // $NON-NLS$
            sb.appendHtmlConstant("<img src=\"images/cancel.svg\" style=\"float: left; margin: 0 1em 1em 0;\"/>"); // $NON-NLS$
            sb.appendEscaped(responseMessage);
            sb.appendHtmlConstant("</div>"); // $NON-NLS$
        }

        clear();
        this.panel.add(new HTML(sb.toSafeHtml()));
    }

    private void clear() {
        for (int i = this.panel.getWidgetCount() - 1; i >= 0; i--) {
            this.panel.getWidget(i).removeFromParent();
        }
    }

    private FlowPanel createElement(Element element) {
        final FlowPanel xmlParentElement = new FlowPanel();
        if (element.hasChildren()) {
            xmlParentElement.setStyleName("xmlParentElement open");
            xmlParentElement.add(createOpener());
            xmlParentElement.add(createClosedElement(element));
            xmlParentElement.add(createOpenElement(element));
        }
        else {
            xmlParentElement.setStyleName("xmlParentElement");
            checkErrorType(element, xmlParentElement);
            addTagStart(xmlParentElement, element, !element.hasText());
            if (element.hasText()) {
                xmlParentElement.add(new InlineLabel(element.getText().getLabel()));
                addTagEnd(xmlParentElement, element);
            }
        }
        return xmlParentElement;
    }

    private void checkErrorType(Element element, FlowPanel panel) {
        if ("ErrorType".equals(element.getXsdType().getLocalName())) { // $NON-NLS$
            panel.addStyleName("errorType");
        }
    }

    private Label createOpener() {
        final Label opener = new Label();
        opener.setStyleName("opener");
        opener.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (opener.getParent().getStyleName().equals("xmlParentElement open")) { // $NON-NLS$
                    opener.getParent().setStyleName("xmlParentElement closed");
                }
                else {
                    opener.getParent().setStyleName("xmlParentElement open");
                }
            }
        });
        return opener;
    }

    private FlowPanel createClosedElement(Element element) {
        final FlowPanel panel = new FlowPanel();
        panel.setStyleName("closed");
        checkErrorType(element, panel);
        addTagStart(panel, element, false);
        final InlineLabel lblClosedDots = new InlineLabel(" ... ");
        lblClosedDots.setStyleName("closedDots");
        panel.add(lblClosedDots);
        addTagEnd(panel, element);
        return panel;
    }

    private void addTagStart(FlowPanel panel, final Element element, boolean closed) {
        panel.add(new InlineLabel("<")); // $NON-NLS$
        final InlineLabel tagName = new InlineLabel(element.getLabel());
        tagName.setStyleName("tagName tagStart");
        tagName.getElement().setAttribute("data-type", element.getXsdType().getLocalName()); // $NON-NLS$
        tagName.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (selectionListener != null) {
                    selectionListener.onElementClicked(element);
                }
            }
        });
        panel.add(tagName);
        addAttributes(panel, element);
        panel.add(new InlineLabel(closed ? "/>" : ">")); // $NON-NLS$
    }

    private void addAttributes(FlowPanel panel, Element element) {
        if (element.hasAttributes()) {
            for (Attribute attribute : element.getAttributes()) {
                addAttribute(panel, attribute);
            }
        }
    }

    private void addAttribute(FlowPanel panel, final Attribute attribute) {
        panel.add(new InlineLabel(" "));
        final InlineLabel attKey = new InlineLabel(attribute.getKey());
        attKey.setStyleName("attKey");
        attKey.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (selectionListener != null) {
                    selectionListener.onAttributeClicked(attribute);
                }
            }
        });
        panel.add(attKey);
        panel.add(new InlineLabel("=\""));
        final InlineLabel attValue = new InlineLabel(attribute.getValue());
        attValue.setStyleName("attValue");
        panel.add(attValue);
        panel.add(new InlineLabel("\""));
    }

    private void addTagEnd(FlowPanel panel, Element element) {
        panel.add(new InlineLabel("</")); // $NON-NLS$
        final InlineLabel tagNameEnd = new InlineLabel(element.getLabel());
        tagNameEnd.setStyleName("tagName");
        panel.add(tagNameEnd);
        panel.add(new InlineLabel(">")); // $NON-NLS$
    }

    private FlowPanel createOpenElement(Element element) {
        final FlowPanel panel = new FlowPanel();
        panel.setStyleName("open");
        checkErrorType(element, panel);
        final FlowPanel tagStart = new FlowPanel();
        tagStart.setStyleName("tagStart");
        addTagStart(tagStart, element, false);
        panel.add(tagStart);

        for (Element child : element.getChildren()) {
            panel.add(createElement(child));
        }

        final FlowPanel tagEnd = new FlowPanel();
        tagEnd.setStyleName("tagEnd");
        addTagEnd(tagEnd, element);
        panel.add(tagEnd);
        return panel;
    }
}
