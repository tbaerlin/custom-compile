/*
 * ConvensysDownloadsReader.java
 *
 * Created on 07.10.2010 10:08:55
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.convensys;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.data.DownloadableItem;
import de.marketmaker.istar.domainimpl.data.DownloadableItemImpl;

/**
 * @author oflege
 */
class DownloadsReader extends IstarMdpExportReader<Map<Long, List<DownloadableItem>>> {
    private static final Logger logger = LoggerFactory.getLogger(DownloadsReader.class);

    private static final Comparator<DownloadableItem> DOWNLOAD_COMPARATOR = new Comparator<DownloadableItem>() {
        public int compare(DownloadableItem o1, DownloadableItem o2) {
            final int byDate = o2.getYear() - o1.getYear();
            if (byDate != 0) {
                return byDate;
            }
            return o1.getDescription().compareTo(o2.getDescription());
        }
    };

    private final Map<Long, List<DownloadableItem>> result
            = new HashMap<>();

    private int numDocs = 0;

    DownloadsReader() {
        super("DESCRIPTION");
    }

    protected void handleRow() {
        final Long iid = getLong("IID");
        final Integer year = getInt("YEAR");
        final String description = get("DESCRIPTION");
        final String filename = get("FILENAME");

        if (iid == null || year == null
                || "-".equals(filename) || !StringUtils.hasText(filename)
                || !StringUtils.hasText(description)) {
            return;
        }

        final DownloadableItemImpl di
                = new DownloadableItemImpl(year, description, filename, null, null);

        List<DownloadableItem> items = result.get(iid);
        if (items == null) {
            items = new ArrayList<>();
            this.result.put(iid, items);
        }
        items.add(di);
        this.numDocs++;
    }

    protected Map<Long, List<DownloadableItem>> getResult() {
        return this.result;
    }

    protected void endDocument() {
        for (List<DownloadableItem> items : this.result.values()) {
            items.sort(DOWNLOAD_COMPARATOR);
        }
        logger.info("<endDocument> read " + numDocs + " docs for " + result.size() + " iids");
    }

    public static void main(String[] args) throws Exception {
        new DownloadsReader().read(new File("d:/produktion/var/data/provider/istar-convensys-download.xml.gz"));
    }
}
