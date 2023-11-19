package de.marketmaker.istar.merger.provider.cdapi;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.FieldMask;
import de.marketmaker.istar.merger.provider.cdapi.AbstractCDApiResponse.Result;
import de.marketmaker.istar.merger.util.grpc.GrpcChannelManager;
import de.marketmaker.istar.merger.web.oauth.AccessTokenProvider;
import dev.infrontfinance.grpc.cdapi.common.CommonServiceGrpc;
import dev.infrontfinance.grpc.cdapi.common.CommonServiceGrpc.CommonServiceBlockingStub;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommon.ListListingsRequest;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommon.ListListingsRequest.Builder;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommon.ListListingsResponse;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommon.ListingsData;
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
public class CommonDataProviderImpl extends AbstractCDApiProvider implements CommonDataProvider {

  /**
   * Metrics Keys
   */
  private static final String COREDATA_API_EDIDATA_FETCH = "coredata.api.common.fetch";

  private CommonServiceBlockingStub commonServiceStub;

  public CommonDataProviderImpl(GrpcChannelManager channelManager, AccessTokenProvider provider) {
    super(channelManager, provider);
    this.commonServiceStub = CommonServiceGrpc.newBlockingStub(channel());
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
                    .addPaths("issuer.common.sector.id")
                    .addPaths("issuer.common.sector.name")
                    .addPaths("issuer.common.sub_sector.id")
                    .addPaths("issuer.common.sub_sector.name")
                    .build());
    // add all ISINs as a parameter of the request
    isins.stream().filter(Objects::nonNull).forEach(builder::addIsin);

    final ListListingsRequest request = builder.build();

    ListListingsResponse listingResponse;
    try {
      listingResponse =
          this.commonServiceStub.withCallCredentials(callCredentials()).listListings(request);

    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Code.UNAVAILABLE) {
        // create new channel to retry one more
        this.commonServiceStub = CommonServiceGrpc.newBlockingStub(channel());
      }
      listingResponse =
          this.commonServiceStub.withCallCredentials(callCredentials()).listListings(request);
    }
    return listingResponse;
  }

  private List<CommonDataResponse> doFetchData(String locale, List<String> isins) {

    if (isins == null || isins.size() == 0) {
      throw new IllegalArgumentException("ISINs are not provided.");
    }

    final ListListingsResponse listingResponse = doFetchListingData(locale, isins);

    final List<CommonDataResponse> responses = new ArrayList<>();
    if (listingResponse.getDataCount() == 0) {
      return responses;
    }

    // we create a non-resizing hashmap, size < capacity * loadfactor + 1
    final Map<String, ListingsData> isinToListing = new HashMap<>(listingResponse.getDataCount() + 1, 1);

    for (final ListingsData listingsData : listingResponse.getDataList()) {
      final String isin = listingsData.getInstrument().getCommon().getIsin();
      isinToListing.put(isin, listingsData);
    }

    // return the response list by comparing with the given isins
    isins.forEach(
        isin -> {
          final ListingsData data = isinToListing.get(isin);
          if (data == null) {
            // unknown isins mapped with a NULL response in order to keep the order
            responses.add(new CommonDataResponse(Result.NULL).withIsin(isin));
          } else {
            // we represent all fields in a flat structure (no hierarchy). So, we merge maps all
            // together.
            // In case duplicate keys (normally this should not happen) the latest one overrides
            final Map<FieldDescriptor, Object> mappedFields =
                Stream.of(
                        data.getListing().getCommon().getAllFields(),
                        data.getIssuer().getCommon().getSector().getAllFields(),
                        data.getIssuer().getCommon().getSubSector().getAllFields()
                    )
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v2));

            if (mappedFields.size() == 0) {
              // also no fields means a NULL result
              responses.add(new CommonDataResponse(Result.NULL).withIsin(isin));
            } else {
              final CommonDataResponse response =
                  new CommonDataResponse(Result.SUCCESS, locale).withIsin(isin);
              mappedFields.forEach(response::addFieldValueWithPrefix);

              responses.add(response);
            }
          }
        });

    return responses;
  }

  @Override
  public List<CommonDataResponse> fetchCommonData(String locale, List<String> isins) {
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
      return Collections.singletonList(new CommonDataResponse(status));
    } catch (Exception e) {
      // unknown errors must be logged
      log.error("<fetchEdiData> cannot fetch data from CDAPI, SYMBOL: {}", String.join(", ", isins), e);
      return Collections.singletonList(new CommonDataResponse(Status.UNKNOWN));
    }
  }


}
