/*
 * ExportMonitorImpl.java
 *
 * Created on 04.09.15 08:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.monitoring;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.netflix.servo.monitor.DynamicCounter;
import com.netflix.servo.monitor.MonitorConfig;

import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress;

/**
 * @author oflege
 */
final class AmqpMonitors {

    static final AmqpMonitors INSTANCE = new AmqpMonitors();

    private final ConcurrentMap<MonitorConfig, Impl> monitors = new ConcurrentHashMap<>();

    private static class Impl implements AmqpMonitor {

        private final MonitorConfig numCfg;

        private final MonitorConfig inCfg;

        private final MonitorConfig outCfg;

        public Impl(MonitorConfig cfg) {
            this.numCfg = config("amqpNumRequestsIn", cfg);
            this.inCfg = config("amqpNumBytesIn", cfg);
            this.outCfg = config("amqpNumBytesOut", cfg);
        }

        private static MonitorConfig config(String name, MonitorConfig cfg) {
            return MonitorConfig.builder(name).withTags(cfg.getTags()).build();
        }

        @Override
        public void ack(int numBytesOut, int numBytesIn) {
            DynamicCounter.increment(this.numCfg);
            DynamicCounter.increment(this.outCfg, numBytesOut);
            DynamicCounter.increment(this.inCfg, numBytesIn);
        }
    }

    private AmqpMonitors() {
    }

    AmqpMonitor getMonitor(Class<?> clazz, AmqpRpcAddress address) {
        final MonitorConfig cfg = MonitorConfig.builder(clazz.getSimpleName())
                .withTag("queue", address.getRequestQueue())
                .withTag("proxy", clazz.isInterface() ? "true" : "false")
                .build();
        final Impl result = new Impl(cfg);
        final Impl existing = this.monitors.putIfAbsent(cfg, result);
        return (existing != null) ? existing : result;
    }
}
