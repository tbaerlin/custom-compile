/*
 * MMIntradayPusher.java
 *
 * Created on 22.02.12 09:29
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mm;

import de.marketmaker.istar.common.lifecycle.Disposable;
import de.marketmaker.istar.common.util.IoUtils;

import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.Socket;
import java.util.Scanner;

/**
 * Talks to a pm instance on the localhost by means of a tcp socket.
 * @author tkiesgen
 */
@NotThreadSafe
public class MMIntradayPusher implements InitializingBean, Disposable {
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_EVEN);

    private static BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private int port;

    private Scanner scanner;

    private PrintWriter writer;

    public void setPort(int port) {
        this.port = port;
    }

    public void afterPropertiesSet() throws Exception {
        connect();
    }

    private void connect() {
        try {
            final Socket socket = new Socket("localhost", this.port);
            this.scanner = new Scanner(socket.getInputStream());
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            this.logger.info("<connect> connected " + socket);
        } catch (IOException e) {
            this.logger.error("<connect> failed for port " + this.port, e);
            close(); // to ensure sc and pw are null
        }
    }

    private boolean submitCommand(String cmd, boolean waitForAnswer) {
        return submitCommand(cmd, waitForAnswer, 1);
    }

    private boolean submitCommand(String cmd, boolean waitForAnswer, int retryCount) {
        if (!ensureConnected()) {
            return false;
        }
        this.writer.println(cmd);
        if (this.writer.checkError()) {
            this.logger.error("<submitCommand> failed");
            close();
            return (retryCount > 0) && submitCommand(cmd, waitForAnswer, retryCount - 1);
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<submitCommand> '" + cmd + "'");
        }
        if (waitForAnswer && !readAnswer()) {
            this.logger.warn("<submitCommand> did not get ok for '" + cmd + "'");
            return false;
        }
        return true;
    }

    private boolean ensureConnected() {
        if (this.writer == null) {
            connect();
        }
        return this.writer != null;
    }

    public void dispose() throws Exception {
        submitCommand("/", false);
        close();
    }

    private void close() {
        this.logger.info("<close> connection to pm");
        IoUtils.close(this.writer);
        this.writer = null;
        if (this.scanner != null) {
            this.scanner.close();
        }
        this.scanner = null;
    }

    public void offline() throws IOException {
        submitCommand("IntradayMode offline", true);
    }

    public void clear() throws IOException {
        submitCommand("ClearPushData", true);
    }

    private boolean readAnswer() {
        if (this.scanner.hasNextLine()) {
            final String line = this.scanner.nextLine();
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<readAnswer> '" + line + "'");
            }
            if ("ok".equals(line)) {
                return true;
            }
            if ("error".equals(line)) {
                return false;
            }
            this.logger.warn("<readAnswer> read '" + line + "'!?");
            return false;
        }
        this.logger.warn("<readAnswer> no more lines!?");
        return false;
    }

    public void pushData(MMPriceUpdate pu) throws IOException {
        final StringBuilder sb = new StringBuilder("PushSnapData ")
                .append(pu.getMmwkn()).append(";")
                .append(DTF.print(pu.getDate()));

        final BigDecimal currencyFactor = pu.isCent() ? ONE_HUNDRED : BigDecimal.ONE;

        appendPrice(sb, currencyFactor, pu.getOpen());
        appendPrice(sb, currencyFactor, pu.getHigh());
        appendPrice(sb, currencyFactor, pu.getLow());
        appendPrice(sb, currencyFactor, pu.getClose());

        if (pu.isFund()) {
            appendPrice(sb, currencyFactor, pu.getKassa());
        }
        else {
            sb.append(";");
        }

        appendPrice(sb, BigDecimal.ONE, pu.getVolume());
        appendPrice(sb, BigDecimal.ONE, pu.getContracts());

        submitCommand(sb.toString(), true);
    }

    private void appendPrice(StringBuilder stb, BigDecimal currencyFactor, BigDecimal price) {
        stb.append(";");
        if (price != null) {
            stb.append(price.divide(currencyFactor, MC).toPlainString());
        }
    }
}
