/*
 * ViewSelectionViewButtons.java
 *
 * Created on 26.03.2008 11:57:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.ButtonGroup;
import de.marketmaker.itools.gwtutil.client.widgets.Separator;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ViewSelectionViewButtons extends ViewSelectionView {
    protected Button[] buttons;
    private ButtonGroup buttonGroup;

    protected Separator[] separators;

    private int selected;

    public ViewSelectionViewButtons(IndexedViewSelectionModel model) {
        this(model, null);
    }

    public ViewSelectionViewButtons(IndexedViewSelectionModel model, FloatingToolbar toolbar) {
        super(model, toolbar);
        prepareButtons();
    }

    public void updateButtons() {
        final int selectedView = this.model.getSelectedView();
        final boolean unselected = this.model.isUnselected();
        this.selected = -1;
        for (int i = 0; i < this.buttons.length; i++) {
            final boolean pressed = i == selectedView && !unselected;
            final ViewSpec viewSpec = this.model.getViewSpec(i);
            this.buttons[i].setText(viewSpec.getName());
            if (viewSpec.getIconCls() != null) {
                IconImage.setIconStyle(this.buttons[i], viewSpec.getIconCls());
            }
            if (viewSpec.getTooltip() != null) {
                this.buttons[i].setTitle(viewSpec.getTooltip());
            }
            if (pressed) {
                this.buttonGroup.setSelected(this.buttons[i]);
            }
            this.buttons[i].setEnabled((this.model.isSelectable(i) || pressed));
            this.buttons[i].setVisible(this.model.isVisible(i));
            this.separators[i].setVisible(i > 0 && this.model.isVisible(i));
            if (pressed) {
                this.selected = i;
            }
        }
    }

    private void changeView() {
        if (this.selected >= 0) {
            this.model.selectView(this.selected);
        }
    }

    protected void addButtons() {
        for (int i = 0; i < this.buttons.length; i++) {
            this.toolbar.add(this.separators[i]);
            add(this.buttons[i]);
        }
    }

    protected void addSeparator() {
        this.toolbar.addSeparator();
    }

    protected void addSpacer() {
// TODO 2.0:        this.toolbar.addSpacer();
    }

    protected void add(Widget widget) {
        this.toolbar.add(widget);
    }

    private int getIndex(Button button) {
        for (int i = 0, length = buttons.length; i < length; i++) {
            if (button == buttons[i]) {
                return i;
            }
        }
        return (this.selected < 0) ? 0 : this.selected;
    }

    protected void initButtons() {
        this.buttonGroup = new ButtonGroup();
        this.buttonGroup.addSelectionHandler(new SelectionHandler<Button>() {
            @Override
            public void onSelection(SelectionEvent<Button> event) {
                selected = getIndex(event.getSelectedItem());
                changeView();
            }
        });
        this.buttons = new Button[this.model.getViewCount()];
        this.separators = new Separator[this.model.getViewCount()];

        for (int i = 0; i < this.buttons.length; i++) {
            final ViewSpec viewSpec = this.model.getViewSpec(i);
            this.buttons[i] = Button.text(viewSpec.getName()).icon(viewSpec.getIconCls()).tooltip(viewSpec.getTooltip()).build();
            this.buttonGroup.add(this.buttons[i]);
            this.separators[i] = new Separator();
        }
    }
}
