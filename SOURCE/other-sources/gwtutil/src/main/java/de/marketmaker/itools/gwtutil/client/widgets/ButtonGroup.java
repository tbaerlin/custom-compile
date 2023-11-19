package de.marketmaker.itools.gwtutil.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;

/**
 * @author Ulrich Maurer
 *         Date: 09.10.12
 */
public class ButtonGroup implements HasSelectionHandlers<Button> {
    private HandlerManager handlerManager = new HandlerManager(this);
    private List<Button> listButtons = new ArrayList<>();
    private Button selected = null;

    public ButtonGroup() {
    }

    public ButtonGroup(Button... buttons) {
        for (Button button : buttons) {
            add(button);
        }
    }

    public void add(final Button button) {
        this.listButtons.add(button);
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setSelected(button);
            }
        });
        if (button instanceof SelectButton) {
            ((SelectButton)button).addSelectionHandler(new SelectionHandler<MenuItem>() {
                @Override
                public void onSelection(SelectionEvent<MenuItem> event) {
                    setSelected(button);
                }
            });
        }
    }

    public void selectFirst() {
        if (this.listButtons.isEmpty()) {
            return;
        }
        setSelected(this.listButtons.get(0));
    }

    public void setSelected(Button button) {
        if (this.selected != null) {
            this.selected.setActive(false);
        }
        this.selected = button;
        if (button != null) {
            button.setActive(true);
            SelectionEvent.fire(this, button);
        }
    }

    public Button getSelected() {
        return selected;
    }

    public boolean setSelectedData(String key, Object value) {
        for (Button button : this.listButtons) {
            if (value.equals(button.getData(key))) {
                setSelected(button);
                return true;
            }
            if (button instanceof SelectButton) {
                if (((SelectButton)button).setSelectedData(key, value)) {
                    setSelected(button);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<Button> handler) {
        return this.handlerManager.addHandler(SelectionEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }
}
