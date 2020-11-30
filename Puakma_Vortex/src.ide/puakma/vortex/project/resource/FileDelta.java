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
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;

public class FileDelta implements IFileDelta
{
  private int type;

  private int flags;

  private IFile file;

  private IPath fullPath;

  private IPath movedTo;

  private IPath movedFrom;

  private IPath projectPath;

  public static final FileDelta createDelta(IResourceDelta delta)
  {
    FileDelta ret = new FileDelta();
    
    // SETUP TYPE
    int type = delta.getKind();
    if(((type & IResourceDelta.ADDED) == IResourceDelta.ADDED)) {
      if((delta.getFlags() & IResourceDelta.MOVED_FROM) == IResourceDelta.MOVED_FROM) {
        ret.type = MOVED;
        ret.movedFrom = delta.getMovedFromPath();
        ret.movedTo = delta.getFullPath();
      }
      else {
        ret.type = ADDED;
      }
    }
    else if((type & IResourceDelta.REMOVED) == IResourceDelta.REMOVED) {
      if((delta.getFlags() & IResourceDelta.MOVED_TO) == IResourceDelta.MOVED_TO) {
        // WE MAKE THIS REMOVED JUST WHEN MOVING TO SOME OTHER PROJECT
        // IF THIS IS MOVE INSIDE THIS PROJECT, WE SHOULD FORGET THIS EVENT
        // BECAUSE THERE IS ONE MORE EVENT WHICH IS ADDED EVENT
        if(delta.getFullPath().segment(0).equals(delta.getMovedToPath().segment(0)) == true)
          return null;
        
        ret.movedFrom = delta.getFullPath();
        ret.movedTo = delta.getMovedToPath();
      }
      else {
        ret.type = REMOVED;
      }
    }
    else if((type & IResourceDelta.CHANGED) == IResourceDelta.CHANGED) {
      ret.type = CHANGED;
    }
    else
      return null;
    
    // NOW SETUP FLAGS
    int flags = delta.getFlags();
    if((flags & IResourceDelta.CONTENT) == IResourceDelta.CONTENT)
      ret.flags |= IResourceDelta.CONTENT;
    if((flags & IResourceDelta.MARKERS) == IResourceDelta.MARKERS)
      ret.flags |= IResourceDelta.MARKERS;
    if((flags & IResourceDelta.REPLACED) == IResourceDelta.REPLACED)
      ret.flags |= IResourceDelta.REPLACED;
    if((flags & IResourceDelta.ENCODING) == IResourceDelta.ENCODING)
      ret.flags |= IResourceDelta.ENCODING;
    
    ret.file = (IFile) delta.getResource();
    ret.fullPath = delta.getFullPath();
    ret.projectPath = delta.getProjectRelativePath();
    
    StringBuffer sb = new StringBuffer();
    writeDebugString(delta, sb);
    System.out.println(sb);
    return ret;
  }

  public int getType()
  {
    return type;
  }

  public IFile getFile()
  {
    return file;
  }

  public IPath getFullPath()
  {
    return fullPath;
  }
  
  public boolean isMovingFromDifferentProject()
  {
    if(this.getType() == MOVED) {
      return movedFrom.segment(0).equals(movedTo.segment(0)) == false;
    }
    
    return false;
  }

  public IPath getMovedFromPath()
  {
    return movedFrom;
  }

  public IPath getMovedToPath()
  {
    return movedTo;
  }

  public boolean isContentChange()
  {
    return ((flags & IResourceDelta.CONTENT) == IResourceDelta.CONTENT)
           || ((flags & IResourceDelta.REPLACED) == IResourceDelta.REPLACED);
  }

  public boolean isMarkersChange()
  {
    return (flags & IResourceDelta.MARKERS) == IResourceDelta.MARKERS;
  }

  public boolean isEncondingChange()
  {
    return (flags & IResourceDelta.ENCODING) == IResourceDelta.ENCODING;
  }

  public IPath getProjectPath()
  {
    return projectPath;
  }
  
  public String getProjectName()
  {
    return getProject().getName();
  }
  
  public IProject getProject()
  {
    return this.file.getProject();
  }

  public static void writeDebugString(IResourceDelta delta, StringBuffer buffer)
  {
    buffer.append(delta.getFullPath());
    buffer.append('[');
    switch(delta.getKind()) {
      case IResourceDelta.ADDED:
        buffer.append('+');
      break;
      case IResourceDelta.ADDED_PHANTOM:
        buffer.append('>');
      break;
      case IResourceDelta.REMOVED:
        buffer.append('-');
      break;
      case IResourceDelta.REMOVED_PHANTOM:
        buffer.append('<');
      break;
      case IResourceDelta.CHANGED:
        buffer.append('*');
      break;
      case IResourceDelta.NO_CHANGE:
        buffer.append('~');
      break;
      default:
        buffer.append('?');
      break;
    }
    buffer.append("]: {"); //$NON-NLS-1$
    int changeFlags = delta.getFlags();
    boolean prev = false;
    if((changeFlags & delta.CONTENT) != 0) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("CONTENT"); //$NON-NLS-1$
      prev = true;
    }
    if((changeFlags & delta.MOVED_FROM) != 0) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("MOVED_FROM(" + delta.getMovedFromPath() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
      prev = true;
    }
    if((changeFlags & delta.MOVED_TO) != 0) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("MOVED_TO(" + delta.getMovedToPath() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
      prev = true;
    }
    if((changeFlags & delta.OPEN) != 0) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("OPEN"); //$NON-NLS-1$
      prev = true;
    }
    if((changeFlags & delta.TYPE) != 0) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("TYPE"); //$NON-NLS-1$
      prev = true;
    }
    if((changeFlags & delta.SYNC) != 0) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("SYNC"); //$NON-NLS-1$
      prev = true;
    }
    if((changeFlags & delta.MARKERS) != 0) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("MARKERS"); //$NON-NLS-1$
      //writeMarkerDebugString(buffer);
      prev = true;
    }
    if((changeFlags & delta.REPLACED) != 0) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("REPLACED"); //$NON-NLS-1$
      prev = true;
    }
    if((changeFlags & delta.DESCRIPTION) != 0) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("DESCRIPTION"); //$NON-NLS-1$
      prev = true;
    }
    if((changeFlags & delta.ENCODING) != 0) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("ENCODING"); //$NON-NLS-1$
      prev = true;
    }
    buffer.append("}");
  }
  
  public static void writeDebugString(IFileDelta delta, StringBuffer buffer)
  {
    buffer.append('[');
    switch(delta.getType()) {
      case ADDED:
        buffer.append('+');
      break;
      case REMOVED:
        buffer.append('-');
      break;
      case CHANGED:
        buffer.append('*');
      break;
      case MOVED:
        buffer.append('>');
      break;
      default:
        buffer.append('?');
      break;
    }
    buffer.append("] ");
    buffer.append(delta.getFullPath());
    buffer.append(" {"); //$NON-NLS-1$
    boolean prev = false;
    if(delta.isContentChange()) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("CONTENT"); //$NON-NLS-1$
      prev = true;
    }
    if(delta.getType() == MOVED) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("FROM(" + delta.getMovedFromPath() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
      buffer.append(" | "); //$NON-NLS-1$
      buffer.append("TO(" + delta.getMovedToPath() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
      if(delta.isMovingFromDifferentProject()) {
        buffer.append(" | ");
        buffer.append("MOVING_DIFFERENT_PROJECT");
      }
      prev = true;
    }
    if(delta.isMarkersChange()) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("MARKERS"); //$NON-NLS-1$
      //writeMarkerDebugString(buffer);
      prev = true;
    }
    // TODO: should we also implement replace???
//    if(delta.isReplace()) {
//      if(prev)
//        buffer.append(" | "); //$NON-NLS-1$
//      buffer.append("REPLACED"); //$NON-NLS-1$
//      prev = true;
//    }
    if(delta.isEncondingChange()) {
      if(prev)
        buffer.append(" | "); //$NON-NLS-1$
      buffer.append("ENCODING"); //$NON-NLS-1$
      prev = true;
    }
    buffer.append("}");
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    writeDebugString(this, sb);
    return sb.toString();
  }
}
