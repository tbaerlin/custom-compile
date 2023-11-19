package de.marketmaker.istar.feed.mux;

import de.marketmaker.istar.common.nioframework.SelectorThread;
import dev.infrontfinance.dm.vertx.HealthCheckSupport;
import io.vertx.ext.healthchecks.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author zzhao
 */
@Slf4j
@RequiredArgsConstructor
public class MuxHealthCheck implements InitializingBean, DisposableBean {

  public static final String HCN_SELECTOR_THREAD = SelectorThread.class.getSimpleName();

  private final HealthCheckSupport healthCheckSupport;

  private final SelectorThread selectorThread;

  @Override
  public void destroy() throws Exception {
    this.healthCheckSupport.unregister(HCN_SELECTOR_THREAD);
    log.info("<destroy> unregistered health check on {}", HCN_SELECTOR_THREAD);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.healthCheckSupport.register(HCN_SELECTOR_THREAD, 5000, promise ->
        promise.complete(this.selectorThread.isRunning() && !this.selectorThread.isDone()
            ? Status.OK() : Status.KO()));
    log.info("<afterPropertiesSet> registered health check on {}", HCN_SELECTOR_THREAD);
  }
}
