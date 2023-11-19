/*
 * AuthItemFactory.java
 *
 * Created on 26.07.12 08:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.widgets;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

/**
 * @author Markus Dick
 */
public interface AuthItemFactory extends AutoBeanFactory {
    public AutoBean<AuthItem> authItem();
    public AutoBean<AuthItemStore> authItemStore();
}
