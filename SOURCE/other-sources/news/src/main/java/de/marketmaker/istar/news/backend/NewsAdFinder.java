/*
 * NewsAdBlocker.java
 *
 * Created on 10.07.2008 15:24:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.news.data.NewsRecordImpl;

/**
 * Finds ads in text of NewsRecordImpl objects and stores the position(s) of the ad(s) in that record
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class NewsAdFinder implements NewsRecordHandler, InitializingBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile List<Pattern> patterns = Collections.emptyList();

    private Resource patternSource;

    public void setPatternSource(Resource patternSource) {
        this.patternSource = patternSource;
    }

    public void afterPropertiesSet() throws Exception {
        readAdPatterns();
    }

    @ManagedOperation
    public void readAdPatterns() {
        List<Pattern> tmp = new ArrayList<>();
        Scanner s = null;
        try {
            s = new Scanner(this.patternSource.getInputStream());
            int n = 0;
            while (s.hasNextLine()) {
                n++;
                final String line = s.nextLine();
                if (!StringUtils.hasText(line) || line.startsWith("#")) {
                    continue;
                }
                try {
                    final Pattern p = Pattern.compile(line, Pattern.DOTALL);
                    tmp.add(p);
                } catch (PatternSyntaxException e) {
                    this.logger.warn("<afterPropertiesSet> ignoring invalid pattern in line " + n);
                }
            }
            if (!tmp.isEmpty()) {
                this.patterns = tmp;
                this.logger.info("<afterPropertiesSet> read " + tmp.size() + " ad patterns");
            }
            else {
                this.logger.warn("<readAdPatterns> no valid patterns in " + this.patternSource);
            }
        } catch (Exception e) {
            this.logger.error("<readAdPatterns> failed", e);
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }

    public void handle(NewsRecordImpl newsRecord) {
        findAds(newsRecord);
    }

    void findAds(NewsRecordImpl newsRecord) {
        if (newsRecord == null) {
            return;
        }
        final String s = newsRecord.getText();
        if (s == null) {
            return;
        }
        int[][] adPositions = null;
        int numFound = 0;
        for (Pattern pattern : this.patterns) {
            final Matcher m = pattern.matcher(s);
            while (m.find()) {
                if (adPositions == null) {
                    adPositions = new int[this.patterns.size()][];
                }
                if (m.groupCount() > 0) {
                    adPositions[numFound++] = new int[] { m.start(1), m.end(1)};
                }
                else {
                    adPositions[numFound++] = new int[] { m.start(), m.end()};
                }
            }
        }
        if (numFound > 0) {
            newsRecord.setAdPositions(Arrays.copyOf(adPositions, numFound));
        }
    }
}
