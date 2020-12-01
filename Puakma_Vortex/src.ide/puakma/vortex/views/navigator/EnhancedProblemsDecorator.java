/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Nov 3, 2005
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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.ui.ProblemsLabelDecorator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IColorDecorator;
import org.eclipse.jface.viewers.IFontDecorator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import puakma.coreide.objects2.JavaObject;
import puakma.vortex.JdtUtils;
import puakma.vortex.VortexPlugin;
import puakma.vortex.preferences.PreferenceConstants;
import puakma.vortex.project.ProjectUtils;

public class EnhancedProblemsDecorator extends ProblemsLabelDecorator
                                       implements IFontDecorator, IColorDecorator
{
  private Font boldFont;
  private Viewer viewer;

  public EnhancedProblemsDecorator(Viewer viewer)
  {
    super();
    
    this.viewer = viewer;
    
    // COPY THE FONT, AND MAKE IT BOLD
    final Font font = getViewer().getControl().getFont();
    FontData[] fd = font.getFontData();
    boldFont = new Font(Display.getDefault(), fd[0].getName(), fd[0].getHeight(), fd[0].getStyle() | SWT.BOLD);
    getViewer().getControl().addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        boldFont.dispose();
      }
    });
  }
  
  private Viewer getViewer()
  {
    return viewer;
  }
  
  public Font decorateFont(Object element)
  {
    int severity = getElementSeverity(element);
    if(severity == IMarker.SEVERITY_ERROR)
      return boldFont;
//    System.out.println(element);
    return null;
  }

  /**
   * This function get the highest available element severity.
   *
   * @param element is the element we want to examine
   * @return severity for the element
   */
  private int getElementSeverity(Object element)
  {
    try {
      JavaObject jo = (JavaObject) AdapterUtils.getObject(element, JavaObject.class);
      if(jo != null) {
        ICompilationUnit unit = ProjectUtils.getCompilationUnit(jo);
        return JdtUtils.getCompilationUnitSeverity(unit);
      }
      
      if(element instanceof ICompilationUnit) {
        return JdtUtils.getCompilationUnitSeverity((ICompilationUnit)element);
      }
      
      if(element instanceof IPackageFragment) {
        IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
        boolean useFlatPackages = store.getBoolean(PreferenceConstants.PREF_NAVIGATOR_USE_FLAT_PACKAGES);
        return JdtUtils.getFragmentSeverity((IPackageFragment) element, useFlatPackages);
      }
    }
    catch(CoreException ex) {
      // DO NOTHING THERE
    }
    return 0;
  }

  public Color decorateForeground(Object element)
  {
    return null;
  }

  public Color decorateBackground(Object element)
  {
    return null;
  }

}
