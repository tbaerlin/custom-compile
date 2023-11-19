package de.marketmaker.istar.merger.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;

import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.*;
import static de.marketmaker.istar.ratios.RatioFieldDescription.underlyingIid;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DataStructureGenerator {

    private static final InstrumentTypeEnum[] TYPES
            = new InstrumentTypeEnum[]{BND, CER, FND, STK, WNT, OPT, FUT, IND, CUR, GNS, MER, ZNS};

    private STGroup stringTemplateGroup;

    public void setStringTemplateGroup(STGroup stringTemplateGroup) {
        this.stringTemplateGroup = stringTemplateGroup;
    }

    public static void main(String[] args) throws IOException {
        final DataStructureGenerator g = new DataStructureGenerator();

        g.setStringTemplateGroup(new STGroupDir(LocalConfigProvider.getIstarSrcDir()
                + "/merger/src/main/java/de/marketmaker/istar/merger/util", "utf8", '$', '$'));

        for (final InstrumentTypeEnum type : TYPES) {
            g.generateInstrument(type, createWriter("InstrumentRatios", type));
            g.generateQuote(type, createWriter("QuoteRatios", type));
            g.generateGetterSetter(type, createWriter("RatiosPropertySupport", type));
        }
    }

    private static PrintWriter createWriter(String prefix, InstrumentTypeEnum type) throws IOException {
        return new PrintWriter(
                new FileWriter(LocalConfigProvider.getIstarSrcDir()
                        + "/ratios/src/main/java/de/marketmaker/istar/ratios/frontend/"
                        + prefix + type.name() + ".java")
        );
    }

    private void generateGetterSetter(InstrumentTypeEnum type,
            PrintWriter writer) throws IOException {
        final ST template = this.stringTemplateGroup.getInstanceOf("ratiospropertysupport");

        final BitSet bs = new BitSet();
        bs.or(RatioFieldDescription.getInstrumentFieldids());
        bs.or(RatioFieldDescription.getQuoteRatiosFieldids());
        bs.or(RatioFieldDescription.getQuoteStaticFieldids());

        final List<FieldValues> instrumentfields = new ArrayList<>();
        final List<FieldValues> quotefields = new ArrayList<>();

        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            final RatioFieldDescription.Field field = RatioFieldDescription.getFieldById(i);
            if (!field.isApplicableFor(type)) {
                continue;
            }

            final String datatype = getType(field);
            final String typeMethodname = datatype.substring(0, 1).toUpperCase() + datatype.substring(1);

            final FieldValues fieldValues = new FieldValues(field, typeMethodname);

            if (field.isInstrumentField()) {
                instrumentfields.add(fieldValues);
            }
            else {
                quotefields.add(fieldValues);
            }
        }

        template.add("instrumentfields", instrumentfields);
        template.add("quotefields", quotefields);
        template.add("type", type.name());

        write(writer, template);
    }

    private void write(PrintWriter writer, ST template) throws IOException {
        template.write(new AutoIndentWriter(writer, "\n"));
        writer.close();
    }

    private void generateInstrument(InstrumentTypeEnum type,
            PrintWriter writer) throws IOException {
        final ST template = this.stringTemplateGroup.getInstanceOf("instrumentratios");

        final BitSet bs = RatioFieldDescription.getInstrumentFieldids();

        final List<FieldValues> fields = new ArrayList<>();
        final List<FieldValues> enumfields = new ArrayList<>();
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            final RatioFieldDescription.Field field = RatioFieldDescription.getFieldById(i);
            if (!field.isApplicableFor(type)) {
                continue;
            }

            final FieldValues fieldValues = new FieldValues(field, null);

            fields.add(fieldValues);

            if (field.isEnum() && field.type() == RatioFieldDescription.Type.STRING) {
                enumfields.add(fieldValues);
            }
        }
        template.add("fields", fields);
        template.add("enumfields", enumfields);
        template.add("type", type.name());
        template.add("derivative", underlyingIid.isApplicableFor(type));

        write(writer, template);
    }

    private void generateQuote(InstrumentTypeEnum type, PrintWriter writer) throws IOException {
        final ST template = this.stringTemplateGroup.getInstanceOf("quoteratios");

        final BitSet bs = new BitSet();
        bs.or(RatioFieldDescription.getQuoteRatiosFieldids());
        bs.or(RatioFieldDescription.getQuoteStaticFieldids());

        final List<FieldValues> fields = new ArrayList<>();
        final List<FieldValues> enumfields = new ArrayList<>();
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            final RatioFieldDescription.Field field = RatioFieldDescription.getFieldById(i);
            if (!field.isApplicableFor(type)) {
                continue;
            }
            final FieldValues fieldValues = new FieldValues(field, null);

            fields.add(fieldValues);

            if (field.isEnum() && field.type() == RatioFieldDescription.Type.STRING) {
                enumfields.add(fieldValues);
            }
        }
        template.add("fields", fields);
        template.add("enumfields", enumfields);
        template.add("type", type.name());

        write(writer, template);
    }

    private static String getNullValue(RatioFieldDescription.Field field) {
        switch (field.type()) {
            case BOOLEAN:
                return "false";
            case TIME:
            case DATE:
                return "Integer.MIN_VALUE";
            case ENUMSET:
                return "RatioEnumSet.unmodifiableBitSet()";
            case DECIMAL:
            case NUMBER:
            case TIMESTAMP:
                return "Long.MIN_VALUE";
            case STRING:
                return "null";
        }
        throw new IllegalArgumentException("failed for " + field.name());
    }

    private static String getType(RatioFieldDescription.Field field) {
        switch (field.type()) {
            case BOOLEAN:
                return "boolean";
            case TIME:
            case DATE:
                return "int";
            case ENUMSET:
                return "BitSet";
            case DECIMAL:
            case NUMBER:
            case TIMESTAMP:
                return "long";
            case STRING:
                return "String";
        }
        throw new IllegalArgumentException("failed for " + field.name());
    }

    private static String getMethodName(RatioFieldDescription.Field f) {
        final String s = f.name();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public final static class FieldValues {
        private final RatioFieldDescription.Field field;

        private final String typeMethodName;

        public FieldValues(RatioFieldDescription.Field field, String typeMethodName) {
            this.field = field;
            this.typeMethodName = typeMethodName;
        }

        public String getType() {
            return DataStructureGenerator.getType(this.field);
        }

        public String getName() {
            return this.field.name();
        }

        public String getDefaultValue() {
            return getNullValue(this.field);
        }

        public String getMethodName() {
            return DataStructureGenerator.getMethodName(this.field);
        }

        public String getTypeMethodName() {
            return this.typeMethodName;
        }

        public int getFieldid() {
            return this.field.id();
        }

        public boolean isLocalized() {
            return this.field.isLocalized();
        }

        public boolean isNumeric() {
            return this.field.isNumeric();
        }

        public boolean isDeprecated() {
            return this.field.isDeprecated();
        }
    }
}
