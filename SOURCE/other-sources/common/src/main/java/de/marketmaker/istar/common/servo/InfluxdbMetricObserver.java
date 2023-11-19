/*
 * InfluxdbMetricObserver.java
 *
 * Created on 02.09.15 09:07
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.servo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.netflix.servo.Metric;
import com.netflix.servo.annotations.DataSourceLevel;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.annotations.MonitorTags;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.StandardTagKeys;
import com.netflix.servo.tag.TagList;
import com.netflix.servo.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.Constants;

import static de.marketmaker.istar.common.servo.ServoObjectNameMapper.getMeasurement;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Sends metrics to an influxdb http interface. Assumes that all metrics can be posted in a
 * single request.
 * @author oflege
 */
public class InfluxdbMetricObserver implements MetricObserver {
    private static final int SLEEP_MS_BEFORE_RETRY = 5000;

    private static final int SLEEP_MS_AFTER_TIMEOUT = 1000;

    private static final int MAX_RETRY_COUNT = 10;

    private static final DecimalFormat DF = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    static {
        DF.applyLocalizedPattern("0.#######");
    }

    private static String formatValue(final Number number) {
        synchronized (DF) {
            return DF.format(number.doubleValue());
        }
    }

    private static final String DB = Constants.getProperty("influxdb.database", Constants.DOMAIN_ID);

    private static final String DEFAULT_TAGS;

    static {
        final StringBuilder sb = new StringBuilder()
                .append(",app=").append(Constants.APP_NAME)
                .append(",host=").append(Constants.MACHINE_NAME);
        if (!DB.equals(Constants.DOMAIN_ID)) {
            sb.append(",domain=").append(Constants.DOMAIN_ID);
        }
        DEFAULT_TAGS = sb.toString();
    }

    /**
     * @return an InfluxdbMetricObserver or null iff<ul>
     * <li>{@link Constants#DOMAIN_ID} == "dev" and System property <tt>influxdb.observer</tt>
     * is not <tt>true</tt></li>
     * <li>System property <tt>influxdb.observer</tt> is </tt>false</tt></li>
     * </ul>
     */
    static InfluxdbMetricObserver create() {
        final String value = Constants.getProperty("influxdb.observer", null);
        if (Constants.DEV_DOMAIN.equals(Constants.DOMAIN_ID)) {
            if (!"true".equals(value)) {
                return null;
            }
        }
        else if ("false".equals(value)) {
            return null;
        }

        final String host = Constants.getProperty("influxdb.host", "dz-elastic");
        try {
            //noinspection ResultOfMethodCallIgnored
            InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            LoggerFactory.getLogger(InfluxdbMetricObserver.class).error("<create> host '" + host
                    + "' is unknown, InfluxdbMetricObserver is disabled");
            return null;
        }
        return new InfluxdbMetricObserver(host);
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final URL url;

    @SuppressWarnings("unused")
    @MonitorTags
    private final TagList tags
            = BasicTagList.of(Tags.newTag(StandardTagKeys.MONITOR_ID.getKeyName(), "influxdb"));

    /**
     * Total number of times update has been called.
     */
    @Monitor(name = "updateCount", type = DataSourceType.COUNTER)
    private final AtomicInteger updateCount = new AtomicInteger(0);

    /**
     * Total number of metrics submitted to influxdb.
     */
    @Monitor(name = "metricsCount", type = DataSourceType.COUNTER)
    private final AtomicInteger metricsCount = new AtomicInteger(0);

    /**
     * Number of times update failed with an exception.
     */
    @Monitor(name = "updateFailureCount", type = DataSourceType.COUNTER)
    private final AtomicInteger failedUpdateCount = new AtomicInteger(0);

    private InfluxdbMetricObserver(String host) {
        try {
            this.url = new URL("http://" + host + ":8086/write?db=" + DB + "&precision=s");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.logger.info("<init> url=" + this.url);
    }

    @Override
    public void update(List<Metric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return;
        }
        try {
            if (!doUpdate(metrics)) {
                this.failedUpdateCount.incrementAndGet();
            }
        } finally {
            this.updateCount.incrementAndGet();
            this.metricsCount.addAndGet(metrics.size());
        }
    }

    @Override
    public String getName() {
        return "influxdb";
    }

    private boolean doUpdate(List<Metric> metrics) {
        int n = 0;
        byte[] postData = format(metrics);
        while (n++ < MAX_RETRY_COUNT) {
            try {
                boolean success = sendMetrics(postData);
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<doUpdate> sent " + metrics.size() + " metrics to influxdb");
                }
                return success;
            } catch (ConnectException | UnknownHostException e) {
                // service or name service probably temporarily unavailable
                this.logger.warn("<doUpdate> catched " + e.getClass().getName()
                        + " for " + this.url.toString());
                if (!sleep(SLEEP_MS_BEFORE_RETRY)) {
                    return false;
                }
            } catch (SocketTimeoutException ce) {
                this.logger.warn("<doUpdate> socket timeout for " + this.url.toString());
                if (!sleep(SLEEP_MS_AFTER_TIMEOUT)) {
                    return false;
                }
            } catch (IllegalArgumentException e) {
                // try to figure out which metric caused the bad request:
                if (metrics.size() == 1) {
                    this.logger.warn("<doUpdate> received BAD_REQUEST for \n" +
                            new String(postData, StandardCharsets.UTF_8));
                }
                else {
                    int mid = metrics.size() / 2;
                    doUpdate(metrics.subList(0, mid));
                    doUpdate(metrics.subList(mid, metrics.size()));
                }
                return false;
            } catch (Exception e) {
                this.logger.warn("<doUpdate> failed", e);
                return false;
            }
        }
        this.logger.warn("<doUpdate> failed, max number of retries exceeded");
        return false;
    }

    private boolean sleep(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            this.logger.warn("<doUpdate> interrupted?");
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    protected boolean sendMetrics(byte[] postData) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(4000);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
        try (OutputStream wr = conn.getOutputStream()) {
            wr.write(postData);
        }
        final int rc = conn.getResponseCode();
        if (rc == HttpURLConnection.HTTP_NO_CONTENT) {
            return true;
        }

        if (rc == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new IllegalArgumentException();
        }

        this.logger.warn("<sendMetrics> unexpected response code " + rc);
        return false;
    }

    private byte[] format(List<Metric> metrics) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(metrics.size() * 80);
        try (OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            for (Metric m : metrics) {
                final String measurement = asMeasurement(m);
                osw.append(measurement).append('\n');
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(measurement);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e); // should never happen
        }
        return baos.toByteArray();
    }

    public static String asMeasurement(Metric m) {
        final StringBuilder sb = new StringBuilder(80);
        Map<String, String> tags = new LinkedHashMap<>(m.getConfig().getTags().asMap());

        tags.remove(DataSourceLevel.KEY);
        tags.remove("class");
        tags.remove("unit"); // usually unit=MILLISECONDS, probably not necessary

        final String dstype = tags.remove(DataSourceType.KEY);

        final String measurement = getMeasurement(m.getConfig(), tags);
        sb.append(measurement.replace('.', '_')).append(DEFAULT_TAGS)
//                .append(",dstype=").append(dstype)
        ;
        for (Map.Entry<String, String> e : tags.entrySet()) {
            sb.append(',');
            if (!e.getValue().contains("=")) {
                sb.append(e.getKey()).append('=');
            }
            sb.append(e.getValue());
        }

        sb.append(" value=");
        if (m.hasNumberValue()) {
            sb.append(formatValue(m.getNumberValue()));
        }
        else {
            sb.append("\"").append(String.valueOf(m.getValue())).append("\"");
        }
        // influxdb can better compress data if seconds are used; since we sample at
        // most every 10s, that is fine with us; if this is changed, the precision
        // parameter in the url has to be changed accordingly
        sb.append(" ").append(MILLISECONDS.toSeconds(m.getTimestamp()));
        return sb.toString();
    }
}
