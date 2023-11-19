/*
 * SecurityPortraitOverviewController.java
 *
 * Created on 07.06.13 16:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import de.marketmaker.iview.mmgwt.mmweb.client.DelegatingPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.SecurityIdSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.Collection;

/**
 * @author Markus Dick
 */
public class SecurityPortraitOverviewController extends DelegatingPageController implements HasSecurityIdSnippets {
    private final String def;

    public SecurityPortraitOverviewController(ContentContainer contentContainer, String def) {
        super(contentContainer);
        this.def = def;
    }

    protected void initDelegate() {
        this.delegate = SnippetsFactory.createFlexController(getContentContainer(), def);
    }

    public void setSecurityId(ShellMMType shellMMType, String securityId) {
        HasSecurityIdSnippets.Tool.setSecurityId(this, shellMMType, securityId);
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        HasSecurityIdSnippets.Tool.doOnPortraitPlaceChange(this, event);
        super.onPlaceChange(event);
    }

    @Override
    public Collection<SecurityIdSnippet> getSecurityIdSnippets() {
        return HasSecurityIdSnippets.Tool.getSecurityIdSnippets(getSnippets());
    }
}
