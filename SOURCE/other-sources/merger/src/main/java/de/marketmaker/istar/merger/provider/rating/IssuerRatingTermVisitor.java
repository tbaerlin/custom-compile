/*
 * ISRFinderTermVisitor.java
 *
 * Created on 07.05.12 16:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import java.util.List;
import java.util.Stack;

import de.marketmaker.istar.domainimpl.rating.RatingSystemProvider;
import de.marketmaker.istar.merger.web.finder.Term;
import de.marketmaker.istar.merger.web.finder.TermVisitor;
import de.marketmaker.istar.merger.web.finder.Terms;

/**
 * @author zzhao
 */
public class IssuerRatingTermVisitor implements TermVisitor {

    public static final String SEP_OR = "@";

    private final Stack<IssuerRatingFilter> stack;

    private final RatingSystemProvider ratingSystemProvider;

    public IssuerRatingTermVisitor(RatingSystemProvider ratingSystemProvider) {
        this.ratingSystemProvider = ratingSystemProvider;
        this.stack = new Stack<>();
    }

    @Override
    public void visit(Terms.AndOp term) {
        final List<Term> subTerms = term.getTerms();
        for (int i = subTerms.size() - 1; i >= 0; i--) {
            subTerms.get(i).accept(this);
        }
        final IssuerRatingFilter.And and = new IssuerRatingFilter.And();
        for (int i = 0; i < subTerms.size(); i++) {
            and.add(this.stack.pop());
        }
        this.stack.push(and);
    }

    @Override
    public void visit(Terms.OrOp term) {
        final List<Term> subTerms = term.getTerms();
        for (int i = subTerms.size() - 1; i >= 0; i--) {
            subTerms.get(i).accept(this);
        }
        final IssuerRatingFilter.Or or = new IssuerRatingFilter.Or();
        for (int i = 0; i < subTerms.size(); i++) {
            or.add(this.stack.pop());
        }
        this.stack.push(or);
    }

    @Override
    public void visit(Terms.NotOp term) {
        term.getTerm().accept(this);
        final IssuerRatingFilter.Not not = new IssuerRatingFilter.Not(this.stack.pop());
        this.stack.push(not);
    }

    @Override
    public void visit(Terms.In term) {
        final IssuerRatingDescriptor desc = IssuerRatingDescriptor.fromValue(term.getIdentifier());
        final IssuerRatingFilter.In in = new IssuerRatingFilter.In(desc, this.ratingSystemProvider);
        for (String val : term.getValues()) {
            in.add(val);
        }

        this.stack.push(in);
    }

    @Override
    public void visit(Terms.Relation term) {
        final IssuerRatingDescriptor desc = IssuerRatingDescriptor.fromValue(term.getIdentifier());
        // todo check op and desc combinations??
        final String[] values = term.getValue().split(SEP_OR);
        if (values.length == 1) {
            this.stack.push(new IssuerRatingFilter.Relation(desc, term.getOp(), term.getValue(),
                    this.ratingSystemProvider));
        }
        else {
            switch (desc) {
                case CURRENCYISO:
                case COUNTRYISO:
                case SOURCE:
                    final IssuerRatingFilter.In in = new IssuerRatingFilter.In(desc, this.ratingSystemProvider);
                    for (String value : values) {
                        in.add(value);
                    }
                    this.stack.push(in);
                    break;
                case ISSUERNAME:
                    final IssuerRatingFilter.Or or = new IssuerRatingFilter.Or();
                    for (String value : values) {
                        or.add(new IssuerRatingFilter.Relation(desc, term.getOp(), value,
                                this.ratingSystemProvider));
                    }
                    this.stack.push(or);
                    break;
                default:
                    throw new UnsupportedOperationException("no support for: " + desc);
            }
        }
    }

    public IssuerRatingFilter getFilter() {
        return this.stack.pop();
    }
}
