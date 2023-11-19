package de.marketmaker.iview.tools.pngtogiftask.gif;

//==============================================================================
//==============================================================================
// We're doing a very simple linear hashing thing here, which seems sufficient
// for our needs.  I make no claims for this approach other than that it seems
// an improvement over doing a brute linear search for each pixel on the one
// hand, and creating a Java object for each pixel (if we were to use a Java
// Hashtable) on the other.  Doubtless my little hash could be improved by
// tuning the capacity (at the very least).  Suggestions are welcome.
class ReverseColorMap {

    private static class ColorRecord {
        int rgb;
        int ipalette;

        ColorRecord(int rgb, int ipalette) {
            this.rgb = rgb;
            this.ipalette = ipalette;
        }
    }

    // I wouldn't really know what a good hashing capacity is, having missed out
    // on data structures and algorithms class :)  Alls I know is, we've got a lot
    // more space than we have time.  So let's try a sparse table with a maximum
    // load of about 1/8 capacity.
    private static final int HCAPACITY = 2053;  // a nice prime number

    // our hash table proper
    private ColorRecord[] hTable = new ColorRecord[HCAPACITY];

    //----------------------------------------------------------------------------
    // Assert: rgb is not negative (which is the same as saying, be sure the
    // alpha transparency byte - i.e., the high byte - has been masked out).
    //----------------------------------------------------------------------------
    int getPaletteIndex(int rgb) {
        ColorRecord rec;

        //noinspection StatementWithEmptyBody
        for (int itable = rgb % hTable.length;
             (rec = hTable[itable]) != null && rec.rgb != rgb;
             itable = ++itable % hTable.length
                )
            ;

        if (rec != null)
            return rec.ipalette;

        return -1;
    }

    //----------------------------------------------------------------------------
    // Assert: (1) same as above; (2) rgb key not already present
    //----------------------------------------------------------------------------
    void put(int rgb, int ipalette) {
        int itable;

        //noinspection StatementWithEmptyBody
        for (itable = rgb % hTable.length;
             hTable[itable] != null;
             itable = ++itable % hTable.length
                )
            ;

        hTable[itable] = new ColorRecord(rgb, ipalette);
    }
}
