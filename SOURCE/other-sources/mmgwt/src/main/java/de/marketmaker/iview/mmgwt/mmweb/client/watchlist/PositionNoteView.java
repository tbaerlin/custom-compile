package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;


import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.TextArea;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.itools.gwtutil.client.widgets.input.LimitedTextArea;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;

public class PositionNoteView {
    public static final PositionNoteView INSTANCE = new PositionNoteView();

    interface Presenter {
        void setContent(String content);
    }

    private final DialogIfc dialog;
    private Presenter presenter;
    private final TextArea textArea;

    private PositionNoteView() {
        this.textArea = new LimitedTextArea(255);
        this.textArea.setWidth("228px"); // $NON-NLS-0$
        this.textArea.setHeight("208px"); // $NON-NLS-0$
        this.dialog = Dialog.getImpl().createDialog()
                .withTitle(I18n.I.comment())
                .withWidget(this.textArea)
                .withDefaultButton(I18n.I.ok(), new Command() {
                    @Override
                    public void execute() {
                        presenter.setContent(textArea.getText());
                    }
                })
                .withButton(I18n.I.cancel())
                .withFocusWidget(this.textArea);
    }

    public void showDialog(final String content, Presenter presenter) {
        this.presenter = presenter;
        this.textArea.setText(content);
        this.dialog.show();
        this.textArea.setCursorPos(content.length());
    }
}
