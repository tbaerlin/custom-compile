/*
 * SingleEvaluatedPosition.java
 *
 * Created on 07.08.2006 15:54:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import static de.marketmaker.istar.merger.Constants.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SingleEvaluatedPosition extends AbstractEvaluatedPosition {
    private final PortfolioPosition position;

    private final PriceRecord currentPrice;

    private final Quote quote;

    private final BigDecimal exchangerate;

    private final Assets assets;

    /**
     * Maintains Asset objects based on a FIFO model (sells reduce the amount of the first
     * (e.g. earlier) buy(s). Therefore, Assets will, for any given list of orders, contain
     * either only Asset objects with buy orders or only those with sell orders).
     */
    public static class Assets {
        private final LinkedList<Asset> q = new LinkedList<>();

        private final BigDecimal quotedPerFactor;

        /**
         * The costs for buying those assets that have later been sold and all charges for
         * buying and selling assets.
         */
        private BigDecimal realizedCost = BigDecimal.ZERO;

        /**
         * The realized gains from selling assets.
         */
        private BigDecimal realizedGain = BigDecimal.ZERO;

        public Assets(PortfolioPosition position) {
            this.quotedPerFactor = position.isQuotedPerPercent() ? ONE_PERCENT : BigDecimal.ONE;
            for (Order order : position.getOrders()) {
                this.push(order, quotedPerFactor);
            }
        }

        public BigDecimal getQuotedPerFactor() {
            return quotedPerFactor;
        }

        private void push(Order order, BigDecimal quotedPerFactor) {
            this.realizedCost = this.realizedCost.add(order.getCharge());

            final Asset asset = new Asset(order);

            if (q.isEmpty()) {
                q.add(asset);
                return;
            }
            if (q.peek().isBuy() == asset.isBuy()) { // another buy or sell
                q.add(asset);
                return;
            }

            // if we get here, q contains only buys (or sells) and order is sell (or buy).

            BigDecimal volumeToMatch = order.getVolume();

            while (!q.isEmpty() && volumeToMatch.compareTo(BigDecimal.ZERO) > 0) {
                final Asset first = q.peek();

                // the volume of the first asset that is matched by volumeToMatch
                final BigDecimal accountableVolume = volumeToMatch.min(first.volume);

                if (first.isBuy()) {
                    this.realizedCost = this.realizedCost.add(getRealizedValue(first.order, accountableVolume, quotedPerFactor));
                    this.realizedGain = this.realizedGain.add(getRealizedValue(order, accountableVolume, quotedPerFactor));
                }
                else {
                    this.realizedGain = this.realizedGain.add(getRealizedValue(first.order, accountableVolume, quotedPerFactor));
                    this.realizedCost = this.realizedCost.add(getRealizedValue(order, accountableVolume, quotedPerFactor));
                }

                if (first.volume.compareTo(volumeToMatch) <= 0) {
                    q.remove();
                    volumeToMatch = volumeToMatch.subtract(first.volume);
                }
                else {
                    first.decreaseVolumeBy(volumeToMatch);
                    return;
                }
            }
            if (q.isEmpty() && volumeToMatch.compareTo(BigDecimal.ZERO) > 0) {
                q.add(new Asset(order, volumeToMatch));
            }
        }

        private BigDecimal getRealizedValue(Order o, BigDecimal volume, BigDecimal quotedPerFactor) {
            return volume.multiply(o.getPrice(), MC).multiply(o.getExchangerate(), MC).multiply(quotedPerFactor, MC);
        }

        private BigDecimal getOrderValue(boolean inPortfolioCurrency) {
            BigDecimal result = BigDecimal.ZERO;
            for (Asset asset : q) {
                result = result.add(asset.getValue(inPortfolioCurrency), MC);
            }
            return result;
        }

        private BigDecimal getCurrentValue(BigDecimal price, BigDecimal exchangerate) {
            if (price == null) {
                return BigDecimal.ZERO;
            }
            return getTotalVolume().multiply(price, MC).multiply(exchangerate, MC);
        }

        private boolean isBuys() {
            return this.q.isEmpty() || this.q.peek().isBuy();
        }

        private BigDecimal getTotalVolume() {
            BigDecimal result = BigDecimal.ZERO;
            for (Asset asset : q) {
                result = result.add(asset.volume, MC);
            }
            if (!isBuys()) {
                result = result.multiply(MINUS_ONE);
            }
            return result;
        }

        // result might be in percent when used on perpercent quoted instruments
        public BigDecimal getAverageOrderPrice(boolean inPortfolioCurrency) {
            final BigDecimal totalVolume = getTotalVolume();
            if (totalVolume.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return getOrderValue(inPortfolioCurrency).divide(totalVolume, MC);
        }

        public String toString() {
            final StringBuilder sb = new StringBuilder(this.q.size() * 30);
            sb.append("Assets:\n");
            for (Asset asset : q) {
                asset.appendTo(sb);
            }
            return sb.toString();
        }

        public List<Asset> getAssets() {
            return new ArrayList<>(q);
        }
    }


    /**
     * A number of securities that have been bought/sold in a certain quantity
     * for a certain price. The volume represents the number of shares from the order
     * that are still in the user's Portfolio (if you buy 200 shares and sell 50, there
     * will be an asset associated the the buy order and a volume of 150).
     */
    public static class Asset {
        public final static Comparator<Asset> BY_ORDER_DATE_COMPARATOR = new Comparator<Asset>() {
            @Override
            public int compare(Asset asset1, Asset asset2) {
                return Order.BY_DATE_COMPARATOR.compare(asset1.getOrder(), asset2.getOrder());
            }
        };

        private final Order order;

        private BigDecimal volume;

        private Asset(Order o) {
            this(o, o.getVolume());
        }

        private Asset(Order o, BigDecimal volume) {
            this.order = o;
            this.volume = volume;
        }

        private boolean isBuy() {
            return this.order.isBuy();
        }

        private StringBuilder appendTo(StringBuilder sb) {
            return sb.append(isBuy() ? "BUY" : "SELL")
                    .append(" #").append(this.volume.toPlainString())
                    .append(" ").append(this.order.getPrice().toPlainString())
                    .append(", exchRate=").append(this.order.getExchangerate().toPlainString())
                    .append("\n");
        }

        private void decreaseVolumeBy(BigDecimal v) {
            this.volume = this.volume.subtract(v);
        }

        private BigDecimal getValue(boolean inPortfolioCurrency) {
            BigDecimal result = this.volume.multiply(this.order.getPrice(), MC);
            if (inPortfolioCurrency) {
                result = result.multiply(this.order.getExchangerate(), MC);
            }
            if (!this.isBuy()) {
                result = result.multiply(MINUS_ONE);
            }
            return result;
        }

        public Order getOrder() {
            return order;
        }

        public BigDecimal getVolume() {
            return volume;
        }
    }

    public SingleEvaluatedPosition(PortfolioPosition position, Quote quote,
            PriceRecord currentPrice,
            BigDecimal exchangerate) {
        this.position = position;
        this.assets = new Assets(position);
        this.quote = quote;
        this.currentPrice = currentPrice;
        this.exchangerate = exchangerate;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(200);
        sb.append("EvaluatedPosition ").append(position.getQid()).append("\n");
        sb.append(this.assets);
        return sb.toString();
    }

    /**
     * @return the cost realized with orders for this position. Costs are realized whenever a
     * buy order can be matched with one or more sell orders. Additionally, all charges for buy
     * and sell orders are realized costs.
     */
    @Override
    public BigDecimal getRealizedCost() {
        return this.assets.realizedCost;
    }

    /**
     * @return the gain realized with orders for this position. Gains are realized whenever a
     * sell order can be matched with one or more buy orders.
     */
    @Override
    public BigDecimal getRealizedGain() {
        return this.assets.realizedGain;
    }

    @Override
    public BigDecimal getOrderValue() {
        return this.assets.getOrderValue(false)
                .multiply(getQuotedPerFactor(), MC);
    }

    @Override
    public BigDecimal getOrderValueInPortfolioCurrency() {
        return this.assets.getOrderValue(true)
                .multiply(getQuotedPerFactor(), MC);
    }

    @Override
    public BigDecimal getCurrentValue() {
        return this.assets.getCurrentValue(getPriceOrBidAskMid(), BigDecimal.ONE)
                .multiply(getQuotedPerFactor(), MC);
    }

    @Override
    public BigDecimal getCurrentValueInPortfolioCurrency() {
        return this.assets.getCurrentValue(getPriceOrBidAskMid(), this.exchangerate)
                .multiply(getQuotedPerFactor(), MC);
    }

    private BigDecimal getPriceOrBidAskMid() {
        final BigDecimal price = this.currentPrice.getPrice().getValue();
        if (price == null) {
            return this.currentPrice.getBidAskMidPrice();
        }
        return price;
    }

    @Override
    public BigDecimal getPreviousCloseValue() {
        return this.assets.getCurrentValue(getPreviousPriceOrBidAskMid(), BigDecimal.ONE)
                .multiply(getQuotedPerFactor(), MC);
    }

    @Override
    public BigDecimal getPreviousCloseValueInPortfolioCurrency() {
        return this.assets.getCurrentValue(getPreviousPriceOrBidAskMid(), this.exchangerate)
                .multiply(getQuotedPerFactor(), MC);
    }

    private BigDecimal getPreviousPriceOrBidAskMid() {
        final BigDecimal previousPrice = this.currentPrice.getPreviousClose().getValue();
        if (previousPrice == null) {
            return this.currentPrice.getPreviousBidAskMidPrice();
        }
        return previousPrice;
    }

    @Override
    public BigDecimal getAverageOrderPrice() {
        return this.assets.getAverageOrderPrice(false);
    }

    @Override
    public BigDecimal getAverageOrderPriceInPortfolioCurrency() {
        return this.assets.getAverageOrderPrice(true);
    }

    @Override
    public BigDecimal getTotalVolume() {
        return this.assets.getTotalVolume();
    }

    @Override
    public PriceRecord getCurrentPrice() {
        return currentPrice;
    }

    @Override
    public BigDecimal getExchangerate() {
        return exchangerate;
    }

    @Override
    public Quote getQuote() {
        return quote;
    }

    @Override
    public DateTime getLastOrderDate() {
        final List<Order> orders = this.position.getOrders();
        if (orders.isEmpty()) {
            return null;
        }
        return (orders.get(orders.size() - 1)).getDate();
    }

    public PortfolioPosition getPosition() {
        return position;
    }
}
