/*
 * Controller.java
 *
 * Created on 16.09.2005 15:22:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.longs.LongCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.lifecycle.Initializable;
import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.monitor.Resource;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Controller implements Initializable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private PriceProviderFile priceProviderFile;

    private CalcController calcController;

    private File priceFileBaseDir;

    private boolean useGzipFiles = true;

    private final List<InstrumentTypeEnum> types = new ArrayList<>();

    public Controller() {
    }

    public void setUseGzipFiles(boolean useGzipFiles) {
        this.useGzipFiles = useGzipFiles;
    }

    public void setTypes(String[] typesStr) {
        for (final String s : typesStr) {
            this.types.add(InstrumentTypeEnum.valueOf(s));
        }
        this.logger.info("<setTypes> types = " + this.types);
    }

    public void setCalcController(CalcController calcController) {
        this.calcController = calcController;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setPriceProviderFile(PriceProviderFile priceProviderFile) {
        this.priceProviderFile = priceProviderFile;
    }

    public void setPriceFileBaseDir(File priceFileBaseDir) {
        this.priceFileBaseDir = priceFileBaseDir;
    }

    public void initialize() throws Exception {
        for (final InstrumentTypeEnum type : this.types) {
            final Resource resource = new FileResource(getFile(type));

            this.activeMonitor.addResource(resource);

            resource.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    readFile(type);
                }
            });
        }
    }

    private File getFile(InstrumentTypeEnum type) {
        return new File(this.priceFileBaseDir, "ratio-prices-" + type.name().toLowerCase() + ".csv"
                + (this.useGzipFiles ? ".gz" : ""));
    }

    private void readFile(InstrumentTypeEnum type) {
        final File f = getFile(type);
        this.logger.info("<readFile> new price file, starting to read " + f.getName());

        final TimeTaker tt = new TimeTaker();
        final LongCollection changedQuoteids;
        try {
            changedQuoteids = this.priceProviderFile.read(f);
        } catch (Throwable t) {
            this.logger.error("<readFile> failed for " + f.getName(), t);
            return;
        }

        this.calcController.addQuoteidsToCalc(changedQuoteids);
        this.logger.info("<readFile> #changed for " + type.name() + ": " + changedQuoteids.size()
                + ", took: " + tt);
    }

    public void calcAll() {
        this.logger.info("<calcAll> triggerung calc of all qids");
        final LongCollection qids = this.priceProviderFile.getAllQuoteids();
        this.calcController.addQuoteidsToCalc(qids);
    }
}
