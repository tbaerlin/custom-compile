/*
 * ProtobufDataCli.java
 *
 * Created on 03.11.11 13:46
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.protobuf;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.util.IoUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * @author oflege
 */
public class ProtobufDataCli {

    private static class Options {
        Set<Long> ids = new LinkedHashSet<>();

        boolean showIds = false;

        boolean showWhat = false;

        boolean all = false;

        boolean withKeys = true;

        int head = Integer.MAX_VALUE;

        Set<String> fields;
    }

    private static void usage() {
        System.err.println("Usage: ProtobufDataCli [options] [ids] [files]");
        System.err.println("[options]");
        System.err.println(" -ids           : print ids contained in each file to stdout and exit");
        System.err.println(" -what          : print names of fields contained in each file to stdout and exit");
        System.err.println(" -no-keys       : data file contains no keys (implicit for .gz files)");
        System.err.println(" -all           : ignore [ids], print all records from all files");
        System.err.println(" -head n        : ignore [ids], print first n records from all files");
        System.err.println(" -fields n[,m]* : specify field names used for printing");
        System.err.println("[ids] numeric iids or qids separated by blanks");
        System.err.println("      to print messages for those ids from each file to stdout");
        System.err.println("[files] names of input files");

        System.exit(1);
    }

    public static void main(String[] args) {
        Options options = new Options();
        int n = 0;
        if (args.length == 0) {
            usage();
        }
        while (n < args.length && args[n].startsWith("-")) {
            if ("-ids".equals(args[n])) {
                options.showIds = true;
            } else if ("-what".equals(args[n])) {
                options.showWhat = true;
            } else if ("-all".equals(args[n])) {
                options.all = true;
            } else if ("-head".equals(args[n])) {
                options.head = Integer.parseInt(args[++n]);
            } else if ("-fields".equals(args[n])) {
                options.fields = new HashSet<>(Arrays.asList(args[++n].split(",")));
            } else {
                usage();
            }
            n++;
        }
        while (n < args.length && args[n].matches("\\d+")) {
            options.ids.add(Long.parseLong(args[n++]));
        }
        int numFiles = args.length - n;
        while (n < args.length) {
            File f = new File(args[n++]);
            if (!f.canRead()) {
                System.err.println("No such file: " + f.getAbsolutePath());
                continue;
            }
            if (numFiles > 1) {
                System.out.println("#---------------------------");
                System.out.println("#" + f.getAbsolutePath());
                System.out.println("#---------------------------");
            }
            try {
                ProtobufDataCli cli = new ProtobufDataCli(f, options);
                cli.execute();
            } catch (Exception e) {
                System.err.println("error while reading " + f.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }

    private File file;

    private Options options;

    public ProtobufDataCli(File file, Options options) {
        this.file = file;
        this.options = options;
    }

    private static Descriptors.Descriptor getDescriptorForClass(String className) throws Exception {
        final Class<?> clazz = Class.forName(className);
        Method getDecriptor = clazz.getDeclaredMethod("getDescriptor");
        return (Descriptors.Descriptor) getDecriptor.invoke(null);
    }

    private static Method getBuilderMethod(String className) throws Exception {
        final Class<?> clazz = Class.forName(className);
        return clazz.getDeclaredMethod("newBuilder");
    }

    private static String getClassName(File file) throws IOException {
        DataInputStream dis = null;
        try {
            dis = createStream(file);
            return dis.readUTF();
        } finally {
            IoUtils.close(dis);
        }
    }

    private static DataInputStream createStream(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        return f.getName().endsWith(".gz")
                ? new DataInputStream(new GZIPInputStream(fis))
                : new DataInputStream(fis);
    }

    private void execute() throws Exception {
        if (this.file.getName().endsWith(".gz") || !this.options.withKeys) {
            WithoutKeysReader reader = new WithoutKeysReader(file, options);
            reader.execute();
        } else {
            WithKeysReader reader = new WithKeysReader(file, options);
            reader.afterPropertiesSet();
            reader.execute();
            reader.destroy();
        }
    }

    private static void printWhat(Descriptors.Descriptor descriptor, String className,
            Set<Descriptors.Descriptor> printed) {
        if (className != null) {
            System.out.println("Class: " + className);
        }
        System.out.println("Name : " + descriptor.getFullName());
        System.out.printf("%-20s %3s %-10s%n", "Field", "Idx", "Type");
        System.out.println("-----------------------------------");
        for (Descriptors.FieldDescriptor fd : descriptor.getFields()) {
            String type = fd.getType().toString();
            if (fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
                type += (":" + fd.getMessageType().getFullName());
            }
            System.out.printf("%-20s %3d %-10s%n", fd.getName(), fd.getIndex(), type);
        }
        System.out.println("===================================");
        for (Descriptors.FieldDescriptor fd : descriptor.getFields()) {
            if (fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
                if (!printed.contains(fd.getMessageType())) {
                    printed.add(fd.getMessageType());
                    printWhat(fd.getMessageType(), null, printed);
                }
            }
        }
    }

    static class MessagePrinter {
        private final StringBuilder sb = new StringBuilder(100);

        private Method newBuilder;

        private Set<String> printFields;

        MessagePrinter(String className, Options options)
                throws Exception {
            this.printFields = options.fields;
            this.newBuilder = getBuilderMethod(className);
        }

        private void printMessage(Message msg) throws Exception {
            if (msg != null) {
                printFields(msg);
            }
        }

        private void printMessage(byte[] bytes) throws Exception {
            GeneratedMessage.Builder builder = (GeneratedMessage.Builder) newBuilder.invoke(null);
            Message msg = builder.mergeFrom(bytes).buildPartial();
            printFields(msg);
        }

        private void printFields(Message msg) {
            sb.setLength(0);
            append(msg, false);
            System.out.println(sb.toString());
        }

        private void append(Message msg, boolean appendComma) {
            boolean x = appendComma;
            for (Descriptors.FieldDescriptor fd : msg.getDescriptorForType().getFields()) {
                if (this.printFields != null && !this.printFields.contains(fd.getName())) {
                    continue;
                }
                if (fd.isRepeated()) {
                    if (msg.getRepeatedFieldCount(fd) > 0) {
                        if (x && sb.length() > 0) sb.append(", ");
                        sb.append(fd.getName()).append("=");
                        if (ProtobufDataReader.isRepeatedLocalizedString(fd)) {
                            //noinspection unchecked
                            sb.append(ProtobufDataReader.toLocalizedString((List<ProviderProtos.LocalizedString>) msg.getField(fd)));
                        } else {
                            sb.append("[");
                            for (int i = 0; i < msg.getRepeatedFieldCount(fd); i++) {
                                if (i > 0) sb.append(", ");
                                sb.append("(#").append(i + 1).append(")");
                                if (fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
                                    sb.append("{");
                                    append((Message) msg.getRepeatedField(fd, i), false);
                                    sb.append("}");
                                } else {
                                    sb.append(msg.getRepeatedField(fd, i));
                                }
                            }
                            sb.append("]");
                        }
                    }
                } else if (msg.hasField(fd)) {
                    if (x && sb.length() > 0) sb.append(", ");
                    sb.append(fd.getName()).append("=");
                    if (fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
                        sb.append("{");
                        append((Message) msg.getField(fd), false);
                        sb.append("}");
                    } else {
                        if (fd.getType() == Descriptors.FieldDescriptor.Type.ENUM) {
                            Descriptors.EnumValueDescriptor field
                                    = (Descriptors.EnumValueDescriptor) msg.getField(fd);
                            sb.append(field.getFullName());
                        } else {
                            sb.append(msg.getField(fd));
                        }
                    }
                }
                x = true;
            }
        }
    }


    static class WithKeysReader extends ProtobufDataReader {
        private Options options;

        private String className;

        private MessagePrinter messagePrinter;

        private Method newBuilder;


        WithKeysReader(File f, Options options) throws Exception {
            setFile(f);
            this.className = getClassName(f);
            setDescriptor(getDescriptorForClass(className));
            this.options = options;
            this.messagePrinter = new MessagePrinter(className, options);
            this.newBuilder = getBuilderMethod(className);
        }

        private void execute() throws Exception {
            if (options.showIds) {
                for (long key : keysAndOffsets[0]) {
                    System.out.println(key);
                }
                return;
            }
            if (options.showWhat) {
                printWhat(this.descriptor, this.className, new HashSet<Descriptors.Descriptor>());
                return;
            }
            if (options.all || options.head != Integer.MAX_VALUE) {
                long[] ids = getIdsSortedByOffsetInFile();
                for (int i = 0, n = ids.length; i < n && i < options.head; i++) {
                    this.messagePrinter.printMessage(deserialize(ids[i]));
                }
            } else if (!options.ids.isEmpty()) {
                for (Long id : options.ids) {
                    Message m = deserialize(id);
                    if (m == null) {
                        System.err.println(id + ".iid not found");
                    } else {
                        this.messagePrinter.printMessage(m);
                    }
                }
            }
        }

        Message deserialize(long id) throws Exception {
            GeneratedMessage.Builder builder = (GeneratedMessage.Builder) newBuilder.invoke(null);
            if (build(id, builder)) {
                return builder.build();
            }
            return null;
        }

        private long[] getIdsSortedByOffsetInFile() {
            final long[] keys = Arrays.copyOf(keysAndOffsets[0], keysAndOffsets[0].length);
            final long[] offsets = Arrays.copyOf(keysAndOffsets[1], keysAndOffsets[1].length);
            ArraysUtil.sort(offsets, keys);
            return keys;            
        }
    }

    static class WithoutKeysReader {
        private String className;

        private Descriptors.Descriptor descriptor;

        private MessagePrinter messagePrinter;

        private File f;

        private Options options;

        WithoutKeysReader(File f, Options options) throws Exception {
            this.f = f;
            this.options = options;
            this.className = getClassName(f);
            this.descriptor = getDescriptorForClass(className);
            this.messagePrinter = new MessagePrinter(className, options);
        }

        private void execute() throws Exception {
            if (options.showIds) {
                System.err.println("cannot show ids for file without keys");
                return;
            }
            if (!options.ids.isEmpty()) {
                System.err.println("cannot filter by ids for file without keys");
                return;
            }
            if (options.showWhat) {
                printWhat(descriptor, className, new HashSet<Descriptors.Descriptor>());
                return;
            }
            if (options.all || options.head != Integer.MAX_VALUE) {
                DataInputStream dis = createStream(f);
                /* ignore */
                dis.readUTF();
                CodedInputStream cis = CodedInputStream.newInstance(dis);
                cis.setSizeLimit(Integer.MAX_VALUE);
                int len;
                int num = 0;
                while ((len = cis.readInt32()) > 0 && num++ < options.head) {
                    byte[] bytes = cis.readRawBytes(len);
                    this.messagePrinter.printMessage(bytes);
                }
                dis.close();
            }
        }
    }
}