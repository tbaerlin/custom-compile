/*
 * PdfFactsheetMetadata.java
 *
 * Created on 2/25/15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.spring.MessageSourceFactory;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Stefan Willenbrock
 */
public class PdfFactsheetMetadata implements AtomController {

    private static final MessageSource MESSAGES = MessageSourceFactory.create(PdfFactsheetMetadata.class);

    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String name;

        private final List<Section> sections;

        public Item(String name) {
            this.name = name;
            this.sections = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public List<Section> getSections() {
            return sections;
        }

        protected Item addSection(String key, String type, String label, boolean check) {
            if (check) {
                addSection(key, type, label);
            }
            return this;
        }

        protected Item addSection(String key, String type, String label) {
            sections.add(new Section(key, type, label));
            return this;
        }
    }

    public static class Section implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String key;

        private final String type;

        private final String label;

        public Section(String key, String type, String label) {
            this.key = key;
            this.type = type;
            this.label = label;
        }

        public String getKey() {
            return key;
        }

        public String getType() {
            return type;
        }

        public String getLabel() {
            return label;
        }
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        final Profile profile = requestContext.getProfile();
        final Locale locale = requestContext.getLocale();

        final List<Item> items = new ArrayList<>();
        final String booleanType = "Boolean";

        items.add(new Item(InstrumentTypeEnum.STK.name())
                .addSection("convensys", booleanType, getMessage("labelConvensys", locale), profile.isAllowed(Selector.CONVENSYS_I))
                .addSection("convensysShares", booleanType, getMessage("labelConvensysShares", locale), profile.isAllowed(Selector.CONVENSYS_I))
                .addSection("estimates", booleanType, getMessage("labelEstimates", locale), profile.isAllowed(Selector.THOMSONREUTERS_ESTIMATES_DZBANK) || profile.isAllowed(Selector.FACTSET))
                .addSection("screener", booleanType, getMessage("labelScreener", locale), profile.isAllowed(Selector.SCREENER))
                .addSection("showAlternatives", booleanType, getMessage("labelShowAlternativesStk", locale), profile.isAllowed(Selector.PRODUCTALTERNATIVES)));

        items.add(new Item(InstrumentTypeEnum.CER.name())
                .addSection("showAlternatives", booleanType, getMessage("labelShowAlternativesCer", locale))
                .addSection("singlePage", booleanType, getMessage("labelSinglePage", locale)));

        final Map<String, Object> model = new HashMap<>();
        model.put("items", items);
        return new ModelAndView("pdffactsheetmetadata", model);
    }
    
    private static String getMessage(String msg, Locale locale) {
        return MESSAGES.getMessage("pdffactsheet." + msg, null, locale);
    }
}
