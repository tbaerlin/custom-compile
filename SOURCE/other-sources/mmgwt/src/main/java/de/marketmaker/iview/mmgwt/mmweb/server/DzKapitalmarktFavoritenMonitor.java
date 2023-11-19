/*
 * UserMessageMonitor.java
 *
 * Created on 07.08.2008 13:00:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.block.MscVwdCmsKeyGenerator;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DzKapitalmarktFavoritenMonitor implements MmwebResponseListener, InitializingBean,
        DisposableBean {

    protected final Log logger = LogFactory.getLog(getClass());

    private String url;

    private volatile String lastTimestamp;

    private Timer timer;

    private long period = 5 * 60 * 1000; // every 5min

    private final int timeout = (int) TimeUnit.SECONDS.convert(10, TimeUnit.MILLISECONDS);

    public void afterPropertiesSet() throws Exception {
        if (this.url == null) {
            this.logger.info("<afterPropertiesSet> no url set => deactivated");
            return;
        }
        this.timer = new Timer(getClass().getSimpleName(), true);
        this.timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                updateTimestamp();
            }
        }, 0, this.period);
    }


    public void setUrl(String url) {
        if (StringUtils.hasText(url)) {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                this.logger.warn("<setUrl> failed", e);
                throw new IllegalArgumentException(e);
            }
            this.url = url;
        }
    }

    public void destroy() throws Exception {
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    public void onBeforeSend(HttpSession session, MmwebResponse response) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (!profile.isAllowed(Selector.DZ_BANK_USER)) {
            return;
        }
        final String id = this.lastTimestamp;
        if (id != null) {
            response.addProperty(AppConfig.PROP_KEY_KAPITALMARKTFAVORITENID, id);
        }
    }

    public void setPeriod(long period) {
        this.period = period;
        this.logger.info("<setPeriod> " + this.period);
    }

    private void updateTimestamp() {
        final long lastModified = getLastModified();
        if (lastModified > 0) {
            final String update = Long.toString(lastModified, Character.MAX_RADIX);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<updateTimestamp> lastModified = " + new DateTime(lastModified));
            }
            this.lastTimestamp = update;
        }
    }

    private long getLastModified() {
        try {
            final String request = this.url + MscVwdCmsKeyGenerator.getVwdCmsQueryParameters();
            final HttpURLConnection con = (HttpURLConnection) new URL(request).openConnection();
            con.setRequestMethod("HEAD");
            con.setReadTimeout(this.timeout);
            con.setConnectTimeout(this.timeout);
            con.connect();
            final long result = con.getHeaderFieldDate("X-Last-Modified", 0);
            con.disconnect();
            return result;
        } catch (Exception e) {
            this.logger.warn("<getLastModified> failed", e);
        }
        return 0;
    }

    public static void main(String[] args) throws Exception {
        final DzKapitalmarktFavoritenMonitor monitor = new DzKapitalmarktFavoritenMonitor();
        monitor.setPeriod(4000);
        monitor.setUrl("http://gisweb.vwd.com/dzbank/interface/Publikationen/checkMd5.htn?");
        monitor.afterPropertiesSet();
        Thread.sleep(20000);
        System.out.println("Output: " + monitor.getLastModified());
        monitor.destroy();
    }
}