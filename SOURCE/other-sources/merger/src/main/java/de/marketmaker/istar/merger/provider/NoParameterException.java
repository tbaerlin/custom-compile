/*
 * NoParameterException.java
 *
 * Created on 04.10.2006 11:39:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NoParameterException extends MergerException {

  public NoParameterException(String message) {
    super(message);
  }

  public String getCode() {
    return "parameter.missing";
  }
}
