package de.marketmaker.istar.merger.provider.sfdr;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.FieldMask;
import de.marketmaker.istar.merger.provider.cdapi.AbstractCDApiProvider;
import de.marketmaker.istar.merger.provider.cdapi.AbstractCDApiResponse.Result;
import de.marketmaker.istar.merger.util.grpc.GrpcChannelManager;
import de.marketmaker.istar.merger.web.oauth.AccessTokenProvider;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommon.InstrumentsData;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommon.ListInstrumentsResponse;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommon.ListListingsRequest;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommon.ListListingsRequest.Builder;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommonMessage.EetSfdrPai;
import dev.infrontfinance.grpc.cdapi.common.CdApiCommonMessage.EetSustainabilityProductInformation;
import dev.infrontfinance.grpc.cdapi.common.CommonServiceGrpc;
import dev.infrontfinance.grpc.cdapi.common.CommonServiceGrpc.CommonServiceBlockingStub;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SfdrDataProviderImpl extends AbstractCDApiProvider implements SfdrDataProvider {

  /**
   * Metrics Keys
   */
  private static final String COREDATA_API_SFDRDATA_FETCH = "coredata.api.sfdrdata.fetch";

  private CommonServiceBlockingStub commonService;

  public SfdrDataProviderImpl(GrpcChannelManager channelManager, AccessTokenProvider provider) {
    super(channelManager, provider);
    this.commonService = CommonServiceGrpc.newBlockingStub(channel());
  }

  private List<SfdrDataResponse> doFetchData(String locale, List<String> isins) {

    if (isins == null || isins.size() == 0) {
      throw new IllegalArgumentException("ISINs are not provided.");
    }

    final Builder builder = ListListingsRequest.newBuilder()
        .setOffset(0)
        .setLimit(isins.size());
    // add all ISINs as a parameter of the request
    isins.stream().filter(Objects::nonNull).forEach(builder::addIsin);
    // building the query, we have to give all paths explicitly
    // otherwise CDAPI doesn't return the full data
    final FieldMask.Builder fieldMaskBuilder = FieldMask.newBuilder()
        .addPaths("instrument.common.isin");
    // add all spi paths
    EetSustainabilityProductInformation.getDescriptor().getFields().stream()
        .map(fieldDescriptor -> "eet_spi." + fieldDescriptor.getName())
        .forEach(fieldMaskBuilder::addPaths);
    // add all sfdr_pai paths
    EetSfdrPai.getDescriptor().getFields().stream()
        .map(fieldDescriptor -> "eet_sfdr_pai." + fieldDescriptor.getName())
        .forEach(fieldMaskBuilder::addPaths);
    builder.setFields(fieldMaskBuilder.build());

    final ListListingsRequest request = builder.build();
    ListInstrumentsResponse listingResponse;
    try {
      listingResponse = this.commonService.withCallCredentials(callCredentials())
          .listInstruments(request);
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Code.UNAVAILABLE) {
        // create a new channel to retry once more
        this.commonService = CommonServiceGrpc.newBlockingStub(channel());
      }
      listingResponse = this.commonService.withCallCredentials(callCredentials())
          .listInstruments(request);
    }

    final List<SfdrDataResponse> responses = new ArrayList<>();
    if (listingResponse.getDataCount() == 0) {
      return responses;
    }

    final List<InstrumentsData> dataList = listingResponse.getDataList();
    dataList.forEach(data -> {
      final String isin = data.getInstrument().getCommon().getIsin();
      final Map<FieldDescriptor, Object> spiFields = data.getEetSpi().getAllFields();
      final Map<FieldDescriptor, Object> sfdrFields = data.getEetSfdrPai().getAllFields();

      if (spiFields.isEmpty() && sfdrFields.isEmpty()) {
        responses.add(new SfdrDataResponse(Result.NULL).withIsin(isin));
        return;
      }

      final SfdrDataResponse response = new SfdrDataResponse(Result.SUCCESS, locale)
          .withIsin(isin).fillSpiFields(spiFields).fillSfdrFields(sfdrFields);
      responses.add(response);
    });

    return responses;
  }

  @Override
  public List<SfdrDataResponse> fetchSfdrData(String locale, List<String> isins) {
    try {
      return timed(() -> doFetchData(locale, isins), COREDATA_API_SFDRDATA_FETCH);
    } catch (StatusRuntimeException e) {
      final Status status = parseException(e);
      if (status.getCode() == Code.UNAUTHENTICATED) {
        log.error("<fetchSfdrData> authentication error on CDAPI, SYMBOLS: {}, detail: {}", isins, status.getDescription(), e);
      }
      if (status.getCode() != Code.INVALID_ARGUMENT) {
        // unknown errors must be logged
        log.error("<fetchSfdrData> cannot fetch data from CDAPI, SYMBOLS: {}", isins, e);
      }
      return Collections.singletonList(new SfdrDataResponse(status));
    } catch (Exception e) {
      // unknown errors must be logged
      log.error("<fetchSfdrData> cannot fetch data from CDAPI, SYMBOLS: {}", isins, e);
      return Collections.singletonList(new SfdrDataResponse(Status.UNKNOWN));
    }
  }
}
