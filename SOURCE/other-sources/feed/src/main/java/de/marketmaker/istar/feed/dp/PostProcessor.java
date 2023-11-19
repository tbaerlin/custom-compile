/*
 * PostProcessor.java
 *
 * Created on 07.06.13 11:48
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp;

/**
 * @author mwilke
 */

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.SnapRecordDefault;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.instrument.EntitlementQuote;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.feed.vwd.EntitlementProviderVwd;

public class PostProcessor implements InitializingBean {

    String entitlementRules = "D:/produktion/var/data/web/EntitlementRules.XFeed.txt";

    String entitlementFieldGroups = "D:/produktion/var/data/web/EntitlementFieldGroups.txt";

    String inFilename;

    String outFilename;

    ProfileProvider profileProvider;

    String profileName;

    Matcher matcher;

    public static final Matcher EINS_ODER_DREI_DTB = Pattern.compile("[1,3].(.*).DTB").matcher("");
    public static final Matcher EINS_DTB = Pattern.compile("1.(.*).DTB").matcher("");

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public void setProfileProvider(ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    public void setEntitlementRules(String entitlementRules) {
        this.entitlementRules = entitlementRules;
    }

    public void setEntitlementFieldGroups(String entitlementFieldGroups) {
        this.entitlementFieldGroups = entitlementFieldGroups;
    }

    public void setInFilename(String inFilename) {
        this.inFilename = inFilename;
    }

    public void setOutFilename(String outFilename) {
        this.outFilename = outFilename;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        PrintWriter pw = new PrintWriter(outFilename, SnapRecordDefault.DEFAULT_CHARSET.name());
        Profile profile = null;

        if (StringUtils.hasText(this.profileName)) {
            // 34:120148
            String[] p = this.profileName.split(":");
            ProfileRequest pr = ProfileRequest.byVwdId(p[1], p[0]);
            final ProfileResponse profileResponse = this.profileProvider.getProfile(pr);
            profile = profileResponse.getProfile();
        }

        final EntitlementProviderVwd ep = new EntitlementProviderVwd();
        ep.setEntitlementRules(new File(entitlementRules));
        ep.setEntitlementFieldGroups(new File(entitlementFieldGroups));
        ep.afterPropertiesSet();

        final Scanner scanner = new Scanner(new File(inFilename), SnapRecordDefault.DEFAULT_CHARSET.name());
        int lineNumber = 0;

        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            lineNumber++;
            if (!StringUtils.hasText(line)) {
                continue;
            }
            if (lineNumber == 1) {
                // print headline
                pw.println(line);
                continue;
            }
            final String vkey = line.substring(0, line.indexOf(";"));

            if ((this.matcher != null) && (this.matcher.reset(vkey).matches())) {
                continue;
            }

            if (StringUtils.hasText(this.profileName)) {
                final String[] entitlements = ep.getEntitlement(vkey).getEntitlements(KeysystemEnum.VWDFEED);
                final PriceQuality pq = profile.getPriceQuality(EntitlementQuote.create(null, null, entitlements));
                if (pq == PriceQuality.NONE) {
                    continue;
                }
            }

            pw.println(line.substring(2));

        }
        scanner.close();
        pw.close();

    }
}

