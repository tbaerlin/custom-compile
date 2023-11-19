package de.marketmaker.istar.merger.query;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.marketmaker.istar.merger.web.finder.Term;
import de.marketmaker.istar.merger.web.finder.TermVisitor;
import de.marketmaker.istar.merger.web.finder.Terms;

/**
 * create criteria based on a term's syntax tree
 * call visit to start the double dispatch for a term object and call getCriterion()
 * to retrieve the created criteria
 *
 * there is a special hack implemented which allows  to query multiple properties by
 * using "@" in a property name  (e.g. foo@bar == 'bla'  --> foo == 'bla' OR bar == 'bla')
 *
 * @author mwohlf
 */
public class BeanSearchTermVisitor implements TermVisitor {

    private final LinkedList<BeanCriterion> stack = new LinkedList<>();

    @Override
    public void visit(Terms.AndOp term) {
        final List<Term> subTerms = term.getTerms();
        for (Term subTerm : subTerms) {
            subTerm.accept(this);
        }
        final Conjunction conjunction = new Conjunction();
        for (int i = subTerms.size(); i > 0; i--) {
            conjunction.add(stack.pop());
        }
        stack.push(conjunction);
    }

    @Override
    public void visit(Terms.OrOp term) {
        final List<Term> subTerms = term.getTerms();
        for (Term subTerm : subTerms) {
            subTerm.accept(this);
        }
        final Disjunction disjunction = new Disjunction();
        for (int i = subTerms.size(); i > 0; i--) {
            disjunction.add(stack.pop());
        }
        stack.push(disjunction);
    }

    @Override
    public void visit(Terms.NotOp term) {
        term.getTerm().accept(this);
        final BeanCriterion not = Restrictions.not(stack.pop());
        stack.push(not);
    }

    @Override
    public void visit(Terms.In term) {
        String propertyName = term.getIdentifier();
        Set<String> values = term.getValues();
        if (!propertyName.contains("@")) {
            BeanCriterion in = Restrictions.in(propertyName, values);
            stack.push(in);
        } else {
            final Disjunction disjunction = Restrictions.or();
            final String[] strings = propertyName.split("@");
            for (final String string : strings) {
                disjunction.add(Restrictions.in(string, values));
            }
            stack.push(disjunction);
        }
    }

    @Override
    public void visit(Terms.Relation term) {
        String propertyName = term.getIdentifier();
        Terms.Relation.Op operator = term.getOp();
        String value = term.getValue();
        if (!propertyName.contains("@")) {
            BeanCriterion relation = createCriterionForOperator(operator, propertyName, value);
            stack.push(relation);
        } else {
            final Disjunction disjunction = Restrictions.or();
            final String[] strings = propertyName.split("@");
            for (final String string : strings) {
                disjunction.add(createCriterionForOperator(operator, string, value));
            }
            stack.push(disjunction);
        }
    }

    private BeanCriterion createCriterionForOperator(Terms.Relation.Op operator, String property, String value) {
        switch (operator) {
            case EQ:
                return Restrictions.eq(property, value);
            case NEQ:
                return Restrictions.not(Restrictions.eq(property, value));
            case GT:
                return Restrictions.gt(property, value);
            case GTE:
                return Restrictions.gte(property, value);
            case LT:
                return Restrictions.lt(property, value);
            case LTE:
                return Restrictions.lte(property, value);
            case MATCHES:
                return Restrictions.match(property, value);
            default:
                throw new BeanCriterionException("no idea what the operator '" + operator +"'"
                        + " is supposed to do, the property was: '" + property + "', the value is '" + value + "'");
        }
    }

    public BeanCriterion getCriterion() {
        return stack.pop();
    }

}
