package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.MainView;

import static com.google.gwt.dom.client.Style.Position.ABSOLUTE;
import static com.google.gwt.dom.client.Style.Unit.PCT;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 15.08.14
 */
public class InitSequenceProgressBoxAs implements InitSequenceProgressBox {
    private final Panel panelGlass;
    private final Label labelProgressBar = new Label();

    public InitSequenceProgressBoxAs() {
        this.panelGlass = new FlowPanel();
        this.panelGlass.getElement().getStyle().setZIndex(10);
        final Style sG = this.panelGlass.getElement().getStyle();
        sG.setPosition(ABSOLUTE);
        sG.setTop(0, PX);
        sG.setRight(0, PX);
        sG.setBottom(0, PX);
        sG.setLeft(0, PX);
        sG.setWidth(100, PCT);
        sG.setHeight(100, PCT);
        sG.setOpacity(0);

        final Label labelProgressBottom = new Label();
        labelProgressBottom.setStyleName("as-southPanel");
        this.panelGlass.add(labelProgressBottom);
        final Style sB = labelProgressBottom.getElement().getStyle();
        sB.setPosition(ABSOLUTE);
        sB.setBottom(0, PX);
        sB.setRight(0, PX);
        sB.setLeft(0, PX);
        sB.setHeight(MainView.FOOTER_HEIGHT, PX);
        sB.setWidth(100, PCT);
        sB.setPaddingTop(0, PX);

        RootPanel.get().insert(this.panelGlass, 0);

        this.labelProgressBar.setStyleName("as-southPanel");
        final Style sP = this.labelProgressBar.getElement().getStyle();
        sP.setPosition(ABSOLUTE);
        sP.setBottom(0, PX);
//        sP.setProperty("right", "auto");
        sP.setLeft(0, PX);
        sP.setHeight(MainView.FOOTER_HEIGHT, PX);
        sP.setWidth(0, PCT);
        sP.setPaddingTop(0, PX);
        RootPanel.get().insert(this.labelProgressBar, 1);
    }

    @Override
    public void update(String message, int step, int count) {
        double progress = (double)step / count;
        Firebug.info("InitSequenceProgressBox.update(" + step + "/" + count + "): " + message);
        this.panelGlass.getElement().getStyle().setOpacity(progress);
        this.labelProgressBar.getElement().getStyle().setWidth(progress * 100, PCT);
    }

    @Override
    public void close() {
        this.panelGlass.removeFromParent();
        this.labelProgressBar.removeFromParent();
    }
}
