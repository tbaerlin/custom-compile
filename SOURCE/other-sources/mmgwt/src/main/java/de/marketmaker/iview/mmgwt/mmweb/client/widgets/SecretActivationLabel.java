package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.core.client.Duration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Label;

import de.marketmaker.itools.gwtutil.client.event.KeyModifiers;

/**
 * @author umaurer
 */
public class SecretActivationLabel extends Label {
    private double time = 0l;

    private Command doubleclickCommand;

    private Command secretCommand;

    public SecretActivationLabel() {
        this("");
    }

    public SecretActivationLabel(String text) {
        super(text);
        sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEUP);
    }

    public void setDoubleclickCommand(Command command) {
        this.doubleclickCommand = command;
    }

    public void setSecretCommand(Command command) {
        this.secretCommand = command;
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONMOUSEDOWN:
                if (KeyModifiers.isCtrl(event) && event.getButton() == Event.BUTTON_LEFT) {
                    if (Duration.currentTimeMillis() - this.time < 500
                            && this.doubleclickCommand != null) {
                        this.doubleclickCommand.execute();
                    }
                    this.time = Duration.currentTimeMillis();
                }
                break;
            case Event.ONMOUSEUP:
                if (KeyModifiers.isShift(event) && event.getButton() == Event.BUTTON_LEFT
                        && Duration.currentTimeMillis() - this.time < 2000
                        && this.secretCommand != null) {
                    this.secretCommand.execute();
                }
                break;
        }
    }

}
