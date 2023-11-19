/*
 * ProtobufGroupByDataWriter.java
 *
 * Created on 12.12.11 16:48
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.protobuf;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A specialized {@link ProtobufDataWriter} that assembles objects from adjacent rows with the same key
 * in a single object. <p>Configuration options:
 * <dl>
 * <dt><tt>protobufGroupByClassName</tt></dt>
 * <dd>Qualified name of the protobuf message class that corresponds to each group of rows with
 * the same key column value. Whenever the
 * key column's value changes, a new builder will be created and populated with those values
 * from the current row are described by <code>myColumns</code></dd>
 * <dt><tt>itemsField</tt></dt>
 * <dd>Name of the repeated field in the protobuf massage for which each row will trigger
 * the addition of a new object (default is <tt>items</tt>). Those objects will be built by
 * the superclass.</dd>
 * </dl>
 * The <tt>itemsField</tt> can be omitted if the target message contains a repeated message field
 * of the same type as defined by the row message builder (i.e., <tt>protobufClassName</tt>). If
 * multiple such fields are present, the result set must contain a column called {@value #FIELD_ID},
 * which is supposed to contain the uppercase name of the field to be set for that particular row.
 *
 * @author oflege
 */
public class ProtobufGroupByDataWriter extends ProtobufDataWriter {

    private static final String FIELD_ID = "FIELD_ID";

    private long lastId = -1;

    private Method createMyBuilderMethod;

    private GeneratedMessage.Builder myBuilder;

    private Descriptors.FieldDescriptor itemsFieldDescriptor;

    private final Map<String, Descriptors.FieldDescriptor> descriptorMap = new HashMap<>();

    private int fieldIdColumnIndex;

    private List<ColumnInfo> myColumns = null;

    public ProtobufGroupByDataWriter() throws Exception {
    }

    @Override
    protected String getMainMessageClassName() {
        return props.getProperty("protobufGroupByClassName");
    }

    private GeneratedMessage.Builder createMyBuilder() throws Exception {
        if (this.createMyBuilderMethod == null) {
            final String className = props.getProperty("protobufGroupByClassName");
            final Class<?> clazz = Class.forName(className);
            this.createMyBuilderMethod = clazz.getDeclaredMethod("newBuilder");
        }
        return (GeneratedMessage.Builder) this.createMyBuilderMethod.invoke(null);
    }

    @Override
    protected void beforeFirstRow() throws Exception {
        super.beforeFirstRow();

        Descriptors.Descriptor rowType = createBuilder().getDescriptorForType();

        final Descriptors.Descriptor descriptor = createMyBuilder().getDescriptorForType();
        this.myColumns = initColumns(descriptor);

        this.itemsFieldDescriptor = descriptor.findFieldByName(props.getProperty("itemsField", "items"));
        if (this.itemsFieldDescriptor == null) {
            for (Descriptors.FieldDescriptor fd : descriptor.getFields()) {
                if (fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE
                        && fd.getMessageType() == rowType) {
                    this.descriptorMap.put(fd.getName().toUpperCase(), fd);
                }
            }
            if (this.descriptorMap.isEmpty()) {
                throw new Exception("no field of type " + rowType.getName()
                        + " in group type " + descriptor.getName());
            }
            if (getColumns().containsKey(FIELD_ID)) {
                this.fieldIdColumnIndex = findColumnIndex(FIELD_ID);
            }
            else if (descriptorMap.size() == 1) {
                this.itemsFieldDescriptor = this.descriptorMap.values().iterator().next();
            }
            else {
                throw new Exception("undefined itemsField and no column " + FIELD_ID
                        + " to select field from " + this.descriptorMap.keySet());
            }

        } else if (this.itemsFieldDescriptor.getMessageType() != rowType) {
            throw new Exception("itemsField type " + this.itemsFieldDescriptor.getMessageType().getName()
                + " != row type " + rowType.getName());
        }
    }

    @Override
    protected void handleRowObject(long id, GeneratedMessage.Builder builder) throws Exception {
        if (id != this.lastId) {
            writeCurrentObject();
            this.myBuilder = createMyBuilder();
            handleColumns(this.myBuilder, id, this.myColumns);
            this.lastId = id;
        }
        if (builder.isInitialized()) {
            final Descriptors.FieldDescriptor fd = getTargetField();
            if (fd != null) {
                this.myBuilder.addRepeatedField(fd, builder.build());
            }
        }
    }

    private Descriptors.FieldDescriptor getTargetField() throws Exception {
        if (this.itemsFieldDescriptor != null) {
            return this.itemsFieldDescriptor;
        }
        String fieldId = getString(this.fieldIdColumnIndex);
        Descriptors.FieldDescriptor result = this.descriptorMap.get(fieldId);
        if (result == null) {
            throw new Exception("field id '" + fieldId + "' not in " + this.descriptorMap.keySet());
        }
        return result;
    }

    @Override
    protected void afterLastRow() throws Exception {
        writeCurrentObject();
        super.afterLastRow();
    }

    private void writeCurrentObject() throws IOException {
        if (this.myBuilder != null && this.myBuilder.isInitialized()) {
            writeObject(this.lastId, this.myBuilder);
        }
    }

    @Override
    protected Map<String, Map<String, String>> getStringCaches() {
        final Map<String, Map<String, String>> result = super.getStringCaches();
        result.putAll(getStringCaches(this.myColumns));
        return result;
    }
}
