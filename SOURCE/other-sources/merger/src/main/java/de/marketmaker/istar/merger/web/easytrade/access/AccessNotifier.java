package de.marketmaker.istar.merger.web.easytrade.access;

import dev.infrontfinance.dm.proto.Access.Molecule;

/**
 * @author zzhao
 */
public interface AccessNotifier {

  void notify(Molecule.Builder builder);
}
