/*
 * GsonUtil.java
 *
 * Created on 15.01.2010 12:01:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;

/**
 * A Gson object is expensive to create and it caches info for de-/serializing objects,
 * but it is also thread-safe, the best way to use it is having a single instance like this
 * class does.
 * @author oflege
 */
public class GsonUtil {
    private static class DateTimeSerializer implements JsonSerializer<DateTime> {
        @Override
        public JsonElement serialize(DateTime src, Type type,
                JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(src.toString());
        }
    }

    private static class DateTimeDeserializer implements JsonDeserializer<DateTime> {
        @Override
        public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new DateTime(json.getAsJsonPrimitive().getAsString());
        }
    }
    private static class LocalDateSerializer implements JsonSerializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate src, Type type,
            JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(src.toString());
        }
    }

    private static class LocalDateDeserializer implements JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            return new LocalDate(json.getAsJsonPrimitive().getAsString());
        }
    }

    private static class PeriodSerializer implements JsonSerializer<Period> {
        @Override
        public JsonElement serialize(Period src, Type type,
                JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(src.toString());
        }
    }

    private static class PeriodDeserializer implements JsonDeserializer<Period> {
        @Override
        public Period deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new Period(json.getAsJsonPrimitive().getAsString());
        }
    }

    private static final Gson INSTANCE;

    static {
        final GsonBuilder b = new GsonBuilder();
        b.registerTypeAdapter(DateTime.class, new DateTimeSerializer());
        b.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        b.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
        b.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        b.registerTypeAdapter(Period.class, new PeriodSerializer());
        b.registerTypeAdapter(Period.class, new PeriodDeserializer());
        INSTANCE = b.create();
    }


    public static String toJson(Object o) {
        return INSTANCE.toJson(o);
    }

    public static <V> V fromJson(String json, Class<V> clazz) {
        return INSTANCE.fromJson(json, clazz);
    }

}
