/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    30/05/2006
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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import puakma.vortex.VortexPlugin;

public class UploaderPreferencePage extends FieldEditorPreferencePage implements
                                                                     IWorkbenchPreferencePage
{
  public UploaderPreferencePage()
  {
    super(GRID);

    setPreferenceStore(VortexPlugin.getDefault().getPreferenceStore());
    setDescription("Puakma Vortex upload system settings");
  }

  protected void createFieldEditors()
  {
    String labelText = "On rename of action class";
    int columns = 1;
    String[][] labelAndValues = {
        {"Automatically rename action", PreferenceConstants.UP_RENAME_AUTOMATICALLY},
        {"Ask user to rename action", PreferenceConstants.UP_RENAME_ASK_USER},
        {"Do nothing", PreferenceConstants.UP_RENAME_DO_NOTHING},
    };
    addField(new RadioGroupFieldEditor(PreferenceConstants.PREF_UPLOAD_RENAME_ACTION,
                                       labelText, columns, labelAndValues,
                                       getFieldEditorParent(), true));
  }

  public void init(IWorkbench workbench)
  {

  }

}
