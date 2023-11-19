package de.marketmaker.itools.gwtutil.client.widgets.menu;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.HasIcon;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;

import java.util.HashMap;

/**
 * @author Ulrich Maurer
 *         Date: 01.10.12
 */
public class MenuItem extends Composite implements HasIcon {
    private static final String BASE_CLASS = "mm-menuItem";
    private FlowPanel panel;
    private final HTML label;
    private Image imageIcon = null;
    private boolean enabled = true;
    private HandlerManager handlerManager = new HandlerManager(this);
    private HashMap<String, Object> mapData = null;
    private char prefixChar;

    public MenuItem() {
        this("", null, null);
    }

    public MenuItem(String text) {
        this(text, null, null);
    }

    public MenuItem(String text, ClickHandler clickHandler) {
        this(text, null, clickHandler);
    }

    public MenuItem(String text, String iconClass, ClickHandler clickHandler) {
        this(text == null || text.length() < 1
                        ? (char) 0
                        : Character.toLowerCase(text.charAt(0))
                ,text == null
                        ? SafeHtmlUtils.fromSafeConstant(StringUtility.NULL_FORMATTED)
                        : SafeHtmlUtils.fromString(text)
                , iconClass, clickHandler);
    }

    public MenuItem(SafeHtml text) {
        this((char) 0, text, null, null);
    }

    public MenuItem(SafeHtml text, String iconClass, ClickHandler clickHandler) {
        this((char) 0, text, iconClass, clickHandler);
    }

    public MenuItem(char prefixChar, SafeHtml text, String iconClass, ClickHandler clickHandler) {
        this.panel = new FlowPanel();
        this.panel.setStyleName(BASE_CLASS);
        this.label = new HTML(text);
        this.prefixChar = prefixChar;
        this.label.setStyleName("label");
        this.panel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (enabled) {
                    handlerManager.fireEvent(event);
                }
            }
        }, ClickEvent.getType());
        this.panel.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                if (enabled) {
                    handlerManager.fireEvent(event);
                }
            }
        }, MouseOverEvent.getType());
        if (clickHandler != null) {
            addClickHandler(clickHandler);
        }
        this.panel.add(this.label);
        if (iconClass != null) {
            this.imageIcon = new Image("clear.cache.gif");
            this.imageIcon.addStyleName(iconClass);
            this.panel.add(this.imageIcon);
        }
        initWidget(panel);
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return this.handlerManager.addHandler(ClickEvent.getType(), handler);
    }

    public MenuItem withClickHandler(ClickHandler handler) {
        addClickHandler(handler);
        return this;
    }

    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
        return this.handlerManager.addHandler(MouseOverEvent.getType(), handler);
    }

    public void click() {
        WidgetUtil.click(this.label);
    }

    public String getHtml() {
        return this.label.getHTML();
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            this.label.removeStyleName("disabled");
        }
        else {
            this.label.addStyleName("disabled");
        }
    }

    public void setIcon(AbstractImagePrototype imagePrototype) {
        if (this.imageIcon != null) {
            this.imageIcon.removeFromParent();
        }

        if (imagePrototype != null) {
            this.imageIcon = imagePrototype.createImage();
            this.imageIcon.getElement().getStyle().setTop((21 - this.imageIcon.getHeight()) / 2, Style.Unit.PX);
            this.imageIcon.getElement().getStyle().setLeft((21 - this.imageIcon.getWidth()) / 2, Style.Unit.PX);
            this.panel.add(this.imageIcon);
        }
    }

    public void setIconStyle(String styleName) {
        if (this.imageIcon != null) {
            this.imageIcon.removeFromParent();
        }
        if (styleName != null) {
            this.imageIcon = new Image("clear.cache.gif");
            this.imageIcon.addStyleName(styleName);
            this.panel.add(this.imageIcon);
        }
    }

    public MenuItem withIcon(String iconClass) {
        setIcon(IconImage.get(iconClass));
        return this;
    }

    public void setData(String data) {
        String[] values = data.split(":");
        setData(values[0], values[1]);
    }

    public void setData(String key, Object value) {
        if (this.mapData == null) {
            this.mapData = new HashMap<>();
        }
        this.mapData.put(key, value);
    }

    public Object getData(String key) {
        return this.mapData == null ? null : this.mapData.get(key);
    }

    public MenuItem withData(String key, Object value) {
        setData(key, value);
        return this;
    }

    public boolean hasPrefixChar(char c) {
        return c == this.prefixChar;
    }

    public void setText(String text) {
        this.label.setText(text);
    }
}
