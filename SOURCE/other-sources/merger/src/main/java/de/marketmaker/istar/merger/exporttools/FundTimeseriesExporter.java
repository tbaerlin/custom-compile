/*
 * SignalExporter.java
 *
 * Created on 19.02.2009 11:14:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.exporttools;

import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseries;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesProvider;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesRequest;
import de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundTimeseriesExporter implements ApplicationContextAware,
    ApplicationListener<ContextRefreshedEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final DecimalFormat PF = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    private static final Set<String> FUND_MARKETS = new HashSet<String>();

    static {
        PF.applyLocalizedPattern("0.00#");

        FUND_MARKETS.add("FONDS");
        FUND_MARKETS.add("SWXFO");
    }

    private HistoricTimeseriesProvider historicTimeseriesProvider;

    private File instrumentFile;

    private File outDir;

    private PrintWriter pw;

    private int limit = 0;

    private LocalDate firstDate = new LocalDate(1970, 1, 1);

    private ConfigurableApplicationContext appCtx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appCtx = (ConfigurableApplicationContext) applicationContext;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setOutDir(File outDir) {
        this.outDir = outDir;
    }

    public void setHistoricTimeseriesProvider(
            HistoricTimeseriesProvider historicTimeseriesProvider) {
        this.historicTimeseriesProvider = historicTimeseriesProvider;
    }

    public void setInstrumentFile(File instrumentFile) {
        this.instrumentFile = instrumentFile;
    }

    public void setMonthsToExport(int months) {
        if (months > 0) {
            this.firstDate = new LocalDate().minusMonths(months + 1).withDayOfMonth(1);
        }
        this.logger.info("<setFirstDate> " + this.firstDate);
    }

    private void export() throws Exception {
        RequestContextHolder.setRequestContext(new RequestContext(ProfileFactory.valueOf(true), LbbwMarketStrategy.INSTANCE));
        final TimeTaker tt = new TimeTaker();

        final File tempfile = new File(this.outDir, "fund-performances-" + DTF.print(new LocalDate()) + ".csv.gz.tmp");
        final File finalfile = new File(this.outDir, "fund-performances-" + DTF.print(new LocalDate()) + ".csv.gz");
        this.pw = null;
        int n = 0;
        int total = 0;
        InstrumentDirDao dao = null;
        try {
            this.pw = new PrintWriter(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(tempfile))));

            dao = new InstrumentDirDao(this.instrumentFile.getParentFile());

            final Iterator<Instrument> it = dao.iterator();
            while (it.hasNext()) {
                final Instrument instrument = it.next();
                total++;
                final List<Quote> quotes = getQuotes(instrument);
                if (quotes == null) {
                    continue;
                }

                for (final Quote quote : quotes) {
                    exportTimeseries(quote);
                    if (++n == this.limit) {
                        this.logger.info("<export> limit reached");
                        break;
                    }
                    if (n % 1000 == 0) {
                        this.logger.info("<export> " + n + ", " + total);
                    }
                }
            }
        } finally {
            IoUtils.close(this.pw);
        }
        try {
            tempfile.renameTo(finalfile);
            this.logger.info("<export> renamed tempfile, wrote " + finalfile.getAbsolutePath());
        } catch (Exception e) {
            this.logger.error("<export> cannot rename "+tempfile.getAbsolutePath()+" to "+finalfile.getAbsolutePath());
        }
        this.logger.info("<export> wrote " + n + " quotes, took " + tt);
    }

    private List<Quote> getQuotes(Instrument instrument) {
        switch (instrument.getInstrumentType()) {
            case FND:
                final List<Quote> quotes = new ArrayList<Quote>();
                for (final Quote quote : instrument.getQuotes()) {
                    if (FUND_MARKETS.contains(quote.getSymbolVwdfeedMarket())) {
                        quotes.add(quote);
                    }
                }
                return quotes.isEmpty() ? null : quotes;
            case IND:
                return instrument.getQuotes();
        }
        return null;
    }

    private void exportTimeseries(Quote q) {
        if (q.getSymbolVwdcode() == null || q.getSymbolMmwkn() == null) {
            return;
        }

        final HistoricTimeseriesRequest tsr = new HistoricTimeseriesRequest(q, this.firstDate, new LocalDate());
        tsr.addMmTalk("if(is[\"Fonds\"];Rücknahme[_;true;true;30];Close[_;true;true;30])");
        if (q.getInstrument().getInstrumentType() == InstrumentTypeEnum.FND) {
            tsr.addMmTalk("$p:=if(is[\"Fonds\"];Rücknahme[_;true;true;30];Close[_;true;true;30]);($p/$p.Before[3]-1)*100");
        }

        final List<HistoricTimeseries> hts = this.historicTimeseriesProvider.getTimeseries(tsr);

        dumpDataPF(q, hts.get(0));
        if (q.getInstrument().getInstrumentType() == InstrumentTypeEnum.FND) {
            dumpData3M(q, hts.get(1));
        }
    }

    /**
     * dump timeseries where each value is the 3-mon performance
     * @param q quote to dump
     * @param timeseries timeseries to dump
     */
    private void dumpData3M(Quote q, HistoricTimeseries timeseries) {
        if (timeseries == null) {
            return;
        }
        final StringBuilder sb = new StringBuilder(100);
        sb.append(q.getSymbolVwdcode()).append(";3m-bvi");

        LocalDate day = timeseries.getStartDay();
        for (int i = 0; i < timeseries.size(); i++) {
            final double value = timeseries.getValue(i);
            final String date = DTF.print(day);
            day = day.plusDays(1);

            if (Double.isNaN(value)) {
                continue;
            }
            sb.append(";").append(date).append(":").append(PF.format(value));
        }

        this.pw.println(sb.toString());
    }

    /**
     * dump timeseries where each value is the normalized bvi performance
     * @param q quote to dump
     * @param timeseries timeseries to dump, each value has to be normalized
     */
    private void dumpDataPF(Quote q, HistoricTimeseries timeseries) {
        if (timeseries == null) {
            return;
        }

        final double startvalue = getStartvalue(timeseries);

        if (Double.isNaN(startvalue)) {
            return;
        }

        final StringBuilder sb = new StringBuilder(100);
        sb.append(q.getSymbolVwdcode()).append(";nbvi");

        LocalDate day = timeseries.getStartDay();
        for (int i = 0; i < timeseries.size(); i++) {
            final double value = timeseries.getValue(i);
            final String date = DTF.print(day);
            day = day.plusDays(1);

            if (Double.isNaN(value)) {
                continue;
            }
            sb.append(";").append(date).append(":").append(PF.format(value / startvalue));
        }

        this.pw.println(sb.toString());
    }

    private double getStartvalue(HistoricTimeseries timeseries) {
        for (int i = 0; i < timeseries.size(); i++) {
            final double value = timeseries.getValue(i);
            if (!Double.isNaN(value)) {
                return value;
            }
        }
        return Double.NaN;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent evt) {
        try {
            export();
        } catch (Exception e) {
            this.logger.error("<onApplicationEvent> ", e);
        }
        this.appCtx.close();
    }
}
