/*
 * StkAnalystenschaetzung.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.Pattern;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.estimates.EstimatesProvider;

/**
 * Returns a list of instruments which estimation records were last updated not earlier than the given reference date.
 * <p>The records are identified by the vwd specific instrument id.</p>
 * <p>If the reference date is not set, all records are returned.</p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkEstimatesDirectory extends EasytradeCommandController {

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

    private EstimatesProvider estimatesProvider;

    public StkEstimatesDirectory() {
        super(Command.class);
    }

    public void setEstimatesProvider(EstimatesProvider estimatesProvider) {
        this.estimatesProvider = estimatesProvider;
    }

    public static class Command {
        private String referenceDate;

        /**
         * @return The reference date as a timestamp according to ISO 8601:2004 in the format <code>YYYY-MM-DD<b>T</b>hh:mm:ss</code>, e.g. <code>2011-03-01T13:00:00</code>.
         * @sample 2011-03-01T13:00:00
         */
        @Pattern(regex = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")
        public String getReferenceDate() {
            return referenceDate;
        }

        public void setReferenceDate(String referenceDate) {
            this.referenceDate = referenceDate;
        }
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;
        final Map<String, Object> model = new HashMap<>();
        final List<Long> symbols = this.estimatesProvider.getEstimatesDirectory(
                RequestContextHolder.getRequestContext().getProfile(), getReferenceDate(cmd));

        model.put("symbols", symbols);
        return new ModelAndView("stkestimatesdirectory", model);
    }

    private DateTime getReferenceDate(Command cmd) {
        final String str = cmd.getReferenceDate();
        return (null == str) ? null : DTF.parseDateTime(str);
    }
}
