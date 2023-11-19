/*
 * Features.java
 *
 * Created on 27.03.13 17:18
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.EnumSet;

import javax.inject.Singleton;

import com.google.gwt.regexp.shared.RegExp;

import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags.Feature.*;

/**
 * @author oflege
 */
@NonNLS
@Singleton
public class FeatureFlags {

    public enum Feature {
        AS_WITH_NETHELP,
        NAME_SUFFIX, // ISTAR-458
        VWD_RELEASE_2014,
        LME_CHANGES_2014,
        VWD_IT_LOCALIZATION, // ISTAR-598
        CONTRIBUTOR_PORTRAIT, // Redmine #10262
        DZ_RELEASE_2015,   // ISTAR-708
        DZ_RELEASE_2016,    // ISTAR-773
        ICE_DESIGN, // as design for mm[web]
        VWD_RELEASE_2015 // ISTAR-738
        ;

        /**
         * @deprecated Use {@linkplain #isEnabled0(Feature)}
         */
        @Deprecated
        public boolean isEnabled() {
            return FeatureFlags.isEnabled(this);
        }
    }

    private static final EnumSet<Feature> DEV_FEATURES = EnumSet.of(
            AS_WITH_NETHELP,
            NAME_SUFFIX,
            VWD_RELEASE_2014,
            LME_CHANGES_2014,
            VWD_IT_LOCALIZATION,
            CONTRIBUTOR_PORTRAIT,
            DZ_RELEASE_2015,
            DZ_RELEASE_2016,
            ICE_DESIGN,
            VWD_RELEASE_2015
    );

    private static final EnumSet<Feature> PROD_FEATURES = EnumSet.of(
            NAME_SUFFIX,
            VWD_RELEASE_2014,
            DZ_RELEASE_2015,
            ICE_DESIGN
    );

    @Deprecated
    private static EnumSet<Feature> getFeatures() {
        return Ginjector.INSTANCE.getSessionData().isDev() ? DEV_FEATURES : PROD_FEATURES;
    }

    // to delay features for web, kwt, apobank, see: ISTAR-537
    private static final String DENIED_FEATURES_KEY = "deniedFeatures";

    /**
     * @deprecated Use {@linkplain #isEnabled0(String)}
     */
    @Deprecated
    public static boolean isEnabled(String s) {
        try {
            String[] orParts = s.split(RegExp.quote("||"));

            for (String part : orParts) {
                System.out.println(part);
                if (isEnabledSimple(part.trim())) {
                    return true;
                }
            }

            return false;
        } catch (IllegalArgumentException e) {
            DebugUtil.logToServer("ERROR invalid feature flag '" + s + "'");
            return false;
        }
    }

    /**
     * @deprecated Use {@linkplain #isEnabled0(Feature)}
     */
    @Deprecated
    private static boolean isEnabled(Feature feature) {
        return getFeatures().contains(feature) && !isDisabledInGuidefs(feature);
    }

    public boolean isEnabled0(Feature feature) {
        return isEnabled(feature);
    }

    public boolean isEnabled0(String s) {
        return isEnabled(s);
    }

    @Deprecated
    private static boolean isEnabledSimple(String s) {
        return s.startsWith("!") && !Feature.valueOf(s.substring(1)).isEnabled()
                || Feature.valueOf(s).isEnabled();
    }

    @Deprecated
    private static boolean isDisabledInGuidefs(Feature f) {
        final JSONWrapper deniedFeatures = Ginjector.INSTANCE.getSessionData().getGuiDef(DENIED_FEATURES_KEY);
        for (int i = 0; i < deniedFeatures.size(); i++) {
            if (f.name().equals(deniedFeatures.get(i).stringValue())) {
                return true;
            }
        }
        return false;
    }

}
