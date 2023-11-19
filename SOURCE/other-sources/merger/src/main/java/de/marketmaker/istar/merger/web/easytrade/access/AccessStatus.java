package de.marketmaker.istar.merger.web.easytrade.access;

import lombok.Getter;

/**
 * @author zzhao
 */
public enum AccessStatus {
  OK(0),
  NO_DATA(1),
  UNKNOWN_SYMBOL(2),
  DENIED(3),
  CANCELED(4),
  SERVER_ERROR(5),
  TIMEOUT(6),
  BAD_REQUEST(7),
  /**
   * General error, used in {@link de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController}.
   * There are many error situations raised by {@link org.springframework.validation.Errors#reject}.
   */
  ERROR(8),
  NOT_PROCESSED(9),
  //
  ;

  @Getter
  private final int code;

  AccessStatus(int code) {
    this.code = code;
  }
}
