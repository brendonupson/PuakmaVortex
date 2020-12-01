package puakma.vortex.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import puakma.vortex.VortexPlugin;

public class ConsolePreferencesPage extends FieldEditorPreferencePage implements
                                                                     IWorkbenchPreferencePage
{
  public ConsolePreferencesPage()
  {
    super(GRID);
    setPreferenceStore(VortexPlugin.getDefault().getPreferenceStore());
    setDescription("Puakma Vortex console settings");
  }

  /**
   * Creates the field editors. Field editors are abstractions of the common GUI
   * blocks needed to manipulate various types of preferences. Each field editor
   * knows how to save and restore itself.
   */
  public void createFieldEditors()
  {
    addField(new ColorFieldEditor(PreferenceConstants.PREF_CONSOLE_FG_COLOR,
                                  "Console foreground colour", getFieldEditorParent()));
    addField(new ColorFieldEditor(PreferenceConstants.PREF_CONSOLE_BG_COLOR,
                                  "Console fackground colour", getFieldEditorParent()));
    
    addField(new BooleanFieldEditor(PreferenceConstants.CONSOLE_CLEAR_AFTER_EXECUTE,
                                    "Clear previous commands log before executing a new one",
                                    getFieldEditorParent()));
  }

  public void init(IWorkbench workbench)
  {
  }
}
