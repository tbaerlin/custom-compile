/*
 * FinderControllerRegistry.java
 *
 * Created on 18.07.2008 16:12:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import de.marketmaker.itools.gwtutil.client.util.Firebug;

import java.util.HashMap;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FinderControllerRegistry {
    private static final HashMap<String, FinderController> INSTANCES = new HashMap<String, FinderController>();

    static void put(String key, FinderController controller) {
        if (INSTANCES.containsKey(key)) {
            Firebug.warn("FinderControllerRegistry:  the key '" + key + "' is already registered, ignoring the second registration!");
            return;
        }
        INSTANCES.put(key, controller);
    }

    public static FinderController get(String key) {
        return INSTANCES.get(key);
    }

}
