/*
 * AbstractUpdateMenuMethod.java
 *
 * Created on 18.02.2016 14:33
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.gwt.user.client.Command;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainView;
import de.marketmaker.iview.mmgwt.mmweb.client.MenuModel;
import de.marketmaker.iview.mmgwt.mmweb.client.MainView;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
@NonNLS
public abstract class AbstractUpdateMenuMethod<P extends AbstractUpdateMenuMethod<P, T>, T>
        implements Command {

    protected abstract String getId(T element);

    protected abstract String getName(T element);

    private final String targetMenuId;

    private final String hiddenChildId;

    private final String hiddenChildName;

    private final String childIdPrefix;

    private final String childControllerId;

    private final Supplier<List<T>> elementSupplier;

    private final Supplier<MenuModel> menuModelSupplier = AbstractMainController.INSTANCE::getMenuModel;

    private Optional<List<T>> elements = Optional.empty();

    private Optional<MenuModel> menuModel = Optional.empty();

    private Optional<Command> onAfterExecute = Optional.of((Command) () -> {
        final AbstractMainView view = AbstractMainController.INSTANCE.getView();
        if (view instanceof MainView) {
            ((MainView) view).syncMenuModelAndModuleIcons();
        }
    });

    public AbstractUpdateMenuMethod(String targetMenuId, String hiddenChildId,
            String hiddenChildName, String childIdPrefix,
            String childControllerId, Supplier<List<T>> elementSupplier) {
        this.targetMenuId = targetMenuId;
        this.hiddenChildId = hiddenChildId;
        this.hiddenChildName = hiddenChildName;
        this.childIdPrefix = childIdPrefix;
        this.childControllerId = childControllerId;
        this.elementSupplier = elementSupplier;
    }

    @SuppressWarnings("unchecked")
    public P withElements(List<T> elements) {
        this.elements = Optional.ofNullable(elements);
        return (P) this;
    }

    @SuppressWarnings("unchecked")
    public P withMenuModel(MenuModel menuModel) {
        this.menuModel = Optional.ofNullable(menuModel);
        return (P) this;
    }

    @SuppressWarnings("unchecked")
    public P doNotUpdateView() {
        this.onAfterExecute = Optional.empty();
        return (P) this;
    }

    @Override
    public void execute() {
        if (!SessionData.isAsDesign()) {
            throw new UnsupportedOperationException("only supported in ICE/AS design");
        }

        final MenuModel menuModel = this.menuModel.orElseGet(this.menuModelSupplier);
        final MenuModel.Item root = menuModel.getElement(this.targetMenuId);
        if (root == null) {
            Firebug.warn("<" + this.getClass().getSimpleName() + ".execute> Failed to update menu items: target menu with ID " + this.targetMenuId + " not found.");
            return;
        }
        menuModel.clearItems(root);

        for (T element : this.elements.orElse(this.elementSupplier.get())) {
            root.add(menuModel.createItem(getMenuItemId(element), getControllerId(element), getName(element), null, isEnabled(element)));
        }
        root.add(menuModel.createItem(this.hiddenChildId, this.hiddenChildId, this.hiddenChildName, null, true).hide());

        //do not use Ideas conversion here: compiled code will throw a ReferenceError (due to a failing cast)
        //noinspection Convert2MethodRef
        this.onAfterExecute.ifPresent(command -> command.execute());
    }

    protected boolean isEnabled(@SuppressWarnings("UnusedParameters") T element) {
        return true;
    }

    protected String getControllerId(T element) {
        return this.childControllerId + "/" + getId(element);
    }

    protected String getMenuItemId(T element) {
        return this.childIdPrefix + "_" + getId(element);
    }
}
