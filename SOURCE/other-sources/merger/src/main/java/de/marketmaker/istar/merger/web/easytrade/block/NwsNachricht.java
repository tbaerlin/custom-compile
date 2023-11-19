/*
 * NwsNachricht.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.queryParser.ParseException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.news.NewsProvider;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.NewsQueryException;
import de.marketmaker.istar.news.backend.News2Document;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;
import de.marketmaker.istar.news.frontend.NewsRecord;
import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsResponse;

/**
 * Returns a particular news item identified by its id, or, if no newsid is specified, the
 * latest available news item.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NwsNachricht extends EasytradeCommandController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Map<InstrumentTypeEnum, Integer> ORDER_BY_TYPE
            = new EnumMap<>(InstrumentTypeEnum.class);

    static {
        ORDER_BY_TYPE.put(InstrumentTypeEnum.IND, 0);
        ORDER_BY_TYPE.put(InstrumentTypeEnum.STK, 1);
        ORDER_BY_TYPE.put(InstrumentTypeEnum.GNS, 1);
        ORDER_BY_TYPE.put(InstrumentTypeEnum.CUR, 2);
        ORDER_BY_TYPE.put(InstrumentTypeEnum.MER, 3);
    }

    private static int getOrderForType(Quote q) {
        final Integer order = ORDER_BY_TYPE.get(q.getInstrument().getInstrumentType());
        return (order != null) ? order : 10;
    }

    static final String QUOTES_KEY = "quotes";

    private NewsProvider newsProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public static class Command {
        private String newsid;

        private String marketStrategy;

        private boolean useShortId;

        private boolean withRawText;

        /**
         * @return id of the requested news, which can be obtained from the result of a news search;
         * leave unspecified to retrieve latest available news
         */
        public String getNewsid() {
            return newsid;
        }

        public void setNewsid(String newsid) {
            this.newsid = newsid;
        }

        /**
         * @return name of the marketStrategy used to determine a representative quote for each of the
         * instruments associated with the returned news item. Leave unspecified to use the default,
         * client-specific strategy.
         */
        public String getMarketStrategy() {
            return this.marketStrategy;
        }

        public void setMarketStrategy(String marketStrategy) {
            this.marketStrategy = marketStrategy;
        }

        @MmInternal
        public boolean isUseShortId() {
            return useShortId;
        }

        public void setUseShortId(boolean useShortId) {
            this.useShortId = useShortId;
        }

        @MmInternal
        public boolean isWithRawText() {
            return withRawText;
        }

        public void setWithRawText(boolean withRawText) {
            this.withRawText = withRawText;
        }
    }

    public NwsNachricht() {
        super(Command.class);
    }

    public void setNewsProvider(NewsProvider newsProvider) {
        this.newsProvider = newsProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command c = (Command) o;

        final NewsRequest nr = new NewsRequest();
        if (StringUtils.hasText(c.getNewsid())) {
            if (c.isUseShortId()) {
                setQuery(c, nr);
            }
            else {
                nr.setNewsids(Arrays.asList(c.getNewsid()));
            }
        }
        else {
            nr.setCount(1);
            nr.setOffset(0);
            nr.setWithText(true);
            nr.setWithHitCount(false);
        }
        nr.setWithRawText(c.isWithRawText());


        final Map<String, Object> model = new HashMap<>();

        final NewsRecord item = getNews(nr);
        if (item != null) {
            model.put("item", item);

            final List<Quote> quotes = getQuotes(item, c);
            if (!quotes.isEmpty()) {
                model.put(QUOTES_KEY, quotes);
            }
        }

        model.put("useShortId", c.isUseShortId());
        model.put("withRawText", c.isWithRawText());

        return new ModelAndView("nwsnachricht", model);
    }

    private void setQuery(Command c, NewsRequest nr) {
        try {
            nr.setQuery(NewsIndexConstants.FIELD_SHORTID + ":"
                    + News2Document.encodeShortid(Integer.parseInt(c.getNewsid())));
        } catch (ParseException e) {
            // should never happen,
            throw new NewsQueryException(e);
        } catch (NumberFormatException e) {
            throw new BadRequestException("invalid newsid '" + c.getNewsid() + "'");
        }
    }

    private NewsRecord getNews(NewsRequest nr) {
        final NewsResponse news = this.newsProvider.getNews(nr, false);
        if (news.isValid() && !news.getRecords().isEmpty()) {
            return news.getRecords().get(0);
        }
        return null;
    }

    private List<Quote> getQuotes(NewsRecord item, Command c) {
        final Set<String> instrumentIds = item.getAttributes().get(NewsAttributeEnum.IID);
        if (instrumentIds == null || instrumentIds.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Long> iids = new ArrayList<>(instrumentIds.size());
        for (String instrumentId : instrumentIds) {
            iids.add(Long.parseLong(instrumentId));
        }

        final List<Instrument> instruments = this.instrumentProvider.identifyInstruments(iids);
        if (instruments == null) {
            return Collections.emptyList();
        }

        return getQuotes(instruments, c);
    }

    private List<Quote> getQuotes(List<Instrument> instruments, Command c) {
        final List<Quote> result = new ArrayList<>(instruments.size());
        for (Instrument i : instruments) {
            if (i != null) {
                try {
                    final Quote quote = this.instrumentProvider.getQuote(i, null, c.getMarketStrategy());
                    if (quote != null) {
                        result.add(quote);
                    }
                } catch (UnknownSymbolException e) {
                    // ignore, user has no permission to see any quote for i, just leave it out.
                    // TODO: wouldn't it be better to modify market strategy to return null?
                }
            }
        }

        if (result.size() > 1) {
            sortQuotes(result);
        }
        return result;
    }

    private void sortQuotes(List<Quote> result) {
        final QuoteNameStrategy qns = RequestContextHolder.getRequestContext().getQuoteNameStrategy();
        result.sort(new Comparator<Quote>() {
            @Override
            public int compare(Quote q1, Quote q2) {
                final int cmp = getOrderForType(q1) - getOrderForType(q2);
                return (cmp != 0) ? cmp : qns.getName(q1).compareTo(qns.getName(q2));
            }
        });
    }

}
