/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Feb 3, 2006
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

/**
 * Class containing some graphical algorithms.
 *
 * @author Martin Novak
 */
public class GraphicsUtils
{
  /**
   * This function checks if the newRec interferes with some table in the diagram
   * 
   * @param bounds is array with all tables in database which has been set already
   * @param bound is the Rectangle we want to check
   * @return true if some of the points in the table are inside some other table
   *              or some other table is inside this rectangle
   */
  public static boolean pointsInterfere(int[][] bounds, int[] bound)
  {
    for(int i = 0; i < bounds.length; ++i) {
      if(pointsInterfere(bounds[i], bound) || pointsInterfere(bound, bounds[i]))
        return true;
    }
    
    return false;
  }

  /**
   * This function checks if some of the corner points of b2 is inside b1. NOT vice versa!!! You have
   * to call this function once more to get b1 inside b2
   *
   * @param b1
   * @param b2
   * @return true if two rectangles interferes, false otherwise
   */
  static boolean pointsInterfere(int[] b1, int[] b2)
  {
    // CHECK IF SOME POINT OF b2 IS NOT IN b1
    
    // LEFT SIDE AT FIRST
    boolean leftInside  = b1[0] <= b2[0] && b2[0] <= b1[2];
    boolean rightInside = b1[0] <= b2[2] && b2[2] <= b1[2];
    boolean topInside   = b1[1] <= b2[1] && b2[1] <= b1[3];
    boolean botInside   = b1[1] <= b2[3] && b2[3] <= b1[3];
    
    if((leftInside && (topInside || botInside)) || (rightInside && (topInside || botInside)))
      return true;
    
    return false;
  }
}
