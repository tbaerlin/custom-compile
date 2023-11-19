/*
 * Dialog.java
 *
 * Created on 27.02.15
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;

import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.itools.gwtutil.client.widgets.input.ValidationFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.Caption;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.TextBox;

/**
 * Factory for implementations of DialogIfc.
 * <p>The following styles may be useful:</p>
 * <ul>
 *     <li><code>mm-noPadding</code> removes the default padding around the content widget</li>
 *     <li><code>mm-noMaxWidth</code> removes the max width limit of the dialog</li>
 * </ul>
 *
 * @author umaurer
 */
public class Dialog {
    private static DialogImpl dialogImpl;

    public static DialogImpl getImpl() {
        if (dialogImpl == null) {
            if (SessionData.isAsDesign()) {
                dialogImpl = new DialogImpl() {
                    @Override
                    public DialogIfc createDialog() {
                        return new de.marketmaker.itools.gwtutil.client.widgets.Dialog();
                    }
                };
            }
            else {
                dialogImpl = new DialogImpl() {
                    @Override
                    public DialogIfc createDialog() {
                        return new com.extjs.gxt.ui.client.mm.Dialog();
                    }
                };
            }
        }
        return dialogImpl;
    }

    private Dialog() {
    }

    public static DialogIfc confirm(String message, final Command yesCommand) {
        return getImpl().confirm(I18n.I.confirmation(), "dialog-question", TextUtil.toSafeHtml(message), yesCommand); // $NON-NLS$
    }

    public static DialogIfc confirm(SafeHtml message, final Command yesCommand) {
        return getImpl().confirm(I18n.I.confirmation(), "dialog-question", message, yesCommand); // $NON-NLS$
    }

    public static DialogIfc confirm(String title, SafeHtml message, final Command yesCommand) {
        return getImpl().confirm(title, "dialog-question", message, yesCommand); // $NON-NLS$
    }

    public static DialogIfc confirm(String title, String message, final Command yesCommand) {
        return getImpl().confirm(title, "dialog-question", TextUtil.toSafeHtml(message), yesCommand); // $NON-NLS$
    }

    public static DialogIfc confirm(String title, String iconStyle, String message, final Command yesCommand) {
        return getImpl().confirm(title, iconStyle, TextUtil.toSafeHtml(message), yesCommand);
    }

    public static DialogIfc info(String message) {
        return getImpl().box(I18n.I.info(), "dialog-info", TextUtil.toSafeHtml(message), null); // $NON-NLS$
    }

    public static DialogIfc info(String title, String message) {
        return getImpl().box(title, "dialog-info", TextUtil.toSafeHtml(message), null); // $NON-NLS$
    }

    public static DialogIfc info(String title, SafeHtml message) {
        return getImpl().box(title, "dialog-info", message, null); // $NON-NLS$
    }

    public static DialogIfc info(String message, final Command okCommand) {
        return getImpl().box(I18n.I.info(), "dialog-info", TextUtil.toSafeHtml(message), okCommand); // $NON-NLS$
    }

    public static DialogIfc warning(SafeHtml message) {
        return getImpl().box(I18n.I.warning(), "dialog-warning", message, null); // $NON-NLS$
    }

    public static DialogIfc warning(String title, SafeHtml message, Command okCommand) {
        return getImpl().box(title, "dialog-warning", message, okCommand); // $NON-NLS$
    }

    public static DialogIfc warning(String title, String message, Command okCommand) {
        return getImpl().box(title, "dialog-warning", TextUtil.toSafeHtml(message), okCommand); // $NON-NLS$
    }

    public static DialogIfc error(SafeHtml message) {
        return getImpl().box(I18n.I.error(), "dialog-error", message, null); // $NON-NLS$
    }

    public static DialogIfc error(String message) {
        return getImpl().box(I18n.I.error(), "dialog-error", TextUtil.toSafeHtml(message), null); // $NON-NLS$
    }

    public static DialogIfc error(String title, String message) {
        return getImpl().box(title, "dialog-error", TextUtil.toSafeHtml(message), null); // $NON-NLS$
    }

    public static void prompt(String title, String label, String value, final PromptCallback callback) {
        prompt(title, label, value, null, null, callback);
    }

    public static void prompt(String title, String label, String value, String inputDescription, final ValidationFeature.Validator validator, final PromptCallback callback) {
        prompt (title, null, label, value, inputDescription, validator, callback);
    }

    public static void prompt(String title, String descriptionHtml, String label, String value, String inputDescription, final ValidationFeature.Validator validator, final PromptCallback callback) {
        final FlexTable table = new FlexTable();
        Styles.tryAddStyles(table, Styles.get().generalFormStyle());

        table.setCellSpacing(4);
        final TextBox tb = new TextBox();
        if (value != null) {
            tb.setValue(value);
            tb.addAttachHandler(event -> tb.selectAll());
        }
        if (validator != null) {
            new ValidationFeature<>(tb, validator);
        }
        table.setWidget(0, 0, new Caption(label));
        table.setWidget(0, 1, tb);
        table.getFlexCellFormatter().addStyleName(0, 1, "mm-dlg-c-prompt");
        if (inputDescription != null) {
            table.setText(1, 1, inputDescription);
        }

        if(StringUtil.hasText(descriptionHtml)) {
            table.insertRow(0);
            table.setHTML(0, 0, descriptionHtml);
            table.getFlexCellFormatter().setColSpan(0, 0, 2);
            table.getFlexCellFormatter().addStyleName(0, 0, "mm-dlg-c-prompt-description");
        }

        final SimplePanel view = Styles.tryAddStyles(new SimplePanel(), Styles.get().generalViewStyle());
        view.add(table);

        final DialogIfc dialog;
        (dialog = getImpl().createDialog())
                .withTitle(title)
                .withWidget(view)
                .withFocusWidget(tb)
                .withDefaultButton(I18n.I.ok(), () -> {
                    final String value1 = tb.getValue();
                    if (callback.isValid(value1)) {
                        callback.execute(value1);
                    }
                    else {
                        dialog.keepOpen();
                    }
                })
                .withButton(I18n.I.cancel())
                .withCloseButton()
                .show();
    }

    public static abstract class PromptCallback {
        protected boolean isValid(String value) {
            return true;
        }
        public abstract void execute(String value);
    }
}
