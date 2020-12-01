package puakma.vortex.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import puakma.vortex.VortexPlugin;

public class NavigatorPreferencePage extends FieldEditorPreferencePage
                                    implements IWorkbenchPreferencePage
{
  public NavigatorPreferencePage()
  {
    super(GRID);
    setPreferenceStore(VortexPlugin.getDefault().getPreferenceStore());
    setDescription("Set some preferences for the Tornado Navigator view");
  }

  /**
   * Creates the field editors. Field editors are abstractions of the common GUI
   * blocks needed to manipulate various types of preferences. Each field editor
   * knows how to save and restore itself.
   */
  public void createFieldEditors()
  {
    String labelText = "Default action on double click on database";
    int columns = 1;
    String[][] labelAndValues = {
        {"Open query editor", PreferenceConstants.DB_DBLCLK_OPEN_QUERY},
        {"Open schema editor", PreferenceConstants.DB_DBLCLK_OPEN_SCHEMA},
        {"Open database connection settings", PreferenceConstants.DB_DBLCLK_OPEN_DB_SETTINGS},
        {"Unfold database", PreferenceConstants.DB_DBLCLK_UNFOLD},
    };

    addField(new RadioGroupFieldEditor(PreferenceConstants.PREF_DB_DOUBLECLICK_DEFAULT_ACTION, labelText,
                                       columns, labelAndValues, getFieldEditorParent(), true));
//    addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH,
//                                      "&Directory preference:", getFieldEditorParent()));
//    addField(new BooleanFieldEditor(PreferenceConstants.P_BOOLEAN,
//                                    "&An example of a boolean preference",
//                                    getFieldEditorParent()));
//
//    addField(new RadioGroupFieldEditor(PreferenceConstants.P_CHOICE,
//                                       "An example of a multiple-choice preference", 1,
//                                       new String[][] { { "&Choice 1", "choice1" },
//                                           { "C&hoice 2", "choice2" } },
//                                       getFieldEditorParent()));
//    addField(new StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:",
//                                   getFieldEditorParent()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench)
  {
  }
}
