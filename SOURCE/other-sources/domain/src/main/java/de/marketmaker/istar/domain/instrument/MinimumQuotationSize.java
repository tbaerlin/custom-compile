/*
* MinimumQuotationSize.java
*
* Created on 15.02.2006
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/

package de.marketmaker.istar.domain.instrument;

import de.marketmaker.istar.domain.Currency;

/*
 * @author Martin Wilke
 *
*/

public interface MinimumQuotationSize {

    Number getNumber();

    Unit getUnit();

    Currency getCurrency();

    boolean isUnitPercent();
    
    enum Unit {
        NOTHING(0),
        UNIT(1),
        PERCENT(2),
        PERMILLE(3),
        POINT(4),
        OTHER(9);

        private final int id;

        private Unit(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static Unit valueOf(int id) {
            switch (id) {
                case 0:
                    return NOTHING;
                case 1:
                    return UNIT;
                case 2:
                    return PERCENT;
                case 3:
                    return PERMILLE;
                case 4:
                    return POINT;
                case 9:
                    return OTHER;
                default:
                    throw new IllegalArgumentException("unknown unit id: " + id);
            }
        }
    }

}
