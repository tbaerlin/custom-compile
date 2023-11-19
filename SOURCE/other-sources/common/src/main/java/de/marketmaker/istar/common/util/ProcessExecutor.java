/*
 * ProcessExecutor.java
 *
 * Created on 21.08.2006 16:29:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Arrays;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ProcessExecutor implements DisposableBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int DEFAULT_MAX_OUTPUT_SIZE = 1024;

    private static final int BUFFER_SIZE = 4096;

    private static final long TIMEOUT_SECONDS = 5;

    private static final AtomicInteger INSTANCE_COUNT = new AtomicInteger();

    private final ExecutorService es = Executors.newSingleThreadExecutor(r ->
            new Thread(r, "ProcessExecutor-" + INSTANCE_COUNT.incrementAndGet()));

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client owns result")
    public static class Result {
        final int returnCode;
        final byte[] output;

        public Result(int returnCode, byte[] output) {
            this.returnCode = returnCode;
            this.output = output;
        }

        public byte[] getOutput() {
            return output;
        }

        public int getReturnCode() {
            return returnCode;
        }
    }

    public void destroy() throws Exception {
        this.es.shutdown();
        this.logger.info("<destroy> shutdown");
    }

    private Result doExecute(final int maxOutputSize, String... args) throws Exception {
        final TimeTaker tt = new TimeTaker();

        final ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE);
        final byte[] tmp = new byte[BUFFER_SIZE];

        BufferedInputStream br = null;
        try {
            final Process process = pb.start();
            final InputStream is = process.getInputStream();
            br = new BufferedInputStream(is);
            int num;
            boolean overflow = false;
            while ((num = br.read(tmp)) != -1) {
                if (!overflow) {
                    baos.write(tmp, 0, num);
                    if (baos.size() > maxOutputSize) {
                        overflow = true;
                    }
                }
            }
            final int result = process.waitFor();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<doExecute> for " + Arrays.toString(args) + " took " + tt);
            }

            if (overflow) {
                this.logger.warn("<doExecute> failed: " + Arrays.toString(args)
                    + ", maxOutputSize " + maxOutputSize + " exceeded");
                return new Result(-1, null);
            }
            return new Result(result, baos.toByteArray());

        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    public final Result execute(final String... args) {
        return execute(DEFAULT_MAX_OUTPUT_SIZE, args);
    }

    public final Result execute(final int maxOutputSize, final String... args) {
        final Future<Result> result = this.es.submit(() -> doExecute(maxOutputSize, args));
        try {
            return result.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            this.logger.error("<execute> interrupted?!");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            this.logger.error("<execute> failed for " + Arrays.toString(args), e);
        } catch (TimeoutException e) {
            this.logger.error("<execute> timeout for " + Arrays.toString(args));
        }
        return null;
    }

}
