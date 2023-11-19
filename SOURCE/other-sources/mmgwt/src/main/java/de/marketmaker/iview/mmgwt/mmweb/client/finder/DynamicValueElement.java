package de.marketmaker.iview.mmgwt.mmweb.client.finder;

/**
 * Created on 25.01.12 09:48
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * This interface marks FinderFormElements that can be deactivated by
 * an instance of {@link de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinder.DynamicSearchHandler}
 *
 *
 * @author Michael Lösch
 */

public interface DynamicValueElement {
    public boolean getValue();

    public void setValue(boolean checked);
}
