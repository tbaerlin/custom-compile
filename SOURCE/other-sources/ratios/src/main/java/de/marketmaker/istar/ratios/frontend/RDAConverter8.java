package de.marketmaker.istar.ratios.frontend;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * To migrate old RDA version 7 files to version 8
 *
 * @author ytas
 */
public class RDAConverter8 {

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.err.println("RDAConverter8 {src_rda} {tar_rda} {type}");
      System.exit(1);
    }

    final Path src = Paths.get(args[0]);
    final Path tar = Paths.get(args[1]);
    final InstrumentTypeEnum insType = InstrumentTypeEnum.valueOf(args[2].toUpperCase());
    final TypeData typeData = new TypeData(insType);

    final FileRatioDataReader7 reader =
        new FileRatioDataReader7(src.toFile(), typeData.getType(), typeData);
    reader.read();

    final FileRatioDataWriter writer = new FileRatioDataWriter(tar.toFile(), typeData, 8);
    writer.write();
  }
}
