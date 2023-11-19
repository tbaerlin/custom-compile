/*
 * IstarListRequest.java
 *
 * Created on 07.05.12 15:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.web.finder.Term;

/**
 * @author zzhao
 */
public class IstarQueryListRequest extends IstarListRequest {

    static final long serialVersionUID = 1266447034787695647L;

    private final String query;

    private final Term queryTerm;

    private final Profile profile;

    public IstarQueryListRequest(int offset, int count, String sortBy, boolean ascending,
            String query) {
        super(offset, count, sortBy, ascending);
        this.query = query;
        this.queryTerm = null;
        this.profile = null;
    }

    // TODO: we should split this into two classes imho
    public IstarQueryListRequest(int offset, int count, String sortBy, boolean ascending,
            Term queryTerm, Profile profile) {
        super(offset, count, sortBy, ascending);
        this.query = null;
        this.queryTerm = queryTerm;
        this.profile = profile;
    }

    public String getQuery() {
        return query;
    }

    public Term getQueryTerm() {
        return queryTerm;
    }

    public Profile getProfile() {
        return profile;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        sb.append(", query=").append(query != null ? query : queryTerm);
    }
}
