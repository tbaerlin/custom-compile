/*
 * BndVKRGrafik.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Fetches a list of initial letters that have lexicon entries.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscLexikonInitialen extends AbstractMscLexicon {
    public static final class Command {
        private String lexiconId;

        /**
         * @return A lexicon ID. Defaults to {@value null}.
         * @see de.marketmaker.istar.domainimpl.lexicon.LexiconDownloader#DEFAULT_TYPE
         */
        public String getLexiconId() {
            return lexiconId;
        }

        /**
         * @param lexiconId A lexicon ID.
         * @see de.marketmaker.istar.domainimpl.lexicon.LexiconDownloader#DEFAULT_TYPE
         */
        public void setLexiconId(String lexiconId) {
            this.lexiconId = lexiconId;
        }
    }

    public MscLexikonInitialen() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws Exception {
        return new ModelAndView("msclexikoninitialen", "initialen",
                this.lexiconProvider.getInitials(((Command)o).getLexiconId()));
    }
}
