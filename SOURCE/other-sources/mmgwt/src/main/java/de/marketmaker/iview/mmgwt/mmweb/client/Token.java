package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;

/**
 * Created on 22.02.13 16:16
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class Token {

    private final StringBuilder token;
    private final HistoryContext context;

    public Token(String token) {
        this(token, null);
    }

    public Token(String token, HistoryContext context) {
        this.token = new StringBuilder();
        this.token.append(token);
        this.context = context;
    }

    public String getToken() {
        return token.toString();
    }

    public HistoryContext getHistoryContext() {
        return context;
    }

    public Token appendToken(String token) {
        this.token.append(token);
        return this;
    }
}
