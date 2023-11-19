package de.marketmaker.istar.merger.query;

import de.marketmaker.istar.merger.web.finder.Query2Term;
import de.marketmaker.istar.merger.web.finder.Term;
import org.antlr.runtime.RecognitionException;

import java.util.Collection;
import java.util.HashSet;

/**
 * a filter for beans, can be configured with a filter expression in the constructor
 * expressions might be: propertyname == 'value'
 *
 * @param <T> the bean class we want to filter
 * @author mwohlf
 *
 */
public class BeanFilter<T> {

    private final String filterExpression;

    public BeanFilter(String filterExpression) {
        this.filterExpression = filterExpression;
    }

    public Collection<T> filter(Collection<T> input) throws RecognitionException {
        BeanCriterion criterion = createCriteria();
        HashSet<T> output = new HashSet<>();
        for (T element : input) {
            if (criterion.evaluate(element)) {
                output.add(element);
            }
        }
        return output;
    }

    BeanCriterion createCriteria() throws RecognitionException {
        final Term term = Query2Term.toTerm(filterExpression);
        BeanSearchTermVisitor visitor = new BeanSearchTermVisitor();
        term.accept(visitor);
        return visitor.getCriterion();
    }

}
