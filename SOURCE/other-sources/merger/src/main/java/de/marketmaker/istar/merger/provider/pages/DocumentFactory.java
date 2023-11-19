package de.marketmaker.istar.merger.provider.pages;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.feed.pages.PageData;

/**
 * DocumentFactory.java
 * Created on 15.07.2010 15:50:05
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

/**
 * This class provides static methods for creating
 * {@link org.apache.lucene.document.Document}s representing
 * a page.
 * @author Sebastian Wild
 */
public final class DocumentFactory {
    /**
     * selector value for pages without selector(s)
     */
    static final String PUBLIC_SELECTOR = "0";

    /**
     * This class lists all {@link org.apache.lucene.document.Field}s used for a page.
     * When constructing a custom query, consider using these enum values to get the
     * correct name of those fields, e.g.<br/>
     * {@code PageField.TITLE.fieldName()} <br/>
     * returns the name of field used to hold the PDL-title of the page.
     */
    public static class PageField {
        /**
         * The page number (id) of the page. Note that some pages exist in several languages,
         * so only the tuple of pagenumber and language is unique.
         */
        public static final PageField PAGE_NUMBER
                = new PageField("pagenumber", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS,
                Field.TermVector.NO, 1.0f);

        /**
         * The PDL-title of the page. Does not always exist.
         */
        public static final PageField TITLE
                = new PageField("title", Field.Store.YES, Field.Index.ANALYZED_NO_NORMS,
                Field.TermVector.WITH_POSITIONS, 2.0f);

        /**
         * The head line of this page as guessed by {@link DocumentFactory#guessHeading(PdlPage)}.
         * For most pages, this gives a concise description of their content ...
         * for some it doesn't.
         */
        public static final PageField HEADING
                = new PageField("heading", Field.Store.YES, Field.Index.ANALYZED_NO_NORMS,
                Field.TermVector.WITH_POSITIONS, 1.5f);

        /**
         * Multi-values field containing the referenced symbols and fields.
         * We store those by concatenating the symbol's id with
         * {@link de.marketmaker.istar.merger.provider.pages.DocumentFactory#SYMBOL_AND_SYMBOLFIELD_SEPARATOR}
         * and the field number. E.g. referencing field 80 of symbol 25548.qid gets stored as
         * {@code "25548.qid^80"} if
         * {@link de.marketmaker.istar.merger.provider.pages.DocumentFactory#SYMBOL_AND_SYMBOLFIELD_SEPARATOR}
         * is {@code "^"}.
         * <br/><br/>
         */
        public static final PageField KEY_FIELD
                = new PageField("keyfield", Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS,
                Field.TermVector.NO, 1.0f);

        /**
         * Multi-values field containing the referenced symbols, <em>without</em> fields.
         * Use this field to search for a symbol, indifferent to used fields. <br/>
         * (The same functionality is possible using a {@link org.apache.lucene.search.PrefixQuery}
         * on the field {@link #KEY_FIELD}, but such queries are much slower.)
         */
        public static final PageField KEY
                = new PageField("key", Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS,
                Field.TermVector.YES, 1.0f);

        /**
         * Multi-values field containing selectors
         */
        public static final PageField SELECTOR
                = new PageField("selector", Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS,
                Field.TermVector.NO, 1.0f);

        /**
         * Multi-values field containing referenced page numbers.
         */
        public static final PageField POINTER
                = new PageField("pointer", Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS,
                Field.TermVector.NO, 1.0f);

        /**
         * The language of static text in this page. See {@link de.marketmaker.istar.merger.provider.pages.DocumentFactory.PageLanguage}
         * for possible choices.
         */
        public static final PageField LANGUAGE
                = new PageField("language", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS,
                Field.TermVector.NO, 2.0f);

        /**
         * Multivalued field containing all static text entries. The entries are guaranteed to
         * appear in order, so phrase queries should work as expected.
         */
        public static final PageField STATIC_TEXT
                = new PageField("statictext", Field.Store.NO, Field.Index.ANALYZED_NO_NORMS,
                Field.TermVector.WITH_POSITIONS, 1.0f);


        private final String fieldName;

        private final Field.Store storePolicy;

        private final Field.Index indexPolicy;

        private final Field.TermVector termVectorPolicy;

        private final float boost;

        private PageField(String fieldName, Field.Store storePolicy, Field.Index indexPolicy,
                Field.TermVector termVectorPolicy, float boost) {
            this.fieldName = fieldName;
            this.storePolicy = storePolicy;
            this.indexPolicy = indexPolicy;
            this.termVectorPolicy = termVectorPolicy;
            this.boost = boost;
        }

        public Field.Store getStorePolicy() {
            return storePolicy;
        }

        public Field.Index getIndexPolicy() {
            return indexPolicy;
        }

        public Field.TermVector getTermVectorPolicy() {
            return termVectorPolicy;
        }

        public String fieldName() {
            return fieldName;
        }

        public float getBoost() {
            return boost;
        }

    }

    /**
     * This enum lists possible choices for the language of static text in a page.
     */
    public enum PageLanguage {
        /**
         * {@code DEFAULT} language means, we take the text from the
         * <tt>text</tt>-field in pages, but explicitly state, that there
         * there is a German version of this page, as well.
         */
        DEFAULT,
        /**
         * {@code GERMAN} language means the <tt>textg</tt>-field, i.e. the
         * explicitly German-marked version of a page. This version
         * should only exist, if there is a default version, as well, and it should
         * only contain German text.
         */
        GERMAN,
        /**
         * {@code UNSPECIFIED} language is used, if only
         * <b>one</b> version of a page is present. This version may be
         * German or English, we do not state which. However, it is guaranteed that
         * for such pages, <tt>textg</tt> is {@code null}.
         */
        UNSPECIFIED
    }


    /**
     * This separator string is used to separate symbol and field id.
     */
    public static final String SYMBOL_AND_SYMBOLFIELD_SEPARATOR = "^";

    /**
     * The maximal number of lines considered to be the heading of a page.
     * Used in guessHeading.
     */
    public static final int NUMBER_OF_LINES_IN_HEADING = 2;

    private static PdlPageFactory FACTORY = new PdlPageFactory();

    private static final Pattern LETTER_SPACING = Pattern.compile("(^| )((\\p{javaLetter}( |$)){3,})");

    /**
     * ...only used for testing...
     * Convenience overload for
     * {@link de.marketmaker.istar.merger.provider.pages.DocumentFactory#createDocument(
     *de.marketmaker.istar.feed.pages.PageData, boolean)}
     * @param page
     * @return
     */
    static Document createDocument(PageData page) {
        return createDocument(page, false);
    }

    /**
     * ...only used for testing...
     * Creates a Lucene Document from the given page. {@code useTextG}
     * can be used to select the language version. During indexing,
     * you will typically call this method twice, if the page has
     * an alternative German text.
     * @param page the page to create a Document from
     * @param useTextG whether to use alternative German text
     * @return Document with contents from {@code page}
     */
    static Document createDocument(PageData page, boolean useTextG) {
        if (page == null) {
            throw new NullPointerException("Page may not be null.");
        }
        final String content = useTextG ? page.getTextg() : page.getText();
        if (content == null) {
            throw new IllegalArgumentException("Page with id " +
                    page.getId() + " does not have " +
                    (useTextG ? "german" : "default") + " version.");
        }

        PdlPage parsedPage = FACTORY.createPage(String.valueOf(page.getId()), content);

        return createDocument(page, useTextG, parsedPage);
    }


    static Document createDocument(PageData page, boolean useTextG, PdlPage parsedPage) {
        final Document result = new Document();
        result.add(createField(PageField.PAGE_NUMBER, String.valueOf(page.getId())));
        result.add(createField(PageField.TITLE, parsedPage.getTitle()));
        result.add(createField(PageField.HEADING, guessHeading(parsedPage)));
        result.add(createField(PageField.LANGUAGE, getLanguage(page, useTextG).name()));

        addSelectors(result, page.getSelectors());

        /* For the actual contents of the page, we iterate over         *
         * all found PDL-objects and add them as the appropriate field. *
         * Note that Lucene supports multi-valued fields.               */
        for (PdlObject obj : parsedPage.getObjects()) {
            switch (obj.getType()) {
                case PdlObject.TYPE_TEXT:
                    result.add(createField(PageField.STATIC_TEXT, removeLetterSpacing(obj.getContent())));
                    break;
                case PdlObject.TYPE_PAGEPOINTER:
                    result.add(createField(PageField.POINTER, obj.getContent().trim()));
                    break;
                case PdlObject.TYPE_DATA:
                    assert (obj instanceof PdlDataObject);
                    PdlDataObject dataObj = (PdlDataObject) obj;
                    result.add(createField(PageField.KEY, dataObj.getRequestObject()));
                    result.add(createField(PageField.KEY_FIELD, dataObj.getRequestObject() +
                            SYMBOL_AND_SYMBOLFIELD_SEPARATOR + dataObj.getFieldId()));
                    break;
                default:
                    throw new IllegalArgumentException("Page with id " +
                            page.getId() + " contains a PDL object of illegal " +
                            "type " + obj.getType());
            }
        }
        return result;
    }

    private static void addSelectors(Document result, final Set<String> selectors) {
        if (selectors == null) {
            result.add(createField(PageField.SELECTOR, PUBLIC_SELECTOR));
            return;
        }
        for (String selector : selectors) {
            result.add(createField(PageField.SELECTOR, EntitlementsVwd.normalize(selector)));
        }
    }

    private static PageLanguage getLanguage(PageData page, boolean useTextG) {
        if (page.getTextg() == null || page.getText() == null) {
            return PageLanguage.UNSPECIFIED;
        }
        return useTextG ? PageLanguage.GERMAN : PageLanguage.DEFAULT;
    }

    private static Field createField(PageField field, String content) {
        Field result = new Field(field.fieldName(), content,
                field.getStorePolicy(), field.getIndexPolicy(),
                field.getTermVectorPolicy());
        result.setBoost(field.getBoost());
        return result;
    }


    /**
     * This method uses a heuristic to extract the heading of
     * page. Although most pages have a given PDL title, some do not.
     * This method only looks at text <em>inside</em> the page.
     * @param page the page to guess the heading for
     * @return the heading found <em>inside</em> page,
     * or "", if none was found.
     */
    static String guessHeading(final PdlPage page) {
        /*
         * Headings seem to occur in two different variants:
         *  a) lineSpanHeading:
         *     One or two lines spreading the whole page width, in centered and inverse style.
         *  b) firstLineCenterHeading:
         *     A manually centered text in the 0th line (between page number and index link).
         * We always try to detect both variants, but prefer a) if both is found.
         * The reason is simple: Detection of a) works more reliably.
         */
        String lineSpanHeading = null;
        String firstLineCenterHeading = "";
        int spanHeadingLines = 0;
        for (final PdlObject obj : page.getObjects()) {
            if (obj.getType() != PdlObject.TYPE_TEXT) {
                continue;
            }
            final String content = obj.getContent().trim();
            if (obj.getY() == 0 && /* first line heading */
                    !"INDEX".equals(content) && !content.endsWith("vwd") &&
                    !content.matches("\\d+")) {
                firstLineCenterHeading = content;
            }
            else {
                if (spanHeadingLines >= NUMBER_OF_LINES_IN_HEADING) break;
                if (obj.hasAttribute(PdlObject.PAGE_ATTR_DISPLAY_INVERSE) &&
                        obj.hasAttribute(PdlObject.PAGE_ATTR_ALIGN_CENTER) &&
                        obj.getX() == 1 && obj.getY() <= 2 &&
                        obj.getDisplayWidth() > page.getWidth() / 2 + 1 && /* span heading */
                        !"INDEX".equals(content) && !content.endsWith("vwd") &&
                        !content.matches("\\d+")) {
                    lineSpanHeading =
                            (lineSpanHeading == null ? "" : lineSpanHeading) + content + " ";
                    spanHeadingLines++;
                }
            }
        }
        // prefer span heading over first line center heading
        final String result = (lineSpanHeading == null) ? firstLineCenterHeading : lineSpanHeading;
        return removeLetterSpacing(result);
    }

    /**
     * Removes letterspaced writing from text, if present.
     * A rather crude heuristic is used to detect letterspacing:
     * Once three single-character-words are found, letterspacing
     * is assumed and spaces are removed until there is a
     * word with more than one character again.
     * <br />
     * If no letterspacing is found, text is result.
     * @param text the text to convert to non-letterspaced.
     * May not be null.
     * @return text without letterspacing
     */
    static String removeLetterSpacing(final String text) {
        final Matcher m = LETTER_SPACING.matcher(text);
        if (!m.find()) {
            return text;
        }
        final StringBuffer sb = new StringBuffer(text.length());
        do {
            final String g = m.group();
            final String replacement =
                    (g.startsWith(" ") ? " " : "") + g.replaceAll(" ", "") + (g.endsWith(" ") ? " " : "");

            m.appendReplacement(sb, replacement);
        } while (m.find());
        m.appendTail(sb);
        return sb.toString().replaceAll("\\s{2,}", " ");
    }

    /**
     * non-instantiable
     */
    private DocumentFactory() {
        throw new AssertionError();
    }
}
