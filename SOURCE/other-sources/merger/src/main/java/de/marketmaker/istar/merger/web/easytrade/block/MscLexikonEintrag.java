/*
 * BndVKRGrafik.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.lexicon.LexiconElement;
import de.marketmaker.istar.merger.provider.NoParameterException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fetches the lexicon entry identified by given ID or any lexicon entry with the given initial capital letter.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscLexikonEintrag extends AbstractMscLexicon {
    public static final class Command {
        private String id;
        private String initial;
        private String lexiconId;

        /**
         * @return An ID of a lexicon entry.
         * @sample 4370
         */
        public String getId() {
            return id;
        }

        /**
         * @param id An ID of a lexicon entry
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * @return An initial capital letter.
         * @sample A
         * @sample O
         */
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
         * param lexiconId a lexiconId. Defaults to {@value null}.
         * @see de.marketmaker.istar.domainimpl.lexicon.LexiconDownloader#DEFAULT_TYPE
         */
        public void setLexiconId(String lexiconId) {
            this.lexiconId = lexiconId;
        }
    }

    public MscLexikonEintrag() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {

        final Command cmd = (Command) o;

        final LexiconElement element;
        if (StringUtils.hasText(cmd.getId())) {
            element = this.lexiconProvider.getElement(cmd.getLexiconId(), cmd.getId());
        }
        else {
            final List<LexiconElement> elements
                    = this.lexiconProvider.getElements(cmd.getLexiconId(), cmd.getInitial());
            if (elements == null || elements.isEmpty()) {
                throw new NoParameterException("no elements for initial " + cmd.getInitial());
            }
            element = elements.get(0);
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("element", element);
        return new ModelAndView("msclexikoneintrag", model);
    }
}
