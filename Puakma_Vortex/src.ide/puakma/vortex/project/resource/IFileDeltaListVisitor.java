/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    15/10/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.project.resource;

import org.eclipse.core.resources.IProject;

public interface IFileDeltaListVisitor
{
  /**
   * Lists all projects we are interested in.
   */
  IProject[] listProjects(IFileDeltaListVisitor visitor);

  /**
   * Processes all resource deltas. Items in the list are {@link IFileDelta}.
   */
  public void acceptAllDeltas(IFileDelta[] deltas);
}
