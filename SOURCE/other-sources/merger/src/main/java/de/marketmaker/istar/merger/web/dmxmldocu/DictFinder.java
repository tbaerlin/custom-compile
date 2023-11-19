/*
 * FndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.dmxmldocu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;
import de.marketmaker.istar.merger.web.easytrade.block.AtomController;

/**
 * @author zzhao
 */
@MmInternal
public class DictFinder implements AtomController {

    private static final String DICT_XML_SUFFIX = "-dict.xml";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File dictDir;

    public void setDictDir(String dictPath) {
        this.dictDir = new File(dictPath);
        if (!this.dictDir.exists() || !this.dictDir.canRead() || !this.dictDir.isDirectory()) {
            throw new IllegalArgumentException("invalid zone dict folder: " + dictPath);
        }
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        final HashMap<String, Object> model = new HashMap<>(5, 1f);
        final Zone zone = (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);
        model.put("dict", getZoneDict(zone.getName()));
        return new ModelAndView("dictfinder", model);
    }

    private Object getZoneDict(String name) throws IOException {
        final File dictXml = new File(this.dictDir, name + DICT_XML_SUFFIX);
        if (!dictXml.exists() || !dictXml.canRead() || !dictXml.isFile()) {
            this.logger.error("<getZoneDict> no valid response dict for zone: " + name);
            throw new IllegalStateException("no valid response dict for zone: " + name);
        }

        return FileCopyUtils.copyToString(new BufferedReader(new FileReader(dictXml)));
    }
}
