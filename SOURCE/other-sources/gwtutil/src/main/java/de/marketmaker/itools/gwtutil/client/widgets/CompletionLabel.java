package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Ulrich Maurer
 *         Date: 16.11.11
 */
public class CompletionLabel extends Composite {
    private final Label label;
    private static final Label popup = new Label();
    private static final Style popupStyle = popup.getElement().getStyle();

    static {
        popupStyle.setVisibility(Style.Visibility.HIDDEN);
        RootPanel.get().add(popup);
        popup.setStyleName("mm-completion-popup");
        popup.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                popupStyle.setVisibility(Style.Visibility.HIDDEN);
            }
        });
    }

    public CompletionLabel(final String text) {
        this.label = new Label(text);
        this.label.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                showPopup();
            }
        });

        initWidget(this.label);
    }

    public void setText(String text) {
        this.label.setText(text);
    }

    private void showPopup() {
        popup.setText(label.getText());
        popupStyle.clearVisibility();
        popupStyle.setLeft(label.getAbsoluteLeft(), Style.Unit.PX);
        //Padding and Position fixes the strange onMouseOut behaviour of IE9
        popupStyle.setTop(label.getAbsoluteTop() - 1, Style.Unit.PX);
        popupStyle.setPaddingTop(1, Style.Unit.PX);
        popupStyle.setPaddingBottom(1, Style.Unit.PX);
    }
}
