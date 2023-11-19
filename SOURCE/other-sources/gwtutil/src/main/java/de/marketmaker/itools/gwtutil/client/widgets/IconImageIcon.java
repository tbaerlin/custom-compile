package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;

/**
 * Author: umaurer
 * Created: 03.07.14
 */
public class IconImageIcon extends Composite implements HasClickHandlers, HasFocusHandlers, HasBlurHandlers {
    private final Image image = new Image("clear.cache.gif");
    private ImageSpec normalIcon;

    private ImageSpec disabledIcon;
    private boolean disabled = false;

    private ImageSpec hoverIcon;
    private boolean hover = false;
    private HandlerRegistration mouseOverReg;
    private HandlerRegistration mouseOutReg;

    private ImageSpec mouseDownIcon;
    private boolean mouseDown = false;
    private HandlerRegistration mouseDownReg;
    private HandlerRegistration mouseUpReg;

    private HandlerManager handlerManager;

    public IconImageIcon(String normalIcon) {
        this(IconImage.getImageResource(normalIcon));
    }

    private IconImageIcon(ImageSpec normalIcon) {
        this.normalIcon = normalIcon;
        this.image.setStyleName("mm-imageSpecIcon");
        updateImage();
        initWidget(this.image);
    }

    public IconImageIcon withIcon(String normalIcon) {
        this.normalIcon = IconImage.getImageResource(normalIcon);
        updateImage();
        return this;
    }

    public IconImageIcon withDisabledIcon(String disabledIcon) {
        this.disabledIcon = IconImage.getImageResource(disabledIcon);
        updateImage();
        return this;
    }

    public IconImageIcon withHoverIcon(String hoverIcon) {
        if (hoverIcon == null) {
            if (this.mouseOverReg != null) {
                this.mouseOverReg.removeHandler();
                this.mouseOutReg.removeHandler();
                this.mouseOverReg = null;
                this.mouseOutReg = null;
            }
        }
        else {
            if (this.mouseOverReg == null) {
                this.mouseOverReg = this.image.addMouseOverHandler(event -> {
                    this.hover = true;
                    updateImage();
                });
                this.mouseOutReg = this.image.addMouseOutHandler(event -> {
                    this.hover = false;
                    updateImage();
                });
            }
        }
        this.hoverIcon = IconImage.getImageResource(hoverIcon);
        updateImage();
        return this;
    }

    public IconImageIcon withMouseDownIcon(String mouseDownIcon) {
        if (mouseDownIcon == null) {
            if (this.mouseDownReg != null) {
                this.mouseDownReg.removeHandler();
                this.mouseUpReg.removeHandler();
                this.mouseDownReg = null;
                this.mouseUpReg = null;
            }
        }
        else {
            if (this.mouseDownReg == null) {
                this.mouseDownReg = this.image.addMouseDownHandler(event -> {
                    this.mouseDown = true;
                    updateImage();
                });
                this.mouseUpReg = this.image.addMouseUpHandler(event -> {
                    this.mouseDown = false;
                    updateImage();
                });
            }
        }
        this.mouseDownIcon = IconImage.getImageResource(mouseDownIcon);
        updateImage();
        return this;
    }

    public IconImageIcon withClickHandler(ClickHandler clickHandler) {
        addClickHandler(clickHandler);
        return this;
    }

    public IconImageIcon withStyleName(String additionalStyleName) {
        this.image.addStyleName(additionalStyleName);
        return this;
    }

    private void updateImage() {
        final Style style = this.image.getElement().getStyle();
        if (this.disabled) {
            if (this.disabledIcon == null) {
                style.setOpacity(0.3);
                use(this.normalIcon);
            }
            else {
                style.clearOpacity();
                use(this.disabledIcon);
            }
        }
        else {
            style.clearOpacity();
            if (this.mouseDown && this.mouseDownIcon != null) {
                use(this.mouseDownIcon);
            }
            else if (this.hover && this.hoverIcon != null) {
                use(this.hoverIcon);
            }
            else {
                use(this.normalIcon);
            }
        }
    }

    private void use(ImageSpec imageSpec) {
        imageSpec.applyTo(this.image.getElement());
    }

    public HandlerRegistration addClickHandler(ClickHandler clickHandler) {
        if (this.handlerManager == null) {
            this.handlerManager = new HandlerManager(this);
            this.image.addClickHandler(event -> {
                if (!this.disabled) {
                    this.handlerManager.fireEvent(event);
                    checkHover(event);
                }
            });
            if (!this.disabled) {
                this.image.getElement().getStyle().setCursor(Style.Cursor.POINTER);
            }
        }
        return this.handlerManager.addHandler(ClickEvent.getType(), clickHandler);
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return this.image.addDomHandler(handler, FocusEvent.getType());
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return this.image.addDomHandler(handler, BlurEvent.getType());
    }

    private void checkHover(ClickEvent event) {
        final int clientX = event.getNativeEvent().getClientX();
        final int clientY = event.getNativeEvent().getClientY();
        final int absoluteLeft = this.image.getAbsoluteLeft();
        final int absoluteTop = this.image.getAbsoluteTop();
        final int offsetWidth = this.image.getOffsetWidth();
        final int offsetHeight = this.image.getOffsetHeight();
        if (!(isInside(clientX, absoluteLeft, offsetWidth) && isInside(clientY, absoluteTop, offsetHeight))) {
            this.hover = false;
            updateImage();
        }
    }

    private boolean isInside(int click, int pos, int offset) {
        return click >= pos && click <= pos + offset;
    }

    public void setEnabled(boolean enabled) {
        this.disabled = !enabled;
        if (this.handlerManager != null) {
            this.image.getElement().getStyle().setCursor(this.disabled ? Style.Cursor.DEFAULT : Style.Cursor.POINTER);
        }
        updateImage();
    }

    public boolean isEnabled() {
        return !this.disabled;
    }

    public int getWidth() {
        return this.normalIcon.getWidth();
    }
}
