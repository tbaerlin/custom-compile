/*
 * SecurityIdSnippet.java
 *
 * Created on 07.06.13 14:37
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import de.marketmaker.iview.pmxml.ShellMMType;

/**
 * @author Markus Dick
 */
public interface SecurityIdSnippet {
    /**
     * @param shellMMType @see{de.marketmaker.iview.pmxml.ShellMMType}
     * @param mmSecurityId @see{de.marketmaker.iview.pmxml.ShellMMInfo#getMMSecurityId} The ID custom securities must start with an &quot;&amp;&quot;
     */
    void setSecurityId(ShellMMType shellMMType, String mmSecurityId);
}
