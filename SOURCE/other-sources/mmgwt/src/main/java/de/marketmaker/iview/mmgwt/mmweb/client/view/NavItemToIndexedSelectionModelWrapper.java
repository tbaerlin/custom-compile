package de.marketmaker.iview.mmgwt.mmweb.client.view;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import de.marketmaker.itools.gwtutil.client.util.Firebug;

/**
 * @author Ulrich Maurer
 *         Date: 13.12.12
 */
public class NavItemToIndexedSelectionModelWrapper implements NavItemSelectionModel {
    private final String viewGroup;
    private List<NavItemSpec> listNavItems;
    private IndexedViewSelectionModelImpl viewSelectionModel;
    private HandlerManager handlerManager = new HandlerManager(this);

    public NavItemToIndexedSelectionModelWrapper(final NavItemSpec root) {
        this(root, DOM.createUniqueId());
    }

    public NavItemToIndexedSelectionModelWrapper(NavItemSpec root, String viewGroup) {
        this.viewGroup = viewGroup;
        this.listNavItems = new ArrayList<NavItemSpec>();
        addToList(this.listNavItems, root);
        final IndexedViewSelectionModel.Callback callback = new IndexedViewSelectionModel.Callback() {
            @Override
            public void onViewChanged() {
                fireSelectionEvent();
            }
        };
        this.viewSelectionModel = new IndexedViewSelectionModelImpl(callback, asViewSpecs(this.listNavItems), 0, viewGroup);
    }

    public IndexedViewSelectionModelImpl getViewSelectionModel() {
        return this.viewSelectionModel;
    }

    @Override
    public void doUpdate() {
        final int selectedView = this.viewSelectionModel.getSelectedView();
        this.viewSelectionModel.update(asViewSpecs(this.listNavItems), selectedView, this.viewGroup);
        for (NavItemSpec listNavItem : this.listNavItems) {
            setSelectable(listNavItem, listNavItem.isEnabled());
        }
    }

    private void addToList(List<NavItemSpec> list, NavItemSpec navItemSpec) {
        if (navItemSpec.getController() != null) {
            list.add(navItemSpec);
        }
        if (navItemSpec.getChildren() == null) {
            return;
        }
        for (NavItemSpec child : navItemSpec.getChildren()) {
            addToList(list, child);
        }
    }

    private ViewSpec[] asViewSpecs(List<NavItemSpec> list) {
        final ViewSpec[] result = new ViewSpec[list.size()];
        for (int i = 0, length = list.size(); i < length; i++) {
            final NavItemSpec navItemSpec = list.get(i);
            result[i] = new ViewSpec(navItemSpec.getId(), navItemSpec.getName(), null, null);
        }
        return result;
    }

    private int getViewIndex(NavItemSpec navItemSpec) {
        return getViewIndex(navItemSpec, -1);
    }

    private int getViewIndex(NavItemSpec navItemSpec, int defaultValue) {
        for (int i = 0; i < this.listNavItems.size(); i++) {
            if (navItemSpec == listNavItems.get(i)) {
                return i;
            }
        }
        return defaultValue;
    }

    @Override
    public void setVisibility(NavItemSpec navItemSpec, boolean visible) {
        if (navItemSpec == null) {
            return;
        }
        final int n = getViewIndex(navItemSpec);
        if (n != -1) {
            this.viewSelectionModel.setVisible(n, visible);
        }
    }

    @Override
    public boolean isVisible(NavItemSpec navItemSpec) {
        final int n = getViewIndex(navItemSpec);
        return n != -1 && this.viewSelectionModel.isVisible(n);
    }

    @Override
    public NavItemSpec getSelected() {
        final int selectedView = this.viewSelectionModel.getSelectedView();
        if (selectedView == -1) {
               return null;
        }
        return this.listNavItems.get(selectedView);
    }

    @Override
    public void setSelected(NavItemSpec navItemSpec, boolean fireEvents) {
        this.viewSelectionModel.selectView(getViewIndex(navItemSpec), false);
    }

    @Override
    public void setSelected(NavItemSpec navItemSpec, boolean fireEvents, boolean fireEvenIfAlreadySelected) {
        Firebug.warn("<NavItemToIndexedSelectionModelWrapper.setSelected> parameter fireEvenIfAlreadySelected is ignored!");
        setSelected(navItemSpec, fireEvents);
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<NavItemSpec> handler) {
        return this.handlerManager.addHandler(SelectionEvent.getType(), handler);
    }

    private void fireSelectionEvent() {
        SelectionEvent.fire(this, getSelected());
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    @Override
    public void setSelectable(NavItemSpec navItemSpec, boolean selectable) {
        final int n = getViewIndex(navItemSpec);
        this.viewSelectionModel.setSelectable(n, selectable);
    }

    @Override
    public boolean isSelectable(NavItemSpec navItemSpec) {
        final int n = getViewIndex(navItemSpec);
        return n != -1 && this.viewSelectionModel.isSelectable(n);
    }
}
