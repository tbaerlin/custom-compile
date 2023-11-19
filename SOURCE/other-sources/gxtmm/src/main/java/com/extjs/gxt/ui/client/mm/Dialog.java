package com.extjs.gxt.ui.client.mm;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.AbstractDialog;
import de.marketmaker.itools.gwtutil.client.widgets.DialogButton;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.itools.gwtutil.client.widgets.PopupPanelFix;

/**
 * Author: umaurer
 * Created: 12.02.15
 */
public class Dialog extends AbstractDialog {
    private final Window window = new Window();


    public Dialog() {
        final FlowPanel panel = createPanel();

        this.window.addStyleName("gxt-dlg-popup");
        this.window.add(panel);
        this.window.setClosable(false);
        this.window.setAutoWidth(true);
        this.window.setAutoHeight(true);
        this.window.setModal(true);
        this.window.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent attachEvent) {
                setFocusToFocusWidget();
                fixBottomPadding();
            }
        });
        this.window.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                PopupPanelFix.addFrameDummy(Dialog.this.window);
            }
        });
    }

    @Override
    public DialogIfc withStyle(String styleName) {
        this.window.addStyleName(styleName);
        return this;
    }

    @Override
    public Dialog withTitle(SafeHtml title) {
        this.window.setHeading(title.asString());
        return this;
    }

    @Override
    public Dialog withTitle(String title) {
        this.window.setHeading(SafeHtmlUtils.htmlEscape(title));
        return this;
    }

    @Override
    protected DialogButton createButton(String text, final Command c) {
        return new Button(SafeHtmlUtils.htmlEscape(text), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                onButtonClicked(c);
            }
        });
    }

    @Override
    protected void clickButton(Widget button) {
        WidgetUtil.click(button);
    }

    @Override
    public Dialog withCloseButton(String iconClass) {
        this.window.setClosable(true);
        return this;
    }

    @Override
    protected void deferredSetFocus(final Widget widget) {
        if (widget instanceof Component) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    ((Component) widget).focus();
                }
            });
        }
        else {
            WidgetUtil.deferredSetFocus(widget);
        }
    }

    @Override
    public void show() {
        this.window.show();
    }

    @Override
    protected void doCloseDialog() {
        this.window.hide();
    }
}
