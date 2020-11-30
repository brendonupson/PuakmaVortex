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
package puakma.vortex.project;

import org.eclipse.core.resources.IProject;

import puakma.utils.lang.ArrayUtils;
import puakma.vortex.project.resource.IFileDeltaListVisitor;

public abstract class AbstractProjectFileVisitor implements IFileDeltaListVisitor
{
  private IProject[] projects;
  
  public AbstractProjectFileVisitor()
  {
    projects = new IProject[0];
  }
  
  public AbstractProjectFileVisitor(IProject[] projects)
  {
    setupProjects(projects);
  }
  
  public void setupProjects(IProject[] projects)
  {
    this.projects = (IProject[]) ArrayUtils.removeDuplicates(projects);
  }

  public IProject[] listProjects(IFileDeltaListVisitor visitor)
  {
    return projects;
  }
}
