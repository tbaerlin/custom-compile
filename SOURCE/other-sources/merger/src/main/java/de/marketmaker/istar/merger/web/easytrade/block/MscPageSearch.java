/*
 * MscPageSearch.java
 *
 * Created on 21.07.2010 13:20:01
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.provider.pages.DocumentFactory;
import de.marketmaker.istar.merger.provider.pages.MergerPageSearchRequest;
import de.marketmaker.istar.merger.provider.pages.MergerPageSearchResponse;
import de.marketmaker.istar.merger.provider.pages.PageSearchProvider;
import de.marketmaker.istar.merger.provider.pages.PageSummary;
import de.marketmaker.istar.merger.web.easytrade.EnumEditor;
import de.marketmaker.istar.merger.web.easytrade.ListCommandWithOptionalPaging;
import de.marketmaker.istar.merger.web.easytrade.ListHelper;
import de.marketmaker.istar.merger.web.easytrade.ListResult;

/**
 * Search vwd pages for texts or symbols.
 * @author Sebastian Wild
 */
public class MscPageSearch extends EasytradeCommandController {

    private PageSearchProvider pageSearchProvider;

    public void setPageSearchProvider(PageSearchProvider pageSearchProvider) {
        this.pageSearchProvider = pageSearchProvider;
    }

    /**
     * Command class holding the key value pairs that describe the page query. <br/>
     * To types of queries can be given:
     * <ul>
     * <li>simple query.<br/>
     * For those, the following fields may be filled.
     * <ul>
     * <li>{@link #text}</li>
     * <li>{@link #usedSymbol}</li>
     * <li>{@link #referencedPage}</li>
     * <li>{@link #language}</li>
     * </ul>
     * All given values are required, i.e. the <em>conjunction</em> of all those
     * is used.
     * </li>
     * <li>custom query.<br/>
     * This can be any Lucene Query in the standard syntax understood by
     * {@link org.apache.lucene.queryParser.QueryParser}.
     * The used field names are listed in {@link de.marketmaker.istar.merger.provider.pages.DocumentFactory.PageField}.
     * </li>
     * </ul>
     */
    @SuppressWarnings("UnusedDeclaration")
    public static class Command extends ListCommandWithOptionalPaging {

        // Spring requires paramterless ctor.

        public Command() {
            setAscending(false); // default sort by score makes no sense ascending
        }

        private String[] usedSymbol;

        /**
         * required pattern for elements of {@link #usedSymbol}
         */
        public static final Pattern SYMBOL_PATTERN = Pattern.compile("([^\\^\\s]+)(?:\\^(\\d+))?");

        private int[] referencedPage;

        private String[] text;

        private DocumentFactory.PageLanguage language;

        private String customQuery;

        private boolean andQuery;

        /**
         * Symbol(s) for which either any field is displayed on returned pages or, if the
         * fieldId is added as a symbol suffix (separated by '^'), that specific field is displayed.
         * If multiple values are given, all must occur on the page.
         * @sample AUDREF.EZB^30
         */
        public String[] getUsedSymbol() {
            return usedSymbol;
        }

        public void setUsedSymbol(String[] usedSymbol) {
            this.usedSymbol = usedSymbol;
        }

        /**
         * @return page number(s) referenced on the returned pages
         */
        public int[] getReferencedPage() {
            return referencedPage;
        }

        public void setReferencedPage(int[] referencedPage) {
            this.referencedPage = referencedPage;
        }

        /**
         * Text phrase(s) to search, the text(s) <b>must appear as static text</b>  on the page
         * (i.e., search for dynamic content is currently not possible).
         * The order does not matter at all. Each entry can either be a single word, or a phrase of words
         * separated by whitespace.
         */
        public String[] getText() {
            return text;
        }

        public void setText(String[] text) {
            this.text = text;
        }

        /**
         * @return search page text in the given language if available
         */
        public DocumentFactory.PageLanguage getLanguage() {
            return language;
        }

        public void setLanguage(DocumentFactory.PageLanguage language) {
            this.language = language;
        }

        @MmInternal
        public boolean isAndQuery() {
            return andQuery;
        }

        public void setAndQuery(boolean andQuery) {
            this.andQuery = andQuery;
        }

        @MmInternal
        public String getCustomQuery() {
            return customQuery;
        }

        public void setCustomQuery(String customQuery) {
            this.customQuery = customQuery;
        }

        @Override
        public String toString() {
            Map<String, String> props = new TreeMap<>();
            props.put("usedSymbols", Arrays.toString(this.getUsedSymbol()));
            props.put("language", String.valueOf(getLanguage()));
            props.put("refedPages", Arrays.toString(getReferencedPage()));
            props.put("words", Arrays.toString(getText()));
            props.put("andQuery", Boolean.toString(isAndQuery()));
            props.put("customQuery", this.getCustomQuery());
            return "MscPageSearch$Command" + props.toString();
        }
    }


    private static final Map<String, Comparator<PageSummary>> PAGE_SUMMARY_SORTERS =
            new HashMap<>();

    private static final String DEFAULT_SORT_BY = "score";

    private static final List<String> SORT_FIELDS;

    static {
        PAGE_SUMMARY_SORTERS.put("score", PageSummary.BY_SCORE_COMPARATOR);
        PAGE_SUMMARY_SORTERS.put("pagenumber", PageSummary.BY_PAGENUMBER_COMPARATOR);
        PAGE_SUMMARY_SORTERS.put("heading", PageSummary.BY_CONTENT_SUMMARY_COMPARATOR);

        final List<String> list = new ArrayList<>(PAGE_SUMMARY_SORTERS.keySet());
        SORT_FIELDS = Collections.unmodifiableList(list);
    }


    public MscPageSearch() {
        super(Command.class);
    }

    @Override
    protected void initBinder(HttpServletRequest httpServletRequest,
            ServletRequestDataBinder binder) throws Exception {
        super.initBinder(httpServletRequest, binder);

        binder.registerCustomEditor(DocumentFactory.PageLanguage.class,
                EnumEditor.create(DocumentFactory.PageLanguage.class));
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        Command cmd = (Command) o;
        MergerPageSearchRequest searchRequest = createRequest(cmd, errors).
                withPreferredLanguage(DocumentFactory.PageLanguage.DEFAULT).build();
        return findPages(cmd, searchRequest);
    }

    ModelAndView findPages(Command cmd, MergerPageSearchRequest request) {
        this.logger.info("<findPages> query: " + request.getLuceneQuery());
        final MergerPageSearchResponse response =
                this.pageSearchProvider.searchPages(request);
        final List<PageSummary> pages = response.getFoundPages();
        final ListResult listResult = ListResult.create(cmd, SORT_FIELDS,
                DEFAULT_SORT_BY, pages.size());
        if (PAGE_SUMMARY_SORTERS.containsKey(listResult.getSortedBy())) {
            final Comparator<PageSummary> comparator =
                    PAGE_SUMMARY_SORTERS.get(listResult.getSortedBy());
            pages.sort(comparator);
            if (!cmd.isAscending()) {
                Collections.reverse(pages);
            }
            ListHelper.clipPage(cmd,pages);
        } else if (cmd.getOffset() > pages.size()) {
            pages.clear();
        }

        listResult.setCount(pages.size());
        final Map<String, Object> model = new HashMap<>();
        model.put("listinfo", listResult);
        model.put("pagesummaries", pages);
        return new ModelAndView("mscpagesearch", model);
    }

    /**
     * Converts the given {@link de.marketmaker.istar.merger.web.easytrade.block.MscPageSearch.Command}
     * {@code cmd} into a {@link de.marketmaker.istar.merger.provider.pages.MergerPageSearchRequest.Builder}
     * with the corresponding query restrictions set.
     * @param cmd the command containing the query
     * @param errors bind errors
     * @return converted request
     */
    private MergerPageSearchRequest.Builder createRequest(Command cmd, Errors errors) {
        this.logger.info("<createRequest> cmd " + cmd);
        MergerPageSearchRequest.Builder builder = new MergerPageSearchRequest.Builder();
        if (cmd.getCustomQuery() == null) {
            builder.asAndQuery(cmd.isAndQuery());

            if (cmd.getLanguage() != null) {
                builder.requireLanguage(cmd.getLanguage());
            }
            if (cmd.getReferencedPage() != null) {
                for (final int referencedPage : cmd.getReferencedPage()) {
                    builder.requirePointer(referencedPage);
                }
            }
            if (cmd.getText() != null) {
                for (final String text : cmd.getText()) {
                    if (!text.contains(" ")) {
                        builder.requireText(text);
                    }
                    else {
                        // use phrase query for phrases
                        builder.requireTextPhrase(Arrays.asList(text.split("\\s+")));
                    }
                }
            }
            if (cmd.getUsedSymbol() != null) {
                for (final String usedSymbol : cmd.getUsedSymbol()) {
                    final Matcher matcher = Command.SYMBOL_PATTERN.matcher(usedSymbol);
                    if (matcher.find()) {
                        final String symbol = matcher.group(1);
                        final String field = matcher.group(2);
                        if (field == null) {
                            builder.requireSymbol(symbol);
                        }
                        else {
                            try {
                                builder.requireSymbolField(symbol, Integer.parseInt(field));
                            } catch (Exception e) {
                                // the regexp ensures that field does only consist of digits
                            }
                        }
                    }
                    else {
                        errors.rejectValue("usedSymbol", "invalid.symbol", new Object[]{usedSymbol},
                                "Invalid symbol or symbol+field : " + usedSymbol);
                    }
                }
            }
        }
        else {
            // custom query was specified
            final String queryString = cmd.getCustomQuery();
            PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new KeywordAnalyzer());
            final Analyzer textAnalyzer = new WhitespaceAnalyzer();
            analyzer.addAnalyzer(DocumentFactory.PageField.HEADING.fieldName(), textAnalyzer);
            analyzer.addAnalyzer(DocumentFactory.PageField.TITLE.fieldName(), textAnalyzer);
            analyzer.addAnalyzer(DocumentFactory.PageField.STATIC_TEXT.fieldName(), textAnalyzer);
            QueryParser parser = new QueryParser(Version.LUCENE_24, DocumentFactory.PageField.STATIC_TEXT.fieldName(),
                    analyzer);
            Query luceneQuery;
            try {
                luceneQuery = parser.parse(queryString);
                builder.requireQuery(luceneQuery);
            } catch (ParseException e) {
                errors.rejectValue("customQuery", "invalid.query", new Object[]{queryString},
                        "Invalid Lucene query : " + queryString);
                builder = new MergerPageSearchRequest.Builder();
            }
        }
        return builder;
    }

}
