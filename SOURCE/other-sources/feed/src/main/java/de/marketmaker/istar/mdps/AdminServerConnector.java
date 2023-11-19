/*
 * MDPSJavaLHAPIWrapperBean.java
 *
 * Created on 14.09.2006 10:11:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps;

import de.marketmaker.istar.common.spring.ApplicationObjectSupport;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.ClassUtils;

import de.marketmaker.istar.common.statistics.ResetStatistics;

/**
 * Connects to the mdps AdmServer
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class AdminServerConnector extends ApplicationObjectSupport implements
        InitializingBean, DisposableBean, ApplicationListener, AdminSession.Callback {

    private CommandLineSupport commandLineSupport;

    private ShowStats showStats;

    private ResetStatistics resetStatistics;

    private AdminProtocolSupport protocolSupport;

    private volatile boolean stop = false;

    private String host;

    private int port;

    private final AtomicBoolean ready = new AtomicBoolean(false);

    private final CountDownLatch readyLatch = new CountDownLatch(1);

    private final CompletableFuture<Integer> initialMdpsPid = new CompletableFuture<>();

    private volatile int mdpsPid = -1;

    private final ScheduledExecutorService es
            = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new Thread(r, ClassUtils.getShortName(AdminServerConnector.this.getClass()));
        }
    }) {
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            logger.info("<afterExecute> admin session stopped", t);
            if (!initialMdpsPid.isDone()) {
                initialMdpsPid.completeExceptionally(t);
            }
            if (stop || !ready.get()) {
                return;
            }
            logger.info("<afterExecute> scheduled admin server reconnect in 5s");
            es.schedule(createNewSession(), 5, TimeUnit.SECONDS);
        }
    };

    private InetSocketAddress primaryAdminServer;

    public void setCommandLineSupport(CommandLineSupport commandLineSupport) {
        this.commandLineSupport = commandLineSupport;
    }

    public void setResetStatistics(ResetStatistics resetStatistics) {
        this.resetStatistics = resetStatistics;
    }

    public void setShowStats(ShowStats showStats) {
        this.showStats = showStats;
    }

    public void setAdminServerHost(String host) {
        this.host = host;
    }

    public void setAdminServerPort(int port) {
        this.port = port;
    }

    private void scheduleShutdown() {
        new Timer("Shutdown", true).schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 1000);
    }

    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if ((applicationEvent instanceof ContextRefreshedEvent)) {
            final ContextRefreshedEvent evt = (ContextRefreshedEvent) applicationEvent;
            if (evt.getApplicationContext() == getApplicationContext()) {
                this.ready.set(true);
                this.readyLatch.countDown();
            }
        }
    }

    public void destroy() throws Exception {
        this.stop = true;
        this.es.shutdownNow();
    }

    public void afterPropertiesSet() throws Exception {
        this.protocolSupport = new AdminProtocolSupport(MdpsMain.getMdpsName());

        this.primaryAdminServer = getAddress();

        final AdminSession session = createNewSession();
        this.es.schedule(session, 0, TimeUnit.SECONDS);

        // also called for side effect: raises exception if login to admin server failed
        final Integer mdpsPid = this.initialMdpsPid.get(60, TimeUnit.SECONDS);
        if (mdpsPid == null) {
            throw new IllegalStateException("failed connecting to admin server " + this.primaryAdminServer);
        }
        this.logger.info("<afterPropertiesSet> mdpsPid = " + mdpsPid);
    }

    public void setMdpsProcessId(int id) {
        if (!this.initialMdpsPid.isDone()) {
            this.initialMdpsPid.complete(id);
        }
        if (this.showStats != null) {
            this.showStats.setMdpsProcessId(id);
        }
        this.mdpsPid = id;
        // todo: do s.th. with id, tell other beans?
    }

    public void awaitReady() throws InterruptedException {
        this.readyLatch.await();
    }

    public boolean isStopped() {
        return this.stop;
    }

    private InetSocketAddress getAddress() {
        return new InetSocketAddress(this.host, this.port);
    }

    private AdminSession createNewSession() {
        return new AdminSession(this.primaryAdminServer, this.protocolSupport, this);
    }

    public void execute(AdminRequestContext context) {
        final String cmd = context.getCommand();

        if (cmd.equalsIgnoreCase("stop")) {
            this.stop = true;
            context.cancelResponse();
            scheduleShutdown();
            return;
        }

        final String[] args = context.getArgs();

        try {
            context.setResult(adminCallback(cmd, args));
        } catch (Exception e) {
            context.setFailed();
            context.setResult(e.getMessage());
            this.logger.warn("<execute> failed for " + context, e);
        }
    }

    @ManagedOperation
    public String invokeCommand(String cmd) {
        final String[] args = cmd.split("\\s+");
        return adminCallback(args[0], Arrays.copyOfRange(args, 1, args.length));
    }

    private String adminCallback(String cmd, String[] args) {
        this.logger.info("<adminCallback> '" + cmd + "' " + Arrays.toString(args));
        if (cmd.equalsIgnoreCase("help")) {
            return getHelp();
        }
        else if (cmd.equalsIgnoreCase("show_profile")) {
            return getProfile();
        }
        else if (this.showStats != null && cmd.equalsIgnoreCase("show_stats")) {
            return this.showStats.getStats();
        }
        else if (this.resetStatistics != null && cmd.equalsIgnoreCase("reset_stats")) {
            this.resetStatistics.resetStatistics();
            return "ok";
        }
        else if (this.commandLineSupport != null && this.commandLineSupport.isValidCommand(cmd, args)) {
            return this.commandLineSupport.execute(cmd, args);
        }
        return ("Unbekanntes Kommando: " + cmd + " " + Arrays.toString(args));
    }

    private String getProfile() {
        final OutputBuilder builder = new OutputBuilder().printHeader(this.mdpsPid);

        final TreeSet<String> orderedNames = new TreeSet<>(MdpsMain.PROFILE.stringPropertyNames());

        int n = 0;
        for (String name : orderedNames) {
            n = Math.max(n, name.length());
        }
        final String fmt = "%-" + n + "s = %s%n";

        builder.line(80).println();
        builder.println("PARAMETERS").line(80).println();
        for (String name : orderedNames) {
            builder.printf(fmt, name, MdpsMain.PROFILE.getProperty(name));
        }
        builder.println();
        return builder.build();
    }

    private String getHelp() {
        final StringWriter sw = new StringWriter(200);
        final PrintWriter pw = new PrintWriter(sw);
        pw.println();

        printCommand(pw, "help", "zeigt verwendbare Kommandos an");
        printCommand(pw, "stop", "beendet das Programm");
        if (this.showStats != null) {
            printCommand(pw, "show_stats", "zeigt Statistiken");
        }
        if (this.resetStatistics != null) {
            printCommand(pw, "reset_stats", "setzt Statistiken zurueck");
        }
        printCommand(pw, "show_profile", "zeigt Laufzeitvariablen");
        pw.println();

        if (this.commandLineSupport != null) {
            final TreeMap<String, String> commands = new TreeMap<>();
            this.commandLineSupport.appendCommands(commands);
            printCommands(pw, commands);
            pw.println();
        }

        return sw.toString();
    }

    private void printCommands(PrintWriter pw, Map<String, String> commands) {
        for (Map.Entry<String, String> entry : commands.entrySet()) {
            printCommand(pw, entry.getKey(), entry.getValue());
        }
    }

    private void printCommand(PrintWriter pw, final String key, final String value) {
        pw.printf("%-30s - %s%n", key.toUpperCase(), value);
    }


}