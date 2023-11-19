/*
 * MMTalkTableRequest.java
 *
 * Created on 29.10.2008 14:34:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mm;

import java.util.List;

import org.springframework.util.StringUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MMTalkTableRequest extends MMServiceRequest {
    static final long serialVersionUID = 1L;

    private String contextHandle;

    private String preFormula;

    public MMTalkTableRequest(MMKeyType keytype) {
        super(keytype);
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        if (StringUtils.hasText(this.contextHandle)) {
            sb.append(", context=").append(this.contextHandle);
        }
        if (StringUtils.hasText(this.preFormula)) {
            sb.append(", preFormula=").append(this.preFormula);
        }
    }

    public MMTalkTableRequest appendFormula(String formula) {
        doAddFormula(formula);
        return this;
    }

    public MMTalkTableRequest appendKey(String key) {
        doAddKey(key);
        return this;
    }

    public String getContextHandle() {
        return this.contextHandle;
    }

    public String getPreFormula() {
        return this.preFormula;
    }

    public MMTalkTableRequest withContextHandle(String contextHandle) {
        this.contextHandle = contextHandle;
        return this;
    }

    public MMTalkTableRequest withFormula(String formula) {
        this.formulas.clear();
        doAddFormula(formula);
        return this;
    }

    public MMTalkTableRequest withFormulas(List<String> formulas) {
        this.formulas.clear();
        for (String formula : formulas) {
            doAddFormula(formula);
        }
        return this;
    }

    public MMTalkTableRequest withKey(String key) {
        this.keys.clear();
        doAddKey(key);
        return this;
    }

    public MMTalkTableRequest withKeys(List<String> keys) {
        this.keys.clear();
        for (String key : keys) {
            doAddKey(key);
        }
        return this;
    }

    public MMTalkTableRequest withPreFormula(String preFormula) {
        this.preFormula = preFormula;
        return this;
    }
}
