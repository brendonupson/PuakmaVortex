/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 4, 2005
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;

public class JdtUtils
{
  public static int getCompilationUnitSeverity(ICompilationUnit unit) throws CoreException
  {
    IResource res = unit.getResource();
    return getResourceSeverity(res);
  }
  
  /**
   * This function get the problem severity of the resource. If there is no problem, -1 is
   * returned.
   * 
   * @param res is the resource to check
   * @return IMarker.SEVERITY_INFO, SEVERITY_WARNING, SEVERITY_ERROR or -1 if there is
   *         no problem.
   * @throws CoreException
   */
  public static int getResourceSeverity(IResource res) throws CoreException
  {
    int severity = -1;
    if(res != null && res.isAccessible()) {
      IMarker[] markers = res.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
      if(markers != null) {
        for(int i = 0; i < markers.length; ++i) {
          int tempSeverity = markers[i].getAttribute(IMarker.SEVERITY, -1);
          if(tempSeverity == IMarker.SEVERITY_ERROR)
            return IMarker.SEVERITY_ERROR;
          if(tempSeverity == IMarker.SEVERITY_WARNING)
            severity = IMarker.SEVERITY_WARNING;
          else {
            // SET INFO ONLY IF WARNING IS NOT SET
            if(severity != IMarker.SEVERITY_WARNING)
              severity = IMarker.SEVERITY_INFO;
          }
        }
      }
    }
    return 0;
  }
  
  /**
   * Get the highest severity for the package fragment.
   * @param fragment is the package to examine
   * @param isFlat if true then the subpackages markers are not counted
   * @return severity or -1 if there is no known severity there
   * @throws CoreException is something is bad
   */
  public static int getFragmentSeverity(IPackageFragment fragment, boolean isFlat) throws CoreException
  {
    if(isFlat) {
      // SO WE CAN CHECK ONLY ALL THE COMPILATION UNITS INSIDE THIS PACKAGE
      ICompilationUnit[] cu = fragment.getCompilationUnits();
      int severity = -1;
      for(int i = 0; i < cu.length; ++i) {
        int tempSeverity = JdtUtils.getCompilationUnitSeverity(cu[i]);
        if(tempSeverity == IMarker.SEVERITY_ERROR)
          return IMarker.SEVERITY_ERROR;
        else if(tempSeverity == IMarker.SEVERITY_WARNING)
          severity = IMarker.SEVERITY_WARNING;
        else if(tempSeverity == IMarker.SEVERITY_INFO) {
          if(severity != IMarker.SEVERITY_WARNING)
            severity = IMarker.SEVERITY_INFO;
        }
        // IGNORE NO SEVERITY THERE
      }
      
      return severity;
    }
    else {
      IResource res = fragment.getResource();
      return JdtUtils.getResourceSeverity(res);
    }
  }
}
