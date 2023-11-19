package de.marketmaker.istar.merger.provider.cdapi;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.grpc.Status;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Getter
@Slf4j
public class CommonDataResponse extends AbstractCDApiResponse {

  @Data
  @Builder
  private static class MessageView {
    private final String code;
    private final String name;
    private final String nameShort;
    private final String nameLong;
  }

  private String isin;

  private final Map<String, Object> fields = new LinkedHashMap<>(32);

  public CommonDataResponse(Result result) {
    super(result);
  }

  public CommonDataResponse(Result result, String locale) {
    super(result, locale);
  }

  public CommonDataResponse(Status errorStatus) {
    super(errorStatus);
  }

  public CommonDataResponse withIsin(String isin) {
    this.isin = isin;
    return this;
  }

  public void addFieldValueWithPrefix(FieldDescriptor field, Object value) {

    String name = field.getFullName();

    final int idx = name.lastIndexOf(".");
    if (idx > 0) {
      final int idx2 = name.lastIndexOf(".", idx - 1);
      if (idx2 > 0) {
        name = name.substring(idx2 + 1, idx) + StringUtils.capitalize(field.getJsonName());
      }
    }

    this.fields.put(name, sanitize(field, value));
  }

  public void addFieldValue(FieldDescriptor field, Object value) {
    this.fields.put(field.getJsonName(), sanitize(field, value));
  }

  public void add(String name, Object value) {
    this.fields.put(name, value);
  }

  private Object sanitize(FieldDescriptor field, Object value) {
    if (field.getType() == FieldDescriptor.Type.MESSAGE) {
      final AbstractMessage message = ((AbstractMessage) value);

      final String code = resolveField(message, "code");
      final String name = resolveFields(message,
          "name_" + this.locale, "name_" + DEFAULT_LOCALE, "name");
      final String nameShort = resolveFields(message,
          "name_short_" + this.locale, "name_short_" + DEFAULT_LOCALE, "name_short");
      final String nameLong = resolveFields(message,
          "name_long_" + this.locale, "name_long_" + DEFAULT_LOCALE, "name_long");

      return MessageView.builder()
          .code(code)
          .name(name)
          .nameShort(nameShort)
          .nameLong(nameLong)
          .build();
    }

    if (value instanceof EnumValueDescriptor) {
      // to not break toString implementation
      return value.toString();
    }

    return nullIfEmpty(value);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "EdiDataResponse[", "]")
        .add("result=" + this.result)
        .add("locale='" + this.locale + "'")
        .add("errorStatus=" + this.errorStatus)
        .toString();
  }


}
