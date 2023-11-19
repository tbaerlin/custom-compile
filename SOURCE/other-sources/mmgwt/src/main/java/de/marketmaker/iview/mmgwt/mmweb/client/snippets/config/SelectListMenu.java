package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;

/**
 * Author: umaurer
 * Created: 02.03.15
 */
public class SelectListMenu {
    public static void configure(final AbstractSnippet snippet, Widget triggerWidget, final Command selectionCommand) {
        final Menu menu = new Menu();
        for (final QuoteWithInstrument qwi : SessionData.INSTANCE.getList("list_snippet_config")) { // $NON-NLS$
            final MenuItem menuItem = new MenuItem(qwi.getName());
            menuItem.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    if (!menu.isNoSelectionItem(menuItem)) {
                        final SnippetConfiguration config = snippet.getConfiguration();
                        config.put("listid", qwi.getQuoteData().getQid()); // $NON-NLS$
                        config.put("titleSuffix", qwi.getName()); // $NON-NLS$
                        config.put("titleSuffixSelectList", qwi.getName()); // $NON-NLS$
                        selectionCommand.execute();
                    }
                }
            });
            menu.add(menuItem);
        }
        menu.show(triggerWidget);
    }
}
