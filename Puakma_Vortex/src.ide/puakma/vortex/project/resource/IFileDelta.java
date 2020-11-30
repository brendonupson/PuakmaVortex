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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public interface IFileDelta
{
  public static final int ERROR = 0;

  public static final int ADDED = 1;

  public static final int REMOVED = 2;

  public static final int CHANGED = 3;

  public static final int MOVED = 4;

  /**
   * Returns the type of the event.
   */
  public int getType();

  /**
   * Returns affected file.
   */
  public IFile getFile();

  /**
   * Returns the full path of the affected file within workspace.
   */
  public IPath getFullPath();

  /**
   * If this is {@link #MOVED} event, then returns path from which we have moved
   * the file. If this is not {@link #MOVED} event, returns null.
   */
  public IPath getMovedFromPath();

  /**
   * If this is {@link #MOVED} event, then this returns the destination file, if
   * not, returns null.
   */
  public IPath getMovedToPath();

  /**
   * Returns true if the event is moving file from some different project to this one.
   */
  public boolean isMovingFromDifferentProject();

  /**
   * Returns true if this event is {@link #CHANGED} event, and it is changing
   * the content of the file.
   */
  public boolean isContentChange();

  public boolean isMarkersChange();

  public boolean isEncondingChange();

  /**
   * Returns the path of the project to which affected resource belongs.
   */
  public IPath getProjectPath();

  /**
   * Returns name of the project affected by this resource change.
   */
  public String getProjectName();

  public IProject getProject();
}
