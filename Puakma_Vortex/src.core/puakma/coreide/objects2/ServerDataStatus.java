package puakma.coreide.objects2;

/**
 * This class is used to save information about the status of design data/source
 * on the server.
 * 
 * @author Martin Novak
 */
public class ServerDataStatus
{
  private long sourceLen;

  private long dataLen;

  private long sourceCrc32;

  private long dataCrc32;

  private long time;

  private String author;

  /**
   * @return lenght of the source of the design object
   */
  public long getSourceLength()
  {
    return sourceLen;
  }

  public void setSourceLength(long len)
  {
    this.sourceLen = len;
  }

  /**
   * @return length of the data of the design object
   */
  public long getDataLength()
  {
    return dataLen;
  }

  public void setDataLength(long len)
  {
    this.dataLen = len;
  }

  public long getSourceCrc32()
  {
    return sourceCrc32;
  }

  public void setSourceCrc32(long crc)
  {
    this.sourceCrc32 = crc;
  }

  /**
   * @return CRC32 checksum for design object's data
   */
  public long getDataCrc32()
  {
    return dataCrc32;
  }

  public void setDataCrc32(long crc)
  {
    this.dataCrc32 = crc;
  }

  public boolean equals(Object obj)
  {
    if(obj instanceof ServerDataStatus) {
      ServerDataStatus status = (ServerDataStatus) obj;
      // NOTE THAT WE ACTUALLY DON'T CARE ABOUT TIME AND AUTHOR SINCE THE TIME
      // MIGHT FAIL, AND AUTHOR TOO IF THERE ARE TWO WITH THE SAME NAME
      boolean ret = status.dataCrc32 == dataCrc32 && status.sourceCrc32 == sourceCrc32
                    && status.dataLen == dataLen && status.sourceLen == sourceLen;
      return ret;
    }
    return false;
  }

  /**
   * @return time in miliseconds when the design object has been updated
   */
  public long getUpdateTime()
  {
    return time;
  }
  
  public void setUpdateTime(long time)
  {
    this.time = time;
  }
  
  /**
   * @return the name of the author of the design object
   */
  public String getAuthor()
  {
    return author;
  }
  
  public void setAuthor(String author)
  {
    this.author = author;
  }
}
