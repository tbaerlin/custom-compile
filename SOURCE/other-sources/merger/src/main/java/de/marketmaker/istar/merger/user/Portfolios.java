/*
 * Portfolios.java
 *
 * Created on 27.07.2006 11:28:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.Serializable;

import de.marketmaker.istar.merger.user.Portfolio;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class Portfolios implements Serializable {
    static final long serialVersionUID = 1L;

    private List<Portfolio> portfolios;

    public String toString() {
        return new StringBuilder(1000).append("Portfolios").append(this.portfolios).toString();
    }

    Portfolios deepCopy() {
        final List<Portfolio> tmp = new ArrayList<>();
        for (Portfolio portfolio : portfolios) {
            tmp.add(portfolio.deepCopy());
        }
        return new Portfolios(tmp);
    }

    Portfolios(List<Portfolio> portfolios) {
        this.portfolios = new ArrayList(portfolios.size());
        this.portfolios.addAll(portfolios);
    }

    public Portfolio getWatchlist(long id) {
        return filter(id, true);
    }

    public Portfolio getPortfolio(long id) {
        return filter(id, false);
    }

    public Portfolio get(long id) {
        for (Portfolio portfolio : portfolios) {
            if (id == portfolio.getId()) {
                return portfolio;
            }
        }
        return null;
    }

    private Portfolio filter(long id, boolean watchlist) {
        final Portfolio result = get(id);
        if (result == null) {
            return null;
        }
        return (result.isWatchlist() == watchlist) ? result : null;
    }

    public List<Portfolio> getPortfolios() {
        return filter(false);
    }

    public List<Portfolio> getWatchlists() {
        return filter(true);
    }

    private List<Portfolio> filter(final boolean watchlist) {
        final List<Portfolio> result = new ArrayList<>(this.portfolios.size());
        for (Portfolio portfolio : portfolios) {
            if (portfolio.isWatchlist() == watchlist) {
                result.add(portfolio);
            }
        }
        return result;
    }

    void replaceExistingWith(Portfolio wNew) {
        for (int i = 0; i < this.portfolios.size(); i++) {
            final Portfolio w = this.portfolios.get(i);
            if (w.getId() == wNew.getId()) {
                this.portfolios.set(i, wNew);
                return;
            }
        }
    }

    void add(Portfolio w) {
        this.portfolios.add(w);
    }

    void remove(long id) {
        for (Iterator<Portfolio> it = this.portfolios.iterator(); it.hasNext();) {
            if (it.next().getId() == id) {
                it.remove();
                return;
            }
        }
    }
}
