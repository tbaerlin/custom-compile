/*
 * EerMetaData.java
 *
 * Created on 21.03.12 10:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.econoday;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.econoday.CountryNameProvider;
import de.marketmaker.istar.merger.provider.econoday.EconodayProvider;
import de.marketmaker.istar.merger.provider.econoday.Event;
import de.marketmaker.istar.merger.provider.econoday.FrequencyEnum;
import de.marketmaker.istar.merger.web.PermissionDeniedException;
import de.marketmaker.istar.merger.web.easytrade.block.AtomController;

/**
 * Provides meta data available in economic calendar service.
 * <p>
 * Economic calendar is consisted of series of major economic events. Those events have attributes
 * like country, code or frequency when they occur. Accordingly they can also be searched using
 * these attributes. This service delivers all available value candidates under those attributes to
 * help build queries.
 * </p>
 *
 * @author zzhao
 */
public class EdMetaData implements AtomController {

    private EconodayProvider econodayProvider;

    private CountryNameProvider countryNameProvider;

    public void setEconodayProvider(EconodayProvider econodayProvider) {
        this.econodayProvider = econodayProvider;
    }

    public void setCountryNameProvider(CountryNameProvider countryNameProvider) {
        this.countryNameProvider = countryNameProvider;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (!profile.isAllowed(Selector.ECONODAY)) {
            throw new PermissionDeniedException(Selector.ECONODAY.getId());
        }

        final List<Event> events = this.econodayProvider.getAvailableEvents();

        final Map<String, String> countryMap = new HashMap<>(30);
        final Map<String, String> codeMap = new HashMap<>(300);
        final Map<Integer, String> frequencyMap = new HashMap<>(10);
        for (FrequencyEnum frequencyEnum : FrequencyEnum.values()) {
            frequencyMap.put(frequencyEnum.getValue(), frequencyEnum.toString());
        }

        final Map<String, Event> eventMap = new HashMap<>(300);

        for (Event event : events) {
            countryMap.put(event.getCountry(), getCountryName(event.getCountry()));
            codeMap.put(event.getCode(), event.getName());
            eventMap.put(event.getCode(), event);
        }

        final Map<String, Object> model = new HashMap<>(5);
        model.put("country", countryMap);
        model.put("eventCode", codeMap);
        model.put("frequency", frequencyMap);
        model.put("event", eventMap);

        return new ModelAndView("edmetadata", model);
    }

    private String getCountryName(String symbol) {
        String cn = this.countryNameProvider.getCountryName(symbol, Language.en);
        return StringUtils.isBlank(cn) ? symbol : cn;
    }
}
