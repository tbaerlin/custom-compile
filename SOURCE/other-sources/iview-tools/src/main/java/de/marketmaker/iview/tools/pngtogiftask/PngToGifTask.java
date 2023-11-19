package de.marketmaker.iview.tools.pngtogiftask;

import de.marketmaker.iview.tools.pngtogiftask.gif.DirectGif89Frame;
import de.marketmaker.iview.tools.pngtogiftask.gif.Gif89Encoder;
import de.marketmaker.iview.tools.pngtogiftask.gif.Gif89Frame;
import de.marketmaker.iview.tools.pngtogiftask.neuquant.NeuQuant;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author umaurer
 */
public class PngToGifTask extends MatchingTask {
    protected File srcDir = null;
    protected File destDir = null;
    protected boolean force = false;
    protected boolean ifNotExists = false;


    public static void main(String[] args) throws Exception {
        final PngToGifTask ptgt = new PngToGifTask();
        for (String arg : args) {
            final File file = new File(arg);
            System.out.println("convert " + file.toString());
            ptgt.setSrcDir(file.getParentFile());
            ptgt.setDestDir(file.getParentFile());
            ptgt.convert(file.getName());
        }
    }


    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    @Override
    public void execute() throws BuildException {
        if (this.srcDir == null) {
            throw new BuildException("No input files! srcdir has to be set.");
        }

        if (this.destDir == null) {
            throw new BuildException("destdir attribute is not set!");
        }

        if (this.force && this.ifNotExists) {
            throw new BuildException("force and ifNotExists cannot both be true");
        }

        fileset.setDir(this.srcDir);
        @SuppressWarnings({"deprecation"})
        DirectoryScanner ds = fileset.getDirectoryScanner(project);
        String[] includedFiles = ds.getIncludedFiles();
        // Add file and its path to the input file vector.
        int counter = 0;
        for (String includedFile : includedFiles) {
            if (includedFile.endsWith(".png")) {
                try {
                    final boolean converted = convert(includedFile);
                    if (converted) {
                        counter++;
                    }
                }
                catch (IOException e) {
                    throw new BuildException("cannot convert png file: " + includedFile, e);
                }
            }
        }
        log("converted files from png to gif - count: " + counter);
    }

    private boolean convert(String includedFile) throws IOException {
        final File sourceFile = new File(this.srcDir, includedFile);
        final String filename = sourceFile.getName();
        if (filename.matches(".*\\.[0-9]+\\.[0-9]+\\.png$")) {
            final File destFile = new File(this.destDir, includedFile.replaceFirst("\\.[0-9]+\\.[0-9]+\\.png$", ".gif"));
            if (this.ifNotExists && destFile.isFile()) {
                return false;
            }
            if (!this.force && destFile.isFile() && destFile.lastModified() > sourceFile.lastModified()) {
                return false;
            }
            final File destParent = destFile.getParentFile();
            if (!destParent.isDirectory()) {
                //noinspection ResultOfMethodCallIgnored
                destParent.mkdirs();
            }
            convert(sourceFile.getParentFile(), filename.replaceFirst("\\.[0-9]+\\.[0-9]+\\.png$", ""), destFile);
        }
        else {
            final File destFile = new File(this.destDir, includedFile.substring(0, includedFile.length() - 4) + ".gif");
            if (!this.force && destFile.isFile() && destFile.lastModified() > sourceFile.lastModified()) {
                return false;
            }
            final File destParent = destFile.getParentFile();
            if (!destParent.isDirectory()) {
                //noinspection ResultOfMethodCallIgnored
                destParent.mkdirs();
            }
            convert(sourceFile, destFile);
        }
        return true;
    }

    private void convert(File sourceFile, File destFile) throws IOException {
        final BufferedImage image = ImageIO.read(sourceFile);

        try {
            final FileOutputStream outputStream = new FileOutputStream(destFile);
            new Gif89Encoder(image).encode(outputStream);
            outputStream.close();
        }
        catch (IOException e) {
            final FileOutputStream outputStream = new FileOutputStream(destFile);
            final NeuQuant nq = new NeuQuant(image);
            nq.init();
            new Gif89Encoder(dither(nq, image)).encode(outputStream);
            outputStream.close();
        }
        catch (RuntimeException e) {
            throw new RuntimeException("cannot convert " + sourceFile.getAbsolutePath() + " to " + destFile.getAbsolutePath(), e);
        }
    }

    class AnimationFile implements Comparable<AnimationFile> {
        final int id;
        final int delayMillis;
        final File file;

        AnimationFile(int id, int delayMillis, File file) {
            this.id = id;
            this.delayMillis = delayMillis;
            this.file = file;
        }

        public int getId() {
            return id;
        }

        public int getDelayMillis() {
            return delayMillis;
        }

        public File getFile() {
            return file;
        }

        public int compareTo(AnimationFile o) {
            return this.id - o.id;
        }
    }

    private void convert(final File sourceDir, final String baseName, final File destFile) throws IOException {
        final File[] files = sourceDir.listFiles();
        if (files == null) {
            return;
        }
        final java.util.List<AnimationFile> list = new ArrayList<>();
        for (final File file : files) {
            final String filename = file.getName();
            if (!filename.startsWith(baseName)) {
                continue;
            }
            final Pattern pattern = Pattern.compile("^\\.([0-9]+)\\.([0-9]+)\\.png$");
            final Matcher matcher = pattern.matcher(filename.substring(baseName.length()));
            if (!matcher.matches()) {
                continue;
            }
            final int id = Integer.parseInt(matcher.group(1));
            final int durationMillis = Integer.parseInt(matcher.group(2));
            list.add(new AnimationFile(id, durationMillis, file));
        }

        Collections.sort(list);

        final NeuQuant nq = initNeuQuant(list);
        nq.init();

        final Gif89Encoder encoder = new Gif89Encoder();
        encoder.setLoopCount(0);
        for (AnimationFile animationFile : list) {
            try {
                final BufferedImage image = dither(nq, ImageIO.read(animationFile.getFile()));
                final Gif89Frame frame = new DirectGif89Frame(image);
                frame.setDelay(animationFile.getDelayMillis() / 10);
                frame.setDisposalMode(Gif89Frame.DM_BGCOLOR);
                encoder.addFrame(frame);
            }
            catch (IOException e) {
                throw new BuildException("cannot convert animated gif file: " + animationFile.getFile().getName(), e);
            }
        }
        final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destFile));
        encoder.encode(outputStream);
        outputStream.close();
    }

    private NeuQuant initNeuQuant(final java.util.List<AnimationFile> list) throws IOException {
        AnimationFile af = list.get(0);
        BufferedImage image = ImageIO.read(af.getFile());
        final int width = image.getWidth();
        final int height = image.getHeight();
        final BufferedImage bigImage = createCompatibleDestImage(image, width, height * list.size(), null);
        final Graphics2D g2 = bigImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        for (int i = 1; i < list.size(); i++) {
            image = ImageIO.read(list.get(i).getFile());
            g2.drawImage(image, 0, height * i, null);
        }
        return new NeuQuant(bigImage);
    }

    private BufferedImage dither(NeuQuant nq, BufferedImage src) {
        final BufferedImage dest = createCompatibleDestImage(src, null);

        int width = src.getWidth ();
        int height = src.getHeight ();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                dest.setRGB (x, y, nq.convert(src.getRGB(x, y)));
            }
        }

        return dest;
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        return createCompatibleDestImage(src, src.getWidth(), src.getHeight(), destCM);
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src, int width, int height, ColorModel destCM) {
        if (destCM == null) {
            destCM = src.getColorModel();
        }

        return new BufferedImage (
               destCM,
               destCM.createCompatibleWritableRaster (width, height),
               destCM.isAlphaPremultiplied (),
               null);
    }





}
