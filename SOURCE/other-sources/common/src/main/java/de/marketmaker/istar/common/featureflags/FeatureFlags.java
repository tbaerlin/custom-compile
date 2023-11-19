/*
 * FeatureFlagUtil.java
 *
 * Created on 17.07.12 15:59
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.featureflags;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import de.marketmaker.istar.common.Constants;

/**
 * Tool for feature flags to enable migration processes with mixed test and production states.
 * Use -DfeatureFlags=__type__ to lead specific feature sets. Currently features-dev.txt and
 * features-prod.txt are supported while prod is loaded when no -D parameter is specified.
 * @author tkiesgen
 */
public class FeatureFlags {

    public enum Flag {
        PROD, // FIXED FLAG, DO NOT REMOVE
        PROTOBUF_TICK_OPTIMIZATION,
        TEST_MARKET_STRATEGIES_IF_AVAILABLE, // allows for testing market strategies by having a second market strategy with suffix _TEST, which is loaded in dev environment
        TICK_ITERATOR_IMPROVEMENT, // new tick iterator which does not check for negative time offsets
        EOD_HISTORY,  // mike's backend
        PIB_DEV_ONLY,
        NEW_WP_NAMES, // ISTAR-455
        LME_PRICEDATA_DEV, LME_PRICEDATA_PROD, // ISTAR-551 - DEV is enabled in dev, PROD is enabled in prod -> use in zone definition with 'context.LME_PRICEDATA_DEV=true' and/or 'context.LME_PRICEDATA_PROD=true' activitate per zone AND environment
        VWD_RELEASE_2014,  // ISTAR-537, deactivated in zones web.prop,kwt.prop,apobank.prop
        LME_CHANGES_2014, // ISTAR-550
        VWD_RIMPAR_DECIMAL_TAG, // flag to support tag name change in MSC_Ticks
        VWD_RIMPAR_MSC_TICKDATA, // flag to support response type change in MSC_Ticks
        REQUEST_FFMST_WITH_FFM, // ISTAR-685
        DZ_RELEASE_2015, // ISTAR-708
        TICK_VOLUME_PERMISSION_FIX, // ISTAR-757 (R-78784)
        DZ_RELEASE_2015_2,
        HISTORIC_DATA_PERMISSION_CHECK // CORE-13414
        ;

        public boolean isEnabled() {
            return FeatureFlags.isEnabled(this);
        }
    }

    private static final Set<Flag> DEFAULT_FLAGS = readFlags();

    private static volatile Set<Flag> s_flags = DEFAULT_FLAGS;

    private static void setFlags(Set<Flag> flags) {
        s_flags = flags;
    }

    /**
     * Overrides the current flags with those from the resource file, can be used to revert the
     * effect of calling {@link #setFixedFlags(de.marketmaker.istar.common.featureflags.FeatureFlags.Flag...)}.
     */
    public static void setSystemFlags() {
        setFlags(DEFAULT_FLAGS);
        LoggerFactory.getLogger(FeatureFlags.class).info("<setSystemFlags> " + DEFAULT_FLAGS);
    }

    /**
     * Overrides the current flags, main use is for testing different configurations
     * @param fs to be used
     */
    public static void setFixedFlags(Flag... fs) {
        final Set<Flag> flags = asSet(fs);
        setFlags(flags);
        LoggerFactory.getLogger(FeatureFlags.class).info("<setFixedFlags> " + flags);
    }

    private static Set<Flag> asSet(Flag... flags) {
        if (flags == null || flags.length == 0) {
            return EnumSet.noneOf(Flag.class);
        }
        return EnumSet.copyOf(Arrays.asList(flags));
    }

    private static Set<Flag> readFlags() {
        final EnumSet<Flag> result = EnumSet.noneOf(Flag.class);

        final String flags = Constants.getFeatureFlags();
        if ("prod".equals(flags)) {
            result.add(Flag.PROD);
        }

        final String filename = "features-" + flags + ".txt";

        try (Scanner scanner = new Scanner(new ClassPathResource(filename, FeatureFlags.class).getInputStream())) {
            while (scanner.hasNextLine()) {
                result.add(Flag.valueOf(scanner.nextLine().trim()));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isEnabled(Flag flag) {
        return s_flags.contains(flag);
    }

    public static boolean isDisabled(Flag flag) {
        return !isEnabled(flag);
    }
}
