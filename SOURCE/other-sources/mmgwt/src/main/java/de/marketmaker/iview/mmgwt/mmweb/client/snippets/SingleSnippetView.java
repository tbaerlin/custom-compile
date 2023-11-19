/*
 * SingleSnippetView.java
 *
 * Created on 18.06.2013 16:30
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;

import java.util.List;

/**
 * Remark: NeedsScrollLayout does not work for the intended Snippets, e.g. PmReportSnippet
 * @author Markus Dick
 */
public class SingleSnippetView extends ContentPanel {
    public SingleSnippetView(final List<Snippet> snippets) {
        setLayout(new FitLayout());
        setHeaderVisible(false);

        for (Snippet s : snippets) {
            final SnippetConfiguration config = s.getConfiguration();
            if (config.getBoolean("isObjectInfo", false)) { // $NON-NLS$
                continue;
            }

            final SnippetView<?> snippetView = s.getView();
            if (snippetView == null) {
                DebugUtil.logToServer("null view for " + config); // $NON-NLS$
                add(new HTML(I18n.I.error()));
            }
            else {
                snippetView.setContainer(this, true);
            }
        }
    }
}
