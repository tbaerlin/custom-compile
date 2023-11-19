/*
 * Terms.java
 *
 * Created on 13.08.2008 14:02:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.finder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

/**
 * Collection of static member classes that implement the {@link de.marketmaker.istar.merger.web.finder.Term}
 * interface.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Terms {
    static final Map<String, Relation.Op> OPS = new HashMap<>();

    static {
        OPS.put("=", Relation.Op.EQ);
        OPS.put("==", Relation.Op.EQ);
        OPS.put("<", Relation.Op.LT);
        OPS.put("<=", Relation.Op.LTE);
        OPS.put(">", Relation.Op.GT);
        OPS.put(">=", Relation.Op.GTE);
        OPS.put("!=", Relation.Op.NEQ);
        //OPS.put("<>", Relation.Op.NEQ);  // removed because it is not part of the grammar file
        OPS.put("=~", Relation.Op.MATCHES);
    }

    protected abstract static class Conjunction implements Term {
        static final long serialVersionUID = -5009311161627484576L;

        protected List<Term> terms;

        protected Conjunction(List<Term> terms) {
            this.terms = terms;
        }

        public List<Term> getTerms() {
            return terms;
        }
    }

    public static class OrOp extends Conjunction {
        static final long serialVersionUID = 1L;

        public OrOp(List<Term> terms) {
            super(terms);
        }

        public String toString() {
            return "(" + StringUtils.collectionToDelimitedString(terms, " ODER ") + ")";
        }

        @Override
        public void accept(TermVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static class AndOp extends Conjunction {
        static final long serialVersionUID = 1L;

        public AndOp(List<Term> terms) {
            super(terms);
        }

        public String toString() {
            return "(" + StringUtils.collectionToDelimitedString(terms, " UND ") + ")";
        }

        @Override
        public void accept(TermVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static class NotOp implements Term {
        static final long serialVersionUID = 1L;

        private final Term term;

        public NotOp(Term term) {
            this.term = term;
        }

        public Term getTerm() {
            return term;
        }

        public String toString() {
            return "!(" + term + ")";
        }

        @Override
        public void accept(TermVisitor visitor) {
            visitor.visit(this);
        }
    }


    public static class Relation implements Term {
        static final long serialVersionUID = 1L;

        public enum Op {
            EQ, NEQ, GT, GTE, LT, LTE, MATCHES
        }

        private final Op op;

        private final String identifier;

        private final String value;

        public Relation(String identifier, Op op, String value) {
            this.identifier = identifier;
            this.op = op;
            this.value = value;
        }

        public String getIdentifier() {
            return identifier;
        }

        public Op getOp() {
            return op;
        }

        public String getValue() {
            return value;
        }

        public String toString() {
            return identifier + " " + op + " " + value;
        }

        @Override
        public void accept(TermVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static class In implements Term {
        static final long serialVersionUID = 1L;

        private final String identifier;

        private final Set<String> values;

        public In(String identifier, Set<String> values) {
            this.identifier = identifier;
            this.values = values;
        }

        public String getIdentifier() {
            return identifier;
        }

        public Set<String> getValues() {
            return values;
        }

        public String toString() {
            return identifier + " IN (" + StringUtils.collectionToCommaDelimitedString(values) + ")";
        }

        @Override
        public void accept(TermVisitor visitor) {
            visitor.visit(this);
        }
    }
}
