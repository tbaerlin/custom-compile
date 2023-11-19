package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.UIObject;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.Map;

/**
 * Author: umaurer
 * Created: 24.01.14
 */
public class SpsCombo extends SpsBoundWidget<SelectButton, SpsLeafProperty> {
    private final Map<String, String> mapEnum;
    private final String enumNullValue;
    private boolean menuLazilyLoaded = false;

    public SpsCombo(Map<String, String> mapEnum, String enumNullValue) {
        this.mapEnum = mapEnum;
        this.enumNullValue = enumNullValue;
    }

    public SpsCombo withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    @Override
    protected SelectButton createWidget() {
        Menu menu = new Menu();

        final boolean hasNullValue = hasEnumNullValue();

        if (!hasNullValue && !isMandatory()) {
            menu.addNoSelectionItem(new MenuItem(TextUtil.NO_SELECTION_TEXT));
        }

        final String selectedEnumValue;
        if (getBindFeature().getSpsProperty() != null) {
            selectedEnumValue = getBindFeature().getSpsProperty().getStringValue();
        }
        else {
            selectedEnumValue = null;
        }

        //only add the currently selected enum into the menu and probably the null item...
        //all other items are loaded lazily if one clicks the menu
        for (Map.Entry<String, String> entry : this.mapEnum.entrySet()) {
            final String key = entry.getKey();

            if (hasNullValue && key != null && key.equals(this.enumNullValue)) {
                if (isMandatory()) {
                    continue;
                }
                menu.addFirst(new MenuItem(TextUtil.NO_SELECTION_TEXT).withData("key", key)); // $NON-NLS$
            }
            else {
                if (key != null && key.equals(selectedEnumValue)) {
                    menu.add(new MenuItem(entry.getValue()).withData("key", key)); // $NON-NLS$
                    break;
                }
            }
        }

        final SelectButton selectButton = new SelectButton(Button.RendererType.SPAN)
                .withMenu(menu, false)
                .withClickOpensMenu()
                .withNoSelectionText(TextUtil.NO_SELECTION_TEXT)
                .withSelectionHandler(new SelectionHandler<MenuItem>() {
                    @Override
                    public void onSelection(SelectionEvent<MenuItem> event) {
                        final MenuItem selectedItem = event.getSelectedItem();
                        getBindFeature().getSpsProperty().setValue(getValue(selectedItem));
                    }
                });

        selectButton.setOnShowMenuCallback(new SelectButton.OnShowMenuCallback() {
            @Override
            public void onShowMenu(UIObject me, Menu menu, Command finishShowCommand) {
                if (SpsCombo.this.menuLazilyLoaded) {
                    finishShowCommand.execute();
                    return;
                }
                loadMenuLazily(menu, menu.getSelectedItem());
                SpsCombo.this.menuLazilyLoaded = true;
                finishShowCommand.execute();
            }
        });

        return selectButton;
    }

    private boolean hasEnumNullValue() {
        return StringUtil.hasText(this.enumNullValue);
    }

    private void loadMenuLazily(Menu menu, MenuItem selectedItem) {
        menu.removeAll(false);

        if (!hasEnumNullValue() && !isMandatory()) {
            menu.addNoSelectionItem(new MenuItem(TextUtil.NO_SELECTION_TEXT));
        }

        for (Map.Entry<String, String> entry : this.mapEnum.entrySet()) {
            final String key = entry.getKey();
            if (hasEnumNullValue() && key != null && key.equals(this.enumNullValue)) {
                if (isMandatory()) {
                    continue;
                }

                //reuse currently selected menu item
                if (selectedItem != null && key.equals(selectedItem.getData("key"))) {  // $NON-NLS$
                    menu.addFirst(selectedItem);
                    menu.setSelectedItem(selectedItem);
                }
                else {
                    menu.addFirst(new MenuItem(TextUtil.NO_SELECTION_TEXT).withData("key", key)); // $NON-NLS$
                }
            }
            else {
                //reuse currently selected menu item
                if (selectedItem != null && key != null && key.equals(selectedItem.getData("key"))) {  // $NON-NLS$
                    menu.add(selectedItem);
                    menu.setSelectedItem(selectedItem);
                }
                else {
                    menu.add(new MenuItem(entry.getValue()).withData("key", key)); // $NON-NLS$
                }
            }
        }
    }

    @Override
    public void onPropertyChange() {
        setValue(getBindFeature().getSpsProperty().getStringValue(), false);
    }

    private void setValue(String value, boolean fireEvent) {
        final SelectButton selectButton = getWidget();

        if (selectButton != null && !selectButton.setSelectedData("key", value, fireEvent)) { // $NON-NLS$
            //Just add the selected data as a new menu item if it is not present in the current menu.
            //The menu is loaded lazily if one clicks the SelectButton.
            //Then, this menu item will be reused in the resulting menu.
            final Menu menu = selectButton.getMenu();
            if (menu != null) {
                final SafeHtml label;
                if (value == null || hasEnumNullValue() && value.equals(this.enumNullValue)) {
                    label = TextUtil.NO_SELECTION_TEXT;
                }
                else {
                    label = SafeHtmlUtils.fromString(this.mapEnum.get(value));
                }

                menu.add(new MenuItem(label).withData("key", value)); // $NON-NLS$
                if (!selectButton.setSelectedData("key", value, fireEvent)) { // $NON-NLS$
                    selectButton.setSelectedItem(null, fireEvent); //should never happen
                }
            }
            else {
                selectButton.setSelectedItem(null, fireEvent);
            }
        }
    }

    private String getValue() {
        return getValue(getWidget().getSelectedItem());
    }

    private String getValue(MenuItem selectedItem) {
        return selectedItem == null ? null : (String) selectedItem.getData("key"); // $NON-NLS$
    }
}
