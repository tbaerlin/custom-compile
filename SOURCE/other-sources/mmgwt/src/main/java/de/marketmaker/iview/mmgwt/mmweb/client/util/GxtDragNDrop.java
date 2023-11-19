package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.extjs.gxt.ui.client.event.DNDEvent;

/**
 * Created on 20.05.2010 11:31:13
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public interface GxtDragNDrop<T> {
    void onDrop(T t);
    void onDragEnter(T t, DNDEvent e);
}
