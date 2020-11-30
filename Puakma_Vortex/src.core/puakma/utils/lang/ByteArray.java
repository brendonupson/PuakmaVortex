/*
 * Author: Martin Novak
 * Date:   Aug 10, 2005
 */
package puakma.utils.lang;

public class ByteArray
{
  private byte[] content;
  
  public ByteArray()
  {
  }
  
  public ByteArray(byte[] wrapped)
  {
    this.content = wrapped;
  }

  public byte[] getBytes()
  {
    return content == null ? new byte[0] : content;
  }
  
  public boolean isContentNull()
  {
    return content == null;
  }
  
  public void wrap(byte[] wrapped)
  {
    this.content = wrapped;
  }
  
  /**
   * This function copies data to internal buffer. This is not wrapping!
   * 
   * @param data is the data which will be wrapped
   */
  public void deepWrap(byte[] data)
  {
    this.content = data.clone();
  }

  public int length()
  {
    return content.length;
  }
}
