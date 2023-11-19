/*
 * PfOrderAendern.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 *
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Pattern;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.merger.user.UpdateOrderCommand;
import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * Edit the attributes of an order
 */

public class PfOrderAendern extends UserHandler {
    private static final DateTime FIRST_ORDER_DATE = new YearMonthDay(1950, 1, 1).toDateTimeAtMidnight();

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    public static class Command extends UserCommandImpl {
        private Long portfolioid;
        private Long orderid;
        private BigDecimal amount;
        private BigDecimal price;
        private BigDecimal exchangeRate;
        private BigDecimal charge;
        private String date;
        private String ordertyp;


        /**
         * @return id of the affected portfolio
         */
        @NotNull
        public Long getPortfolioid() {
            return portfolioid;
        }

        public void setPortfolioid(Long portfolioid) {
            this.portfolioid = portfolioid;
        }

        /**
         * @return order date
         */
        @NotNull
        @Pattern(regex="\\d{4}-\\d{2}-\\d{2}")
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        /**
         * @return id of the order to edit
         */
        @NotNull
        public Long getOrderid() {
            return orderid;
        }

        public void setOrderid(Long orderid) {
            this.orderid = orderid;
        }

        /**
         * @return quantity of shares
         */
        @NotNull
        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        /**
         * @return order fees
         */
        public BigDecimal getCharge() {
            return charge;
        }

        public void setCharge(BigDecimal charge) {
            this.charge = charge;
        }

        @RestrictedSet("KAUF,VERKAUF,BUY,SELL")
        public String getOrdertyp() {
            return ordertyp;
        }

        public void setOrdertyp(String ordertyp) {
            this.ordertyp = ordertyp;
        }

        public void setOrdertype(String ordertype) {
            this.ordertyp = ordertype;
        }

        /**
         * @return the purchase price per share
         */
        @NotNull
        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        /**
         * @return must be set if the instruments currency is not the same as the portfolios
         */
        public BigDecimal getExchangeRate() {
            return exchangeRate;
        }

        public void setExchangeRate(BigDecimal exchangeRate) {
            this.exchangeRate = exchangeRate;
        }
    }

    public PfOrderAendern() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Long localUserId = getLocalUserId(cmd);

        final DateTime orderDate = getOrderDate(cmd.getDate(), errors);
        if (orderDate == null || orderDate.isAfter(new DateTime())) {
            return null;
        }

        final UpdateOrderCommand uoc = new UpdateOrderCommand();
        uoc.setUserid(localUserId);
        uoc.setPortfolioid(cmd.getPortfolioid());
        uoc.setOrderid(cmd.getOrderid());
        uoc.setBuy("KAUF".equals(cmd.getOrdertyp()) ||"BUY".equals(cmd.getOrdertyp()));
        uoc.setDate(orderDate);
        uoc.setPrice(cmd.getPrice());
        uoc.setExchangeRate(cmd.getExchangeRate());
        uoc.setVolume(cmd.getAmount());
        uoc.setCharge(cmd.getCharge());

        getUserProvider().updateOrder(uoc);

        final Map<String, Object> model = new HashMap<>();
        model.put("portfolioid", cmd.getPortfolioid());
        model.put("orderid", cmd.getOrderid());
        return new ModelAndView("pforderaendern", model);
    }

    static DateTime getOrderDate(String dateStr, BindException errors) {
        final DateTime result;
        try {
            result = DTF.parseDateTime(dateStr);
        } catch (IllegalFieldValueException e) {
            errors.rejectValue("date", "invalid.order.date", new Object[] { dateStr },
                    "invalid date: " + dateStr);
            return null;
        }
        if (result.isBefore(FIRST_ORDER_DATE) || result.isAfter(new DateTime())) {
            errors.rejectValue("date", "invalid.order.date", new Object[] { dateStr },
                    "invalid date: " + dateStr);
            return null;
        }
        return result;
    }
}