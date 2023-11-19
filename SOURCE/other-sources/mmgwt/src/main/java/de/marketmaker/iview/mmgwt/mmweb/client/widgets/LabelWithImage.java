package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;

/**
 * @author Ulrich Maurer
 *         Date: 24.03.11
 */
public class LabelWithImage extends Composite {
    final FlowPanel panel = new FlowPanel();


    public LabelWithImage(String iconImageStyle, String text) {
        this(IconImage.get(iconImageStyle), text);
    }

    public LabelWithImage(AbstractImagePrototype imagePrototype, String text) {
        this.panel.setStyleName("mm-nobreak");
        this.panel.add(imagePrototype.createImage());
        this.panel.add(new InlineLabel(text));
        initWidget(this.panel);
    }
}
