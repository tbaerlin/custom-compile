package de.marketmaker.istar.merger.web.easytrade.access.notifier;

import de.marketmaker.istar.merger.web.easytrade.access.AccessNotifier;
import dev.infrontfinance.dm.proto.Access.Molecule.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zzhao
 */
@Slf4j
public class LoggingAccessNotifier implements AccessNotifier {

  @Override
  public void notify(Builder builder) {
    log.info("<notify> {}", builder.build());
  }
}
