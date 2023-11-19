package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * @author Ulrich Maurer
 *         Date: 02.10.12
 */
public interface HasIcon {
    public void setIcon(AbstractImagePrototype imagePrototype);
    public void setIconStyle(String styleName);
}