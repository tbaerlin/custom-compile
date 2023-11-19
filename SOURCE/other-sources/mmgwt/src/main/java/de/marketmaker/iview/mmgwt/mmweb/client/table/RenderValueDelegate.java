package de.marketmaker.iview.mmgwt.mmweb.client.table;

import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;

/**
 * Created on 02.02.2010 09:35:35
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public interface RenderValueDelegate<T> {
    public T getValue(Price price);
    public int compare(Price oldPrice, Price newPrice);
}
