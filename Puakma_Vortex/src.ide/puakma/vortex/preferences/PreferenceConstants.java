package puakma.vortex.preferences;

import org.eclipse.swt.graphics.RGB;

/**
 * Constant definitions for plug-in preferences
 */
public interface PreferenceConstants
{
  /**
   * Tornado console view foreground color.
   */
  public static final String PREF_CONSOLE_FG_COLOR = "consoleFgColour";

  /**
   * Tornado console view background color.
   */
  public static final String PREF_CONSOLE_BG_COLOR = "consoleBgColour";

  public static final String CONSOLE_CLEAR_AFTER_EXECUTE = "consoleClearAfterExecute";

  public static final RGB CONSOLE_FG_COLOR_DEFAULT = new RGB(0, 255, 0);

  public static final RGB CONSOLE_BG_COLOR_DEFAULT = new RGB(0, 0, 0);

  public static final boolean CONSOLE_CLEAR_AFTER_EXECUTE_DEFAULT = true;

  /**
   * Tornado Navigator view default action on double click on database item.
   */
  public static final String PREF_DB_DOUBLECLICK_DEFAULT_ACTION = "dbDoubleClickDefaultAction";

  public static final String DB_DBLCLK_OPEN_SCHEMA = "dbDoubleClickOpenSchema";

  public static final String DB_DBLCLK_OPEN_QUERY = "dbDoubleClickOpenQuery";

  public static final String DB_DBLCLK_UNFOLD = "dbDoubleClickUnfold";

  public static final String DB_DBLCLK_OPEN_DB_SETTINGS = "dbDoubleClickOpenDbSettings";

  /**
   * Default action on renaming action class.
   */
  public static final String PREF_UPLOAD_RENAME_ACTION = "uploaderRenameAction";

  public static final String UP_RENAME_ASK_USER = "uploaderRenameAskUser";

  public static final String UP_RENAME_DO_NOTHING = "uploaderRenameDoNothing";

  public static final String UP_RENAME_AUTOMATICALLY = "uploaderRenameAutoRename";

  /**
   * Constant for printing out debug information to error log. Significantly
   * slows down Vortex.
   */
  public static final String PREF_DEBUG_PRINT = "debugPrint";
  
  public static final String PREF_DEBUG_LOG_TO_CONSOLE_ONLY = "debugLogToConsole";
  
  public static final String PREF_NAVIGATOR_USE_FLAT_PACKAGES = "useFlatPackages";
  
  /**
   * Design editor page upload directory default directory
   */
  public static final String PREF_DOEDITOR_DEFAULT_DIR = "DOEditorDefaultDir";
  
  /**
   * The name of the lastly selected connection preference
   */
  public static final String PREF_CONN_LAST_SELECTED = "connsLastSelection";

  /**
   * Enables check box in dialog for renaming class name with actions.
   */
  public static final String PREF_TOOGLE_RENAME_CLASS_WITH_ACTION = "renameClassWithAction";

  /**
   * Last file to which we have exported pmx application.
   */
  public static final String PREF_EXPORT_LAST_FILE = "exportLastFile";

  /**
   * Tells the pmx file export dialog if export sources should be checked by default.
   */
  public static final String PREF_EXPORT_SOURCES = "exportSources";

  /**
   * Last pmx file we imported
   */
  public static final String PREF_IMPORT_LAST_FILE = "importLastFile";

  /**
   * Last path used in new resource wizards
   */
  public static final String PREF_NEW_WIZARD_PATH = "newWizardPath";

  /**
   * If true then proxy is enabled for Vortex.
   * TODO: in 3.3 move to the common facilities
   */
  public static final String PREF_PROXY_ENABLE = "proxyEnabled";
  
  public static final String PREF_PROXY_HOST = "proxyHost";
  
  public static final String PREF_PROXY_PORT = "proxyPort";
  
  public static final String PREF_PROXY_USER = "proxyUser";
  
  public static final String PREF_PROXY_PWD = "proxyPwd";
  
  /**
   * Database tables generation wizard - selection of what to do with the
   * existing tables.
   */
  public static final String PREF_DBGEN_EXISTING_TABLES_ACTION = "dbGenExistingDbsAction";
  
  public static final int DBGEN_DROP_OLD = 0;
  
  public static final int DBGEN_CREATE_IF_NOT_EXIST = 1;
  
  /**
   * Database tables generation wizard - selection of what to do with some error
   * situations.
   */
  public static final String PREF_DBGEN_ERROR_HANDLING = "dbGenErrorHandling";
  
  public static final int DBGEN_ERROR_IGNORE = 0;
  
  public static final int DBGEN_ERROR_STOP = 1;
  
  public static final int DBGEN_ERROR_ROLLBACK = 2;
  
  /**
   * Boolean value determining whether is the top editor visible or not.
   */
  public static final String PREF_DBED_TOP_EDITOR_VISIBLE = "dbschemaTopEditorVisible";
  
  /**
   * Color value of db schema editor background
   */
  public static final String PREF_DBED_COLOR_BACKGROUND = "dbschemaColorBackground";

  public static final RGB DEF_DBED_COLOR_BACKGROUND = new RGB(245, 245, 245);

  /**
   * Color value of the db schema editor header left side (the header is gradient)
   */
  public static final String PREF_DBED_COLOR_TABLEHEAD_LEFT = "dbschemaColorTableHeadLeft";
  
  public static final RGB DEF_DBED_COLOR_TABLEHEAD_LEFT = new RGB(150, 165, 185);

  /**
   * Color value of the db schema editor header right side (the header is gradient)
   */
  public static final String PREF_DBED_COLOR_TABLEHEAD_RIGHT = "dbschemaColorTableHeadRight";
  
  public static final RGB DEF_DBED_COLOR_TABLEHEAD_RIGHT = new RGB(218, 226, 238);

  /**
   * Color value of the db schema editor body left side (the body background is gradient)
   */
  public static final String PREF_DBED_COLOR_TABLEBODY_LEFT = "dbschemaColorTableBodyLeft";

  public static final RGB DEF_DBED_COLOR_TABLEBODY_LEFT = new RGB(185, 198, 219);
  
  /**
   * Color value of the db schema editor body right side (the body background is gradient)
   */
  public static final String PREF_DBED_COLOR_TABLEBODY_RIGHT = "dbschemaColorTableBodyRight";
  
  public static final RGB DEF_DBED_COLOR_TABLEBODY_RIGHT = new RGB(218, 226, 238);
}
