package de.marketmaker.iview.mmgwt.mmweb.client.finder;

/**
 * Created on 18.10.11 10:46
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface CloneableFinderFormElement<E extends FinderFormElement> extends FinderFormElement {
    E cloneElement(String newId);
    void fireCloneEvent();
    void fireDeletedEvent();
    boolean isClone();
}