//******************************************************************************
// Gif89Frame.java
//******************************************************************************
package de.marketmaker.iview.tools.pngtogiftask.gif;

import java.awt.Point;
import java.io.OutputStream;
import java.io.IOException;

//==============================================================================
/** First off, just to dispel any doubt, this class and its subclasses have
 *  nothing to do with GUI "frames" such as java.awt.Frame.  We merely use the
 *  term in its very common sense of a still picture in an animation sequence.
 *  It's hoped that the restricted context will prevent any confusion.
 *  <p>
 *  An instance of this class is used in conjunction with a Gif89Encoder object
 *  to represent and encode a single static image and its associated "control"
 *  data.  A Gif89Frame doesn't know or care whether it is encoding one of the
 *  many animation frames in a GIF movie, or the single bitmap in a "normal"
 *  GIF. (FYI, this design mirrors the encoded GIF structure.)
 *  <p>
 *  Since Gif89Frame is an abstract class we don't instantiate it directly, but
 *  instead create instances of its concrete subclasses, IndexGif89Frame and
 *  DirectGif89Frame.  From the API standpoint, these subclasses differ only
 *  in the sort of data their instances are constructed from.  Most folks will
 *  probably work with DirectGif89Frame, since it can be constructed from a
 *  java.awt.Image object, but the lower-level IndexGif89Frame class offers
 *  advantages in specialized circumstances.  (Of course, in routine situations
 *  you might not explicitly instantiate any frames at all, instead letting
 *  Gif89Encoder's convenience methods do the honors.)
 *  <p>
 *  As far as the public API is concerned, objects in the Gif89Frame hierarchy
 *  interact with a Gif89Encoder only via the latter's methods for adding and
 *  querying frames.  (As a side note, you should know that while Gif89Encoder
 *  objects are permanently modified by the addition of Gif89Frames, the reverse
 *  is NOT true.  That is, even though the ultimate encoding of a Gif89Frame may
 *  be affected by the context its parent encoder object provides, it retains
 *  its original condition and can be reused in a different context.)
 *  <p>
 *  The core pixel-encoding code in this class was essentially lifted from
 *  Jef Poskanzer's well-known <cite>Acme GifEncoder</cite>, so please see the
 *  <a href="../readme.txt">readme</a> containing his notice.
 *
 * @version 0.90 beta (15-Jul-2000)
 * @author J. M. G. Elliott (tep@jmge.net)
 * @see Gif89Encoder
 * @see DirectGif89Frame
 * @see IndexGif89Frame
 */
public abstract class Gif89Frame {

  //// Public "Disposal Mode" constants ////

  /** The animated GIF renderer shall decide how to dispose of this Gif89Frame's
   *  display area.
   * @see Gif89Frame#setDisposalMode
   */
  public static final int DM_UNDEFINED = 0;
  
  /** The animated GIF renderer shall take no display-disposal action.
   * @see Gif89Frame#setDisposalMode
   */  
  public static final int DM_LEAVE     = 1;
  
  /** The animated GIF renderer shall replace this Gif89Frame's area with the
   *  background color.
   * @see Gif89Frame#setDisposalMode
   */  
  public static final int DM_BGCOLOR   = 2;
  
  /** The animated GIF renderer shall replace this Gif89Frame's area with the
   *  previous frame's bitmap.
   * @see Gif89Frame#setDisposalMode
   */    
  public static final int DM_REVERT    = 3;

  //// Bitmap variables set in package subclass constructors ////
  int    theWidth = -1;
  int    theHeight = -1;
  byte[] ciPixels;

  //// GIF graphic frame control options ////
  private Point   thePosition = new Point(0, 0);
  private boolean isInterlaced;
  private int     csecsDelay;
  private int     disposalCode = DM_LEAVE;

  //----------------------------------------------------------------------------
  /** Set the position of this frame within a larger animation display space.
   *
   * @param p
   *   Coordinates of the frame's upper left corner in the display space.
   *   (Default: The logical display's origin [0, 0])
   * @see Gif89Encoder#setLogicalDisplay
   */
  public void setPosition(Point p)
  {
    thePosition = new Point(p);
  }   

  //----------------------------------------------------------------------------
  /** Set or clear the interlace flag.
   *
   * @param b
   *   true if you want interlacing.  (Default: false)
   */  
  public void setInterlaced(boolean b)
  {
    isInterlaced = b;
  }
 
  //----------------------------------------------------------------------------
  /** Set the between-frame interval.
   *
   * @param interval
   *   Centiseconds to wait before displaying the subsequent frame.
   *   (Default: 0)
   */    
  public void setDelay(int interval)
  {
    csecsDelay = interval;
  }

  //----------------------------------------------------------------------------
  /** Setting this option determines (in a cooperative GIF-viewer) what will be
   *  done with this frame's display area before the subsequent frame is
   *  displayed.  For instance, a setting of DM_BGCOLOR can be used for erasure
   *  when redrawing with displacement.
   *
   * @param code
   *   One of the four int constants of the Gif89Frame.DM_* series.
   *  (Default: DM_LEAVE)
   */   
  public void setDisposalMode(int code)
  {
    disposalCode = code;
  }

  //----------------------------------------------------------------------------
  Gif89Frame() {}  // package-visible default constructor

  //----------------------------------------------------------------------------
  abstract Object getPixelSource();  

  //----------------------------------------------------------------------------
  int getWidth() { return theWidth; }

  //----------------------------------------------------------------------------
  int getHeight() { return theHeight; }

  //----------------------------------------------------------------------------
  byte[] getPixelSink() { return ciPixels; } 

  //----------------------------------------------------------------------------
  void encode(OutputStream os, boolean epluribus, int color_depth,
              int transparent_index) throws IOException
  {
    writeGraphicControlExtension(os, epluribus, transparent_index);
    writeImageDescriptor(os);
    new GifPixelsEncoder(
      theWidth, theHeight, ciPixels, isInterlaced, color_depth
    ).encode(os);
  }

  //----------------------------------------------------------------------------
  private void writeGraphicControlExtension(OutputStream os, boolean epluribus,
                                            int itransparent) throws IOException
  {
    int transflag = itransparent == -1 ? 0 : 1;
    if (transflag == 1 || epluribus)   // using transparency or animating ?
    {
      os.write((int) '!');             // GIF Extension Introducer
      os.write(0xf9);                  // Graphic Control Label
      os.write(4);                     // subsequent data block size
      os.write((disposalCode << 2) | transflag); // packed fields (1 byte)
      Put.leShort(csecsDelay, os);  // delay field (2 bytes)
      os.write(itransparent);          // transparent index field
      os.write(0);                     // block terminator
    }  
  }

  //----------------------------------------------------------------------------
  private void writeImageDescriptor(OutputStream os) throws IOException
  {
    os.write((int) ',');                // Image Separator
    Put.leShort(thePosition.x, os);
    Put.leShort(thePosition.y, os);
    Put.leShort(theWidth, os);
    Put.leShort(theHeight, os);
    os.write(isInterlaced ? 0x40 : 0);  // packed fields (1 byte)
  }
}

