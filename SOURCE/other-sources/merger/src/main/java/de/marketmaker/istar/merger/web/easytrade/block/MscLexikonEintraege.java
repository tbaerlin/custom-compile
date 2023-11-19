/*
 * MscLexikonEintraege.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.lexicon.LexiconElement;
import de.marketmaker.istar.merger.web.easytrade.ListCommandWithOptionalPaging;
import de.marketmaker.istar.merger.web.easytrade.ListHelper;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.MultiListSorter;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.Collator;
import java.util.*;

/**
 * Fetches lexicon entries for a given initial capital letter.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscLexikonEintraege extends AbstractMscLexicon {
    private final static String SORTFIELD = "titel";
    private final static List<String> SORTFIELDS = Arrays.asList(SORTFIELD);
    private final static Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);
    private static final Comparator<LexiconElement> COMPARATOR = new Comparator<LexiconElement>() {
        public int compare(LexiconElement o1, LexiconElement o2) {
            return GERMAN_COLLATOR.compare(o1.getItem(), o2.getItem());
        }
    };

    public static final class Command extends ListCommandWithOptionalPaging {
        private String initial;
        private String lexiconId;

        /**
         * @return An initial capital letter.
         * @sample A
         * @sample O
         */
        @NotNull
        public String getInitial() {
            return initial;
        }

        /**
         * @param initial An initial capital letter.
         */
        public void setInitial(String initial) {
            this.initial = initial;
        }

        /**
         * @return A lexicon ID. Defaults to {@value null}.
         * @see de.marketmaker.istar.domainimpl.lexicon.LexiconDownloader#DEFAULT_TYPE
         */
        public String getLexiconId() {
            return lexiconId;
        }

        /**
         * param lexiconId a lexicon ID.
         */
        public void setLexiconId(String lexiconId) {
            this.lexiconId = lexiconId;
        }
    }

    public MscLexikonEintraege() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {

        final Command cmd = (Command) o;

        final Map<String, Object> model = new HashMap<>();
        final List<LexiconElement> elements
                = this.lexiconProvider.getElements(cmd.getLexiconId(), cmd.getInitial());

        final ListResult listResult = ListResult.create(cmd, SORTFIELDS, SORTFIELD, elements.size());
        final MultiListSorter mls = new MultiListSorter(COMPARATOR, !listResult.isAscending());
        mls.sort(elements);
        ListHelper.clipPage(cmd, elements);
        listResult.setCount(elements.size());

        model.put("elements", elements);
        model.put("listinfo", listResult);

        return new ModelAndView("msclexikoneintraege", model);
    }
}
