/*
 * DesktopSnippetView.java
 *
 * Created on 14.08.2008 15:22:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.desktop.Desktop;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.DesktopIcon;

import java.util.List;

/**
 * @author Ulrich Maurer
 */
public class DesktopSnippetView<S extends Snippet<S>> extends SnippetView<S> {
    private final Desktop desktop;

    public DesktopSnippetView(S snippet) {
        super(snippet);
        setTitle(getConfiguration().getString("title")); // $NON-NLS$
        this.desktop = new Desktop(Desktop.Mode.FLOW);
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.desktop);
    }

    @SuppressWarnings("unchecked")
    public void update(List<DesktopIcon> listDesktopIcons) {
        this.desktop.clear();
        for (DesktopIcon desktopIcon : listDesktopIcons) {
            this.desktop.add(desktopIcon);
        }
    }
}
