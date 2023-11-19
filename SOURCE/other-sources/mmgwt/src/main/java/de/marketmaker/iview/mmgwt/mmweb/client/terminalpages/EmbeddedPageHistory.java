/*
 * EmbeddedPageHistory.java
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.terminalpages;

import de.marketmaker.iview.mmgwt.mmweb.client.Ginjector;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.logging.Logger;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * @author Stefan Willenbrock
 */
@NonNLS
public class EmbeddedPageHistory {

    public static final String PAGE_HISTORY_TOKEN = "HIST"; // $NON-NLS$

    private static final int HISTORY_SIZE_LIMIT = 10;

    private final String pageKey;

    private final String defaultPageId;

    private final int limit;

    private String current = null;

    private final List<String> next = new ArrayList<>();

    private final List<String> previous = new ArrayList<>();

    private Logger logger = Ginjector.INSTANCE.getLogger();

    public EmbeddedPageHistory(String pageKey, String defaultPageId) {
        this(pageKey, defaultPageId, HISTORY_SIZE_LIMIT);
    }

    public EmbeddedPageHistory(String pageKey, String defaultPageId, int limit) {
        this(pageKey, defaultPageId, limit, Ginjector.INSTANCE.getLogger());
    }

    public EmbeddedPageHistory(String pageKey, String defaultPageId, int limit, Logger logger) {
        this.pageKey = pageKey;
        this.defaultPageId = defaultPageId;
        this.limit = limit;
        this.logger = logger;
    }

    public void updatePageHistory(HistoryToken historyToken) {
        final String t = historyToken.getByNameOrIndexFromAll("s", 1, this.defaultPageId); // $NON-NLS$
        if (hasValidVwdPage(historyToken)) {
            if (isHistoryToken(historyToken)) {
                visitHistory(t);
            }
            else {
                visitNew(t);
            }
        }
    }

    private boolean hasValidVwdPage(HistoryToken historyToken) {
        final int numParams = historyToken.getIndexedParams().size();

        if (1 < numParams && historyToken.get(0).equals(this.pageKey)) {
            try {
                Integer.valueOf(historyToken.get(1));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    public boolean isHistoryToken(HistoryToken historyToken) {
        final int numParams = historyToken.getIndexedParams().size();
        if (3 == numParams) {
            return historyToken.get(2).equals(PAGE_HISTORY_TOKEN);
        }
        return false;
    }

    String next() {
        if (hasNext()) {
            final String item = moveLast(this.next, this.previous);
            return item;
        }
        return null;
    }

    String previous() {
        if (hasPrevious()) {
            final String item = moveLast(this.previous, this.next);
            return item;
        }
        return null;
    }

    /**
     * @return The new current element
     */
    private String moveLast(List<String> from, List<String> to) {
        int last = from.size() - 1;
        String n = from.get(last);
        from.remove(last);
        addLimited(to, this.current);
        return n;
    }

    void visitNew(String page) {
        this.next.clear();
        addLimited(this.previous, this.current);
        visitHistory(page);
    }

    void visitHistory(String page) {
        this.current = page;
        logHistory();
    }

    private void addLimited(List<String> list, String element) {
        if (element == null) {
            return;
        }
        if (list.size() > this.limit - 1 && !list.isEmpty()) {
            list.remove(0);
        }
        list.add(element);
    }

    boolean hasNext() {
        return !this.next.isEmpty();
    }

    boolean hasPrevious() {
        return !this.previous.isEmpty();
    }

    @Inject
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    protected void logHistory() {
        final List<String> reversedNext = new ArrayList<>(this.next);
        Collections.reverse(reversedNext);
        this.logger.log(getClass().getSimpleName()
                + "<logHistory> previous: " + Arrays.deepToString(this.previous.toArray())
                + ", current: " + this.current
                + ", next: " + Arrays.deepToString(reversedNext.toArray()));
    }
}
