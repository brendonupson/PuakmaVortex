package puakma.vortex.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;

import puakma.vortex.VortexPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{
  public void initializeDefaultPreferences()
  {
    IPreferenceStore store = VortexPlugin.getDefault().getPreferenceStore();
    
    PreferenceConverter.setDefault(store, PreferenceConstants.PREF_CONSOLE_FG_COLOR, PreferenceConstants.CONSOLE_FG_COLOR_DEFAULT);
    PreferenceConverter.setDefault(store, PreferenceConstants.PREF_CONSOLE_BG_COLOR, PreferenceConstants.CONSOLE_BG_COLOR_DEFAULT);
    store.setDefault(PreferenceConstants.CONSOLE_CLEAR_AFTER_EXECUTE, PreferenceConstants.CONSOLE_CLEAR_AFTER_EXECUTE_DEFAULT);
//    store.setDefault(PreferenceConstants.P_BOOLEAN, true);
//    store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
//    store.setDefault(PreferenceConstants.P_STRING, "Default value");

    store.setDefault(PreferenceConstants.PREF_DB_DOUBLECLICK_DEFAULT_ACTION, PreferenceConstants.DB_DBLCLK_OPEN_QUERY);

    store.setDefault(PreferenceConstants.PREF_UPLOAD_RENAME_ACTION, PreferenceConstants.UP_RENAME_ASK_USER);
    //store.setDefault(PreferenceConstants.DBED_FONT_NAME, "#")
    
    store.setDefault(PreferenceConstants.PREF_DEBUG_PRINT, false);
    store.setDefault(PreferenceConstants.PREF_DEBUG_LOG_TO_CONSOLE_ONLY, false);
    
    store.setDefault(PreferenceConstants.PREF_NAVIGATOR_USE_FLAT_PACKAGES, true);
    
    store.setDefault(PreferenceConstants.PREF_PROXY_ENABLE, false);
    store.setDefault(PreferenceConstants.PREF_PROXY_HOST, "");
    store.setDefault(PreferenceConstants.PREF_PROXY_PORT, 3128);
    store.setDefault(PreferenceConstants.PREF_PROXY_USER, "");
    store.setDefault(PreferenceConstants.PREF_PROXY_PWD, "");
    
    store.setDefault(PreferenceConstants.PREF_DBGEN_EXISTING_TABLES_ACTION, PreferenceConstants.DBGEN_DROP_OLD);
    store.setDefault(PreferenceConstants.PREF_DBGEN_ERROR_HANDLING, PreferenceConstants.DBGEN_ERROR_STOP);
    
    store.setDefault(PreferenceConstants.PREF_DBED_TOP_EDITOR_VISIBLE, true);
    PreferenceConverter.setDefault(store, PreferenceConstants.PREF_DBED_COLOR_BACKGROUND, PreferenceConstants.DEF_DBED_COLOR_BACKGROUND);
    PreferenceConverter.setDefault(store, PreferenceConstants.PREF_DBED_COLOR_TABLEHEAD_LEFT, PreferenceConstants.DEF_DBED_COLOR_TABLEHEAD_LEFT);
    PreferenceConverter.setDefault(store, PreferenceConstants.PREF_DBED_COLOR_TABLEHEAD_RIGHT, PreferenceConstants.DEF_DBED_COLOR_TABLEHEAD_RIGHT);
    PreferenceConverter.setDefault(store, PreferenceConstants.PREF_DBED_COLOR_TABLEBODY_LEFT, PreferenceConstants.DEF_DBED_COLOR_TABLEBODY_LEFT);
    PreferenceConverter.setDefault(store, PreferenceConstants.PREF_DBED_COLOR_TABLEBODY_RIGHT, PreferenceConstants.DEF_DBED_COLOR_TABLEBODY_RIGHT);
  }
}
