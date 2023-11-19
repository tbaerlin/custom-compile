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
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.FocusImpl;
import de.marketmaker.itools.gwtutil.client.util.FocusKeyHandler;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;

/**
 * @author umaurer
 */
public class ImageButton extends Composite implements HasClickHandlers , FocusKeyHandler, Focusable, HasFocusHandlers, HasBlurHandlers {
    private final Renderer renderer;
    private final HandlerManager handlerManager = new HandlerManager(this);
    private boolean enabled = true;
    private boolean mouseOver = false;
    private boolean mouseClicked = false;
    private Image imageDefault = null;
    private Image imageDisabled = null;
    private Image imageActive = null;
    private String lastStyle = "duMMy";

    interface Renderer {
        void setImage(Image image);
        void addStyleName(String styleName);
        void removeStyleName(String styleName);
        Widget getWidget();
    }

    class SimpleRenderer implements Renderer {
        final SimplePanel panel;

        SimpleRenderer(SimplePanel panel, String baseStyle) {
            this.panel = panel;
            this.panel.setStyleName(baseStyle == null ? "as-button" : baseStyle);
            final MouseHandler mouseHandler = new MouseHandler();
            this.panel.addDomHandler(mouseHandler, MouseOverEvent.getType());
            this.panel.addDomHandler(mouseHandler, MouseOutEvent.getType());
            this.panel.addDomHandler(mouseHandler, MouseDownEvent.getType());
            this.panel.addDomHandler(mouseHandler, MouseUpEvent.getType());
            this.panel.addDomHandler(mouseHandler, ClickEvent.getType());
        }

        @Override
        public void setImage(Image image) {
            this.panel.setWidget(image);
            if (image != null) {
                final int height = image.getHeight();
                this.panel.getElement().getStyle().setHeight(height, Style.Unit.PX);
                this.panel.getElement().getStyle().setLineHeight(height, Style.Unit.PX);
            }
        }

        @Override
        public void addStyleName(String styleName) {
            this.panel.addStyleName(styleName);
        }

        @Override
        public void removeStyleName(String styleName) {
            this.panel.removeStyleName(styleName);
        }

        @Override
        public Widget getWidget() {
            return this.panel;
        }
    }

    class TableRenderer implements Renderer {
        final Grid grid;

        TableRenderer(SimplePanel panel, String baseStyle) {
            panel.setStyleName(baseStyle == null ? "mm-btn mm-btn-image" : baseStyle);

            this.grid = new MouseAwareGrid(3, 3);
            this.grid.setCellPadding(0);
            this.grid.setCellSpacing(0);

            this.grid.setHTML(0, 0, "<div>&nbsp;</div>");
            this.grid.setHTML(0, 1, "<div>&nbsp;</div>");
            this.grid.setHTML(0, 2, "<div>&nbsp;</div>");
            this.grid.setHTML(1, 0, "<div>&nbsp;</div>");
            this.grid.setHTML(1, 2, "<div>&nbsp;</div>");
            this.grid.setHTML(2, 0, "<div>&nbsp;</div>");
            this.grid.setHTML(2, 1, "<div>&nbsp;</div>");
            this.grid.setHTML(2, 2, "<div>&nbsp;</div>");

            final HTMLTable.RowFormatter rowFormatter = this.grid.getRowFormatter();
            rowFormatter.setStyleName(0, "top");
            rowFormatter.setStyleName(1, "mid");
            rowFormatter.setStyleName(2, "bottom");

            final HTMLTable.CellFormatter formatter = this.grid.getCellFormatter();
            formatter.setStyleName(0, 0, "left");
            formatter.setStyleName(0, 1, "center");
            formatter.setStyleName(0, 2, "right");
            formatter.setStyleName(1, 0, "left");
            formatter.setStyleName(1, 1, "center");
            formatter.setStyleName(1, 2, "right");
            formatter.setStyleName(2, 0, "left");
            formatter.setStyleName(2, 1, "center");
            formatter.setStyleName(2, 2, "right");
            panel.setWidget(this.grid);
        }

        @Override
        public void setImage(Image image) {
            if (image == null) {
                this.grid.setHTML(1, 1, "&nbsp;");
            }
            else {
                this.grid.setWidget(1, 1, image);
            }
        }

        @Override
        public void addStyleName(String styleName) {
            this.grid.addStyleName("mm-btn-" + styleName);
        }

        @Override
        public void removeStyleName(String styleName) {
            this.grid.removeStyleName("mm-btn-" + styleName);
        }

        @Override
        public Widget getWidget() {
            return this.grid;
        }
    }

    public ImageButton(Image imageDefault, Image imageDisabled, Image imageActive) {
        this(null, imageDefault, imageDisabled, imageActive);
    }

    public ImageButton(final String baseStyle, Image imageDefault, Image imageDisabled, Image imageActive) {
        this.imageDefault = imageDefault;
        this.imageDisabled = imageDisabled;
        this.imageActive = imageActive;

        final SimplePanel panel = new SimplePanel();
        this.renderer = Button.getRendererType() == Button.RendererType.SIMPLE
                ? new SimpleRenderer(panel, baseStyle)
                : new TableRenderer(panel, baseStyle);
        this.renderer.setImage(this.imageDefault);
        updateStyle();
        initWidget(panel);
        WidgetUtil.makeFocusable(this.renderer.getWidget(), this);
    }

    class MouseAwareGrid extends Grid {
        MouseAwareGrid(int rows, int columns) {
            super(rows, columns);
            final MouseHandler mouseHandler = new MouseHandler();
            addDomHandler(mouseHandler, MouseOverEvent.getType());
            addDomHandler(mouseHandler, MouseOutEvent.getType());
            addDomHandler(mouseHandler, MouseDownEvent.getType());
            addDomHandler(mouseHandler, MouseUpEvent.getType());
            addDomHandler(mouseHandler, ClickEvent.getType());
        }
    }

    class MouseHandler implements MouseOverHandler, MouseOutHandler, MouseDownHandler, MouseUpHandler, ClickHandler {
        public void onMouseOver(MouseOverEvent event) {
            if (enabled) {
                setMouseOver(true);
            }
        }

        public void onMouseOut(MouseOutEvent event) {
            mouseClicked = false;
            setMouseOver(false);
        }

        public void onMouseDown(MouseDownEvent event) {
            if (enabled) {
                mouseClicked = true;
                updateStyle();
            }
        }

        public void onMouseUp(MouseUpEvent event) {
            mouseClicked = false;
            updateStyle();
        }

        public void onClick(ClickEvent event) {
            fireClickEvent(event);
        }
    }

    private void fireClickEvent(ClickEvent event) {
        if (this.enabled) {
            this.handlerManager.fireEvent(event);
//            this.widget.setStyleName(STYLE_DEFAULT);
        }
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler clickHandler) {
        return this.handlerManager.addHandler(ClickEvent.getType(), clickHandler);
    }

/*
    public void setTitle(String tooltip) {
        this.widget.setTitle(tooltip);
    }
*/

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            this.renderer.setImage(this.imageDefault);
        }
        else if (this.imageDisabled != null) {
            this.renderer.setImage(this.imageDisabled);
        }
        updateStyle();
    }

    public void setMouseOver(boolean mouseOver) {
        if (this.mouseOver == mouseOver) {
            return;
        }
        this.mouseOver = mouseOver;
        updateStyle();
    }

    protected void updateStyle() {
        if (!this.enabled) {
            updateStyle("disabled");
        }
        else if (this.mouseClicked) {
            updateStyle("clicked");
        }
        else if (this.mouseOver) {
            updateStyle("over");
        }
        else {
            updateStyle("default");
        }
    }

    protected void updateStyle(final String style) {
        if (style.equals(this.lastStyle)) {
            return;
        }
        this.renderer.removeStyleName(this.lastStyle);
        this.renderer.addStyleName(style);
        this.lastStyle = style;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setActive(boolean active) {
        if (!this.enabled) {
            throw new RuntimeException("ImageButton.setActive(true) not allowed for disabled button");
        }
        assert(imageActive != null);
        this.renderer.setImage(active ? this.imageActive : this.imageDefault);
    }

    private void click() {
        WidgetUtil.click(this.renderer.getWidget().getElement());
    }

    @Override
    public boolean onFocusKeyClick() {
        click();
        return true;
    }

    @Override
    public boolean onFocusKeyEscape() {
        // implemented in subclass
        return false;
    }

    @Override
    public boolean onFocusKeyHome() {
        // implemented in subclass
        return false;
    }

    @Override
    public boolean onFocusKeyPageUp() {
        // implemented in subclass
        return false;
    }

    @Override
    public boolean onFocusKeyUp() {
        // implemented in subclass
        return false;
    }

    @Override
    public boolean onFocusKeyDown() {
        // implemented in subclass
        return false;
    }

    @Override
    public boolean onFocusKeyPageDown() {
        // implemented in subclass
        return false;
    }

    @Override
    public boolean onFocusKeyEnd() {
        // implemented in subclass
        return false;
    }

    @Override
    public boolean onFocusKey(char c) {
        // implemented in subclass
        return false;
    }

    @Override
    public boolean onFocusDelete() {
        // implemented in subclass
        return false;
    }

    @Override
    public boolean onFocusAdd() {
        // implemented in subclass
        return false;
    }

    @Override
    public int getTabIndex() {
        return FocusImpl.getFocusImplForPanel().getTabIndex(this.renderer.getWidget().getElement());
    }

    @Override
    public void setAccessKey(char key) {
        FocusImpl.getFocusImplForPanel().setAccessKey(this.renderer.getWidget().getElement(), key);
    }

    @Override
    public void setFocus(boolean focused) {
        if (focused) {
            FocusImpl.getFocusImplForPanel().focus(this.renderer.getWidget().getElement());
        }
        else {
            FocusImpl.getFocusImplForPanel().blur(this.renderer.getWidget().getElement());
        }
    }

    @Override
    public void setTabIndex(int tabIndex) {
        FocusImpl.getFocusImplForPanel().setTabIndex(this.getWidget().getElement(), tabIndex);
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return this.renderer.getWidget().addDomHandler(handler, FocusEvent.getType());
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return this.renderer.getWidget().addDomHandler(handler, BlurEvent.getType());
    }
}
