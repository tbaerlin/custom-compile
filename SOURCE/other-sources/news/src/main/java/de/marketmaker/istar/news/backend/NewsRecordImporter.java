/*
 * NewsRecordBuilder.java
 *
 * Created on 08.03.2007 16:00:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.news.data.NewsRecordImpl;

/**
 * Allows to import serialized NewsRecords from a file and forward them to some handler(s).
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class NewsRecordImporter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<NewsRecordHandler> handlers;

    private NewsSymbolIdentifier symbolIdentifier;

    public void setHandler(NewsRecordHandler handler) {
        setHandlers(Arrays.asList(handler));
    }

    public void setHandlers(List<NewsRecordHandler> handlers) {
        this.handlers = handlers;
    }

    public void setSymbolIdentifier(NewsSymbolIdentifier symbolIdentifier) {
        this.symbolIdentifier = symbolIdentifier;
    }

    @ManagedOperation(description = "import serialized NewsRecords from file")
    @ManagedOperationParameters({
            @ManagedOperationParameter(
                    name = "filename",
                    description = "name of file to import")})
    public void importNews(String filename) {
        final File f = new File(filename);
        if (!f.canRead()) {
            this.logger.warn("<importNews> no such file " + f.getAbsolutePath());
            return;
        }
        this.logger.info("<importNews> from " + f.getAbsolutePath() + "...");
        final TimeTaker tt = new TimeTaker(); 
        int numRead = 0;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(f));
            while (true) {
                final NewsRecordImpl nr = (NewsRecordImpl) ois.readObject();
                if (this.symbolIdentifier != null) {
                    this.symbolIdentifier.assignInstrumentsTo(nr);
                }
                if (nr.getInstruments() == null) {
                    nr.setInstruments(Collections.<Instrument>emptySet());
                }
                sendToHandlers(nr);
                ++numRead;
            }
        } catch (EOFException e) {
            // ignore
        } catch (Exception e) {
            this.logger.warn("<importNews> failed after importing " + numRead + " news", e);
        }
        finally {
            IoUtils.close(ois);
            tt.stop();
        }
        this.logger.info("<importNews> imported " + numRead + " news, took " + tt);
    }

    private void sendToHandlers(NewsRecordImpl newsRecord) {
        for (final NewsRecordHandler handler : this.handlers) {
            handler.handle(newsRecord);
        }
    }

    public static void main(String[] args) throws Exception {
        final NewsRecordImporter recordImporter = new NewsRecordImporter();

        recordImporter.setHandler(new NewsRecordHandler() {
            int i = 0;
            public void handle(NewsRecordImpl r) {
                System.out.println(++i + " " + r.getTimestamp() + " " + r.getHeadline());
            }
        });
        recordImporter.importNews("d:/temp/newsdump.bin");
    }
}
