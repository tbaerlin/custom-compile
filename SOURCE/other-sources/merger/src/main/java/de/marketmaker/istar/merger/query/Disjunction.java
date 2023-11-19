package de.marketmaker.istar.merger.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mwohlf
 */
public class Disjunction extends BeanCriterion {

    private final List<BeanCriterion> subCriteria;

    Disjunction() {
        this.subCriteria = new ArrayList<>(3);
    }

    Disjunction add(BeanCriterion criterion) {
        this.subCriteria.add(criterion);
        return this;
    }


    @Override
    public boolean evaluate(Object bean) {
        for (BeanCriterion criterion : subCriteria) {
            if (criterion.evaluate(bean)) {
                return true;
            }
        }
        return false;
    }

}
