package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.merger.provider.pages.MergerPageResponse;
import de.marketmaker.istar.merger.provider.pages.VwdPageProvider;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * @author zzhao
 */
@RequiredArgsConstructor
public class AccessMscPageDisplay implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final MergerPageResponse page = (MergerPageResponse) model.get("page");
    if (page == null) {
      return toAccess(null, getMajorSymbols(req), AccessStatus.NO_DATA);
    }

    return toAccess(null, getMajorSymbols(req),
        (VwdPageProvider.NOT_ALLOWED_DE.equalsIgnoreCase(page.getText())
            || VwdPageProvider.NOT_ALLOWED_EN.equalsIgnoreCase(page.getText()))
            ? AccessStatus.NO_DATA : AccessStatus.OK);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getParameter(req, "pageId");
  }
}
