package de.marketmaker.istar.merger.web.easytrade;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Size;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.easytrade.HasSymbolArray;

/**
 * Custom command for only symbol support
 */
public class SymbolArrayOnlyCommand implements HasSymbolArray {

  private String[] symbol;

  public void setSymbol(String[] symbol) {
    this.symbol = HttpRequestUtil.filterParametersWithText(symbol);
  }

  /**
   * @sample FR0010521575
   */
  @NotNull
  @Size(min = 1, max = 100)
  public String[] getSymbol() {
    return this.symbol;
  }
}
