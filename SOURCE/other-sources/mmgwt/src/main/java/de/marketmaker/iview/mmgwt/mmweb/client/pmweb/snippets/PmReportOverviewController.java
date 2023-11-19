/*
 * PmReportOverviewController.java
 *
 * Created on 18.06.13 12:11
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.HasSecurityIdSnippets;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.MetadataAware;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitOverviewController;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.Collection;

/**
 * @author Markus Dick
 */
public class PmReportOverviewController extends PortraitOverviewController implements HasSecurityIdSnippets, MetadataAware {

    public PmReportOverviewController(ContentContainer contentContainer, String def) {
        super(contentContainer, def);
    }

    @Override
    public boolean isMetadataNeeded() {
        return true;
    }

    @Override
    protected void initDelegate() {
        this.delegate = SnippetsFactory.createSingleController(getContentContainer(), this.def);
    }

    @Override
    public boolean isPrintable() {
        return false;
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        HasSecurityIdSnippets.Tool.doOnPortraitPlaceChange(this, event);
        super.onPlaceChange(event);
    }

    public void setSecurityId(ShellMMType type, String securityId) {
        HasSecurityIdSnippets.Tool.setSecurityId(this, type, securityId);
    }

    @Override
    public Collection<SecurityIdSnippet> getSecurityIdSnippets() {
        return HasSecurityIdSnippets.Tool.getSecurityIdSnippets(getSnippets());
    }
}