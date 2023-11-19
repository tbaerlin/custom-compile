/*
 * SpsSelectDepotSymbolPicker.java
 *
 * Created on 28.07.2014 10:03
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.context.DmxmlContextFacade;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.SecurityAccountBalanceItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.BlockAndTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author mdick
 */
public class SpsSelectDepotSymbolPicker extends SpsObjectBoundWidget<SelectButton, SpsLeafProperty, SpsLeafProperty>
        implements AsyncCallback<ResponseType> {
    private static final String KEY = "data";  // $NON-NLS$

    private final DmxmlContextFacade dmxmlContextFacade;
    private final BlockAndTalker<SecurityAccountBalanceItem.Talker, List<SecurityAccountBalanceItem>, SecurityAccountBalanceItem> bat;

    private final Menu menu = new Menu();

    private final SelectButton button = new SelectButton()
            .withIcon("pm-investor-depot") // $NON-NLS$
            .withMenu(this.menu)
            .withClickOpensMenu();

    private ShellMMInfo defaultMainInput;
    private final DmxmlContext dmxmlContext;

    public SpsSelectDepotSymbolPicker(Supplier<DmxmlContextFacade> dmxmlContextFacadeSupplier) {
        this.dmxmlContextFacade = dmxmlContextFacadeSupplier.get();
        this.dmxmlContext = this.dmxmlContextFacade.createOrGetContext(1);
        this.bat = new BlockAndTalker<>(dmxmlContext, new SecurityAccountBalanceItem.Talker());
        this.bat.setPrivacyModeActive(PrivacyMode.isActive());
        this.dmxmlContext.setCancellable(false);

        this.dmxmlContextFacade.subscribe(this.dmxmlContext, this);

        this.button.setEnabled(false);
        this.button.addSelectionHandler(event -> updateProperty(event.getSelectedItem()));
    }

    public SpsSelectDepotSymbolPicker withVisible(boolean visible) {
        this.bat.getBlock().setEnabled(visible);
        this.button.setVisible(visible);
        return this;
    }

    @Override
    public void release() {
        this.dmxmlContextFacade.unsubscribe(this.dmxmlContext, this);
        this.dmxmlContext.removeBlock(this.bat.getBlock());
    }

    @Override
    public void onPropertyChange() {
        this.button.setSelectedItem(null, false);
    }

    @Override
    public void onObjectPropertyChange() {
        final String databaseId = resolveDatabaseId();

        if(!StringUtil.hasText(databaseId)) {
            Firebug.warn("<SpsSelectDepotSymbolPicker.onObjectPropertyChange> failed to resolve database ID!");
            return;
        }

        this.bat.getBlock().setEnabled(true);
        this.bat.setDatabaseId(databaseId);
        this.dmxmlContextFacade.reload();
    }

    private String resolveDatabaseId() {
        final SpsLeafProperty property = getObjectBindFeature().getSpsProperty();
        if(property != null) {
            if(property.isShellMMInfo()) {
                final ShellMMInfo shellMMInfo = property.getShellMMInfo();
                if(shellMMInfo != null) {
                    if (ShellMMType.ST_DEPOT != shellMMInfo.getTyp()) {
                        Firebug.warn("<SpsSelectDepotSymbolPicker.onObjectPropertyChange> bound secondary object is not of type ST_DEPOT");
                    }
                    return shellMMInfo.getId();
                }
            }
            return property.getStringValue();
        }

        if(this.defaultMainInput != null) {
            final String id = this.defaultMainInput.getId();
            Firebug.info("<SpsSelectDepotSymbolPicker.onObjectPropertyChange> using activity's main input ("+ id + "/" + this.defaultMainInput.getTyp() + ") as fallback");
            return id;
        }
        return null;
    }

    @Override
    public void onFailure(Throwable caught) {
        Firebug.error("<SpsSelectDepotSymbolPicker.onObjectPropertyChange.onFailure> Query security account balance failed!", caught);
        this.button.setEnabled(false);
    }

    @Override
    public void onSuccess(ResponseType result) {
        if(!this.bat.getBlock().isEnabled()) {
            return;
        }

        this.button.setEnabled(false);

        if(!this.bat.getBlock().isResponseOk()) {
            Firebug.error("<SpsSelectDepotSymbolPicker.onObjectPropertyChange.onSuccess> Query security account balance failed!");
        }

        this.menu.removeAll(false);
        this.button.setSelectedItem(null, false);

        final List<SecurityAccountBalanceItem> items = this.bat.createResultObject();
        if(items == null) {
            return;
        }

        this.button.setEnabled(!items.isEmpty());

        for(SecurityAccountBalanceItem item : items) {
            final StringBuilder sb = new StringBuilder();
            final ShellMMInfo instrument = item.getInstrument();
            sb.append(instrument.getBezeichnung()).append(" (").append(instrument.getISIN()).append(")");
            final String total = item.getTotal();
            if(StringUtil.hasText(total)) {
                sb.append(", ").append(Renderer.PRICE_0MAX5.render(total))
                        .append(" ").append(I18n.I.orderEntryAmountNominal());
            }

            final String locked = item.getLocked();
            if(!"0".equals(locked)) {  // $NON-NLS$
                sb.append(", ")
                        .append(I18n.I.orderEntryAmountNominalLocked(Renderer.PRICE_0MAX5.render(locked)))
                        .append(", ")
                        .append(I18n.I.orderEntryAmountNominalSaleable(Renderer.PRICE_0MAX5.render(item.getSaleable())));
            }

            MenuItem menuItem = new MenuItem(sb.toString());
            menuItem.setEnabled(true);
            menuItem.setData(KEY, item.getInstrument());
            this.menu.add(menuItem);
        }

        this.bat.getBlock().setEnabled(false);
    }

    private void updateProperty(MenuItem selectedItem) {
        this.button.setSelectedItem(null, false);
        updateButtonCaption();

        if(selectedItem == null) {
            return;
        }

        final SpsLeafProperty spsProperty = getBindFeature().getSpsProperty();
        if(spsProperty != null) {
            spsProperty.setValue((ShellMMInfo) selectedItem.getData(KEY));
        }
    }

    @Override
    protected SelectButton createWidget() {
        updateButtonCaption();
        return this.button;
    }

    private void updateButtonCaption() {
        if(hasCaption()) {
            this.button.setHTML(getCaption());
        }
    }

    public SpsSelectDepotSymbolPicker withMainInput(ShellMMInfo mainInput) {
        this.defaultMainInput = mainInput;
        return this;
    }

    public SpsObjectBoundWidget withoutObjectBind(boolean withoutObjectBind) {
        if(withoutObjectBind) {
            onObjectPropertyChange();
        }
        return this;
    }
}
