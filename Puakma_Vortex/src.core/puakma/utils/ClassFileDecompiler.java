/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 20, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Gets some stuff from java .class binary file. It doesn't get everything. Now it's
 * implemented only getting class and package name. If needed more - implement it!
 *
 * @author Martin Novak
 */
public class ClassFileDecompiler
{
  public static final int TYPE_UTF8 = 1;
  public static final int TYPE_INT = 3;
  public static final int TYPE_FLOAT = 4;
  public static final int TYPE_LONG = 5;
  public static final int TYPE_DOUBLE = 6;
  public static final int TYPE_CLASS = 7;
  public static final int TYPE_STRING = 8;
  public static final int TYPE_FIELDREF = 9;
  public static final int TYPE_METHODREF = 10;
  public static final int TYPE_IFACEMETHODREF = 11;
  public static final int TYPE_NAMEANDTYPE = 12;

  private short minorVersion;
  private short majorVersion;
  
  private int access;
  
  private ConstantField header[];
  private String className;
  private String packageName;
  private String parentClassName;
  
  /**
   * This class represents one field in class file header.
   *
   * @author Martin Novak
   */
  class ConstantField {
    int tag;
    Object data;
    String data2;
    int index1;
    int index2;

    public ConstantField(int tag)
    {
      this.tag = tag;
    }
    
  }

  /**
   * Initializes class decompilter with input stream. Then it starts parsing.
   * If in input stream is not class, or some error occurs, throws ClassFileDecompilerException
   *
   * @param is
   * @throws ClassFileDecompilerException
   */
  public void parse(InputStream is) throws ClassFileDecompilerException
  {
    DataInputStream dis = new DataInputStream(is);

    try {
      // read unique value
      int magic = dis.readInt();
      if(magic != 0xcafebabe)
        throw new ClassFileDecompilerException("Is not java class file. Invalid magic number");
      
      minorVersion = (short) dis.readUnsignedShort();
      majorVersion = (short) dis.readUnsignedShort();
      
      int constPoolSize = dis.readUnsignedShort();
      header = new ConstantField[constPoolSize];
      
      for(int i = 1; i < constPoolSize; ++i) {
        // read header tag
        int tag = dis.readUnsignedByte();
        header[i] = new ConstantField(tag);
        
        switch(tag) {
          case TYPE_UTF8:
            short len = (short) dis.readUnsignedShort();
            byte[] bytes = new byte[len];
            if(dis.read(bytes) == -1)
              throw new ClassFileDecompilerException("Reached end of file without reading string field");
            header[i].data = new String(bytes, "UTF8");
          break;
          case TYPE_INT:
            int j = dis.readInt();
            header[i].data = new Integer(j);
          break;
          case TYPE_FLOAT:
            int val = dis.readInt();
            float f = Float.intBitsToFloat(val);
            header[i].data = new Float(f);
          break;
          case TYPE_LONG: {
            int il;
            long l;
            l = dis.readInt() << 32;
            il = dis.readInt();
            l = l | il;
            header[i].data = new Long(l);
            ++i;
          }
          break;
          case TYPE_DOUBLE: {
            int il;
            long l;
            l = dis.readInt() << 32;
            il = dis.readInt();
            l = l | il;
            double d = Double.longBitsToDouble(l);
            header[i].data = new Double(d);
            ++i;
          }
          break;
          case TYPE_CLASS:
          case TYPE_STRING: {
            header[i].index1 = dis.readUnsignedShort();
          }
          break;
          case TYPE_FIELDREF:
          case TYPE_METHODREF:
          case TYPE_IFACEMETHODREF: {
            header[i].index1 = dis.readUnsignedShort();
            header[i].index2 = dis.readUnsignedShort(); // name_and_type_index
          }
          break;
          case TYPE_NAMEANDTYPE: {
            header[i].index1 = dis.readUnsignedShort();
            header[i].index2 = dis.readUnsignedShort(); // descriptor_index
          }
          break;
          default:
            throw new ClassFileDecompilerException("Unknown constant type value");
        }
      }
      
      for(int i = 1; i < constPoolSize; ++i) {
        if(header[i] != null) {
          switch(header[i].tag) {
            case TYPE_CLASS:
            case TYPE_STRING: {
              header[i].data = header[header[i].index1].data;
            }
            break;
          }
        }
      }
      
      //int access = dis.readUnsignedShort();
      int classIndex = dis.readUnsignedShort();
      int parentClassIndex = dis.readUnsignedShort();
      
      if(classIndex > header.length - 1)
        throw new ClassFileDecompilerException("Invalid header of class file - index of class name is invalid");
      if(parentClassIndex > header.length - 1)
        throw new ClassFileDecompilerException("Invalid header of class file - index of parent class name is invalid");
      
      // now parse class name
      String fullClassName = (String) header[classIndex].data;
      String parentClassName = (String) header[parentClassIndex].data;
      
      if(fullClassName == null || fullClassName.length() == 0)
        throw new ClassFileDecompilerException("Invalid class name - zero size");
      if(parentClassName == null || parentClassName.length() == 0)
        throw new ClassFileDecompilerException("Invalid parent class name - zero size");
      
      fullClassName = fullClassName.replace('/','.');
      int lastDot = fullClassName.lastIndexOf('.');
      if(lastDot != -1) {
        className = fullClassName.substring(lastDot + 1);
        packageName = fullClassName.substring(0,lastDot);
      }
      else {
        className = fullClassName;
        packageName = "";
      }
      
      this.parentClassName = parentClassName;
    }
    catch(IOException e) {
      throw new ClassFileDecompilerException("IO exception occured - " +
                                             e.getLocalizedMessage(), e);
    }
    finally {
      try { dis.close(); }
      catch(IOException e1) {  }
    }
  }
  /**
   * Returns access to the class (package, public, private...
   *
   * @return int with access
   */
  public int getAccess()
  {
    return access;
  }

  public String getClassName()
  {
    return className;
  }
  public short getMajorVersion()
  {
    return majorVersion;
  }
  public short getMinorVersion()
  {
    return minorVersion;
  }
  public String getPackageName()
  {
    return packageName;
  }
  
  public String getParentClassName()
  {
    return parentClassName;
  }
  
  /**
   * Checks class name for errors. This is without packages.
   */
  public static String checkClassName(String name)
  {
    if(name.length() == 0)
      return "Class name cannot be empty";
    char c = name.charAt(0);
    if(Character.isJavaIdentifierStart(c) == false)
      return "Class name cannot start with '" + c + "'";
    for(int i = 1; i < name.length(); ++i) {
      c = name.charAt(i);
      if(Character.isJavaIdentifierPart(c) == false)
        return "Class name cannot contain character '" + c + "'";
    }
      
    return null;
  }
}
