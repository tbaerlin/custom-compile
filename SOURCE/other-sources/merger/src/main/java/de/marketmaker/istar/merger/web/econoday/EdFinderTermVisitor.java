/*
 * EdFinderTermVisitor.java
 *
 * Created on 30.03.12 12:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.econoday;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.merger.web.finder.Term;
import de.marketmaker.istar.merger.web.finder.TermVisitor;
import de.marketmaker.istar.merger.web.finder.Terms;

/**
 * @author zzhao
 */
public class EdFinderTermVisitor implements TermVisitor {

    private static final String PARAN_LEFT = "(";

    private static final String PARAN_RIGHT = ")";

    private static final String MY_SQL_OP_OR = " OR ";

    private static final String MY_SQL_OP_AND = " AND ";

    private static final String MY_SQL_OP_IN = " IN ";

    private static final String MY_SQL_OP_NOT = "NOT";

    private static final String SEP_OR = "@";

    private static final String RELEASED_ON = "r.released_on";

    private static final String RELEASED_ON_DATE = "DATE(r.released_on)";

    private final StringBuilder sb = new StringBuilder();

    private boolean negation = false;

    @Override
    public void visit(Terms.AndOp term) {
        final List<Term> terms = term.getTerms();
        if (terms.size() > 1) {
            this.sb.append(PARAN_LEFT);
        }
        for (Term subTerm : terms) {
            subTerm.accept(this);
            this.sb.append(MY_SQL_OP_AND);
        }
        this.sb.setLength(this.sb.length() - MY_SQL_OP_AND.length());
        if (terms.size() > 1) {
            this.sb.append(PARAN_RIGHT);
        }
    }

    @Override
    public void visit(Terms.OrOp term) {
        final List<Term> terms = term.getTerms();
        if (terms.size() > 1) {
            this.sb.append(PARAN_LEFT);
        }
        for (Term subTerm : terms) {
            subTerm.accept(this);
            this.sb.append(MY_SQL_OP_OR);
        }
        this.sb.setLength(this.sb.length() - MY_SQL_OP_OR.length());
        if (terms.size() > 1) {
            this.sb.append(PARAN_RIGHT);
        }
    }

    @Override
    public void visit(Terms.NotOp term) {
        this.negation = true;
        term.getTerm().accept(this);
        this.negation = false;
    }

    @Override
    public void visit(Terms.In term) {
        this.sb.append(getEdFinderId(term.getIdentifier()));
        if (this.negation) {
            this.sb.append(" ").append(MY_SQL_OP_NOT);
        }
        this.sb.append(MY_SQL_OP_IN).append(PARAN_LEFT);
        for (String val : term.getValues()) {
            this.sb.append("'").append(val).append("',");
        }
        this.sb.replace(this.sb.length() - 1, this.sb.length(), PARAN_RIGHT);
    }

    @Override
    public void visit(Terms.Relation term) {
        final String[] split = term.getValue().split(SEP_OR);
        if (split.length > 1) {
            this.sb.append(PARAN_LEFT);
        }
        final String identifier = getEdFinderId(term.getIdentifier());
        final String mySqlOp = getMySqlOp(term.getOp());
        for (String val : split) {
            final String cleanVal = StringUtils.trimAllWhitespace(val);
            this.sb.append(getLeftOperand(identifier, cleanVal))
                    .append(" ").append(mySqlOp).append(" '")
                    .append(cleanVal).append("'").append(MY_SQL_OP_OR);
        }
        this.sb.setLength(this.sb.length() - MY_SQL_OP_OR.length());
        if (split.length > 1) {
            this.sb.append(PARAN_RIGHT);
        }
    }

    public static String getLeftOperand(String identifier, String val) {
        if (RELEASED_ON.equals(identifier)) {
            if (P_DATE.matcher(val).matches()) {
                return RELEASED_ON_DATE;
            }
        }
        return identifier;
    }

    private static final Pattern P_DATE = Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2}$");

    private String getEdFinderId(String identifier) {
        if ("country".equalsIgnoreCase(identifier)) {
            return "b.country";
        }
        else if ("eventCode".equalsIgnoreCase(identifier)) {
            return "r.event_code";
        }
        else if ("releasedOn".equalsIgnoreCase(identifier)) {
            return RELEASED_ON;
        }
        throw new UnsupportedOperationException("not implemented for: " + identifier);
    }

    private String getMySqlOp(Terms.Relation.Op op) {
        switch (op) {
            case EQ:
                return "=";
            case GT:
                return ">";
            case GTE:
                return ">=";
            case LT:
                return "<";
            case LTE:
                return "<=";
            case NEQ:
                return "!=";
            default:
                throw new UnsupportedOperationException("no support for: " + op);
        }
    }

    public String getMySqlQuery() {
        return this.sb.toString();
    }

    public void reset() {
        this.sb.setLength(0);
    }
}
