/*
 * MarketManagerQueryConverter.java
 *
 * Created on 27.03.2007 11:12:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.news;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;

import static org.apache.lucene.search.BooleanClause.Occur.*;

/**
 * Converts queries in old marketmanager query format (= before lucene) into
 * lucene query
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MarketManagerQueryConverter implements QueryConverter {

    private static final Pattern OLD_QUERY_FORMAT
            = Pattern.compile("(keyword|reference|provider|language|category)\\s*=");

    public String toLuceneQueryString(String s) throws Exception {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        if (!OLD_QUERY_FORMAT.matcher(s).find()) {
            return s;
        }
        final MarketManagerQueryLexer lexer = new MarketManagerQueryLexer(new ANTLRStringStream(s));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final MarketManagerQueryParser parser = new MarketManagerQueryParser(tokens);
        final MarketManagerQueryParser.query_return r = parser.query();
        final CommonTree t = (CommonTree) r.getTree();
        final Query query = createQuery(t);
        return (query != null) ? query.toString() : null;
    }

/*
    private void dumpTree(CommonTree t, int indent) {
        if (t == null) {
            return;
        }
        System.out.println("                     ".substring(0, indent) + t.getText() + ":" + t.getType());
        for (int i = 0; i <  t.getChildCount(); i++) {
            dumpTree((CommonTree) t.getChild(i), indent + 2);
        }
    }
*/

    private Query createQuery(CommonTree t) {
        if (t == null) {
            return null;
        }

        final BooleanClause.Occur occur;
        switch (t.getType()) {
            case MarketManagerQueryParser.AND_OP: {
                occur = MUST;
                break;
            }
            case MarketManagerQueryParser.OR_OP: {
                occur = SHOULD;
                break;
            }
            case MarketManagerQueryParser.NOT_OP: {
                occur = MUST_NOT;
                break;
            }
            default: {
                if ("category".equals(t.getText())) {
                    return termQuery(NewsIndexConstants.FIELD_SELECTOR,
                            EntitlementsVwd.normalize(getChildText(t).toUpperCase()));
                }
                if ("language".equals(t.getText())) {
                    return termQuery(NewsIndexConstants.FIELD_LANGUAGE, getChildText(t));
                }
                if ("provider".equals(t.getText())) {
                    return termQuery(NewsIndexConstants.FIELD_AGENCY, resolveAgencyName(getChildText(t)));
                }
                if ("reference".equals(t.getText())) {
                    return termQuery(NewsIndexConstants.FIELD_SYMBOL, getChildText(t));
                }
                if ("keyword".equals(t.getText())) {
                    final String text = getChildText(t);
                    if ("*".equals(text)) {
                        // QueryParser turns *:* into MatchAllDocsQuery
                        return termQuery("*", "*");
                    }
                    return termQuery(NewsIndexConstants.FIELD_TEXT, text);
                }
                return null;
            }
        }

        final BooleanQuery result = new BooleanQuery();
        final List<BooleanClause> clauses = getClauses(t, occur);
        for (BooleanClause booleanClause : clauses) {
            final Query q = booleanClause.getQuery();
            if (q instanceof BooleanQuery) {
                // if a nested ! clause is to be added, add it directly.
                final BooleanQuery by = (BooleanQuery) q;
                if (by.clauses().size() == 1) {
                    final BooleanClause nestedClause = (BooleanClause) by.clauses().get(0);
                    if (nestedClause.getOccur() == MUST_NOT) {
                        result.add(nestedClause);
                        continue;
                    }
                }
            }
            result.add(booleanClause);
        }
        return result;
    }

    private String resolveAgencyName(String s) {
        if ("DPA".equals(s)) { // EMail HBaethge@vwd.com 2009-Jan-06
            return "dpa-AFX";
        }
        if ("TR".equals(s)) { // EMail HBaethge@vwd.com 2009-Oct-12
            return "TFN";
        }
        return s;
    }

    private Query termQuery(String field, String value) {
        return new TermQuery(new Term(field, value));
    }

    private String getChildText(CommonTree t) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < t.getChildCount(); i++) {
            sb.append(t.getChild(i).getText());
        }
        return sb.toString();
    }

    private List<BooleanClause> getClauses(CommonTree t, BooleanClause.Occur occur) {
        List<BooleanClause> result = new ArrayList<>();
        for (int i = 0; i < t.getChildCount(); i++) {
            final Query query = createQuery((CommonTree) t.getChild(i));
            if (query != null) {
                result.add(new BooleanClause(query, occur));
            }
        }
        return result;
    }
}
