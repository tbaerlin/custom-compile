package de.marketmaker.istar.merger.query;

/**
 * @author mwohlf
 */
public class BeanCriterionException extends IllegalArgumentException {

    BeanCriterionException(String message) {
        super(message);
    }

    public BeanCriterionException(String message, Throwable ex) {
        super(message, ex);
    }
}
