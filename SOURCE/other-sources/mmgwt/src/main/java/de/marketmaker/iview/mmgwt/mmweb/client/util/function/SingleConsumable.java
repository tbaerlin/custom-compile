/*
 * SingleConsumable.java
 *
 * Created on 11.12.2015 12:04
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util.function;

import java.util.Optional;

/**
 * Stores a value until it as been consumed.
 * Returns only optionals to ease usage.
 * Maybe useful, e.g., for handling pending block parameters, IDs, requests, etc.
 * @author mdick
 */
public final class SingleConsumable<T> {
    private T consumable = null;

    public void push(T value) {
        this.consumable = value;
    }

    public Optional<T> pull() {
        if (this.consumable == null) {
            return Optional.empty();
        }

        final T currentValue = this.consumable;
        this.consumable = null;
        return Optional.of(currentValue);
    }
}
