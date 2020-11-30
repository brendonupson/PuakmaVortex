/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Sep 27, 2004
 *
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.rcp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import puakma.vortex.views.console.TornadoConsoleView;
import puakma.vortex.views.navigator.PuakmaResourceView;

/**
 * @author Martin Novak
 */
public class PuakmaDeveloperPerspective implements IPerspectiveFactory
{
  /**
   * Name of the perspective.
   */
  public static String PERSPECTIVE_ID = PuakmaDeveloperPerspective.class.getName();

  public void createInitialLayout(IPageLayout layout)
  {
    // String editorArea = layout.getEditorArea();
    // IFolderLayout folder = layout.createFolder("bottom", IPageLayout.BOTTOM,
    // (float)0.25, editorArea);
    // folder.addPlaceholder(PuakmaView.class.getName());
    // folder.addPlaceholder("org.eclipse.jdt.ui.PackageExplorer");
    // folder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
    //    
    // folder= layout.createFolder("left", IPageLayout.LEFT, (float)0.25,
    // editorArea);
    // folder.addPlaceholder(PuakmaView.class.getName());
    
    String editorArea = layout.getEditorArea();

    layout.addView(PuakmaResourceView.VIEW_ID, IPageLayout.LEFT, 0.3f,
                   IPageLayout.ID_EDITOR_AREA);
    
    
    IFolderLayout folder= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea);
    folder.addView("org.eclipse.pde.runtime.LogView");
    folder.addView(TornadoConsoleView.VIEW_ID);
    
//    folder.addPlaceholder(IPageLayout.ID_RES_NAV);
//    layout.addView(TornadoConsoleView.VIEW_ID, IPageLayout.BOTTOM, 0.75f,
//                   IPageLayout.ID_EDITOR_AREA);
    // layout.addView("org.eclipse.jdt.ui.PackageExplorer",IPageLayout.RIGHT,
    // 0.75f,IPageLayout.ID_EDITOR_AREA);
//    layout.addView("org.eclipse.pde.runtime.LogView", IPageLayout.BOTTOM, 0.75f,
//                   IPageLayout.ID_EDITOR_AREA);
    // layout.addView(IConsoleConstants.ID_CONSOLE_VIEW,IPageLayout.BOTTOM,
    // IPageLayout.RATIO_MAX,IPageLayout.ID_EDITOR_AREA);
  }
}
