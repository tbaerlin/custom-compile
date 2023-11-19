package de.marketmaker.istar.merger.query;

import java.util.Arrays;
import java.util.Collection;

/**
 * convenience class to work with BeanCriterions
 *
 * @author mwohlf
 */
public class Restrictions {

    // chaining

    public static Disjunction or(BeanCriterion... criteria) {
        Disjunction disjunction = new Disjunction();
        for (BeanCriterion criterion : criteria) {
            disjunction.add(criterion);
        }
        return disjunction;
    }

    public static Conjunction and(BeanCriterion... criteria) {
        Conjunction conjunction = new Conjunction();
        for (BeanCriterion criterion : criteria) {
            conjunction.add(criterion);
        }
        return conjunction;
    }

    public static BeanCriterion not(BeanCriterion beanCriterion) {
        return new BeanCriterion.Not(beanCriterion);
    }


    // creation

    public static InCriterion in(String propertyName, String... values) {
        return new InCriterion(propertyName, Arrays.asList(values));
    }

    public static InCriterion in(String propertyName, Collection<String> values) {
        return new InCriterion(propertyName, values);
    }

    public static EqualCriterion eq(String propertyName, String value) {
        return new EqualCriterion(propertyName, value);
    }

    public static GreaterCriterion gt(String propertyName, String value) {
        return new GreaterCriterion(propertyName, value);
    }

    public static LesserCriterion lt(String propertyName, String value) {
        return new LesserCriterion(propertyName, value);
    }

    public static MatchCriterion match(String propertyName, String value) {
        return new MatchCriterion(propertyName, value);
    }

    // instead of composition, do we need special classes for these?

    public static BeanCriterion gte(String propertyName, String value) {
        return new Disjunction()
                .add(new GreaterCriterion(propertyName, value))
                .add(new EqualCriterion(propertyName, value));
    }

    public static BeanCriterion lte(String propertyName, String value) {
        return new Disjunction()
                .add(new LesserCriterion(propertyName, value))
                .add(new EqualCriterion(propertyName, value));
    }

}
