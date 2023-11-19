package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Author: umaurer
 * Created: 12.02.15
 */
public interface DialogIfc {

    DialogIfc withStyle(String styleName);

    DialogIfc withTitle(SafeHtml title);

    DialogIfc withTitle(String title);

    DialogIfc withTopWidget(Widget widget);

    DialogIfc withBottomWidget(Widget widget);

    DialogIfc withMessage(SafeHtml message);

    DialogIfc withMessage(String message);

    DialogIfc withWidget(Widget widget);

    DialogIfc withImage(String iconClass);

    DialogIfc withImage(AbstractImagePrototype image);

    DialogIfc withImage(Image image);

    DialogButton addButton(String text, Command c);
    DialogIfc withButton(String text, Command c);

    DialogButton addButton(String text);
    DialogIfc withButton(String text);

    DialogButton addDefaultButton(String text, Command c);
    DialogIfc withDefaultButton(String text, Command c);

    DialogIfc withCloseButton(String iconClass);
    DialogIfc withCloseButton();

    DialogIfc withCloseCommand(Command c);

    DialogIfc withEscapeCommand(Command c);

    DialogIfc withFocusWidget(Widget w);

    void show();

    void keepOpen();

    void closePopup();
}
