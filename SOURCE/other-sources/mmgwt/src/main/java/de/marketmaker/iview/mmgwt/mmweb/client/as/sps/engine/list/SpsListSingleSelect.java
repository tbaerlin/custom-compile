package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.pmxml.ListWidgetDescColumn;
import de.marketmaker.iview.pmxml.MM;

import java.util.List;

/**
 * Author: umaurer
 * Created: 03.04.14
 */
public class SpsListSingleSelect<P extends SpsProperty> extends SpsBoundWidget<SelectButton, P> {
    private final String columnsKeyField;
    private final List<ListWidgetDescColumn> columns;
    private final SpsListBindFeature spsListBindFeature;
    private final Context context;
    private Menu menu = new Menu();

    public SpsListSingleSelect(final Context context, final BindToken parentToken, final BindToken itemsBindToken, String columnsKeyField, List<ListWidgetDescColumn> columns) {
        this.context = context;
        this.spsListBindFeature = new SpsListBindFeature(context, parentToken, itemsBindToken) {
            @Override
            public void onChange() {
                onItemsChange();
            }
        };
        this.columnsKeyField = columnsKeyField;
        this.columns = columns;
    }

    @Override
    public void release() {
        super.release();
        this.spsListBindFeature.release();
    }

    private void onItemsChange() {
        this.menu.removeAll(false);
        final SpsListProperty itemListProperty = this.spsListBindFeature.getSpsProperty();
        boolean hasNullValue = false;
        for (SpsProperty spsProperty : itemListProperty.getChildren()) {
            if (!(spsProperty instanceof SpsGroupProperty)) {
                throw new IllegalStateException("not group property: " + spsProperty.getBindToken()); // $NON-NLS$
            }
            final SpsGroupProperty gp = (SpsGroupProperty) spsProperty;
            final SpsLeafProperty spsLeafProperty = (SpsLeafProperty) gp.get(this.columnsKeyField);
            final String key = spsLeafProperty.getStringValue();
            final MM mm = spsLeafProperty.getDataItem();
            this.menu.add(new MenuItem(getText(gp, this.columns)).withData("key", key).withData("mm", mm)); // $NON-NLS$
            if (key == null) {
                hasNullValue = true;
            }
        }
        if (!(isMandatory() || hasNullValue)) {
            this.menu.addNoSelectionItem(new MenuItem(TextUtil.NO_SELECTION_TEXT));
        }
        onPropertyChange();
    }

    static String getText(SpsGroupProperty gp, List<ListWidgetDescColumn> columns) {
        final StringBuilder sb = new StringBuilder();
        String comma = "";
        for (ListWidgetDescColumn column : columns) {
            sb.append(comma);
            final SpsLeafProperty cellProperty = (SpsLeafProperty) gp.get(column.getFieldName());
            sb.append(cellProperty.getStringValue());
            comma = ", ";
        }
        return sb.toString();
    }

    @Override
    public void onPropertyChange() {
        final String key = getPropertyKey();
        if (!getWidget().setSelectedData("key", key, false)) { // $NON-NLS$
            getWidget().setSelectedItem(null, false);
        }
    }

    public String getPropertyKey() {
        final P p = getBindFeature().getSpsProperty();
        if (p instanceof SpsLeafProperty) {
            return ((SpsLeafProperty) p).getStringValue();
        }
        else if (p instanceof SpsListProperty) {
            final SpsListProperty lp = (SpsListProperty) p;
            if (lp.getChildCount() < 1) {
                return null;
            }
            return ((SpsLeafProperty) lp.get(0)).getStringValue();
        }
        return null;
    }

    @Override
    protected SelectButton createWidget() {
        final SelectButton selectButton = new SelectButton(Button.RendererType.SPAN)
                .withMenu(this.menu, false)
                .withNoSelectionText(TextUtil.NO_SELECTION_TEXT)
                .withClickOpensMenu()
                .withSelectionHandler(new SelectionHandler<MenuItem>() {
                    @Override
                    public void onSelection(SelectionEvent<MenuItem> event) {
                        setPropertyValue(event.getSelectedItem() == null
                                ? null
                                : (MM) event.getSelectedItem().getData("mm")); // $NON-NLS$
                    }
                });
        selectButton.addStyleName("sps-list-single");
        return selectButton;
    }

    private void setPropertyValue(MM mm) {
        final P p = getBindFeature().getSpsProperty();
        if (p instanceof SpsLeafProperty) {
            ((SpsLeafProperty) p).setValue(mm, true, true);
        }
        else if (p instanceof SpsListProperty) {
            final SpsListProperty lp = (SpsListProperty) p;
            lp.clear(false);
            final SpsLeafProperty leafProperty = new SpsLeafProperty("", lp, SpsUtil.getChildParsedTypeInfo(this.context, lp));
            leafProperty.setValue(mm, true, true);
            lp.add(leafProperty, true);
        }
    }
}