package de.marketmaker.istar.feed.ordered.tick;

import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
public class KessyFileConverter {

    private final Logger logger = LoggerFactory.getLogger(KessyFileConverter.class);

    private final String lineSeparator = System.getProperty("line.separator");

    private final String csvSeparator = ";";

    private final String mapSeparator = ":";

    private final DateTimeFormatter df = ISODateTimeFormat.date();

    private final DateTimeFormatter dtf = ISODateTimeFormat.hourMinuteSecondMillis();

    @Value("${withFieldNames:false}")
    private boolean withFieldNames;

    @Value("${inputFile}")
    private File inputFile;

    @Value("${outputFile}")
    private File outputFile;

    Map<String, List<Map<VwdFieldDescription.Field, String>>> result;

    public void setWithFieldNames(boolean withFieldNames) {
        this.withFieldNames = withFieldNames;
    }

    public Map<String, List<Map<VwdFieldDescription.Field, String>>> parse(BufferedReader reader) throws IOException {
        this.result = new HashMap<>();

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            String vwdCode = StringUtils.substringBefore(line, this.csvSeparator);
            if (!StringUtils.isAsciiPrintable(vwdCode)) {
                continue;
            }
            Map<VwdFieldDescription.Field, String> fields = parseLine(line);

            List<Map<VwdFieldDescription.Field, String>> ticks = result.computeIfAbsent(vwdCode, i -> new ArrayList<>());
            ticks.add(fields);
        }

        return this.result;
    }

    private Map<VwdFieldDescription.Field, String> parseLine(String line) {
        return Arrays.stream(StringUtils.split(line, this.csvSeparator)).filter(s -> s.contains(this.mapSeparator))
                .map(s -> StringUtils.split(s, this.mapSeparator, 2))
                .filter(a -> a.length > 1)
                .map(entry -> new String[]{entry[0], getValue(entry[0], entry[1])})
                .filter(entry -> filter(entry[1]))
                //.map(v -> { this.logger.info(Arrays.toString(v)); return v; })
                .collect(Collectors.toMap(entry -> getField(entry[0]), entry -> getValue(entry[0], entry[1])));
    }

    private VwdFieldDescription.Field getField(String field) {
        if (this.withFieldNames) {
            return VwdFieldDescription.getFieldByName(field);
        } else {
            return VwdFieldDescription.getField(Integer.valueOf(field));
        }
    }

    private String getValue(String field, String value) {
        VwdFieldDescription.Field typedField = getField(field);
        VwdFieldDescription.Type type = typedField.type();

        try {
            switch (type) {
                case DATE:
                    return this.df.print(this.df.parseLocalTime(value));
                case TIME:
                    return this.dtf.print(this.dtf.parseLocalTime(value));
                case UINT:
                    return String.valueOf(Integer.parseUnsignedInt(value));
                case PRICE:
                    return new BigDecimal(value).toString();
                default:
                    return value;
            }
        } catch (Exception e) {
            this.logger.warn("<getValue> ignored invalid value of field {}: {}", typedField.toString(), value);
        }
        return null;
    }

    public void print(Writer writer) throws IOException {
        for (String key : this.result.keySet()) {
            writer.write(String.format("# %s%n", key));

            List<Map<VwdFieldDescription.Field, String>> maps = this.result.get(key);
            for (Map<VwdFieldDescription.Field, String> map : maps) {
                String header = printHeader(key, map);
                String time = printTime(key, map);
                if (isNull(header) && isNull(time)) {
                    this.logger.warn("<print> ignored row missing symbol or time");
                    continue;
                }

                ArrayList<VwdFieldDescription.Field> fields = new ArrayList<>(map.keySet());
                fields.sort(Comparator.comparing(VwdFieldDescription.Field::id));

                writer.append(header).append(this.csvSeparator);
                writer.append(time).append(this.csvSeparator);

                writer.append(fields.stream().map(f -> print(f, map.get(f))).collect(Collectors.joining(this.csvSeparator)));
                writer.append(this.csvSeparator).append(this.lineSeparator); // dangling separator
            }
        }
    }

    private boolean filter(String value) {
        return nonNull(value) && StringUtils.isAsciiPrintable(value);
    }

    private String print(VwdFieldDescription.Field field, String value) {
        return String.format("%d:%s", field.id(), value);
    }

    private String printHeader(String vendorKey, Map<VwdFieldDescription.Field, String> fields) {
        final StringBuilder sb = new StringBuilder();

        sb.append(isTrade(vendorKey, fields) ? 'T' : '-');
        sb.append(isBid(vendorKey, fields) ? 'B' : '-');
        sb.append(isAsk(vendorKey, fields) ? 'A' : '-');
        sb.append(isClose(vendorKey, fields) ? 'X' : '-');

        return sb.toString();
    }

    private String printTime(String vendorKey, Map<VwdFieldDescription.Field, String> fields) {
        String time = fields.remove(VwdFieldDescription.ADF_Zeit);
        return nonNull(time) ? this.dtf.print(this.dtf.parseLocalTime(time)) : null;
    }

    private boolean isTrade(String vendorKey, Map<VwdFieldDescription.Field, String> fields) {
        return fields.containsKey(VwdFieldDescription.ADF_Bezahlt);
    }

    private boolean isBid(String vendorKey, Map<VwdFieldDescription.Field, String> fields) {
        return fields.containsKey(VwdFieldDescription.ADF_Geld);
    }

    private boolean isAsk(String vendorKey, Map<VwdFieldDescription.Field, String> fields) {
        return fields.containsKey(VwdFieldDescription.ADF_Brief);
    }

    private boolean isClose(String vendorKey, Map<VwdFieldDescription.Field, String> fields) {
        return fields.containsKey(VwdFieldDescription.ADF_Schluss);
    }

    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.inputFile), Charset.forName("ISO8859_15")));
             BufferedWriter writer = new BufferedWriter(new FileWriter(this.outputFile))) {
            this.parse(reader);
            this.print(writer);

            System.out.printf("Wrote file %s%n", this.outputFile);
        } catch (Exception e) {
            this.logger.error("<run> failed by {}", e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));
            ctx.registerShutdownHook();
            ctx.register(KessyFileConverter.class);
            ctx.refresh();

            KessyFileConverter exporter = ctx.getBean(KessyFileConverter.class);
            exporter.run();
        }
    }
}
