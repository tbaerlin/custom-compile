package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.DOMUtil;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;

/**
 * Author: umaurer
 * Created: 10.02.15
 */
public class Dialog extends AbstractDialog {
    private final PopupPanel popupPanel = new PopupPanel(false, true);
    private final PopupPositionCenter popupPositionCallback = new PopupPositionCenter(this.popupPanel);

    private FlowPanel header = new FlowPanel();
    private InlineHTML headerText = new InlineHTML();

    private Button closeButton;

    public Dialog() {
        final FlowPanel panel = createPanel();
        this.popupPanel.setGlassEnabled(true);
        this.popupPanel.setStyleName("mm-dlg-popup");
        this.popupPanel.setWidget(panel);
        this.popupPanel.addAttachHandler(attachEvent -> {
            setFocusToFocusWidget();
            fixBottomPadding();
        });
        this.popupPanel.addAttachHandler(event -> PopupPanelFix.addFrameDummy(Dialog.this.popupPanel));
        WidgetUtil.makeDraggable(this.popupPanel, this.header);
    }

    @Override
    protected FlowPanel createPanel() {
        this.header.setStyleName("mm-dlg-h");
        this.header.add(this.headerText);
        final FlowPanel panel = super.createPanel();
        panel.insert(this.header, 0);
        return panel;
    }

    @Override
    public DialogIfc withStyle(String styleName) {
        this.popupPanel.addStyleName(styleName);
        return this;
    }

    @Override
    public Dialog withTitle(SafeHtml title) {
        this.headerText.setHTML(title);
        return this;
    }

    @Override
    public Dialog withTitle(String title) {
        this.headerText.setText(title);
        return this;
    }

    @Override
    protected DialogButton createButton(String text, final Command c) {
        return Button.text(text).clickHandler(clickEvent -> onButtonClicked(c)).build();
    }

    @Override
    protected void clickButton(Widget button) {
        ((Button)button).click();
    }

    @Override
    public Dialog withCloseButton(String iconClass) {
        if (this.closeButton != null) {
            throw new IllegalStateException("closeButton is already initialized");
        }
        this.closeButton = Button.icon(iconClass)
                .clickHandler(clickEvent -> closePopup())
                .build();
        this.closeButton.addStyleName("mm-dlg-hb"); // hb = headerButton
        this.header.add(this.closeButton);
        return this;
    }

    protected boolean setFocusToFocusWidget() {
        if (!super.setFocusToFocusWidget()) {
            deferredSetFocus(this.closeButton);
            return true;
        }
        return false;
    }

    @Override
    public void show() {
        DOMUtil.setTopZIndex(this.popupPanel);
        this.popupPanel.setPopupPositionAndShow(this.popupPositionCallback);
    }

    @Override
    protected void doCloseDialog() {
        this.popupPanel.hide();
    }
}
