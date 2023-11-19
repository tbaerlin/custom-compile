/*
 * PfOrderAusfuehren.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 **/
package de.marketmaker.istar.merger.web.easytrade.block;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Pattern;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.user.AddOrderCommand;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.ErrorUtils;

/**
 * Creates an order of a given quote (identified by symbol)
 */
public class PfOrderAusfuehren extends UserHandler {
    private EasytradeInstrumentProvider instrumentProvider;

    private IsoCurrencyConversionProvider currencyConversionProvider;

    private static final String PF_CURRENCY = "EUR";

    public void setCurrencyConversionProvider(
            IsoCurrencyConversionProvider currencyConversionProvider) {
        this.currencyConversionProvider = currencyConversionProvider;
    }

    public static class Command extends DefaultSymbolCommand implements UserCommand {
        private String userid;

        private Long portfolioid;

        //        private Long orderid;
        private BigDecimal amount;

        private BigDecimal price;

        private BigDecimal exchangeRate;

        private BigDecimal charge;

        private String date;

        private String ordertyp;

        private Long companyid;

        private boolean sellFromOtherMarkets;

        @NotNull
        public Long getCompanyid() {
            return companyid;
        }

        public void setCompanyid(Long companyid) {
            this.companyid = companyid;
        }

        /**
         * @return id of the portfolio owner
         */
        @NotNull
        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

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
        @Pattern(regex = "\\d{4}-\\d{2}-\\d{2}")
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
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

        /**
         * @return order fees
         */
        public BigDecimal getCharge() {
            return charge;
        }

        public void setCharge(BigDecimal charge) {
            this.charge = charge;
        }

        public boolean isSellFromOtherMarkets() {
            return sellFromOtherMarkets;
        }

        public void setSellFromOtherMarkets(boolean sellFromOtherMarkets) {
            this.sellFromOtherMarkets = sellFromOtherMarkets;
        }
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public PfOrderAusfuehren() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;
        final Long localUserId = getLocalUserId(cmd);

        boolean onlyWithPosition = false;
        Quote quote;

        try {
            quote = this.instrumentProvider.getQuote(cmd);
        } catch (UnknownSymbolException e) {
            onlyWithPosition = true;
            quote = createMissingQuote(cmd);
        }

        if (quote == null) {
            ErrorUtils.rejectSymbol(cmd.getSymbol(), errors);
            return null;
        }

        final DateTime orderDate = PfOrderAendern.getOrderDate(cmd.getDate(), errors);
        if (orderDate == null || orderDate.isAfter(new DateTime())) {
            return null;
        }

        if (cmd.getAmount().toBigInteger().longValue() < 0 || cmd.getPrice().toBigInteger().longValue() < 0) {
            return null;
        }


        final BigDecimal exchangeRate;
        if (cmd.getExchangeRate() != null) {
            exchangeRate = cmd.getExchangeRate();
        }
        else if (!PF_CURRENCY.equals(quote.getCurrency().getSymbolIso())) {
            exchangeRate = this.currencyConversionProvider.getConversion(quote.getCurrency().getSymbolIso(),
                    PF_CURRENCY, orderDate.toLocalDate()).getRate().getValue();
        }
        else {
            exchangeRate = BigDecimal.ONE;
        }

        final AddOrderCommand aoc = new AddOrderCommand();
        aoc.setUserid(localUserId);
        aoc.setPortfolioid(cmd.getPortfolioid());
        aoc.setBuy("KAUF".equals(cmd.getOrdertyp()) || "BUY".equals(cmd.getOrdertyp()));
        aoc.setDate(orderDate);
        aoc.setExchangeRate(exchangeRate);
        aoc.setPrice(cmd.getPrice());
        aoc.setCharge(cmd.getCharge());
        aoc.setQuote(quote);
        aoc.setVolume(cmd.getAmount());
        aoc.setOnlyWithPosition(onlyWithPosition);

        List<Long> orderIds = new ArrayList<>();
        try {
            if (cmd.isSellFromOtherMarkets()) {
                List<AddOrderCommand> orderCommands = getUserProvider().distributeOrderWithinInstrument(aoc, instrumentProvider);
                for (AddOrderCommand command : orderCommands) {
                    orderIds.add(getUserProvider().addOrder(command));
                }
            }
            else {
                orderIds.add(getUserProvider().addOrder(aoc));
            }
        } catch (IllegalStateException e) {
            ErrorUtils.rejectSymbol(cmd.getSymbol(), errors);
            return null;
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("portfolioid", cmd.getPortfolioid());
        model.put("orderids", orderIds);
        return new ModelAndView("pforderausfuehren", model);
    }

    private Quote createMissingQuote(final Command cmd) {
        final String symbol = cmd.getSymbol();
        final String orderType = cmd.getOrdertyp();
        if (symbol != null && symbol.endsWith(EasytradeInstrumentProvider.QID_SUFFIX)
                && ("VERKAUF".equals(orderType) || "SELL".equals(orderType))) {

            try {
                return new QuoteDp2(EasytradeInstrumentProvider.id(symbol));
            } catch (Exception e) {
                logger.info("<createMissingQuote> error parsing symbol " + symbol);
            }
        }
        return null;
    }
}
