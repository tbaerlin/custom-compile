/*
 * BndVKRGrafik.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.common.validator.Size;
import de.marketmaker.istar.domain.data.EdgData;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domainimpl.data.NullEdgData;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.certificatedata.CertificateDataProvider;
import de.marketmaker.istar.merger.web.easytrade.chart.BaseImgSymbolCommand;

import static de.marketmaker.istar.merger.web.easytrade.chart.EdgChart.DEFAULT_LOCALE;

/**
 * Returns the url of a standard EDG rating chart for a given symbol (or nil if no
 * EDG data is available).
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EdgChart extends EasytradeChartController {
    public static class Command extends BaseImgSymbolCommand {
        public Command() {
            setLocale(DEFAULT_LOCALE);
        }

        /**
         * @return locale used to render the chart
         * default is "{@value de.marketmaker.istar.merger.web.easytrade.chart.EdgChart#DEFAULT_LOCALE}".
         */
        @Override
        @NotNull
        @RestrictedSet("de,en")
        public String getLocale() {
            return super.getLocale();
        }

    }

    // format required by native edg rendering
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final ModelAndView EMPTY_RESULT = new ModelAndView("edgchart");

    private CertificateDataProvider certificateDataProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public EdgChart() {
        super(Command.class, "edgchart.png");
    }

    public void setCertificateDataProvider(CertificateDataProvider certificateDataProvider) {
        this.certificateDataProvider = certificateDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final BaseImgSymbolCommand cmd = (BaseImgSymbolCommand) o;

        final Instrument instrument;
        try {
            instrument = this.instrumentProvider.identifyInstrument(cmd);
        } catch (UnknownSymbolException e) {
            this.logger.info("<doHandle> unknown: " + cmd);
            return EMPTY_RESULT;
        }


        final EdgData data = new EdgDataMethod(this.certificateDataProvider, instrument).invoke();
        if (data == NullEdgData.INSTANCE) {
            return EMPTY_RESULT;
        }

        final String uri = getURI(cmd, instrument, data);
        if (uri == null) {
            return EMPTY_RESULT;
        }

        return new ModelAndView("edgchart", "request", uri);
    }

    private String getURI(BaseImgSymbolCommand cmd, Instrument instrument, EdgData data) {
        final Integer risk = data.getEdgTopClass();
        final Integer score = data.getEdgTopScore();
        final LocalDate date = data.getEdgRatingDate();
        if (risk == null || score == null || date == null) {
            return null;
        }

        return new StringBuilder(60)
                .append(getMappedChartName(cmd))
                .append("?symbol=").append(instrument.getSymbolIsin())
                .append("&rating=").append(score)
                .append("&risk=").append(risk)
                .append("&date=").append(DTF.print(date))
                .append("&locale=").append(cmd.getLocale().toLowerCase().substring(0, 2))
                .toString();
    }
}