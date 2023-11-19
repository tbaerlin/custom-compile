/*
 * QuoteVolumeProvider.java
 *
 * Created on 02.09.2009 14:12:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class QuoteVolumeOrderProvider implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Long, Integer> orderByQid = new HashMap<>();

    private File file;

    public void setFile(File file) {
        this.file = file;
    }

    public void afterPropertiesSet() throws Exception {
        readItems();
    }

    public Integer getQuoteOrder(Quote q) {
        return this.orderByQid.get(q.getId());
    }

    private void readItems() throws Exception {
        final ItemsReader ir = new ItemsReader();
        ir.read(this.file);
        this.logger.info("<readItems> read " + this.orderByQid.size() + " quoteids");
    }

    private class ItemsReader extends AbstractSaxReader {
        private int n = 0;

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if (tagName.equals("QUOTE")) {
                    orderByQid.put(Long.parseLong(getCurrentString(false)), ++n);
                }
                else {
                    notParsed(tagName);
                }
            }
            catch (Exception e) {
                this.logger.error("<endElement> error in " + tagName, e);
                this.errorOccured = true;
            }
        }

        protected void reset() {
        }
    }
}
