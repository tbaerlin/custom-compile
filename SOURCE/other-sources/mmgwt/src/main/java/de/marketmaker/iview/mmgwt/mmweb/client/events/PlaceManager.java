/*
 * PlaceManager.java
 *
 * Created on 04.12.2009 12:15:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * @author oflege
 */
public interface PlaceManager extends ValueChangeHandler<String>, PlaceChangeHandler {
    /**
     * Fires PlaceChangeEvent with the current history token, or, if that is empty, with
     * defaultPlace
     * @param defaultPlace to be used if current place is not defined
     */
    void fireCurrentPlace(String defaultPlace);

    /**
     * Gets the history token for the current place
     * @return current place's token
     */
    String getCurrentPlace();

    /**
     * Some place changes are not added to the browser's history immediately, as the resulting
     * page may not be displayed itself but rather forward to another page directly (see
     * {@link de.marketmaker.iview.mmgwt.mmweb.client.PriceSearchController}. If, however, that
     * page should be added to the history later, the controller can invoke this method.
     */
    void addPendingHistoryToken();
}
