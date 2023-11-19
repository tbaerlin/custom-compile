/*
 * FontManager.java
 *
 * Created on 10.08.2009 14:00:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;

/**
 * Helper class that registers fonts from a given directory so that these fonts
 * can be used by applications that use Graphics2D for rendering something (e.g., charts).
 * Currently only true type fonts are supported.
 * 
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class FontManager implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File fontDir = new File(System.getProperty("istar.home"), "fonts");

    public void setFontDir(File fontDir) {
        this.fontDir = fontDir;
    }

    public void afterPropertiesSet() throws Exception {
        registerFonts();
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "name", description = "name of font file")
    })
    public String registerFont(String name) {
        final File f = new File(this.fontDir, name);
        if (!f.canRead()) {
            return "No such file: " + f.getAbsolutePath();
        }
        final Font font = registerFont(f);
        if (font != null) {
            return "Registered " + font.getName();
        }
        return "Failed to register font";
    }

    private void registerFonts() {
        if (!this.fontDir.exists()) {
            throw new IllegalStateException("no such dir: " + this.fontDir.getAbsolutePath());
        }
        final File[] files = this.fontDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".ttf");
            }
        });
        for (File file : files) {
            registerFont(file);
        }
    }

    private Font registerFont(File file) {
        InputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            final Font font = Font.createFont(Font.TRUETYPE_FONT, bis);
            this.logger.info("<registerFont> " + font.getName());
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        }
        catch (Exception e) {
            this.logger.warn("<registerFont> failed for " + file.getAbsolutePath(), e);
            return null;
        }
        finally {
            IoUtils.close(bis);
        }
    }
}
