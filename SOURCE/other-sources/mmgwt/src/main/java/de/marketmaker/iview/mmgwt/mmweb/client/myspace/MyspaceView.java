/*
 * MyspaceView.java
 *
 * Created on 11.04.2008 14:37:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.myspace;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.input.ValidationFeature;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuButton;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.DashboardSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MDISnippetsController;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MDISnippetsView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a  number user configurable pages, each page appears in its own tab; pages can be removed,
 * renamed, and added
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MyspaceView extends ContentPanel implements ContentView {
    private static final int MAX_NUM_SNIPPETS = 10;
    public static final int NAME_MAX_CHAR_COUNT = 20;

    public static MyspaceView INSTANCE;

    private MDISnippetsView activeSpace;

    private String currentStartPageConfigId = null;

    private Menu menuSpaces;

    private final SelectButton buttonSpaces;

    private final MenuButton btnSnippets;

    private final Button btnCreateSpace;

    private final Button btnDeleteSpace;

    private final Button btnAddSnippet;

    private final CardLayout cardLayout = new CardLayout();

    private final MyspaceController controller;

    private ValidationFeature.Validator spaceNameValidator = new ValidationFeature.Validator() {
        @Override
        public boolean isValid(String value, List<String> messages) {
            if (value == null || value.isEmpty() || value.length() > 20) {
                messages.add(I18n.I.from1ToNChars(NAME_MAX_CHAR_COUNT));
                return false;
            }
            return true;
        }
    };

    MyspaceView(MyspaceController controller) {
        INSTANCE = this;
        this.controller = controller;
        setHeaderVisible(false);

        this.cardLayout.setDeferredRender(true);
        setLayout(this.cardLayout);

        final FloatingToolbar toolbar = new FloatingToolbar();
        toolbar.addLabel(I18n.I.spaces() + ":"); // $NON-NLS$
        setTopComponent(toolbar);

        this.menuSpaces = new Menu();
        for (int i = 0; i < controller.getNumSpaces(); i++) {
            final MDISnippetsView spaceView = controller.getSpaceView(i);
            final MenuItem item = createSpaceItem(spaceView);
            this.menuSpaces.add(item);
            add(spaceView);
        }
        this.buttonSpaces = new SelectButton().withMenu(this.menuSpaces);
        this.buttonSpaces.setClickOpensMenu(true);
        this.buttonSpaces.addSelectionHandler(new SelectionHandler<MenuItem>() {
            @Override
            public void onSelection(SelectionEvent<MenuItem> e) {
                setActiveSpace(e.getSelectedItem());
            }
        });
        toolbar.add(this.buttonSpaces);
        toolbar.addEmpty();

        if (!SessionData.isAsDesign()) {
            toolbar.add(Button.icon("space-to-home-icon") // $NON-NLS$
                    .tooltip(I18n.I.currentSpaceAsStartPage())
                    .clickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                            asStartPage();
                        }
                    })
                    .build()
            );
        }

        this.btnCreateSpace = Button.icon("space-new-icon") // $NON-NLS$
                .tooltip(I18n.I.createNewSector())
                .clickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        createSpace();
                    }
                })
                .build();
        toolbar.add(this.btnCreateSpace);

        final Button btnRenameSpace = Button.icon("space-edit-icon") // $NON-NLS$
                .tooltip(I18n.I.renameCurrentSpace())
                .clickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        renameSpace();
                    }
                })
                .build();
        toolbar.add(btnRenameSpace);

        this.btnDeleteSpace = Button.icon("space-delete-icon") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        removeSpace();
                    }
                })
                .tooltip(I18n.I.deleteCurrentSpace())
                .build();
        toolbar.add(this.btnDeleteSpace);

        toolbar.addSeparator();
        toolbar.addEmpty();

        toolbar.addLabel(I18n.I.contents() + ":"); // $NON-NLS$
        final Menu snippetMenu = createSnippetMenu();

        if (SessionData.isAsDesign()) {
            this.btnSnippets = new MenuButton()
                    .withIcon("space-add-snippet-icon") // $NON-NLS$
                    .withMenu(snippetMenu)
                    .withClickOpensMenu();
            toolbar.add(this.btnSnippets);
            this.btnAddSnippet = null;
        }
        else {
            this.btnSnippets = new SelectButton()
                    .withMenu(snippetMenu)
                    .withClickOpensMenu();
            toolbar.add(this.btnSnippets);

            this.btnAddSnippet = Button.icon("space-add-snippet-icon") // $NON-NLS$
                    .tooltip(I18n.I.addContent())
                    .clickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                            addSnippet();
                        }
                    })
                    .build();
            toolbar.add(this.btnAddSnippet);
        }
        updateButtonStates();
    }

    public Widget getWidget() {
        return this;
    }

    public void onBeforeHide() {
        captureSnippetCoordinates();
    }

    private MenuItem createSpaceItem(final MDISnippetsView spaceView) {
        return new MenuItem(spaceView.getName()).withData("spaceView", spaceView); // $NON-NLS$
    }

    private void setActiveSpace(MenuItem menuItem) {
        if (menuItem != null) {
            setActiveSpace((MDISnippetsView) menuItem.getData("spaceView")); // $NON-NLS$
        }
    }

    protected void updateHomeIcon() {
        final String startPageConfigId = controller.getStartPageConfigId();
        if ((this.currentStartPageConfigId == null && startPageConfigId != null)
                || (this.currentStartPageConfigId != null && !this.currentStartPageConfigId.equals(startPageConfigId))) {
            final ArrayList<MDISnippetsController> controllers = controller.getSubControllers();
            for (int i = 0; i < controllers.size(); i++) {
                final MDISnippetsController controller = controllers.get(i);
                if (controller.getConfig().getId().equals(startPageConfigId)) {
                    this.menuSpaces.getItems().get(i).setIcon(IconImage.get("space-home-icon")); // $NON-NLS$
                } else {
                    this.menuSpaces.getItems().get(i).setIcon(null);
                }
            }
        }

        if (this.activeSpace.getController().getConfig().getId().equals(startPageConfigId)) {
            this.buttonSpaces.setIcon(IconImage.get("space-home-icon")); // $NON-NLS$
        } else {
            this.buttonSpaces.removeIcon();
        }
    }

    public void updateButtonStates() {
        final int numSpaces = controller.getNumSpaces();
        this.btnCreateSpace.setEnabled(numSpaces < SessionData.INSTANCE.getMaxNumMyspaces());
        this.btnDeleteSpace.setEnabled(numSpaces > 1);
        final boolean allowAddSnippets = this.activeSpace != null
                && this.activeSpace.getItems().size() < MAX_NUM_SNIPPETS;
        if (this.btnAddSnippet == null) {
            this.btnSnippets.setEnabled(allowAddSnippets);
        }
        else {
            this.btnAddSnippet.setEnabled(allowAddSnippets);
        }
    }

    public MDISnippetsView getActiveSpace() {
        return this.activeSpace;
    }

    public void setActiveSpace(int spaceIndex) {
        setActiveSpace(controller.getSpaceView(spaceIndex));
    }

    private void setActiveSpace(MDISnippetsView view) {
        controller.setCurrent(view.getController());
        this.activeSpace = view;
        this.cardLayout.setActiveItem(view);
        this.buttonSpaces.setText(view.getName());
        controller.refresh();
        updateHomeIcon();
        updateButtonStates();
    }


    public int getActiveSpaceId() {
        return this.controller.getViewIndex(getActiveSpace());
    }

    void captureSnippetCoordinates() {
        if (!isAttached() || !isVisible() || controller.getNumSpaces() == 0) {
            return;
        }
        getActiveSpace().captureSnippetCoordinates();
    }

    private void asStartPage() {
        controller.asStartPage(getActiveSpaceId());
        updateHomeIcon();
    }

    private Menu createSnippetMenu() {
        final Menu menu = new Menu();
        for (final SnippetMenuConfig.Item item : SnippetMenuConfig.INSTANCE.getItems()) {
            if(isPmWebSnippet(item)) {
                continue;
            }

            final MenuItem menuItem = new MenuItem(item.getClazz().getTitle()).withData("snippetMenuItem", item); // $NON-NLS$
            if (SessionData.isAsDesign()) {
                menuItem.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        addSnippet(item);
                    }
                });
            }
            menu.add(menuItem);
        }
        return menu;
    }

    private boolean isPmWebSnippet(SnippetMenuConfig.Item item) {
        return item.getClazz().getClass() == DashboardSnippet.Class.class;
    }

    private void addSnippet() {
        final MenuItem selectedItem = this.btnSnippets.getMenu().getSelectedItem();
        if (selectedItem != null) {
            addSnippet((SnippetMenuConfig.Item) selectedItem.getData("snippetMenuItem")); // $NON-NLS$
        }
    }

    private void addSnippet(SnippetMenuConfig.Item snippetMenuItem) {
        this.controller.addSnippet(getActiveSpaceId(), snippetMenuItem.getClazz());
        updateButtonStates();
    }

    private void createSpace() {
        Dialog.prompt(I18n.I.createSpace()
                , I18n.I.name(), ""
                , I18n.I.from1ToNChars(NAME_MAX_CHAR_COUNT)
                , this.spaceNameValidator
                , new Dialog.PromptCallback() {
            @Override
            protected boolean isValid(String value) {
                return StringUtil.hasText(value) && spaceNameValidator.isValid(value, new ArrayList<String>());
            }

            @Override
            public void execute(String value) {
                createSpace(value);
            }
        });
    }

    public MDISnippetsController createSpace(String name) {
        final MDISnippetsController controller = this.controller.createSpace(name);
        final MDISnippetsView view = controller.getView();
        final MenuItem item = createSpaceItem(view);
        this.menuSpaces.add(item);
        add(view);
        setActiveSpace(view);
        return controller;
    }

    private void removeSpace() {
        Dialog.confirm(I18n.I.confirmRemoveSpace(getActiveSpace().getName()), new Command() {
            public void execute() {
                removeSpace(getActiveSpace());
            }
        });
    }

    private void removeSpace(MDISnippetsView spaceView) {
        final int spaceId = getActiveSpaceId();
        final MenuItem item = this.buttonSpaces.getSelectedItem();
        this.menuSpaces.remove(item);
        this.controller.removeSpace(spaceId);
        remove(spaceView);
        setActiveSpace(0);
    }

    private void renameSpace() {
        Dialog.prompt(I18n.I.renameSpace()
                , I18n.I.name(), getActiveSpace().getName()
                , I18n.I.from1ToNChars(NAME_MAX_CHAR_COUNT)
                , this.spaceNameValidator
                , new Dialog.PromptCallback() {
            @Override
            protected boolean isValid(String value) {
                return StringUtil.hasText(value) && spaceNameValidator.isValid(value, new ArrayList<String>());
            }

            @Override
            public void execute(String value) {
                renameSpace(value);
            }
        });
    }

    private void renameSpace(String title) {
        final int spaceViewId = getActiveSpaceId();
        this.controller.renameSpace(spaceViewId, title);
        final MenuItem oldItem = this.buttonSpaces.getSelectedItem();
        final MenuItem newItem = createSpaceItem(this.activeSpace);
        this.menuSpaces.remove(oldItem);
        this.menuSpaces.insert(newItem, spaceViewId);
        updateHomeIcon();
        this.buttonSpaces.setSelectedItem(newItem);
    }
}
