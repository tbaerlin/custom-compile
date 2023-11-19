package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.CheckBox;

/**
 * @author umaurer
 */
public class ChartcenterFormCheckBox extends CheckBox {
    private LabelElement labelElement;

    public ChartcenterFormCheckBox(String label, boolean asHtml) {
        super(label, asHtml);
        InputElement inputElement = null;
        this.labelElement = null;

        final Element element = getElement();
        final NodeList<Node> nodeList = element.getChildNodes();
        for (int i = 0, nodeListLength = nodeList.getLength(); i < nodeListLength; i++) {
            final Node node = nodeList.getItem(i);
            final String upperNodeName = node.getNodeName().toUpperCase();
            if ("INPUT".equals(upperNodeName)) { // $NON-NLS-0$
                inputElement = (InputElement)node;
            }
            else if ("LABEL".equals(upperNodeName)) { // $NON-NLS-0$
                this.labelElement = (LabelElement)node;
            }
        }
        assert(inputElement != null);
        assert(this.labelElement != null);

        element.removeChild(inputElement);
        element.appendChild(inputElement);
    }

    public void setLabelStyleName(String style) {
        this.labelElement.setClassName(style);
    }
}
