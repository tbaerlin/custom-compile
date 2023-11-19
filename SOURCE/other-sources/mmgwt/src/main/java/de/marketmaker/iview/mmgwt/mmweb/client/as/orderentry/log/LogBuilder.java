/*
 * LogBuilder.java
 *
 * Created on 04.02.13 13:21
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.log;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.LinkedList;

/**
 * @author Markus Dick
 */
public class LogBuilder {
    private final StringBuilder log = new StringBuilder();
    private int objectDepth = 0;
    private int listDepth = 0;
    private SeparatorState separatorState = new SeparatorState(false);
    private final LinkedList<SeparatorState> separatorsStates = new LinkedList<SeparatorState>();

    public LogBuilder() {
        addUserData();
    }

    public LogBuilder(String name) {
        this.log.append("<").append(name).append(">"); //$NON-NLS$
        addUserData();
    }

    protected void addUserData() {
        add("user.login", SessionData.INSTANCE.getUser().getLogin()); //$NON-NLS$
        add("user.vwdId", SessionData.INSTANCE.getUser().getVwdId()); //$NON-NLS$
    }

    public LogBuilder add(String parameter, Object value) {
        if(value == null) {
            return add(parameter, null, false);
        }
        return add(parameter, String.valueOf(value), false);
    }

    public LogBuilder addEnum(String parameter, Enum value) {
        if(value == null) {
            return add(parameter, null, false);
        }

        add(parameter + ".name", value.name(), false); //$NON-NLS$
        add(parameter + ".ordinal", Integer.toString(value.ordinal()), false); //$NON-NLS$

        return this;
    }

    public LogBuilder add(String parameter, String value) {
        return add(parameter, value, true);
    }

    public LogBuilder add(String parameter, String value, boolean wrapValueWithQuotes) {
        final boolean withQuotes = value != null && wrapValueWithQuotes;
        addSeparator();

        this.log.append(parameter);
        this.log.append("="); //$NON-NLS$
        if(withQuotes) this.log.append("\""); //$NON-NLS$
        this.log.append(value);
        if(withQuotes) this.log.append("\""); //$NON-NLS$

        return this;
    }

    public LogBuilder add(String message) {
        addSeparator();

        this.log.append(message);

        return this;
    }

    public LogBuilder beginComplexParameter(String parameter) {
        addSeparator();

        this.log.append(parameter);
        this.log.append("="); //$NON-NLS$

        pushSeparatorState();

        return this;
    }

    public LogBuilder endComplexParameter() {
        pullSeparatorState();
        return this;
    }

    public LogBuilder beginList() {
        addSeparator();
        pushSeparatorState();

        this.listDepth++;
        this.log.append("[ "); //$NON-NLS$
        return this;
    }

    public LogBuilder endList() {
        pullSeparatorState();

        if(this.listDepth-- > 0) {
            this.log.append(" ]"); //$NON-NLS$
        }
        return this;
    }

    public LogBuilder beginObject(String name) {
        addSeparator();
        pushSeparatorState();

        this.objectDepth++;
        if(StringUtil.hasText(name)) {
            this.log.append(name).append(" ");
        }
        this.log.append("{ ");
        return this;
    }

    public LogBuilder endObject() {
        pullSeparatorState();

        if(this.objectDepth-- > 0) {
           this.log.append(" }"); //$NON-NLS$
        }
        return this;
    }

    private void pushSeparatorState() {
        this.separatorsStates.addLast(this.separatorState);
        this.separatorState = new SeparatorState(false);
    }

    private void pullSeparatorState() {
        this.separatorState = this.separatorsStates.removeLast();
    }

    private void addSeparator() {
        if(this.separatorState.isAdd()) {
            this.log.append(", "); //$NON-NLS$
        }
        else {
            this.separatorState.setAdd(true);
        }
    }

    @Override
    public String toString() {
        return log.toString() + ";";
    }

    private static final class SeparatorState {
        private boolean add;

        private SeparatorState(boolean state) {
            this.add = state;
        }

        private boolean isAdd() {
            return this.add;
        }

        private void setAdd(boolean add) {
            this.add = add;
        }
    }
}
