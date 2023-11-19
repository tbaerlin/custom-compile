/*
 * News2XmlPush.java
 *
 * Created on 23.03.2007 14:24:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.ClassUtils;
import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import de.marketmaker.istar.common.util.PeriodEditor;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.connect.BufferWriter;
import de.marketmaker.istar.merger.web.view.stringtemplate.StringTemplateViewResolver;
import de.marketmaker.istar.news.backend.NewsRecordHandler;
import de.marketmaker.istar.news.data.NewsRecordImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class News2XmlPush implements NewsRecordHandler, InitializingBean {
    private static final AtomicInteger THREAD_ID = new AtomicInteger();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String groupName = "ins";

    private String viewName = "inspushnews";

    private StringTemplateViewResolver viewResolver;

    private STGroup group;

    private BufferWriter sink;

    private int defaultBufferSize = 8192;

    private String charsetName = "UTF-8";

    private String xsdUrl;

    private boolean blockAds = false;

    private Period doNotPushNewsOlderThan = Period.hours(12);

    /**
     * we have to coordinate heartbeats and regular news, so its best to run everything
     * in a single thread
     */
    private final ScheduledExecutorService ses
            = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new Thread(r, ClassUtils.getShortName(getClass()) + "-" + THREAD_ID.incrementAndGet());
        }
    });

    /**
     * A pending heatbeat that will be cancelled when a new news arrives before the heartbeat is due.
     * This field will only be read/written by ses's worker thread
     */
    private ScheduledFuture<?> pendingHeartbeat;

    /**
     * Performs the actual heartbeat action
     */
    private final Runnable heartbeat = new Runnable() {
        public void run() {
            News2XmlPush.this.pendingHeartbeat = null;
            doHandle(null);
        }
    };

    /**
     * time between a previous event (heartbeat or news) and the next heartbeat. Set to 0 to
     * disable heartbeats
     */
    private int heartbeatIntervalInSeconds = 60;

    public News2XmlPush() {
    }

    public void setXsdUrl(String xsdUrl) {
        this.xsdUrl = xsdUrl;
    }

    public void setDoNotPushNewsOlderThan(String doNotPushNewsOlderThan) {
        this.doNotPushNewsOlderThan = PeriodEditor.fromText(doNotPushNewsOlderThan);
        this.logger.info("<setDoNotPushNewsOlderThan> " + this.doNotPushNewsOlderThan);
    }

    public void setHeartbeatIntervalInSeconds(int heartbeatIntervalInSeconds) {
        this.heartbeatIntervalInSeconds = heartbeatIntervalInSeconds;
        if (this.heartbeatIntervalInSeconds > 0) {
            this.logger.info("<setHeartbeatIntervalInSeconds> " + this.heartbeatIntervalInSeconds);
        }
        else {
            this.logger.info("<setHeartbeatIntervalInSeconds> heartbeat disabled");
        }
    }

    public void setDefaultBufferSize(int defaultBufferSize) {
        this.defaultBufferSize = defaultBufferSize;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public void setSink(BufferWriter sink) {
        this.sink = sink;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setViewResolver(StringTemplateViewResolver viewResolver) {
        this.viewResolver = viewResolver;
    }

    public void setBlockAds(boolean blockAds) {
        this.blockAds = blockAds;
    }

    public void afterPropertiesSet() throws Exception {
        this.group = this.viewResolver.getGroup(this.groupName);
        scheduleNextHeartbeat();
    }

    public void handle(final NewsRecordImpl newsRecord) {
        if (!shouldBePushed(newsRecord)) {
            return;
        }
        this.ses.submit(() -> doHandle(newsRecord));
    }

    private boolean shouldBePushed(NewsRecordImpl nr) {
        DateTime at = nr.getAgencyTimestamp();
        if (at == null) {
            at = nr.getNdbTimestamp();
        }
        return at == null || at.plus(this.doNotPushNewsOlderThan).isAfter(new DateTime());
    }

    private void doHandle(NewsRecordImpl newsRecord) {
        if (this.pendingHeartbeat != null) {
            this.pendingHeartbeat.cancel(false);
        }

        try {
            final TimeTaker tt = new TimeTaker();
            final ByteBuffer bb = toBuffer(newsRecord);
            final int numBytes = bb.remaining();
            this.sink.write(bb);

            if (this.logger.isDebugEnabled()) {
                if (newsRecord != null) {
                    this.logger.debug("<handle> successfully sent news for " + newsRecord.getId() + " w/ " + numBytes + " bytes in " + tt);
                }
                else {
                    this.logger.debug("<handle> sent heartbeat");
                }
            }

        } catch (Exception e) {
            this.logger.error("<handle> failed", e);
        } finally {
            scheduleNextHeartbeat();
        }
    }

    private void scheduleNextHeartbeat() {
        if (this.heartbeatIntervalInSeconds > 0) {
            this.pendingHeartbeat =
                    this.ses.schedule(this.heartbeat, this.heartbeatIntervalInSeconds, TimeUnit.SECONDS);
        }
    }

    private ByteBuffer toBuffer(NewsRecordImpl newsRecord) throws IOException {
        if (newsRecord == null) {
            return toBuffer(new byte[0]);
        }

        final ST template = this.group.getInstanceOf(this.viewName);

        template.add("item", newsRecord);
        template.add("blockAds", this.blockAds);
        template.add("xsdUrl", this.xsdUrl);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(this.defaultBufferSize);
        final OutputStreamWriter osw = new OutputStreamWriter(baos, Charset.forName(this.charsetName));
        template.write(new NoIndentWriter(osw));
        osw.close();

        return toBuffer(baos.toByteArray());
    }

    private ByteBuffer toBuffer(byte[] bytes) {
        final ByteBuffer result = ByteBuffer.wrap(new byte[bytes.length + 4]);
        result.putInt(bytes.length);
        result.put(bytes, 0, bytes.length);
        result.flip();
        return result;
    }
}
