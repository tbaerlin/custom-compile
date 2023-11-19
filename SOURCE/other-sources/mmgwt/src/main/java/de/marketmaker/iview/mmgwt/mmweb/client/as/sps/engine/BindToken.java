package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * Created on 25.03.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class BindToken {
    public static final char CHAR_INVALIDATION = '\\';
    public static final char CHAR_TOKEN_SEPARATOR = '/';
    public static final char CHAR_INDEX_START = '[';
    public static final char CHAR_INDEX_END = ']';
    public static final char CHAR_VAR_START = '{';
    public static final char CHAR_VAR_END = '}';
    public static final BindToken EMPTY_ROOT_TOKEN = new BindToken(null, true, false);
    private final boolean absolute;
    private final BindKey bindKey;
    private final boolean containsVariable;

    public static BindToken create(String bindKeys) {
        return create(null, bindKeys);
    }

    public static BindToken create(BindToken token, String bindKeysString) {
        if (bindKeysString == null) {
            throw new NullPointerException("bindKeys is null"); // $NON-NLS$
        }
        final BindKeys bindKeys = split(bindKeysString);
        final BindKey bindKey = bindKeys.getFirst();
        final boolean absolute = isAbsolute(bindKeysString);
        if (token == null || absolute) {
            if (bindKeysString.isEmpty()) {
                throw new IllegalStateException("nonempty bind expected (parent token is null)"); // $NON-NLS$
            }
            return new BindToken(bindKey, absolute, bindKeys.containsVariable);
        }
        else if (token.bindKey == null) {
            if (bindKeysString.isEmpty()) {
                throw new IllegalStateException("nonempty bind expected (parent token.bindKey is null)"); // $NON-NLS$
            }
            return new BindToken(bindKey, token.isAbsolute(), bindKeys.containsVariable);
        }
        else {
            final BindKey bindKeyParent = copy(token.bindKey);
            getLast(bindKeyParent).setNext(bindKey);
            return new BindToken(bindKeyParent, token.isAbsolute(), bindKeys.containsVariable);
        }
    }

    public static BindToken copy(BindToken me) {
        if(me == null) {
            return null;
        }
        if(me == BindToken.EMPTY_ROOT_TOKEN) {
            return BindToken.EMPTY_ROOT_TOKEN;
        }

        return new BindToken(copy(me.bindKey), me.absolute, me.containsVariable);
    }

    public static BindToken resolveVariables(BindToken parentToken, BindToken bindToken, Context context) {
        final boolean absolute = isAbsolute(parentToken, bindToken, context);
        return new BindToken(copyAndResolve(bindToken.getHead(), parentToken, context), absolute, false);
    }

    private static boolean isAbsolute(BindToken parentToken, BindToken bindToken, Context context) {
        boolean absolute = bindToken.isAbsolute();
        if (absolute || !(bindToken.getHead() instanceof BindKeyVariable)) {
            return absolute;
        }
        final BindToken absoluteToken = BindToken.create(parentToken, ((BindKeyVariable) bindToken.getHead()).getBindKeys());
        final SpsLeafProperty varProp = (SpsLeafProperty) context.getRootProp().get(absoluteToken.getHead());
        if (varProp.getStringValue() == null) {
            throw new IllegalStateException("variableBindKeys property value must not be null!"); // $NON-NLS$
        }
        return  StringUtil.startsWith(varProp.getStringValue(), CHAR_TOKEN_SEPARATOR);
    }

    public BindKey getLast() {
        return getLast(this.bindKey);
    }

    private static BindKey copy(BindKey bindKey) {
        return _copy(bindKey, null, null);
    }

    private static BindKey copyAndResolve(BindKey bindKey, BindToken parentToken, Context context) {
        return _copy(bindKey, parentToken, context);
    }

    private static BindKey _copy(BindKey bindKey, BindToken parentToken, Context context) {
        final BindKey newBindKey;
        if (bindKey instanceof BindKeyIndexed) {
            newBindKey = new BindKeyIndexed(((BindKeyIndexed) bindKey).getIndex());
        }
        else if (bindKey instanceof BindKeyNamed) {
            newBindKey = new BindKeyNamed(((BindKeyNamed) bindKey).getName());
        }
        else if (bindKey instanceof BindKeyVariable) {
            if (context == null) {
                newBindKey = new BindKeyVariable(((BindKeyVariable) bindKey).getBindKeys());
            }
            else {
                final BindToken token = BindToken.create(parentToken, ((BindKeyVariable) bindKey).getBindKeys());
                final SpsLeafProperty varProp = (SpsLeafProperty) context.getRootProp().get(token.getHead());
                newBindKey = new BindKeyNamed(varProp.getStringValue());
            }
        }
        else {
            throw new IllegalArgumentException("unhandled BindKey type: " + bindKey.getClass().getName()); // $NON-NLS$
        }
        if (bindKey.getNext() != null) {
            newBindKey.setNext(_copy(bindKey.getNext(), parentToken, context));
        }
        return newBindKey;
    }

    private static BindKey getLast(BindKey bindKey) {
        if(bindKey == null) {
            return null;
        }
        while (bindKey.getNext() != null) {
            bindKey = bindKey.getNext();
        }
        return bindKey;
    }

    BindToken(BindKey bindKey, boolean absolute, boolean containsVariable) {
        this.bindKey = bindKey;
        this.absolute = absolute;
        this.containsVariable = containsVariable;
    }

    private static boolean isAbsolute(String bindKeys) {
        return bindKeys.length() > 0
                && bindKeys.charAt(0) == CHAR_TOKEN_SEPARATOR;
    }

    private enum State {
        NORMAL, INVALIDATE, LIST_ID, LIST_END, VAR, VAR_END
    }

    static class BindKeys {
        private BindKey first = null;
        private BindKey last = null;
        private boolean containsVariable = false;

        void appendNamed(StringBuilder sb) {
            final String name = sb.toString();
            sb.setLength(0);
            if (name.isEmpty()) {
                throw new IllegalArgumentException("empty name"); // $NON-NLS$
            }
            append(new BindKeyNamed(name));
        }

        void appendIndexed(StringBuilder sb) {
            final String index = sb.toString();
            sb.setLength(0);
            if (!index.matches("[0-9]+")) { // $NON-NLS$
                throw new IllegalArgumentException("invalid list index: " + index); // $NON-NLS$
            }
            append(new BindKeyIndexed(Integer.valueOf(index)));
        }

        void appendVariable(StringBuilder sb) {
            this.containsVariable = true;
            final String bindKeys = sb.toString();
            sb.setLength(0);
            if (bindKeys.isEmpty()) {
                throw new IllegalArgumentException("empty bindKeys"); // $NON-NLS$
            }
            append(new BindKeyVariable(bindKeys));
        }


        private void append(BindKey bindKey) {
            if (this.first == null) {
                this.first = bindKey;
                this.last = bindKey;
            }
            else {
                bindKey.setPrev(this.last);
                this.last.setNext(bindKey);
                this.last = bindKey;
            }
        }

        private BindKey getFirst() {
            return this.first;
        }
    }

    /**
     * Split bindKeys while evaluating // as single / with a bindKey.
     */
    private static BindKeys split(String bindKeys) {
        if (isAbsolute(bindKeys)) {
            bindKeys = bindKeys.substring(1);
        }
        final BindKeys result = new BindKeys();
        if (bindKeys.isEmpty()) {
            return result;
        }
        final StringBuilder sb = new StringBuilder(bindKeys.length());
        State state = State.NORMAL;
        final char[] chars = bindKeys.toCharArray();
        for (char c : chars) {
            switch (state) {
                case NORMAL:
                    if (c == CHAR_INVALIDATION) {
                        state = State.INVALIDATE;
                    }
                    else if (c == CHAR_INDEX_START) {
                        if (sb.length() != 0) {
                            result.appendNamed(sb);
                        }
                        state = State.LIST_ID;
                    }
                    else if (c == CHAR_TOKEN_SEPARATOR) {
                        result.appendNamed(sb);
                    }
                    else if (c == CHAR_VAR_START) {
                        if (sb.length() > 0) {
                            throw new IllegalStateException("unexpected text before '" + CHAR_VAR_START + "': " + sb); // $NON-NLS$
                        }
                        state = State.VAR;
                    }
                    else {
                        sb.append(c);
                    }
                    break;
                case INVALIDATE:
                    sb.append(c);
                    state = State.NORMAL;
                    break;
                case LIST_ID:
                    if (c == CHAR_INDEX_END) {
                        result.appendIndexed(sb);
                        state = State.LIST_END;
                    }
                    else if (c >= '0' && c <= '9') {
                        sb.append(c);
                    }
                    else {
                        throw new IllegalArgumentException("illegal index character: " + c); // $NON-NLS$
                    }
                    break;
                case LIST_END:
                    if (c == CHAR_TOKEN_SEPARATOR) {
                        state = State.NORMAL;
                    }
                    else {
                        throw new IllegalArgumentException("unexpected character after list: " + c); // $NON-NLS$
                    }
                    break;
                case VAR:
                    if (c == CHAR_VAR_END) {
                        result.appendVariable(sb);
                        state = State.VAR_END;
                    }
                    else {
                        sb.append(c);
                    }
                    break;
                case VAR_END:
                    if (c == CHAR_TOKEN_SEPARATOR) {
                        state = State.NORMAL;
                    }
                    else {
                        throw new IllegalArgumentException("unexpected character after variable: " + c); // $NON-NLS$
                    }
                    break;
                default:
                    throw new IllegalStateException("unhandled state: " + state); // $NON-NLS$
            }
        }

        switch (state) {
            case NORMAL:
                result.appendNamed(sb);
                break;
            case INVALIDATE:
                throw new IllegalArgumentException("unexpected end after " + CHAR_INVALIDATION); // $NON-NLS$
            case LIST_ID:
                throw new IllegalArgumentException("unexpected end after " + CHAR_INDEX_START); // $NON-NLS$
            case LIST_END:
                // ok
                break;
            case VAR:
                throw new IllegalArgumentException("unexpected end after " + CHAR_VAR_START); // $NON-NLS$
            case VAR_END:
                // ok
                break;
            default:
                throw new IllegalStateException("unhandled state: " + state); // $NON-NLS$
        }
        return result;
    }

    public String getConcatBindKey() {
        return getConcatBindKey(this.bindKey, this.absolute);
    }

    public static String getConcatBindKey(BindKey bindKey, boolean absolute) {
        if (bindKey == null) {
            return absolute ? "/" : "";
        }
        final StringBuilder sb = new StringBuilder();
        while (bindKey != null) {
            if (bindKey instanceof BindKeyIndexed) {
                sb.append(CHAR_INDEX_START).append(((BindKeyIndexed) bindKey).getIndex()).append(CHAR_INDEX_END);
            }
            else if (bindKey instanceof BindKeyNamed) {
                handleAbsolute(absolute, sb);
                final String name = ((BindKeyNamed) bindKey).getName();
                if(name != null) {
                    appendEncoded(sb, name);
                }
                else {
                    appendEncoded(sb, "{ERROR: named bind key's name is null}"); // $NON-NLS$
                }
            }
            else if (bindKey instanceof BindKeyVariable) {
                handleAbsolute(absolute, sb);
                sb.append(CHAR_VAR_START).append(((BindKeyVariable) bindKey).getBindKeys()).append(CHAR_VAR_END);
            }
            else {
                throw new IllegalArgumentException("unhandled BindKey type: " + bindKey.getClass().getName()); // $NON-NLS$
            }
            bindKey = bindKey.getNext();
        }
        return sb.toString();
    }

    private static void handleAbsolute(boolean absolute, StringBuilder sb) {
        if (sb.length() > 0 || absolute) {
            sb.append(CHAR_TOKEN_SEPARATOR);
        }
    }

    private static void appendEncoded(StringBuilder sb, String name) {
        for (char c : name.toCharArray()) {
            switch (c) {
                case '/':
                case '[':
                    sb.append('\\');
                default:
                    sb.append(c);
            }
        }
    }

    public BindKey getHead() {
        return this.bindKey;
    }

    public boolean isAbsolute() {
        return this.absolute;
    }

    public BindToken append(String bindKeys) {
        return BindToken.create(this, bindKeys);
    }

    public boolean containsVariable() {
        return this.containsVariable;
    }

    @Override
    public String toString() {
        return getConcatBindKey();
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }
}