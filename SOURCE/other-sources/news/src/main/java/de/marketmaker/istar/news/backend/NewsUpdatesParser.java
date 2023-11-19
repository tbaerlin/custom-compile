/*
 * NewsUpdatesParser.java
 *
 * Created on 30.03.2007 12:43:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a file line by line and extracts updates for news records. An update is defined as
 * <pre>
 * <em>fieldname</em>=[oldValue1:newValue1(,oldValueX:newValueX)*]
 * </pre>
 * where fieldname is the name of a vwd feed field (e.g., NDB_IsinList) that is used
 * in {@link de.marketmaker.istar.news.frontend.NewsAttributeEnum} and the mapping of old
 * to new values is defined in groovy map literal syntax.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class NewsUpdatesParser {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final File file;

    static final Pattern UPDATE_WITH_MAP
            = Pattern.compile("(\\w+)=\\[(\\w+:\\w+(?:,\\w+:\\w+)*)]");

    public NewsUpdatesParser(File file) {
        this.file = file;
    }

    List<NewsRecordUpdate> getUpdates() {
        final List<NewsRecordUpdate> result = parseUpdates();
        if (!result.isEmpty()) {
            //noinspection ResultOfMethodCallIgnored
            this.file.renameTo(getNameForProcessedFile());
        }
        return result;
    }

    private List<NewsRecordUpdate> parseUpdates() {
        if (this.file == null || !this.file.canRead()) {
            return Collections.emptyList();
        }

        try (Scanner s = new Scanner(this.file)) {
            return parseUpdates(s);
        } catch (IOException e) {
            this.logger.error("<getUpdates> failed to parse", e);
            return Collections.emptyList();
        }
    }

    private File getNameForProcessedFile() {
        return new File(this.file.getParentFile(), this.file.getName()
            + DateTimeFormat.forPattern("_yyyy-MM-dd_HH-mm-ss").print(new DateTime()));
    }

    private List<NewsRecordUpdate> parseUpdates(Scanner s) {
        final List<NewsRecordUpdate> result = new ArrayList<>();

        while (s.hasNextLine()) {
            final String line = s.nextLine();
            if (!StringUtils.hasText(line) || line.startsWith("#")) {
                continue;
            }

            final Matcher m = UPDATE_WITH_MAP.matcher(line.replaceAll("\\s", ""));
            if (m.matches()) {
                VwdFieldDescription.Field f = VwdFieldDescription.getFieldByName(m.group(1));
                if (f == null) {
                    throw new IllegalArgumentException("Unknown field: " + m.group(1));
                }
                Map<String, String> mappings = new HashMap<>();
                for (String mapping : m.group(2).split(",")) {
                    int p = mapping.indexOf(':');
                    mappings.put(mapping.substring(0, p), mapping.substring(p + 1));
                }
                result.add(new NewsRecordUpdate(f, mappings));
            }
            else {
                this.logger.warn("<parseUpdates> invalid line: '" + line + "'");
            }
        }

        this.logger.info("<parseUpdates> parsed " + result);
        return result;
    }

    public static void main(String[] args) {
        final List<NewsRecordUpdate> updates = new NewsUpdatesParser(new File("/Users/oflege/tmp/updates.txt")).getUpdates();
        System.out.println(updates.size());
    }
}
