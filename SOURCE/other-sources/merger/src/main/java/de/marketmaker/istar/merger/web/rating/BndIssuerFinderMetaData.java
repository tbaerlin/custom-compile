/*
 * BNDIssuerFinderMetaData.java
 *
 * Created on 07.05.12 14:48
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.rating;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingDescriptor;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingMetaDataKey;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingProvider;
import de.marketmaker.istar.merger.provider.rating.RatingSource;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaItem;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;

/**
 * Retrieves meta data related with bond issuer finder.
 * <p>
 * Bond issuer meta data contains available issuer rating fields that are relevant for categorizing
 * and querying issuers. This block delivers both the fields and their values.
 * </p>
 *
 * @author zzhao
 */
public class BndIssuerFinderMetaData extends EasytradeCommandController {

    public static class Command {
        private boolean withDetailedSymbol;

        public boolean isWithDetailedSymbol() {
            return withDetailedSymbol;
        }

        public void setWithDetailedSymbol(boolean withDetailedSymbol) {
            this.withDetailedSymbol = withDetailedSymbol;
        }
    }

    private IssuerRatingProvider provider;

    public BndIssuerFinderMetaData() {
        super(Command.class);
    }

    public void setProvider(IssuerRatingProvider provider) {
        this.provider = provider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) throws Exception {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final Locale locale = RequestContextHolder.getRequestContext().getLocale();
        final Map<IssuerRatingMetaDataKey, List<Object>> metaData = this.provider.getMetaData(
                ((Command) o).isWithDetailedSymbol());

        return new ModelAndView("bndissuerfindermetadata", createModel(profile, locale, metaData));
    }

    static Map<String, Object> createModel(Profile profile, Locale locale,
                                           Map<IssuerRatingMetaDataKey, List<Object>> metaData) {
        final HashMap<String, Object> result = new HashMap<>(10);
        for (Map.Entry<IssuerRatingMetaDataKey, List<Object>> entry : metaData.entrySet()) {
            final IssuerRatingDescriptor desc = entry.getKey().getDesc();
            if (desc.accept(profile)) {
                if (desc == IssuerRatingDescriptor.SOURCE) {
                    result.put(entry.getKey().getName(), toFinderItemList(profileFilter(profile, entry.getValue())));
                } else if (desc == IssuerRatingDescriptor.COUNTRYISO) {
                    result.put(entry.getKey().getName(), localize(locale, entry.getValue()));
                } else {
                    result.put(entry.getKey().getName(), toFinderItemList(entry.getValue()));
                }
            }
        }
        return result;
    }

    private static List<RatingSource> profileFilter(Profile profile, List<Object> list) {
        final List<RatingSource> result = new ArrayList<>(list.size());
        for (Object obj : list) {
            final RatingSource src = (RatingSource) obj;
            if (src.accept(profile)) {
                result.add(src);
            }
        }
        return result;
    }

    private static List<FinderMetaItem> localize(Locale locale, Collection<?> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        final List<FinderMetaItem> result = new ArrayList<>(list.size());
        for (Object obj : list) {
            final String countryIso = String.valueOf(obj);
            result.add(new FinderMetaItem(countryIso, localize(locale, countryIso), 0));
        }
        result.sort(new Comparator<FinderMetaItem>() {
            @Override
            public int compare(FinderMetaItem left, FinderMetaItem right) {
                return left.getName().compareTo(right.getName());
            }
        });
        return result;
    }

    static String localize(Locale locale, String countryIso) {
        return new Locale("", countryIso).getDisplayCountry(locale);
    }

    private static List<FinderMetaItem> toFinderItemList(Collection<?> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        final List<FinderMetaItem> ret = new ArrayList<>(list.size());
        for (Object obj : list) {
            if (obj instanceof RatingSource) {
                final RatingSource source = (RatingSource) obj;
                ret.add(new FinderMetaItem(source.name(), source.getFullName(), 0));
            }
            else {
                ret.add(new FinderMetaItem(String.valueOf(obj), String.valueOf(obj), 0));
            }
        }

        return ret;
    }
}
