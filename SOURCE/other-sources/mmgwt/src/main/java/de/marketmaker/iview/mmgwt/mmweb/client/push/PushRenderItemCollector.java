/*
 * PushRenderItemCollector.java
 *
 * Created on 02.03.2010 09:04:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.push;

import java.util.ArrayList;

import com.google.gwt.dom.client.Element;

/**
 * @author oflege
 */
public interface PushRenderItemCollector {
    ArrayList<PushRenderItem> collect(ArrayList<Element> pushedTDs);
}
