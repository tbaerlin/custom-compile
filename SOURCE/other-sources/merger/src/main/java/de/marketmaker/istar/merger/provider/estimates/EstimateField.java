/*
 * EstimateField.java
 *
 * Created on 10.09.2008 07:16:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.estimates;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
* @author Thomas Kiesgen
*/
public class EstimateField {
    private BigDecimal value;
    private Integer numberOfAnalysts;
    private Integer upRevisions;
    private Integer downRevisions;
    private Integer unchangedRevisions;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Integer getNumberOfAnalysts() {
        return numberOfAnalysts;
    }

    public void setNumberOfAnalysts(Integer numberOfAnalysts) {
        this.numberOfAnalysts = numberOfAnalysts;
    }

    public Integer getUpRevisions() {
        return upRevisions;
    }

    public void setUpRevisions(Integer upRevisions) {
        this.upRevisions = upRevisions;
    }

    public Integer getDownRevisions() {
        return downRevisions;
    }

    public void setDownRevisions(Integer downRevisions) {
        this.downRevisions = downRevisions;
    }

    public Integer getUnchangedRevisions() {
        return unchangedRevisions;
    }

    public void setUnchangedRevisions(Integer unchangedRevisions) {
        this.unchangedRevisions = unchangedRevisions;
    }
}
