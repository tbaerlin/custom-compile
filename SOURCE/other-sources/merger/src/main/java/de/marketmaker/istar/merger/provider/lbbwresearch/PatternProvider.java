package de.marketmaker.istar.merger.provider.lbbwresearch;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Hold and provide patterns to find rating and target price
 * @author mcoenen
 */
public class PatternProvider implements InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String priceWithCurrency = "(\\d+(?:[,.]\\d+)?)\\s+[A-Z]{3}";

    private String buyHoldSell__de = "KAUFEN|HALTEN|VERKAUFEN";
    private String rating__de = "RATING";
    private String targetPrice__de = "KURSZIEL";
    private String unchanged__de = "[^)]*";

    private String buyHoldSell__en = "BUY|HOLD|SELL";
    private String rating__en = "RATING";
    private String targetPrice__en = "TARGET PRICE";
    private String unchanged__en = unchanged__de;

    public void setUnchanged__en(String unchanged__en) {
        this.unchanged__en = unchanged__en;
    }

    public void setPriceWithCurrency(String priceWithCurrency) {
        this.priceWithCurrency = priceWithCurrency;
    }

    public void setRating__de(String rating__de) {
        this.rating__de = rating__de;
    }

    public void setTargetPrice__de(String targetPrice__de) {
        this.targetPrice__de = targetPrice__de;
    }

    public void setBuyHoldSell__de(String buyHoldSell__de) {
        this.buyHoldSell__de = buyHoldSell__de;
    }

    public void setUnchanged__de(String unchanged__de) {
        this.unchanged__de = unchanged__de;
    }

    public void setRating__en(String rating__en) {
        this.rating__en = rating__en;
    }

    public void setTargetPrice__en(String targetPrice__en) {
        this.targetPrice__en = targetPrice__en;
    }

    public void setBuyHoldSell__en(String buyHoldSell__en) {
        this.buyHoldSell__en = buyHoldSell__en;
    }

    Map<String, Map<String, Pattern>> patternsPerLanguage = Collections.emptyMap();

    private Map<String, Integer> ratings = Collections.emptyMap();

    @Override
    public String toString() {
        return "PatternProvider{" +
                "priceWithCurrency='" + priceWithCurrency + '\'' +
                ", buyHoldSell__de='" + buyHoldSell__de + '\'' +
                ", buyHoldSell__en='" + buyHoldSell__en + '\'' +
                ", rating__de='" + rating__de + '\'' +
                ", rating__en='" + rating__en + '\'' +
                ", targetPrice__de='" + targetPrice__de + '\'' +
                ", targetPrice__en='" + targetPrice__en + '\'' +
                ", unchanged__de='" + unchanged__de + '\'' +
                ", unchanged__en='" + unchanged__en + '\'' +
                '}';
    }

    @Override
    public void afterPropertiesSet() {

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Initializing patterns with " + this.toString());
        }

        this.patternsPerLanguage =
                ImmutableMap.<String, Map<String, Pattern>>builder()
                        .put("GERMAN",
                                ImmutableMap.of(
                                        "rating", buildRatingPattern(rating__de, buyHoldSell__de, unchanged__de),
                                        "targetPrice", buildTargetPricePattern(targetPrice__de, priceWithCurrency, unchanged__de)
                                )
                        )
                        .put("ENGLISH",
                                ImmutableMap.of(
                                        "rating", buildRatingPattern(rating__en, buyHoldSell__en, unchanged__en),
                                        "targetPrice", buildTargetPricePattern(targetPrice__en, priceWithCurrency, unchanged__en)
                                )
                        )
                        .build();

        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();

        String[] splitRatings = buyHoldSell__de.split("\\|");
        builder.put(splitRatings[0], BasicDocument.BUY_VALUE);
        builder.put(splitRatings[1], BasicDocument.HOLD_VALUE);
        builder.put(splitRatings[2], BasicDocument.SELL_VALUE);

        splitRatings = buyHoldSell__en.split("\\|");
        builder.put(splitRatings[0], BasicDocument.BUY_VALUE);
        builder.put(splitRatings[1], BasicDocument.HOLD_VALUE);
        builder.put(splitRatings[2], BasicDocument.SELL_VALUE);

        this.ratings = builder.build();
    }

    int getRatingValue(String rating) {
        return this.ratings.getOrDefault(rating.toUpperCase(), BasicDocument.NO_VALUE);
    }

    private Pattern buildRatingPattern(String rating, String buyHoldSell, String unchanged) {
        return Pattern.compile(
                rating + "\\r?\\n(" + buyHoldSell + ")\\s+?\\((?:(" + buyHoldSell + ")|" + unchanged + ")\\)",
                Pattern.CASE_INSENSITIVE
        );
    }

    private Pattern buildTargetPricePattern(String targetPrice, String priceWithCurrency, String unchanged) {
        return Pattern.compile(
                targetPrice + "\\r?\\n" + priceWithCurrency + "\\s+\\((?:" + priceWithCurrency + "|" + unchanged + ")\\)",
                Pattern.CASE_INSENSITIVE
        );
    }

    public static void main(String[] args) {
        System.out.println(Optional.of(13).map(Optional::of).orElseGet(() -> Optional.of(4)));


        final PatternProvider patternProvider = new PatternProvider();
        Pattern p = patternProvider.buildTargetPricePattern(patternProvider.targetPrice__de, patternProvider.priceWithCurrency, patternProvider.unchanged__de);
        System.out.println(p.pattern());
        Matcher targetPriceMatcher = p.matcher("asdasdsad ... KURSZIEL\n" + "13 CHF (UNVERÄNDERT)");
        targetPriceMatcher.find();
        System.out.println(targetPriceMatcher.group(0));
        System.out.println(new BigDecimal(targetPriceMatcher.group(1).replace(',', '.')));
        System.out.println("2 " + targetPriceMatcher.group(2));
    }
}
