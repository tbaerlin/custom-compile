package de.marketmaker.istar.merger.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mwohlf
 */
public class Conjunction extends BeanCriterion {

    private final List<BeanCriterion> subCriteria;

    Conjunction() {
        this.subCriteria = new ArrayList<>(3);
    }

    Conjunction add(BeanCriterion criterion) {
        this.subCriteria.add(criterion);
        return this;
    }


    @Override
    public boolean evaluate(Object bean) {
        for (BeanCriterion criterion : subCriteria) {
            if (!criterion.evaluate(bean)) {
                return false;
            }
        }
        return true;
    }

}
