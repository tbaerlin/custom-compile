/*
 * NewsDumpViewer.java
 *
 * Created on 25.10.12 09:39
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.feed.mdps.MdpsNewsParser;
import de.marketmaker.istar.feed.mdps.SimpleMdpsRecordSource;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.news.backend.NewsRecordBuilder;
import de.marketmaker.istar.news.backend.NewsRecordHandler;
import de.marketmaker.istar.news.data.NewsRecordImpl;

/**
 * View news dumped with {@link de.marketmaker.istar.feed.dump.MdpsFeedDumper}.
 * @author oflege
 */
public class NewsDumpViewer extends NewsViewerBase implements NewsRecordHandler {
    private final MdpsNewsParser parser = new MdpsNewsParser();

    private Predicate<NewsRecordImpl> predicate = (n) -> true;

    public NewsDumpViewer() {
        final NewsRecordBuilder builder = new NewsRecordBuilder();
        builder.setAcceptNewsWithoutSelector(true);
        builder.setLogOldNews(false);
        builder.setHandler(this);
        parser.setFeedBuilders(builder);
    }

    private void and(Predicate<NewsRecordImpl> p) {
        this.predicate = this.predicate.and(p);
    }

    private void or(Predicate<NewsRecordImpl> p) {
        this.predicate = this.predicate.or(p);
    }

    private void dump(byte[] bytes) throws InterruptedException {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        SimpleMdpsRecordSource smrs = new SimpleMdpsRecordSource(bb);

        while (bb.hasRemaining()) {
            this.parser.parse(smrs.getFeedRecord());
        }
    }

    @Override
    public void handle(NewsRecordImpl newsRecord) {
        if (this.predicate.test(newsRecord)) {
            printItem(newsRecord);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        NewsDumpViewer viewer = new NewsDumpViewer();
        int n = 0;
        while (n < args.length && args[n].startsWith("-")) {
            if ("-t".equals(args[n])) {
                viewer.textLen = Integer.parseInt(args[++n]);
            }
            else if ("-rt".equals(args[n])) {
                viewer.rawTextLen = Integer.parseInt(args[++n]);
            }
            else if ("-p".equals(args[n])) {
                viewer.predicate = parsePredicate(args[++n].split("=", 2));
            }
            else if ("-a".equals(args[n]) || "-o".equals(args[n])) {
                Consumer<Predicate<NewsRecordImpl>> c = "-a".equals(args[n])
                        ? viewer::and : viewer::or;
                c.accept(parsePredicate(args[++n].split("=", 2)));
            }
            else if ("-h".equals(args[n])) {
                System.err.println("Usage: NewsDumpViewer [options] files");
                System.err.println("Options:");
                System.err.println("-t <n> : print news story (at most n chars, 0 = all)");
                System.err.println("-rt <n> : print raw news story (at most n chars, 0 = all)");
                System.err.println("-p <field>(=|=~)<value> : set predicate to filter news (default: true)");
                System.err.println("-a <field>(=|=~)<value> : add and predicate to filter news");
                System.err.println("-o <field>(=|=~)<value> : add or predicate to filter news");
                return;
            }
            n++;
        }

        for (int i = n; i < args.length; i++) {
            String arg = args[i];
            File fileIn = new File(arg);
            byte[] bytes = FileCopyUtils.copyToByteArray(new InflaterInputStream(new FileInputStream(fileIn),
                    new Inflater(true), 8 * 1024));
            viewer.dump(bytes);
        }
    }

    protected static Predicate<NewsRecordImpl> parsePredicate(String[] fieldAndValue) {
        int fid = VwdFieldDescription.getFieldByName(fieldAndValue[0]).id();
        boolean match = fieldAndValue[1].startsWith("~");
        String value = match ? fieldAndValue[1].substring(1) : fieldAndValue[1];
        return (nr) -> {
            SnapField sf = nr.getSnapRecord().getField(fid);
            if (!sf.isDefined()) {
                return false;
            }
            if ("*".equals(value)) {
                return true;
            }
            String str = String.valueOf(sf.getValue());
            return match ? str.contains(value) : str.equals(value);
        };
    }
}
