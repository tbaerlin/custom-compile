package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.extjs.gxt.ui.client.widget.MessageBox;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * Author: umaurer
 * Created: 15.08.14
 */
public class InitSequenceProgressBoxLegacy implements InitSequenceProgressBox {
    private final MessageBox box;

    public InitSequenceProgressBoxLegacy() {
        this.box = MessageBox.progress(I18n.I.initializing(), I18n.I.pleaseWait(), I18n.I.initializing() + "...");  // $NON-NLS$
        this.box.getDialog().addStyleName("mm-styledDialog");
    }

    @Override
    public void update(String message, int step, int count) {
        double progress = (double)step / count;
        this.box.getProgressBar().updateProgress(progress, message);

    }

    @Override
    public void close() {
        this.box.close();
    }
}
