/*
 * ImportPortfolio.java
 *
 * Created on 23.11.2006 19:13:48
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user.userimport_pb;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.merger.Constants;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImportPortfolio {
    private final String username;
    private final String listname;
    private final int num;
    private final List<ImportPosition> positions = new ArrayList<>();
    private boolean portfolio;

    public ImportPortfolio(String username, String listname, int num, boolean portfolio) {
        this.username = username;
        this.listname = listname;
        this.num = num;
        this.portfolio = portfolio;
    }

    public void add(ImportPosition ip) {
        this.positions.add(ip);
    }

    public String getUsername() {
        return username;
    }

    public String getListname() {
        return listname;
    }

    public int getNum() {
        return num;
    }

    public List<ImportPosition> getPositions() {
        return positions;
    }

    public boolean isPortfolio() {
        return portfolio;
    }

    public void finish() {
        final Map<String, ImportPosition> map = new HashMap<>();
        for (final ImportPosition position : this.positions) {
            final String key = position.getWkn() + "@" + position.getMarket();
            if (!map.containsKey(key)) {
                map.put(key, position);
            }
            else {
                if (position.getOrdervalue() != null && position.getVolume().compareTo(BigDecimal.ZERO) != 0) {
                    final ImportPosition oldPosition = map.get(key);

                    final BigDecimal volume = position.getVolume().add(oldPosition.getVolume());
                    final BigDecimal value = position.getOrdervalue().multiply(position.getVolume(), Constants.MC)
                            .add(oldPosition.getOrdervalue().multiply(oldPosition.getVolume(), Constants.MC)).divide(volume, Constants.MC);

                    final String notiz;
                    if (StringUtils.hasText(position.getNotiz()) && StringUtils.hasText(oldPosition.getNotiz())) {
                        notiz = position.getNotiz() + ", " + oldPosition.getNotiz();
                    }
                    else if (StringUtils.hasText(position.getNotiz())) {
                        notiz = position.getNotiz();
                    }
                    else if (StringUtils.hasText(position.getNotiz())) {
                        notiz = oldPosition.getNotiz();
                    }
                    else {
                        notiz = "";
                    }

                    final ImportPosition newPosition = new ImportPosition(position.getWkn(), position.getCurrency(),
                            position.getMarket(), value, oldPosition.getOrderdate(), volume, notiz);

                    map.put(key, newPosition);

//                        System.out.println("ALARM for " + position);
                }
            }
        }
        this.positions.clear();
        this.positions.addAll(map.values());
    }

    public String toString() {
        return "ImportPortfolio[username=" + username
                + ", listname=" + listname
                + ", num=" + num
                + ", portfolio?=" + portfolio
                + ", #positions=" + positions.size()
                + "]";
    }
}
