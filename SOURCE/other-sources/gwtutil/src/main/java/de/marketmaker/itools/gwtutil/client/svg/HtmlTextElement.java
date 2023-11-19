package de.marketmaker.itools.gwtutil.client.svg;

import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.CssUtil;

import static com.google.gwt.dom.client.Style.Position.ABSOLUTE;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 14.08.15
 */
public class HtmlTextElement implements IsWidget {
    private final HTML html;

    public HtmlTextElement(float x, float y, float width, float height, float centerX, float centerY, final SafeHtml text) {
        this.html = new HTML(text);
        final float xTransformOrigin = width / 2;
        final Style style = this.html.getElement().getStyle();
        style.setPosition(ABSOLUTE);
        style.setLeft(centerX + x - xTransformOrigin, PX);
        style.setTop(centerY + y, PX);
        style.setWidth(width, PX);
        style.setHeight(height, PX);
        CssUtil.setTransformOrigin(style, xTransformOrigin + "px 0");
    }

    @Override
    public Widget asWidget() {
        return this.html;
    }

    public void setText(String text) {
        this.html.setText(text);
    }

    public void setText(SafeHtml text) {
        this.html.setHTML(text);
    }

    public HtmlTextElement withRotation(float angleGrad) {
        CssUtil.setRotate(this.html.getElement().getStyle(), angleGrad);
        return this;
    }

    public HtmlTextElement withStyle(String styleName) {
        this.html.addStyleName(styleName);
        return this;
    }
}
