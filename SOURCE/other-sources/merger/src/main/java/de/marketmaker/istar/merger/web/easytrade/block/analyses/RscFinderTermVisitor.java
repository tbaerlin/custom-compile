/*
 * RscFinderTermVisitor.java
 *
 * Created on 05.04.12 09:16
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.analyses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.analyses.backend.Analysis2Document;
import de.marketmaker.istar.analyses.frontend.AnalysesIndexConstants;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.SimpleSearchCommand;
import de.marketmaker.istar.merger.web.finder.Term;
import de.marketmaker.istar.merger.web.finder.TermVisitor;
import de.marketmaker.istar.merger.web.finder.Terms;
import de.marketmaker.istar.news.backend.NewsQuerySupport;

import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.IID_SUFFIX;
import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.QID_SUFFIX;

/**
 * creates a lucene query based on a finder query expression
 *
 * @author oflege
 */
class RscFinderTermVisitor implements TermVisitor {
    private static final EnumSet<InstrumentTypeEnum> COUNT_TYPES = EnumSet.of(InstrumentTypeEnum.STK);

    private static final LocalDate START_DATE = new LocalDate(2000, 1, 1);

    private static final LocalDate END_DATE = new LocalDate(2038, 1, 1);

    private static final String WILDCARD = "*";

    private static final Map<String, String> DEFAULT_KEYWORD_2_LUCENE_FIELDS = new HashMap<>();

    private static final Map<String, String> WEBSIM_KEYWORD_2_LUCENE_FIELDS = new HashMap<>();

    static {
        DEFAULT_KEYWORD_2_LUCENE_FIELDS.put("analyst", AnalysesIndexConstants.FIELD_SOURCE);
        DEFAULT_KEYWORD_2_LUCENE_FIELDS.put("sector", AnalysesIndexConstants.FIELD_BRANCH);
        DEFAULT_KEYWORD_2_LUCENE_FIELDS.put("region", AnalysesIndexConstants.FIELD_COUNTRY);
        DEFAULT_KEYWORD_2_LUCENE_FIELDS.put("recommendation", AnalysesIndexConstants.FIELD_RECOMMENDATION);
        DEFAULT_KEYWORD_2_LUCENE_FIELDS.put("category", AnalysesIndexConstants.FIELD_CATEGORY);

        WEBSIM_KEYWORD_2_LUCENE_FIELDS.put("raccfond", AnalysesIndexConstants.FIELD_RACCFOND);
        WEBSIM_KEYWORD_2_LUCENE_FIELDS.put("racctecn", AnalysesIndexConstants.FIELD_RACCTECN);
        WEBSIM_KEYWORD_2_LUCENE_FIELDS.put("category", AnalysesIndexConstants.FIELD_CATEGORY);
    }

    private final EasytradeInstrumentProvider instrumentProvider;

    private final Deque<Query> queryStack = new LinkedList<>();

    private final Map<String, String> finder2luceneFields;

    public RscFinderTermVisitor(Selector selector, EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
        switch (selector) {
            case WEB_SIM_ANALYSES:
                this.finder2luceneFields = WEBSIM_KEYWORD_2_LUCENE_FIELDS;
                break;
            case AWP_ANALYSER:
                this.finder2luceneFields = DEFAULT_KEYWORD_2_LUCENE_FIELDS;
                break;
            case DPA_AFX_ANALYSES:
                this.finder2luceneFields = DEFAULT_KEYWORD_2_LUCENE_FIELDS;
                break;
            default:
                this.finder2luceneFields = DEFAULT_KEYWORD_2_LUCENE_FIELDS;
                break;
        }
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

        if (this.finder2luceneFields.containsKey(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            return createTermQuery(this.finder2luceneFields.get(fieldname), value);
        }
        // fulltext search with custom query parser
        else if ("searchstring".equals(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            try {
                return new QueryParser(Version.LUCENE_30, AnalysesIndexConstants.FIELD_TEXT,
                        NewsQuerySupport.createAnalyzer()).parse(value);
            } catch (ParseException ex) {
                throw new BadRequestException("invalid query: '" + value + "'");
            }
        }
        // iid searches
        else if ("index".equals(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            return createIidQuery(iidsForIndex(value));
        }
        else if ("symbol".equals(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            return createIidQuery(iidsForSymbol(value));
        }
        // timerange
        else if ("start".equals(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ, Terms.Relation.Op.GT, Terms.Relation.Op.GTE);
            return createDateQuery(parseDate(value), END_DATE);
        }
        else if ("end".equals(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ, Terms.Relation.Op.LT, Terms.Relation.Op.LTE);
            return createDateQuery(START_DATE, parseDate(value).plusDays(1));
        }

        else if ("date".equals(fieldname)) {
            return createDateQuery(rel);
        }
        else if (AnalysesIndexConstants.FIELD_TARGET.equals(fieldname)) {
            return createDoubleQuery(fieldname, rel);
        }
        else if (AnalysesIndexConstants.FIELD_TIMEFRAME.equals(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            return createTimeframeQuery(fieldname, rel);
        }
        throw new BadRequestException("unknown field: '" + fieldname + "'");
    }

    private Query createTimeframeQuery(String field, Terms.Relation rel) {
        String value = rel.getValue();
        try {
            return new QueryParser(Version.LUCENE_30, field,
                    NewsQuerySupport.createAnalyzer()).parse(value);
        } catch (ParseException ex) {
            throw new BadRequestException("invalid query: '" + value + "'");
        }
    }

    private Query createDoubleQuery(String field, Terms.Relation rel) {
        Double value;
        try {
            value = new Double(rel.getValue());
        } catch (NumberFormatException ex) {
            throw new BadRequestException("invalid number format: '" + rel.getValue() + "'");
        }
        if (rel.getOp() == Terms.Relation.Op.EQ) {
            return NumericRangeQuery.newDoubleRange(field, value, value, true, true);
        }
        else if (rel.getOp() == Terms.Relation.Op.GTE) {
            return NumericRangeQuery.newDoubleRange(field, value, null, true, false);
        }
        else if (rel.getOp() == Terms.Relation.Op.GT) {
            return NumericRangeQuery.newDoubleRange(field, value, null, false, false);
        }
        else if (rel.getOp() == Terms.Relation.Op.LTE) {
            return NumericRangeQuery.newDoubleRange(field, null, value, false, true);
        }
        else if (rel.getOp() == Terms.Relation.Op.LT) {
            return NumericRangeQuery.newDoubleRange(field, null, value, false, false);
        }
        throw new BadRequestException("unsupported relation '" + rel + "'");
    }

    private Query createDateQuery(Terms.Relation rel) {
        String value = rel.getValue();
        LocalDate from = START_DATE;
        LocalDate to = END_DATE;
        if (rel.getOp() == Terms.Relation.Op.EQ) {
            from = parseDate(value);
            to = parseDate(value).plusDays(1);
        }
        else if (rel.getOp() == Terms.Relation.Op.GTE) {
            from = parseDate(value);
        }
        else if (rel.getOp() == Terms.Relation.Op.GT) {
            from = parseDate(value).plusDays(1);
        }
        else if (rel.getOp() == Terms.Relation.Op.LTE) {
            to = parseDate(value).plusDays(1);
        }
        else if (rel.getOp() == Terms.Relation.Op.LT) {
            to = parseDate(value);
        }
        else {
            throw new BadRequestException("unsupported relation '" + rel + "'");
        }
        return createDateQuery(from, to);
    }

    private LocalDate parseDate(String value) {
        return DateUtil.parseDate(value).toLocalDate();
    }

    private Query createDateQuery(LocalDate from, LocalDate to) {
        return NumericRangeQuery.newIntRange(AnalysesIndexConstants.FIELD_DATE,
                Analysis2Document.encodeTimestamp(from.toDateTimeAtStartOfDay().getMillis()),
                Analysis2Document.encodeTimestamp(to.toDateTimeAtStartOfDay().getMillis()),
                true, true);
    }

    private Query createIidQuery(List<Long> instrumentids) {
        final BooleanQuery result = new BooleanQuery();
        for (Long iid : instrumentids) {
            TermQuery term = createTermQuery(AnalysesIndexConstants.FIELD_IID, iid.toString());
            result.add(term, BooleanClause.Occur.SHOULD);
        }
        return result;
    }

    private void ensureOp(Terms.Relation rel, Terms.Relation.Op... ops) {
        for (Terms.Relation.Op op : ops) {
            if (rel.getOp() == op) {
                return;
            }
        }
        throw new BadRequestException("unsupported relation '" + rel + "'");
    }

    private TermQuery createTermQuery(String fieldname, String value) {
        return new TermQuery(new org.apache.lucene.index.Term(fieldname, value));
    }

    private void visitSubterms(final List<Term> terms, final BooleanClause.Occur occur) {
        final BooleanQuery query = new BooleanQuery();
        for (Term subTerm : terms) {
            subTerm.accept(this);
            query.add(this.queryStack.pop(), occur);
        }
        this.queryStack.push(query);
    }

    private List<Long> iidsForSymbol(String value) {
        if (value.endsWith(IID_SUFFIX)) {
            return Collections.singletonList(EasytradeInstrumentProvider.id(value));
        }

        if (value.endsWith(QID_SUFFIX)) {
            return Collections.singletonList(this.instrumentProvider.identifyInstrument(value,
                    SymbolStrategyEnum.QID).getId());
        }

        final SimpleSearchCommand command = new SimpleSearchCommand(value, null, null,
                InstrumentProvider.StrategyEnum.DEFAULT, COUNT_TYPES, COUNT_TYPES,
                null, null, 0, 50, 50, true);
        final SearchResponse sr = this.instrumentProvider.simpleSearch(command);

        final ArrayList<Long> result = new ArrayList<>();
        for (Instrument instrument : sr.getInstruments()) {
            result.add(instrument.getId());
        }

        return result;
    }

    private List<Long> iidsForIndex(String index) {
        if (!StringUtils.hasText(index) || !index.endsWith(".qid")) {
            throw new BadRequestException("invalid index: '" + index + "'");
        }
        final List<Quote> quotes = this.instrumentProvider.getIndexQuotes(index);
        final List<Long> result = new ArrayList<>(quotes.size());
        for (final Quote quote : quotes) {
            if (quote != null) {
                result.add(quote.getInstrument().getId());
            }
        }
        return result;
    }

}
