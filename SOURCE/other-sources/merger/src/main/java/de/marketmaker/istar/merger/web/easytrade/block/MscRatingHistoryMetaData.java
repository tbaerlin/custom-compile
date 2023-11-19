/*
 * MscRatingHistory.java
 *
 * Created on 13.09.12 09:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.rating.history.RatingHistoryProvider;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaItem;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * Retrieves rating history meta data.
 * <p>
 * Rating history meta data contains all available rating systems and their respective rating
 * values and counts found in rating histories for all available instruments.
 * </p>
 * @author zzhao
 */
public class MscRatingHistoryMetaData implements AtomController {

    private static final Map<String, Selector[]> PERM;

    static {
        PERM = new HashMap<>();
        PERM.put(RatioFieldDescription.morningstars.name().toLowerCase(), new Selector[] {Selector.RATING_MORNINGSTAR});
        PERM.put(RatioFieldDescription.ratingFeri.name().toLowerCase(), new Selector[] {Selector.RATING_FERI});
        PERM.put(RatioFieldDescription.ratingFitchShortTerm.name().toLowerCase(), new Selector[] {Selector.RATING_FITCH});
        PERM.put(RatioFieldDescription.ratingFitchLongTerm.name().toLowerCase(), new Selector[] {Selector.RATING_FITCH});
        PERM.put(RatioFieldDescription.ratingMoodysShortTerm.name().toLowerCase(), new Selector[] {Selector.RATING_MOODYS});
        PERM.put(RatioFieldDescription.ratingMoodysLongTerm.name().toLowerCase(), new Selector[] {Selector.RATING_MOODYS});
        PERM.put(RatioFieldDescription.ratingSnPShortTerm.name().toLowerCase(), new Selector[] {Selector.RATING_SuP});
        PERM.put(RatioFieldDescription.ratingSnPLongTerm.name().toLowerCase(), new Selector[] {Selector.RATING_SuP});
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private RatingHistoryProvider ratingHistoryProvider;

    public void setRatingHistoryProvider(RatingHistoryProvider ratingHistoryProvider) {
        this.ratingHistoryProvider = ratingHistoryProvider;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws Exception {
        final Map<String, List<FinderMetaItem>> listMap = this.ratingHistoryProvider.getRatingHistoryMetaData();
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final HashMap<String, Object> model = new HashMap<>();
        model.put("systems", listMap.entrySet().stream()
                .filter(e -> {
                    final String ck = e.getKey().trim().toLowerCase();
                    if (PERM.get(ck) == null) {
                        this.logger.warn("<handleRequest> no permission defined {}", ck);
                        return false;
                    }
                    return isAllowed(profile, PERM.get(ck));
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return new ModelAndView("mscratinghistorymetadata", model);
    }

    private static boolean isAllowed(Profile p, Selector... selectors) {
        for (Selector s : selectors) {
            if (p.isAllowed(s)) {
                return true;
            }
        }
        return false;
    }
}
