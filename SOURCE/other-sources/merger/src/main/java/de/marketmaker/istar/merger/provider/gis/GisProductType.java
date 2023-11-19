/*
 * DocumentType.java
 *
 * Created on 22.02.19 14:20
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gis;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author twiegel
 */
public enum GisProductType {
  Flowprodukt,
  Zinsprodukt,
  Zeichnungsprodukt,
  Sonstige;

  private static final Map<String, GisProductType> BY_VALUE =
      EnumSet.allOf(GisProductType.class)
          .stream()
          .collect(Collectors.toMap(Enum::name, Function.identity()));
  public static GisProductType DEFAULT = Zeichnungsprodukt;

  public static GisProductType resolve(String dzProductType) {
    return BY_VALUE.getOrDefault(dzProductType, DEFAULT);
  }
}