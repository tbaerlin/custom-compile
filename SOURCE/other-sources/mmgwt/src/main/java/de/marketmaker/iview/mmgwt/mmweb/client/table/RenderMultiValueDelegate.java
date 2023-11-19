package de.marketmaker.iview.mmgwt.mmweb.client.table;

import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;

/**
 * Created on 03.02.2010 10:21:52
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public interface RenderMultiValueDelegate<T> {
    public T getFirstValue(Price price);
    public T getSecondValue(Price price);
    public int compareFirst(Price oldPrice, Price newPrice);
    public int compareSecond(Price oldPrice, Price newPrice);
}
