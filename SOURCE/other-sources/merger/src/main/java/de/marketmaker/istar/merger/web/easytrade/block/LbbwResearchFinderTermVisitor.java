/*
 * RscFinderTermVisitor.java
 *
 * Created on 05.04.12 09:16
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.joda.time.DateTime;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.finder.Term;
import de.marketmaker.istar.merger.web.finder.TermVisitor;
import de.marketmaker.istar.merger.web.finder.Terms;
import de.marketmaker.istar.news.analysis.NewsAnalyzer;

import static de.marketmaker.istar.merger.provider.gisresearch.GisResearchIndexConstants.FIELD_NAME;
import static de.marketmaker.istar.merger.provider.lbbwresearch.LbbwResearchIndexConstants.*;

/**
 * Creates a lucene query based on a finder query expression
 * @author oflege
 */
class LbbwResearchFinderTermVisitor implements TermVisitor {
    private static final LocalDate START_DATE = LocalDate.of(2000, 1, 1);

    private static final LocalDate END_DATE = LocalDate.of(2038, 1, 1);

    private static final Map<String, String> KEYWORD_QUERY_FIELDS =
            ImmutableMap.<String, String>builder()
                    .put("country", FIELD_COUNTRY)
                    .put("name", FIELD_NAME)
                    .put("isin", FIELD_ISIN)
                    .put("documentType", FIELD_DOCUMENT_TYPE)
                    .put("category", FIELD_CATEGORY)
                    .put("sector", FIELD_SECTOR)
                    .put("documentLanguage", FIELD_LANGUAGE)
                    .build();

    private final Deque<Query> queryStack = new LinkedList<>();

    private final LbbwResearchFinder finder;

    private final LbbwResearchFinder.Command cmd;

    public LbbwResearchFinderTermVisitor(LbbwResearchFinder finder,
            LbbwResearchFinder.Command cmd) {
        this.finder = finder;
        this.cmd = cmd;
    }

    @Override
    public void visit(Terms.AndOp term) {
        visitSubterms(term.getTerms(), BooleanClause.Occur.MUST);
    }

    @Override
    public void visit(Terms.OrOp term) {
        visitSubterms(term.getTerms(), BooleanClause.Occur.SHOULD);
    }

    @Override
    public void visit(Terms.NotOp term) {
        visitSubterms(Collections.singletonList(term.getTerm()), BooleanClause.Occur.MUST_NOT);
    }

    @Override
    public void visit(Terms.In term) {
        throw new UnsupportedOperationException();
    }

    public Query getResult() {
        return this.queryStack.pop();
    }

    @Override
    public void visit(Terms.Relation term) {
        this.queryStack.push(createQuery(term));
    }

    private Query createQuery(Terms.Relation rel) {
        String fieldname = rel.getIdentifier();
        String value = rel.getValue();

        if (KEYWORD_QUERY_FIELDS.containsKey(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            return createKeywordQuery(KEYWORD_QUERY_FIELDS.get(fieldname), value);
        }

        if ("text".equals(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            return getPrefixQuery(value, FIELD_TEXT);
        }
        if ("title".equals(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            return getPrefixQuery(value, FIELD_TITLE);
        }
        if ("symbol".equals(fieldname) || "wkn".equals(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            this.cmd.setSymbol(value);
            return this.finder.getSymbolQuery(this.cmd);
        }
        if ("publicationDate".equals(fieldname)) {
            return createDateQuery(rel);
        }
        if ("rating".equals(fieldname)) {
            int intValue = Integer.parseInt(value);
            return NumericRangeQuery.newIntRange(FIELD_RATING, intValue, intValue, true, true);
        }
        if ("targetPrice".equals(fieldname)) {
            return this.createTargetPriceQuery(rel);
        }

        throw new BadRequestException("unknown field: '" + fieldname + "'");
    }

    private Query getPrefixQuery(String value, final String field) {
        try {
            final Query q =
                    new QueryParser(
                            Version.LUCENE_30,
                            field,
                            new NewsAnalyzer()
                    ).parse(value.endsWith("*") ? value.substring(0, value.length() - 1) : value);
            if (!(q instanceof TermQuery)) {
                return q;
            }
            return new PrefixQuery(((TermQuery) q).getTerm());
        } catch (ParseException e) {
            throw new BadRequestException("invalid query: '" + value + "'");
        }
    }

    private Query createDateQuery(Terms.Relation rel) {
        String value = rel.getValue();
        LocalDate from = START_DATE;
        LocalDate to = END_DATE;
        boolean maxIncl = true;

        switch(rel.getOp()) {
            case EQ:
                from = parseDate(value);
                to = from.plusDays(1);
                maxIncl = false;
                break;

            case GT:
                from = parseDate(value).plusDays(1);
                break;

            case GTE:
                from = parseDate(value);
                break;

            case LT:
                to = parseDate(value);
                maxIncl = false;
                break;

            case LTE:
                to = parseDate(value).plusDays(1);
                maxIncl = false;
                break;

            default:
                throw new BadRequestException("unsupported relation '" + rel + "'");
        }

        return NumericRangeQuery.newLongRange(FIELD_PUBLICATION_DATE,
                from.atStartOfDay().toEpochSecond(ZoneOffset.UTC),
                to.atStartOfDay().toEpochSecond(ZoneOffset.UTC),
                true, maxIncl);
    }

    private LocalDate parseDate(String value) {
        DateTime dateTime = DateUtil.parseDate(value);
        return LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
    }

    private Query createTargetPriceQuery(Terms.Relation rel) {
        BigDecimal requestedValue = new BigDecimal(rel.getValue().replace(',', '.'));
        long value = requestedValue.multiply(TARGET_PRICE_FACTOR).longValue();
        long from = Long.MIN_VALUE;
        long to = Long.MAX_VALUE;
        boolean minIncl = true;
        boolean maxIncl = true;

        switch(rel.getOp()) {
            case EQ:
                from = value;
                to = value;
                break;

            // Intentional fall-through
            case GT:
                minIncl = false;
            case GTE:
                from = value;
                break;

            // Intentional fall-through
            case LT:
                maxIncl = false;
            case LTE:
                to = value;
                break;

            default:
                throw new BadRequestException("unsupported relation '" + rel + "'");
        }
        return NumericRangeQuery.newLongRange(FIELD_TARGET_PRICE, from, to, minIncl, maxIncl);
    }

    private void ensureOp(Terms.Relation rel, Terms.Relation.Op... ops) {
        for (Terms.Relation.Op op : ops) {
            if (rel.getOp() == op) {
                return;
            }
        }
        throw new BadRequestException("unsupported relation '" + rel + "'");
    }

    private Query createKeywordQuery(String fieldname, String valueExpr) {
        String[] values = valueExpr.split("@");
        final BooleanQuery bq = new BooleanQuery();
        for (String value : values) {
            final TermQuery tq = new TermQuery(new org.apache.lucene.index.Term(fieldname,
                    value.startsWith("+") ? value.substring(1) : value));
            if (values.length == 1) {
                return tq;
            }
            bq.add(tq, BooleanClause.Occur.SHOULD);
        }
        return bq;
    }

    private void visitSubterms(final List<Term> terms, final BooleanClause.Occur occur) {
        final BooleanQuery query = new BooleanQuery();
        for (Term subTerm : terms) {
            subTerm.accept(this);
            query.add(this.queryStack.pop(), occur);
        }
        this.queryStack.push(query);
    }
}
