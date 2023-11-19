package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.expression;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;

import java.util.List;
import java.util.Stack;

/**
 * Author: umaurer
 * Created: 23.01.14
 */
public class ExpressionHandler {
    private final Context context;

    public ExpressionHandler(Context context) {
        this.context = context;
    }

    public int getIntValue(String expression) {
        return evaluateInt(parse(expression));
    }

/*
    public boolean getBooleanValue(String expression) {
        return evaluateBoolean(parse(expression));
    }
*/

    private Expression parse(String expression) {
        final char[] chars = expression.toCharArray();
        final StringBuilder sb = new StringBuilder();
        final Stack<Expression> stack = new Stack<Expression>();
        Expression e = new Expression();
        for (char c : chars) {
            switch (c) {
                case ' ':
                    // ignore
                    break;
                case ',':
                    setNameAndResetSb(e, sb);
                    if (e.hasNoName() || stack.isEmpty()) {
                        throw new IllegalStateException("Comma not expected here"); // $NON-NLS$
                    }
                    stack.peek().addParam(e);
                    e = new Expression();
                    break;
                case '(':
                    setNameAndResetSb(e, sb);
                    stack.push(e);
                    e = new Expression();
                    break;
                case ')':
                    setNameAndResetSb(e, sb);
                    final Expression tmp = e;
                    e = stack.pop();
                    e.addParam(tmp);
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        setNameAndResetSb(e, sb);
        return e;
    }

    private int evaluateInt(Expression e) {
        final String name = e.getName();
        if ("$sum".equals(name)) { // $NON-NLS$
            return sum(e);
        }
        else if ("$enumCount".equals(name)) { // $NON-NLS$
            return enumCount(e);
        }
        return Integer.parseInt(name);
    }

    private int sum(Expression e) {
        int sum = 0;
        for (Expression param : e.getParams()) {
            sum += evaluateInt(param);
        }
        return sum;
    }

    private int enumCount(Expression e) {
        final List<Expression> params = e.getParams();
        if (params.size() != 1) {
            throw new IllegalArgumentException("$enumCount has too much params: " + e); // $NON-NLS$
        }
        return context.getEnumCount(BindToken.create(evaluateString(params.get(0))));
    }

    private void setNameAndResetSb(Expression e, StringBuilder sb) {
        if (sb.length() > 0) {
            e.setName(sb.toString());
            sb.setLength(0);
        }
    }

/*
    private boolean evaluateBoolean(Expression e) {
        final String name = e.getName();
        if ("$isDataNull".equals(name)) { // $NON-NLS$
            return isDataNull(e);
        }
        else if ("$isDataNotNull".equals(name)) { // $NON-NLS$
            return !isDataNull(e);
        }
        return Boolean.parseBoolean(name);
    }
*/

/*
    private boolean isDataNull(Expression e) {
        final List<Expression> params = e.getParams();
        if (params.size() != 1) {
            throw new IllegalArgumentException(e.getName() + " has too much params: " + e); // $NON-NLS$
        }
        return getProperty(params.get(0)) == null;
    }
*/

/*
    private SpsLeafProperty getProperty(Expression e) {
        return this.context.getLeaf(evaluateString(e));
    }
*/

    private String evaluateString(Expression e) {
        return e.getName();
    }
}
