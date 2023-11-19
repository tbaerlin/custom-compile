package de.marketmaker.iview.tools.pngtogiftask.gif;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;

//==============================================================================
class GifColorTable {

    // the palette of ARGB colors, packed as returned by Color.getRGB()
    private int[] theColors = new int[256];

    // other basic attributes
    private int colorDepth;
    private int transparentIndex = -1;

    // these fields track color-index info across frames
    private int ciCount = 0; // count of distinct color indices
    private ReverseColorMap ciLookup;    // cumulative rgb-to-ci lookup table

    //----------------------------------------------------------------------------
    GifColorTable() {
        ciLookup = new ReverseColorMap();  // puts us into "auto-detect mode"
    }


    //----------------------------------------------------------------------------
    GifColorTable(Color[] colors) {
        int n2copy = Math.min(theColors.length, colors.length);
        for (int i = 0; i < n2copy; ++i)
            theColors[i] = colors[i].getRGB();
    }

    //----------------------------------------------------------------------------
    int getDepth() {
        return colorDepth;
    }

    //----------------------------------------------------------------------------
    int getTransparent() {
        return transparentIndex;
    }

    //----------------------------------------------------------------------------
    // default: -1 (no transparency)
    void setTransparent(int color_index) {
        transparentIndex = color_index;
    }

    //----------------------------------------------------------------------------
    void processPixels(Gif89Frame gf) throws IOException {
        if (gf instanceof DirectGif89Frame)
            filterPixels((DirectGif89Frame) gf);
        else
            trackPixelUsage((IndexGif89Frame) gf);
    }

    //----------------------------------------------------------------------------
    void closePixelProcessing()  // must be called before encode()
    {
        colorDepth = computeColorDepth(ciCount);
    }

    //----------------------------------------------------------------------------
    void encode(OutputStream os) throws IOException {
        // size of palette written is the smallest power of 2 that can accomdate
        // the number of RGB colors detected (or largest color index, in case of
        // index pixels)
        int palette_size = 1 << colorDepth;
        for (int i = 0; i < palette_size; ++i) {
            os.write(theColors[i] >> 16 & 0xff);
            os.write(theColors[i] >> 8 & 0xff);
            os.write(theColors[i] & 0xff);
        }
    }

    //----------------------------------------------------------------------------
    // This method accomplishes three things:
    // (1) converts the passed rgb pixels to indexes into our rgb lookup table
    // (2) fills the rgb table as new colors are encountered
    // (3) looks for transparent pixels so as to set the transparent index
    // The information is cumulative across multiple calls.
    //
    // (Note: some of the logic is borrowed from Jef Poskanzer's code.)
    //----------------------------------------------------------------------------
    private void filterPixels(DirectGif89Frame dgf) throws IOException {
        if (ciLookup == null)
            throw new IOException("RGB frames require palette autodetection");

        this.transparentIndex = 0;
        this.theColors[0] = 0x00d808e5;
        this.ciCount++;

        int[] argb_pixels = (int[]) dgf.getPixelSource();
        byte[] ci_pixels = dgf.getPixelSink();
        int npixels = argb_pixels.length;
        for (int i = 0; i < npixels; ++i) {
            int argb = argb_pixels[i];

            // handle transparency
            if ((argb >>> 24) < 0x80)        // transparent pixel?
                if (transparentIndex == -1)    // first transparent color encountered?
                    transparentIndex = ciCount;  // record its index
                else if (argb != theColors[transparentIndex]) // different pixel value?
                {
                    // collapse all transparent pixels into one color index
                    ci_pixels[i] = (byte) transparentIndex;
                    continue;  // CONTINUE - index already in table
                }

            // try to look up the index in our "reverse" color table
            int color_index = ciLookup.getPaletteIndex(argb & 0xffffff);

            if (color_index == -1)  // if it isn't in there yet
            {
                if (ciCount == 256)
                    throw new IOException("can't encode as GIF (> 256 colors)");

                // store color in our accumulating palette
                theColors[ciCount] = argb;

                // store index in reverse color table
                ciLookup.put(argb & 0xffffff, ciCount);

                // send color index to our output array
                ci_pixels[i] = (byte) ciCount;

                // increment count of distinct color indices
                ++ciCount;
            }
            else  // we've already snagged color into our palette
                ci_pixels[i] = (byte) color_index;  // just send filtered pixel
        }
    }

    //----------------------------------------------------------------------------
    private void trackPixelUsage(IndexGif89Frame igf) throws IOException {
        byte[] ci_pixels = (byte[]) igf.getPixelSource();
        int npixels = ci_pixels.length;
        for (int i = 0; i < npixels; ++i)
            if (ci_pixels[i] >= ciCount)
                ciCount = ci_pixels[i] + 1;
    }

    //----------------------------------------------------------------------------
    private int computeColorDepth(int colorcount) {
        // color depth = log-base-2 of maximum number of simultaneous colors, i.e.
        // bits per color-index pixel
        if (colorcount <= 2)
            return 1;
        if (colorcount <= 4)
            return 2;
        if (colorcount <= 16)
            return 4;
        return 8;
    }
}
