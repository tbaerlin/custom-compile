/*
 * StkScreenerMetadata.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jcip.annotations.GuardedBy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.ByteUtil;

/**
 * Returns the <i>Four Stars</i> upside rating images and the trend direction images used within responses from <i>theScreener</i> related blocks (e.g. {@see STK_ScreenerData}, {@see STK_ScreenerInterest}).
 * <p>The raw bytes of the images are Base64 encoded.</p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkScreenerMetadata implements AtomController,  InitializingBean {
    private File imageDir;
    @GuardedBy("images")
    private final Map<String, String> images = new TreeMap<>();

    public void setImageDir(File imageDir) {
        this.imageDir = imageDir;
    }

    public void afterPropertiesSet() throws Exception {
        final File[] files = this.imageDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".gif");
            }
        });

        for (final File file : files) {
            final byte[] bytes = FileCopyUtils.copyToByteArray(file);
            final String encodedImage = ByteUtil.toBase64String(bytes);
            synchronized (this.images) {
                this.images.put(file.getName(), encodedImage);
            }
        }
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final Map<String, Object> model = new HashMap<>();
        synchronized (this.images) {
            model.put("images", this.images);
        }
        return new ModelAndView("stkscreenermetadata", model);
    }
}