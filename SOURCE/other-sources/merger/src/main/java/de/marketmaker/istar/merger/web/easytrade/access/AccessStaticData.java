package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;

import de.marketmaker.istar.domain.data.MasterData;
import de.marketmaker.istar.domain.data.NullMasterDataBond;
import de.marketmaker.istar.domain.data.NullMasterDataCertificate;
import de.marketmaker.istar.domain.data.NullMasterDataFund;
import de.marketmaker.istar.domain.data.NullMasterDataStock;
import de.marketmaker.istar.domain.data.NullMasterDataWarrant;
import de.marketmaker.istar.domain.data.WMData;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;

/**
 * @author zzhao
 */
public class AccessStaticData implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    // supports only one requested symbol
    final String atomName = req.getName();
    final Quote quote = (Quote) model.get("quote");
    switch (atomName) {
      case "FUT_StaticData":
        return Access.of(quote, getAccessStatus(model, "quote"));
      case "GIS_StaticData":
        return Access.of(quote, getAccessStatus(model, "dzrecord"));
      case "OPT_StaticData":
        return Access.of(quote, getAccessStatus(model, "contractSize"));
      case "WM_StaticData":
        final WMData wmData = (WMData) model.get("wmData");
        return Access.of(quote,
            wmData != null && wmData.getFields() != null && !wmData.getFields().isEmpty()
                ? AccessStatus.OK : AccessStatus.NO_DATA);
      default:
        final MasterData masterData = (MasterData) model.get("masterData");
        return Access.of(quote, getAccessStatus(masterData));
    }
  }

  private AccessStatus getAccessStatus(MasterData masterData) {
    if (masterData == null) {
      return AccessStatus.NO_DATA;
    }

    if (masterData instanceof NullMasterDataBond
        || masterData instanceof NullMasterDataFund
        || masterData instanceof NullMasterDataCertificate
        || masterData instanceof NullMasterDataStock
        || masterData instanceof NullMasterDataWarrant
    ) {
      return AccessStatus.NO_DATA;
    }

    return AccessStatus.OK;
  }

  private AccessStatus getAccessStatus(Map<String, Object> model, String key) {
    return model.get(key) == null ? AccessStatus.NO_DATA : AccessStatus.OK;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
