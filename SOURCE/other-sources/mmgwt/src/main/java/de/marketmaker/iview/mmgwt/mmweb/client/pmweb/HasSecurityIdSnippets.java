/*
 * HasSecurityIdSnippets.java
 *
 * Created on 10.09.2014 09:28
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.SecurityIdSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @see de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.SecurityIdSnippet
 * @author mdick
 */
public interface HasSecurityIdSnippets {
    /**
     * @param shellMMType @see{de.marketmaker.iview.pmxml.ShellMMType}
     * @param mmSecurityId @see{de.marketmaker.iview.pmxml.ShellMMInfo#getMMSecurityId} The ID. Custom securities must start with an &quot;&amp;&quot;
     */
    void setSecurityId(ShellMMType shellMMType, String mmSecurityId);

    Collection<SecurityIdSnippet> getSecurityIdSnippets();

    public final class Tool {
        private Tool() { }

        /**
         *  A default impl. for method setSecurityId.
         *  TODO: migrate to Java 8 if available (as default method impl. for setSecurityId)
         */
        public static void setSecurityId(HasSecurityIdSnippets me, ShellMMType shellMMType, String securityId) {
            for (SecurityIdSnippet securityIdSnippet : me.getSecurityIdSnippets()) {
                securityIdSnippet.setSecurityId(shellMMType, securityId);
            }
        }

        public static Collection<SecurityIdSnippet> getSecurityIdSnippets(Collection<Snippet> snippets) {
            ArrayList<SecurityIdSnippet> securityIdSnippets = null;

            for (Snippet snippet : snippets) {
                if(snippet instanceof SecurityIdSnippet) {
                    if(securityIdSnippets == null) {
                        securityIdSnippets = new ArrayList<>(snippets.size());
                    }
                    securityIdSnippets.add((SecurityIdSnippet)snippet);
                }
            }
            if(securityIdSnippets == null) {
                return Collections.emptyList();
            }
            return securityIdSnippets;
        }

        public static void doOnPortraitPlaceChange(HasSecurityIdSnippets hasSecurityIdSnippets, PlaceChangeEvent event) {
            final HistoryToken historyToken = event.getHistoryToken();
            final String iidOrSid = historyToken.get(1, null);
            final String shellMMTypeName = historyToken.get(PmWebModule.TOKEN_NAME_CUSTOM_SECURITY_TYPE);

            if(StringUtil.hasText(iidOrSid) && iidOrSid.endsWith(".sid") && StringUtil.hasText(shellMMTypeName)) {  // $NON-NLS$
                final String sid = PmSecurityUtil.stripOfSecurityIdSuffix(iidOrSid);

                ShellMMType shellMMType;
                try {
                    shellMMType = ShellMMType.valueOf(shellMMTypeName);
                }
                catch(IllegalArgumentException iae) {
                    Firebug.warn("Unknown shellMMType '" + shellMMTypeName + "'. Using default ST_WP", iae);
                    shellMMType = ShellMMType.ST_WP;
                }

                hasSecurityIdSnippets.setSecurityId(shellMMType, sid);
            }
        }
    }
}
