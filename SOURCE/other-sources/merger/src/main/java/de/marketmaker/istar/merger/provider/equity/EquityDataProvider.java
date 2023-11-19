package de.marketmaker.istar.merger.provider.equity;

import de.marketmaker.istar.merger.provider.edi.EdiDataResponse;
import java.util.List;

public interface EquityDataProvider {

  /**
   * Queries the EDI data directly from the COREDATA API.
   * Implementations must provide the results in the order of the given ISINs.
   *
   * @param locale language code with small letters like en, de, etc.
   * @param isins A list of ISINs. Only ISIN is supported as a symbol.
   * @return response list sorted by the given isin list.
   */
  List<EquityDataResponse> fetchEquityData(String locale, List<String> isins);

}
