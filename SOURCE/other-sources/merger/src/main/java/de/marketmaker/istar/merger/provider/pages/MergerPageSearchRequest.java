package de.marketmaker.istar.merger.provider.pages;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.merger.provider.pages.DocumentFactory.PageField;
import de.marketmaker.istar.merger.provider.pages.DocumentFactory.PageLanguage;

/**
 * MergerPageSearchRequest.java
 * Created on 15.07.2010 15:19:14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

/**
 * Instances of this class are sent to a {@link  de.marketmaker.istar.merger.provider.pages.PageSearchProvider},
 * to search for pages.
 * In the main, this class wrapps a Lucene {@link org.apache.lucene.search.Query}.
 * Consider using the {@link de.marketmaker.istar.merger.provider.pages.MergerPageSearchRequest.Builder}
 * class to programmatically create an instances of this class.
 * @author Sebastian Wild
 */
public class MergerPageSearchRequest extends AbstractIstarRequest {

    private static final long serialVersionUID = 7507532112944711180L;

    /**
     * This class is used to construct a new MergerPageSearchRequest.
     * Use it to consecutively add constraints to the
     * search. The require*-methods offer typical constraints.
     * The constraints are <em>cumulated</em>, i.e. pages must fulfil all or at least one of them,
     * depending on parameter asOrQuery.
     */
    public static class Builder {

        /**
         * Starts a new Builder for MergerPageSearchRequest without any constraints.
         */
        public Builder() {
        }

        /**
         * Adds a new constraint to this builder.
         * The constraint is fulfilled, if the page contains a data field that references
         * the symbol with name {@code symbol}. It does <em>not</em> matter, which field
         * of the symbol is referenced.
         * @param symbol the symbol to search for
         * @return this builder for method chaining
         */
        public Builder requireSymbol(String symbol) {
            Query query = new TermQuery(new Term(PageField.KEY.fieldName(), symbol));
            constraints.add(query);
            return this;
        }

        /**
         * Adds a new constraint to this builder.
         * The constraint is fulfilled, if the page contains a data field that references
         * the symbol with name {@code symbol} and the data field with index {@code field}
         * @param symbol the symbol to search for
         * @param field the id of the data field inside {@code symbol} to look for
         * @return this builder for method chaining
         */
        public Builder requireSymbolField(String symbol, int field) {
            Query query = new TermQuery(new Term(PageField.KEY_FIELD.fieldName(),
                    symbol + DocumentFactory.SYMBOL_AND_SYMBOLFIELD_SEPARATOR + field));
            constraints.add(query);
            return this;
        }

        /**
         * Adds a new constraint to this builder.
         * The constraint is fulfilled, if the page has a pointer/link to the page with
         * page number {@code targetPageNumber}.
         * @param targetPageNumber the page number of the pointer to search for
         * @return this builder for method chaining
         */
        public Builder requirePointer(int targetPageNumber) {
            Query query = new TermQuery(new Term(PageField.POINTER.fieldName(),
                    String.valueOf(targetPageNumber)));
            constraints.add(query);
            return this;
        }

        /**
         * Adds a new constraint to this builder.
         * The constraint is fulfilled, if the {@code word} appears in the static text of
         * the page.
         * @param word the word of static text to search for
         * @return this builder for method chaining
         */
        public Builder requireText(String word) {
            BooleanQuery query = new BooleanQuery();
            query.add(new TermQuery(new Term(PageField.STATIC_TEXT.fieldName(),
                    word.toLowerCase())), BooleanClause.Occur.SHOULD);
            query.add(new TermQuery(new Term(PageField.HEADING.fieldName(),
                    word.toLowerCase())), BooleanClause.Occur.SHOULD);
            query.add(new TermQuery(new Term(PageField.TITLE.fieldName(),
                    word.toLowerCase())), BooleanClause.Occur.SHOULD);
            constraints.add(query);
            return this;
        }

        /**
         * Adds a new constraint to this builder.
         * The constraint is fulfilled, if the {@code word} appears in the static text of
         * the page.
         * @param words the phrase of static text to search for
         * @return this builder for method chaining
         */
        public Builder requireTextPhrase(final List<? extends String> words) {
            BooleanQuery query = new BooleanQuery();
            final Set<PageField> contentFields = new HashSet<>(Arrays.asList(
                    PageField.TITLE,
                    PageField.HEADING,
                    PageField.STATIC_TEXT));
            for (PageField field : contentFields) {
                PhraseQuery phraseQuery = new PhraseQuery();
                for (final String word : words) {
                    phraseQuery.add(new Term(field.fieldName(), word.toLowerCase()));
                }
                query.add(phraseQuery, BooleanClause.Occur.SHOULD);
            }
            constraints.add(query);
            return this;
        }

        /**
         * Set the preferred language. The search is guaranteed to return only the preferred
         * language version of a page that fulfills all the other requirements. However,
         * non-preferred language versions may be returned if they fulfil requirements while
         * preferred versions do not.
         * @param lang the preferred language
         * @return this (for chaining)
         */
        public Builder withPreferredLanguage(PageLanguage lang) {
            if (lang == PageLanguage.UNSPECIFIED) {
                throw new IllegalArgumentException("UNSPECIFIED language cannot be made preference");
            }
            this.preferredLanguage = lang;
            return this;
        }

        /**
         * Restricts this search to only consider pages present in language {@code lang}.
         * @param lang the language to use
         * @return this for chaining
         */
        public Builder requireLanguage(PageLanguage lang) {
            this.constraints.add(new TermQuery(new Term(PageField.LANGUAGE.fieldName(), lang.name())));
            return this;
        }

        /**
         * Adds the custom query to the list of constraints.
         * @param query query to add
         * @return this (for chaining)
         */
        public Builder requireQuery(Query query) {
            constraints.add(query);
            return this;
        }

        public Builder asAndQuery(boolean flag) {
            this.andQuery = flag;
            return this;
        }

        /**
         * creates a new MergerPageSearchRequest object incorporating the constraints
         * given to this builder.
         * @return new MergerPageSearchRequest object with constraints.
         */
        public MergerPageSearchRequest build() {
            if (constraints.size() == 0)
                /* logically, it would make sense to return ALL pages ...          *
                 * but is is much more probable that someone misspelled something, *
                 * so create a dummy query that will for sure return no pages.     */
                return new MergerPageSearchRequest(new TermQuery(new Term("nothing", "$")), null);
            else {
                final Query requirements;
                if (constraints.size() == 1) {
                    requirements = constraints.get(0);
                }
                else {
                    final BooleanClause.Occur type
                            = this.andQuery ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;

                    final BooleanQuery conjunction = new BooleanQuery();
                    for (final Query clause : constraints) {
                        conjunction.add(clause, type);
                    }
                    requirements = conjunction;
                }
                return new MergerPageSearchRequest(requirements, preferredLanguage);
            }
        }

        private List<Query> constraints = new LinkedList<>();

        private boolean andQuery = true;

        private PageLanguage preferredLanguage = null;
    }

    private final Query luceneQuery;

    private final PageLanguage preferredLanguage;

    /**
     * retrieve the {@link org.apache.lucene.search.Query} stored in this request.
     * @return stored query
     */
    public Query getLuceneQuery() {
        return luceneQuery;
    }

    public PageLanguage getPreferredLanguage() {
        return preferredLanguage;
    }

    /**
     * Creates a new MergerPageSearchRequest holding the given Lucene query.
     * This constructor is for special requirements; consider using the
     * {@link de.marketmaker.istar.merger.provider.pages.MergerPageSearchRequest.Builder}
     * for this class instead. It offers means to implement typical queries much more
     * conveniently.
     * @param luceneQuery the query to put into this request
     * @param preferredLanguage the preferred language to search for or null to ignore
     */
    public MergerPageSearchRequest(final Query luceneQuery, final PageLanguage preferredLanguage) {
        this.luceneQuery = luceneQuery;
        this.preferredLanguage = preferredLanguage;
    }

}
