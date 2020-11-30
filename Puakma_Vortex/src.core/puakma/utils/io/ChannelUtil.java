/*
 * Author: Martin Novak
 * Date:   Feb 13, 2005
 */
package puakma.utils.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;


/**
 * Collection of utils for channels, and NIO.
 *
 * @author Martin Novak
 */
public class ChannelUtil
{
  /**
   * Copies one channel to another.
   *
   * @param dest is the destination channel
   * @param src is the source channel
   * @throws IOException
   */
  public static void copyChannel(WritableByteChannel dest, ReadableByteChannel src) throws IOException
  {
    ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
    
    while(src.read(buffer) != -1) {
      buffer.flip();
      
      while(buffer.hasRemaining())
        dest.write(buffer);
      
      buffer.clear();
    }
  }

  /**
   * @param dest
   * @param src
   * @throws IOException
   */
  public static void copyEncodeChannel(WritableByteChannel dest, ReadableByteChannel src) throws IOException
  {
    ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
    
    while(src.read(buffer) != -1) {
      buffer.flip();
      
      while(buffer.hasRemaining())
        dest.write(buffer);
      
      buffer.clear();
    }
  }
}
