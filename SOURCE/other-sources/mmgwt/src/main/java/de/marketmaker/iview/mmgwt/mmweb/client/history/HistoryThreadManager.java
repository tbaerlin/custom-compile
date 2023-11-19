package de.marketmaker.iview.mmgwt.mmweb.client.history;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.HistoryThreadEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 15.11.12 15:47
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class HistoryThreadManager implements PlaceChangeHandler {
    public static final int MAX_THREAD_NUM = 10;

    private final HandlerManager eventBus;

    private final Map<Integer, HistoryThread> threads = new LinkedHashMap<>();
    private final Map<Integer, HistoryThread> hidsAndThreads = new HashMap<>();

    private HistoryThread activeThread;
    private HistoryThread previousThread;

    @SuppressWarnings("UnusedParameters") // remove this constructor, when idoc has removed second parameter
    public HistoryThreadManager(HandlerManager eventBus, String idocDummy) {
        this(eventBus);
    }

    public HistoryThreadManager(HandlerManager eventBus) {
        newThread(null);
        this.eventBus = eventBus;
        this.eventBus.addHandler(PlaceChangeEvent.getType(), this);
    }

    public void newThread(final HistoryToken token) {
        final HistoryThread thread = new HistoryThread();
        this.threads.put(thread.getId(), thread);
        this.previousThread = this.activeThread;
        switchToThread(thread.getId(), true);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                fireNewThreadEvent(thread.getId());
                if (token != null) {
                    firePlaceChangeEvent(token);
                }
            }
        });
    }

    public HistoryThreadManagerMemento replaceThreads(HistoryToken initialPlace) {
        assert initialPlace != null : "parameter initialPlace must not be null!";

        final HistoryThreadManagerMemento replaced = new HistoryThreadManagerMemento(this);

        this.previousThread = null;
        this.activeThread = null;
        this.threads.clear();
        this.hidsAndThreads.clear();

        newThread(initialPlace.toBuilder().withHistoryId(null).build());
        fireReplacedAllThreadsEvent(this.activeThread.getId());

        return replaced;
    }

    public HistoryThreadManagerMemento replaceThreads(HistoryThreadManagerMemento memento) {
        final HistoryThreadManagerMemento replaced = new HistoryThreadManagerMemento(this);

        this.previousThread = memento.previousThread;
        this.activeThread = memento.activeThread;

        this.threads.clear();
        this.threads.putAll(memento.threads);
        this.hidsAndThreads.clear();
        this.hidsAndThreads.putAll(memento.hidsAndThreads);

        fireReplacedAllThreadsEvent(this.activeThread.getId());

        return replaced;
    }

    private void firePlaceChangeEvent(HistoryToken token) {
        this.eventBus.fireEvent(new PlaceChangeEvent(token));
    }

    private void fireNewThreadEvent(int threadId) {
        this.eventBus.fireEvent(HistoryThreadEvent.createNewThreadEvent(threadId));
    }

    private void fireDelThreadEvent(int threadId) {
        this.eventBus.fireEvent(HistoryThreadEvent.createDelThreadEvent(threadId));
    }

    public void delThread(int threadId, int selectThreadId) {
        if (this.threads.size() <= 1) {
            return;
        }
        this.threads.remove(threadId);
        fireDelThreadEvent(threadId);

        if (this.threads.containsKey(selectThreadId)) {
            switchToThread(selectThreadId);
        }
        else {
            switchToThread(this.threads.keySet().iterator().next());
        }
        this.previousThread = this.activeThread; // Hack, since previous thread is deleted
    }

    public void delThread(int threadId) {
        delThread(threadId, -1);
    }

    public void switchToThread(int threadId) {
        switchToThread(threadId, false);
    }

    public void switchToThread(int threadId, boolean silent) {
        if (!this.threads.containsKey(threadId) || this.threads.get(threadId) == this.activeThread) {
            return;
        }
        this.previousThread = this.activeThread;
        this.activeThread = this.threads.get(threadId);
        if (!silent) {
            fireThreadSwitch(threadId);
            this.activeThread.firePlaceChange(this.activeThread.getActiveItem());
        }
    }

    private void fireThreadSwitch(int threadId) {
        this.eventBus.fireEvent(HistoryThreadEvent.createThreadChangedEvent(threadId));
    }

    private void fireReplacedAllThreadsEvent(int activeThreadId) {
        this.eventBus.fireEvent(HistoryThreadEvent.createThreadReplacedAllEvent(activeThreadId));
    }

    public boolean hasInvalidHistoryId(PlaceChangeEvent event) {
        if (!SessionData.isAsDesign()) {
            return false;
        }
        final HistoryToken currentToken = event.getHistoryToken();
        final Integer hid = currentToken.getHistoryId();
        // no hid means invalid hid (e.g. after login)
        if (hid == null) {
            return true;
        }
        // hid can't be bigger than its counter, so something must be pretty wrong
        if (hid > PlaceChangeEvent.getLastAutoIncId()) {
            return true;
        }
        if (this.hidsAndThreads.containsKey(hid)) {
            final HistoryThread historyThread = this.hidsAndThreads.get(hid);
            final HistoryItem histoyItem = historyThread.getHistoyItem(hid);
            //hid is known but there is no HistoryItem in the registered thread -> someone manipulated the url!
            if (histoyItem == null) {
                return true;
            }
            final HistoryToken savedToken = histoyItem.getPlaceChangeEvent().getHistoryToken();
            //HistoryItem was found but the tokens are unequal -> someone manipulated the url!
            if (!savedToken.equals(currentToken)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPlaceChange(final PlaceChangeEvent event) {
        // if the hid is not valid, fire new PlaceChangeEvent without invalid hid and generate a new valid hid
        if (hasInvalidHistoryId(event)) {
            Firebug.warn("onPlaceChange hasInvalidHistoryId " + event.toDebugString());
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    //set breadcrumb mark again (breadcrumb back and context list switch problem in conjunction with browser back/forward)
                    final HistoryItem activeItem = activeThread.getActiveItem();
                    if (activeItem != null) {
                        final PlaceChangeEvent placeChangeEvent = activeItem.getPlaceChangeEvent();
                        if (placeChangeEvent != null) {
                            if (placeChangeEvent.getHistoryContext() != null && placeChangeEvent.getHistoryContext().isBreadCrumb()) {
                                activeItem.markAsBreadCrumb();
                            }
                        }
                    }

                    // fire a new event.
                    // Do not reuse the list context if the active item has one, because it will be
                    // very confusing if you are preparing to view an account with context list items of the currently
                    // active context, e.g., a depot context list. In such a case the user views an account, but if she
                    // switches to the next context list, she will get a depot instead of an account!
                    // see: AS-1188
                    final HistoryToken.Builder tokenBuilder = HistoryToken.Builder.fromHistoryToken(event.getHistoryToken());
                    final PlaceChangeEvent newEvent = createPlaceChangeEvent(activeItem, tokenBuilder);
                    if (newEvent != null) {
                        HistoryThreadManager.this.eventBus.fireEvent(newEvent);
                    } else {
                        HistoryThreadManager.this.eventBus.fireEvent(tokenBuilder.buildEvent());
                    }
                }
            });
            return;
        }
        // create new HistoryItem to represent the next "view", if it doesn´t exists
        // (hidsAndThreads contains hid) means the item exists!
        final Integer hid = event.getHistoryToken().getHistoryId();
        if (this.hidsAndThreads.containsKey(hid)) {
            // switch to assigned thread
            final HistoryThread thread = this.hidsAndThreads.get(hid);
            switchToThread(thread.getId());
            thread.setActiveItem(hid);
        }
        else {
            // create new HistoryItem
            if (event.getHistoryContext() != null && event.getHistoryContext().isBreadCrumb()) {
                this.activeThread.getActiveItem().markAsBreadCrumb();
            }
            else {
                final HistoryItem activeItem = this.activeThread.getActiveItem();
                if (activeItem != null) {
                    activeItem.markAsNotBreadCrumb();
                }
            }
            final HistoryItem item = HistoryItem.createItem(event);
            this.activeThread.add(item);
            this.hidsAndThreads.put(item.getHid(), this.activeThread);
        }
    }

    private PlaceChangeEvent createPlaceChangeEvent(HistoryItem activeItem, HistoryToken.Builder tokenBuilder) {
        if(activeItem == null) {
            return null;
        }

        final HistoryContext activeContext = activeItem.getPlaceChangeEvent().getHistoryContext();

        if(activeContext == null) {
            // possibly navigating in sub controllers (only relevant to handle browser back)
            final HistoryItem upstream = getNearestUpstreamNonNullOrExplicitNullContext();
            if (upstream == null) {
                return null;
            }

            final HistoryContext upstreamContext = upstream.getPlaceChangeEvent().getHistoryContext();
            if (upstreamContext == null) {
                return null;
            }
            return getPlaceChangeEventWithEmptyContextWithoutContextLists(tokenBuilder.build(), upstreamContext);
        }

        return getPlaceChangeEventWithEmptyContextWithoutContextLists(tokenBuilder.build(), activeContext);
    }

    private PlaceChangeEvent getPlaceChangeEventWithEmptyContextWithoutContextLists(HistoryToken newToken, HistoryContext historyContext) {
        final EmptyContext newContext = EmptyContext.create(historyContext.getName())
                .withIconKey(historyContext.getIconKey())
                .withoutBreadCrumb();
        return new PlaceChangeEvent(newToken, newContext);
    }

    public void saveState(HistoryItem historyItem, ThreadStateHandler hss) {
        if (historyItem == null || hss == null) {
            return;
        }
        try {
            final String stateKey = getStateKey(historyItem, hss);
            if (StringUtil.hasText(stateKey)) {
                final HistoryThread thread = this.hidsAndThreads.get(historyItem.getHid());
                thread.putStateProperties(stateKey, hss.saveState(historyItem));
            }
        }
        catch (Exception e) {
            Firebug.warn("<HistoryThreadManager.saveState> exception while saving state", e);
        }
    }


    public void loadState(HistoryItem historyItem, ThreadStateHandler hss) {
        if (historyItem == null || hss == null) {
            return;
        }
        try {
            final String stateKey = getStateKey(historyItem, hss);
            if (StringUtil.hasText(stateKey)) {
                final HistoryThread thread = this.hidsAndThreads.get(historyItem.getHid());
                hss.loadState(historyItem, thread.getStateProperties(stateKey));
            }
        }
        catch (Exception e) {
            Firebug.warn("<HistoryThreadManager.loadState> exception while loading state", e);
        }
    }

    private String getStateKey(HistoryItem historyItem, ThreadStateHandler hss) {
        try {
            return hss.getStateKey(historyItem);
        }
        catch (GetStateKeyException ske) {
            Firebug.error("<HistoryThreadManager.loadState> caught GetStateKeyException: " + ske.getMessage(), ske.getCause());
            return null;
        }
    }

    public List<Integer> getAllHistoryThreadIds() {
        return new ArrayList<>(this.threads.keySet());
    }

    public int getActiveThreadId() {
        return getActiveThread().getId();
    }

    HistoryThread getActiveThread() {
        return this.activeThread;
    }

    HistoryThread getHistoryThread(int threadId) {
        return this.threads.get(threadId);
    }

    public SafeHtml getThreadTitle(int threadId) {
        return getHistoryThread(threadId).getTitle();
    }

    public void clear() {
        for (Map.Entry<Integer, HistoryThread> entry : this.threads.entrySet()) {
            entry.getValue().clear();
        }
        this.hidsAndThreads.clear();
    }

    public void back() {
        this.activeThread.back();
    }

    public void backToBreadCrumb() {
        this.activeThread.backToBreadCrumb();
    }

    public void next() {
        this.activeThread.next();
    }


    public HistoryItem getActiveThreadHistoryItem() {
        return this.activeThread.getActiveItem();
    }

    public HistoryItem getNearestUpstreamNonNullOrExplicitNullContext() {
        return this.activeThread.getNearestUpstreamNonNullOrExplicitNullContext();
    }

    public HistoryItem getPreviousThreadHistoryItem() {
        return this.previousThread.getActiveItem();
    }

    public HistoryThread getPreviousThread() {
        return this.previousThread;
    }

    public int getPreviousThreadId() {
        return this.previousThread.getId();
    }

    public boolean hasPreviousThread() {
        return this.previousThread != null;
    }

    public void setThreadTitle(int threadId, SafeHtml safeHtml) {
        final HistoryThread thread = getHistoryThread(threadId);
        if(thread == null) {
            Firebug.error("no history thread with id " + threadId + " found");
            return;
        }
        thread.setTitle(safeHtml);

        final HistoryItem activeItem = thread.getActiveItem();
        if(activeItem != null) {
            activeItem.setDebugHint(safeHtml);
        }
    }

    public static final class HistoryThreadManagerMemento {
        private final Map<Integer, HistoryThread> threads = new LinkedHashMap<>();
        private final Map<Integer, HistoryThread> hidsAndThreads = new HashMap<>();

        private HistoryThread activeThread;
        private HistoryThread previousThread;

        private HistoryThreadManagerMemento(HistoryThreadManager htm) {
            this.threads.putAll(htm.threads);
            this.hidsAndThreads.putAll(htm.hidsAndThreads);
            this.activeThread = htm.activeThread;
            this.previousThread = htm.previousThread;
        }
    }
}