package de.marketmaker.istar.merger.provider.sfdr;

import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import de.marketmaker.istar.merger.provider.cdapi.AbstractCDApiResponse;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommonMessage.EetSfdrPai;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommonMessage.EetSustainabilityProductInformation;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommonMessage.EetSustainabilityProductInformation.Dictionary;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommonMessage.EetSustainabilityProductInformation.TaxonomyObjectives;
import io.grpc.Status;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
public class SfdrDataResponse extends AbstractCDApiResponse {

  private String isin;

  private final Map<String, Object> spiFields = new LinkedHashMap<>(32);
  private final Map<String, Object> sfdrFields = new LinkedHashMap<>(32);

  public SfdrDataResponse(Result result) {
    super(result);
  }

  public SfdrDataResponse(Result result, String locale) {
    super(result, locale);
  }

  public SfdrDataResponse(Status errorStatus) {
    super(errorStatus);
  }

  public SfdrDataResponse withIsin(String isin) {
    this.isin = isin;
    return this;
  }

  private static class SustainabilityProductInformationView {

    /**
     * One-on-one map to message SustainabilityProductInformation.Dictionary.
     * See: <a href="https://bitbucket.org/vauwede/cdapi-monorepo/src/master/protos/dev/infrontfinance/cdapi/common/message.proto">SustainabilityProductInformation.Dictionary</a>
     */
    @Data
    @Builder
    private static class DictionaryView {
      private final String code;
      private final String name;

      public static DictionaryView build(Dictionary message, String locale) {
        return DictionaryView.builder()
            .code(resolveField(message, "code"))
            .name(resolveLocalizedField(message, "name", locale))
            .build();
      }
    }

    /**
     * One-on-one map to message SustainabilityProductInformation.Data.
     * See: <a href="https://bitbucket.org/vauwede/cdapi-monorepo/src/master/protos/dev/infrontfinance/cdapi/common/message.proto">SustainabilityProductInformation.Data</a>
     */
    @Data
    @Builder
    private static class DataView {
      private final Double minimumShare;
      private final Double reportedShare;

      public static DataView build(dev.infrontfinance.grpc.cdapi.common.CdApiCommonMessage.EetSustainabilityProductInformation.Data message) {
        return DataView.builder()
            .minimumShare(resolveField(message, "minimum_share"))
            .reportedShare(resolveField(message, "reported_share"))
            .build();
      }
    }

    /**
     * One-on-one map to message SustainabilityProductInformation.TaxonomyObjectives.
     * See: <a href="https://bitbucket.org/vauwede/cdapi-monorepo/src/master/protos/dev/infrontfinance/cdapi/common/message.proto">SustainabilityProductInformation.TaxonomyObjectives</a>
     */
    @Data
    @Builder
    private static class TaxonomyObjectivesView {
      private final Boolean hasClimateMitigation;
      private final Boolean hasClimateAdaption;
      private final Boolean hasProtectionWaterMarineResources;
      private final Boolean hasTransitionCircularEconomy;
      private final Boolean hasReductionEnvironmentalPollution;
      private final Boolean hasProtectionRestorationBiodiversityEcosystems;

      public static TaxonomyObjectivesView build(TaxonomyObjectives message) {
        return TaxonomyObjectivesView.builder()
            .hasClimateMitigation(resolveField(message, "has_climate_mitigation"))
            .hasClimateAdaption(resolveField(message, "has_climate_adaption"))
            .hasProtectionWaterMarineResources(resolveField(message, "has_protection_water_marine_resources"))
            .hasTransitionCircularEconomy(resolveField(message, "has_transition_circular_economy"))
            .hasReductionEnvironmentalPollution(resolveField(message, "has_reduction_environmental_pollution"))
            .hasProtectionRestorationBiodiversityEcosystems(resolveField(message, "has_protection_restoration_biodiversity_ecosystems"))
            .build();
      }
    }

  }

  private static class SfdrPaiView {

    /**
     * One-on-one map to message SfdrPai.Data.
     * See: <a href="https://bitbucket.org/vauwede/cdapi-monorepo/src/master/protos/dev/infrontfinance/cdapi/common/message.proto">SfdrPai.Data</a>
     */
    @Data
    @Builder
    private static class DataView {
      private final Boolean isConsidered;
      private final String id;
      private final Double value;
      private final Double coverage;
      private final Double eligibleAssets;

      public static SfdrPaiView.DataView build(dev.infrontfinance.grpc.cdapi.common.CdApiCommonMessage.EetSfdrPai.Data message) {
        return DataView.builder()
            .isConsidered(resolveField(message, "is_considered"))
            .id(resolveField(message, "id"))
            .value(resolveField(message, "value"))
            .coverage(resolveField(message, "coverage"))
            .eligibleAssets(resolveField(message, "eligible_assets"))
            .build();
      }
    }

  }

  public SfdrDataResponse fillSpiFields(Map<FieldDescriptor, Object> fieldDescriptorMap) {
    fieldDescriptorMap.forEach(this::addSpiFieldValue);
    return this;
  }

  public SfdrDataResponse fillSfdrFields(Map<FieldDescriptor, Object> fieldDescriptorMap) {
    fieldDescriptorMap.forEach(this::addSfdrFieldValue);
    return this;
  }

  public void addSpiFieldValue(FieldDescriptor field, Object value) {
    this.spiFields.put(field.getJsonName(), sanitize(field, value));
  }

  public void addSfdrFieldValue(FieldDescriptor field, Object value) {
    this.sfdrFields.put(field.getJsonName(), sanitize(field, value));
  }

  private Object sanitize(FieldDescriptor field, Object value) {
    if (field.getType() == Type.MESSAGE) {

      if (value instanceof EetSustainabilityProductInformation.Dictionary) {
        return SustainabilityProductInformationView.DictionaryView
            .build((EetSustainabilityProductInformation.Dictionary) value, this.locale);
      }

      if (value instanceof EetSustainabilityProductInformation.Data) {
        return SustainabilityProductInformationView.DataView
            .build((EetSustainabilityProductInformation.Data) value);
      }

      if (value instanceof EetSustainabilityProductInformation.TaxonomyObjectives) {
        return SustainabilityProductInformationView.TaxonomyObjectivesView
            .build((TaxonomyObjectives) value);
      }

      if (value instanceof EetSfdrPai.Data) {
        return SfdrPaiView.DataView.build((EetSfdrPai.Data) value);
      }

      if (value instanceof Collection) {
        return ((Collection<?>) value).stream()
            .map(v -> sanitize(field, v))
            .collect(Collectors.toList());
      }
    }

    if (value instanceof EnumValueDescriptor) {
      // to not break toString implementation
      return value.toString();
    }

    return nullIfEmpty(value);
  }

  public Map<String, Object> getSpiFields(String... ignoreFields) {
    Predicate<String> composite = k -> true;
    for (String field : ignoreFields) {
      composite = composite.and(k -> !k.equals(field));
    }
    return getSpiFields(composite);
  }

  public Map<String, Object> getSpiFields(Predicate<String> fieldFilter) {
    return collectFields(this.spiFields, fieldFilter);
  }

  public Map<String, Object> getSfdrFields(String... ignoreFields) {
    Predicate<String> composite = k -> true;
    for (String field : ignoreFields) {
      composite = composite.and(k -> !k.equals(field));
    }
    return getSfdrFields(composite);
  }

  public Map<String, Object> getSfdrFields(Predicate<String> fieldFilter) {
    return collectFields(this.sfdrFields, fieldFilter);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "SfdrDataResponse[", "]")
        .add("errorStatus='" + this.errorStatus + "'")
        .add("result=" + this.result)
        .add("locale='" + this.locale + "'")
        .add("isin=" + this.isin)
        .toString();
  }
}
