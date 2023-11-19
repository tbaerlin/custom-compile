/*
 * MscRatingData.java
 *
 * Created on 03.11.2011 17:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.RatingData;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.RatingDataProvider;
import de.marketmaker.istar.merger.provider.RatingDataRequest;
import de.marketmaker.istar.merger.provider.RatingDataResponse;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscRatingData extends EasytradeCommandController {
    private EasytradeInstrumentProvider instrumentProvider;

    private RatingDataProvider ratingDataProvider;

    public MscRatingData() {
        super(DefaultSymbolCommand.class);
    }

    public void setRatingDataProvider(RatingDataProvider ratingDataProvider) {
        this.ratingDataProvider = ratingDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final SymbolCommand cmd = (SymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);

        long iid = quote.getInstrument().getId();
        final RatingDataResponse ratingData
                = this.ratingDataProvider.getData(new RatingDataRequest(iid));

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("ratingData", withProfile(ratingData.getData(iid),
                RequestContextHolder.getRequestContext().getProfile()));
        return new ModelAndView("mscratingdata", model);
    }

    private static final Pattern P_RATING_AGENCY = Pattern.compile("getRating(Fitch|Moodys|SnP).+");

    private static final Map<String, Selector> PERM;

    static {
        PERM = new HashMap<>();
        PERM.put("Fitch", Selector.RATING_FITCH);
        PERM.put("Moodys", Selector.RATING_MOODYS);
        PERM.put("SnP", Selector.RATING_SuP);
    }

    static RatingData withProfile(final RatingData delegate, final Profile profile) {
        // might be considered as a general approach (annotation on domain data interfaces?)
        return (RatingData) Proxy.newProxyInstance(RatingData.class.getClassLoader(),
                new Class[]{RatingData.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method,
                            Object[] args) throws Throwable {
                        if (delegate == null) {
                            return null;
                        }
                        final Matcher matcher = P_RATING_AGENCY.matcher(method.getName());
                        if (matcher.matches()) {
                            if (profile.isAllowed(PERM.get(matcher.group(1)))) {
                                return method.invoke(delegate, args);
                            }
                            else {
                                return null;
                            }
                        }
                        else {
                            return method.invoke(delegate, args);
                        }
                    }
                });
    }
}
