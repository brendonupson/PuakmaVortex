/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 21, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.views.navigator;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import puakma.coreide.objects2.ApplicationObject;
import puakma.utils.lang.StringUtil;
import puakma.vortex.controls.BaseTreeViewer;

/**
 * @author Martin Novak
 */
public class ApplicationTreeViewer extends BaseTreeViewer
{
  private InternalViewerFilter filter;
  private boolean flat;
  
  class InternalViewerFilter extends ViewerFilter
  {
    String internalFilter;

    public boolean select(Viewer viewer, Object parentElement, Object element)
    {
      if(internalFilter == null)
        return true;
      else {
        ApplicationObject ao = (ApplicationObject) AdapterUtils.getObject(element, ApplicationObject.class);
        if(ao != null) {
          if(StringUtil.indexOfSecondIgnoreCase(ao.getName(),internalFilter) == -1)
            return false;
          else
            return true;
        }
        else
          return true;
      }
    }

    public void setText(String filterText)
    {
      this.internalFilter = filterText;
    }
  }

  public ApplicationTreeViewer(Composite parent)
  {
    super(parent);
    initialize();
  }
  
  public void initialize()
  {
    // TODO: add some listener to preferences...
    boolean isFlat = true;
    final ApplicationTreeViewController controller = new ApplicationTreeViewController(this, isFlat);
    setContentProvider(controller);

    ApplicationTreeViewLabelProvider labelProvider = new ApplicationTreeViewLabelProvider(this);
    DecoratingLabelProvider decorator= new DecoratingLabelProvider(labelProvider,
            new EnhancedProblemsDecorator(this));
    setLabelProvider(decorator);
    setSorter(new ApplicationTreeViewSorter());
    setInput(controller.getRoot());
    
    enableDefaultDoubleClick(true);
    
    filter = new InternalViewerFilter();
    
    getTree().addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        controller.dispose();
      }
    });
  }
  
  
  public void filterText(String filterText)
  {
    try {
      getTree().setRedraw(false);
      if(filter.internalFilter != null)
        removeFilter(filter);

      if(filterText != null && filterText.length() > 0) {
        filter.setText(filterText);
        addFilter(filter);
      }
      else
        filter.internalFilter = null;
    }
    finally {
      getTree().setRedraw(true);
    }
  }
  
  public void setFlat(boolean isFlat)
  {
    this.flat = isFlat;
  }
}
