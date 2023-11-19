package de.marketmaker.istar.feed.exporttools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.zip.GZIPOutputStream;

import de.marketmaker.istar.feed.ordered.tick.TickFileMapper;

public abstract class ExporterBaseWriter extends TickFileMapper {

    public ExporterBaseWriter(ByteOrder byteOrder, File file) {
        super(byteOrder, file);
    }

    public void process(File out) throws IOException {
        try (FileChannel chIn = new RandomAccessFile(this.file, "r").getChannel();
             FileOutputStream fos = new FileOutputStream(out);
             GZIPOutputStream gzos = new GZIPOutputStream(fos);
             PrintWriter pw = new PrintWriter(gzos)) {
            process(chIn, pw);
        } catch (Throwable t) {
            this.logger.error("FATAL error for " + this.file.getName(), t);
        } finally {
            unmapBuffers();
        }
    }

    protected abstract void process(FileChannel chIn, PrintWriter pw) throws IOException;

}
