package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Created with IntelliJ IDEA.
 * User: umaurer
 * Date: 06.05.13
 * Time: 16:18
 */
public interface NavigationWidget extends IsWidget {
    void onNavWidgetSelected();
    void changeSelection(String[] ids);
    boolean isResponsibleFor(String[] ids);
}
