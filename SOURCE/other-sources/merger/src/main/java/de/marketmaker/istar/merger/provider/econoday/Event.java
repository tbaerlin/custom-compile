/*
 * Event.java
 *
 * Created on 15.03.12 15:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author zzhao
 */
public class Event extends Version {

    private final String code;

    private String name;

    private String country;

    private FrequencyEnum frequency;

    private String definition;

    private String description;

    Event(long revision, String code) {
        super(revision);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getCountry() {
        return country;
    }

    void setCountry(String country) {
        this.country = country;
    }

    public FrequencyEnum getFrequency() {
        return frequency;
    }

    void setFrequency(FrequencyEnum frequency) {
        this.frequency = frequency;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return code.equals(((Event) o).code);

    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return "Event{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", frequency=" + frequency +
                '}';
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(new Event(1,"1"));
        }
        try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(os.toByteArray()))) {
            System.out.println(is.readObject());
        }
    }
}
