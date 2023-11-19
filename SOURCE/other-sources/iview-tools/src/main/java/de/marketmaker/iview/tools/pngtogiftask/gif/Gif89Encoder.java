//******************************************************************************
// Gif89Encoder.java
//******************************************************************************
package de.marketmaker.iview.tools.pngtogiftask.gif;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

//==============================================================================

/**
 * This is the central class of a JDK 1.1 compatible GIF encoder that, AFAIK,
 * supports more features of the extended GIF spec than any other Java open
 * source encoder.  Some sections of the source are lifted or adapted from Jef
 * Poskanzer's <cite>Acme GifEncoder</cite> (so please see the
 * <a href="../readme.txt">readme</a> containing his notice), but much of it,
 * including nearly all of the present class, is original code.  My main
 * motivation for writing a new encoder was to support animated GIFs, but the
 * package also adds support for embedded textual comments.
 * <p/>
 * There are still some limitations.  For instance, animations are limited to
 * a single global color table.  But that is usually what you want anyway, so
 * as to avoid irregularities on some displays.  (So this is not really a
 * limitation, but a "disciplinary feature" :)  Another rather more serious
 * restriction is that the total number of RGB colors in a given input-batch
 * mustn't exceed 256.  Obviously, there is an opening here for someone who
 * would like to add a color-reducing preprocessor.
 * <p/>
 * The encoder, though very usable in its present form, is at bottom only a
 * partial implementation skewed toward my own particular needs.  Hence a
 * couple of caveats are in order.  (1) During development it was in the back
 * of my mind that an encoder object should be reusable - i.e., you should be
 * able to make multiple calls to encode() on the same object, with or without
 * intervening frame additions or changes to options.  But I haven't reviewed
 * the code with such usage in mind, much less tested it, so it's likely I
 * overlooked something.  (2) The encoder classes aren't thread safe, so use
 * caution in a context where access is shared by multiple threads.  (Better
 * yet, finish the library and re-release it :)
 * <p/>
 * There follow a couple of simple examples illustrating the most common way to
 * use the encoder, i.e., to encode AWT Image objects created elsewhere in the
 * program.  Use of some of the most popular format options is also shown,
 * though you will want to peruse the API for additional features.
 * <p/>
 * <p/>
 * <strong>Animated GIF Example</strong>
 * <pre>
 *  import net.jmge.gif.Gif89Encoder;
 *  // ...
 *  void writeAnimatedGIF(Image[] still_images,
 *                        String annotation,
 *                        boolean looped,
 *                        double frames_per_second,
 *                        OutputStream out) throws IOException
 *  {
 *    Gif89Encoder gifenc = new Gif89Encoder();
 *    for (int i = 0; i < still_images.length; ++i)
 *      gifenc.addFrame(still_images[i]);
 *    gifenc.setComments(annotation);
 *    gifenc.setLoopCount(looped ? 0 : 1);
 *    gifenc.setUniformDelay((int) Math.round(100 / frames_per_second));
 *    gifenc.encode(out);
 *  }
 *  </pre>
 *
 * <strong>Static GIF Example</strong>
 * <pre>
 *  import net.jmge.gif.Gif89Encoder;
 *  // ...
 *  void writeNormalGIF(Image img,
 *                      String annotation,
 *                      int transparent_index,  // pass -1 for none
 *                      boolean interlaced,
 *                      OutputStream out) throws IOException
 *  {
 *    Gif89Encoder gifenc = new Gif89Encoder(img);
 *    gifenc.setComments(annotation);
 *    gifenc.setTransparentIndex(transparent_index);
 *    gifenc.getFrameAt(0).setInterlaced(interlaced);
 *    gifenc.encode(out);
 *  }
 *  </pre>
 *
 * @author J. M. G. Elliott (tep@jmge.net)
 * @version 0.90 beta (15-Jul-2000)
 * @see Gif89Frame
 * @see DirectGif89Frame
 * @see IndexGif89Frame
 */
@SuppressWarnings("UnusedDeclaration")
public class Gif89Encoder {

    private Dimension dispDim = new Dimension(0, 0);
    private GifColorTable colorTable;
    private int bgIndex = 0;
    private int loopCount = 1;
    private String theComments;
    private ArrayList<Gif89Frame> vFrames = new ArrayList<>();

    //----------------------------------------------------------------------------

    /**
     * Use this default constructor if you'll be adding multiple frames
     * constructed from RGB data (i.e., AWT Image objects or ARGB-pixel arrays).
     */
    public Gif89Encoder() {
        // empty color table puts us into "palette autodetect" mode
        colorTable = new GifColorTable();
    }

    //----------------------------------------------------------------------------

    /**
     * Like the default except that it also adds a single frame, for conveniently
     * encoding a static GIF from an image.
     *
     * @param static_image Any Image object that supports pixel-grabbing.
     * @throws IOException See the addFrame() methods.
     */
    public Gif89Encoder(Image static_image) throws IOException {
        this();
        addFrame(static_image);
    }

    //----------------------------------------------------------------------------

    /**
     * This constructor installs a user color table, overriding the detection of
     * of a palette from ARBG pixels.
     * <p/>
     * Use of this constructor imposes a couple of restrictions:
     * (1) Frame objects can't be of type DirectGif89Frame
     * (2) Transparency, if desired, must be set explicitly.
     *
     * @param colors Array of color values; no more than 256 colors will be read, since that's
     *               the limit for a GIF.
     */
    public Gif89Encoder(Color[] colors) {
        colorTable = new GifColorTable(colors);
    }

    //----------------------------------------------------------------------------

    /**
     * Convenience constructor for encoding a static GIF from index-model data.
     * Adds a single frame as specified.
     *
     * @param colors    Array of color values; no more than 256 colors will be read, since
     *                  that's the limit for a GIF.
     * @param width     Width of the GIF bitmap.
     * @param height    Height of same.
     * @param ci_pixels Array of color-index pixels no less than width * height in length.
     * @throws IOException See the addFrame() methods.
     */
    public Gif89Encoder(Color[] colors, int width, int height, byte ci_pixels[])
            throws IOException {
        this(colors);
        addFrame(width, height, ci_pixels);
    }

    //----------------------------------------------------------------------------

    /**
     * Get the number of frames that have been added so far.
     *
     * @return Number of frame items.
     */
    public int getFrameCount() {
        return vFrames.size();
    }

    //----------------------------------------------------------------------------

    /**
     * Get a reference back to a Gif89Frame object by position.
     *
     * @param index Zero-based index of the frame in the sequence.
     * @return Gif89Frame object at the specified position (or null if no such frame).
     */
    public Gif89Frame getFrameAt(int index) {
        return isOk(index) ? vFrames.get(index) : null;
    }

    //----------------------------------------------------------------------------

    /**
     * Add a Gif89Frame frame to the end of the internal sequence.  Note that
     * there are restrictions on the Gif89Frame type: if the encoder object was
     * constructed with an explicit color table, an attempt to add a
     * DirectGif89Frame will throw an exception.
     *
     * @param gf An externally constructed Gif89Frame.
     * @throws IOException If Gif89Frame can't be accommodated.  This could happen if either (1) the
     *                     aggregate cross-frame RGB color count exceeds 256, or (2) the Gif89Frame
     *                     subclass is incompatible with the present encoder object.
     */
    public void addFrame(Gif89Frame gf) throws IOException {
        accommodateFrame(gf);
        vFrames.add(gf);
    }

    //----------------------------------------------------------------------------

    /**
     * Convenience version of addFrame() that takes a Java Image, internally
     * constructing the requisite DirectGif89Frame.
     *
     * @param image Any Image object that supports pixel-grabbing.
     * @throws IOException If either (1) pixel-grabbing fails, (2) the aggregate cross-frame RGB
     *                     color count exceeds 256, or (3) this encoder object was constructed with
     *                     an explicit color table.
     */
    public void addFrame(Image image) throws IOException {
        addFrame(new DirectGif89Frame(image));
    }

    //----------------------------------------------------------------------------

    /**
     * The index-model convenience version of addFrame().
     *
     * @param width     Width of the GIF bitmap.
     * @param height    Height of same.
     * @param ci_pixels Array of color-index pixels no less than width * height in length.
     * @throws IOException Actually, in the present implementation, there aren't any unchecked
     *                     exceptions that can be thrown when adding an IndexGif89Frame
     *                     <i>per se</i>.  But I might add some pedantic check later, to justify the
     *                     generality :)
     */
    public void addFrame(int width, int height, byte ci_pixels[])
            throws IOException {
        addFrame(new IndexGif89Frame(width, height, ci_pixels));
    }

    //----------------------------------------------------------------------------

    /**
     * Like addFrame() except that the frame is inserted at a specific point in
     * the sequence rather than appended.
     *
     * @param index Zero-based index at which to insert frame.
     * @param gf    An externally constructed Gif89Frame.
     * @throws IOException If Gif89Frame can't be accommodated.  This could happen if either (1)
     *                     the aggregate cross-frame RGB color count exceeds 256, or (2) the
     *                     Gif89Frame subclass is incompatible with the present encoder object.
     */
    public void insertFrame(int index, Gif89Frame gf) throws IOException {
        accommodateFrame(gf);
        vFrames.add(index, gf);
    }

    //----------------------------------------------------------------------------

    /**
     * Set the color table index for the transparent color, if any.
     *
     * @param index Index of the color that should be rendered as transparent, if any.
     *              A value of -1 turns off transparency.  (Default: -1)
     */
    public void setTransparentIndex(int index) {
        colorTable.setTransparent(index);
    }

    //----------------------------------------------------------------------------

    /**
     * Sets attributes of the multi-image display area, if applicable.
     *
     * @param dim        Width/height of display.  (Default: largest detected frame size)
     * @param background Color table index of background color.  (Default: 0)
     * @see Gif89Frame#setPosition
     */
    public void setLogicalDisplay(Dimension dim, int background) {
        dispDim = new Dimension(dim);
        bgIndex = background;
    }

    //----------------------------------------------------------------------------

    /**
     * Set animation looping parameter, if applicable.
     *
     * @param count Number of times to play sequence.  Special value of 0 specifies
     *              indefinite looping.  (Default: 1)
     */
    public void setLoopCount(int count) {
        loopCount = count;
    }

    //----------------------------------------------------------------------------

    /**
     * Specify some textual comments to be embedded in GIF.
     *
     * @param comments String containing ASCII comments.
     */
    public void setComments(String comments) {
        theComments = comments;
    }

    //----------------------------------------------------------------------------

    /**
     * A convenience method for setting the "animation speed".  It simply sets
     * the delay parameter for each frame in the sequence to the supplied value.
     * Since this is actually frame-level rather than animation-level data, take
     * care to add your frames before calling this method.
     *
     * @param interval Interframe interval in centiseconds.
     */
    public void setUniformDelay(int interval) {
        for (Gif89Frame vFrame : vFrames) {
            vFrame.setDelay(interval);
        }
    }

    //----------------------------------------------------------------------------

    /**
     * After adding your frame(s) and setting your options, simply call this
     * method to write the GIF to the passed stream.  Multiple calls are
     * permissible if for some reason that is useful to your application.  (The
     * method simply encodes the current state of the object with no thought
     * to previous calls.)
     *
     * @param out The stream you want the GIF written to.
     * @throws IOException If a write error is encountered.
     */
    public void encode(OutputStream out) throws IOException {
        boolean is_sequence = getFrameCount() > 1;

        // N.B. must be called before writing screen descriptor
        colorTable.closePixelProcessing();

        // write GIF HEADER
        Put.ascii("GIF89a", out);

        // write global blocks
        writeLogicalScreenDescriptor(out);
        colorTable.encode(out);
        if (is_sequence && loopCount != 1) {
            writeNetscapeExtension(out);
        }
        if (theComments != null && theComments.length() > 0) {
            writeCommentExtension(out);
        }

        // write out the control and rendering data for each frame
        for (Gif89Frame vFrame : vFrames) {
            vFrame.encode(out, is_sequence, colorTable.getDepth(), colorTable.getTransparent());
        }

        // write GIF TRAILER
        out.write((int) ';');

        out.flush();
    }

    //----------------------------------------------------------------------------

    /**
     * A simple driver to test the installation and to demo usage.  Put the JAR
     * on your classpath and run ala
     * <blockquote>java net.jmge.gif.Gif89Encoder {filename}</blockquote>
     * The filename must be either (1) a JPEG file with extension 'jpg', for
     * conversion to a static GIF, or (2) a file containing a list of GIFs and/or
     * JPEGs, one per line, to be combined into an animated GIF.  The output will
     * appear in the current directory as 'gif89out.gif'.
     * <p/>
     * (N.B. This test program will abort if the input file(s) exceed(s) 256 total
     * RGB colors, so in its present form it has no value as a generic JPEG to GIF
     * converter.  Also, when multiple files are input, you need to be wary of the
     * total color count, regardless of file type.)
     *
     * @param args Command-line arguments, only the first of which is used, as mentioned
     *             above.
     */
    public static void main(String[] args) {
        try {
            if (args[0].toLowerCase().endsWith(".jpg") || args[0].toLowerCase().endsWith(".png")) {
                final File fileIn = new File(args[0]);
                final File fileOut = new File(args[0].substring(0, args[0].length() - 4) + ".gif");
                final OutputStream out = new BufferedOutputStream(new FileOutputStream(fileOut));
                new Gif89Encoder(ImageIO.read(fileIn)).encode(out);
                out.close();
                System.out.println("file written to " + fileOut.getPath());
            }
            else {
                final OutputStream out = new BufferedOutputStream(new FileOutputStream("gif89out.gif"));
                final BufferedReader in = new BufferedReader(new FileReader(args[0]));
                final Gif89Encoder ge = new Gif89Encoder();

                String line;
                while ((line = in.readLine()) != null) {
                    ge.addFrame(ImageIO.read(new File(line.trim())));
                }
                ge.setLoopCount(0);  // let's loop indefinitely
                ge.encode(out);

                in.close();
                out.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------------
    private void accommodateFrame(Gif89Frame gf) throws IOException {
        dispDim.width = Math.max(dispDim.width, gf.getWidth());
        dispDim.height = Math.max(dispDim.height, gf.getHeight());
        colorTable.processPixels(gf);
    }

    //----------------------------------------------------------------------------
    private void writeLogicalScreenDescriptor(OutputStream os) throws IOException {
        Put.leShort(dispDim.width, os);
        Put.leShort(dispDim.height, os);

        // write 4 fields, packed into a byte  (bitfieldsize:value)
        //   global color map present?         (1:1)
        //   bits per primary color less 1     (3:7)
        //   sorted color table?               (1:0)
        //   bits per pixel less 1             (3:varies)
        os.write(0xf0 | colorTable.getDepth() - 1);

        // write background color index
        os.write(bgIndex);

        // Jef Poskanzer's notes on the next field, for our possible edification:
        // Pixel aspect ratio - 1:1.
        //Putbyte( (byte) 49, outs );
        // Java's GIF reader currently has a bug, if the aspect ratio byte is
        // not zero it throws an ImageFormatException.  It doesn't know that
        // 49 means a 1:1 aspect ratio.  Well, whatever, zero works with all
        // the other decoders I've tried so it probably doesn't hurt.

        // OK, if it's good enough for Jef, it's definitely good enough for us:
        os.write(0);
    }

    //----------------------------------------------------------------------------
    private void writeNetscapeExtension(OutputStream os) throws IOException {
        // n.b. most software seems to interpret the count as a repeat count
        // (i.e., interations beyond 1) rather than as an iteration count
        // (thus, to avoid repeating we have to omit the whole extension)

        os.write((int) '!');           // GIF Extension Introducer
        os.write(0xff);                // Application Extension Label

        os.write(11);                  // application ID block size
        Put.ascii("NETSCAPE2.0", os);  // application ID data

        os.write(3);                   // data sub-block size
        os.write(1);                   // a looping flag? dunno

        // we finally write the relevent data
        Put.leShort(loopCount > 1 ? loopCount - 1 : 0, os);

        os.write(0);                   // block terminator
    }

    //----------------------------------------------------------------------------
    private void writeCommentExtension(OutputStream os) throws IOException {
        os.write((int) '!');     // GIF Extension Introducer
        os.write(0xfe);          // Comment Extension Label

        int remainder = theComments.length() % 255;
        int nsubblocks_full = theComments.length() / 255;
        int nsubblocks = nsubblocks_full + (remainder > 0 ? 1 : 0);
        int ibyte = 0;
        for (int isb = 0; isb < nsubblocks; ++isb) {
            int size = isb < nsubblocks_full ? 255 : remainder;

            os.write(size);
            Put.ascii(theComments.substring(ibyte, ibyte + size), os);
            ibyte += size;
        }

        os.write(0);    // block terminator
    }

    //----------------------------------------------------------------------------
    private boolean isOk(int frame_index) {
        return frame_index >= 0 && frame_index < vFrames.size();
    }
}


