/*
 * TopicBuilder.java
 *
 * Created on 14.08.2008 07:49:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.feed.DateTimeProviderImpl;
import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;

/**
 * Creates a set of topic identifiers for a given NewsRecord based on a set of rules
 * read from a configured resource.<p>
 * Since rules are evaluated at index time to populate the "topic" field and also at news retrieval
 * time to set the topic field in NewsRecordImpl objects, it does not make sense to reload
 * topic rules at runtime. Whenever these rules change, it will be necessary to reindex all
 * news to reflect the new rules in the index.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TopicBuilder implements InitializingBean, NewsRecordHandler {
    /**
     * Each rule implements this interface
     */
    static interface TopicRule {
        boolean appliesTo(NewsRecordImpl nr);

        String getTopic();
    }

    /**
     * A set of rules that all must evaluate to true in order for this rule to be true
     */
    private static class AndRule implements TopicRule {
        private final List<TopicRule> terms;

        private AndRule(List<TopicRule> terms) {
            this.terms = terms;
        }

        public boolean appliesTo(NewsRecordImpl nr) {
            for (TopicRule term : terms) {
                if (!term.appliesTo(nr)) return false;
            }
            return true;
        }

        public String getTopic() {
            return this.terms.get(0).getTopic();
        }
    }

    /**
     * A rule that checks whether any of its specified values are contained in the values of
     * a specific enum attribute of a news record
     */
    private static class EnumRule implements TopicRule {
        enum Occur {MUST, SHOULD, MUST_NOT}

        final NewsAttributeEnum field;

        final Set<String> values;

        final String topic;

        final Occur occur;

        private EnumRule(String topic, NewsAttributeEnum field, Set<String> values, Occur occur) {
            this.topic = topic;
            this.field = field;
            this.values = values;
            this.occur = occur;
        }

        public boolean appliesTo(NewsRecordImpl nr) {
            final Set<String> values = (this.field == NewsAttributeEnum.SELECTOR)
                    ? nr.getNumericSelectors()
                    : nr.getAttributes(this.field);
            switch (this.occur) {
                case MUST:
                    return values != null && values.containsAll(this.values);
                case SHOULD:
                    return values != null && !Collections.disjoint(values, this.values);
                case MUST_NOT:
                    return values == null || Collections.disjoint(values, this.values);
                default:
                    return false;
            }
        }

        public String getTopic() {
            return this.topic;
        }
    }

    /**
     * A rule that checks whether a certain field in a news records exactly matches a given string
     */
    private static class SimpleRule implements TopicRule {
        final int field;

        final Set<String> requiredValues;

        final String topic;

        private SimpleRule(String topic, int field, Set<String> requiredValues) {
            this.topic = topic;
            this.field = field;
            this.requiredValues = requiredValues;
        }

        public boolean appliesTo(NewsRecordImpl nr) {
            final SnapField sf = nr.getField(this.field);
            return sf != null && requiredValues.contains(String.valueOf(sf.getValue()));
        }

        public String getTopic() {
            return this.topic;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, List<TopicRule>> rules = new HashMap<>();

    private Resource rulesSource;

    public void afterPropertiesSet() throws Exception {
        final Matcher m = Pattern.compile("^(\\w+)\\.(.+?)=(.*)$").matcher("");
        final Matcher sm = Pattern.compile("^selector:(\\w+)$").matcher("");

        int errorCount = 0;
        try (Scanner s = new Scanner(this.rulesSource.getInputStream())) {
            int n = 0;
            while (s.hasNextLine()) {
                n++;
                final String line = s.nextLine();
                if (!StringUtils.hasText(line) || line.startsWith("#")) {
                    continue;
                }
                m.reset(line);
                if (!m.find()) {
                    this.logger.error("<afterPropertiesSet> invalid line " + n + " in rules: '" + line + "'");
                    errorCount++;
                    continue;
                }

                String topic = m.group(1);
                String agency = m.group(2);
                String expr = m.group(3);

                sm.reset(expr);
                if (sm.matches() && topicMatchesSelector(topic, sm.group(1))) {
                    continue; // each selector will be mapped to a topic anyway, so we can ignore that
                }

                List<TopicRule> rules = this.rules.get(agency);
                if (rules == null) {
                    rules = new ArrayList<>();
                    this.rules.put(agency, rules);
                }

                try {
                    rules.add(createRule(topic, expr));
                } catch (Exception e) {
                    this.logger.error("<afterPropertiesSet> failed to create rule for line "
                            + n + " '" + line + "': " + e.getMessage());
                    errorCount++;
                }
            }
        }
        if (errorCount > 0) {
            throw new IllegalArgumentException(errorCount + " errors in " + this.rulesSource);
        }
    }

    private boolean topicMatchesSelector(String topic, String selector) {
        try {
            return EntitlementsVwd.normalize(selector).equals(topic);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void setRulesSource(Resource rulesSource) {
        this.rulesSource = rulesSource;
    }


    public void handle(NewsRecordImpl newsRecord) {
        newsRecord.setTopics(getTopics(newsRecord));
    }

    /**
     * Returns the meta topics associated with a news record (does not include the "native"
     * topics that are based on the selectors)
     * @param nr news record
     * @return topics, may be an empty set
     */
    Set<String> getTopics(NewsRecordImpl nr) {
        final List<TopicRule> rules = this.rules.get(nr.getAgency());
        if (rules == null) {
            return Collections.emptySet();
        }
        Set<String> result = null;
        for (TopicRule rule : rules) {
            if (rule.appliesTo(nr)) {
                if (result == null) {
                    result = new HashSet<>();
                }
                result.add(rule.getTopic());
            }
        }
        return (result == null) ? Collections.<String>emptySet() : result;
    }

    private TopicRule createRule(String topic, String expr) {
        final String[] ands = expr.split("\\s*&&\\s*");
        if (ands.length > 1) {
            final List<TopicRule> rules = new ArrayList<>(ands.length);
            for (String and : ands) {
                rules.add(createRule(topic, and));
            }
            return new AndRule(rules);
        }
        final String[] tokens = expr.split(":");
        final VwdFieldDescription.Field field = getField(tokens[0]);
        if (field == null) {
            throw new IllegalArgumentException("no such field: '" + tokens[0] + "'");
        }
        final NewsAttributeEnum attributeEnum = getEnum(field);

        if (attributeEnum != null) {
            String[][] valueSets = toSets(tokens[1]);
            if (attributeEnum == NewsAttributeEnum.SELECTOR) {
                normalizeSelectors(valueSets);
            }
            return create(topic, attributeEnum, valueSets[0], valueSets[1], valueSets[2]);
        }
        else {
            return new SimpleRule(topic, field.id(), toSet(tokens[1]));
        }
    }

    private TopicRule create(String topic, NewsAttributeEnum attributeEnum,
            String[] must, String[] should, String[] mustNot) {
        List<TopicRule> rules = new ArrayList<>();
        if (must.length > 0) {
            rules.add(new EnumRule(topic, attributeEnum, new HashSet<>(Arrays.asList(must)), EnumRule.Occur.MUST));
        }
        if (should.length > 0 && must.length == 0) {
            rules.add(new EnumRule(topic, attributeEnum, new HashSet<>(Arrays.asList(should)), EnumRule.Occur.SHOULD));
        }
        if (mustNot.length > 0) {
            rules.add(new EnumRule(topic, attributeEnum, new HashSet<>(Arrays.asList(mustNot)), EnumRule.Occur.MUST_NOT));
        }

        return rules.size() > 1 ? new AndRule(rules) : rules.get(0);
    }

    private VwdFieldDescription.Field getField(String token) {
        if ("selector".equals(token)) {
            return VwdFieldDescription.NDB_Selectors;
        }
        return VwdFieldDescription.getFieldByName(token);
    }

    private NewsAttributeEnum getEnum(VwdFieldDescription.Field field) {
        for (NewsAttributeEnum anEnum : NewsAttributeEnum.values()) {
            if (anEnum.getField() == field) {
                return anEnum;
            }
        }
        return null;
    }

    private void normalizeSelectors(String[][] values) {
        for (String[] value : values) {
            for (int j = 0; j < value.length; j++) {
                value[j] = EntitlementsVwd.toNumericSelector(value[j]);
            }
        }
    }

    private Set<String> toSet(String token) {
        if (token.startsWith("(") && token.endsWith(")")) {
            final String[] tokens = token.substring(1, token.length() - 1).split("\\s+");
            return new HashSet<>(Arrays.asList(tokens));
        }
        return Collections.singleton(token.trim());
    }

    private String[][] toSets(String token) {
        final String[] tokens;
        if (token.startsWith("(") && token.endsWith(")")) {
            tokens = token.substring(1, token.length() - 1).split("\\s+");
        }
        else {
            tokens = new String[]{token};
        }

        List<String> must = new ArrayList<>();
        List<String> should = new ArrayList<>();
        List<String> mustNot = new ArrayList<>();

        for (String s : tokens) {
            if (s.startsWith("+")) {
                must.add(s.substring(1));
            }
            else if (s.startsWith("-")) {
                mustNot.add(s.substring(1));
            }
            else {
                should.add(s);
            }
        }

        return new String[][]{
                must.toArray(new String[must.size()]),
                should.toArray(new String[should.size()]),
                mustNot.toArray(new String[mustNot.size()])
        };
    }

    private static NewsRecordImpl getRecord() throws Exception {
        NewsRecordBuilder nrb = new NewsRecordBuilder();
        final ParsedRecord pr = new ParsedRecord(DateTimeProviderImpl.INSTANCE);
        pr.reset(new FeedRecord("dpa-AFXFOO,BARHEADLINEGER".getBytes(), 0, 25));
        pr.setField(VwdFieldDescription.NDB_ID_News_Agency.id(), 0, 7);
        pr.setField(VwdFieldDescription.NDB_Genre.id(), 7, 7);
        pr.setField(VwdFieldDescription.NDB_Headline.id(), 14, 8);
        pr.setField(VwdFieldDescription.NDB_Country.id(), 22, 3);
        return nrb.createNewsRecord(pr);
    }



    public static void main(String[] args) throws Exception {
        TopicBuilder tb = new TopicBuilder();
        final String path = "d:/produktion/var/data/topicRules.txt";
        tb.setRulesSource(new FileSystemResource(path));
        tb.afterPropertiesSet();
        System.out.println("read rules from " + path);

        NewsRecordImpl newsRecord = getRecord();
        final Set<String> topics = tb.getTopics(newsRecord);
        System.out.println("topics = " + topics);

    }
}
