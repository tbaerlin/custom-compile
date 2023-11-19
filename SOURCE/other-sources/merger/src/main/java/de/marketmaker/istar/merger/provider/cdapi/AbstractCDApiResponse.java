package de.marketmaker.istar.merger.provider.cdapi;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.grpc.Status;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

@Getter
public abstract class AbstractCDApiResponse {

  protected static final String DEFAULT_LOCALE = "en";

  public enum Result {
    NULL, ERROR, SUCCESS;

    public boolean hasError() {
      return this == ERROR;
    }
  }

  protected final Result result;
  protected final String locale;
  protected Status errorStatus;

  public AbstractCDApiResponse(Result result) {
    this(result, DEFAULT_LOCALE);
  }

  public AbstractCDApiResponse(Result result, String locale) {
    this.result = result;
    this.locale = locale;
  }

  public AbstractCDApiResponse(Status errorStatus) {
    this(Result.ERROR, DEFAULT_LOCALE);
    this.errorStatus = errorStatus;
  }

  /**
   * Resolve a field against the given locale.
   * Tries to resolve in the following order:
   * <ul>
   *   <li>"{fieldname}_{locale}"</li>
   *   <li>"{fieldname}_{DEFAULT_LOCALE}"</li>
   *   <li>"{fieldname}"</li>
   * </ul>
   * @param message {@link AbstractMessage}
   * @param name field name
   * @param locale locale in string, like "en", de, etc.
   * @return field value or null
   */
  protected static <T> T resolveLocalizedField(AbstractMessage message, String name, String locale) {
    return resolveFields(message, name + "_" + locale, name + "_" + DEFAULT_LOCALE, name);
  }

  /**
   * Resolve value by fieldname with multiple fallback options.
   * The fields are resolved by the given fieldname order.
   *
   * @param message {@link AbstractMessage}
   * @param fieldnames array of multiple fieldnames
   * @return field value or null
   */
  protected static <T> T resolveFields(AbstractMessage message, String... fieldnames) {
    for (String name : fieldnames) {
      final Object value = resolveField(message, name);
      if (value != null)
        return (T) value;
    }
    return null;
  }

  protected static <T> T resolveField(AbstractMessage message, String name) {
    final Descriptors.Descriptor descriptor = message.getDescriptorForType();
    final FieldDescriptor fieldDescriptor = descriptor.findFieldByName(name);
    if (fieldDescriptor == null) {
      return null;
    }
    // hasField check gives us whether the field is really set or not
    if (!message.hasField(fieldDescriptor)) {
      return null;
    }
    return (T) nullIfEmpty(message.getField(fieldDescriptor));
  }

  protected static <T> T nullIfEmpty(T value) {
    return value == null || StringUtils.isEmpty(String.valueOf(value)) ? null : value;
  }

  protected static Map<String, Object> collectFields(Map<String, Object> fieldMap, Predicate<String> fieldFilter) {
    return fieldMap.keySet().stream() // copy protection
        .filter(fieldFilter)
        .collect(Collectors.toMap(k -> k, fieldMap::get, (v1, v2) -> v2, LinkedHashMap::new));
  }

}

