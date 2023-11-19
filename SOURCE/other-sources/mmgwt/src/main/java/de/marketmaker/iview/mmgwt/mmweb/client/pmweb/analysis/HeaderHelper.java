package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.Config;

/**
 * Created on 03.03.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
class HeaderHelper {
    private Config config;
    private SafeHtml safeHtml;

    public boolean isFor(Config config) {
        return this.config != null && this.config.equals(config);
    }

    public void update(Config config, SafeHtml header) {
        this.config = config;
        this.safeHtml = header;
    }

    public SafeHtml getSafeHtml() {
        return this.safeHtml;
    }
}
