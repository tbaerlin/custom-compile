package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;

import de.marketmaker.istar.domain.data.RatingData;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @author hummell
 */
@Slf4j
public class AccessMscRatingData implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Quote quote = (Quote) model.get("quote");

    final RatingData ratingData = (RatingData) model.get("ratingData");
    if (ratingData == null || !hasData(ratingData)) {
      return toAccess(quote, getMajorSymbols(req), AccessStatus.NO_DATA);
    }
    return toAccess(quote, getMajorSymbols(req), AccessStatus.OK);
  }

  private boolean hasData(RatingData ratingData) {
    boolean hasData = false;
    if (ratingData.getRatingFitchST() != null) {
      hasData = true;
    }
    if (ratingData.getRatingFitchIssuerST() != null) {
      hasData = true;
    }
    if (ratingData.getRatingMoodysST() != null) {
      hasData = true;
    }
    if (ratingData.getRatingSnPST() != null) {
      hasData = true;
    }
    if (ratingData.getRatingSnPLocalST() != null) {
      hasData = true;
    }
    return hasData;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
