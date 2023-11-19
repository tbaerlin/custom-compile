package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Command;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;

/**
 * Author: umaurer
 * Created: 10.02.15
 */
public abstract class DialogImpl {
    public abstract DialogIfc createDialog();

    public DialogIfc confirm(String title, String iconStyle, SafeHtml message, Command yesCommand) {
        final DialogIfc dialog = createDialog()
                .withTitle(title)
                .withImage(IconImage.get(iconStyle))
                .withMessage(message)
                .withDefaultButton(I18n.I.yes2(), yesCommand)
                .withButton(I18n.I.no2())
                .withCloseButton();
        dialog.show();
        return dialog;
    }

    public DialogIfc box(String title, String iconStyle, SafeHtml message, Command okCommand) {
        final DialogIfc dialog = createDialog()
                .withTitle(title)
                .withImage(IconImage.get(iconStyle))
                .withMessage(message)
                .withDefaultButton(I18n.I.ok(), okCommand)
                .withCloseButton();
        dialog.show();
        return dialog;
    }
}
