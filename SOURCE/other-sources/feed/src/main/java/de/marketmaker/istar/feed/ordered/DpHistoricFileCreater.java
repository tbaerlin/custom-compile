package de.marketmaker.istar.feed.ordered;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.DateTimeProviderStatic;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.mdps.MdpsMessageTypes;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import java.io.File;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;

/**
 * For generating a sample DPH file in case needed for testing purposes.
 * Adjust OUTPUT_DIR if needed.
 *
 * @author ytas
 */
public class DpHistoricFileCreater {

  private static final String USER_HOME = System.getProperty("user.home");
  private static final String OUTPUT_DIR = USER_HOME + "/produktion/var/data/dph";

  protected static final int KEY_TYPE =
      MdpsTypeMappings.getMappingForMdpsKey(new ByteString("710000.ETR,E"));

  protected static final DateTime NOW = new DateTime(2012, 9, 24, 12, 0, 1, 0);

  protected static final DateTimeProvider DTP = new DateTimeProviderStatic(NOW);

  protected static ParsedRecord createRecord(Map<Integer, Object> map) {
    final ParsedRecord pr = new ParsedRecord();
    pr.reset(new FeedRecord(new byte[10], 0, 10));

    pr.setMessageType(VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE);
    pr.setKeyType(KEY_TYPE);
    pr.setMessageTimestamp(DTP.current().feedTimestamp);
    pr.setMessageTypeMdps((byte) MdpsMessageTypes.UPDATE);
    pr.setMessageType(VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE);
    setFields(pr, map);
    return pr;
  }

  private static void setFields(ParsedRecord pr, Map<Integer, Object> map) {
    final ByteBuffer b = ByteBuffer.wrap(pr.getRecord().getData());
    for (Map.Entry<Integer, Object> entry : map.entrySet()) {
      final Object v = entry.getValue();
      if (v instanceof Integer) {
        if (VwdFieldDescription.getField(entry.getKey()).type() == VwdFieldDescription.Type.TIME) {
          pr.setField(entry.getKey(), MdpsFeedUtils.encodeTime((Integer) v));
        } else {
          pr.setField(entry.getKey(), (Integer) v);
        }
      } else if (v instanceof Long) {
        pr.setField(entry.getKey(), (Long) v);
      } else if (v instanceof BigDecimal) {
        pr.setField(entry.getKey(), MdpsFeedUtils.encodePrice((BigDecimal) v));
      } else if (v instanceof String) {
        final int from = b.position();
        b.put(((String) v).getBytes(SnapRecord.DEFAULT_CHARSET));
        pr.setField(entry.getKey(), from, b.position() - from);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    final FeedDataRepository r = new FeedDataRepository();
    r.setDataFactory(OrderedFeedDataFactory.RT_NT_TICKS);
    final FeedData fd = r.register(VendorkeyVwd.getInstance("1.710000.ETR"));

    final Map<Integer, Object> fields = new HashMap<>();
    fields.put(VwdFieldDescription.ADF_Boersenzeit.id(), NOW.getSecondOfDay());
    fields.put(VwdFieldDescription.ADF_Bezahlt.id(), new BigDecimal("20.34"));
    fields.put(VwdFieldDescription.ADF_Bezahlt_Umsatz.id(), 2);
    fields.put(VwdFieldDescription.ADF_Quelle.id(), "ZZ");
    fields.put(VwdFieldDescription.ADF_Handelsdatum.id(), DateUtil.toYyyyMmDd(NOW));

    final OrderedFeedUpdateBuilder feedUpdateBuilder = new OrderedFeedUpdateBuilder();
    final DpHistoricUpdateBuilder builder = new DpHistoricUpdateBuilder();
    builder.setBaseDirectory(new File(OUTPUT_DIR));
    builder.setFieldIds(new int[]{
        VwdFieldDescription.ADF_Boersenzeit.id(),
        VwdFieldDescription.ADF_Bezahlt.id(),
        VwdFieldDescription.ADF_Bezahlt_Umsatz.id(),
        VwdFieldDescription.ADF_Quelle.id(),
        VwdFieldDescription.ADF_Handelsdatum.id()
    });
    builder.afterPropertiesSet();
    feedUpdateBuilder.setBuilders(new OrderedUpdateBuilder[]{builder});
    ParsedRecord pr = createRecord(fields);
    feedUpdateBuilder.process(fd, pr);
    fields.put(VwdFieldDescription.ADF_Handelsdatum.id(), DateUtil.toYyyyMmDd(NOW.minusDays(1)));
    pr = createRecord(fields);
    feedUpdateBuilder.process(fd, pr);
    builder.rotateFile();
  }

}
