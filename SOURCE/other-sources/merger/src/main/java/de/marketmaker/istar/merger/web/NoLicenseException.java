/*
 * NoProfileException.java
 *
 * Created on 29.03.2007 10:08:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NoLicenseException extends MergerException {

  public NoLicenseException(String message) {
    super(message);
  }


  public String getCode() {
    return "request.no-license";
  }
}
