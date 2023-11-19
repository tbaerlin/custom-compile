package de.marketmaker.itools.gwtutil.client.widgets.input;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.impl.FocusImpl;
import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;

/**
 * User: umaurer
 * Date: 04.06.13
 * Time: 16:42
 */
public abstract class ClickWidget extends Composite implements HasValueChangeHandlers<Boolean>, Focusable, HasFocusHandlers, HasBlurHandlers {
    private final Image image = new Image();
    private final ClickHandler clickHandler;
    private final MouseOverHandler mouseOverHandler;
    private final MouseOutHandler mouseOutHandler;
    private Boolean checked;
    private boolean enabled = true;

    public ClickWidget(String styleName, Boolean checked) {
        this.image.setUrl("clear.cache.gif");
        this.image.setStyleName("mm-clickWidget");
        this.image.addStyleName(styleName);
        WidgetUtil.makeFocusable(this.image, new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_SPACE) {
                    setNextCheckState();
                }
            }
        });
        this.clickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setNextCheckState();
                WidgetUtil.deferredSetFocus(image);
            }
        };
        this.image.addClickHandler(this.clickHandler);
        this.mouseOverHandler = new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                image.addStyleName("hover");
            }
        };
        this.image.addMouseOverHandler(this.mouseOverHandler);
        this.mouseOutHandler = new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                image.removeStyleName("hover");
            }
        };
        this.image.addMouseOutHandler(this.mouseOutHandler);

        this.image.addStyleName(getStyle(null));
        setChecked(checked);

        initWidget(this.image);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
        return this.addHandler(handler, ValueChangeEvent.getType());
    }

    void fireValueChangeEvent(Boolean value) {
        ValueChangeEvent.fire(this, value);
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return this.image.addDomHandler(handler, FocusEvent.getType());
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return this.image.addDomHandler(handler, BlurEvent.getType());
    }

    abstract Boolean getCheckedAfterClick();

    public void setNextCheckState() {
        setChecked(getCheckedAfterClick());
    }

    /**
     * Sets the checked state if, and ony if the widget <em>is enabled</em> and the current and given value are different.
     * @deprecated use {@link #setCheckedValue(Boolean)} instead. Be aware of different implementation details!
     */
    @Deprecated
    public void setChecked(Boolean checked) {
        //noinspection deprecation
        setChecked(checked, true);
    }

    /**
     * Sets the checked state if, and ony if the widget <em>is enabled</em> and the current and given value are different.
     * @deprecated use {@link #setCheckedValue(Boolean, boolean)} instead. Be aware of different implementation details!
     */
    @Deprecated
    public void setChecked(Boolean checked, boolean fireEvent) {
        if (!this.enabled) {
            return;
        }
        setCheckedValue(checked, fireEvent);
    }

    /**
     * Sets the checked state if the current value is different from the given value.
     * Does not fire any value change events.
     * Behaves like {@linkplain HasValue#setValue(Object)}
     * Due to some name clashes with {@linkplain Radio} we cannot implement {@linkplain HasValue} here.
     */
    public void setCheckedValue(Boolean checked) {
        setCheckedValue(checked, false);
    }

    /**
     * Sets the checked state if the current value is different from the given value.
     * Behaves like {@linkplain HasValue#setValue(Object, boolean)}
     * Due to some name clashes with {@linkplain Radio} we cannot implement {@linkplain HasValue} here.
     * @param fireEvent fires a value change event if true
     */
    public void setCheckedValue(Boolean checked, boolean fireEvent) {
        if (CompareUtil.equals(this.checked, checked)) {
            return;
        }
        this.image.removeStyleName(getStyle(this.checked));
        this.checked = checked;
        this.image.addStyleName(getStyle(this.checked));
        if (fireEvent) {
            fireValueChangeEvent(checked);
        }
    }

    /**
     * Gets the checked state.
     * Behaves like {@linkplain HasValue#setValue(Object, boolean)}.
     * Due to some name clashes with {@linkplain Radio} we cannot implement {@linkplain HasValue} here.
     */
    public Boolean getCheckedValue() {
        return this.checked;
    }

    private String getStyle(Boolean checked) {
        return checked == null
                ? "unspecified"
                : checked ? "checked" : "unchecked";
    }

    /**
     * Gets the checked state.
     * Behaves like {@linkplain HasValue#setValue(Object, boolean)}.
     * Due to some name clashes with {@linkplain Radio} we cannot implement {@linkplain HasValue} here.
     * @deprecated use {@link #getCheckedValue()}
     */
    @Deprecated
    public Boolean getChecked() {
        return getCheckedValue();
    }

    public boolean isChecked() {
        return this.checked != null && this.checked;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            this.image.removeStyleName("disabled");
        }
        else {
            this.image.addStyleName("disabled");
        }
    }

    public Label createLabel(String text) {
        return configureLabel(new Label(text));
    }

    public Label createLabel(SafeHtml safeHtml) {
        return configureLabel(new HTML(safeHtml));
    }

    public Label createSpan(SafeHtml safeHtml) {
        return configureLabel(new InlineHTML(safeHtml));
    }

    public Label configureLabel(Label label) {
        label.addStyleName("mm-clickWidget-label");
        label.addClickHandler(this.clickHandler);
        label.addMouseOverHandler(this.mouseOverHandler);
        label.addMouseOutHandler(this.mouseOutHandler);
        return label;
    }

    @Override
    public int getTabIndex() {
        return FocusImpl.getFocusImplForPanel().getTabIndex(this.image.getElement());
    }

    @Override
    public void setAccessKey(char key) {
        FocusImpl.getFocusImplForPanel().setAccessKey(this.image.getElement(), key);
    }

    @Override
    public void setFocus(boolean focused) {
        if (focused) {
            FocusImpl.getFocusImplForPanel().focus(this.image.getElement());
        }
        else {
            FocusImpl.getFocusImplForPanel().blur(this.image.getElement());
        }
    }

    @Override
    public void setTabIndex(int tabIndex) {
        FocusImpl.getFocusImplForPanel().setTabIndex(this.image.getElement(), tabIndex);
    }
}
