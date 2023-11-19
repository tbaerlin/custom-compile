/*
 * FinderQueryParserSupport.java
 *
 * Created on 09.12.11 16:13
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.finder.Query2Term;
import de.marketmaker.istar.merger.web.finder.Term;
import de.marketmaker.istar.merger.web.finder.Terms;
import de.marketmaker.istar.ratios.RatioFieldDescription;

import static de.marketmaker.istar.ratios.frontend.SearchParameterParser.*;

/**
 * @author oflege
 */
public class FinderQueryParserSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinderQueryParserSupport.class);

    private static final Pattern ALREADY_QUOTED_COMPARISON = Pattern.compile(".*(=~|==?|!=|>=|<=|>|<)\\s*'[^']*'.*");

    private static final Pattern QUERY_IN = Pattern.compile(".*\\w+\\s*IN\\s*\\([^\\)]*\\)");

    private Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields;

    FinderQueryParserSupport(Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        this.fields = fields;
    }

    Map<String, String> parseQuery(String query) {
        if (!StringUtils.hasText(query)) {
            return new HashMap<>();
        }
        try {
            return doParseQuery(query);
        } catch (RecognitionException | RuntimeException ex1) {
            // RuntimeException is triggered by emitErrorMessage in @lexer::members in FinderQuery.g
            try {
                return doParseQuery(ensureQuotedValues(query));
            } catch (RecognitionException | RuntimeException ex2) {
                throw new BadRequestException("cannot parse '" + query + "'"
                      //  + " : "  + ex1.getMessage()
                        + " : "  + ex2.getMessage()
                );
            }
        }
    }

    /**
     * returns a list of 'fieldname-relation' to 'fieldvalue' mappings
     *
     * @param query the search expression
     * @return a map with relations of the parse-tree's first level
     * @throws RecognitionException
     */
    private Map<String, String> doParseQuery(String query) throws RecognitionException {
        final Map<String, String> result = new HashMap<>();

        final Term queryTerm = Query2Term.toTerm(query);

        final List<Term> terms;
        if (queryTerm instanceof Terms.AndOp) {
            terms = ((Terms.AndOp) queryTerm).getTerms();
        } else if (queryTerm instanceof Terms.OrOp) {
            throw new BadRequestException("OR expression is not supported: '" + query + "'");
        } else {
            terms = Collections.singletonList(queryTerm);
        }

        for (final Term term : terms) {
            if (!(term instanceof Terms.Relation)) {
                throw new BadRequestException("term is not a relation: '" + term + "'");
            }
            final Terms.Relation relation = (Terms.Relation) term;
            final String fieldname = relation.getIdentifier();
            final String value = relation.getValue();


            if (fieldname.indexOf("@") > 0) {
                final String[] strings = fieldname.split("@");
                final StringBuilder sb = new StringBuilder(fieldname.length());
                for (final String string : strings) {
                    final RatioFieldDescription.Field field = getRatiofield(string);
                    if (null == field) {
                        LOGGER.warn("<parseQuery> no matching ratio field description: " + string);
                        continue;
                    }
                    sb.append("@").append(field.name());
                }
                result.put(sb.toString().substring(1), value);
                continue;
            }

            final RatioFieldDescription.Field ratiofield = getRatiofield(fieldname);

            if (ratiofield == null) {
                if (relation.getOp() == Terms.Relation.Op.EQ) {
                    // TODO: add fields to permissioned fields
                    LOGGER.info("<parseQuery> no ratiofield, adding term: " + relation);
                    result.put(fieldname, value);
                    continue;
                }
                else {
                    LOGGER.warn("<parseQuery> no ratiofield for term: " + relation);
                    throw new BadRequestException("No ratiofield: " + fieldname);
                }
            }

            final String rtField = ratiofield.name().toLowerCase();

            // check for range query
            if (value != null
                    && value.contains("_")
                    && (relation.getOp() == Terms.Relation.Op.EQ || relation.getOp() == Terms.Relation.Op.NEQ)
                    && (ratiofield.type() == RatioFieldDescription.Type.DATE
                            || ratiofield.type() == RatioFieldDescription.Type.DECIMAL
                            || ratiofield.type() == RatioFieldDescription.Type.NUMBER)) {
                result.put(rtField + SUFFIX_RANGE, value);
                continue;
            }

            switch (relation.getOp()) {
                case EQ:
                    result.put(rtField, value);
                    break;
                case NEQ:
                    result.put(rtField, value);
                    result.put(rtField + SUFFIX_INOUT, "false");
                    break;
                case GT:
                    result.put(rtField + SUFFIX_LOWER, value);
                    result.put(rtField + SUFFIX_LOWER_INCLUSIVE, "false");
                    break;
                case GTE:
                    result.put(rtField + SUFFIX_LOWER, value);
                    break;
                case LT:
                    result.put(rtField + SUFFIX_UPPER, value);
                    result.put(rtField + SUFFIX_UPPER_INCLUSIVE, "false");
                    break;
                case LTE:
                    result.put(rtField + SUFFIX_UPPER, value);
                    break;
                default:
                    throw new BadRequestException("unsupported relation " + relation.getOp() + " in '" + term + "'");
            }
        }
        return result;
    }

    /**
     * TODO: make this method private and don't use it in external code.
     * This method can only handle the most simple queries and may return query strings that are
     * plain wrong in case more complex queries are used as parameters.
     * @param query Query to be quoted
     * @return Quoted query
     */
    public static String ensureQuotedValues(String query) {
        if (!StringUtils.hasText(query)) {
            return query;
        }

        // already quoted
        if (ALREADY_QUOTED_COMPARISON.matcher(query).matches() || QUERY_IN.matcher(query).matches()) {
            return query;
        }

        // the resulting map cannot describe 'or' terms, so it is sufficient to handle 'and'
        final String[] terms = query.split("(&&|\\s+and\\s+)");
        StringBuilder sb = new StringBuilder(query.length() * 2);
        for (final String term : terms) {
            final String[] tokens = term.split("==?|!=|>=|<=|>|<");
            if (tokens.length != 2) {
                return query;
            }
            final String fieldname = tokens[0];
            final String value = tokens[1];
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append(fieldname.trim());
            sb.append(term.substring(fieldname.length(), term.length() - value.length()).trim());
            final String s = value.trim();
            sb.append("'");
            for (int i = 0; i < s.length(); i++) {
                sb.append(s.charAt(i));
                if (s.charAt(i) == '\'') {
                    sb.append("'");
                }
            }
            sb.append("'");
        }
        String result = sb.toString();
        if (!result.equals(query) && LOGGER.isDebugEnabled()) {
            LOGGER.debug("<ensureQuotedValues> [" + query + "] => [" + result + "]");
        }
        return result;
    }

    private RatioFieldDescription.Field getRatiofield(String fieldname) {
        try {
            return fields.get(RatioDataRecord.Field.valueOf(fieldname));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("unknown field: " + fieldname);
        }
    }

    public static void main(String[] args) {
        FinderQueryParserSupport fqp = new FinderQueryParserSupport(RatioFieldDescription.FIELDNAMES.get(InstrumentTypeEnum.STK));
//        System.out.println(fqp.parseQuery("marketCapitalization>=20 and marketCapitalization<=200"));
//        System.out.println(fqp.parseQuery("name==foo and marketCapitalization<=200"));
        System.out.println(fqp.parseQuery("bisKey==1_846900_"));
//        System.out.println(fqp.parseQuery("marketCapitalization>=20 and 23"));
    }
}
