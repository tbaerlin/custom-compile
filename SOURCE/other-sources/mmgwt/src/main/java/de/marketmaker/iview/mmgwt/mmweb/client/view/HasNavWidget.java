package de.marketmaker.iview.mmgwt.mmweb.client.view;

import de.marketmaker.iview.mmgwt.mmweb.client.as.NavigationWidget;

/**
 * @author Ulrich Maurer
 *         Date: 11.12.12
 */
public interface HasNavWidget {

    abstract class NavWidgetCallback {
        public abstract void setNavWidget(NavigationWidget widget);
        public abstract void showGlass();
    }

    void requestNavWidget(NavWidgetCallback callback);
    boolean providesContentHeader();
}
