package de.marketmaker.istar.merger.provider.chain;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.finder.Term;
import de.marketmaker.istar.merger.web.finder.TermVisitor;
import de.marketmaker.istar.merger.web.finder.Terms;


class ChainTermVisitor implements TermVisitor {

    private final Deque<Query> queryStack = new LinkedList<>();

    private static final Set<String> VALID_FIELDS = ImmutableSet.<String>builder()
            .add(new String []{
                    ChainIndex.CHAIN_INSTRUMENT,
                    ChainIndex.CHAIN_NAME,
                    ChainIndex.CHAIN_FLAG,
                    ChainIndex.CHAIN_CHANGE_DATE,
                    ChainIndex.NAME,
                    ChainIndex.QID,
                    ChainIndex.VWD_CODE,
                    ChainIndex.VWD_SYMBOL,
                    ChainIndex.NAMES,
                    ChainIndex.TICKERS,
                    ChainIndex.VALOR,
                    ChainIndex.ISIN,
                    ChainIndex.WKN,
                    ChainIndex.LEI,
            })
            .build();

    private static final Filter EMPTY_FILTER = new Filter() {
        @Override
        public DocIdSet getDocIdSet(IndexReader indexReader) throws IOException {
            return DocIdSet.EMPTY_DOCIDSET;
        }
    };

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

    private Query createQuery(Terms.Relation term) {
        String fieldname = term.getIdentifier();
        String value = term.getValue();

        if (VALID_FIELDS.contains(fieldname)) {
            ensureOp(term, Terms.Relation.Op.EQ, Terms.Relation.Op.MATCHES);
            try {
                return createQuery(value, fieldname);
            } catch (ParseException e) {
                throw new BadRequestException("invalid term: '" + term + "'");
            }
        } else {
            throw new BadRequestException("unknown field: '" + fieldname + "'");
        }
    }

    private Query createQuery(String value, String field) throws ParseException {
        String query = value.trim();
        if (query.isEmpty()) {
            throw new BadRequestException("empty query for field: '" + field + "'");
        }
        if (query.length() < 3) {
            return new ConstantScoreQuery(EMPTY_FILTER);
        }
        final boolean quoted = query.startsWith("\"") && query.endsWith("\"");
        if (quoted) {
            return new QueryParser(
                    Version.LUCENE_30,
                    field,
                    ChainIndex.createChainsAnalyzer()
            ).parse(query);
        }

        if (query.contains(" ")) {
            final BooleanQuery result = new BooleanQuery();
            Arrays.stream(query.split(" ")).forEach(s -> {
                try {
                    result.add(
                            new QueryParser(
                                    Version.LUCENE_30,
                                    field,
                                    ChainIndex.createChainsAnalyzer()
                            ).parse(s.endsWith("*") ? s : s + "*"),
                            BooleanClause.Occur.MUST
                    );
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            });
            return result;
        }

        if (ChainIndex.CHAIN_INSTRUMENT.equals(field) && !query.startsWith("#")) {
            query = "#" + query;
        }
        return new QueryParser(
                Version.LUCENE_30,
                field,
                ChainIndex.createChainsAnalyzer()
        ).parse(query.endsWith("*") ? query : query + "*");
    }

    private void ensureOp(Terms.Relation rel, Terms.Relation.Op... ops) {
        for (Terms.Relation.Op op : ops) {
            if (rel.getOp() == op) {
                return;
            }
        }
        throw new BadRequestException("unsupported relation '" + rel + "'");
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
