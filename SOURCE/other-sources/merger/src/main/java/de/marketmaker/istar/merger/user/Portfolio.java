/*
 * Watchlist.java
 *
 * Created on 24.07.2006 14:24:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.marketmaker.istar.merger.Constants;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Portfolio implements Serializable {
    protected static final long serialVersionUID = 1L;

    private long id;

    private String name;

    private boolean watchlist;

    private BigDecimal cash;

    private String currencyCode;

    private final List<PortfolioPosition> positions = new ArrayList<>();

    private Map<String, PortfolioPositionNote> notes = new HashMap<>();


    Portfolio() {
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(1000);
        sb.append(this.watchlist ? "Watchlist[" : "Portfolio[")
                .append(this.id)
                .append(", name=" + this.name);
        if (!this.watchlist) {
            sb.append(", cash=" + this.cash)
                    .append(", currency=" + this.currencyCode);
        }
        sb.append(", positions=").append(this.positions);
        sb.append("]");
        return sb.toString();
    }

    Portfolio deepCopy() {
        final Portfolio result = new Portfolio();
        result.id = this.id;
        result.name = this.name;
        result.watchlist = this.watchlist;
        result.cash = this.cash;
        result.currencyCode = this.currencyCode;

        if (this.notes != null) {
            result.notes = new HashMap<>();
            for (Map.Entry<String, PortfolioPositionNote> note : notes.entrySet()) {
                PortfolioPositionNote noteCopy = note.getValue().deepCopy();
                result.notes.put(noteCopy.getItemId(), noteCopy);
            }
        }

        for (PortfolioPosition position : positions) {
            result.positions.add(position.deepCopy());
        }
        return result;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public long getId() {
        return id;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public String getName() {
        return name;
    }

    public List<PortfolioPosition> getPositions(boolean withEmptyPositions) {
        return withEmptyPositions
                ? getPositions()
                : getNonEmptyPositions();
    }

    public List<PortfolioPosition> getPositions() {
        return Collections.unmodifiableList(this.positions);
    }

    public List<PortfolioPosition> getNonEmptyPositions() {
        final List<PortfolioPosition> result = new ArrayList<>(this.positions.size());

        for (PortfolioPosition position : this.positions) {
            if (position.getTotalVolume().compareTo(BigDecimal.ZERO) != 0) {
                result.add(position);
            }
        }
        return result;
    }

    public PortfolioPosition getPosition(long id) {
        for (PortfolioPosition position : positions) {
            if (position.getId() == id) {
                return position;
            }
        }
        return null;
    }

    boolean removePosition(long id) {
        for (Iterator<PortfolioPosition> it = this.positions.iterator(); it.hasNext(); ) {
            final PortfolioPosition portfolioPosition = it.next();
            if (portfolioPosition.getId() == id) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    boolean hasPositionForQuote(long qid) {
        return getPositionForQuote(qid) != null;
    }

    PortfolioPosition getPositionForQuote(long qid) {
        for (PortfolioPosition position : positions) {
            if (position.getQid() == qid) {
                return position;
            }
        }
        return null;
    }

    List<PortfolioPosition> getPositionsForInstrument(long iid) {
        List<PortfolioPosition> result = new ArrayList<>();

        for (PortfolioPosition position : positions) {
            if (position.getIid() == iid) {
                result.add(position);
            }
        }

        return result;
    }


    boolean isWatchlist() {
        return watchlist;
    }

    void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    void setId(long id) {
        this.id = id;
    }

    void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    void addCash(BigDecimal amount) {
        this.cash = this.cash.add(amount, Constants.MC);
    }

    void setName(String name) {
        this.name = name;
    }

    void addPosition(PortfolioPosition position) {
        this.positions.add(position);
    }

    void setWatchlist(boolean watchlist) {
        this.watchlist = watchlist;
    }

    public Order getOrder(Long orderid) {
        for (PortfolioPosition position : positions) {
            for (Order order : position.getOrders()) {
                if (order.getId() == orderid) {
                    return order;
                }
            }
        }
        return null;
    }

    public PortfolioPosition getPositionWithOrder(long orderid) {
        for (PortfolioPosition position : positions) {
            for (Order order : position.getOrders()) {
                if (order.getId() == orderid) {
                    return position;
                }
            }
        }
        return null;
    }

    public Set<Long> getInstrumentIds() {
        final Set<Long> result = new HashSet<>();
        for (PortfolioPosition pp : this.positions) {
            result.add(pp.getIid());
        }
        return result;
    }

    public Set<Long> getQuoteIds() {
        final Set<Long> result = new HashSet<>();
        for (PortfolioPosition pp : this.positions) {
            result.add(pp.getQid());
        }
        return result;
    }

    public static List<PortfolioPosition> filterPositivePositions(List<PortfolioPosition> positions) {
        List<PortfolioPosition> positivePositions = new ArrayList<>();

        for (PortfolioPosition position : positions) {
            if (position.getTotalVolume().compareTo(BigDecimal.ZERO) > 0) {
                positivePositions.add(position);
            }
        }

        return positivePositions;
    }

    public Map<String, PortfolioPositionNote> getNotes() {
        return notes;
    }

    public void setNotes(Map<String, PortfolioPositionNote> notes) {
        this.notes = notes;
    }

}
