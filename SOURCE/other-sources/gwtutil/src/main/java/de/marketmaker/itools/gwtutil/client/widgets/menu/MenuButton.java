package de.marketmaker.itools.gwtutil.client.widgets.menu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.event.ItemAddedEvent;
import de.marketmaker.itools.gwtutil.client.event.ItemAddedHandler;
import de.marketmaker.itools.gwtutil.client.event.ItemRemovedEvent;
import de.marketmaker.itools.gwtutil.client.event.ItemRemovedHandler;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;

/**
 * @author Ulrich Maurer
 *         Date: 01.10.12
 */
public class MenuButton extends Button {
    private Menu menu;

    private boolean menuOpen = false;

    private boolean clickOpensMenu = false;

    private OnShowMenuCallback onShowMenuCallback;

    public MenuButton() {
    }

    public MenuButton(RendererType rendererType) {
        super(rendererType);
    }

    public MenuButton(String text) {
        super(text);
    }

    @Override
    public MenuButton withIcon(String iconMapping) {
        setIcon(iconMapping);
        return this;
    }

    public MenuButton withTooltip(String tooltip) {
        Tooltip.addQtip(this, tooltip);
        return this;
    }

    @Override
    public MenuButton withClickHandler(ClickHandler clickHandler) {
        super.withClickHandler(clickHandler);
        return this;
    }

    public MenuButton withMenu(Menu menu) {
        if (this.menu != null) {
            throw new IllegalStateException("menu is already initialized");
        }
        this.menu = menu;
        this.menu.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                menuOpen = false;
                updateStyle();
            }
        });
        final MenuAddRemoveHandler menuAddRemoveHandler = new MenuAddRemoveHandler();
        this.menu.addItemAddedHandler(menuAddRemoveHandler);
        this.menu.addItemRemovedHandler(menuAddRemoveHandler);
        if (this.menu.getItemCount() == 0) {
            getRightContentWidget().setStyleName("menu-trigger disabled");
        }
        return this;
    }

    public Menu getMenu() {
        return this.menu;
    }

    public boolean isClickOpensMenu() {
        return clickOpensMenu;
    }

    public void setClickOpensMenu(boolean clickOpensMenu) {
        this.clickOpensMenu = clickOpensMenu;
    }

    public void setOnShowMenuCallback(OnShowMenuCallback onShowMenuCallback) {
        this.onShowMenuCallback = onShowMenuCallback;
    }

    public MenuButton withClickOpensMenu() {
        setClickOpensMenu(true);
        return this;
    }

    @Override
    protected void fireClickEvent(ClickEvent event) {
        if (!isEnabled()) {
            return;
        }
        if (event.getX() > getRightContentX() + 2 || isClickOpensMenu()) {
            showMenu();
        }
        else if (event.getX() < getRightContentX()) {
            super.fireClickEvent(event);
        }
    }

    @Override
    protected Widget createRightContentWidget() {
        final Image image = new Image("clear.cache.gif");
        image.setStyleName("menu-trigger");
        return image;
    }

    class MenuAddRemoveHandler implements ItemAddedHandler<MenuItem>, ItemRemovedHandler<MenuItem> {
        @Override
        public void onItemAdded(ItemAddedEvent<MenuItem> event) {
            getRightContentWidget().setStyleName("menu-trigger");
        }

        @Override
        public void onItemRemoved(ItemRemovedEvent<MenuItem> event) {
            if (menu.getItemCount() == 0) {
                getRightContentWidget().setStyleName("menu-trigger disabled");
            }
        }
    }

    void showMenu() {
        if (!isEnabled()) {
            return;
        }
        if (this.onShowMenuCallback == null) {
            _showMenu();
            return;
        }

        this.onShowMenuCallback.onShowMenu(this, this.menu, new Command() {
            @Override
            public void execute() {
                _showMenu();
            }
        });
    }

    private void _showMenu() {
        if (this.menu.getItemCount() == 0) {
            return;
        }
        this.menu.setMinWidth(getElement().getOffsetWidth());
        this.menu.show(this);
        this.menuOpen = true;
        updateStyle();
    }

    @Override
    protected void updateStyle() {
        if (this.menuOpen) {
            updateStyle("clicked");
        }
        else {
            super.updateStyle();
        }
    }

    @Override
    public boolean onFocusKeyClick() {
        if (this.menuOpen) {
            final MenuItem keySelectionItem = this.menu.getKeySelectionItem();
            if (keySelectionItem != null) {
                keySelectionItem.click();
            }
            else {
                this.menu.hide();
            }
            getElement().focus();
            return true;
        }
        else if (this.clickOpensMenu) {
            showMenu();
            this.menu.moveKeySelection(0);
            getElement().focus();
            return true;
        }
        else {
            return super.onFocusKeyClick();
        }
    }

    @Override
    public boolean onFocusKeyEscape() {
        this.menu.hide();
        return true;
    }

    @Override
    public boolean onFocusKeyHome() {
        if (this.menuOpen) {
            this.menu.setKeySelectionIndex(0);
            return true;
        }
        return false;
    }

    @Override
    public boolean onFocusKeyPageUp() {
        if (this.menuOpen) {
            this.menu.moveKeySelection(-this.menu.getPopupPageItemCount());
            return true;
        }
        return false;
    }

    @Override
    public boolean onFocusKeyUp() {
        if (this.menuOpen) {
            this.menu.moveKeySelection(-1);
            return true;
        }
        return false;
    }

    @Override
    public boolean onFocusKeyDown() {
        if (!this.menuOpen) {
            showMenu();
            this.menu.moveKeySelection(0);
        }
        else {
            this.menu.moveKeySelection(1);
        }
        return true;
    }

    @Override
    public boolean onFocusKeyPageDown() {
        if (this.menuOpen) {
            this.menu.moveKeySelection(this.menu.getPopupPageItemCount());
            return true;
        }
        return false;
    }

    @Override
    public boolean onFocusKeyEnd() {
        if (this.menuOpen) {
            this.menu.setKeySelectionIndex(this.menu.getItemCount() - 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean onFocusKey(char c) {
        if (this.menuOpen) {
            this.menu.setKeySelectionPrefix(c);
            return true;
        }
        return false;
    }

    public interface OnShowMenuCallback {
        void onShowMenu(UIObject me, Menu menu, Command finishShowCommand);
    }
}
