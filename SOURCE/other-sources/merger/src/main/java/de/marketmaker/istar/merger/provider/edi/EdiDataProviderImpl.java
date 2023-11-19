package de.marketmaker.istar.merger.provider.edi;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.FieldMask;
import de.marketmaker.istar.merger.provider.cdapi.AbstractCDApiProvider;
import de.marketmaker.istar.merger.provider.cdapi.AbstractCDApiResponse.Result;
import de.marketmaker.istar.merger.util.grpc.GrpcChannelManager;
import de.marketmaker.istar.merger.web.oauth.AccessTokenProvider;
import dev.infrontfinance.grpc.cdapi.bond.BondServiceGrpc;
import dev.infrontfinance.grpc.cdapi.bond.BondServiceGrpc.BondServiceBlockingStub;
import dev.infrontfinance.grpc.cdapi.bond.CdApiBond.InterestRateCalculationMethodsData;
import dev.infrontfinance.grpc.cdapi.bond.CdApiBond.InterestRatePeriodsData;
import dev.infrontfinance.grpc.cdapi.bond.CdApiBond.ListInterestRateCalculationMethodsResponse;
import dev.infrontfinance.grpc.cdapi.bond.CdApiBond.ListInterestRatePeriodsResponse;
import dev.infrontfinance.grpc.cdapi.bond.CdApiBond.ListListingsRequest;
import dev.infrontfinance.grpc.cdapi.bond.CdApiBond.ListListingsRequest.Builder;
import dev.infrontfinance.grpc.cdapi.bond.CdApiBond.ListListingsResponse;
import dev.infrontfinance.grpc.cdapi.bond.CdApiBond.ListingsData;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EdiDataProviderImpl extends AbstractCDApiProvider implements EdiDataProvider {

  /**
   * Metrics Keys
   */
  private static final String COREDATA_API_EDIDATA_FETCH = "coredata.api.edidata.fetch";

  private BondServiceBlockingStub bondServiceStub;

  public EdiDataProviderImpl(GrpcChannelManager channelManager, AccessTokenProvider provider) {
    super(channelManager, provider);
    this.bondServiceStub = BondServiceGrpc.newBlockingStub(channel());
  }

  private ListListingsResponse doFetchListingData(String locale, List<String> isins) {

    if (isins == null || isins.size() == 0) {
      throw new IllegalArgumentException("ISINs are not provided.");
    }

    final Builder builder =
        ListListingsRequest.newBuilder()
            .setOffset(0)
            .setLimit(isins.size())
            .setFields(
                FieldMask.newBuilder()
                    .addPaths("instrument.common.isin")
                    .addPaths("instrument.bond_edi")
                    .addPaths("classification.bond.bond_category")
                    .addPaths("classification.bond.bond_type")
                    .build());
    // add all ISINs as a parameter of the request
    isins.stream().filter(Objects::nonNull).forEach(builder::addIsin);

    final ListListingsRequest request = builder.build();

    ListListingsResponse listingResponse;
    try {
      listingResponse =
          this.bondServiceStub.withCallCredentials(callCredentials()).listListings(request);

    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Code.UNAVAILABLE) {
        // create new channel to retry one more
        this.bondServiceStub = BondServiceGrpc.newBlockingStub(channel());
      }
      listingResponse =
          this.bondServiceStub.withCallCredentials(callCredentials()).listListings(request);
    }
    return listingResponse;
  }

  private ListInterestRatePeriodsResponse doFetchInterestRatePeriodsData(String locale, List<String> isins) {

    if (isins == null || isins.size() == 0) {
      throw new IllegalArgumentException("ISINs are not provided.");
    }

    final Builder builder =
        ListListingsRequest.newBuilder()
            .setOffset(0)
            .setLimit(isins.size())
            .setFields(
                FieldMask.newBuilder()
                    .addPaths("instrument.common.isin")
                    .addPaths("instrument.bond.interest_rate_period.code")
                    .addPaths("instrument.bond.interest_rate_period.name")
                    .build());
    // add all ISINs as a parameter of the request
    isins.forEach(builder::addIsin);

    final ListListingsRequest request = builder.build();

    ListInterestRatePeriodsResponse listInterestRatePeriodsResponse;
    try {
      listInterestRatePeriodsResponse =
          this.bondServiceStub.withCallCredentials(callCredentials()).listInterestRatePeriods(request);

    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Code.UNAVAILABLE) {
        // create new channel to retry one more
        this.bondServiceStub = BondServiceGrpc.newBlockingStub(channel());
      }
      listInterestRatePeriodsResponse =
          this.bondServiceStub.withCallCredentials(callCredentials()).listInterestRatePeriods(request);
    }

    return listInterestRatePeriodsResponse;
  }

  private ListInterestRateCalculationMethodsResponse doFetchInterestRateCalculationMethodsData(String locale, List<String> isins) {

    if (isins == null || isins.size() == 0) {
      throw new IllegalArgumentException("ISINs are not provided.");
    }

    final Builder builder =
        ListListingsRequest.newBuilder()
            .setOffset(0)
            .setLimit(isins.size())
            .setFields(
                FieldMask.newBuilder()
                    .addPaths("instrument.common.isin")
                    .addPaths("instrument.bond.interest_rate_calculation_method.code")
                    .addPaths("instrument.bond.interest_rate_calculation_method.name")
                    .build());
    // add all ISINs as a parameter of the request
    isins.forEach(builder::addIsin);

    final ListListingsRequest request = builder.build();

    ListInterestRateCalculationMethodsResponse listInterestRatePeriodsResponse;
    try {
      listInterestRatePeriodsResponse =
          this.bondServiceStub.withCallCredentials(callCredentials()).listInterestRateCalculationMethods(request);

    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Code.UNAVAILABLE) {
        // create new channel to retry one more
        this.bondServiceStub = BondServiceGrpc.newBlockingStub(channel());
      }
      listInterestRatePeriodsResponse =
          this.bondServiceStub.withCallCredentials(callCredentials()).listInterestRateCalculationMethods(request);
    }

    return listInterestRatePeriodsResponse;
  }

  private List<EdiDataResponse> doFetchData(String locale, List<String> isins) {

    if (isins == null || isins.size() == 0) {
      throw new IllegalArgumentException("ISINs are not provided.");
    }

    final ListListingsResponse listingResponse = doFetchListingData(locale, isins);
    final ListInterestRatePeriodsResponse listInterestRatePeriodsResponse = doFetchInterestRatePeriodsData(locale, isins);
    final ListInterestRateCalculationMethodsResponse listInterestRateCalculationMethodsResponse = doFetchInterestRateCalculationMethodsData(locale, isins);

    final List<EdiDataResponse> responses = new ArrayList<>();
    if (listingResponse.getDataCount() == 0) {
      return responses;
    }

    // we create a non-resizing hashmap, size < capacity * loadfactor + 1
    final Map<String, ListingsData> isinToListing = new HashMap<>(listingResponse.getDataCount() + 1, 1);
    final Map<String, InterestRatePeriodsData> isinToInterestRatePeriodsData = new HashMap<>(listInterestRatePeriodsResponse.getDataCount() + 1, 1);
    final Map<String, InterestRateCalculationMethodsData> isinToInterestRateCalculationMethodsData = new HashMap<>(listInterestRateCalculationMethodsResponse.getDataCount() + 1, 1);
    int i = 0;
    for (final ListingsData listingsData : listingResponse.getDataList()) {
      final String isin = listingsData.getInstrument().getCommon().getIsin();
      isinToListing.put(isin, listingsData);
      if (listInterestRatePeriodsResponse.getDataCount() > i) {
        isinToInterestRatePeriodsData.put(isin, listInterestRatePeriodsResponse.getData(i));
      }
      if (listInterestRateCalculationMethodsResponse.getDataCount() > i) {
        isinToInterestRateCalculationMethodsData.put(
            isin, listInterestRateCalculationMethodsResponse.getData(i));
        }
      i++;
    }

    // return the response list by comparing with the given isins
    isins.forEach(
        isin -> {
          final ListingsData data = isinToListing.get(isin);
          final InterestRatePeriodsData interestRatePeriodsData =
              isinToInterestRatePeriodsData.get(isin);
          final InterestRateCalculationMethodsData interestRateCalculationMethodsData =
              isinToInterestRateCalculationMethodsData.get(isin);
          if (data == null) {
            // unknown isins mapped with a NULL response in order to keep the order
            responses.add(new EdiDataResponse(Result.NULL).withIsin(isin));
          } else {
            // we represent all fields in a flat structure (no hierarchy). So, we merge maps all
            // together.
            // In case duplicate keys (normally this should not happen) the latest one overrides
            final Map<FieldDescriptor, Object> mappedFields =
                Stream.of(
                        data.getListing().getCommon().getAllFields(),
                        data.getInstrument().getBondEdi().getAllFields(),
                        data.getClassification().getBond().getAllFields())
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v2));

            if (mappedFields.size() == 0) {
              // also no fields means a NULL result
              responses.add(new EdiDataResponse(Result.NULL).withIsin(isin));
            } else {
              final EdiDataResponse response =
                  new EdiDataResponse(Result.SUCCESS, locale).withIsin(isin);
              mappedFields.forEach(response::addFieldValue);

              if (interestRatePeriodsData != null) {
                final Map<FieldDescriptor, Object> interestRatePeriodsDataFields =
                    interestRatePeriodsData.getInterestRatePeriod().getAllFields();

                for (Map.Entry<FieldDescriptor, Object> e :
                    interestRatePeriodsDataFields.entrySet()) {
                  response.addPrefixedFieldValue("interestRatePeriod", e.getKey(), e.getValue());
                }
              }

              if (interestRateCalculationMethodsData != null) {
                final Map<FieldDescriptor, Object> interestRateCalculationMethodsDataFields =
                    interestRateCalculationMethodsData
                        .getInterestRateCalculationMethod()
                        .getAllFields();

                for (Map.Entry<FieldDescriptor, Object> e :
                    interestRateCalculationMethodsDataFields.entrySet()) {
                  response.addPrefixedFieldValue(
                      "interestRateCalculationMethod", e.getKey(), e.getValue());
                }
              }

              responses.add(response);
            }
          }
        });

    return responses;
  }

  @Override
  public List<EdiDataResponse> fetchEdiData(String locale, List<String> isins) {
    try {
      return timed(() -> doFetchData(locale, isins), COREDATA_API_EDIDATA_FETCH);
    } catch (StatusRuntimeException e) {
      final Status status = parseException(e);
      if (status.getCode() == Code.UNAUTHENTICATED) {
        log.error("<fetchEdiData> authentication error on CDAPI, SYMBOL: {}, detail: {}", String.join(", ", isins), status.getDescription(), e);
      }
      if (status.getCode() != Code.INVALID_ARGUMENT) {
        // unknown errors must be logged
        log.error("<fetchEdiData> cannot fetch data from CDAPI, SYMBOL: {}", String.join(", ", isins), e);
      }
      return Collections.singletonList(new EdiDataResponse(status));
    } catch (Exception e) {
      // unknown errors must be logged
      log.error("<fetchEdiData> cannot fetch data from CDAPI, SYMBOL: {}", String.join(", ", isins), e);
      return Collections.singletonList(new EdiDataResponse(Status.UNKNOWN));
    }
  }


}
