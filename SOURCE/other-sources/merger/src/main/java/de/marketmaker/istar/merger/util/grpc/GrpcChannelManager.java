package de.marketmaker.istar.merger.util.grpc;

import io.grpc.ManagedChannel;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

/**
 * @author ytas
 */
public interface GrpcChannelManager {

  static boolean isChannelActive(ManagedChannel channel) {
    return !channel.isShutdown() && !channel.isTerminated();
  }

  static void shutdownChannel(Logger logger, ManagedChannel channel) {
    try {
      channel.shutdown();
      if (!channel.awaitTermination(10, TimeUnit.SECONDS)) {
        channel.shutdownNow();
        if (!channel.awaitTermination(10, TimeUnit.SECONDS)) {
          logger.warn("<shutdownChannel> channel did not terminate {}", channel);
        }
      }
      logger.debug("<shutdownChannel> channel shut down {}", channel);
    } catch (InterruptedException e) {
      logger.warn("<shutdownChannel> shutting down channel interrupted {}", channel, e);
      channel.shutdownNow();
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      logger.error("<shutdownChannel> cannot shut down channel {}", channel, e);
    }
  }

  ManagedChannel getChannel();
}
