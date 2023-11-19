package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;

/**
 * @author Ulrich Maurer
 *         Date: 13.12.12
 */
public interface NavItemSelectionModel extends HasSelectionHandlers<NavItemSpec> {
    void setVisibility(NavItemSpec navItemSpec, boolean visible);
    boolean isVisible(NavItemSpec navItemSpec);
    void setSelectable(NavItemSpec navItemSpec, boolean selectable);
    boolean isSelectable(NavItemSpec navItemSpec);
    NavItemSpec getSelected();
    void setSelected(NavItemSpec navItemSpec, boolean fireEvents);
    void setSelected(NavItemSpec navItemSpec, boolean fireEvents, boolean fireEvenIfAlreadySelected);
    void doUpdate();
}
