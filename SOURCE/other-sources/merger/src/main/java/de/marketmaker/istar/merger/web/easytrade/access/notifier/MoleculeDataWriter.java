package de.marketmaker.istar.merger.web.easytrade.access.notifier;

import dev.infrontfinance.dm.kafka.utils.io.DataWriter;
import dev.infrontfinance.dm.proto.Access.Molecule;
import java.io.IOException;
import java.io.OutputStream;

public class MoleculeDataWriter implements DataWriter<Molecule> {

  @Override
  public void write(Molecule molecule, OutputStream outputStream) throws IOException {
    molecule.writeDelimitedTo(outputStream);
  }

  @Override
  public void close() throws IOException {
    // no-op
  }
}
