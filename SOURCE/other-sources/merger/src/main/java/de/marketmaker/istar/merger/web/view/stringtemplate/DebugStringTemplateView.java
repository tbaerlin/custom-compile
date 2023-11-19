/*
 * DebugStringTemplateView.java
 *
 * Created on 15.01.2010 11:15:01
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.view.stringtemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTimeConstants;
import org.springframework.util.ClassUtils;

import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;

import net.jcip.annotations.ThreadSafe;

/**
 * Sometimes, specific requests may end up in an endless rendering loop if the templates
 * are not quite right. This class helps to detect such issues. For each template to be
 * rendered, it puts an object into a map that will be removed right after the rendering
 * is done. Once per minute, a background thread checks whether there are any objects in
 * the map older than 1 min. If they are, details for the request belonging to that object
 * will be logged.
 *
 * @author oflege
 */
public class DebugStringTemplateView extends StringTemplateView {
    @ThreadSafe
    private static class PendingRendering {
        private final Map model;
        private final long timestamp;

        private final String uri;

        private final MoleculeRequest mr;

        private final Map<String, String[]> parameters;

        // invoked by thread that renders the view; Since HttpServletRequest is not thread-safe,
        // we have to extract all the information that we need later in the background thread
        // to log a long running rendering task.
        private PendingRendering(Map model, HttpServletRequest request) {
            this.timestamp = System.currentTimeMillis();
            this.model = model;
            this.uri = request.getRequestURI();
            this.mr = (MoleculeRequest)
                    request.getAttribute(MoleculeRequest.REQUEST_ATTRIBUTE_NAME);
            if (this.mr == null) {
                this.parameters = new HashMap<>(request.getParameterMap());
            }
            else {
                this.parameters = null;
            }
        }

        public Map getModel() {
            return this.model;
        }

        public long getTimestamp() {
            return this.timestamp;
        }

        // invoked by background thread to log this object; thread-safe because this object
        // was retrieved from a ConcurrentHashMap and contains only immutable data
        @Override
        public String toString() {
            return HttpRequestUtil.toString(this.uri, this.mr, this.parameters);
        }

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugStringTemplateView.class);

    private static final AtomicInteger KEY_SEQUENCE = new AtomicInteger();

    private static final ConcurrentMap<String, PendingRendering> PENDING =
            new ConcurrentHashMap<>();

    private static final Timer TIMER
            = new Timer(ClassUtils.getShortName(DebugStringTemplateView.class), true);

    static {
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                checkPendingRenderings();
            }
        }, DateTimeConstants.MILLIS_PER_MINUTE, DateTimeConstants.MILLIS_PER_MINUTE);
    }

    private static void checkPendingRenderings() {
        final Set<PendingRendering> evicted = evictOldEntries();

        for (PendingRendering rendering : evicted) {
            LOGGER.warn("<checkPendingRenderings> possibly endless loop for " + rendering);
        }
    }

    private static Set<PendingRendering> evictOldEntries() {
        final long threshold = System.currentTimeMillis() - DateTimeConstants.MILLIS_PER_MINUTE;

        final Set<PendingRendering> result = new HashSet<>();

        final Iterator<Map.Entry<String, PendingRendering>> it = PENDING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, PendingRendering> entry = it.next();
            if (entry.getValue().getTimestamp() < threshold) {
                result.add(entry.getValue());
                it.remove();
            }
        }
        return result;
    }

    @Override
    protected void renderMergedTemplateModel(Map map, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        final String key = Integer.toString(KEY_SEQUENCE.incrementAndGet(), Character.MAX_RADIX);
        PENDING.put(key, new PendingRendering(map, request));
        try {
            super.renderMergedTemplateModel(map, request, response);
        } finally {
            PENDING.remove(key);
        }
    }
}
