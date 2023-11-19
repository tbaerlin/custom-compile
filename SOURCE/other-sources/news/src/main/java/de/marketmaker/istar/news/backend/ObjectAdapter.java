package de.marketmaker.istar.news.backend;

import de.marketmaker.istar.analyses.backend.Protos.Analysis.Rating;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * converting feed record values to Objects for protobuf fields
 */
public interface ObjectAdapter {

    Object adapt(String in);


    ObjectAdapter IDENTITY_ADAPTER = new ObjectAdapter() {
        @Override
        public Object adapt(String in) {
            return in;
        }
    };

    ObjectAdapter LONG_ADAPTER = new ObjectAdapter() {
        @Override
        public Object adapt(String in) {
            return Long.parseLong(in);
        }
    };

    ObjectAdapter PRICE_ADAPTER = new ObjectAdapter() {
        private final Pattern PRICE_PATTERN = Pattern.compile("[0-9]+(\\.[0-9]*)?");

        @Override
        public Object adapt(String in) {
            final String price = normalize(in);
            return PRICE_PATTERN.matcher(price).matches() ? price : null;
        }

        private String normalize(String value) {
            int comma = value.lastIndexOf(',');
            if (comma == -1) {
                return value;
            }
            // has a comma somewhere
            int dot = value.lastIndexOf('.');
            if (dot == -1) {
                // no dot, just replace the comma
                return value.replace(',', '.');
            }
            if (dot > comma) {
                // dot is after the last comma, remove all comma, probably the thousands-separator
                return value.replace(",", "");
            }
            else {
                // comma is after the dot, remove the dot (thousands-separator), replace the comma
                return value.replace(".", "").replace(",", ".");
            }
        }
    };

    ObjectAdapter SHM_RATING_ADAPTER = new ObjectAdapter() {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        private final Map<String, Rating> map = new HashMap<>();
        {
            this.map.put("buy", Rating.BUY);
            this.map.put("equal-weight", Rating.HOLD);
            this.map.put("halten", Rating.HOLD);
            this.map.put("hold", Rating.HOLD);
            this.map.put("kaufen", Rating.BUY);
            this.map.put("neutral", Rating.HOLD);
            this.map.put("outperform", Rating.BUY);
            this.map.put("overweight", Rating.BUY);
            this.map.put("sector outperform", Rating.BUY);
            this.map.put("sector perform", Rating.HOLD);
            this.map.put("sell", Rating.SELL);
            this.map.put("speculative buy", Rating.BUY);
            this.map.put("underperform", Rating.SELL);
            this.map.put("verkaufen", Rating.SELL);
        }

        @Override
        public Object adapt(String in) {
            final Rating result = this.map.get(String.valueOf(in));
            if (result == null) {
                logger.warn("<adapt> unknown rating '" + in + "'");
                return Rating.NONE;
            }
            return result;
        }
    };

    ObjectAdapter DPA_AFX_RATING_ADAPTER = new ObjectAdapter() {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        private final Map<Object, Rating> map = new HashMap<>();
        {
            this.map.put("anb", Rating.BUY);
            this.map.put("buy", Rating.BUY);
            this.map.put("kaufen", Rating.BUY);
            this.map.put("add", Rating.BUY);
            this.map.put("akkummulieren", Rating.BUY);
            this.map.put("overweight", Rating.BUY);
            this.map.put("performace", Rating.BUY);
            this.map.put("ann", Rating.HOLD);
            this.map.put("hold", Rating.HOLD);
            this.map.put("halten", Rating.HOLD);
            this.map.put("neutral", Rating.HOLD);
            this.map.put("marketperform", Rating.HOLD);
            this.map.put("equalweight", Rating.HOLD);
            this.map.put("sectorperform", Rating.HOLD);
            this.map.put("ans", Rating.SELL);
            this.map.put("sell", Rating.SELL);
            this.map.put("verkaufen", Rating.SELL);
            this.map.put("reduce", Rating.SELL);
            this.map.put("reduzieren", Rating.SELL);
            this.map.put("underweight", Rating.SELL);
            this.map.put("underperform", Rating.SELL);
            this.map.put(null, Rating.NONE);
        }

        @Override
        public Object adapt(String in) {
            return getRating(in).getValueDescriptor();
        }

        private Rating getRating(String o) {
            final Rating result = this.map.get(o != null ? o.toLowerCase() : o);
            if (result == null) {
                this.logger.warn("<getRating> unknown rating '" + o + "'");
                return Rating.NONE;
            }
            return result;
        }
    };

    // mapping AWP Normalized Rating Codes
    ObjectAdapter AWP_NORM_RATING_ADAPTER = new ObjectAdapter() {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        private final Map<Object, Rating> map = new HashMap<>();
        {
            this.map.put("01", Rating.BUY);
            this.map.put("02", Rating.HOLD);
            this.map.put("03", Rating.SELL);
            this.map.put("04", Rating.NONE);
        }

        @Override
        public Object adapt(String in) {
            return getRating(in).getValueDescriptor();
        }

        private Rating getRating(Object o) {
            final Rating result = this.map.get(o);
            if (result == null) {
                this.logger.warn("<getRating> unknown rating '" + o + "' using NONE");
                return Rating.NONE;
            }
            return result;
        }
    };

    // mapping AWP Rating Codes
    // TODO: we have seen undocumented rating values: 'SP', 'HA'
    ObjectAdapter AWP_RATING_ADAPTER = new ObjectAdapter() {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        private final Map<Object, Rating> map = new HashMap<>();
        {
            this.map.put("AC", Rating.BUY);
            this.map.put("AD", Rating.BUY);
            this.map.put("AT", Rating.BUY);
            this.map.put("B", Rating.BUY);
            this.map.put("K", Rating.BUY);
            this.map.put("MO", Rating.BUY);
            this.map.put("OP", Rating.BUY);
            this.map.put("OW", Rating.BUY);
            this.map.put("SB", Rating.BUY);
            this.map.put("SL", Rating.BUY);
            this.map.put("ÜG", Rating.BUY);

            this.map.put("EW", Rating.HOLD);
            this.map.put("H", Rating.HOLD);
            this.map.put("HA", Rating.HOLD);
            this.map.put("IL", Rating.HOLD);
            this.map.put("MG", Rating.HOLD);
            this.map.put("MP", Rating.HOLD);
            this.map.put("MW", Rating.HOLD);
            this.map.put("N", Rating.HOLD);
            this.map.put("SP", Rating.HOLD);

            this.map.put("MU", Rating.SELL);
            this.map.put("R", Rating.SELL);
            this.map.put("S", Rating.SELL);
            this.map.put("UGW", Rating.SELL);
            this.map.put("UP", Rating.SELL);
            this.map.put("UW", Rating.SELL);
            this.map.put("VE", Rating.SELL);

            this.map.put("NR", Rating.NONE);
            this.map.put("RS", Rating.NONE);
            this.map.put("SU", Rating.NONE);
            this.map.put("UR", Rating.NONE);
        }

        @Override
        public Object adapt(String in) {
            return getRating(in).getValueDescriptor();
        }

        private Rating getRating(Object o) {
            final Rating result = this.map.get(o);
            if (result == null) {
                this.logger.warn("<getRating> unknown rating '" + o + "' using NONE");
                return Rating.NONE;
            }
            return result;
        }
    };

    // check for empty or "n/a" values
    ObjectAdapter TIMEFRAME_ADAPTER = new ObjectAdapter() {

        @Override
        public Object adapt(String in) {
            if (StringUtils.isEmpty(in) || in.equalsIgnoreCase("n/a")) {
                return null;
            }
            return in;
        }

    };

}
