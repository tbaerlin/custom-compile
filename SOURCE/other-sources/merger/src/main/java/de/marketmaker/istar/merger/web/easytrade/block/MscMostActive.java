/**
 * Created on 09.11.11 16:19
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 */

package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


/**
 * This block returns a list of the most clicked certificates from eniteo.de (b2c) and mmfweb (b2b)
 */

public class MscMostActive implements InitializingBean, AtomController {

    private static final String SEPARATOR = ";";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private File mostActiveFile;

    private final Object mutex = new Object();

    @GuardedBy("mutex")
    private Map<String, List<Instrument>> mostActives = Collections.emptyMap();

    private EasytradeInstrumentProvider instrumentProvider;

    public void setMostActiveFile(File mostActiveFile) {
        this.mostActiveFile = mostActiveFile;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    private void setMostActives(Map<String, List<Instrument>> mostActives) {
        synchronized (this.mutex) {
            this.mostActives = mostActives;
        }
    }

    private Map<String, List<Instrument>> getMostActives() {
        synchronized (this.mutex) {
            return new HashMap<>(this.mostActives);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.activeMonitor != null) {
            this.activeMonitor.addResource(createResource());
        }
        readFile();
    }

    private FileResource createResource() throws Exception {
        final FileResource result = new FileResource(this.mostActiveFile);
        result.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                readFile();
            }
        });
        return result;
    }

    private void readFile() {
        final Map<String, List<Instrument>> map = new HashMap<>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(this.mostActiveFile);
            while (scanner.hasNextLine()) {
                final List<String> line = Arrays.asList(scanner.nextLine().split(SEPARATOR));
                if (line.size() > 1) {
                    map.put(line.get(0), getInstruments(line.subList(1, line.size())));
                }
            }
            setMostActives(map);
            this.logger.info("<readFile> succeeded for " + this.mostActiveFile.getAbsolutePath());
        } catch (Exception e) {
            this.logger.error("<readFile> failed for " + this.mostActiveFile.getAbsolutePath(), e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private List<Instrument> getInstruments(List<String> symbols) {
        final List<Instrument> result = new ArrayList<>();
        for (String symbol : symbols) {
            try {
                result.add(getInstrument(symbol));
            } catch (UnknownSymbolException e) {
                this.logger.warn("<getInstruments> " + e.getMessage());
            }
        }
        return result;
    }

    private Instrument getInstrument(String symbol) {
        return this.instrumentProvider.identifyInstrument(symbol, SymbolStrategyEnum.AUTO);
    }

    private Quote getQuote(Instrument instrument) {
        try {
            return this.instrumentProvider.getQuote(instrument,
                    RequestContextHolder.getRequestContext().getMarketStrategy());
        } catch (Exception e) {
            this.logger.warn("<getQuote> no quote in " + instrument.getId() + ".iid: " + e.getMessage());
            return null;
        }
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        final Map<String, Object> model = new HashMap<>();

        final Map<String, List<Quote>> quotesByVendor = getQuotes();
        model.put("vendors", quotesByVendor.keySet());
        model.putAll(quotesByVendor);

        return new ModelAndView("mscmostactive", model);
    }

    private Map<String, List<Quote>> getQuotes() {
        final Map<String, List<Quote>> result = new HashMap<>();
        for (Map.Entry<String, List<Instrument>> entry : getMostActives().entrySet()) {
            result.put(entry.getKey(), getQuotes(entry.getValue()));
        }
        return result;
    }

    private List<Quote> getQuotes(List<Instrument> instrumentList) {
        final List<Quote> result = new ArrayList<>();
        for (Instrument instrument : instrumentList) {
            final Quote quote = getQuote(instrument);
            if (quote != null) {
                result.add(quote);
            }
        }
        return result;
    }
}
