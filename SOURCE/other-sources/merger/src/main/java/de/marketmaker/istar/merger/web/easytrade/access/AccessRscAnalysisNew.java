package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.Access.SYMBOL_DEFAULT;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_STRING_ARRAY;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.domain.data.StockAnalysis;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zzhao
 */
@RequiredArgsConstructor
public class AccessRscAnalysisNew implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final StockAnalysis analysis = (StockAnalysis) model.get("analysis");
    final boolean hasData = analysis != null && StringUtils.isNotBlank(analysis.getId());
    final AccessStatus status = hasData ? AccessStatus.OK : AccessStatus.NO_DATA;

    return Access.of(getAnalysisId(req), getProviderId(req), status);
  }

  private String getAnalysisId(AtomRequest request) {
    final String[] param = getParameter(request, "analysisid");
    return (param.length > 0) ? param[0] : SYMBOL_DEFAULT;
  }

  private String getProviderId(AtomRequest request) {
    final String[] param = getParameter(request, "providerId");
    return (param.length > 0) ? param[0] : SYMBOL_DEFAULT;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return EMPTY_STRING_ARRAY;
  }
}
