/*
 * MMServiceRequest.java
 *
 * Created on 29.10.2008 14:47:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mm;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class MMServiceRequest extends AbstractIstarRequest {
    protected static final long serialVersionUID = 1L;

    protected final List<String> formulas = new ArrayList<>();

    protected final List<String> keys = new ArrayList<>();

    protected final List<MMPriceUpdate> priceUpdates = new ArrayList<>();

    private final MMKeyType keytype;

    protected MMServiceRequest(MMKeyType keytype) {
        this.keytype = keytype;
    }

    public final List<String> getFormulas() {
        return Collections.unmodifiableList(this.formulas);
    }

    public final List<String> getKeys() {
        return Collections.unmodifiableList(this.keys);
    }

    public List<MMPriceUpdate> getPriceUpdates() {
        // TODO: check for null instead of using readResolve
        // TODO: remove null check after general update in production
        if (this.priceUpdates == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.priceUpdates);
    }

    public final MMKeyType getKeytype() {
        return this.keytype;
    }

    protected void doAddFormula(String f) {
        if (!StringUtils.hasText(f)) {
            throw new IllegalArgumentException("illegal formula '" + f + "'");
        }
        this.formulas.add(f);
    }

    protected void doAddKey(String k) {
        if (!StringUtils.hasText(k)) {
            throw new IllegalArgumentException("illegal key '" + k + "'");
        }
        this.keys.add(k);
    }

    protected void doAddPriceUpdate(MMPriceUpdate priceUpdate) {
        if (priceUpdate != null) {
            this.priceUpdates.add(priceUpdate);
        }
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        sb.append(", keytype=").append(this.keytype)
            .append(", keys=").append(this.keys)
            .append(", formulas=").append(this.formulas);
        if (!this.priceUpdates.isEmpty()) {
            sb.append(", updates=").append(this.priceUpdates);
        }
    }
}
