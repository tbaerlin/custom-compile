/*
 * EventSelector.java
 *
 * Created on 08.06.2010 17:16:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.companydate;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;

/**
 * @author oflege
 */
class EventSelector {

    private final Language language;

    private final boolean allButPossibleWmItems;

    /**
     * @param allButPossibleWmItems Exclude items that possibly been delivered by WMData
     */
    EventSelector(Language language, boolean allButPossibleWmItems) {
        this.language = language;
        this.allButPossibleWmItems = allButPossibleWmItems;
    }

    protected boolean select(Event e) {
        return e.getEvent().isSupported(this.language)
                && (!this.allButPossibleWmItems || isNotPossibleWmItem(e.getEvent()));
    }

    private boolean isNotPossibleWmItem(LocalizedString event) {
        final String name = event.getDe();
        if (name.contains("Ex") && name.contains("Div")) {
            return false;
        }
        if (name.contains("Haup") && !name.contains("erordentlich")) {
            return false;
        }
        return true;
    }

}
