package de.marketmaker.istar.merger.util.grpc;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Default Spring component for Grpc GrpcChannelManager. For each different targetUrl, another Spring
 * Bean should be defined. Optionally, tls=true can be defined.
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultGrpcChannelManager implements GrpcChannelManager, InitializingBean,
    DisposableBean {

  private final String targetUrl;
  private boolean tls;

  private ManagedChannel channel;

  private ManagedChannel createChannel() {
    final ManagedChannel channel;

    final ChannelCredentials credentials;
    if (this.tls) {
      credentials = TlsChannelCredentials.create();
    } else {
      credentials = InsecureChannelCredentials.create();
    }
    channel = Grpc.newChannelBuilder(this.targetUrl, credentials).build();
    log.debug("<createChannel> channel created for target: {}, tls: {}", this.targetUrl, this.tls);

    return channel;
  }

  private void initializeChannel() {
    this.channel = createChannel();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    initializeChannel();
  }

  @Override
  public ManagedChannel getChannel() {
    if (GrpcChannelManager.isChannelActive(this.channel)) {
      return this.channel;
    }
    initializeChannel();
    return this.channel;
  }

  public void setTls(boolean tls) {
    this.tls = tls;
  }

  @Override
  public void destroy() throws Exception {
    GrpcChannelManager.shutdownChannel(log, this.channel);
  }
}
