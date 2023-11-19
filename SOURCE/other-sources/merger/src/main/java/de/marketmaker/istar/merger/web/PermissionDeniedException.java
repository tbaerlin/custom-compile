/*
 * PermissionDeniedException.java
 *
 * Created on 17.09.12 10:49
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.MergerException;
import java.util.Arrays;

/**
 * @author oflege
 */
public class PermissionDeniedException extends MergerException {

  public PermissionDeniedException(String s) {
    super(s);
  }

  public PermissionDeniedException(Selector... missingSelectors) {
    this(toInts(missingSelectors));
  }

  public PermissionDeniedException(int... missingSelectors) {
    super("missing selectors: " + Arrays.toString(missingSelectors));
  }

  private static int[] toInts(Selector[] missingSelectors) {
    final int[] result = new int[missingSelectors.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = missingSelectors[i].getId();
    }
    return result;
  }

  @Override
  public String getCode() {
    return "permission.denied";
  }
}
