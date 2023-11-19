/*
 * IllegalRequestException.java
 *
 * Created on 01.12.2006 10:20:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BadRequestException extends MergerException {

  public BadRequestException(String message) {
    super(message);
  }

  public String getCode() {
    return "request.invalid";
  }
}
