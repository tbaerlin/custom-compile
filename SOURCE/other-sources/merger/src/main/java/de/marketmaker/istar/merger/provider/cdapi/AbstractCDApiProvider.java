package de.marketmaker.istar.merger.provider.cdapi;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import de.marketmaker.istar.merger.util.grpc.GrpcChannelManager;
import de.marketmaker.istar.merger.web.oauth.AccessTokenProvider;
import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;

/**
 * A base class to interact with Core Data Api. Specific endpoints must reside in implementations.
 */
@Slf4j
public abstract class AbstractCDApiProvider {

  private static final String BEARER_TYPE = "Bearer";

  private static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY =
      Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER);

  private static final Metadata.Key<String> ERROR_MESSAGE_METADATA_KEY =
      Metadata.Key.of("grpc-message", ASCII_STRING_MARSHALLER);

  static class TokenSuppliedCallCredentials extends CallCredentials {

    private final AccessTokenProvider tokenProvider;

    public TokenSuppliedCallCredentials(AccessTokenProvider tokenProvider) {
      this.tokenProvider = tokenProvider;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor,
        MetadataApplier applier) {
      appExecutor.execute(() -> {

        try {

          final Optional<String> token = this.tokenProvider.getToken("cdapi");
          if (!token.isPresent()) {
            // better to get/log tokenSupplier bean name
            log.error("<applyRequestMetadata> {} provided null token", this.tokenProvider);
            applier.fail(Status.UNAUTHENTICATED);
            return;
          }

          final Metadata headers = new Metadata();
          headers.put(AUTHORIZATION_METADATA_KEY,
              String.format("%s %s", BEARER_TYPE, token.get()));
          applier.apply(headers);
        } catch (Throwable e) {
          log.error("<applyRequestMetadata> authentication failed", e);
          applier.fail(Status.UNAUTHENTICATED);
        }

      });
    }

    @Override
    public void thisUsesUnstableApi() {
      //
    }
  }

  private final GrpcChannelManager channelManager;
  private final CallCredentials callCredentials;

  private MeterRegistry meterRegistry;

  public AbstractCDApiProvider(GrpcChannelManager channelManager, AccessTokenProvider provider) {
    this.channelManager = channelManager;
    this.callCredentials = new TokenSuppliedCallCredentials(provider);
  }

  protected <V> V timed(Callable<V> callable, String name, String... tags) throws Exception {
    if (this.meterRegistry != null) {
      final Sample sample = Timer.start();
      try {
        return callable.call();
      } finally {
        sample.stop(this.meterRegistry.timer(name, tags));
      }
    } else {
      return callable.call();
    }
  }

  protected ManagedChannel channel() {
    return this.channelManager.getChannel();
  }

  protected CallCredentials callCredentials() {
    return this.callCredentials;
  }

  public void setMeterRegistry(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  /**
   * Parses {@link Status} in {@link StatusRuntimeException} and
   * tries to map special error types to {@link Status.Code}.
   * This is done by parsing the error description
   *
   * @param e exception
   * @return a better Status with correct code
   */
  protected Status parseException(StatusRuntimeException e) {

    final Status status = e.getStatus();
    if (Status.UNKNOWN.getCode() != status.getCode()) {
      return status;
    }

    final Metadata metadata = e.getTrailers();
    if (metadata == null) {
      return status;
    }

    // if the Status is UNKNOWN
    // then try to parse known errors from status description
    final String grpcMessage = metadata.get(ERROR_MESSAGE_METADATA_KEY);
    String type = "";
    String message = "";
    if(grpcMessage != null) {
      try {
        message = URLDecoder.decode(grpcMessage, "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        log.warn("error occured when decoding grpc error message", ex);
      }
      final int index = message.indexOf(":");
      if (index > 0) {
        type = message.substring(0, index).trim();
        // to omit stacktrace
        final int index2 = message.indexOf("\n", index);
        message = message.substring(index + 1, index2).trim();
      }
    }
    // KNOWN ERRORS
    // It's likely to increase, so we keep it as a switch statement
    switch (type) {
      case "TOKEN_INVALID":
        return Status.UNAUTHENTICATED.withDescription(message);
      case "ASSERT_ISIN":
        return Status.INVALID_ARGUMENT.withDescription(message);
      default:
        return status;
    }

  }

}
