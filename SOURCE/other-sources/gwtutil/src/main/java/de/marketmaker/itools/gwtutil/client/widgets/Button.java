package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Element;
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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.FocusImpl;
import de.marketmaker.itools.gwtutil.client.util.FocusKeyHandler;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;

import java.util.HashMap;

/**
 * @author Ulrich Maurer
 *         Date: 01.10.12
 */
public class Button extends Composite implements DialogButton, HasIcon, HasHTML, HasClickHandlers, FocusKeyHandler, Focusable, HasFocusHandlers, HasBlurHandlers {
    public static final String FORCED_LEGACY_BORDERS_STYLE = "form";

    private static final String TABLE_BASE_STYLE = "mm-button";
    private static final String SIMPLE_BASE_STYLE = "as-button";
    private static final String SPAN_BASE_STYLE = "as-button as-button-inline";

    private static RendererType rendererType = RendererType.TABLE;

    private final Renderer renderer;
    private final HandlerManager handlerManager = new HandlerManager(this);
    private boolean enabled = true;
    private boolean active = false;
    private boolean mouseOver = false;
    private boolean mouseClicked = false;

    private HashMap<String, Object> mapData = null;
    private boolean toggleActive = false;
    private String lastStyle = "duMMy";

    private final Widget rightContentWidget = createRightContentWidget();

    public enum RendererType {
        SPAN, SIMPLE, TABLE
    }

    interface Renderer {
        Widget getWidget();
        Element getElement();
        void removeStyleName(String styleName);
        void addStyleName(String styleName);
        int getRightContentX();
        Content getContent();
    }

    abstract class AbstractRenderer<W extends Widget, P extends Panel> implements Renderer {
        final Content content;
        private final W widget;

        protected AbstractRenderer(W widget, P panel, boolean makeFocusable) {
            this.content = new Content(panel);
            this.widget = widget;
            if(makeFocusable) {
                WidgetUtil.makeFocusable(this.widget, Button.this);
            }
        }

        @Override
        public Content getContent() {
            return this.content;
        }

        public W getWidget() {
            return this.widget;
        }

        @Override
        public Element getElement() {
            return this.widget.getElement();
        }

        @Override
        public void removeStyleName(String styleName) {
            this.widget.removeStyleName(styleName);
        }

        @Override
        public void addStyleName(String styleName) {
            this.widget.addStyleName(styleName);
        }
    }

    class SpanRenderer extends AbstractRenderer<FlowPanel, SpanPanel> {
        final Widget rightContentWidget;
        SpanRenderer(String baseStyle, boolean makeFocusable) {
            super(new MouseAwareFlow(), new SpanPanel(), makeFocusable);
            final FlowPanel panel = getWidget();
            panel.getElement().setAttribute("unselectable", "on");

            this.rightContentWidget = getRightContentWidget();

            panel.add(this.content.panel);
            if (this.rightContentWidget != null) {
                panel.add(this.rightContentWidget);
            }

            panel.setStyleName(baseStyle == null ? SPAN_BASE_STYLE : baseStyle);
        }

        @Override
        public int getRightContentX() {
            return this.rightContentWidget.getElement().getOffsetLeft();
        }
    }

    class SimpleRenderer extends AbstractRenderer<FlexTable, FlowPanel> {
        SimpleRenderer(String baseStyle, boolean makeFocusable) {
            super(new MouseAwareTable(), new FlowPanel(), makeFocusable);
            final FlexTable table = getWidget();
            table.setCellPadding(0);
            table.setCellSpacing(0);
            table.getElement().setAttribute("unselectable", "on");

            final Widget rightContentWidget = getRightContentWidget();

            table.setWidget(0, 0, this.content.panel);
            if (rightContentWidget != null) {
                table.setWidget(0, 1, rightContentWidget);
            }

            table.setStyleName(baseStyle == null ? SIMPLE_BASE_STYLE : baseStyle);
        }

        @Override
        public int getRightContentX() {
            return getWidget().getFlexCellFormatter().getElement(0, 1).getOffsetLeft();
        }
    }

    class TableRenderer extends AbstractRenderer<FlexTable, FlowPanel> {
        TableRenderer(String baseStyle, boolean makeFocusable) {
            super(new MouseAwareTable(), new FlowPanel(), makeFocusable);
            final FlexTable table = getWidget();
            table.setCellPadding(0);
            table.setCellSpacing(0);
            table.getElement().setAttribute("unselectable", "on");

            final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
            final Widget rightContentWidget = getRightContentWidget();

            table.setHTML(0, 0, "<div>&nbsp;</div>"); formatter.setStyleName(0, 0, "left");
            table.setHTML(0, 1, "<div>&nbsp;</div>"); formatter.setStyleName(0, 1, "center");
            table.setHTML(0, 2, "<div>&nbsp;</div>"); formatter.setStyleName(0, 2, "right");
            table.setHTML(1, 0, "<div>&nbsp;</div>"); formatter.setStyleName(1, 0, "left");
            table.setWidget(1, 1, this.content.panel);
            formatter.setStyleName(1, 1, "center");
            if (rightContentWidget == null) {
                table.setHTML(1, 2, "<div>&nbsp;</div>"); formatter.setStyleName(1, 2, "right");
            }
            else {
                formatter.setColSpan(0, 1, 2);
                formatter.setColSpan(2, 1, 2);
                table.setWidget(1, 2, rightContentWidget); formatter.setStyleName(1, 2, "center");
                table.setHTML(1, 3, "<div>&nbsp;</div>"); formatter.setStyleName(1, 3, "right");
            }
            table.setHTML(2, 0, "<div>&nbsp;</div>"); formatter.setStyleName(2, 0, "left");
            table.setHTML(2, 1, "<div>&nbsp;</div>"); formatter.setStyleName(2, 1, "center");
            table.setHTML(2, 2, "<div>&nbsp;</div>"); formatter.setStyleName(2, 2, "right");

            final HTMLTable.RowFormatter rowFormatter = table.getRowFormatter();
            rowFormatter.setStyleName(0, "top");
            rowFormatter.setStyleName(1, "mid");
            rowFormatter.setStyleName(2, "bottom");
            table.setStyleName(baseStyle == null ? TABLE_BASE_STYLE : baseStyle);
        }

        @Override
        public int getRightContentX() {
            return getWidget().getFlexCellFormatter().getElement(1,2).getOffsetLeft();
        }
    }

    public static void setRendererType(RendererType type) {
        rendererType = type;
    }

    public static RendererType getRendererType() {
        return rendererType;
    }

    private Renderer createRenderer(RendererType rendererType, String baseStyle, boolean makeFocusable) {
        switch (rendererType) {
            case SPAN:
                return new SpanRenderer(baseStyle, makeFocusable);
            case SIMPLE:
                return new SimpleRenderer(baseStyle, makeFocusable);
            case TABLE:
                return new TableRenderer(baseStyle, makeFocusable);
            default:
                throw new IllegalArgumentException("unhandled renderer: " + rendererType);
        }
    }

    public enum IconPosition {
        LEFT, RIGHT
    }

    class Content {
        private Panel panel;
        private InlineHTML label;
        private IconPosition iconPosition = IconPosition.LEFT;
        private Image imageIcon;

        Content(Panel panel) {
            this.panel = panel;
        }

        void update() {
            this.panel.clear();

            if (this.imageIcon != null && this.iconPosition == IconPosition.LEFT) {
                this.panel.add(this.imageIcon);
            }
            if (this.label != null) {
                this.panel.add(this.label);
            }
            if (this.imageIcon != null && this.iconPosition == IconPosition.RIGHT) {
                this.panel.add(this.imageIcon);
            }
        }
    }

    public static class Factory {
        private final RendererType rendererType;
        private String baseStyle;
        private AbstractImagePrototype iconPrototype;
        private IconPosition iconPosition = IconPosition.LEFT;
        private String text;
        private boolean textIsHtml;
        private SafeHtml tooltip;
        private ClickHandler clickHandler;
        private boolean makeFocusable = true;
        private boolean active = false;
        private boolean toggleActive = false;
        private Menu menu = null;
        private String[] styles;
        private boolean forceLegacyBorders;

        private Factory() {
            this.rendererType = Button.rendererType;
        }

        private Factory(RendererType rendererType) {
            this.rendererType = rendererType;
        }

        public Factory baseStyle(String baseStyle) {
            this.baseStyle = baseStyle;
            return this;
        }

        public Factory icon(String iconMapping) {
            if (iconMapping == null) {
                return this;
            }
            return icon(IconImage.get(iconMapping));
        }

        public Factory icon(String iconMapping, IconPosition iconPosition) {
            return icon(iconMapping).iconPosition(iconPosition);
        }

        public Factory icon(AbstractImagePrototype iconPrototype) {
            this.iconPrototype = iconPrototype;
            return this;
        }

        public Factory icon(AbstractImagePrototype iconPrototype, IconPosition iconPosition) {
            return icon(iconPrototype).iconPosition(iconPosition);
        }

        public Factory iconPosition(IconPosition iconPosition) {
            this.iconPosition = iconPosition;
            return this;
        }

        public Factory text(String text) {
            this.text = text;
            this.textIsHtml = false;
            return this;
        }

        public Factory html(String html) {
            this.text = html;
            this.textIsHtml = true;
            return this;
        }

        public Factory text(SafeHtml safeHtml) {
            this.text = safeHtml.asString();
            this.textIsHtml = true;
            return this;
        }

        public Factory menu(Menu menu) {
            this.menu = menu;
            return this;
        }

        public Factory tooltip(SafeHtml tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Factory tooltip(String tooltip) {
            this.tooltip = tooltip == null ? null : SafeHtmlUtils.fromString(tooltip);
            return this;
        }

        public Factory clickHandler(ClickHandler clickHandler) {
            this.clickHandler = clickHandler;
            return this;
        }

        public Factory makeFocusable(boolean makeFocusable) {
            this.makeFocusable = makeFocusable;
            return this;
        }

        public Factory active() {
            this.active = true;
            return this;
        }

        public Factory active(boolean active) {
            this.active = active;
            return this;
        }

        public Factory toggleActive() {
            this.toggleActive = true;
            return this;
        }

        public Factory additionalStyles(String ... styles) {
            this.styles = styles;
            return this;
        }

        public Factory forceLegacyBorders() {
            this.forceLegacyBorders = true;
            return this;
        }

        public Button build() {
            final Button button = new Button(this.rendererType,
                    this.baseStyle, this.iconPrototype, this.iconPosition,
                    this.text, this.textIsHtml, this.makeFocusable,
                    this.active, this.toggleActive);
            if(this.forceLegacyBorders) {
                button.addStyleName(FORCED_LEGACY_BORDERS_STYLE);
            }
            if(this.styles != null && this.styles.length > 0) {
                for (String style : this.styles) {
                    button.addStyleName(style);
                }
            }
            if (this.clickHandler != null) {
                button.addClickHandler(this.clickHandler);
            }
            if (this.tooltip != null) {
                Tooltip.addQtip(button, this.tooltip);
            }
            if (this.menu != null) {
                button.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        menu.show(button);
                    }
                });
            }
            return button;
        }
    }

    public static Factory span() {
        return new Factory(RendererType.SPAN);
    }

    public static Factory icon(String iconMapping) {
        return new Factory().icon(iconMapping);
    }

    public static Factory icon(String iconMapping, IconPosition iconPosition) {
        return new Factory().icon(iconMapping).iconPosition(iconPosition);
    }

    public static Factory html(String html) {
        return new Factory().html(html);
    }

    public static Factory text(String text) {
        return new Factory().text(text);
    }

    public static Factory text(SafeHtml text) {
        return new Factory().text(text);
    }

    public static Button build() {
        return new Factory().build();
    }

    private Button(RendererType rendererType,
            String baseStyle, AbstractImagePrototype iconPrototype, IconPosition iconPosition,
            String text, boolean textIsHtml, boolean makeFocusable,
            boolean active, boolean toggleActive) {
        this.renderer = createRenderer(rendererType, baseStyle, makeFocusable);
        final Content content = this.renderer.getContent();

        if (iconPrototype != null) {
            content.imageIcon = iconPrototype.createImage();
            content.imageIcon.setStyleName("icon");
        }
        content.iconPosition = iconPosition;

        if (text != null) {
            content.label = new InlineHTML();
            content.label.setStyleName("label");
            if (textIsHtml) {
                content.label.setHTML(text);
            }
            else {
                content.label.setText(text);
            }
        }
        content.update();

        this.active = active;
        this.toggleActive = toggleActive;

        final Widget renderedWidget = this.renderer.getWidget();
        updateStyle();

        initWidget(renderedWidget);
    }

    protected Button() {
        this(rendererType, null, null, IconPosition.LEFT, null, false, true, false, false);
    }

    protected Button(RendererType rendererType) {
        this(rendererType, null, null, IconPosition.LEFT, null, false, true, false, false);
    }

    protected Button(String text) {
        this(rendererType, null, null, IconPosition.LEFT, text, false, true, false, false);
    }

    protected Button(boolean makeFocusable) {
        this(rendererType, null, null, IconPosition.LEFT, null, false, makeFocusable, false, false);
    }

    protected Widget createRightContentWidget() {
        return null;
    }

    protected Widget getRightContentWidget() {
        return this.rightContentWidget;
    }

    protected int getRightContentX() {
        return this.renderer.getRightContentX();
    }

    class MouseAwareTable extends FlexTable {
        MouseAwareTable() {
            final MouseHandler mouseHandler = new MouseHandler();
            addDomHandler(mouseHandler, MouseOverEvent.getType());
            addDomHandler(mouseHandler, MouseOutEvent.getType());
            addDomHandler(mouseHandler, MouseDownEvent.getType());
            addDomHandler(mouseHandler, MouseUpEvent.getType());
            addDomHandler(mouseHandler, ClickEvent.getType());
        }
    }

    class MouseAwareFlow extends FlowPanel {
        MouseAwareFlow() {
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

    protected void fireClickEvent(ClickEvent event) {
        if (this.enabled) {
            if (this.toggleActive) {
                setActive(!this.active);
            }
            this.handlerManager.fireEvent(event);
        }
    }

    public void click() {
        WidgetUtil.click(this.renderer.getElement());
    }

    public Button withClickHandler(ClickHandler clickHandler) {
        addClickHandler(clickHandler);
        return this;
    }

    public HandlerRegistration addClickHandler(ClickHandler clickHandler) {
        return this.handlerManager.addHandler(ClickEvent.getType(), clickHandler);
    }

    public void setIcon(String iconMapping) {
        setIcon(IconImage.get(iconMapping).createImage());
    }

    public Button withIcon(String iconMapping) {
        setIcon(iconMapping);
        return this;
    }

    @Override
    public void setIcon(AbstractImagePrototype imagePrototype) {
        setIcon(imagePrototype == null
                ? null
                : imagePrototype.createImage());
    }

    public void removeIcon() {
        setIcon((Image)null);
    }

    private void setIcon(Image imageIcon) {
        if (imageIcon != null) {
            imageIcon.addStyleName("icon");
        }
        this.renderer.getContent().imageIcon = imageIcon;
        this.renderer.getContent().update();
    }

    public int getIconWidth() {
        return this.renderer.getContent().imageIcon == null
                ? 0
                : this.renderer.getContent().imageIcon.getWidth();
    }

    public void setIconStyle(String styleName) {
        if (styleName == null) {
            setIcon((Image) null);
            return;
        }

        final Image imageIcon = new Image("clear.cache.gif");
        imageIcon.addStyleName(styleName);
        setIcon(imageIcon);
    }

    private void setLabelText(String text, boolean textIsHtml) {
        if (text == null) {
            this.renderer.getContent().label = null;
        }
        else {
            final InlineHTML label = new InlineHTML();
            label.setStyleName("label");
            if (textIsHtml) {
                label.setHTML(text);
            }
            else {
                label.setText(text);
            }
            this.renderer.getContent().label = label;
        }
        this.renderer.getContent().update();
    }

    @Override
    public String getText() {
        return this.renderer.getContent().label.getText();
    }

    public void setText(String text) {
        setLabelText(text, false);
    }

    @Override
    public String getHTML() {
        return this.renderer.getContent().label.getHTML();
    }

    public void setHTML(String html) {
        setLabelText(html, true);
    }

    public void setHTML(SafeHtml html) {
        setLabelText(html.asString(), true);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        updateStyle();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setActive(boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        updateStyle();
    }

    public boolean isActive() {
        return this.active;
    }

    private void setMouseOver(boolean mouseOver) {
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
            updateStyle(this.active ? "active over" : "over");
        }
        else if (this.active) {
            updateStyle("active");
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

    public void setData(String key, Object value) {
        if (this.mapData == null) {
            this.mapData = new HashMap<>();
        }
        this.mapData.put(key, value);
    }

    public void setData(String data) {
        String[] values = data.split(":");
        setData(values[0], values[1]);
    }

    public Object getData(String key) {
        return this.mapData == null ? null : this.mapData.get(key);
    }

    public boolean isToggleActive() {
        return toggleActive;
    }

    public void setToggleActive(boolean toggleActive) {
        this.toggleActive = toggleActive;
    }

    public boolean onFocusKeyClick() {
        click();
        return true;
    }

    public boolean onFocusKeyEscape() {
        // implemented in subclass
        return false;
    }

    public boolean onFocusKeyHome() {
        // implemented in subclass
        return false;
    }

    public boolean onFocusKeyPageUp() {
        // implemented in subclass
        return false;
    }

    public boolean onFocusKeyUp() {
        // implemented in subclass
        return false;
    }

    public boolean onFocusKeyDown() {
        // implemented in subclass
        return false;
    }

    public boolean onFocusKeyPageDown() {
        // implemented in subclass
        return false;
    }

    public boolean onFocusKeyEnd() {
        // implemented in subclass
        return false;
    }

    public boolean onFocusKey(char c) {
        // implemented in subclass
        return false;
    }

    @Override
    public boolean onFocusDelete() {
        return false;
    }

    @Override
    public boolean onFocusAdd() {
        return false;
    }

    @Override
    public int getTabIndex() {
        return FocusImpl.getFocusImplForPanel().getTabIndex(this.renderer.getElement());
    }

    @Override
    public void setAccessKey(char key) {
        FocusImpl.getFocusImplForPanel().setAccessKey(this.renderer.getElement(), key);
    }

    @Override
    public void setFocus(boolean focused) {
        if (focused) {
            FocusImpl.getFocusImplForPanel().focus(this.renderer.getElement());
        }
        else {
            FocusImpl.getFocusImplForPanel().blur(this.renderer.getElement());
        }
    }

    @Override
    public void setTabIndex(int tabIndex) {
        FocusImpl.getFocusImplForPanel().setTabIndex(this.renderer.getElement(), tabIndex);
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
