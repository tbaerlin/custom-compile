package de.marketmaker.iview.mmgwt.mmweb.client.as.navigation;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.ImageSpec;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: umaurer
 * Date: 29.04.13
 * Time: 09:11
 */
public interface Item {
    String getName();
    boolean isLeaf();
    List<? extends Item> getChildren();
    boolean isAlwaysOpen();
    boolean isOpenByDefault();
    boolean isOpenWithParent();
    boolean isOpenWithSelection();
    boolean isClosingSiblings();
    boolean isHasDelegate();
    boolean isSelectFirstChildOnOpen();
    boolean isVisible();
    boolean hasSelectionHandler();
    ImageSpec getIcon();
    SafeHtml getIconTooltip();
    Widget getEndIcon();
    String getEndIconCellClass();
    ImageSpec getLeftIcon();
    SafeHtml getLeftIconTooltip();
}
