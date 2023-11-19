package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import de.marketmaker.itools.gwtutil.client.util.CssUtil;

import static com.google.gwt.dom.client.Style.Position.ABSOLUTE;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 31.03.15
 */
public class CssValues {
    public static final CssValues I = new CssValues();
    public static final int CHART_COLOR_COUNT = 12;
    public static final int READONLY_CHART_COLOR_COUNT = 1;

    private final String[] chartColors;
    private final String[] readonlyChartColors;
    private final int customerColorLineHeight;

    public CssValues() {
        this.chartColors = readChartColors(CHART_COLOR_COUNT, "as-pieTable");  // $NON-NLS$
        this.readonlyChartColors = readChartColors(READONLY_CHART_COLOR_COUNT, "sps-yieldRiskPickerReadonly");  // $NON-NLS$
        this.customerColorLineHeight = getOffsetHeight("ice-customerColorLine"); // $NON-NLS$
    }

    private String[] readChartColors(int chartColorCount, String parentElementStyle) {
        final String[] colors = new String[chartColorCount];
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        for (int i = 0; i < chartColorCount; i++) {
            sb.appendHtmlConstant("<div class=\"color-" + i + "\"></div>");
        }
        final HTML html = new HTML(sb.toSafeHtml());
        html.setStyleName(parentElementStyle);
        final Element htmlElement = html.getElement();
        final Style style = htmlElement.getStyle();
        style.setPosition(ABSOLUTE);
        style.setLeft(-100, PX);
        style.setRight(-100, PX);
        style.setWidth(1, PX);
        style.setHeight(1, PX);
        style.setOverflow(Style.Overflow.HIDDEN);
        RootPanel.get().add(html);
        final NodeList<Element> children = htmlElement.getElementsByTagName("div"); // $NON-NLS$
        for (int i = 0; i < chartColorCount; i++) {
            colors[i] = CssUtil.getComputedPropertyValue(children.getItem(i), "backgroundColor"); // $NON-NLS$
        }
        html.removeFromParent();
        return colors;
    }

    private int getOffsetHeight(String elementStyle) {
        final Label label = new Label();
        label.setStyleName(elementStyle);
        RootPanel.get().add(label);
        final int height = label.getOffsetHeight();
        label.removeFromParent();
        return height;
    }

    public static String[] getChartColors() {
        return I.chartColors;
    }

    public static String[] getReadonlyChartColors() {
        return I.readonlyChartColors;
    }

    public static int getCustomerColorLineHeight() {
        return I.customerColorLineHeight;
    }
}
