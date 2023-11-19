/*
 * IqsCli.java
 *
 * Created on 23.10.13 08:54
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.HexDump;
import de.marketmaker.istar.common.util.IoUtils;

/**
 * @author oflege
 */
public class IqsCli implements Runnable {

    private static final Charset UTF_8 = Charset.forName("utf-8");

    private File outFile;

    private Socket s;

    Thread reader;

    int requestId = 0xff;

    StreamingMessageParser out;

    private Console console = System.console();

    private final List<String> commands = new ArrayList<>();

    private int commandId;

    PrintWriter pw;

    private File historyFile = new File(System.getProperty("user.home"), ".iqscli_history");

    public IqsCli(String[] args) throws Exception {
        int n = 0;
        while (n < args.length && args[n].startsWith("-")) {
            if ("-o".equals(args[n])) {
                outFile = new File(args[++n]);
            }
            else if ("-i".equals(args[n])) {
                initCommands(new File(args[++n]));
                this.commandId = 0;
                this.console = null;
            }
            else if ("-h".equals(args[n])) {
                this.historyFile = new File(args[++n]);
            }
            ++n;
        }
        if (this.commands.isEmpty()) {
            if (console == null) {
                System.err.println("No console.");
                System.exit(1);
            }
            initCommands(this.historyFile);
        }
    }

    private void initCommands(File f) throws IOException {
        this.commands.clear();
        if (f.canRead()) {
            this.commands.addAll(Files.readAllLines(f.toPath(), UTF_8));
        }
        else {
            this.commands.add("quit");
        }
        this.commandId = commands.size();
    }

    private void runLoop() throws IOException {
        try (PrintWriter tmp = (this.outFile != null)
                ? new PrintWriter(new FileOutputStream(outFile), true)
                : console.writer()) {
            pw = tmp;
            out = new StreamingMessageParser(new IqsMessageDumper(pw));
            String line;
            while (!"quit".startsWith(line = readCommand())) {
                final String[] commands = line.split(";");
                for (String cmd : commands) {
                    final String[] tokens = cmd.trim().split("\\s+");
                    try {
                        if (!handle(tokens)) {
                            usage();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            pw.flush();
        } finally {
            close();
            saveHistory();
        }
    }

    private void saveHistory() throws IOException {
        if (this.console != null) {
            Files.write(this.historyFile.toPath(), commands, UTF_8);
        }
    }

    private String readCommand() {
        if (this.console == null) {
            if (this.commandId < this.commands.size()) {
                return this.commands.get(this.commandId++);
            }
            return "quit";
        }
        String cmd;
        while (true) {
            cmd = console.readLine("(" + commandId + ")> ");
            if (cmd.matches("\\d+")) {
                int i = Integer.parseInt(cmd);
                if (i >= 0 && i < commands.size()) {
                    return commands.get(i);
                }
            }
            else if (cmd.matches("ch(\\s+\\d+){0,2}")) {
                clearHistory(cmd);
            }
            else if ("sh".equals(cmd)) {
                showHistory();
            }
            else if ("quit".startsWith(cmd)) {
                return cmd;
            }
            else {
                break;
            }
        }
        this.commands.add(cmd);
        this.commandId++;
        return cmd;
    }

    private boolean handle(String[] tokens) throws Exception {
        if (tokens.length == 0 || !StringUtils.hasText(tokens[0])) {
            return false;
        }
        if ("connect".startsWith(tokens[0])) {
            handleConnect(tokens);
        }
        else if ("login".startsWith(tokens[0])) {
            handleLogin(tokens);
        }
        else if ("recap".startsWith(tokens[0])) {
            handleRecap(tokens);
        }
        else if ("subscribe".startsWith(tokens[0])) {
            handleSubscribe(tokens);
        }
        else if ("unsubsribe".startsWith(tokens[0])) {
            handleUnsubsribe(tokens);
        }
        else if ("LookupSelectors".startsWith(tokens[0])) {
            handleLookup(tokens);
        }
        else if ("heartbeat".startsWith(tokens[0])) {
            handleHeartbeat();
        }
        else if ("sleep".startsWith(tokens[0]) && this.console == null) {
            Thread.sleep(Integer.parseInt(tokens[1]) * 1000);
        }
        else {
            return false;
        }
        return true;
    }

    private void clearHistory(String cmd) {
        final String[] s = cmd.split("\\s+");
        int fromIndex = Math.max(1, s.length > 1 ? Integer.parseInt(s[1]) : 1);
        int toIndex = Math.min(this.commands.size(), s.length > 2 ? Integer.parseInt(s[2]) : this.commands.size());
        this.commands.subList(fromIndex, toIndex).clear();
        this.commandId = this.commands.size();
    }

    private void showHistory() {
        for (int i = 1; i < this.commands.size(); i++) {
            System.out.printf("%4d %s%n", i, this.commands.get(i));
        }
    }

    private void handleConnect(String[] tokens) throws IOException {
        close();
        s = new Socket(tokens[1], Integer.parseInt(tokens[2]));

        this.reader = new Thread(this, "reader");
        this.reader.start();

        System.err.println("Connected");
    }

    private void handleLogin(String[] tokens) throws IOException {
        write(new IqsMessageBuilder(64).prepare(Constants.MSG_LOGON_REQUEST)
                .header(Constants.FID_VERSION, "1.0".getBytes())
                .header(Constants.FID_USERID, tokens[1])
                .header(Constants.FID_PASSWORD, tokens[2])
                .build());
    }

    private void handleRecap(String[] tokens) throws IOException {
        IqsMessageBuilder builder = new IqsMessageBuilder(64).prepare(Constants.MSG_DATA_REQUEST)
                .header(Constants.FID_REQUEST_ID, getRequestId())
                .header(Constants.FID_SERVICEID, Constants.SERVICE_ID_PRICE)
                .header(Constants.FID_DATA_REQUEST_TYPE, Constants.DATA_REQUEST_TYPE_RECAP)
                .header(Constants.FID_OBJECTNAME, tokens[1].toUpperCase());
        if (tokens.length > 2) {
            builder.header(Constants.FID_USERDATA, tokens[2]);
        }
        write(builder.build());
    }

    private void handleLookup(String[] tokens) throws IOException {
        write(new IqsMessageBuilder(128).prepare(Constants.MSG_DATA_REQUEST)
                .header(Constants.FID_REQUEST_ID, getRequestId())
                .header(Constants.FID_SERVICEID, Constants.SERVICE_ID_EXCHANGE_SUBSCRIBE)
                .header(Constants.FID_DATA_REQUEST_TYPE, Constants.DATA_REQUEST_TYPE_EXECUTE)
                .header(Constants.FID_EXECUTE_COMMAND, IqsMessageProcessor.LOOKUP_SELECTORS)
                .header(Constants.FID_EXCHANGE, tokens[1])
                .header(Constants.FID_SECTYPE_LIST, tokens[2])
                .build());
    }

    private void handleSubscribe(String[] tokens) throws IOException {
        IqsMessageBuilder builder = new IqsMessageBuilder(64).prepare(Constants.MSG_DATA_REQUEST)
                .header(Constants.FID_REQUEST_ID, getRequestId())
                .header(Constants.FID_SERVICEID, Constants.SERVICE_ID_PRICE)
                .header(Constants.FID_DATA_REQUEST_TYPE, Constants.DATA_REQUEST_TYPE_RECAP_AND_UPDATES)
                .header(Constants.FID_OBJECTNAME, tokens[1].toUpperCase());
        if (tokens.length > 2) {
            builder.header(Constants.FID_USERDATA, tokens[2]);
        }
        write(builder.build());
    }

    private void handleUnsubsribe(String[] tokens) throws IOException {
        write(new IqsMessageBuilder(64).prepare(Constants.MSG_RELEASE_REQUEST)
                .header(Constants.FID_REQUEST_ID, getRequestId())
                .header(Constants.FID_SERVICEID, Constants.SERVICE_ID_PRICE)
                .header(Constants.FID_OBJECTNAME, tokens[1].toUpperCase())
                .build());
    }

    private void handleHeartbeat() throws IOException {
        write(new IqsMessageBuilder(64).prepare(Constants.MSG_HEARTBEAT_REQUEST)
                .header(Constants.FID_REQUEST_ID, getRequestId())
                .header(Constants.FID_HB_REQUEST_TIMESTAMP, IqsMessageProcessor.DTF.print(new DateTime()).getBytes())
                .build());
    }

    private byte[] getRequestId() {
        return ("0x" + Integer.toHexString(++this.requestId)).getBytes();
    }

    private void write(ByteBuffer bb) throws IOException {
        final OutputStream os = s.getOutputStream();
        os.write(bb.array(), bb.position(), bb.remaining());
        os.flush();
        synchronized (this) {
            pw.println("--> OUT (" + bb.remaining() + " bytes) -- " + timestamp());
            out.parse(bb);
        }
    }

    private void close() {
        if (s != null) {
            IoUtils.close(s);
            s = null;
        }
        if (reader != null) {
            reader.interrupt();
            reader = null;
        }
    }

    private void usage() {
        System.err.println("Commands:");
        System.err.println(" connect <ip> <port>");
        System.err.println(" login <user> <password>");
    }

    @Override
    public void run() {
        final byte[] data = new byte[1 << 16];

        try (InputStream is = this.s.getInputStream()) {
            int i;
            int pos = 0;
            while ((i = is.read(data, pos, data.length - pos)) >= 0) {
                pos += i;

                int n = 0;
                int end;
                while (n < pos && (end = IqsMessageParser.findEnd(data, n, pos)) > n) {
                    if (data[n] != Constants.STX) {
                        throw new IOException(HexDump.toHex(Arrays.copyOf(data, pos)));
                    }
                    synchronized (this) {
                        byte[] msg = Arrays.copyOfRange(data, n, end + 1);
                        pw.println("<-- IN (" + msg.length + " bytes) -- " + timestamp());
                        out.parse(msg);
                    }
                    n = end + 1;
                }
                if (n > 0) {
                    System.arraycopy(data, n, data, 0, pos - n);
                    pos -= n;
                }
            }
        } catch (IOException e) {
            System.err.println("reader failed: " + e.getMessage());
        }
    }

    private String timestamp() {
        return ISODateTimeFormat.hourMinuteSecondMillis().print(System.currentTimeMillis());
    }

    public static void main(String[] args) throws Exception {
        new IqsCli(args).runLoop();
    }
}
