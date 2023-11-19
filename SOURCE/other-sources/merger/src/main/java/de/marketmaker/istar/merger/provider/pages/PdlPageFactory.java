/*
 * PdlPageFactory.java
 *
 * Created on 16.06.2005 13:59:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.TimeTaker;

/**
 * Creates PdlPage objects from textual page descriptions. Since instances or this class
 * are immutable, and all fields are either immutable or thread-safe, those instances
 * are also thread-safe.
 * 
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
class PdlPageFactory {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Pattern is thread-safe, a Matcher would not be thread-safe
    private final Pattern versionPattern = Pattern.compile("<PDL V=([^>]*?)>");

    private final Pattern numberPattern = Pattern.compile("<PN>(\\d+?)(\\.\\w+)?</PN>");

    private final Pattern titlePattern = Pattern.compile("<PT>(.*?)</PT>");

    private final Pattern widthAndHeightPattern = Pattern.compile("<PB W=(\\d+?) H=(\\d+?)>");

    private final Pattern objectPattern = Pattern.compile("<OB ([^>]+)>([\\s\\S]*?)</OB>");

    private final Pattern attributePattern =
            Pattern.compile("O=([DPT]) X=(\\d+?) Y=(\\d+?) W=(\\d+?) H=(\\d+?) DW=(\\d+?) DH=(\\d+?) A=(\\d+?)$");

    private static final PdlObject[] NO_PDL_OBJECT = new PdlObject[0];

    public PdlPage createPage(String id, String rawText) {
        final TimeTaker tt = this.logger.isDebugEnabled() ? new TimeTaker() : null;

        try {
            String title = getPageTitle(rawText);
            String number = getPageNumber(rawText);

            // Sometimes title and number are the other way round
            if (title.trim().matches("\\d+")) {
                String tmp = title;
                title = number;
                number = tmp;
            }

            int width = 0;
            int height = 0;
            final Matcher whMatcher = this.widthAndHeightPattern.matcher(rawText);
            if (whMatcher.find()) {
                width = Integer.parseInt(whMatcher.group(1));
                height = Integer.parseInt(whMatcher.group(2));
            }

            final List<PdlObject> objects = parsePdlObjects(rawText, number, width, height);

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<parse> took " + tt);
            }

            return new PdlPage(getVersion(rawText), number, title, width, height, objects);
        } catch (Exception e) {
            this.logger.warn("<parse> failed for page " + id, e);
            return null;
        }
    }

    private List<PdlObject> parsePdlObjects(String rawText, String number, int width, int height) {
        final List<PdlObject> result = new ArrayList<>(100);

        final Matcher objectMatcher = this.objectPattern.matcher(rawText);
        int n = 0;
        while (objectMatcher.find()) {
            final PdlObject[] pdls = parseObjects(objectMatcher.group(1), objectMatcher.group(2));
            for (PdlObject po : pdls) {
                if (po.getX() > width || po.getY() > height) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("<addPageObject> on page " + number
                                + " " + width + "x" + height
                                + ", ignoring out-of-bounds " + po);
                    }
                    continue;
                }
                po.setId(n++);
                result.add(po);
            }
        }
        return result;
    }

    private PdlObject[] parseObjects(String attributes, String content) {
        Matcher attrMatcher = this.attributePattern.matcher(attributes);
        if (!attrMatcher.matches()) {
            return NO_PDL_OBJECT;
        }

        String type = attrMatcher.group(1);
        int x = Integer.parseInt(attrMatcher.group(2));
        int y = Integer.parseInt(attrMatcher.group(3));
        int width = Integer.parseInt(attrMatcher.group(4));
        int height = Integer.parseInt(attrMatcher.group(5));
        int displayWidth = Integer.parseInt(attrMatcher.group(6));
        int displayHeight = Integer.parseInt(attrMatcher.group(7));
        int attribute = Integer.parseInt(attrMatcher.group(8));

        // if necessary, split into lines, treat each line as separate PdlObject
        final String[] lines = (content.indexOf("\\n") == -1)
                ? new String[]{content}
                : content.split("\\\\n[\\s*]");

        final PdlObject[] result = new PdlObject[lines.length];
        for (int i = 0; i < lines.length; i++) {
            result[i] = PdlObjectFactory.createPdlObject(type, x, y + i, width, height,
                    displayWidth, displayHeight, attribute, lines[i]);
        }
        return result;
    }

    private String getVersion(String page) {
        return getMatch(page, this.versionPattern);
    }

    private String getPageNumber(String page) {
        return getMatch(page, this.numberPattern);
    }

    private String getPageTitle(String page) {
        return getMatch(page, this.titlePattern);
    }

    private String getMatch(String page, Pattern p) {
        final Matcher matcher = p.matcher(page);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "n/a";
    }
}
