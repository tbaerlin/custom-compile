package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.RatioFieldDescription.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

/**
 * To read version 7 RDA files
 *
 * @deprecated only for testing the old RDA files (version <= 7), use {@link RDAReader8} instead.
 * @version 7
 * @author ytas
 */
@Deprecated
public class RDAReader7 {

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println("RDAReader7 {src_rda} {type} {fieldName}");
      System.exit(1);
    }

    final Path src = Paths.get(args[0]);
    final InstrumentTypeEnum insType = InstrumentTypeEnum.valueOf(args[1].toUpperCase());
    final TypeData typeData = new TypeData(insType);

    final FileRatioDataReader7 reader =
        new FileRatioDataReader7(src.toFile(), typeData.getType(), typeData);
    reader.read();

    // fieldName is given
    if (args.length == 3) {
      final Field field = RatioFieldDescription.getFieldByName(args[2]);
      if (field == null) {
        System.err.printf("field: %s not found%n", args[2]);
        System.exit(1);
      }
      final Predicate<Integer> filterByFieldId = fieldId -> field.id() == fieldId;
      typeData.getRatioDatas()
          .forEach(ratioData -> System.out.println(RatioDataUtil.toDebugString(typeData, ratioData, filterByFieldId)));
    } else {
      // print all fields
      typeData.getRatioDatas()
          .forEach(ratioData -> System.out.println(RatioDataUtil.toDebugString(typeData, ratioData)));
    }
  }
}
