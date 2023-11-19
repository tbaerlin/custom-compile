package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: umaurer
 * Created: 23.01.14
 */
public class Expression {
    private String name;
    private List<Expression> params;

    public void setName(String name) {
        if (this.name != null) {
            throw new IllegalStateException("cannot set name twice"); // $NON-NLS$
        }
        this.name = name;
    }

    public boolean hasNoName() {
        return this.name == null;
    }

    public String getName() {
        return name;
    }

    public void addParam(Expression expression) {
        if (this.params == null) {
            this.params = new ArrayList<Expression>();
        }
        this.params.add(expression);
    }

    public List<Expression> getParams() {
        return params;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (params != null) {
            char delim = '(';
            for (Expression param : params) {
                sb.append(delim).append(param);
                delim = ',';
            }
            sb.append(')');
        }
        return sb.toString();
    }
}
