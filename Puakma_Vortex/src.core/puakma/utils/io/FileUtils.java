/*
 * Author: Martin Novak
 * Date:   10.6.2004
 */
package puakma.utils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.zip.CRC32;

/**
 * This class has some helper methods which manipulates with files on file system
 * and in the workspace.
 *
 * @author Martin Novak
 */
public class FileUtils
{
  /**
   * Checks if the directory in which should be the file exists and if not,
   * then create the directory with all necessary parent directories.
   * 
   * @param file is the file to which we check directory
   * @throws FileNotFoundException if the directory or some of the parent
   *                               directories cannot be created
   */
  public static void checkDirectoryForFile(File file) throws FileNotFoundException
  {
    File dir = new File(file.getParent());
//    IPath p = new Path(file.toString());
//    p = p.removeLastSegments(1);
//    File dir = p.toFile();
    if(dir.isDirectory() == false)
      if(dir.mkdirs() == false)
        throw new FileNotFoundException("Cannot create directory " + dir);
  }
  
  /**
   * Copies file from src to dest file.
   *
   * @param dest is the destination file
   * @param src us the source file
   * @throws IOException
   */
  public static void copyFile(File dest, File src) throws IOException
  {
    FileChannel sourceChannel = null;
    FileChannel destinationChannel = null;
    try {
      sourceChannel = new FileInputStream(src).getChannel();
      destinationChannel = new FileOutputStream(dest).getChannel();
      sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
    }
    finally {
      try { sourceChannel.close(); } catch(Exception e) {}
      try { destinationChannel.close(); } catch(Exception e) {}
    }
  }

  /**
   * Clears the directory content. If the directory doesn't exists, then create
   * new empty one. If some file/directory cannot be deleted, then it's ignored,
   * but it returns false.
   *
   * @throws IOException when passing file instead of directory
   */
  public static boolean clearDirectory(File directory) throws IOException
  {
    boolean ok = true;

    if(directory.isDirectory()) {
      String[] files = directory.list();
      for(int i = 0; i < files.length; ++i) {
        File file = new File(directory, files[i]);
        if(file.isDirectory()) {
          clearDirectory(file);
        }

        if(file.delete() == false)
          ok = false;
      }
    }
    else if(directory.isFile()) {
      throw new IOException("Passed argument is a file.");
    }
    else if(directory.exists() == false) {
      ok = directory.mkdirs();
    }

    return ok;
  }
  
  /**
   * Help function to calculate CRC32 checksum on the file.
   */
  public static long calculateCrc(File file) throws IOException
  {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      CRC32 crc = new CRC32();
      byte[] b = new byte[10000];
      int len;
      while((len = fis.read(b)) == 10000)
        crc.update(b);
      if(len != -1)
        crc.update(b, 0, len);
      return crc.getValue();
    }
    finally {
      if(fis != null) try { fis.close(); } catch(Exception ex) {  }
    }
  }
}
