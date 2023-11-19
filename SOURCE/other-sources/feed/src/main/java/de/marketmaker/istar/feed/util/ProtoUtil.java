package de.marketmaker.istar.feed.util;

import com.google.protobuf.ByteString;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldData;
import de.marketmaker.istar.feed.ordered.FieldDataIterator;
import dev.infrontfinance.dm.proto.DmChicago.IntIds;
import dev.infrontfinance.dm.proto.DmChicago.SnapFieldValues;
import dev.infrontfinance.dm.proto.DmChicago.SnapFieldsReq;
import dev.infrontfinance.dm.proto.DmChicago.SnapValue;
import dev.infrontfinance.dm.proto.DmBase.ReqInfo;
import dev.infrontfinance.dm.proto.DmBase.RespInfo;
import java.lang.management.ManagementFactory;
import java.util.BitSet;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zzhao
 */
public class ProtoUtil {

  private static final AtomicLong SEQ = new AtomicLong(0);

  private ProtoUtil() {
    throw new AssertionError("not for instantiation or inheritance");
  }

  public static ReqInfo getReqInfo() {
    return getReqInfo(Locale.GERMANY);
  }

  public static ReqInfo getReqInfo(Locale locale) {
    return ReqInfo.newBuilder()
        .setClientInfo(getRuntimeInfo())
        .setLocale(locale.getLanguage() + "_" + locale.getCountry())
        .build();
  }

  private static String getRuntimeInfo() {
    return String.join(",",
        ManagementFactory.getRuntimeMXBean().getName(),
        "" + System.currentTimeMillis(),
        "" + SEQ.incrementAndGet());
  }

  public static RespInfo getRespInfo(boolean valid) {
    return RespInfo.newBuilder()
        .setServerInfo(getRuntimeInfo())
        .setValid(valid)
        .build();
  }

  public static int[] getVwdFieldOrderIds(SnapFieldsReq req) {
    if (req.hasOrderBitSet()) {
      final BitSet bs = BitSet.valueOf(req.getOrderBitSet().toByteArray());
      return bs.stream().toArray();
    } else {
      final IntIds orderList = req.getOrderList();
      return orderList.getIdsList().stream().mapToInt(Integer::intValue).toArray();
    }
  }

  public static SnapFieldValues.Builder getSnapFieldValues(int timestamp,
      FieldDataIterator fdi, byte[] data, BufferFieldData bfd, int xFeedType) {
    final SnapFieldValues.Builder builder = SnapFieldValues.newBuilder();
    builder.setTimestamp(timestamp);
    // we only interested in values [0-18], one (signed) byte is enough. Otherwise, we don't set. See: MdpsTypeMappings.VWD2MDPS_TYPE
    // This is what we only need on Dpman to resolve entitlement special case related to Mdps old security types.
    if (xFeedType >= 0 && xFeedType <= Byte.MAX_VALUE) {
      builder.setXFeedType(ByteString.copyFrom(new byte[]{(byte) xFeedType}));
    }
    fdi.iterate(bfd.reset(data), fd -> {
      if (fd != null) {
        final SnapValue.Builder valBuilder = SnapValue.newBuilder();
        switch (fd.getType()) {
          case FieldData.TYPE_INT:
          case FieldData.TYPE_TIME:
            valBuilder.setIntVal(fd.getInt());
            break;
          case FieldData.TYPE_PRICE:
            valBuilder.setIntVal(fd.getInt());
            valBuilder.setByteVal(fd.getByte());
            break;
          case FieldData.TYPE_STRING:
            if (fd.getLength() > 0) {
              valBuilder.setStrVal(
                  com.google.protobuf.ByteString.copyFrom(fd.getBytes()));
            }
        }
        builder.addValues(valBuilder);
      } else {
        builder.addValues(SnapValue.getDefaultInstance());
      }
    });

    return builder;
  }
}
