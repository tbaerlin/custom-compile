/*
 * TermVisitor.java
 *
 * Created on 30.03.12 12:28
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.finder;

/**
 * @author zzhao
 */
public interface TermVisitor {

    void visit(Terms.AndOp term);

    void visit(Terms.OrOp term);

    void visit(Terms.NotOp term);

    void visit(Terms.In term);

    void visit(Terms.Relation term);
}
