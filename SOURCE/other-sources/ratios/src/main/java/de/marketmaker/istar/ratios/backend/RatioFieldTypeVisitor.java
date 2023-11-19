package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.ratios.RatioFieldDescription.Field;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When a new Ratio field type is introduced, each implementor of this contract must be updated.
 * Easier to track where we read ratio data based on field type.
 *
 * @author ytas
 */
public interface RatioFieldTypeVisitor {

  Logger log = LoggerFactory.getLogger(RatioFieldTypeVisitor.class);

  default void visit(Field field) {
    switch (field.type()) {
      case NUMBER:
        visitNumber(field);
        break;
      case DECIMAL:
        visitDecimal(field);
        break;
      case STRING:
        if (field.isLocalized()) {
          visitStringLocalized(field, field.getLocales());
        } else {
          visitString(field);
        }
        break;
      case DATE:
        visitDate(field);
        break;
      case TIME:
        visitTime(field);
        break;
      case TIMESTAMP:
        visitTimestamp(field);
        break;
      case BOOLEAN:
        visitBoolean(field);
        break;
      case ENUMSET:
        visitEnumset(field);
        break;
      default:
        log.warn("<visit> cannot handle type: {}, ignoring field: {}", field.type(), field);
    }

  }

  void visitNumber(Field field);

  void visitDecimal(Field field);

  void visitString(Field field);

  void visitStringLocalized(Field field, Locale[] locales);

  void visitDate(Field field);

  void visitTime(Field field);

  void visitTimestamp(Field field);

  void visitBoolean(Field field);

  void visitEnumset(Field field);

}
