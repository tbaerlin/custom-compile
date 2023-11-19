/*
 * NwsFinderTermVisitor.java
 *
 * Created on 07.05.12 11:55
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import de.marketmaker.istar.analyses.frontend.AnalysesIndexConstants;
import de.marketmaker.istar.common.util.DateTimeEditor;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.finder.Term;
import de.marketmaker.istar.merger.web.finder.TermVisitor;
import de.marketmaker.istar.merger.web.finder.Terms;
import de.marketmaker.istar.news.backend.News2Document;
import de.marketmaker.istar.news.backend.NewsQuerySupport;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;
import de.marketmaker.istar.news.frontend.NewsRequest;

import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.QID_SUFFIX;
import static de.marketmaker.istar.news.frontend.NewsIndexConstants.FIELD_PROVIDERCODE;
import static de.marketmaker.istar.news.frontend.NewsIndexConstants.resolveFieldname;

/**
 * @author oflege
 */
class NwsFinderTermVisitor implements TermVisitor {
    private static final EnumSet<InstrumentTypeEnum> COUNT_TYPES = EnumSet.of(InstrumentTypeEnum.STK, InstrumentTypeEnum.IND, InstrumentTypeEnum.GNS);

    private static final DateTime START_DATE = new LocalDate(2000, 1, 1).toDateTimeAtStartOfDay();

    private static final DateTime END_DATE = new LocalDate(2038, 1, 1).toDateTimeAtStartOfDay();

    private static final Pattern DAY_PATTERN
            = Pattern.compile("([0-9]{4}-[0-9]{2}-[0-9]{2}|[0-9]{2}\\.[0-9]{2}\\.[0-9]{4})");

    private static final Set<String> NO_IN_FIELDS = new HashSet<>(Arrays.asList("date", "text", "headline"));

    private static final Set<String> TERM_FIELDS;

    static {
        final HashSet<String> tmp = new HashSet<>(NewsIndexConstants.ATTRIBUTE_2_FIELDNAME.values());
        tmp.addAll(Arrays.asList("agency", "topic", "language", FIELD_PROVIDERCODE));
        TERM_FIELDS = tmp;
    }

    private final EasytradeInstrumentProvider instrumentProvider;

    private final Deque<Query> queryStack = new LinkedList<>();

    private final NewsRequest request;

    private Query iidQuery = null;

    public NwsFinderTermVisitor(EasytradeInstrumentProvider instrumentProvider,
            NewsRequest request) {
        this.instrumentProvider = instrumentProvider;
        this.request = request;
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
        final String fieldname = resolveFieldname(term.getIdentifier());
        if (NO_IN_FIELDS.contains(fieldname)) {
            throw new BadRequestException("cannot use IN with field " + fieldname);
        }
        if (fieldname.equals(NewsIndexConstants.FIELD_ID)) {
            this.queryStack.push(createInQuery(term, fieldname));
            if (this.iidQuery == null) {
                // a top-level, id-only query can be optimized by just adding the ids to the request
                // so that retrieval does not have to deal with lucene querying at all
                this.iidQuery = this.queryStack.getLast();
                this.request.setNewsids(new ArrayList<>(term.getValues()));
            }
        }
        else if (isInstrumentField(fieldname)) {
            this.queryStack.push(createIidQuery(iidsForSymbol(isIidField(fieldname), term.getValues())));
        }
        else if (TERM_FIELDS.contains(fieldname)) {
            this.queryStack.push(createInQuery(term, fieldname));
        }
        else {
            throw new BadRequestException("unknown field: '" + fieldname + "'");
        }
    }

    protected BooleanQuery createInQuery(Terms.In term, String fieldname) {
        BooleanQuery result = new BooleanQuery();
        for (String s : term.getValues()) {
            result.add(createTermQuery(fieldname, s), BooleanClause.Occur.SHOULD);
        }
        return result;
    }

    public Query getResult() {
        final Query query = this.queryStack.poll();
        if (this.iidQuery != null) {
            if (query == this.iidQuery) {
                // we have a top-level, id-only query, continue without lucene query
                return null;
            }
            // iidQuery was only the first subquery among others, remove the ids from the request
            // as it will have to process the query we return below
            this.request.setNewsids(null);
        }
        return query;
    }

    @Override
    public void visit(Terms.Relation term) {
        this.queryStack.push(createQuery(term));
    }

    private Query createQuery(Terms.Relation rel) {
        String fieldname = resolveFieldname(rel.getIdentifier());
        String value = rel.getValue();

        if ("text".equals(fieldname) || "headline".equals(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            return createSearch(fieldname, value);
        }
        else if (isInstrumentField(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            return createIidQuery(iidsForSymbol(isIidField(fieldname), Collections.singleton(value)));
        }
        else if ("date".equals(fieldname)) {
            return createDateQuery(rel);
        }
        else if (TERM_FIELDS.contains(fieldname)) {
            ensureOp(rel, Terms.Relation.Op.EQ);
            final String term = NwsFindersuchergebnis.toValidTerm(value);
            if (term == null) {
                throw new BadRequestException("invalid term value for: " + fieldname + "='" + value + "'");
            }
            return createTermQuery(fieldname, term);
        }

        throw new BadRequestException("unknown field: '" + fieldname + "'");
    }


    private boolean isInstrumentField(String fieldname) {
        return isIidField(fieldname) || "symbol".equals(fieldname);
    }

    private boolean isIidField(String fieldname) {
        return "iid".equals(fieldname);
    }

    private Query createSearch(String fieldname, String value) {
        try {
            return new QueryParser(Version.LUCENE_30,
                    AnalysesIndexConstants.FIELD_TEXT, NewsQuerySupport.createAnalyzer())
                    .parse(toQuery(value, "headline".equals(fieldname)));
        } catch (ParseException e) {
            throw new BadRequestException("invalid query: '" + value + "'");
        }
    }

    private Query createDateQuery(Terms.Relation rel) {
        final String value = rel.getValue();
        final boolean day = isDay(value);
        DateTime from = START_DATE;
        DateTime to = END_DATE;
        if (rel.getOp() == Terms.Relation.Op.EQ) {
            from = parseDate(value);
            to = inc(from, day);
        }
        else if (rel.getOp() == Terms.Relation.Op.GTE) {
            from = parseDate(value);
        }
        else if (rel.getOp() == Terms.Relation.Op.GT) {
            from = inc(parseDate(value), day);
        }
        else if (rel.getOp() == Terms.Relation.Op.LTE) {
            to = inc(parseDate(value), day);
        }
        else if (rel.getOp() == Terms.Relation.Op.LT) {
            to = parseDate(value);
        }
        else {
            throw new BadRequestException("unsupported relation '" + rel + "'");
        }
        return createDateQuery(from, to);
    }

    private DateTime inc(DateTime dt, boolean day) {
        return day ? dt.plusDays(1) : dt.plusSeconds(1);
    }

    private DateTime parseDate(String value) {
        if (isDay(value)) {
            return DateUtil.parseDate(value);
        }
        final DateTimeEditor editor = new DateTimeEditor();
        editor.setAsText(value);
        return (DateTime) editor.getValue();
    }

    private boolean isDay(String value) {
        return DAY_PATTERN.matcher(value).matches();
    }

    private Query createDateQuery(DateTime from, DateTime to) {
        return NumericRangeQuery.newIntRange(NewsIndexConstants.FIELD_TIMESTAMP,
                News2Document.encodeTimestamp(from),
                News2Document.encodeTimestamp(to),
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
        return new TermQuery(
                new org.apache.lucene.index.Term(fieldname, value.toLowerCase()));
    }

    private void visitSubterms(final List<Term> terms, final BooleanClause.Occur occur) {
        final BooleanQuery query = new BooleanQuery();
        for (Term subTerm : terms) {
            subTerm.accept(this);
            query.add(this.queryStack.pop(), occur);
        }
        this.queryStack.push(query);
    }


    private List<Long> iidsForSymbol(boolean iidField, Collection<String> values) {
        final ArrayList<Long> result = new ArrayList<>();
        for (String value : values) {
            if (result.size() >= 100) {
                return result;
            }

            if (iidField) {
                result.add(EasytradeInstrumentProvider.id(value));
                continue;
            }

            if (value.endsWith(QID_SUFFIX)) {
                result.add(this.instrumentProvider.identifyInstrument(value,
                        SymbolStrategyEnum.QID).getId());
                continue;
            }

            final SimpleSearchCommand command = new SimpleSearchCommand(value, null, null,
                    InstrumentProvider.StrategyEnum.DEFAULT, COUNT_TYPES, COUNT_TYPES,
                    null, null, 0, 50, 50, true);
            final SearchResponse sr = this.instrumentProvider.simpleSearch(command);

            for (Instrument instrument : sr.getInstruments()) {
                result.add(instrument.getId());
            }
        }
        return result;
    }

    private String toQuery(String searchstring, boolean headlineOnly) {
        final String term = NwsFindersuchergebnis.toValidTerm(searchstring);

        if (term == null) {
            throw new BadRequestException("invalid searchstring: '" + searchstring + "'");
        }

        if (headlineOnly) {
            return "+" + NewsIndexConstants.FIELD_HEADLINE + ":(" + term + ") ";
        }

        if (term.contains(" ")) {
            return "+" + NewsIndexConstants.FIELD_TEXT + ":(\"" + term + "\")";
        }

        final StringBuilder sb = new StringBuilder("+(");
        sb.append(NewsIndexConstants.FIELD_TEXT).append(":(").append(term).append(") ");
        sb.append(NewsIndexConstants.FIELD_SYMBOL).append(":(").append(term).append(") ");
        sb.append(") ");
        return sb.toString();
    }
}
