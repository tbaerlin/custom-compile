/*
 * ValidatorGroup.java
 *
 * Created on 15.01.13 11:30
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import java.util.HashSet;

/**
 * @author Markus Dick
 */
public class ValidatorGroup {
    private final HashSet<ValidatorHost> validators = new HashSet<ValidatorHost>();

    public ValidatorGroup attach(ValidatorHost validator) {
        this.validators.add(validator);
        return this;
    }

    public ValidatorGroup detach(ValidatorHost validator) {
        this.validators.remove(validator);
        return this;
    }

    public boolean isValid() {
        boolean valid = true;

        for(ValidatorHost v:  this.validators) {
            v.validate();
            valid &= v.isValid();
        }

        return valid;
    }
}
