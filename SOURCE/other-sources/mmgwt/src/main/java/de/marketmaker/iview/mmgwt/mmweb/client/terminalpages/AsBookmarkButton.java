package de.marketmaker.iview.mmgwt.mmweb.client.terminalpages;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuButton;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NaturalComparator;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Author: umaurer
 * Created: 25.07.14
 */
public class AsBookmarkButton extends MenuButton {
    private final TerminalPages tp;
    private final String userPropertyKey;
    private final Menu menu;
    private final Set<String> pageKeys = new TreeSet<>(NaturalComparator.createDefault());
    private ClickHandler clickHandlerAddBookmark = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            pageKeys.add(tp.getLastPage());
            updatePageBookmarks();
        }
    };

    public AsBookmarkButton(TerminalPages tp) {
        this.tp = tp;
        this.userPropertyKey = "asPages(" + tp.getType() + ")"; // $NON-NLS$
        this.menu = new Menu();
        final String sPageKeys = SessionData.INSTANCE.getUserProperty(this.userPropertyKey);
        if (StringUtil.hasText(sPageKeys)) {
            this.pageKeys.addAll(Arrays.asList(sPageKeys.split(",")));
        }
        addMenuItems();
        setIcon("mm-bookmark"); // $NON-NLS$
        withMenu(this.menu);
        withClickOpensMenu();
        addClickHandler(this.clickHandlerAddBookmark);
        setText(I18n.I.bookmarks());
        Tooltip.addQtip(this, I18n.I.bookmarks());
    }

    private void addMenuItems() {
        addAddBookmarkHandler();
        for (final String pageKey : this.pageKeys) {
            this.menu.add(new MenuItem(pageKey, event -> PlaceUtil.goTo(tp.getType().getControllerName() + "/" + pageKey)));
        }
    }

    private void addAddBookmarkHandler() {
        this.menu.add(new MenuItem(I18n.I.pageBookmarkAdd(), this.clickHandlerAddBookmark));
        this.menu.add(new MenuItem(I18n.I.pageBookmarkRemove(), event -> {
            pageKeys.remove(tp.getLastPage());
            updatePageBookmarks();
        }));
        this.menu.addSeparator();
    }

    private void updatePageBookmarks() {
        final String propertyValue = this.pageKeys.isEmpty() ? null : StringUtil.join(",", this.pageKeys);
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(this.userPropertyKey, propertyValue);
        this.menu.removeAll();
        addMenuItems();
    }
}
