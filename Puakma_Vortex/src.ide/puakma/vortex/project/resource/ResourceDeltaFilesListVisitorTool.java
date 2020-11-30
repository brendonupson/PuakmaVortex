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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import puakma.utils.lang.ArrayUtils;
import puakma.vortex.VortexPlugin;

/**
 * Lists all files, and then This resource delta visitor doesn't care about phantoms.
 * 
 * @author Martin Novak
 */
public class ResourceDeltaFilesListVisitorTool implements IResourceChangeListener, IResourceDeltaVisitor
{
  /**
   * This class is a singleton managing all resources from here.
   */
  private static ResourceDeltaFilesListVisitorTool instance;

  private Map<IProject, IFileDeltaListVisitor[]> listenersMap = new HashMap<IProject, IFileDeltaListVisitor[]>();
  
  private IFileDeltaListVisitor[] listeners = new IFileDeltaListVisitor[0];

  public void addListener(IFileDeltaListVisitor visitor)
  {
    synchronized(this) {
      // CHECK THE LISTENERS
      for(int i = 0; i < listeners.length; ++i) {
        if(listeners[i].equals(visitor))
          return;
      }
      // ADD LISTENER
      listeners = (IFileDeltaListVisitor[]) ArrayUtils.append(listeners, visitor);
      IProject[] projects = visitor.listProjects(visitor);
      for(int i = 0; i < projects.length; ++i) {
        IFileDeltaListVisitor[] prjListeners = listenersMap.get(projects[i]);
        if(prjListeners != null)
          prjListeners = (IFileDeltaListVisitor[]) ArrayUtils.append(prjListeners, visitor);
        else
          prjListeners = new IFileDeltaListVisitor[] { visitor };
        
        listenersMap.put(projects[i], prjListeners);
      }
    }
  }
  
  public void removeListener(IFileDeltaListVisitor visitor)
  {
    synchronized(this) {
      listeners = (IFileDeltaListVisitor[]) ArrayUtils.remove(listeners, visitor);
      IProject[] projects = visitor.listProjects(visitor);
      for(int i = 0; i < projects.length; ++i) {
        IFileDeltaListVisitor[] prjListeners = listenersMap.get(projects[i]);
        if(prjListeners != null) {
          prjListeners = (IFileDeltaListVisitor[]) ArrayUtils.remove(prjListeners, visitor);
          if(prjListeners.length == 0)
            listenersMap.remove(projects[i]);
          else
            listenersMap.put(projects[i], prjListeners);
      }
      }
    }
  }

  public boolean visit(IResourceDelta delta) throws CoreException
  {
    List<Object> addedDeltas = new ArrayList<Object>();
    int type = delta.getResource().getType();
    
    // FILTER OUT PHANTOMS, AND TEAM PRIVATE RESOURCES
    IResource res = delta.getResource();
    if(res != null && (res.isTeamPrivateMember() || res.isPhantom()))
      return false;
    
    if(type == IResource.FILE) {
      addedDeltas.add(delta);
    }
    else if(type == IResource.FOLDER) {
      IResourceDelta[] children = delta.getAffectedChildren();
      for(int i = 0; i < children.length; ++i)
        visit(addedDeltas, children[i]);
    }
    else if(type == IResource.PROJECT) {
      IProject prj = (IProject) delta.getResource();
      IFileDeltaListVisitor[] listeners = listenersMap.get(prj);
      if(listeners != null) {
        IResourceDelta[] children = delta.getAffectedChildren();
        for(int i = 0; i < children.length; ++i)
          visit(addedDeltas, children[i]);
      }
    }
    else if(type == IResource.ROOT) {
      IResourceDelta[] children = delta.getAffectedChildren();
      for(int i = 0; i < children.length; ++i)
        visit(children[i]);
    }
    if(addedDeltas.size() == 0)
      return false;
    
    // NOW PASS THE RESOURCE DELTAS TO LISTENERS
    Iterator<Object> it = addedDeltas.iterator();
    while(it.hasNext()) {
      System.out.println(it.next().toString());
    }
    // SORT OUT DELTAS ACCORDING TO THE LISTENERS, BECAUSE LISTENERS WANT TO
    // LISTEN TO SOME SELECTED PROJECTS
    Map<Object, IFileDelta[]> mx = new HashMap<Object, IFileDelta[]>();
    it = addedDeltas.iterator();
    while(it.hasNext()) {
      IFileDelta dx = (IFileDelta) it.next();
      IProject project = dx.getProject();
      IFileDelta[] l = mx.get(project);
      if(l == null)
        l = new IFileDelta[] { dx };
      else
        l = (IFileDelta[]) ArrayUtils.append(l, dx);
      mx.put(project, l);
    }
    
    // NOW RUN THRU ALL GROUPS OF FILES, AND FIND APPROPRIATE VISITORS FOR THEM,
    // AND RUN IT ALL
    it = mx.keySet().iterator();
    while(it.hasNext()) {
      IProject project = (IProject) it.next();
      IFileDeltaListVisitor[] visitors;
      synchronized(this) {
        visitors = listenersMap.get(project);
      }
      if(visitors == null || visitors.length == 0)
        continue;
      IFileDelta[] dx = mx.get(project);
      if(dx == null || dx.length == 0)
        continue;
      for(int i = 0; i < visitors.length; ++i) {
        visitors[i].acceptAllDeltas(dx);
      }
    }

    return false;
  }

  private void visit(List<Object> affectedDeltas, IResourceDelta delta)
  {
    int type = delta.getResource().getType();
    if(type == IResource.FILE) {
      FileDelta d = FileDelta.createDelta(delta);
      if(d != null)
        affectedDeltas.add(d);
    }
    else if(type == IResource.FOLDER) {
      IResourceDelta[] children = delta.getAffectedChildren();
      for(int i = 0; i < children.length; ++i)
        visit(affectedDeltas, children[i]);
    }
    else
      throw new IllegalArgumentException("Invalid resource passed: " + delta.getResource());
  }

  public static void addVisitor(IFileDeltaListVisitor filesVisitor)
  {
    synchronized(ResourceDeltaFilesListVisitorTool.class) {
      if(instance == null) {
        instance = new ResourceDeltaFilesListVisitorTool();
        IWorkspace wspace = ResourcesPlugin.getWorkspace();
        wspace.addResourceChangeListener(instance);
      }
      
      instance.addListener(filesVisitor);
    }
  }

  public static void removeVisitor(IFileDeltaListVisitor filesVisitor)
  {
    synchronized(ResourceDeltaFilesListVisitorTool.class) {
      if(instance == null)
        return;
      
      instance.removeListener(filesVisitor);
    }
  }

  public void resourceChanged(IResourceChangeEvent event)
  {
    switch(event.getType()) {
      case IResourceChangeEvent.POST_CHANGE:
        IResourceDelta delta = event.getDelta();
        // ABORT THIS WHEN THE RESOURCE IS CLOSING / OPENING
        // TODO: THIS SHOULD ACTUALLY BE HANDLED, AND PASSED TO THE CLIENTS!!!
        if((delta.getFlags() & IResourceDelta.OPEN) == IResourceDelta.OPEN)
          return;
        IResourceDelta[] children = delta.getAffectedChildren();
        for(int i = 0; i < children.length; ++i) {
          try {
            children[i].accept(this, false);
          }
          catch(CoreException e) {
            VortexPlugin.log(e);
          }
        }
    }
  }
}
