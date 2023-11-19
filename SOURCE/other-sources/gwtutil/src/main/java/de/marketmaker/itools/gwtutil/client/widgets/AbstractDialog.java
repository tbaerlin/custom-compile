package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;

/**
 * Author: umaurer
 * Created: 16.02.15
 */
public abstract class AbstractDialog implements DialogIfc {
    private final SimplePanel topWidgetPanel = new SimplePanel();
    private final SimplePanel bottomWidgetPanel = new SimplePanel();
    private final FlowPanel contentPanel = new FlowPanel();
    private final HorizontalPanel buttonBar = new HorizontalPanel();

    private InlineHTML message;
    private Widget widget;
    private Image image;
    private boolean keepOpen;
    private Widget focusWidget;
    private DialogButton defaultButton;
    private Command closeCommand;
    private Command escapeCommand;

    protected FlowPanel createPanel() {
        this.topWidgetPanel.setStyleName("mm-dlg-top");
        this.topWidgetPanel.setVisible(false);
        this.bottomWidgetPanel.setStyleName("mm-dlg-bottom");
        this.bottomWidgetPanel.setVisible(false);
        this.contentPanel.setStyleName("mm-dlg-c");
        this.buttonBar.setStyleName("mm-dlg-bb");
        this.buttonBar.setVisible(false);

        final FlowPanel panel = new FlowPanel();
        panel.setStyleName("mm-dlg");
        panel.add(this.topWidgetPanel);
        panel.add(this.contentPanel);
        panel.add(this.bottomWidgetPanel);
        panel.add(this.buttonBar);
        panel.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent e) {
                if (e.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                    keepOpen = false;
                    if (escapeCommand != null) {
                        escapeCommand.execute();
                    }
                    if (!keepOpen) {
                        closePopup();
                    }
                }
                else if (e.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    final EventTarget eventTarget = e.getNativeEvent().getEventTarget();
                    if (Element.is(eventTarget)) {
                        final Element elt = Element.as(eventTarget);
                        if ("TEXTAREA".equals(elt.getTagName().toUpperCase())) { // $NON-NLS$
                            return;
                        }
                    }
                    clickDefaultButton();
                }
            }
        }, KeyDownEvent.getType());
        return panel;
    }

    @Override
    public AbstractDialog withTopWidget(Widget topWidget) {
        this.topWidgetPanel.setVisible(topWidget != null);
        this.topWidgetPanel.setWidget(topWidget);
        return this;
    }

    @Override
    public DialogIfc withBottomWidget(Widget bottomWidget) {
        this.bottomWidgetPanel.setVisible(bottomWidget != null);
        this.bottomWidgetPanel.setWidget(bottomWidget);
        return null;
    }

    @Override
    public AbstractDialog withMessage(SafeHtml message) {
        if (this.message == null) {
            this.message = new InlineHTML(message);
            this.message.setStyleName("mm-dlg-m");
            this.contentPanel.add(this.message);
        }
        else {
            this.message.setHTML(message);
        }
        return this;
    }

    @Override
    public AbstractDialog withMessage(String message) {
        return withMessage(SafeHtmlUtils.fromString(message));
    }

    @Override
    public AbstractDialog withWidget(Widget widget) {
        if (this.widget != null) {
            this.widget.removeFromParent();
        }
        this.widget = widget;
        if (this.widget != null) {
            this.contentPanel.add(this.widget);
        }
        return this;
    }

    @Override
    public AbstractDialog withImage(Image image) {
        if (this.image != null) {
            this.image.removeFromParent();
        }
        this.image = image;
        if (this.image != null) {
            this.image.addStyleName("mm-dlg-i");
            this.contentPanel.insert(this.image, 0);
        }
        return this;
    }

    @Override
    public AbstractDialog withImage(AbstractImagePrototype image) {
        return withImage(image.createImage());
    }

    @Override
    public DialogIfc withImage(String iconClass) {
        return withImage(IconImage.get(iconClass));
    }

    @Override
    public void keepOpen() {
        this.keepOpen = true;
    }

    protected void onButtonClicked(final Command c) {
        this.keepOpen = false;
        if (c != null) {
            c.execute();
        }
        if (!this.keepOpen) {
            closePopup();
        }
    }

    protected abstract DialogButton createButton(String text, final Command c);

    private DialogButton addButton(DialogButton button) {
        this.buttonBar.setVisible(true);
        this.buttonBar.add((Widget) button);
        return button;
    }

    private DialogButton createButtonCheckFocus(String text, final Command c) {
        final DialogButton button = createButton(text, c);
        if (this.focusWidget == null) {
            this.focusWidget = (Widget) button;
        }
        return button;
    }

    @Override
    public DialogButton addButton(String text, Command c) {
        return addButton(createButtonCheckFocus(text, c));
    }

    @Override
    public AbstractDialog withButton(String text, final Command c) {
        addButton(text, c);
        return this;
    }

    @Override
    public DialogButton addButton(String text) {
        return addButton(text, null);
    }

    @Override
    public AbstractDialog withButton(String text) {
        addButton(text, null);
        return this;
    }

    @Override
    public DialogButton addDefaultButton(String text, Command c) {
        this.defaultButton = createButtonCheckFocus(text, c);
        addButton(this.defaultButton);
        return this.defaultButton;
    }

    @Override
    public DialogIfc withDefaultButton(String text, Command c) {
        addDefaultButton(text, c);
        return this;
    }

    private void clickDefaultButton() {
        if (this.defaultButton != null && this.defaultButton.isEnabled()) {
            clickButton((Widget) this.defaultButton);
        }
    }

    protected abstract void clickButton(Widget button);

    @Override
    public DialogIfc withCloseButton() {
        return withCloseButton("dlg-close");
    }

    @Override
    public AbstractDialog withCloseCommand(Command c) {
        this.closeCommand = c;
        return this;
    }

    @Override
    public AbstractDialog withEscapeCommand(Command c) {
        this.escapeCommand = c;
        return this;
    }

    @Override
    public AbstractDialog withFocusWidget(Widget w) {
        this.focusWidget = w;
        return this;
    }

    protected void deferredSetFocus(Widget widget) {
        WidgetUtil.deferredSetFocus(widget);
    }

    protected boolean setFocusToFocusWidget() {
        if (this.focusWidget != null) {
            deferredSetFocus(this.focusWidget);
            return true;
        }
        return false;
    }

    protected void fixBottomPadding() {
        if (this.bottomWidgetPanel.isVisible() || this.buttonBar.isVisible()) {
            this.contentPanel.removeStyleName("mm-dlg-c-bottomPadding");
        }
        else {
            this.contentPanel.addStyleName("mm-dlg-c-bottomPadding");
        }
    }

    protected abstract void doCloseDialog();

    @Override
    public void closePopup() {
        doCloseDialog();
        if (this.closeCommand != null) {
            this.closeCommand.execute();
        }
    }
}
