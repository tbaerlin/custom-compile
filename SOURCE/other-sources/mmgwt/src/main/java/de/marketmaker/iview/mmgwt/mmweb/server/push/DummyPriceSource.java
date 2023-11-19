package de.marketmaker.iview.mmgwt.mmweb.server.push;

import de.marketmaker.iview.mmgwt.mmweb.client.push.PushPrice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created on 04.02.2010 11:49:22
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class DummyPriceSource implements ServletContextListener {

    class QuotePriceGenerator {
        private final PushPrice price;
        private final String vwdCode;
        private Timer timer;
        private PriceObserver observer;

        QuotePriceGenerator(String vwdCode) {
            this.vwdCode = vwdCode;
            this.price = new PushPrice();
            this.price.setVwdCode(vwdCode);
        }

        private String randomString(Random random) {
            return String.valueOf(random.nextInt(100));
        }

        private long randomLong(Random random) {
            return Integer.valueOf(random.nextInt(100)).longValue();
        }

        public void runGenerator() {
            final Random random = new Random();
            this.timer = new Timer();
            this.timer.schedule(new TimerTask() {
                public void run() {
                    price.setPrice(randomString(random));
                    price.setAsk(randomString(random));
                    price.setAskVolume(randomLong(random));
                    price.setBid(randomString(random));
                    price.setBidVolume(randomLong(random));
                    price.setHigh(randomString(random));
                    price.setLow(randomString(random));
                    price.setNumberOfTrades(randomLong(random));
                    price.setTurnoverDay(randomString(random));
                    price.setVolumeDay(randomLong(random));
                    if (observer != null) {
                        observer.update(vwdCode, price);
                    }
                }
            }, 0, random.nextInt(10000));
        }

        public void setPriceObserver(PriceObserver observer) {
            this.observer = observer;
        }

        public boolean hasObserver() {
            return this.observer != null;
        }

        public void stop() {
            this.observer = null;
            if (this.timer != null) {
                this.timer.cancel();
            }
        }
    }

    public static DummyPriceSource INSTANCE;

    final Log logger = LogFactory.getLog(getClass());

    private List<QuotePriceGenerator> availableQuotes;

    public DummyPriceSource() {
    }

    private void initInstance() {
        INSTANCE = this;
    }

    public void contextInitialized(ServletContextEvent sce) {
        initInstance();
        this.availableQuotes = new ArrayList<>();
        this.availableQuotes.add(new QuotePriceGenerator("840400.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("515100.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("519000.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("BAY001.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("520000.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("803200.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("710000.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("514000.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("581005.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("555200.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("555750.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("ENAG99.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("578580.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("578563.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("604843.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("623100.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("716200.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("648300.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("823212.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("593700.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("659990.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("725750.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("843002.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("703712.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("716460.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("620200.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("723610.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("750000.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("766403.ETR"));
        this.availableQuotes.add(new QuotePriceGenerator("500340.ETR"));
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }

    public void registerForUpdates(String vwdCode, PriceObserver observer) {
        this.logger.info(getClass().getName() + " registerForUpdates vwdCode: " + vwdCode);
        for (QuotePriceGenerator qpg : this.availableQuotes) {
            if (qpg.vwdCode.equals(vwdCode) && !qpg.hasObserver()) {
                System.out.println(getClass().getName() + " registerForUpdates runGenerator!");
                qpg.setPriceObserver(observer);
                qpg.runGenerator();
            }
        }
    }

    public void unregisterForUpdates(String vwdCode) {
        this.logger.info(getClass().getName() + " unRegisterForUpdates vwdCode: " + vwdCode);
        for (QuotePriceGenerator qpg : this.availableQuotes) {
            if (qpg.vwdCode.equals(vwdCode) && qpg.hasObserver()) {
                this.logger.info(getClass().getName() + " QuotePriceGenerator stop!");
                qpg.stop();
            }
        }

    }

}
