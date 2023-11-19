/*
 * ViewSelectionViewButtons.java
 *
 * Created on 26.03.2008 11:57:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ViewSelectionViewCombo extends ViewSelectionView {
    private SelectButton button;

    public ViewSelectionViewCombo(IndexedViewSelectionModel model) {
        this(model, null);
    }

    public ViewSelectionViewCombo(IndexedViewSelectionModel model, FloatingToolbar toolbar) {
        super(model, toolbar);
        prepareButtons();
    }

    /*
     * This method does not support changing of visibility of single entries as ViewSelectionViewButtons does!!!
     */
    public void updateButtons() {
        if (this.button == null) {
            return;
        }
        final int selectedView = this.model.getSelectedView();
        final ViewSpec selectedViewSpec = this.model.getViewSpec(selectedView);
        this.button.setText(getShortText(selectedViewSpec.getName()));
    }

    protected void addButtons() {
        if (this.button != null) {
            this.toolbar.add(this.button);
        }
    }

    protected void initButtons() {
        final int viewCount = this.model.getViewCount();
        final Menu menu = new Menu();
        for (int i = 0; i < viewCount; i++) {
            final int viewId = i;
            final ViewSpec viewSpec = this.model.getViewSpec(i);
            final String name = viewSpec.getName();
            final MenuItem item = new MenuItem(name, new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    model.selectView(viewId);
                    button.setText(getShortText(name));
                }
            });
            if (viewSpec.getIconCls() != null) {
                IconImage.setIconStyle(item, viewSpec.getIconCls());
            }
            menu.add(item);
        }
        if (viewCount > 0) {
            this.button = new SelectButton().withMenu(menu);
            this.button.setClickOpensMenu(true);
            this.button.addStyleName("mm-viewSelection-combo"); // $NON-NLS-0$
        }
    }

    private String getShortText(String text) {
        if (text.length() > 40) {
            return text.substring(0, 37) + "..."; // $NON-NLS-0$
        }
        return text;
    }
}
