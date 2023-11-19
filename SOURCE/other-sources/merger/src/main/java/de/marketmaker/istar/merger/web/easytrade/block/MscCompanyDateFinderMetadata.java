/*
 * MscCompanyDateFinderMetadata.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 *
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.util.LocalizedUtil;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shows all possible variants of the <code>event</code> parameter used in the query of {@see MSC_CompanyDateFinder}
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */

public class MscCompanyDateFinderMetadata implements AtomController {
    private static final HashSet<Language> languagesSupportedByTemplate
            = new HashSet<>(Arrays.asList(Language.de, Language.en, Language.it, Language.nl, Language.fr));

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final List<Locale> locales = RequestContextHolder.getRequestContext().getLocales();
        final Map<String, Object> model = new HashMap<>();

        //The template uses GERMAN as default, if one of the given languages is not supported.
        //So, there is not need to add a default language.
        final Language language = LocalizedUtil.getLanguage(languagesSupportedByTemplate, locales);
        if(language != null) {
            model.put(language.name(), Boolean.TRUE);
        }

        return new ModelAndView("msccompanydatefindermetadata", model);
    }
}