/*
 * WMDataProviderImpl.java
 *
 * Created on 02.11.11 15:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.data.WMData;
import de.marketmaker.istar.domain.data.WMData.Field;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.merger.provider.protobuf.ProtobufDataReader;
import de.marketmaker.istar.merger.provider.protobuf.ProviderProtos;
import de.marketmaker.istar.merger.provider.protobuf.WmDataProtos;

/**
 * @author oflege
 */
public class WMDataProviderImpl extends ProtobufDataReader implements WMDataProvider {

    private static final DateTime START_DATE = new LocalDate(1900, 1, 1).toDateTimeAtStartOfDay();

    private static final DateTime END_DATE = new LocalDate(2099, 12, 31).toDateTimeAtStartOfDay();

    private static final long MAX_UNSIGNED_INT = 0xFFFFFFFFL;

    private static final List<String> UNRESTRICTED_FIELDS =
        Arrays.asList("ISIN", "WKN", "GD801A", "GD625", "GD260", "GD460A");

    private File isinMappingFile;

    @SuppressWarnings("unchecked")
    private volatile Object2IntMap<String> isin2iidMapping = Object2IntMaps.EMPTY_MAP;

    public WMDataProviderImpl() {
        super(WmDataProtos.WmMasterData.getDescriptor());
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setIsinMappingFile(File isinMappingFile) {
        this.isinMappingFile = isinMappingFile;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        if (this.activeMonitor != null) {
            FileResource fr = new FileResource(this.isinMappingFile);
            fr.addPropertyChangeListener(evt -> readISINMapping());
            this.activeMonitor.addResource(fr);
        }
        readISINMapping();
    }

    private void readISINMapping() {
        if (this.isinMappingFile == null) {
            this.logger.info("<readISINMapping> no isin mapping file set => skip");
            return;
        }

        final TimeTaker tt = new TimeTaker();
        this.logger.info("<readISINMapping> start ...");
        try {
            this.isin2iidMapping = new WMISINMappingReader().read(this.isinMappingFile);
            this.logger.info("<readISINMapping> read from " + this.isinMappingFile.getName()
                    + ", took " + tt);

        } catch (Exception e) {
            this.logger.error("<readISINMapping> failed for " + this.isinMappingFile.getName(), e);
        }
    }

    public WMData getWMData(long id) {
        WmDataProtos.WmMasterData.Builder builder = WmDataProtos.WmMasterData.newBuilder();
        try {
            if (build(id, builder) && builder.isInitialized()) {
                return asWMData(id, builder.build());
            }
        } catch (InvalidProtocolBufferException e) {
            this.logger.error("<getWMData> failed to deserialize data for " + id, e);
        }
        return null;
    }

    public WMData asWMData(long id, WmDataProtos.WmMasterData wmMasterData) {
        return WMDataImpl.create(id, asFieldMap(wmMasterData,
                WmDataProtos.WmMasterData.getDescriptor()));
    }

    private Map<String, ? extends WMData.Field> asFieldMap(Message message,
            Descriptors.Descriptor d) {
        HashMap<String, WMDataImpl.FieldImpl> result = new HashMap<>();
        for (Descriptors.FieldDescriptor fd : d.getFields()) {
            if ("iid".equals(fd.getName()) || (!fd.isRepeated() && !message.hasField(fd))) {
                continue;
            }
            Object value = message.getField(fd);
            String[] nameAndSuffix = fd.getName().split("_");
            String name = nameAndSuffix[0].toUpperCase();

            boolean isKey = false;
            boolean isTextinfo = false;
            boolean isStart = false;
            if (nameAndSuffix.length > 1) {
                switch (nameAndSuffix[1]) {
                    case "key":
                        isKey = true;
                        break;
                    case "textinfo":
                        isTextinfo = true;
                        break;
                    case "start":
                        isStart = true;
                        break;
                }
            }

            WMDataImpl.FieldImpl fieldImpl = result.get(name);
            if (fieldImpl == null) {
                fieldImpl = new WMDataImpl.FieldImpl(name);
                result.put(name, fieldImpl);
                if (fd.isRepeated() && fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
                    fieldImpl.setType(WMData.WMFieldType.SEQUENCE);
                    fieldImpl.setValue(asSequenceOfGroups(message, fd));
                    continue;
                }
                else {
                    final int type = fd.getOptions().getExtension(ProviderProtos.wmTypeOption);
                    fieldImpl.setType(WMData.WMFieldType.values()[type]);
                }
            }

            switch (fieldImpl.getType()) {
                case STRING:
                    if (isKey) {
                        fieldImpl.setKey(String.valueOf(value));
                    }
                    else if (isTextinfo) {
                        fieldImpl.setTextinfo(String.valueOf(value));
                    }
                    else {
                        fieldImpl.setValue(value);
                    }
                    break;
                case DATE:
                    fieldImpl.setValue(DateUtil.yyyyMmDdToLocalDate((Integer) value));
                    break;
                case DECIMAL:
                    fieldImpl.setValue(toBigDecimal(String.valueOf(value)));
                    break;
                case INTERVAL:
                    final Interval existing = (Interval) fieldImpl.getValue();
                    final LocalDate ld = DateUtil.yyyyMmDdToLocalDate((Integer) value);
                    fieldImpl.setValue(adapt(existing, ld, isStart));
                    break;
                default:
                    throw new IllegalArgumentException("cannot handle " + fieldImpl.getType());
            }
        }
        return result;
    }

    private Interval adapt(Interval existing, LocalDate date, boolean isStart) {
        final DateTime dt = date.toDateTimeAtStartOfDay();
        if (existing != null) {
            return isStart ? existing.withStart(dt) : existing.withEnd(dt);
        }
        // interval with one dummy side that will be changed later
        return isStart ? new Interval(dt, END_DATE) : new Interval(START_DATE, dt);
    }

    private ArrayList<WMData> asSequenceOfGroups(Message message, Descriptors.FieldDescriptor fd) {
        final ArrayList<WMData> result = new ArrayList<>(message.getRepeatedFieldCount(fd));
        for (int i = 0; i < message.getRepeatedFieldCount(fd); i++) {
            Map<String, ? extends WMData.Field> map =
                    asFieldMap((Message) message.getRepeatedField(fd, i), fd.getMessageType());
            result.add(WMDataImpl.create(0L, map));
        }
        return result;
    }

    @Override
    public WMDataResponse getData(WMDataRequest request) {
        final HashMap<Long, WMData> map = new HashMap<>();
        for (final Long iid : request.getIids()) {
            WMData data = getWMData(iid);
            if (data != null) {
                map.put(iid, filterWMData(request.getProfile(), iid, data));
            }
        }

        final Object2IntMap<String> mapping = this.isin2iidMapping;

        final HashMap<String, WMData> isinMap = new HashMap<>();
        for (final String isin : request.getIsins()) {
            int iid = mapping.getInt(isin);
            if (iid == 0) {
                continue;
            }
            WMData data = getWMData(iid & MAX_UNSIGNED_INT);
            if (data != null) {
                isinMap.put(isin, filterWMData(request.getProfile(), iid & MAX_UNSIGNED_INT, data));
            }
        }
        return new WMDataResponse(map, isinMap);
    }

    public WMData filterWMData(Profile profile, Long iid, WMData data) {
        if(profile.isAllowed(Selector.ANY_VWD_TERMINAL_PROFILE)) {
            return data;
        }
        Map<String, Field> filteredFields = new HashMap<>();
        for (Field field : data.getFields()) {
            if (UNRESTRICTED_FIELDS.contains(field.getName())) {
                filteredFields.put(field.getName(), field);
            }
        }
        return WMDataImpl.create(iid, filteredFields);
    }

    public static void main(String[] args) throws Exception {
        WMDataProviderImpl rdp = new WMDataProviderImpl();
        final File providerDir = LocalConfigProvider.getProductionDir("var/data/provider");
        rdp.setFile(args.length > 0 ? new File(args[0]) : new File(providerDir, "istar-wm-masterdata.buf"));
        rdp.setIsinMappingFile(new File(providerDir, "istar-wm-isin-mapping.xml.gz"));
        rdp.afterPropertiesSet();
        final WMDataRequest request = new WMDataRequest(ProfileFactory.valueOf(true),"DE000BASF111");
        final WMDataResponse response = rdp.getData(request);
        System.out.println(response.getData("DE000BASF111"));
    }

    /**
     * @author tkiesgen
     */
    static class WMISINMappingReader extends IstarMdpExportReader<Object2IntMap<String>> {
        private final Object2IntMap<String> values = new Object2IntOpenHashMap<>(1 << 20, 0.8f);

        protected Object2IntMap<String> getResult() {
            return this.values;
        }

        protected void handleRow() {
            final Long iid = getLong("IID");
            final String isin = get("ISIN");
            if (iid == null || !StringUtils.hasText(isin)) {
                return;
            }
            if (iid > MAX_UNSIGNED_INT) {
                throw new IllegalStateException(iid + " > MAX_MAX_UNSIGNED_INT, need Object2LongMap");
            }
            this.values.put(isin, (int) iid.longValue());
        }
    }
}

