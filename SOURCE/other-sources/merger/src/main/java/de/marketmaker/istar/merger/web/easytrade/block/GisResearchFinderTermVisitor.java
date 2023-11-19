/*
 * RscFinderTermVisitor.java
 *
 * Created on 05.04.12 09:16
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.joda.time.LocalDate;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.merger.provider.gisresearch.Research2Document;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.finder.Term;
import de.marketmaker.istar.merger.web.finder.TermVisitor;
import de.marketmaker.istar.merger.web.finder.Terms;
import de.marketmaker.istar.news.analysis.NewsAnalyzer;

import static de.marketmaker.istar.merger.provider.gisresearch.GisResearchIndexConstants.*;

/**
 * Creates a lucene query based on a finder query expression
 * @author oflege
 */
class GisResearchFinderTermVisitor implements TermVisitor {
    private static final LocalDate START_DATE = new LocalDate(2000, 1, 1);

    private static final LocalDate END_DATE = new LocalDate(2038, 1, 1);

    private static final Map<String, String> KEYWORD_QUERY_FIELDS = new HashMap<>();

    static {
        KEYWORD_QUERY_FIELDS.put("issuer", FIELD_ISSUER);
        KEYWORD_QUERY_FIELDS.put("type", FIELD_ASSET_CLASS);
        KEYWORD_QUERY_FIELDS.put("country", FIELD_COUNTRY);
        KEYWORD_QUERY_FIELDS.put("isin", FIELD_ISIN);
        KEYWORD_QUERY_FIELDS.put("sustainable", FIELD_SUSTAINABLE);
        KEYWORD_QUERY_FIELDS.put("recommendation", FIELD_RECOMM);
        KEYWORD_QUERY_FIELDS.put("documentType", FIELD_TYPE);
        KEYWORD_QUERY_FIELDS.put("sector", FIELD_SECTOR);
    }

    private final Deque<Query> queryStack = new LinkedList<>();

    private final GisResearchFinder finder;

    private final GisResearchFinder.Command cmd;

    public GisResearchFinderTermVisitor(GisResearchFinder finder,
            GisResearchFinder.Command cmd) {
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
        else if ("title".equals(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            return getPrefixQuery(value, FIELD_TITLE);
        }
        else if ("symbol".equals(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            this.cmd.setSymbol(value);
            return this.finder.getIssuerQuery(this.cmd);
        }
        if ("priceToFairValuePercent".equals(fieldname)) {
            return createDiffQuery(rel);
        }
        if ("date".equals(fieldname)) {
            return createDateQuery(rel);
        }

        throw new BadRequestException("unknown field: '" + fieldname + "'");
    }

    private Query getPrefixQuery(String value, final String field) {
        try {
            final Query q = new QueryParser(Version.LUCENE_30,
                    field, new NewsAnalyzer())
                    .parse(value.endsWith("*") ? value.substring(0, value.length() - 1) : value);
            if (!(q instanceof TermQuery)) {
                return q;
            }
            return new PrefixQuery(((TermQuery) q).getTerm());
        } catch (ParseException e) {
            throw new BadRequestException("invalid query: '" + value + "'");
        }
    }

    private Query createDiffQuery(Terms.Relation rel) {
        String value = rel.getValue();
        Float min = null;
        Float max = null;
        boolean minIncl = true;
        boolean maxIncl = true;
        if (rel.getOp() == Terms.Relation.Op.EQ) {
            min = parsePercent(value);
            max = min;
        }
        else if (rel.getOp() == Terms.Relation.Op.GTE) {
            min = parsePercent(value);
        }
        else if (rel.getOp() == Terms.Relation.Op.GT) {
            min = parsePercent(value);
            minIncl = false;
        }
        else if (rel.getOp() == Terms.Relation.Op.LTE) {
            max = parsePercent(value);
        }
        else if (rel.getOp() == Terms.Relation.Op.LT) {
            max = parsePercent(value);
            maxIncl = false;
        }
        else {
            throw new BadRequestException("unsupported relation '" + rel + "'");
        }
        return NumericRangeQuery.newFloatRange(FIELD_DIFF, min, max, minIncl, maxIncl);
    }

    private Float parsePercent(String value) {
        try {
            return ((Double) (Double.valueOf(value.replace(',', '.')) / 100d)).floatValue();
        } catch (NumberFormatException e) {
            throw new BadRequestException("invalid number: '" + value + "'");
        }
    }


    private Query createDateQuery(Terms.Relation rel) {
        String value = rel.getValue();
        LocalDate from = START_DATE;
        LocalDate to = END_DATE;
        boolean minIncl = true;
        boolean maxIncl = true;
        if (rel.getOp() == Terms.Relation.Op.EQ) {
            from = parseDate(value);
            to = parseDate(value).plusDays(1);
            maxIncl = false;
        }
        else if (rel.getOp() == Terms.Relation.Op.GTE) {
            from = parseDate(value);
        }
        else if (rel.getOp() == Terms.Relation.Op.GT) {
            from = parseDate(value).plusDays(1);
            maxIncl = false;
        }
        else if (rel.getOp() == Terms.Relation.Op.LTE) {
            to = parseDate(value);
        }
        else if (rel.getOp() == Terms.Relation.Op.LT) {
            to = parseDate(value).minusDays(1);
            minIncl = false;
        }
        else {
            throw new BadRequestException("unsupported relation '" + rel + "'");
        }
        return createDateQuery(from, to, minIncl, maxIncl);
    }

    private LocalDate parseDate(String value) {
        return DateUtil.parseDate(value).toLocalDate();
    }

    private Query createDateQuery(LocalDate from, LocalDate to, boolean minIncl, boolean maxIncl) {
        return NumericRangeQuery.newIntRange(FIELD_DATE,
                Research2Document.encodeTimestamp(from.toDateTimeAtStartOfDay()),
                Research2Document.encodeTimestamp(to.toDateTimeAtStartOfDay()),
                minIncl, maxIncl);
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
