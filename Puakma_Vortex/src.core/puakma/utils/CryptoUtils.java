/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Aug 10, 2005
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtils
{
  public static byte[] getMD5(byte[] input)
  {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
      md.update(input);
      return md.digest();
    }
    catch(NoSuchAlgorithmException e) {
      throw new IllegalStateException(e.getLocalizedMessage());
    }
  }
}
