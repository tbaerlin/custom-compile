package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author umaurer
 */
public class SimpleButton extends Composite implements HasClickHandlers {
    private HTML html;
    private boolean enabled = true;

    public SimpleButton(String text, boolean asHtml) {
        final SimplePanel panel = new SimplePanel();
        if (asHtml) {
            this.html = new HTML(text);
        }
        else {
            this.html = new HTML();
            this.html.setText(text);
        }
        panel.setStyleName("mm-simpleButton");
        this.html.setStyleName("mm-btn-default");
        this.html.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                if (SimpleButton.this.enabled) {
                    SimpleButton.this.html.addStyleName("mm-btn-over");
                }
            }
        });
        this.html.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                SimpleButton.this.html.removeStyleName("mm-btn-over");
            }
        });
        this.html.addMouseDownHandler(new MouseDownHandler() {
            public void onMouseDown(MouseDownEvent event) {
                if (SimpleButton.this.enabled) {
                    SimpleButton.this.html.addStyleName("mm-btn-clicked");
                }
            }
        });
        this.html.addMouseUpHandler(new MouseUpHandler() {
            public void onMouseUp(MouseUpEvent event) {
                if (SimpleButton.this.enabled) {
                    SimpleButton.this.html.removeStyleName("mm-btn-clicked");
                }
            }
        });

        panel.setWidget(this.html);
        initWidget(panel);
    }

    public SimpleButton(String text) {
        this(text, false);
    }

    public SimpleButton() {
        this("", false);
    }

    public void setText(String text) {
        this.html.setText(text);
    }

    public void setHtml(String text) {
        this.html.setHTML(text);
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        final int typeInt = Event.getTypeInt(ClickEvent.getType().getName());
        if (enabled) {
            sinkEvents(typeInt);
        }
        else {
            unsinkEvents(typeInt);
        }
    }

}
