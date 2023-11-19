/*
 * StkStammdaten.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.domain.data.ScreenerUpDownData;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.provider.screener.ScreenerProvider;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Returns for a given region a list of securities with <i>theScreener's</i> upside rating and trend direction.
 * <p>The number of entries can be limited. The limit applies for each trend direction.</p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkScreenerUpDown extends EasytradeCommandController {
    protected EasytradeInstrumentProvider instrumentProvider;
    private ScreenerProvider screenerProvider;

    public StkScreenerUpDown() {
        super(Command.class);
    }

    public void setScreenerProvider(ScreenerProvider screenerProvider) {
        this.screenerProvider = screenerProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public static class Command {
        private String region;
        private int count = Integer.MAX_VALUE;

        /**
         * @return The region
         * @sample EP
         * @sample US
         */
        @NotNull
        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        /**
         * @return Limits the number of entries.
         */
        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        checkPermission(Selector.SCREENER);

        final Command cmd = (Command) o;

        final ScreenerUpDownData data = this.screenerProvider.getUpDownData(cmd.getRegion());

        final List<Instrument> ups = getInstruments(data.getUps(), cmd.getCount());
        final List<Instrument> downs = getInstruments(data.getDowns(), cmd.getCount());

        final Map<String, Object> model = new HashMap<>();
        model.put("region", data.getRegion());
        model.put("referencedate", data.getReferencedate().toDateTimeAtMidnight());
        model.put("ups", ups);
        model.put("downs", downs);
        model.put("upsStars", getStars(data.getUpsStars(), cmd.getCount()));
        model.put("downsStars", getStars(data.getDownsStars(), cmd.getCount()));

        return new ModelAndView("stkscreenerupdown", model);
    }

    private List<Integer> getStars(List<Integer> stars, int count) {
        if (stars.size() <= count) {
            return stars;
        }
        return stars.subList(0, count);
    }

    private List<Instrument> getInstruments(List<Long> iids, int count) {
        final List<Instrument> instruments = this.instrumentProvider.identifyInstruments(iids);
        CollectionUtils.removeNulls(instruments);
        if (instruments.size() <= count) {
            return instruments;
        }

        return instruments.subList(0, count);
    }
}
