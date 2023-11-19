/*
 * ShellMMUtil.java
 *
 * Created on 02.09.2014 15:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.pmxml;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This util is intended to be used by servers and GWT clients.
 * Hence, add only basic things that are available in both worlds!
 *
 * @author mdick
 */
public class ShellMMUtil {
    private static final Set<ShellMMType> DEPOT_OBJECT_TYPES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(
                    ShellMMType.ST_INHABER,
                    ShellMMType.ST_PORTFOLIO,
                    ShellMMType.ST_DEPOT,
                    ShellMMType.ST_KONTO,
                    ShellMMType.ST_PERSON,
                    ShellMMType.ST_INTERESSENT
            )));

    private static final Set<ShellMMType> SECURITY_TYPES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(
                    ShellMMType.ST_AKTIE,
                    ShellMMType.ST_ANLEIHE,
                    ShellMMType.ST_GENUSS,
                    ShellMMType.ST_FOND,
                    ShellMMType.ST_KONJUNKTURDATEN,
                    ShellMMType.ST_BEZUGSRECHT,
                    ShellMMType.ST_DEVISE,
                    ShellMMType.ST_FUTURE,
                    ShellMMType.ST_OPTION,
                    ShellMMType.ST_OS,
                    ShellMMType.ST_INDEX,
                    ShellMMType.ST_CERTIFICATE,

                    ShellMMType.ST_WP
    )));

    public static boolean isSecurity(ShellMMType type) {
        return SECURITY_TYPES.contains(type);
    }

    public static boolean isDepotObject(ShellMMType type) {
        return DEPOT_OBJECT_TYPES.contains(type);
    }

    public static Set<ShellMMType> getSecurityTypes() {
        return SECURITY_TYPES;
    }

    public static Set<ShellMMType> getDepotObjectTypes() {
        return DEPOT_OBJECT_TYPES;
    }
}