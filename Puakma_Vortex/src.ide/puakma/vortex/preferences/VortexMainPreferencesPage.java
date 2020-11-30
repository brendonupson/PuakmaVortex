/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    21/04/2006
 * 
 * Copyright (c) 2006 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.vortex.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.widgets.FormText;

import puakma.vortex.VortexPlugin;
import puakma.vortex.swt.DialogBuilder2;

public class VortexMainPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
  protected Control createContents(Composite parent)
  {
    DialogBuilder2 builder = new DialogBuilder2(parent);
    Composite main = builder.createComposite();
    
    builder.createImage("icon120.gif", -1);
    
    StringBuffer sb = new StringBuffer();
    sb.append("<form>\n");
    sb.append("<p><span font=\"header\" color=\"header\">Puakma Vortex IDE</span></p>\n");
    sb.append("<p>Version: ");
    sb.append(VortexPlugin.getVersionNumberString());
    sb.append("</p>\n");
    sb.append("<p>Build: ");
    sb.append(VortexPlugin.getBuildNumberString());
    sb.append("</p>\n");
    sb.append("<p>Copyright (c) 2005, 2006 webWise Network Consultants Pty Ltd, <a href=\"http://www.wnc.net.au\">http://www.wnc.net.au</a></p>\n");
    sb.append("<br/>\n");
    sb.append("<p>Website: <a href=\"http://www.puakma.net\">http://www.puakma.net</a></p>\n");
    sb.append("<p>Info: <a href=\"mailto:info@puakma.net\">info@puakma.net</a></p>\n");
    sb.append("<br/>\n");
    
    sb.append("</form>\n");
    FormText ft = builder.createFormText(sb.toString());
    ft.setFont("header", JFaceResources.getHeaderFont());
    
    builder.closeComposite();
    builder.finishBuilder();
    return main;
  }

  public void init(IWorkbench workbench)
  {
    
  }
}
