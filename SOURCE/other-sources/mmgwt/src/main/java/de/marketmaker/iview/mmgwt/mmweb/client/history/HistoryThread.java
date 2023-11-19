package de.marketmaker.iview.mmgwt.mmweb.client.history;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 13.11.12 08:14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

class HistoryThread {
    private static int nextThreadId = 0;

    private final List<HistoryItem> items;
    private final Map<Integer, HistoryItem> hidsAndItems;
    private final Map<String, Map<String, String>> stateProperties;
    private HistoryItem activeItem;
    private final int threadId;
    private SafeHtml title;

    HistoryThread() {
        this.threadId = nextThreadId++;
        this.items = new ArrayList<>();
        this.hidsAndItems = new HashMap<>();
        this.stateProperties = new HashMap<>();
    }

    void add(HistoryItem item) {
        add(item, true);
    }

    void add(HistoryItem item, boolean silent) {
        // first, check if the activeItem is the last in the list
        // if not, remove all items after activeItem
        removeSuccessors(this.activeItem);
        this.items.add(item);
        this.hidsAndItems.put(item.getHid(), item);
        this.activeItem = item;
        if (!silent) {
            firePlaceChange(this.getActiveItem());
        }
    }

    private void removeSuccessors(HistoryItem item) {
        final int idx = this.items.indexOf(item);
        if (idx < this.items.size() - 1) {
            final List<HistoryItem> toRemove = this.items.subList(idx + 1, this.items.size());
            this.items.removeAll(toRemove);
        }
        rebuildMap();
    }

    private void rebuildMap() {
        this.hidsAndItems.clear();
        for (HistoryItem item : this.items) {
            this.hidsAndItems.put(item.getHid(), item);
        }
    }

    int getId() {
        return this.threadId;
    }

    HistoryItem getActiveItem() {
        return this.activeItem;
    }

    HistoryItem getNearestUpstreamNonNullOrExplicitNullContext() {
        int i = this.items.indexOf(this.activeItem);
        while(i > 0) {
            final HistoryItem item = this.items.get(i);
            final PlaceChangeEvent placeChangeEvent = item.getPlaceChangeEvent();
            if(placeChangeEvent.getHistoryContext() != null ||
                    placeChangeEvent.isExplicitHistoryNullContext()) {
                return item;
            }
            i--;
        }
        return null;
    }

    HistoryItem backToBreadCrumb() {
        HistoryItem activeItem;
        HistoryItem backItem = back(true);
        do  {
            if (backItem.isBreadCrumb()) {
                firePlaceChange(backItem);
                return backItem;
            }
            activeItem = backItem;
            backItem = back(true);
        } while (backItem != activeItem);
        return back();
    }


    HistoryItem back() {
        return back(false);
    }

    HistoryItem back(boolean silent) {
        final int currentIdx = this.items.indexOf(this.activeItem);
        if (currentIdx > 0) {
            this.activeItem = this.items.get(currentIdx - 1);
            if (!silent)  {
                firePlaceChange(this.getActiveItem());
            }
        }
        return this.activeItem;
    }

    HistoryItem next() {
        final int currentIdx = this.items.indexOf(this.activeItem);
        if (currentIdx < this.items.size() - 1) {
            this.activeItem = this.items.get(currentIdx + 1);
            firePlaceChange(this.getActiveItem());
        }
        return this.activeItem;
    }

    void firePlaceChange(final HistoryItem item) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                EventBusRegistry.get().fireEvent(item.getPlaceChangeEvent());
            }
        });
    }

    void setActiveItem(Integer historyItemId) {
        if (!this.hidsAndItems.containsKey(historyItemId)) {
            throw new IllegalArgumentException("Current HistoryThread " + this.threadId // $NON-NLS$
                    + " doesn't contain HistoryItem with Id " + historyItemId); // $NON-NLS$
        }
        this.activeItem = this.hidsAndItems.get(historyItemId);
    }

    List<HistoryItem> getItems() {
        return new ArrayList<>(this.items);
    }

    void clear() {
        this.items.clear();
        this.activeItem = null;
    }

    void putStateProperties(String token, Map<String, String> properties) {
        this.stateProperties.put(token, properties);
    }

    Map<String, String> getStateProperties(String token) {
        return this.stateProperties.get(token);
    }

    HistoryItem getHistoyItem(Integer historyId) {
        return this.hidsAndItems.get(historyId);
    }

    public SafeHtml getTitle() {
        return this.title;
    }

    public void setTitle(SafeHtml title) {
        this.title = title;
    }
}