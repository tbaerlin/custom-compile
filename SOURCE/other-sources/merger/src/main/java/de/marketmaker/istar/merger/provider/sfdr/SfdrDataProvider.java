package de.marketmaker.istar.merger.provider.sfdr;

import java.util.List;

public interface SfdrDataProvider {

  /**
   * Queries the SFDR data directly from the COREDATA API.
   *
   * @param locale language code with small letters like en, de, etc.
   * @param isins A list of ISIN.
   * @return list of SfdrDataResponse
   */
  List<SfdrDataResponse> fetchSfdrData(String locale, List<String> isins);

}
