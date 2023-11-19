/*
 * NwsListeNachrichten.java
 *
 * Created on 29.01.2007 13:25:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.beans.PropertyEditorSupport;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.PortfolioVaRLightRequest;
import de.marketmaker.istar.merger.provider.PortfolioVaRLightResponse;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.PortfolioPosition;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscPortfolioVaRLight extends AbstractUserListHandler {
    public static class Position {
        private String symbol;

        private BigDecimal quantity;

        private BigDecimal purchasePrice;

        public Position(String symbol, BigDecimal quantity, BigDecimal purchasePrice) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.purchasePrice = purchasePrice;
        }

        public String getSymbol() {
            return symbol;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public BigDecimal getPurchasePrice() {
            return purchasePrice;
        }
    }

    public static class PositionEditor extends PropertyEditorSupport {
        public void setAsText(String text) throws IllegalArgumentException {
            try {
                final String content = text.trim();

                final Position position;
                if (content.startsWith("<")) {
                    position = parseXml(text);
                }
                else {
                    final String[] tokens = content.split(Pattern.quote(";"));
                    final String symbol = tokens[0];
                    final BigDecimal quantity = new BigDecimal(tokens[1]);
                    final BigDecimal purchasePrice = tokens.length > 2 ? new BigDecimal(tokens[2]) : null;
                    position = new Position(symbol, quantity, purchasePrice);
                }
                setValue(position);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        private Position parseXml(String text) throws Exception {
            final Element root = new SAXBuilder().build(new StringReader(text)).getRootElement();
            final String pp = root.getChildTextTrim("purchasePrice");
            return new Position(root.getChildTextTrim("symbol"),
                    new BigDecimal(root.getChildTextTrim("quantity")),
                    StringUtils.hasText(pp) ? new BigDecimal(pp) : null);
        }
    }

    public static class Command extends UserListListCommand {
        private Position[] position;

        private LocalDate date;

        private String currency;

        private SymbolStrategyEnum symbolStrategy;

        private String marketStrategy;

        public Position[] getPosition() {
            return position;
        }

        public void setPosition(Position[] position) {
            this.position = position;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public SymbolStrategyEnum getSymbolStrategy() {
            return symbolStrategy;
        }

        public void setSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
            this.symbolStrategy = symbolStrategy;
        }

        public String getMarketStrategy() {
            return marketStrategy;
        }

        public void setMarketStrategy(String marketStrategy) {
            this.marketStrategy = marketStrategy;
        }

        // override to suppress @NotNull annotation

        @Override
        public String getUserid() {
            return super.getUserid();
        }

        // override to suppress @NotNull annotation

        @Override
        public Long getCompanyid() {
            return super.getCompanyid();
        }
    }

    private HistoricRatiosProvider historicRatiosProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public MscPortfolioVaRLight() {
        super(Command.class);
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        final PortfolioVaRLightRequest varRequest = buildRequest(cmd);

        final PortfolioVaRLightResponse varResponse = this.historicRatiosProvider.getPortfolioVaRLight(varRequest);

        final Map<String, Quote> failedQuotes = new HashMap<>();

        final List<Long> qids = new ArrayList<>();
        for (final Map.Entry<String, SymbolQuote> entry : varResponse.getFailedQuotes().entrySet()) {
            failedQuotes.put(entry.getKey(), null);

            if (entry.getValue() != null) {
                qids.add(entry.getValue().getId());
            }
        }

        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(qids);

        NEXT:
        for (Map.Entry<String, Quote> entry : failedQuotes.entrySet()) {
            for (final Quote quote : quotes) {
                if (entry.getValue() == null || quote.getId() == entry.getValue().getId()) {
                    failedQuotes.put(entry.getKey(), quote);
                    continue NEXT;
                }
            }
        }
        final Map<String, Object> model = new HashMap<>();
        model.put("varResponse", varResponse);
        model.put("failedQuotes", failedQuotes);
        return new ModelAndView("mscportfoliovarlight", model);
    }

    private PortfolioVaRLightRequest buildRequest(Command cmd) {
        if (cmd.getListid() != null) {
            return buildPortfolioBasedRequest(cmd);
        }
        return buildPositionBasedRequest(cmd);
    }

    private PortfolioVaRLightRequest buildPositionBasedRequest(Command cmd) {
        final PortfolioVaRLightRequest varRequest = new PortfolioVaRLightRequest(cmd.getDate(), cmd.getCurrency());

        final List<String> symbols = getSymbols(cmd);

        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(symbols, cmd.getSymbolStrategy(), null, cmd.getMarketStrategy());

        final Map<String, Quote> quotesBySymbol = new HashMap<>();
        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            final String symbol = symbols.get(i);
            quotesBySymbol.put(symbol, quote);
        }

        for (Position position : cmd.getPosition()) {
            final String symbol = position.getSymbol();
            final Quote quote = quotesBySymbol.get(symbol);
            final BigDecimal quantity = position.getQuantity();
            final BigDecimal purchasePrice = position.getPurchasePrice();

            varRequest.addPosition(symbol, quote, quantity, purchasePrice);
        }

        return varRequest;
    }

    private List<String> getSymbols(Command cmd) {
        final List<String> symbols = new ArrayList<>();
        for (final Position position : cmd.getPosition()) {
            symbols.add(position.getSymbol());
        }
        return symbols;
    }

    private PortfolioVaRLightRequest buildPortfolioBasedRequest(Command cmd) {
        final PortfolioVaRLightRequest varRequest = new PortfolioVaRLightRequest(cmd.getDate(), cmd.getCurrency());
        final Portfolio p = getPortfolio(cmd);
        final List<Long> qids = new ArrayList<>();
        for (final PortfolioPosition pp : p.getNonEmptyPositions()) {
            qids.add(pp.getQid());
        }
        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(qids);
        final Map<Long, Quote> quotesByQid = new HashMap<>();
        for (final Quote quote : quotes) {
            quotesByQid.put(quote.getId(), quote);
        }

        for (final PortfolioPosition pp : p.getNonEmptyPositions()) {
            varRequest.addPosition(pp.getQid() + ".qid", quotesByQid.get(pp.getQid()), pp.getTotalVolume());
        }
        return varRequest;
    }
}
